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
    proxy: {
      '/api': {
        target: 'http://localhost:9090',  // 后端服务端口
        changeOrigin: true,
        // 不需要 rewrite，直接使用 /api 路径
        // 后端接口格式：http://localhost:9090/api/xxx
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