import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { parseCook } from "./parser.ts";

export interface ValidationResult {
  errors: string[];
  warnings: string[];
}

const REQUIRED_METADATA = [
  "title",
  "servings",
  "prep time",
  "cook time",
  "difficulty",
  "cuisine",
] as const;

const ALLOWED_DIFFICULTIES = new Set(["facile", "moyenne", "difficile"]);

export function validateRecipe(source: string): ValidationResult {
  const errors: string[] = [];
  const warnings: string[] = [];

  const parsed = parseCook(source);
  const meta = parsed.metadata;

  for (const key of REQUIRED_METADATA) {
    if (!meta[key]) {
      errors.push(`metadata "${key}" manquante`);
    }
  }

  if (meta.difficulty && !ALLOWED_DIFFICULTIES.has(meta.difficulty)) {
    errors.push(
      `metadata "difficulty" invalide: "${meta.difficulty}" (attendu: facile, moyenne, difficile)`,
    );
  }

  if (meta.servings) {
    const n = Number(meta.servings);
    if (!Number.isInteger(n) || n <= 0) {
      errors.push(
        `metadata "servings" doit être un entier positif (reçu: "${meta.servings}")`,
      );
    }
  }

  return { errors, warnings };
}

const currentFile = fileURLToPath(import.meta.url);
if (process.argv[1] === currentFile) {
  const __dirname = path.dirname(currentFile);
  const RECIPES_DIR = path.resolve(__dirname, "../../recipes");

  let entries: string[];
  try {
    entries = fs.readdirSync(RECIPES_DIR);
  } catch {
    console.error(`❌ Cannot read ${RECIPES_DIR}.`);
    process.exit(1);
  }

  const files = entries.filter((f) => f.endsWith(".cook"));
  let failed = 0;

  for (const file of files) {
    const source = fs.readFileSync(path.join(RECIPES_DIR, file), "utf-8");
    const { errors, warnings } = validateRecipe(source);
    if (errors.length || warnings.length) {
      console.log(`\n${file}`);
      for (const e of errors) console.error(`  ✗ ${e}`);
      for (const w of warnings) console.warn(`  ⚠ ${w}`);
    }
    if (errors.length) failed += 1;
  }

  if (failed > 0) {
    console.error(`\n${failed} recette(s) invalide(s).`);
    process.exit(1);
  }
  console.log(`✓ ${files.length} recette(s) valide(s).`);
}
