import type { Sessao } from './tipos'

const base = import.meta.env.VITE_API_URL?.trim().replace(/\/+$/, '')
  || (import.meta.env.MODE === 'production' ? 'https://api.zheiry.com.br' : 'http://localhost:8080')

export class ErroRequisicao extends Error { constructor(message: string, public status: number) { super(message) } }

export async function requisicao<T>(rota: string, opcoes: RequestInit = {}, sessao?: Sessao | null): Promise<T> {
  const cabecalhos = new Headers(opcoes.headers)
  if (opcoes.body && !(opcoes.body instanceof FormData)) cabecalhos.set('Content-Type', 'application/json')
  cabecalhos.set('Accept', 'application/json')
  if (sessao?.token) cabecalhos.set('Authorization', `${sessao.tipo || 'Bearer'} ${sessao.token}`)
  let resposta: Response
  try { resposta = await fetch(`${base}${rota}`, { ...opcoes, headers: cabecalhos }) }
  catch { throw new ErroRequisicao('Não foi possível conectar à API. Confira o endereço e se o servidor está ativo.', 0) }
  if (resposta.status === 204) return undefined as T
  const texto = await resposta.text()
  let corpo: unknown
  try { corpo = texto ? JSON.parse(texto) : undefined } catch { corpo = texto }
  if (!resposta.ok) {
    const obj = corpo && typeof corpo === 'object' && !Array.isArray(corpo)
      ? corpo as Record<string, unknown>
      : undefined
    const erros = obj?.errors && typeof obj.errors === 'object'
      ? Object.values(obj.errors as Record<string, unknown>).filter((v): v is string => typeof v === 'string')
      : []
    const errosDeCampo = obj
      ? Object.entries(obj)
          .filter(([chave, valor]) => !['timestamp', 'status', 'error', 'path'].includes(chave) && typeof valor === 'string')
          .map(([, valor]) => valor as string)
      : []
    const detalhe = (obj?.mensagem as string | undefined)
      ?? (obj?.message as string | undefined)
      ?? (obj?.detail as string | undefined)
    const mensagemErro = detalhe
      || (erros.length ? erros.join(' · ') : '')
      || (errosDeCampo.length ? errosDeCampo.join(' · ') : '')
      || (typeof corpo === 'string' ? corpo : '')
    throw new ErroRequisicao(mensagemErro || `A solicitação falhou (${resposta.status}).`, resposta.status)
  }
  return corpo as T
}
