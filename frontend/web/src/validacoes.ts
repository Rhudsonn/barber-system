export const padraoTelefone = String.raw`^\(?\d{2}\)?\s?9?\d{4}-?\d{4}$`
export const padraoSenha = String.raw`^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#_-])[A-Za-z\d@$!%*?&.#_-]{8,}$`

export const dicaTelefone = 'Informe DDD e número, com ou sem parênteses, espaço e hífen.'
export const dicaSenha = 'Use pelo menos 8 caracteres, com maiúscula, minúscula, número e símbolo.'
