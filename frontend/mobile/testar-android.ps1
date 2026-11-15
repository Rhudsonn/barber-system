param(
    [Parameter(Mandatory)]
    [ipaddress]$IpComputador,
    [int]$PortaApi = 18080
)

$ErrorActionPreference = 'Stop'
$raiz = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$nomeBanco = "barber-android-qa-$PID"
$senhaBanco = [guid]::NewGuid().ToString('N')
$senhaAdmin = [guid]::NewGuid().ToString('N')
$api = $null
$bancoIniciado = $false

try {
    docker run --detach --rm --name $nomeBanco -e "POSTGRES_PASSWORD=$senhaBanco" -e POSTGRES_DB=barber_qa -p 127.0.0.1::5432 postgres:16-alpine | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível iniciar o PostgreSQL. Confira se o Docker Desktop está em execução.' }
    $bancoIniciado = $true
    $portaBanco = (docker port $nomeBanco 5432 | Select-Object -First 1) -replace '^.*:', ''
    for ($i = 0; $i -lt 30; $i++) {
        docker exec $nomeBanco pg_isready -h 127.0.0.1 -U postgres -d barber_qa | Out-Null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 1
    }
    if ($LASTEXITCODE -ne 0) { throw 'O PostgreSQL de teste não ficou disponível.' }

    $config = @{
        SPRING_CONFIG_IMPORT = 'optional:file:./.tmp-build/android-qa-empty.properties'
        SPRING_DATASOURCE_URL = "jdbc:postgresql://127.0.0.1:${portaBanco}/barber_qa?sslmode=disable"
        SPRING_DATASOURCE_USERNAME = 'postgres'
        SPRING_DATASOURCE_PASSWORD = $senhaBanco
        JWT_SECRET = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
        APP_BOOTSTRAP_ADMIN_ENABLED = 'true'
        APP_BOOTSTRAP_ADMIN_NOME = 'Administrador QA'
        APP_BOOTSTRAP_ADMIN_TELEFONE = '11988887777'
        APP_BOOTSTRAP_ADMIN_EMAIL = 'admin@example.test'
        APP_BOOTSTRAP_ADMIN_PASSWORD = $senhaAdmin
        NOTIFICACOES_RESEND_ATIVO = 'false'
    }
    $api = Start-Job -ArgumentList $raiz, $IpComputador.ToString(), $PortaApi, $config -ScriptBlock {
        param($Raiz, $Endereco, $Porta, $Config)
        Set-Location -LiteralPath $Raiz
        foreach ($item in $Config.GetEnumerator()) {
            [Environment]::SetEnvironmentVariable($item.Key, $item.Value, 'Process')
        }
        $maven = @()
        if (Test-Path '.tmp-build/m2/org/springframework/boot') {
            $maven += '-Dmaven.repo.local=.tmp-build/m2'
        }
        $maven += @('spring-boot:run', "-Dspring-boot.run.arguments=--server.address=$Endereco --server.port=$Porta --spring.flyway.enabled=true --spring.jpa.hibernate.ddl-auto=validate")
        .\mvnw.cmd @maven
        if ($LASTEXITCODE -ne 0) { throw "A API encerrou com código $LASTEXITCODE." }
    }

    Write-Host 'Aguardando a API aplicar as migrações e preparar a conta de teste...'
    $pronta = $false
    $login = @{ email = 'admin@example.test'; senha = $senhaAdmin } | ConvertTo-Json -Compress
    for ($i = 0; $i -lt 150 -and $api.State -eq 'Running'; $i++) {
        try {
            $sessao = Invoke-RestMethod "http://${IpComputador}:$PortaApi/login" -Method Post -ContentType 'application/json' -Body $login -TimeoutSec 2
            if ($sessao.token) { $pronta = $true; break }
        } catch {
            Start-Sleep -Seconds 2
        }
    }
    if (!$pronta) {
        Receive-Job $api -Keep
        throw 'A API de teste não ficou disponível.'
    }

    $hoje = Get-Date -Format 'yyyy-MM-dd'
    @"
INSERT INTO barbeiros (id,nome,data_nascimento,telefone,especialidade,email,senha,nivel_acesso,status)
VALUES (41,'Barbeiro QA',DATE '1990-01-01','11911112222','Corte','barbeiro@example.test','fixture','BARBEIRO','ATIVO');
INSERT INTO servicos (id,nome,descricao,categoria,preco,porcentagem_barbearia,duracao_em_minutos,status)
VALUES (51,'Corte QA','Corte QA','CORTE',40,50,30,'ATIVO'),(52,'Barba QA','Barba QA','BARBA',20,50,15,'ATIVO');
INSERT INTO agendamentos (id,cliente_avulso_nome,barbeiro_id,servico_id,data_hora_inicio,data_hora_fim,status_agendamento,criado_em)
VALUES (61,'Cliente QA',41,51,TIMESTAMP '$hoje 12:00:00',TIMESTAMP '$hoje 12:30:00','EM_ANDAMENTO',LOCALTIMESTAMP);
"@ | docker exec -i $nomeBanco psql -U postgres -d barber_qa -v ON_ERROR_STOP=1
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao inserir o atendimento de teste.' }

    $env:EXPO_PUBLIC_API_URL = "http://${IpComputador}:$PortaApi"
    $env:REACT_NATIVE_PACKAGER_HOSTNAME = $IpComputador.ToString()
    Set-Location -LiteralPath $PSScriptRoot
    Write-Host "API: $env:EXPO_PUBLIC_API_URL | Login: admin@example.test | Senha: $senhaAdmin"
    Write-Host 'Abra o QR code no Expo Go compatível com SDK 52. Na Agenda, procure o atendimento #61.'
    npx.cmd --no-install expo start --lan --max-workers 2
    if ($LASTEXITCODE -ne 0) { throw 'O Expo não iniciou. Execute npm ci em frontend/mobile e tente novamente.' }
} finally {
    Set-Location -LiteralPath $raiz
    if ($api) {
        Stop-Job $api
        Remove-Job $api
    }
    if ($bancoIniciado) { docker stop $nomeBanco | Out-Null }
}
