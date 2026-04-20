const UNIT_DISPLAY: Record<string, string> = {
  "càc": "c. à c.",
  "càs": "c. à s.",
};

const PLURALIZABLE = new Set([
  "brin",
  "sachet",
  "bouquet",
  "gousse",
  "pincée",
]);

export function formatUnit(
  qty: number | string | undefined,
  unit: string | undefined,
): string {
  if (!unit) return "";
  const display = UNIT_DISPLAY[unit] ?? unit;
  const n = typeof qty === "number" ? qty : Number(qty);
  if (Number.isFinite(n) && n > 1 && PLURALIZABLE.has(unit)) {
    return display + "s";
  }
  return display;
}

export function formatQty(
  qty: number | string | undefined,
  unit: string | undefined,
): string | null {
  if (qty === undefined || qty === "") return null;
  const u = formatUnit(qty, unit);
  return u ? `${qty} ${u}` : String(qty);
}

export function pluralizeName(qty: number, name: string): string {
  if (qty <= 1) return name;
  const [first, ...rest] = name.split(" ");
  if (/[sxz]$/i.test(first)) return name;
  return [first + "s", ...rest].join(" ");
}
