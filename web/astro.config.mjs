import { defineConfig } from "astro/config";

export default defineConfig({
  site: "https://example.github.io",
  base: process.env.DEPLOY_BASE || "/",
  output: "static",
  trailingSlash: "never",
});
