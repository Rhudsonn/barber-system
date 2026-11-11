import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import type { Sessao } from './tipos'

const chave = 'barber-system-sessao'
type Contexto = { sessao: Sessao | null; entrar: (s: Sessao) => void; sair: () => void }
const SessaoContexto = createContext<Contexto | null>(null)

export function ProvedorSessao({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => { try { return JSON.parse(localStorage.getItem(chave) || 'null') as Sessao | null } catch { return null } })
  const entrar = (s: Sessao) => { localStorage.setItem(chave, JSON.stringify(s)); setSessao(s) }
  const sair = () => { localStorage.removeItem(chave); setSessao(null) }
  const valor = useMemo(() => ({ sessao, entrar, sair }), [sessao])
  return <SessaoContexto.Provider value={valor}>{children}</SessaoContexto.Provider>
}
export function useSessao() { const contexto = useContext(SessaoContexto); if (!contexto) throw new Error('Sessão fora do provedor'); return contexto }

export function SairAoExpirar() {
  const { sair } = useSessao()
  const navegar = useNavigate()
  return () => { sair(); navegar('/entrar') }
}
