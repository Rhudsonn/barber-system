ALTER TABLE comissoes DROP CONSTRAINT uk_comissao_agendamento;
ALTER TABLE agendamentos ADD COLUMN forma_pagamento VARCHAR(20);
ALTER TABLE agendamentos ADD COLUMN valor_total_comanda DECIMAL(12,2);
