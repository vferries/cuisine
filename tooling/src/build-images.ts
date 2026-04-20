import fs from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";
import sharp from "sharp";
import { planImages } from "./image-plan.ts";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, "../..");
const SOURCE_DIR = path.join(ROOT, "recipes/images");
const OUT_DIR = path.join(ROOT, "web/public/images");

async function main() {
  await fs.mkdir(OUT_DIR, { recursive: true });

  let entries: string[];
  try {
    entries = await fs.readdir(SOURCE_DIR);
  } catch {
    console.warn(`⚠️  No source images dir at ${SOURCE_DIR}, skipping.`);
    return;
  }

  const plans = planImages(entries);
  if (plans.length === 0) {
    console.log(`No images to process in ${path.relative(ROOT, SOURCE_DIR)}.`);
    return;
  }

  let count = 0;
  for (const plan of plans) {
    const sourcePath = path.join(SOURCE_DIR, plan.source);
    for (const output of plan.outputs) {
      const outPath = path.join(OUT_DIR, output.filename);
      await sharp(sourcePath)
        .resize({
          width: output.width,
          height: output.height,
          fit: "cover",
        })
        .webp({ quality: 82 })
        .toFile(outPath);
      console.log(`  ✓ ${output.filename} (${output.width}×${output.height})`);
      count += 1;
    }
  }
  console.log(`\n✓ Built ${count} image(s) → ${path.relative(ROOT, OUT_DIR)}/`);
}

main().catch((err) => {
  console.error("❌ Build images failed:", err);
  process.exit(1);
});
