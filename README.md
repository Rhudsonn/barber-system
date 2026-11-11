# Barber System

Sistema de agendamento com API Spring Boot, frontend React/Vite e app Expo/React Native. A aplicação usa PostgreSQL no Supabase, migrations Flyway e validação do schema com Hibernate.

## Banco de dados Supabase

1. Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no `.env` a partir do `.env.example`; mantenha credenciais reais somente no `.env`, que não é versionado. `JWT_SECRET` deve ser texto aleatório com pelo menos 32 bytes.
2. Inicie a API. Flyway aplicará migrations em `src/main/resources/db/migration`; depois Hibernate verificará o schema com `ddl-auto=validate`.

No banco vazio, configure também `BOOTSTRAP_ADMIN_ENABLED=true`, `BOOTSTRAP_ADMIN_NOME`, `BOOTSTRAP_ADMIN_TELEFONE`, `BOOTSTRAP_ADMIN_EMAIL` e `BOOTSTRAP_ADMIN_PASSWORD` na configuração do IntelliJ (senha com pelo menos 12 caracteres). A API cria o administrador inicial apenas se não houver nenhum. Depois faça login na web e cadastre a barbearia em **Sobre**; configure horários e serviços. O bootstrap permanece desativado por padrão.

## Executar pelo IntelliJ IDEA

1. Abra a raiz que contém `pom.xml` e configure o JDK 21 em **File > Project Structure > Project SDK**.
2. Confira as variáveis do Supabase no `.env`.
3. Em **Run > Edit Configurations**, selecione **Barber System API** e confirme que o diretório de trabalho é a raiz que contém `.env` e `pom.xml`. A configuração Run lê as variáveis de `.env` automaticamente. No primeiro uso do banco vazio, configure nele as variáveis do bootstrap descritas acima. Não versione credenciais.
4. Rode **Barber System API**; a API estará em `http://localhost:8080`.
5. Rode **Barber System Web**; o site estará em `http://localhost:5173`.

Se a porta 8080 estiver ocupada no Windows, verifique o processo com `Get-NetTCPConnection -State Listen -LocalPort 8080`. Confirme qual aplicação é antes de encerrá-la.

## Executar pelo terminal integrado do IntelliJ

Na raiz, com `.env` configurado, rode:

```powershell
./mvnw.cmd spring-boot:run
```

Em outro terminal:

```powershell
cd frontend/web
npm install
npm run dev
```

Para conferir o build web, rode `npm run build` em `frontend/web`.

## Mobile (Android)

O passo a passo completo de instalação do Android Studio/AVD, Expo Go, variáveis de ambiente e execução está em [frontend/README.md](frontend/README.md#aplicativo-mobile-android). A API e o PostgreSQL devem estar rodando; no emulador, o app aponta para `http://10.0.2.2:8080`.
## Testes

Os testes backend usam H2 temporário e não acessam o PostgreSQL local. Rode `./mvnw.cmd test` na raiz. O schema real do PostgreSQL é controlado pelas migrations Flyway; não altere o schema manualmente para contornar falhas de validação.

### Checklist manual antes de produção

- [ ] Confirmar que Flyway aplicou V1 no Supabase sem erro.
- [ ] Confirmar cadastro, login, logout e recuperação de senha com conta/e-mail sintéticos.
- [ ] Verificar acesso autorizado e negado para cliente, barbeiro e administrador em agenda, bloqueios, perfil e relatórios.
- [ ] Criar uma reserva válida; tentar horário passado, fora do funcionamento, bloqueado e já ocupado; confirmar que a API rejeita todos os casos inválidos.
- [ ] Fazer duas tentativas simultâneas no mesmo barbeiro/horário; somente uma pode ser confirmada.
- [ ] Criar/editar/remover horários e bloqueios; confirmar atualização após recarregar a web.
- [ ] Testar reagendamento, cancelamento, início, conclusão e não comparecimento com transições válidas e inválidas.
- [ ] Conferir catálogo, equipe, clientes, dados da barbearia, comissões e preferências/histórico de notificações por papel.
- [ ] Testar web em `http://localhost:5173` e app Expo no emulador Android apontando à mesma API e aos mesmos dados locais.
- [ ] Manter e-mail/WhatsApp desativados até configurar credenciais de teste dos provedores; validar o estado apresentado ao administrador.

Mais detalhes de web/mobile: [frontend/README.md](frontend/README.md).

## Documentação em PDF

- [Documentação técnica](docs/01-documentacao-tecnica.pdf)
- [Requisitos do sistema](docs/02-requisitos-do-sistema.pdf)
- [Diagrama de classes](docs/03-diagrama-de-classes.pdf)
- [Lógica das funcionalidades](docs/04-logica-das-funcionalidades.pdf)
- [Documentação do frontend](docs/05-documentacao-frontend.pdf)
