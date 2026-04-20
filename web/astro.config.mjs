import { defineConfig } from "astro/config";

export default defineConfig({
  site: "https://vferries.github.io",
  base: process.env.DEPLOY_BASE || "/",
  output: "static",
  trailingSlash: "never",
});
