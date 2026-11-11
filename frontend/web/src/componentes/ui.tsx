import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from 'react'
import { LoaderCircle } from 'lucide-react'

export function Botao({ children, variante = 'primario', carregando, className = '', ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: 'primario' | 'secundario' | 'fantasma' | 'perigo'; carregando?: boolean }) {
  return <button className={`botao botao-${variante} ${className}`} disabled={props.disabled || carregando} {...props}>{carregando && <LoaderCircle size={16} className="girando" />}{children}</button>
}
export function Campo({ rotulo, ...props }: InputHTMLAttributes<HTMLInputElement> & { rotulo?: string }) {
  return <label className="campo">{rotulo && <span>{rotulo}</span>}<input {...props} /></label>
}
export function Seletor({ rotulo, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { rotulo?: string; children: ReactNode }) {
  return <label className="campo">{rotulo && <span>{rotulo}</span>}<select {...props}>{children}</select></label>
}
export function AreaTexto({ rotulo, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement> & { rotulo?: string }) {
  return <label className="campo">{rotulo && <span>{rotulo}</span>}<textarea {...props} /></label>
}
export function Cartao({ children, className = '' }: { children: ReactNode; className?: string }) { return <section className={`cartao ${className}`}>{children}</section> }
export function Titulo({ titulo, descricao, acao }: { titulo: string; descricao?: string; acao?: ReactNode }) { return <div className="titulo-pagina"><div><h1>{titulo}</h1>{descricao && <p>{descricao}</p>}</div>{acao}</div> }
export function EstadoVazio({ texto = 'Nenhum registro encontrado.' }: { texto?: string }) { return <div className="vazio">{texto}</div> }
export function Carregando({ texto = 'Carregando…' }: { texto?: string }) { return <div className="carregando"><LoaderCircle className="girando" size={22} />{texto}</div> }
export function ErroPainel({ texto }: { texto: string }) { return <div className="erro-painel" role="alert">{texto}</div> }
