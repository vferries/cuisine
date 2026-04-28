export type SortMode = "recent" | "alpha" | "duration";

export interface SortableRecipe {
  slug: string;
  title: string;
  totalTime: number;
  updatedAt: string;
}

export function sortRecipes(
  recipes: SortableRecipe[],
  mode: SortMode,
): string[] {
  const copy = [...recipes];
  switch (mode) {
    case "recent":
      copy.sort((a, b) => (a.updatedAt < b.updatedAt ? 1 : -1));
      break;
    case "alpha":
      copy.sort((a, b) => a.title.localeCompare(b.title, "fr"));
      break;
    case "duration":
      copy.sort((a, b) => a.totalTime - b.totalTime);
      break;
  }
  return copy.map((r) => r.slug);
}
