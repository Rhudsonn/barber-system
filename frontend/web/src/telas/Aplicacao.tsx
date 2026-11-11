import { Navigate, Route, Routes } from 'react-router-dom'
import { ProvedorSessao, useSessao } from '../sessao'
import { PaginaInicial, Entrar, Cadastrar, RecuperarSenha, RedefinirSenha } from './Publicas'
import { Painel } from './Painel'

function Rotas() {
  const { sessao } = useSessao()
  return <Routes>
    <Route path="/" element={<PaginaInicial />} />
    <Route path="/entrar" element={sessao ? <Navigate to="/painel/inicio" replace /> : <Entrar />} />
    <Route path="/cadastro" element={<Cadastrar />} />
    <Route path="/recuperar-senha" element={<RecuperarSenha />} />
    <Route path="/redefinir-senha" element={<RedefinirSenha />} />
    <Route path="/painel/*" element={sessao ? <Painel /> : <Navigate to="/entrar" replace />} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
}

export function Aplicacao() { return <ProvedorSessao><Rotas /></ProvedorSessao> }
