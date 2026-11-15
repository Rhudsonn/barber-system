import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { ArrowRight, CalendarDays, Check, Clock3, MessageCircle, Scissors, ShieldCheck, Sparkles } from 'lucide-react'
import { toast } from 'sonner'
import { requisicao, ErroRequisicao } from '../api'
import { useSessao } from '../sessao'
import type { BarbeariaPublica, Servico } from '../tipos'
import { Botao, Campo, Cartao } from '../componentes/ui'
import { dicaSenha, dicaTelefone, padraoSenha, padraoTelefone } from '../validacoes'
import { linkWhatsAppBrasileiro } from '../whatsapp'

function Fundo({ children }: { children: React.ReactNode | ((barbearia: BarbeariaPublica | null) => React.ReactNode) }) {
  const [barbearia, setBarbearia] = useState<BarbeariaPublica | null>(null)
  useEffect(() => { void requisicao<BarbeariaPublica>('/barbearias/principal').then(setBarbearia).catch(() => undefined) }, [])
  useEffect(() => { if (barbearia?.nomeFantasia) document.title = barbearia.nomeFantasia }, [barbearia?.nomeFantasia])
  return <div className="pagina-publica"><header className="cabecalho-publico"><Link to="/" className="marca">{barbearia?.logoUrl ? <img className="marca-logo" src={barbearia.logoUrl} alt="Logo da barbearia"/> : <span className="marca-simbolo"><Scissors size={19}/></span>}<span className="marca-nome">{barbearia?.nomeFantasia || 'Barbearia'}</span></Link><nav><Link to="/entrar">Entrar</Link><Link className="botao botao-primario" to="/cadastro">Criar conta</Link></nav></header>{typeof children === 'function' ? children(barbearia) : children}<footer className="rodape-publico">© {new Date().getFullYear()} {barbearia?.nomeFantasia || 'Barbearia'} · Cuidado em cada detalhe.</footer></div>
}

export function PaginaInicial() {
  const [servicos, setServicos] = useState<Servico[]>([])
  useEffect(() => { void requisicao<Servico[]>('/servicos/lista/ativos').then(setServicos).catch(() => undefined) }, [])
  return <Fundo>{barbearia => <main><section className="hero"><div className="hero-selo"><Sparkles size={14}/> EXPERIÊNCIA BARBER</div><h1>Seu próximo corte<br/>começa <em>aqui.</em></h1><p>Agende sem complicação e tenha mais tempo para o que realmente importa. Cuidado, estilo e pontualidade em um só lugar.</p><div className="hero-acoes"><Link className="botao botao-primario botao-grande" to="/entrar">Agendar horário <ArrowRight size={17}/></Link><Link className="botao botao-secundario botao-grande" to="/cadastro">Criar minha conta</Link></div><div className="hero-destaques"><span><Check/> Profissionais experientes</span><span><Check/> Agenda em tempo real</span></div></section>
    <section className="secao-publica"><div className="cabecalho-secao"><div><span className="sobrelinha">CUIDADO SOB MEDIDA</span><h2>Serviços em destaque</h2></div><Link to="/entrar">Ver agenda <ArrowRight size={15}/></Link></div>{servicos.length ? <div className="grade-servicos">{servicos.slice(0, 3).map(s => <Cartao key={s.id}><span className="icone-card"><Scissors size={19}/></span><h3>{s.nome}</h3><p>{s.descrica || s.categoria}</p><div className="servico-rodape"><span>{s.duracaoEmMinutos} min</span><strong>{moeda(s.preco)}</strong></div></Cartao>)}</div> : <Cartao className="sobre-barbearia"><span className="icone-card"><CalendarDays/></span><h3>Encontre seu melhor horário</h3><p>Consulte os serviços e horários disponíveis ao entrar na sua conta.</p><Link className="link-roxo" to="/entrar">Acessar agenda <ArrowRight size={15}/></Link></Cartao>}</section>
    <section className="sobre-publica"><div><span className="sobrelinha">A CASA</span><h2>{barbearia?.nomeFantasia || 'Estilo com identidade.'}</h2><p>{barbearia?.descricao || 'Um espaço pensado para você cuidar do visual com calma, qualidade e atenção aos detalhes.'}</p><div className="info-linha"><Clock3 size={17}/><span>{barbearia?.endereco ? `${barbearia.endereco}${barbearia.cidade ? ` · ${barbearia.cidade}` : ''}` : 'Agendamentos simples, rápidos e organizados.'}</span></div>{linkWhatsAppBrasileiro(barbearia?.whatsapp)&&<a className="botao botao-secundario" href={linkWhatsAppBrasileiro(barbearia?.whatsapp)} target="_blank" rel="noopener noreferrer" aria-label="Conversar pelo WhatsApp com a barbearia"><MessageCircle size={18}/> Conversar pelo WhatsApp</a>}</div><div className="sobre-badge"><ShieldCheck size={32}/><strong>Seu tempo<br/>bem cuidado.</strong><span>Agendamento seguro e organizado.</span></div></section>
  </main>}</Fundo>
}

export function Entrar() {
  const [email, setEmail] = useState(''); const [senha, setSenha] = useState(''); const [carregando, setCarregando] = useState(false)
  const { entrar } = useSessao(); const navegar = useNavigate()
  async function enviar(e: FormEvent) { e.preventDefault(); setCarregando(true); try { const sessao = await requisicao<{ token: string; tipo: string; usuarioId: number; perfil: 'CLIENTE' | 'BARBEIRO' | 'ADMINISTRADOR' }>('/login', { method: 'POST', body: JSON.stringify({ email, senha }) }); if (!sessao.usuarioId || !sessao.perfil) throw new Error('A API precisa retornar usuarioId e perfil após o login. Atualize o backend.'); entrar(sessao); navegar('/painel/inicio'); toast.success('Bem-vindo de volta!') } catch (e) { toast.error(e instanceof Error ? e.message : 'Não foi possível entrar.') } finally { setCarregando(false) } }
  return <Fundo><main className="formulario-publico"><div className="formulario-intro"><span className="sobrelinha">BEM-VINDO DE VOLTA</span><h1>Entre na sua conta</h1><p>Acesse sua agenda e continue de onde parou.</p></div><Cartao><form onSubmit={enviar} className="formulario"><Campo rotulo="E-mail" type="email" autoComplete="email" required value={email} onChange={e => setEmail(e.target.value)} placeholder="voce@email.com"/><Campo rotulo="Senha" type="password" autoComplete="current-password" required value={senha} onChange={e => setSenha(e.target.value)} placeholder="Sua senha"/><div className="esqueci"><Link to="/recuperar-senha">Esqueci minha senha</Link></div><Botao type="submit" carregando={carregando} className="botao-largo">Entrar na conta</Botao><p className="texto-centro">Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link></p></form></Cartao></main></Fundo>
}

export function Cadastrar() {
  const [form, setForm] = useState({ nome: '', dataNascimento: '', telefone: '', email: '', senha: '', confirmaSenha: '' }); const [carregando, setCarregando] = useState(false); const navegar = useNavigate()
  async function enviar(e: FormEvent) { e.preventDefault(); if (form.senha !== form.confirmaSenha) { toast.error('As senhas não coincidem.'); return } setCarregando(true); try { await requisicao('/clientes', { method: 'POST', body: JSON.stringify(form) }); toast.success('Conta criada. Agora você pode entrar.'); navegar('/entrar') } catch (e) { toast.error(e instanceof Error ? e.message : 'Não foi possível criar a conta.') } finally { setCarregando(false) } }
  return <Fundo><main className="formulario-publico"><div className="formulario-intro"><span className="sobrelinha">COMECE AGORA</span><h1>Crie sua conta</h1><p>Cadastre-se para consultar serviços e reservar seu horário.</p></div><Cartao><form className="formulario formulario-grade" onSubmit={enviar}><Campo rotulo="Nome completo" required minLength={3} value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })}/><Campo rotulo="Data de nascimento" type="date" required value={form.dataNascimento} onChange={e => setForm({ ...form, dataNascimento: e.target.value })}/><Campo rotulo="Telefone" type="tel" required pattern={padraoTelefone} title={dicaTelefone} placeholder="(11) 99999-9999" value={form.telefone} onChange={e => setForm({ ...form, telefone: e.target.value })}/><Campo rotulo="E-mail" type="email" required value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}/><Campo rotulo="Senha" type="password" autoComplete="new-password" required minLength={8} pattern={padraoSenha} title={dicaSenha} value={form.senha} onChange={e => setForm({ ...form, senha: e.target.value })}/><Campo rotulo="Confirme a senha" type="password" autoComplete="new-password" required value={form.confirmaSenha} onChange={e => setForm({ ...form, confirmaSenha: e.target.value })}/><Botao className="botao-largo coluna-total" type="submit" carregando={carregando}>Criar conta</Botao><p className="texto-centro coluna-total">Já tem cadastro? <Link to="/entrar">Entrar</Link></p></form></Cartao></main></Fundo>
}

export function RecuperarSenha() {
  const [email, setEmail] = useState(''); const [carregando, setCarregando] = useState(false)
  async function enviar(e: FormEvent) { e.preventDefault(); setCarregando(true); try { const r = await requisicao<{ mensagem: string }>('/login/esqueci-senha', { method: 'POST', body: JSON.stringify({ email }) }); toast.success(r.mensagem || 'Se o e-mail estiver cadastrado, você receberá instruções.'); } catch (e) { toast.error(e instanceof ErroRequisicao ? e.message : 'Não foi possível solicitar a recuperação.') } finally { setCarregando(false) } }
  return <Fundo><main className="formulario-publico"><div className="formulario-intro"><span className="sobrelinha">RECUPERAÇÃO SEGURA</span><h1>Esqueceu sua senha?</h1><p>Informe seu e-mail e enviaremos as instruções disponíveis para sua conta.</p></div><Cartao><form className="formulario" onSubmit={enviar}><Campo rotulo="E-mail" type="email" required value={email} onChange={e => setEmail(e.target.value)}/><Botao type="submit" carregando={carregando} className="botao-largo">Solicitar recuperação</Botao><p className="texto-centro"><Link to="/entrar">Voltar para entrar</Link></p></form></Cartao></main></Fundo>
}

export function RedefinirSenha() {
  const [params] = useSearchParams()
  const [token, setToken] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get('token') || params.get('token') || '')
  const [novaSenha, setSenha] = useState(''); const [confirmaNovaSenha, setConfirma] = useState(''); const [carregando, setCarregando] = useState(false); const navegar = useNavigate()
  useEffect(() => {
    if (!token || (!window.location.hash && !window.location.search)) return
    const url = new URL(window.location.href)
    url.hash = ''
    url.searchParams.delete('token')
    window.history.replaceState(window.history.state, '', `${url.pathname}${url.search}`)
  }, [token])
  async function enviar(e: FormEvent) { e.preventDefault(); if (novaSenha !== confirmaNovaSenha) { toast.error('As senhas não coincidem.'); return } setCarregando(true); try { await requisicao('/login/redefinir-senha', { method: 'POST', body: JSON.stringify({ token, novaSenha, confirmaNovaSenha }) }); toast.success('Senha atualizada.'); navegar('/entrar') } catch (e) { toast.error(e instanceof ErroRequisicao ? e.message : 'Não foi possível redefinir a senha.') } finally { setCarregando(false) } }
  return <Fundo><main className="formulario-publico"><div className="formulario-intro"><span className="sobrelinha">NOVA SENHA</span><h1>Redefina seu acesso</h1><p>Use o token recebido e escolha uma senha forte.</p></div><Cartao><form className="formulario" onSubmit={enviar}><Campo rotulo="Token de recuperação" required value={token} onChange={e => setToken(e.target.value)}/><Campo rotulo="Nova senha" type="password" required minLength={8} value={novaSenha} onChange={e => setSenha(e.target.value)}/><Campo rotulo="Confirme a nova senha" type="password" required value={confirmaNovaSenha} onChange={e => setConfirma(e.target.value)}/><Botao type="submit" carregando={carregando} className="botao-largo">Salvar nova senha</Botao></form></Cartao></main></Fundo>
}

function moeda(v: number) { return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v || 0) }
