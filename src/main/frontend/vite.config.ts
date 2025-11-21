/**
 * Vite Configuration for File Manager Frontend
 *
 * This configuration file sets up Vite for building the React TypeScript application.
 * It includes:
 * - React plugin with Fast Refresh for development
 * - Path aliases matching tsconfig.json for clean imports
 * - Build output configuration for Spring Boot integration
 * - Development server with API proxy to backend
 */

import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

/**
 * Vite configuration
 * @see https://vitejs.dev/config/
 */
export default defineConfig({
  /**
   * Plugins
   */
  plugins: [
    react({
      // Enable React Fast Refresh for development
      fastRefresh: true,
    }),
  ],

  /**
   * Path resolution
   * Define path aliases for cleaner imports throughout the application
   */
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@components': resolve(__dirname, './src/components'),
      '@services': resolve(__dirname, './src/services'),
      '@types': resolve(__dirname, './src/types'),
    },
  },

  /**
   * Build configuration
   * Output is placed in Spring Boot's static resources directory
   */
  build: {
    // Output directory for Spring Boot to serve
    outDir: '../resources/static',
    // Clean the output directory before building
    emptyOutDir: true,
    // Generate source maps for debugging
    sourcemap: true,
    // Target modern browsers for smaller bundle size
    target: 'es2022',
    // Optimize chunk splitting
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor chunks for better caching
          'react-vendor': ['react', 'react-dom'],
          'mui-vendor': ['@mui/material', '@mui/icons-material'],
          'utils-vendor': ['axios'],
        },
      },
    },
  },

  /**
   * Development server configuration
   */
  server: {
    // Development server port
    port: 3000,
    // Open browser automatically
    open: false,
    // Proxy API requests to Spring Boot backend
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Log proxy requests
        configure: (proxy) => {
          proxy.on('error', (err) => {
            console.log('Proxy error:', err);
          });
          proxy.on('proxyReq', (proxyReq, req) => {
            console.log('Proxying:', req.method, req.url);
          });
        },
      },
    },
  },

  /**
   * Preview server configuration (for production build preview)
   */
  preview: {
    port: 3001,
  },
});
