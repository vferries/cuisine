/**
 * Minimal Cooklang parser.
 *
 * Handles the Cooklang subset we use:
 * - Metadata lines (`>> key: value`)
 * - Sections (`== Name ==`)  [extension]
 * - Ingredients (`@name{qty%unit}`)
 * - Cookware (`#name{qty}`)
 * - Timers (`~name{qty%unit}`)
 * - Comments (`-- ...` single-line, inline ou pleine ligne)
 * - Block comments (`[- ... -]`, possibly multi-line)
 *
 * Spec : https://cooklang.org/docs/spec/
 *
 * Not implemented (yet): shopping list, servings scaling.
 */

export interface Ingredient {
  name: string;
  quantity?: number | string;
  unit?: string;
}

export interface Cookware {
  name: string;
  quantity?: number | string;
}

export interface Timer {
  name?: string;
  quantity: number | string;
  unit: string;
}

export type Token =
  | { type: "text"; text: string }
  | { type: "ingredient"; ingredient: Ingredient }
  | { type: "cookware"; cookware: Cookware }
  | { type: "timer"; timer: Timer };

export interface Step {
  tokens: Token[];
}

export interface Section {
  name: string;
  steps: Step[];
}

export interface ParsedRecipe {
  metadata: Record<string, string>;
  sections: Section[];
  tips: string[];
  ingredients: Ingredient[];
  cookware: Cookware[];
  timers: Timer[];
}

const WORD_CHAR = /[\p{L}\p{N}\-']/u;
const STEP_BREAK = /[\n.,;!?)]/;

function parseMarker(
  text: string,
  start: number,
  marker: "@" | "#" | "~",
): [Token, number] {
  let i = start + 1;

  // Look ahead for `{` before a step-break — that signals multi-word name.
  let peek = i;
  let foundBrace = false;
  while (peek < text.length) {
    const c = text[peek];
    if (c === "{") {
      foundBrace = true;
      break;
    }
    if (STEP_BREAK.test(c)) break;
    peek++;
  }

  let name: string;
  if (foundBrace) {
    name = text.slice(i, peek).trim();
    i = peek;
  } else {
    let buf = "";
    while (i < text.length && WORD_CHAR.test(text[i])) {
      buf += text[i];
      i++;
    }
    name = buf;
  }

  let quantity: string | undefined;
  let unit: string | undefined;

  if (text[i] === "{") {
    i++;
    let inner = "";
    while (i < text.length && text[i] !== "}") {
      inner += text[i];
      i++;
    }
    i++;
    if (inner.includes("%")) {
      const [q, u] = inner.split("%");
      quantity = q.trim() || undefined;
      unit = u.trim() || undefined;
    } else if (inner.trim()) {
      quantity = inner.trim();
    }
  }

  const consumed = i - start;
  const numericQty = quantity && !isNaN(Number(quantity))
    ? Number(quantity)
    : quantity;

  if (marker === "@") {
    return [
      { type: "ingredient", ingredient: { name, quantity: numericQty, unit } },
      consumed,
    ];
  }
  if (marker === "#") {
    return [
      { type: "cookware", cookware: { name, quantity: numericQty } },
      consumed,
    ];
  }
  return [
    {
      type: "timer",
      timer: {
        name: name || undefined,
        quantity: numericQty ?? "",
        unit: unit ?? "",
      },
    },
    consumed,
  ];
}

function tokenizeStep(text: string): Token[] {
  const tokens: Token[] = [];
  let i = 0;
  let buf = "";

  const flush = () => {
    if (buf) {
      tokens.push({ type: "text", text: buf });
      buf = "";
    }
  };

  while (i < text.length) {
    const c = text[i];
    if (c === "@" || c === "#" || c === "~") {
      flush();
      const [tok, used] = parseMarker(text, i, c);
      tokens.push(tok);
      i += used;
    } else {
      buf += c;
      i++;
    }
  }
  flush();
  return tokens;
}

function stepHasContent(tokens: Token[]): boolean {
  return tokens.some(
    (t) => t.type !== "text" || t.text.trim().length > 0,
  );
}

function stripBlockComments(source: string): string {
  // Remove [- ... -] spans, possibly multi-line. Non-greedy, /s flag covers \n.
  return source.replace(/\[-[\s\S]*?-\]/g, "");
}

function stripInlineLineComment(line: string): string {
  // Remove anything from "--" to end of line (Cooklang single-line comment).
  const idx = line.indexOf("--");
  return idx >= 0 ? line.slice(0, idx) : line;
}

export function parseCook(source: string): ParsedRecipe {
  const lines = stripBlockComments(source).split("\n");
  const metadata: Record<string, string> = {};
  const sections: Section[] = [];

  let currentSection: Section | null = null;
  let currentStepLines: string[] = [];
  let inMetadata = true;

  const flushStep = () => {
    const raw = currentStepLines.join(" ").trim();
    if (!raw || !currentSection) {
      currentStepLines = [];
      return;
    }
    const tokens = tokenizeStep(raw);
    if (stepHasContent(tokens)) {
      currentSection.steps.push({ tokens });
    }
    currentStepLines = [];
  };

  const ensureSection = () => {
    if (!currentSection) {
      currentSection = { name: "Étapes", steps: [] };
    }
    return currentSection;
  };

  for (const rawLine of lines) {
    const cr = rawLine.replace(/\r$/, "");
    // Metadata uses the `>>` prefix; comments inside metadata values are
    // unlikely and stripping them changes nothing useful — apply only to
    // non-metadata lines.
    const line = cr.startsWith(">>") ? cr : stripInlineLineComment(cr);
    const trimmed = line.trim();

    // Metadata
    if (inMetadata) {
      const m = line.match(/^>>\s*([^:]+):\s*(.+)$/);
      if (m) {
        metadata[m[1].trim()] = m[2].trim();
        continue;
      }
      if (trimmed === "") continue;
      inMetadata = false;
    }

    // Section header
    const s = line.match(/^==\s*(.+?)\s*==\s*$/);
    if (s) {
      flushStep();
      if (currentSection) sections.push(currentSection);
      currentSection = { name: s[1], steps: [] };
      continue;
    }

    // Blank line = step separator
    if (trimmed === "") {
      flushStep();
      continue;
    }

    ensureSection();
    currentStepLines.push(line);
  }

  flushStep();
  if (currentSection) sections.push(currentSection);

  // Separate Astuces from regular sections
  const tips: string[] = [];
  const contentSections: Section[] = [];
  for (const sec of sections) {
    if (/astuce/i.test(sec.name)) {
      for (const step of sec.steps) {
        const text = step.tokens
          .map((t) => (t.type === "text" ? t.text : ""))
          .join("")
          .trim();
        if (text) tips.push(text);
      }
    } else {
      contentSections.push(sec);
    }
  }

  // Aggregate ingredients and cookware
  const ingredientMap = new Map<string, Ingredient>();
  const cookwareMap = new Map<string, Cookware>();
  const timers: Timer[] = [];

  for (const sec of contentSections) {
    for (const step of sec.steps) {
      for (const tok of step.tokens) {
        if (tok.type === "ingredient") {
          const ing = tok.ingredient;
          const key = ing.name.toLowerCase();
          const prev = ingredientMap.get(key);
          if (!prev) {
            ingredientMap.set(key, { ...ing });
          } else {
            const canSum =
              prev.unit === ing.unit &&
              typeof prev.quantity === "number" &&
              typeof ing.quantity === "number";
            if (canSum) {
              prev.quantity =
                (prev.quantity as number) + (ing.quantity as number);
            }
          }
        } else if (tok.type === "cookware") {
          const key = tok.cookware.name.toLowerCase();
          const prev = cookwareMap.get(key);
          if (!prev) {
            cookwareMap.set(key, { ...tok.cookware, quantity: 1 });
          } else {
            prev.quantity = (Number(prev.quantity) || 1) + 1;
          }
        } else if (tok.type === "timer") {
          timers.push(tok.timer);
        }
      }
    }
  }

  return {
    metadata,
    sections: contentSections,
    tips,
    ingredients: [...ingredientMap.values()],
    cookware: [...cookwareMap.values()],
    timers,
  };
}
