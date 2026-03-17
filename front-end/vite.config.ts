import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import path from "path"

export default defineConfig({
  plugins: [tailwindcss()],
  server: {
    allowedHosts: ["progenitorial-undemonstratively-gregory.ngrok-free.dev"],
  },
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
});
