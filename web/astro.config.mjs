import { defineConfig } from "astro/config";

export default defineConfig({
  site: "https://example.github.io",
  base: "/",
  output: "static",
  trailingSlash: "never",
});
