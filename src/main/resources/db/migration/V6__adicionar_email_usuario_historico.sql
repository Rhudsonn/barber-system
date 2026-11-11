-- A versão V5 aplicada no banco criou usuario_nome; o modelo atual registra e-mail.
-- IF NOT EXISTS também mantém instalações novas compatíveis com o V5 local.
ALTER TABLE historico_agendamentos
    ADD COLUMN IF NOT EXISTS usuario_email VARCHAR(255);
