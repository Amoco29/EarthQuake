import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Port 5173 : c'est l'origine autorisée par le CORS du backend.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173, strictPort: true },
})
