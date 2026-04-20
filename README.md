# Cuisine

Site statique + (à venir) app Android pour mes recettes, en format Cooklang.

## Démarrage

```bash
pnpm install
pnpm dev
```

Ouvre http://localhost:4321.

## Structure

```
.
├── recipes/              Source de vérité : un .cook par recette
│   └── images/           (à venir) photos 1024×768 par recette
├── tooling/              Scripts Node TS (parser + build-index)
├── web/                  Site Astro (accueil + vues recette)
└── CONVENTIONS.md        Conventions d'écriture des .cook
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

- [x] Scaffold Astro + parser Cooklang minimal
- [x] Accueil avec liste de recettes
- [x] Vue recette (deux colonnes desktop, onglets mobile)
- [ ] Mode cuisson plein écran
- [ ] Recherche client (MiniSearch)
- [ ] Favoris (localStorage)
- [ ] PWA + service worker
- [ ] GitHub Actions (build + deploy Pages)
- [ ] App Android (Kotlin + Compose)
