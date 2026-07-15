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
  "bâton",
  "botte",
]);

const HUMANIZE_UP: Record<string, { unit: string; factor: number }> = {
  g: { unit: "kg", factor: 1000 },
  ml: { unit: "l", factor: 1000 },
};

function humanize(
  qty: number | string | undefined,
  unit: string | undefined,
): [number | string | undefined, string | undefined] {
  const up = unit ? HUMANIZE_UP[unit] : undefined;
  const n = typeof qty === "number" ? qty : Number(qty);
  if (!up || !Number.isFinite(n) || n < up.factor) return [qty, unit];
  return [Math.round((n / up.factor) * 1000) / 1000, up.unit];
}

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
  const [q, u] = humanize(qty, unit);
  const display = formatUnit(q, u);
  return display ? `${q} ${display}` : String(q);
}

export function pluralizeName(qty: number, name: string): string {
  if (qty <= 1) return name;
  const [first, ...rest] = name.split(" ");
  if (/[sxz]$/i.test(first)) return name;
  return [first + "s", ...rest].join(" ");
}
