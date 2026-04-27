export const STORAGE_KEY = "favorites";

export function readFavorites(storage: Storage): string[] {
  try {
    const raw = storage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((s): s is string => typeof s === "string");
  } catch {
    return [];
  }
}

export function writeFavorites(storage: Storage, slugs: string[]): void {
  storage.setItem(STORAGE_KEY, JSON.stringify(slugs));
}

export function isFavorite(slugs: string[], slug: string): boolean {
  return slugs.includes(slug);
}

export function toggleFavorite(slugs: string[], slug: string): string[] {
  return isFavorite(slugs, slug)
    ? slugs.filter((s) => s !== slug)
    : [...slugs, slug];
}
