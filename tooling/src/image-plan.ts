export interface ImageOutput {
  filename: string;
  width: number;
  height: number;
}

export interface ImagePlan {
  source: string;
  slug: string;
  outputs: ImageOutput[];
}

const IMAGE_EXTENSIONS = /\.(png|jpe?g|webp)$/i;

export function planImages(files: string[]): ImagePlan[] {
  return files
    .filter((f) => IMAGE_EXTENSIONS.test(f))
    .map((source) => {
      const slug = source.replace(/\.[^.]+$/, "");
      return {
        source,
        slug,
        outputs: [
          { filename: `${slug}.webp`, width: 1024, height: 768 },
          { filename: `${slug}.thumb.webp`, width: 320, height: 240 },
        ],
      };
    });
}
