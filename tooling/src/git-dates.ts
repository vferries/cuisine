import { execFileSync } from "node:child_process";
import path from "node:path";

/**
 * Date ISO (UTC) du dernier commit touchant le fichier, ou null si le
 * fichier n'a jamais été commité ou n'est pas dans un repo git.
 *
 * On normalise via Date.toISOString() : %cI porte l'offset local du
 * committer, et les tris en aval comparent les chaînes lexicographiquement.
 */
export function lastCommitIso(filePath: string): string | null {
  try {
    const out = execFileSync(
      "git",
      ["log", "-1", "--format=%cI", "--", path.basename(filePath)],
      {
        cwd: path.dirname(filePath),
        encoding: "utf-8",
        stdio: ["ignore", "pipe", "ignore"],
      },
    ).trim();
    return out ? new Date(out).toISOString() : null;
  } catch {
    // Branche attendue : hors repo git. Le caller loggue le fallback mtime.
    return null;
  }
}
