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
      // Tie-break par slug : les recettes committées ensemble partagent
      // la même date git, l'ordre doit rester déterministe.
      copy.sort((a, b) =>
        a.updatedAt === b.updatedAt
          ? a.slug.localeCompare(b.slug)
          : a.updatedAt < b.updatedAt
            ? 1
            : -1,
      );
      break;
    case "alpha":
      copy.sort((a, b) => a.title.localeCompare(b.title, "fr"));
      break;
    case "duration":
      copy.sort(
        (a, b) => a.totalTime - b.totalTime || a.slug.localeCompare(b.slug),
      );
      break;
  }
  return copy.map((r) => r.slug);
}
