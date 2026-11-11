import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { Toaster } from 'sonner'
import { Aplicacao } from './telas/Aplicacao'
import './estilos.css'

ReactDOM.createRoot(document.getElementById('root')!).render(<React.StrictMode><BrowserRouter><Aplicacao /><Toaster theme="dark" richColors position="top-right" /></BrowserRouter></React.StrictMode>)
