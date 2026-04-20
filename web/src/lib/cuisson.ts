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
