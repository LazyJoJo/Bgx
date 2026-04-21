import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig } from 'vite';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@components': path.resolve(__dirname, './src/components'),
      '@hooks': path.resolve(__dirname, './src/hooks'),
      '@pages': path.resolve(__dirname, './src/pages'),
      '@services': path.resolve(__dirname, './src/services'),
      '@store': path.resolve(__dirname, './src/store'),
      '@types': path.resolve(__dirname, './src/types'),
      '@assets': path.resolve(__dirname, './src/assets'),
    },
  },
  server: {
    port: 8080,
    host: '127.0.0.1',
    https: {
      key: './key.pem',
      cert: './cert.pem',
    },
    proxy: {
      '/api': {
        target: 'https://localhost:9090',
        changeOrigin: true,
        secure: false,
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        manualChunks: (id) => {
          if (id.includes('node_modules')) {
            return 'vendor';
            if (id.includes('react')) return 'vendor';
            if (id.includes('antd') || id.includes('@ant-design')) return 'antd';
            if (id.includes('echarts')) return 'echarts';
            if (id.includes('redux')) return 'redux';
          }
        }
      }
    }
  }
})