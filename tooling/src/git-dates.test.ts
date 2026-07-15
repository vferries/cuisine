import { describe, expect, it } from "vitest";
import { execFileSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { lastCommitIso } from "./git-dates.ts";

function initRepo(): string {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "cuisine-git-test-"));
  execFileSync("git", ["init", "-q"], { cwd: dir });
  execFileSync("git", ["config", "user.email", "test@example.com"], { cwd: dir });
  execFileSync("git", ["config", "user.name", "Test"], { cwd: dir });
  return dir;
}

function commit(dir: string, file: string, date: string) {
  execFileSync("git", ["add", file], { cwd: dir });
  execFileSync("git", ["commit", "-q", "-m", `add ${file}`], {
    cwd: dir,
    env: {
      ...process.env,
      GIT_AUTHOR_DATE: date,
      GIT_COMMITTER_DATE: date,
    },
  });
}

describe("lastCommitIso", () => {
  it("retourne la date du dernier commit du fichier, normalisée en UTC", () => {
    const dir = initRepo();
    const file = path.join(dir, "recette.cook");
    fs.writeFileSync(file, ">> title: Test\n\nUne étape.");
    commit(dir, "recette.cook", "2024-03-01T12:00:00+01:00");

    expect(lastCommitIso(file)).toBe("2024-03-01T11:00:00.000Z");
  });

  it("retourne la date du dernier commit, pas du premier", () => {
    const dir = initRepo();
    const file = path.join(dir, "recette.cook");
    fs.writeFileSync(file, "v1");
    commit(dir, "recette.cook", "2024-01-01T10:00:00+00:00");
    fs.writeFileSync(file, "v2");
    commit(dir, "recette.cook", "2024-06-15T10:00:00+00:00");

    expect(lastCommitIso(file)).toBe("2024-06-15T10:00:00.000Z");
  });

  it("retourne null pour un fichier jamais commité", () => {
    const dir = initRepo();
    const file = path.join(dir, "brouillon.cook");
    fs.writeFileSync(file, "pas encore commité");

    expect(lastCommitIso(file)).toBeNull();
  });

  it("retourne null hors d'un repo git", () => {
    const dir = fs.mkdtempSync(path.join(os.tmpdir(), "cuisine-nogit-"));
    const file = path.join(dir, "recette.cook");
    fs.writeFileSync(file, "orphelin");

    expect(lastCommitIso(file)).toBeNull();
  });
});
