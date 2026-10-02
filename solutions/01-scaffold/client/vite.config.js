import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Both backends listen on 3001, so the client works with whichever one is running.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': process.env.API_URL ?? 'http://localhost:3001',
    },
  },
});
