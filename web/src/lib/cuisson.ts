export interface StepToken {
  type: string;
  text?: string;
  ingredient?: { name: string };
  cookware?: { name: string };
  timer?: { name?: string; quantity: number | string; unit: string };
}

export interface Step {
  tokens: StepToken[];
}

export interface Section {
  name: string;
  steps: Step[];
}

export interface FlatStep {
  section: string;
  tokens: StepToken[];
}

export function flattenSteps(sections: Section[]): FlatStep[] {
  return sections.flatMap((s) =>
    s.steps.map((step) => ({ section: s.name, tokens: step.tokens })),
  );
}

const UNIT_TO_SECONDS: Record<string, number> = {
  sec: 1,
  min: 60,
  h: 3600,
};

export function timerSeconds(timer: {
  quantity: number | string;
  unit: string;
}): number {
  const q = typeof timer.quantity === "number"
    ? timer.quantity
    : parseFloat(timer.quantity);
  return q * (UNIT_TO_SECONDS[timer.unit] ?? 0);
}

export function formatSecondsAsTime(seconds: number): string {
  const s = Math.max(0, Math.floor(seconds));
  const minutes = Math.floor(s / 60);
  const remaining = s % 60;
  return `${minutes}:${String(remaining).padStart(2, "0")}`;
}

interface WakeLockLike {
  request(type: "screen"): Promise<unknown>;
}

interface NavigatorLike {
  wakeLock?: WakeLockLike;
}

export async function acquireWakeLock(
  nav: NavigatorLike,
): Promise<unknown | null> {
  const wakeLock = nav.wakeLock;
  if (!wakeLock?.request) return null;
  try {
    return await wakeLock.request("screen");
  } catch {
    return null;
  }
}
