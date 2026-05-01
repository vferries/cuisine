export function pickRandom<T>(
  items: readonly T[],
  rng: () => number = Math.random,
): T | undefined {
  if (items.length === 0) return undefined;
  return items[Math.floor(rng() * items.length)];
}
