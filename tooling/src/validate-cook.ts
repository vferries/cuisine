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

const ALLOWED_INGREDIENT_UNITS = new Set([
  "g",
  "kg",
  "ml",
  "l",
  "càc",
  "càs",
  "pincée",
  "brin",
  "bouquet",
  "sachet",
  "gousse",
]);

const ALLOWED_TIMER_UNITS = new Set(["sec", "min", "h"]);

export interface ValidateOptions {
  imageExists?: (filename: string) => boolean;
}

export function validateRecipe(
  source: string,
  opts: ValidateOptions = {},
): ValidationResult {
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

  const tags = (meta.tags ?? "").split(",").map((t) => t.trim()).filter(Boolean);
  if (tags.length === 0) {
    warnings.push(`metadata "tags" absent ou vide — nourrit la recherche et les chips`);
  }

  if (!meta.source) {
    warnings.push(`metadata "source" absente — utile pour se rappeler d'où vient la recette`);
  }

  for (const ing of parsed.ingredients) {
    if (ing.unit && !ALLOWED_INGREDIENT_UNITS.has(ing.unit)) {
      warnings.push(
        `unité inconnue "${ing.unit}" sur l'ingrédient "${ing.name}" (voir CONVENTIONS)`,
      );
    }
  }

  for (const timer of parsed.timers) {
    if (timer.unit && !ALLOWED_TIMER_UNITS.has(timer.unit)) {
      warnings.push(
        `unité de timer inconnue "${timer.unit}" sur "${timer.name ?? "timer"}" (attendu: sec, min, h)`,
      );
    }
  }

  const totalSteps = parsed.sections.reduce((n, s) => n + s.steps.length, 0);
  if (totalSteps === 0) {
    errors.push(`aucune étape parsée — la recette est-elle vide ?`);
  }

  if (meta.image && opts.imageExists && !opts.imageExists(meta.image)) {
    errors.push(
      `image "${meta.image}" référencée mais fichier absent de recipes/images/`,
    );
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

  const IMAGES_DIR = path.join(RECIPES_DIR, "images");
  let imageFiles: Set<string>;
  try {
    imageFiles = new Set(fs.readdirSync(IMAGES_DIR));
  } catch {
    imageFiles = new Set();
  }
  const imageExists = (name: string) => imageFiles.has(name);

  let failed = 0;

  for (const file of files) {
    const source = fs.readFileSync(path.join(RECIPES_DIR, file), "utf-8");
    const { errors, warnings } = validateRecipe(source, { imageExists });
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
