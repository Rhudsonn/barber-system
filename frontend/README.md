# Frontend Barber System

O frontend contém o site React/Vite e o app React Native/Expo. Ambos consomem a mesma API Spring Boot; nenhum cliente conecta diretamente ao PostgreSQL. Configuração do banco/Docker e execução da API estão no [README da raiz](../README.md).

## Web e PWA

1. Copie `web/.env.example` para `web/.env`; mantenha `VITE_API_URL=http://localhost:8080` para o backend local.
2. Em `web/`, execute `npm install` e `npm run dev`; acesse `http://localhost:5173`.
3. Execute `npm run build` para validar TypeScript e gerar a versão de produção/PWA.

O painel web contempla cadastro/login, recuperação de senha, agenda e reagendamento, serviços, profissionais, clientes, dados da barbearia, horários, bloqueios, comissões e notificações, com opções conforme o perfil autenticado.

## Aplicativo mobile Android

### Requisitos

- Windows 10/11 de 64 bits.
- Node.js LTS (Node 20 recomendado) e npm.
- Android Studio; durante a instalação, mantenha selecionados Android SDK, Android SDK Platform-Tools e Android Virtual Device.
- JDK 21 para a API Spring Boot. O Android Studio inclui o Java necessário para o emulador; usando Expo Go, não é preciso instalar outro JDK para o app.
- Docker Desktop iniciado, para rodar o PostgreSQL local.

### Configuração inicial do Android Studio

1. Instale e abra o Android Studio. No assistente, escolha a instalação padrão e aceite instalar Android SDK, Platform-Tools e Emulator.
2. Abra **More Actions > SDK Manager**. Em **SDK Platforms**, instale **Android 15 / API 35** (ou a plataforma indicada pelo Expo caso o projeto atualize). Em **SDK Tools**, confirme **Android SDK Build-Tools**, **Android SDK Platform-Tools** e **Android Emulator**.
3. Em **More Actions > Virtual Device Manager > Create Device**, escolha um Pixel com Google Play, selecione uma imagem de sistema x86_64 (API 35) e conclua o download. Inicie o emulador pelo botão ▶. Um aparelho com Google Play facilita a instalação do Expo Go.
4. No Windows, configure `ANDROID_HOME` para a pasta do SDK, normalmente `%LOCALAPPDATA%\Android\Sdk`, e adicione `%ANDROID_HOME%\platform-tools` ao `Path`. Feche e reabra os terminais depois de alterar variáveis.
5. No emulador, abra a Play Store, entre (se solicitado) e instale **Expo Go**. Não é necessário abrir o projeto Android no Studio para usar o Expo Go.

### Subir os serviços locais

1. Na raiz do projeto, copie `.env.example` para `.env` e configure os valores locais obrigatórios, principalmente `JWT_SECRET` (mínimo 32 bytes) e as credenciais de bootstrap do administrador se o banco estiver vazio. Não compartilhe nem versione o `.env`.
2. Inicie a API conectada ao PostgreSQL do Supabase, conforme as instruções da raiz.
3. Inicie a API em outro terminal PowerShell, na raiz:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   A primeira execução baixa dependências Maven e aplica a migration do banco. A API deve responder em `http://localhost:8080`.
4. Em `frontend/mobile`, crie `frontend/mobile/.env` copiando `.env.example`. Para o AVD Android, use `EXPO_PUBLIC_API_URL=http://10.0.2.2:8080`. O endereço `10.0.2.2` encaminha do emulador para o `localhost` do computador.
5. Em um terceiro terminal:

   ```powershell
   cd frontend/mobile
   npm install
   npx expo start
   ```

6. Com o emulador aberto e desbloqueado, pressione `a` no terminal Expo para abrir o app no Expo Go. Também é possível escanear o QR code pelo Expo Go, desde que computador e aparelho estejam na mesma rede.

Se o app não alcançar a API, confirme que API e emulador estão rodando e que a variável usa `10.0.2.2` (não `localhost`). Depois de mudar `EXPO_PUBLIC_API_URL`, pare e reinicie o Expo. Em celular físico, use o IP local do computador, por exemplo `http://192.168.0.10:8080`, libere a porta 8080 no firewall e mantenha os dois dispositivos na mesma rede.

### Rodar a versão web

1. Na raiz, inicie PostgreSQL e API conforme os passos 1–3 acima.
2. Em `frontend/web`, copie `.env.example` para `.env` e mantenha `VITE_API_URL=http://localhost:8080`.
3. Execute:

   ```powershell
   cd frontend/web
   npm install
   npm run dev
   ```

4. Acesse `http://localhost:5173`. Para gerar a versão de produção, use `npm run build`.
## API e permissões

O login deve retornar `token`, `tipo`, `usuarioId` e `perfil`; a tela autenticada consulta `GET /login/meu-perfil`. O navegador aplica CORS, então `CORS_ALLOWED_ORIGINS` deve incluir a origem web. No app nativo, configure um endereço da API acessível pela rede do dispositivo.

O app mobile cobre login, consulta e criação/cancelamento de agendamentos, serviços e ações de atendimento compatíveis com cada perfil. As operações administrativas e configurações avançadas ficam na web.

## Validação

### Fechar uma comanda em um celular Android físico

O roteiro abaixo usa um PostgreSQL descartável, uma API de teste e o Expo Go. Instale uma versão do Expo Go compatível com o SDK 52, inicie o Docker Desktop e conecte o celular e o computador à mesma rede Wi-Fi. Use `ipconfig` para encontrar o IPv4 da conexão Wi-Fi do computador.

No PowerShell, a partir da raiz do projeto, instale as dependências e inicie o teste. Troque o IP do exemplo pelo seu.

```powershell
npm --prefix frontend/mobile ci
powershell -NoProfile -ExecutionPolicy Bypass -File .\frontend\mobile\testar-android.ps1 -IpComputador 192.168.1.100
```

Se o celular não alcançar o computador, confira a rede e o acesso às portas TCP 8081 (Metro) e 18080 (API) no perfil privado do Windows. O script imprime a URL da API e as credenciais sintéticas. Ao encerrar o Expo com Ctrl+C, a API e o banco descartável são encerrados.

1. Abra o QR code pelo Expo Go e entre com a conta de teste exibida no terminal.
2. Na **Agenda**, localize **#61 · Corte QA**, abra **Fechar comanda**, selecione **PIX** e **Barba QA**, e confirme. Confira se a agenda mostra **CONCLUIDO**.
3. Abra **Histórico** no mesmo atendimento. O evento de conclusão deve mostrar os dois serviços, PIX e **R$ 60,00**.
4. Abra **Comissões** e consulte a data do fechamento. O atendimento **#61** deve aparecer em dois lançamentos, com **R$ 20,00** de comissão no corte e **R$ 10,00** na barba.

O histórico segue o acesso ao atendimento. A aba de comissões aparece para barbeiros e administradores. O barbeiro vê os próprios lançamentos, e o administrador vê o relatório geral.

- Web: `npm run build`
- Mobile: `npm run typecheck`
- Backend e migrations: `./mvnw.cmd test` na raiz, com Docker em execução; os testes usam PostgreSQL temporário via Testcontainers.
- A validação ponta a ponta deve usar contas e dados sintéticos no PostgreSQL local, nunca dados de produção.
## Implantação do frontend web

Crie um serviço separado para o frontend web no ambiente Production da Railway. Configure o serviço com os seguintes valores:

| Campo | Valor |
| --- | --- |
| Builder | Railpack |
| Root Directory | `/frontend/web` |
| Build command | `npm run build` |
| Start command | `npm start` |
| Node.js | 22.x, definido em `package.json` |
| URL da API | `VITE_API_URL=https://api.zheiry.com.br` nas variáveis do serviço web |
| Healthcheck Path | `/` |

O modo padrão de `npm run build` lê `web/.env.production`. Para desenvolvimento, copie `web/.env.example` para `web/.env.local` e execute `npm ci` e `npm run dev` em `web`. O Vite embute `VITE_API_URL` no JavaScript. Alterações exigem novo build. Variáveis `VITE_*` são públicas.

O servidor Express serve `dist` em `0.0.0.0` na porta `PORT` fornecida pela Railway. Para testar localmente, execute `npm run build` e `npm start` em `web` e acesse `http://localhost:8080`. Defina outra `PORT` se o backend local estiver usando 8080. O servidor entrega os arquivos estáticos primeiro e usa `index.html` ao abrir rotas internas da SPA. Arquivos ausentes em `/assets/` e URLs de arquivos ausentes, como `/sw.js`, retornam 404.

Após autorização para publicar, adicione `app.zheiry.com.br` em Settings → Networking → Public Networking no serviço web. Crie os registros DNS exibidos pela Railway para o domínio, incluindo o CNAME de `app` e o TXT de verificação quando indicado. Aguarde a validação e confirme o certificado HTTPS. Se a interface solicitar Target Port, use a mesma porta definida por `PORT`.

No Railway, confirme que `CORS_ALLOWED_ORIGINS` inclui a origem exata `https://app.zheiry.com.br` e que `PASSWORD_RESET_URL` aponta para `https://app.zheiry.com.br/redefinir-senha`. O preflight de `POST /login` deve autorizar a origem e o cabeçalho `Content-Type`. O de rotas protegidas também deve permitir `Authorization`. Valide esses requisitos antes da publicação.

O build gera `manifest.webmanifest`, `sw.js`, o runtime Workbox e os ícones da PWA. Sirva esses arquivos na raiz do domínio via HTTPS. O precache contempla os arquivos estáticos, e os fluxos que consultam a API dependem de conexão. Após publicar, verifique o registro do service worker, a instalação em um dispositivo móvel e o carregamento direto de `/redefinir-senha` e `/painel/agendamentos`.

[Build na Railway](https://docs.railway.com/builds/build-configuration) · [Rede pública na Railway](https://docs.railway.com/networking/public-networking)
