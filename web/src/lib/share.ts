export interface ShareTarget {
  title: string;
  url: string;
}

export interface ShareDeps {
  share?: (data: ShareTarget) => Promise<void>;
  copy: (text: string) => Promise<void>;
}

export type ShareResult = "shared" | "copied" | "cancelled";

export async function shareRecipe(
  target: ShareTarget,
  deps: ShareDeps,
): Promise<ShareResult> {
  if (deps.share) {
    try {
      await deps.share(target);
      return "shared";
    } catch (err) {
      if ((err as { name?: string })?.name === "AbortError") {
        return "cancelled";
      }
    }
  }
  await deps.copy(target.url);
  return "copied";
}
