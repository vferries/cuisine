export interface ChipFilterDoc {
  slug: string;
  cuisine: string;
  course?: string;
  difficulty?: string;
  tags: string[];
  totalTime: number;
}

export type ChipKey =
  | "all"
  | "rapide"
  | "vege"
  | "asiatique"
  | "francais"
  | "dessert"
  | "favoris";

export interface FilterContext {
  favorites?: string[];
}

const CHIP_PREDICATES: Record<
  Exclude<ChipKey, "all" | "favoris">,
  (r: ChipFilterDoc) => boolean
> = {
  rapide: (r) => r.totalTime <= 30,
  vege: (r) => r.tags.includes("végé"),
  asiatique: (r) => r.tags.includes("asiatique"),
  francais: (r) => r.cuisine === "française",
  dessert: (r) => r.course === "dessert" || r.tags.includes("dessert"),
};

export function filterByChip(
  recipes: ChipFilterDoc[],
  chip: ChipKey,
  ctx: FilterContext = {},
): string[] {
  if (chip === "all") return recipes.map((r) => r.slug);
  if (chip === "favoris") {
    const favs = new Set(ctx.favorites ?? []);
    return recipes.filter((r) => favs.has(r.slug)).map((r) => r.slug);
  }
  const predicate = CHIP_PREDICATES[chip];
  return recipes.filter(predicate).map((r) => r.slug);
}

export type Course = "entrée" | "plat" | "dessert";

export function filterByCourse(
  recipes: ChipFilterDoc[],
  course: Course | null,
): string[] {
  if (!course) return recipes.map((r) => r.slug);
  return recipes.filter((r) => r.course === course).map((r) => r.slug);
}

export type Difficulty = "facile" | "moyenne" | "difficile";

export function filterByDifficulty(
  recipes: ChipFilterDoc[],
  difficulty: Difficulty | null,
): string[] {
  if (!difficulty) return recipes.map((r) => r.slug);
  return recipes
    .filter((r) => r.difficulty === difficulty)
    .map((r) => r.slug);
}

export function filterBySansGluten(
  recipes: ChipFilterDoc[],
  active: boolean,
): string[] {
  if (!active) return recipes.map((r) => r.slug);
  return recipes
    .filter((r) => r.tags.includes("sans gluten"))
    .map((r) => r.slug);
}
