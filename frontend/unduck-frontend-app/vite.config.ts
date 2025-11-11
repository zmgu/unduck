import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// mode에 따라 자동으로 .env / .env.local 로드됨
export default ({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');

  console.log(`✅ 현재 모드: ${mode}, 백엔드 URL: ${env.VITE_BACKEND_API_BASE_URL}`);

  return defineConfig({
    plugins: [react()],
    server: {
      port: 5173, // 기본 Vite dev 포트
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_API_BASE_URL,
          changeOrigin: true,
          secure: false,
        },
      },
    },
  });
};
