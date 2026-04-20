import MiniSearch from "minisearch";

export interface RecipeSearchDoc {
  slug: string;
  title: string;
  cuisine: string;
  tags: string[];
  ingredientNames: string[];
}

function buildIndex(recipes: RecipeSearchDoc[]): MiniSearch<RecipeSearchDoc> {
  const index = new MiniSearch<RecipeSearchDoc>({
    idField: "slug",
    fields: ["title", "ingredientNames", "tags", "cuisine"],
    searchOptions: {
      boost: { title: 3, ingredientNames: 2, tags: 2, cuisine: 1 },
      prefix: true,
      fuzzy: 0.2,
    },
  });
  index.addAll(recipes);
  return index;
}

export function matchingRecipeSlugs(
  recipes: RecipeSearchDoc[],
  query: string,
): string[] {
  if (!query.trim()) return recipes.map((r) => r.slug);
  const index = buildIndex(recipes);
  return index.search(query).map((hit) => hit.id as string);
}
