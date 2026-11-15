ALTER TABLE notificacoes
    ALTER COLUMN agendamento_id DROP NOT NULL,
    ALTER COLUMN nome_servico DROP NOT NULL,
    ALTER COLUMN nome_barbeiro DROP NOT NULL;

UPDATE notificacoes
SET status = 'CANCELADA', erro_seguro = 'Canal WhatsApp desativado.'
WHERE canal = 'WHATSAPP' AND status IN ('PENDENTE', 'PROCESSANDO');

UPDATE notificacoes
SET status = 'CANCELADA', erro_seguro = 'Lembrete atualizado para 2 horas antes.'
WHERE tipo IN ('LEMBRETE_24H', 'LEMBRETE_1H', 'LEMBRETE_30MIN')
  AND status IN ('PENDENTE', 'PROCESSANDO');
