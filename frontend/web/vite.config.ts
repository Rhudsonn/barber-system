import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [react(), VitePWA({ registerType: 'autoUpdate', includeAssets: [], manifest: { name: 'Barber System', short_name: 'Barber', description: 'Agendamentos da barbearia', theme_color: '#09090b', background_color: '#09090b', display: 'standalone', start_url: '/', icons: [{ src: '/pwa-192.svg', sizes: '192x192', type: 'image/svg+xml' }, { src: '/pwa-512.svg', sizes: '512x512', type: 'image/svg+xml' }] } })],
  server: { port: 5173 },
})
