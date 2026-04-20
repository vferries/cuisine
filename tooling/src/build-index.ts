import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { parseCook } from "./parser.ts";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, "../..");
const RECIPES_DIR = path.join(ROOT, "recipes");
const OUT_DIR = path.join(ROOT, "web/src/generated");

interface RecipeMeta {
  slug: string;
  title: string;
  source?: string;
  servings: number;
  prepTime: number;
  cookTime: number;
  totalTime: number;
  difficulty: string;
  cuisine: string;
  region?: string;
  tags: string[];
  image?: string;
  ingredientNames: string[];
  updatedAt: string;
}

function parseMinutes(raw?: string): number {
  if (!raw) return 0;
  const m = raw.match(/(\d+)/);
  return m ? parseInt(m[1], 10) : 0;
}

function parseTags(raw?: string): string[] {
  if (!raw) return [];
  return raw.split(",").map((t) => t.trim()).filter(Boolean);
}

async function main() {
  await fs.mkdir(path.join(OUT_DIR, "recipes"), { recursive: true });

  let entries: string[];
  try {
    entries = await fs.readdir(RECIPES_DIR);
  } catch {
    console.error(`❌ Cannot read ${RECIPES_DIR}. Run from repo root.`);
    process.exit(1);
  }

  const files = entries.filter((f) => f.endsWith(".cook"));
  if (files.length === 0) {
    console.warn(`⚠️  No .cook files found in ${RECIPES_DIR}`);
  }

  const metaList: RecipeMeta[] = [];
  let errors = 0;

  for (const file of files) {
    const slug = file.replace(/\.cook$/, "");
    const filePath = path.join(RECIPES_DIR, file);
    const source = await fs.readFile(filePath, "utf-8");
    const stat = await fs.stat(filePath);

    try {
      const parsed = parseCook(source);
      const meta = parsed.metadata;

      if (!meta.title) {
        console.warn(`⚠️  ${file}: missing >> title, skipping`);
        errors++;
        continue;
      }

      const prepTime = parseMinutes(meta["prep time"]);
      const cookTime = parseMinutes(meta["cook time"]);

      const recipe = {
        slug,
        updatedAt: stat.mtime.toISOString(),
        ...parsed,
      };

      await fs.writeFile(
        path.join(OUT_DIR, "recipes", `${slug}.json`),
        JSON.stringify(recipe, null, 2),
        "utf-8",
      );

      metaList.push({
        slug,
        title: meta.title,
        source: meta.source,
        servings: parseInt(meta.servings ?? "0", 10) || 0,
        prepTime,
        cookTime,
        totalTime: prepTime + cookTime,
        difficulty: meta.difficulty ?? "moyenne",
        cuisine: meta.cuisine ?? "",
        region: meta.region,
        tags: parseTags(meta.tags),
        image: meta.image,
        ingredientNames: parsed.ingredients.map((i) => i.name),
        updatedAt: stat.mtime.toISOString(),
      });

      console.log(`  ✓ ${slug}`);
    } catch (err) {
      console.error(`  ✗ ${file}:`, (err as Error).message);
      errors++;
    }
  }

  metaList.sort((a, b) =>
    a.updatedAt < b.updatedAt ? 1 : -1,
  );

  const index = {
    version: new Date().toISOString(),
    generatedAt: new Date().toISOString(),
    recipes: metaList,
  };

  await fs.writeFile(
    path.join(OUT_DIR, "index.json"),
    JSON.stringify(index, null, 2),
    "utf-8",
  );

  console.log(
    `\n✓ Built ${metaList.length} recipe${metaList.length > 1 ? "s" : ""}`,
    errors > 0 ? `(${errors} error${errors > 1 ? "s" : ""})` : "",
  );
  console.log(`  → ${path.relative(ROOT, OUT_DIR)}/index.json`);

  if (errors > 0) process.exit(1);
}

main().catch((err) => {
  console.error("❌ Build failed:", err);
  process.exit(1);
});
