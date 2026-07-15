export const FRENCH_MONTHS = [
  "janvier",
  "février",
  "mars",
  "avril",
  "mai",
  "juin",
  "juillet",
  "août",
  "septembre",
  "octobre",
  "novembre",
  "décembre",
] as const;

export const ALL_YEAR = "toute l'année";

function monthIndex(name: string): number {
  return FRENCH_MONTHS.indexOf(name as (typeof FRENCH_MONTHS)[number]) + 1;
}

export function isValidSaison(value: string): boolean {
  if (value === ALL_YEAR) return true;
  const parts = value.split("-");
  if (parts.length !== 2) return false;
  return monthIndex(parts[0]) > 0 && monthIndex(parts[1]) > 0;
}

export function expandSaison(value: string): number[] {
  if (!isValidSaison(value)) return [];
  if (value === ALL_YEAR) {
    return Array.from({ length: 12 }, (_, i) => i + 1);
  }
  const [start, end] = value.split("-").map(monthIndex);
  const months: number[] = [];
  for (let m = start; ; m = (m % 12) + 1) {
    months.push(m);
    if (m === end) break;
  }
  return months;
}
