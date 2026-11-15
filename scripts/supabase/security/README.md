# Provisionamento SQL de segurança da Zheiry

Estes são modelos para revisão e execução controlada, não migrations e não foram executados. Use um conjunto por projeto Supabase/barbearia. Nunca execute os scripts de produção apontando para staging, nem o contrário.

## Ordem proposta

1. Faça backup e confirme a restauração em um projeto isolado. Execute `00_inventory_read_only.sql` e salve os resultados sem incluir credenciais.
2. Confirme que a aplicação usa somente Spring/JDBC e que nenhum consumidor legítimo usa a Data API ou a role `service_role` para acessar estas tabelas.
3. Execute `01_create_roles.sql` como administrador do projeto, usando `psql -v ON_ERROR_STOP=1` se aplicável. O arquivo é transacional. Configure senhas diferentes para `barber_runtime` e `barber_migrator` por um canal seguro (por exemplo, prompt interativo `psql \\password` ou o secret manager). Não registre passwords em SQL, shell history, `.env.example` ou logs.
4. Execute `02_ownership_and_grants.sql` como administrador. Todas as alterações ficam em uma transação. Com `psql`, use `-v ON_ERROR_STOP=1`; rode cada arquivo inteiro, nunca linha a linha. Se houver erro, pare e execute `ROLLBACK` ou feche a sessão antes de continuar. Se a transferência de ownership falhar, pare. Não conceda superuser, `BYPASSRLS` ou membership privilegiado para contornar o erro.
5. Execute `03_default_privileges.sql` como leitura de pré-condição. Não altere defaults de `postgres`: defaults PostgreSQL não podem ser limitados a nomes específicos de objetos e uma revogação para o schema `public` também alcançaria objetos Supabase criados ali. Faça todas as futuras migrations da aplicação como `barber_migrator`.
6. Conecte diretamente como `barber_migrator` e execute `04_default_privileges_migrator.sql`. Essa transação só modifica defaults da role dedicada; não conceda membership ao administrador para alterá-los. Em qualquer erro, pare e confirme rollback.
7. Execute `05_enable_rls.sql` como `barber_migrator`. É uma transação única: em qualquer erro, pare e confirme rollback antes de continuar. Se o arquivo tiver sido aplicado anteriormente, não o repita sem antes conferir o inventário e as policies existentes.
8. Execute novamente `00_inventory_read_only.sql` e valide a API com `barber_runtime`. Pare na primeira divergência. Desative a Data API no painel somente após confirmar consumidores externos.

## Correção delta observada no staging

No staging `tmpgqgvnwosuidpogprw`, após conferir o inventário, `06_revoke_runtime_excess_privileges.sql` remove somente `INSERT` em `administrador` e `UPDATE` em `historico_agendamentos` de `barber_runtime`. O `UPDATE` em `recuperacoes_senha` é mantido porque o fluxo de redefinição usa `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`), que requer privilégio `UPDATE` no PostgreSQL. Execute esse delta transacional uma vez; não repita a criação de roles nem as alterações de ownership/grants de `02` quando o inventário já confirmar essas etapas.

## Roles e credenciais

- `barber_runtime` é usada somente pelo backend. Tem DML por tabela e `USAGE` nas sequences identity necessárias; não é owner e não pode executar DDL.
- `barber_migrator` é usada somente pelo job isolado de migrations e é owner dos objetos da aplicação para permitir migrations futuras. Não pode administrar roles ou bancos.
- O backend mantém Flyway desativado. As migrations V1–V7 não devem ser editadas ou reaplicadas; futuras migrations usam a credencial de `barber_migrator` fora do runtime da API.
- `FLYWAY_DB_USERNAME` e `FLYWAY_DB_PASSWORD` não pertencem ao `.env` do backend. O perfil de produção falha se Flyway estiver habilitado.

## RLS e escopo

Cada projeto pertence a uma única barbearia. As policies permitem ao `barber_runtime` acessar as linhas do projeto correspondente e ao `barber_migrator` manter o schema. Elas não fazem isolamento multi-tenant nem usam `auth.uid()`; a separação entre barbearias depende de projeto, endpoint e credenciais exclusivos.

RLS não substitui grants. As revogações de `PUBLIC`, `anon`, `authenticated` e `service_role` são limitadas às 13 tabelas da aplicação/histórico e às sequences identity dessas tabelas. Não há revogações em schemas, objetos internos, funções, views ou extensões do Supabase. `USAGE` no schema `public` é mantido para `barber_runtime`; `CREATE` é concedido a `barber_migrator`. ACLs de schema existentes são apenas inventariadas, pois sua revogação não seria limitada a objetos da aplicação.

Os default privileges de `supabase_admin` são gerenciados pelo Supabase e não devem ser alterados por SQL. O script `03` apenas inspeciona defaults de `postgres` e `barber_migrator`; mudar defaults de `postgres` no schema `public` alcançaria qualquer objeto criado por essa role nesse schema, não apenas objetos Zheiry. Objetos da aplicação devem ser criados pelo `barber_migrator`, com grants e policies explícitos em cada migration futura. Revise também a opção do painel que expõe automaticamente novas tabelas.

## Conexão da API

Configure `DB_USERNAME=barber_runtime`, `DB_URL` com `sslmode=require` e `SUPABASE_PROJECT_REF` específico desta implantação. O guard de produção valida role, projeto, SSL, Flyway desligado e ausência de overrides de URL/usuário/senha via Hikari.

Prefira conexão direta ou shared pooler em session mode para um backend persistente com Hikari. Para shared transaction mode (`6543`), o guard exige `prepareThreshold=0`; este modo não mantém estado de sessão entre transações. Copie host, porta e username do painel Supabase, sem montá-los manualmente.

## Limites e segurança

O inventário confirmou grants amplos nas 13 tabelas, mas estes scripts afetam somente as 12 tabelas da aplicação e `flyway_schema_history`. Não alteram funções, views, extensões ou objetos internos Supabase. Inspecione qualquer objeto adicional antes de revogar grants. `service_role` só é revogada nas tabelas da aplicação porque a arquitetura declarada não usa Data API; revalide isso antes de executar.

Backup/restore pode tornar o projeto indisponível durante a restauração. Senhas de roles customizadas precisam ser redefinidas após restore. Restaure primeiro em novo projeto, revalide `SUPABASE_PROJECT_REF` e configuração de staging, e só então planeje a troca do backend.
