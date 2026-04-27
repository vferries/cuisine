import { beforeEach, describe, expect, it } from "vitest";
import {
  isFavorite,
  readFavorites,
  STORAGE_KEY,
  toggleFavorite,
  writeFavorites,
} from "./favorites";

function memStorage(): Storage {
  const data = new Map<string, string>();
  return {
    get length() {
      return data.size;
    },
    clear: () => data.clear(),
    getItem: (k) => data.get(k) ?? null,
    setItem: (k, v) => {
      data.set(k, v);
    },
    removeItem: (k) => {
      data.delete(k);
    },
    key: (i) => Array.from(data.keys())[i] ?? null,
  };
}

describe("favorites lib", () => {
  let storage: Storage;
  beforeEach(() => {
    storage = memStorage();
  });

  it("readFavorites retourne [] si rien en storage", () => {
    expect(readFavorites(storage)).toEqual([]);
  });

  it("readFavorites retourne [] si JSON invalide", () => {
    storage.setItem(STORAGE_KEY, "{not-json");
    expect(readFavorites(storage)).toEqual([]);
  });

  it("readFavorites retourne [] si la valeur n'est pas un tableau", () => {
    storage.setItem(STORAGE_KEY, JSON.stringify({ foo: "bar" }));
    expect(readFavorites(storage)).toEqual([]);
  });

  it("readFavorites filtre les entrées non-string", () => {
    storage.setItem(STORAGE_KEY, JSON.stringify(["porc", 42, null, "tatin"]));
    expect(readFavorites(storage)).toEqual(["porc", "tatin"]);
  });

  it("writeFavorites persiste la liste", () => {
    writeFavorites(storage, ["porc", "tatin"]);
    expect(readFavorites(storage)).toEqual(["porc", "tatin"]);
  });

  it("toggleFavorite ajoute un slug absent", () => {
    expect(toggleFavorite([], "porc")).toEqual(["porc"]);
    expect(toggleFavorite(["tatin"], "porc")).toEqual(["tatin", "porc"]);
  });

  it("toggleFavorite retire un slug présent", () => {
    expect(toggleFavorite(["porc", "tatin"], "porc")).toEqual(["tatin"]);
  });

  it("isFavorite reflète la présence du slug", () => {
    expect(isFavorite(["porc"], "porc")).toBe(true);
    expect(isFavorite(["porc"], "tatin")).toBe(false);
  });
});
