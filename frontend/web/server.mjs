import { accessSync } from 'node:fs'
import { extname, join, sep } from 'node:path'
import { fileURLToPath } from 'node:url'
import express from 'express'

const dist = fileURLToPath(new URL('./dist', import.meta.url))
const index = join(dist, 'index.html')
const assets = join(dist, 'assets') + sep
const port = Number(process.env.PORT ?? 8080)

if (!Number.isInteger(port) || port < 1 || port > 65535) {
  throw new Error('PORT deve ser uma porta TCP entre 1 e 65535.')
}
accessSync(index)

const app = express()
app.disable('x-powered-by')
app.use(express.static(dist, {
  redirect: false,
  setHeaders(response, file) {
    response.setHeader('Cache-Control', file.startsWith(assets)
      ? 'public, max-age=31536000, immutable'
      : 'no-cache')
  },
}))

app.get('/{*rota}', (request, response, next) => {
  const path = request.path
  if (path === '/assets' || path.startsWith('/assets/') || extname(path) || !request.accepts('html')) {
    return next()
  }

  response.set('Cache-Control', 'no-cache').sendFile(index)
})

app.listen(port, '0.0.0.0', () => {
  console.log(`Frontend web escutando em 0.0.0.0:${port}`)
})
