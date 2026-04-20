# Cuisine

Site statique + (à venir) app Android pour mes recettes, en format [Cooklang](https://cooklang.org/).

## Pour commencer

```bash
pnpm install
pnpm dev
```

Ouvre http://localhost:4321.

## Documents importants

- **[`AGENTS.md`](./AGENTS.md)** — contexte complet du projet : décisions d'architecture, direction de design, état du code, roadmap. À lire en premier si tu (ou Claude Code) prends la main.
- **[`CONVENTIONS.md`](./CONVENTIONS.md)** — format des fichiers `.cook` : métadonnées, sections, ingrédients, ustensiles, timers.

## Structure

```
.
├── AGENTS.md             Contexte projet (lire en premier)
├── CONVENTIONS.md        Format des fichiers .cook
├── recipes/              Source de vérité : un .cook par recette
│   └── images/           (à venir) photos 1024×768 par recette
├── tooling/              Scripts Node TS (parser + build-index)
└── web/                  Site Astro (accueil + vues recette)
```

## Ajouter une recette

1. Crée `recipes/ma-recette.cook` en suivant `CONVENTIONS.md`.
2. (Optionnel) dépose `recipes/images/ma-recette.webp`.
3. `pnpm dev` ou `pnpm build-index` pour régénérer l'index.

## Scripts

| Commande | Action |
|---|---|
| `pnpm build-index` | Parse tous les `.cook`, génère `web/src/generated/` |
| `pnpm dev` | build-index + serveur Astro avec HMR |
| `pnpm build` | Build statique dans `web/dist/` |
| `pnpm preview` | Preview de la build |

## Roadmap

Voir `AGENTS.md` section "Roadmap suggérée" pour l'ordre recommandé.

- [x] Scaffold Astro + parser Cooklang minimal
- [x] Accueil avec liste de recettes
- [x] Vue recette (deux colonnes desktop)
- [ ] Portions dynamiques côté client
- [ ] Recherche MiniSearch câblée
- [ ] Filtres chips fonctionnels
- [ ] Mode cuisson plein écran (`/cuisson/[slug]`)
- [ ] GitHub Actions (build + deploy Pages)
- [ ] Dark mode
- [ ] PWA + service worker
- [ ] App Android (Kotlin + Compose)
