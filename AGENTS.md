# Contexte pour Claude Code

Ce fichier est la mémoire externe du projet. Il résume les décisions prises, l'architecture visée, et ce qu'il reste à faire. Lis-le avant de commencer, et mets-le à jour quand une décision structurante change.

## Vue d'ensemble

Projet personnel : un site web + une app Android native pour consulter mes recettes pendant que je cuisine. Les recettes sont stockées au format [Cooklang](https://cooklang.org/) dans ce repo, qui est la source de vérité unique.

**Utilisateur unique** (le propriétaire du repo). Pas de comptes, pas de backend, pas d'édition en ligne. Les recettes s'éditent au texte dans un éditeur, se commit, et apparaissent partout.

**Objectif à terme : ~100+ recettes.**

## Décisions architecturales

### Monorepo unique avec une source de vérité

Le repo contient la donnée (recettes + images), le site web, les scripts de build, et à terme l'app Android. Une seule PR met à jour une recette qui apparaît partout. La source de vérité sont les fichiers `recipes/*.cook` — tout le reste est dérivé.

### Pipeline de build

À chaque push sur `main`, GitHub Actions (à implémenter) :

1. Parse tous les `.cook` via `tooling/src/build-index.ts`.
2. Écrit `web/src/generated/index.json` (métadonnées + tokens de recherche) et un `web/src/generated/recipes/{slug}.json` par recette (parsed AST).
3. Optimise les images (thumbs WebP 320×240, pas encore implémenté).
4. Build Astro en site statique.
5. Déploie sur GitHub Pages.

### Stratégie Android

**Pas encore commencé.** Stack prévu : Kotlin + Jetpack Compose, dossier `android/`.

**Sync runtime depuis GitHub Pages** — décision : l'APK est léger (~5 Mo), pas de données bundlées. Au premier lancement, l'app fetch `index.json` + les thumbs depuis Pages, puis tout est en cache local (Room ou fichiers). Les syncs suivants sont incrémentaux (diff sur `version + slug + updatedAt`). Trade-off accepté : le premier lancement demande une connexion, ensuite c'est 100% hors-ligne.

GitHub Pages sert donc d'API de fait pour l'app Android — pas besoin de backend dédié.

### PWA non retenue

L'utilisateur a explicitement demandé une vraie app Android native plutôt qu'une PWA.

### Parser Cooklang custom

Le parser dans `tooling/src/parser.ts` est écrit à la main (~230 lignes, zéro dépendance). Il gère le sous-ensemble Cooklang qu'on utilise : métadonnées, sections nommées (extension), ingrédients, ustensiles, timers, commentaires une ligne.

**Alternative** : `@cooklang/cooklang-ts` (le parser officiel). Si on tombe sur un cas limite ou si on veut le shopping list / scaling natif, le remplacer est une option. Pour l'instant le custom suffit et évite une dépendance.

## Direction de design

### Ton général

Éditorial, cookbook, chaleureux. Pas une app utilitaire générique — un livre de recettes personnel qui respire.

### Typographie

- **Display** : Fraunces (serif variable, pour les titres de recettes, titres de sections, nom du site).
- **Body** : Manrope (sans-serif géométrique moderne, pour tout le reste).
- Chargées depuis Google Fonts dans `global.css`.

### Palette

Définie en CSS variables dans `web/src/styles/global.css` :

- **Accent** : corail `#D85A30` (onglets actifs, pills, timers, accroche). Warm, alimentaire, pas cliché.
- **Tint corail** : `#FAECE7` (background des asides et pills).
- **Ink** : `#1A1A18` (texte principal, presque noir mais chaud).
- **Paper warm** : `#FAF7F2` (background du site, papier crème).
- **Paper surface** : `#F3EFE7` (cartes et zones secondaires).

### Layouts

**Mobile (≤ 760px)** : stack. Titre, métadonnées, portions, puis onglets Ingrédients / Étapes / Ustensiles (à implémenter — actuellement tout est stacké).

**Desktop / tablette (> 760px)** : deux colonnes.

- **Gauche (270px, sticky)** : image, portions, ingrédients, ustensiles. Reste visible pendant qu'on scrolle.
- **Droite (flex)** : étapes par section, numérotation continue à travers les sections (1-5 Préparation, 6-10 Cuisson, etc.), timers inline en pills, aside Astuces en bas.

**Très grand écran** (non implémenté) : potentielle 3ᵉ colonne à gauche avec la liste des recettes.

### Mode cuisson dédié (non implémenté)

Vue plein écran, une étape à la fois, timers en parallèle persistants, Wake Lock API pour garder l'écran allumé. Déclenché par le bouton "Mode cuisson" en haut à droite de chaque recette. Route prévue : `/cuisson/[slug]`.

### Accessibilité et détails à respecter

- Cibles tactiles ≥ 44px.
- Contraste AA minimum.
- Mains sales : pas d'interactions fines, gros boutons en mode cuisson.
- Dark mode : pas encore implémenté, à prévoir.

## État actuel du code

### Ce qui fonctionne

- Scaffold pnpm workspace (root, `tooling/`, `web/`).
- Parser Cooklang (`tooling/src/parser.ts`) — parse métadonnées, sections, ingrédients, ustensiles, timers. Agrège les duplications.
- Build-index (`tooling/src/build-index.ts`) — lit `recipes/*.cook`, génère `web/src/generated/index.json` + `web/src/generated/recipes/{slug}.json`.
- Accueil Astro (`web/src/pages/index.astro`) — liste les recettes depuis l'index, filtres visuels, recherche visuelle (pas câblée).
- Vue recette (`web/src/pages/[slug].astro`) — deux colonnes desktop, header, ingrédients, ustensiles, étapes numérotées par section, aside Astuces.
- Une recette de référence : `recipes/porc-bigorre-caramel.cook`.
- Design tokens + composants CSS dans `web/src/styles/global.css`.

### Ce qui ne fonctionne pas encore

- **Recherche** : input et chips affichés mais pas branchés. Prévu : MiniSearch (client-side), indexé au build sur titre (poids 3) / ingrédients (poids 2) / tags (poids 2) / cuisine (poids 1).
- **Portions dynamiques** : les boutons +/− affichent la valeur mais ne recalculent pas les quantités. Prévu : script client qui lit la valeur initiale depuis `servings` et scale les quantités numériques dans la sidebar (ratio simple).
- **Bouton "Mode cuisson"** : présent, ne mène nulle part. Route à créer : `web/src/pages/cuisson/[slug].astro`.
- **Filtres par chip** : visuels, pas câblés.
- **Dark mode** : non implémenté.
- **Images réelles** : placeholder SVG partout, pas de vraies photos. Prévu : `recipes/images/{slug}.webp` + thumbs générés au build.
- **Ustensiles dans la sidebar** : rendu actuel est une liste texte en ligne, pourrait mériter mieux.
- **GitHub Actions** : pas de workflow existant.
- **Validation des `.cook` en CI** : script `tooling/src/validate-cook.ts` mentionné dans CONVENTIONS.md mais pas encore écrit.
- **PWA / offline** : pas fait. Manifest + service worker à prévoir si l'utilisateur change d'avis sur la PWA.
- **App Android** : rien (dossier `android/` à créer).

### Limites connues du parser

- Pas de block-comments `[- ... -]`.
- Pas de bloc `--` pour les notes de recette officielles de Cooklang.
- Pas de shopping list ni scaling natif — fait à la main côté UI.
- La somme des quantités dupliquées ne marche que si l'unité est identique.

## Roadmap suggérée

Dans l'ordre que je recommande :

1. **Portions dynamiques** côté client (petit script vanilla dans `[slug].astro`).
2. **MiniSearch** câblé sur l'accueil.
3. **Filtres chips** fonctionnels (cuisine, tags, temps).
4. **Mode cuisson** plein écran (`/cuisson/[slug]`) avec timers et Wake Lock.
5. **Workflow GitHub Actions** (build + deploy Pages, validate sur PR).
6. **Dark mode** via `prefers-color-scheme`.
7. **Images réelles** + génération de thumbs.
8. **Seconde recette** pour stress-tester les conventions.
9. **Squelette Android** en Kotlin + Compose.
10. **PWA offline** (service worker, cache strategy).

## Conventions de code

- TypeScript strict partout côté JS.
- Pas de framework dans les pages Astro — Astro components + petits scripts vanilla pour l'interactivité côté client. On ajoutera React/Svelte seulement si nécessaire.
- Formatage : pas encore fixé. Prettier par défaut si besoin.
- Git : commits conventionnels (`feat:`, `fix:`, `chore:`, etc.) bienvenus mais pas obligatoires.

## Références

- Format Cooklang : https://cooklang.org/docs/spec/
- Parser officiel (fallback possible) : https://github.com/cooklang/cooklang-ts
- Astro : https://docs.astro.build/
- MiniSearch : https://lucaong.github.io/minisearch/

## Pour toi, Claude Code

Quand tu prends la main :

1. **Lis ce fichier, `README.md`, et `CONVENTIONS.md`** avant de toucher au code.
2. **Vérifie que le scaffold compile** : `pnpm install && pnpm dev`. Règle les soucis d'installation avant d'attaquer une feature.
3. **Respecte la direction de design** : Fraunces + Manrope, palette corail/papier, pas d'ombres portées ou de gradients, tout doit sentir le livre de cuisine et pas l'app SaaS générique.
4. **Mets à jour ce fichier** quand tu prends une décision structurante : changement de stack, nouveau dossier, alternative retenue vs parser custom, etc.
5. **Demande avant de grosses décisions** : changer le parser, ajouter une dépendance lourde (React, Tailwind, etc.), restructurer le monorepo.
6. **Le propriétaire du repo aime les petits pas commitables.** Une feature par commit avec un message clair > une PR géante.
