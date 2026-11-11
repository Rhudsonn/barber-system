ALTER TABLE agendamentos ALTER COLUMN cliente_id DROP NOT NULL;
ALTER TABLE agendamentos ADD COLUMN cliente_avulso_nome VARCHAR(255);
ALTER TABLE agendamentos ADD COLUMN cliente_avulso_telefone VARCHAR(255);
