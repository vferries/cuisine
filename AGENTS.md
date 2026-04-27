# Contexte pour Claude Code

Ce fichier est la mémoire externe du projet. Il résume les décisions prises, l'architecture visée, et ce qu'il reste à faire. Lis-le avant de commencer, et mets-le à jour quand une décision structurante change.

## Vue d'ensemble

Projet personnel : un site web + une app Android native pour consulter mes recettes pendant que je cuisine. Les recettes sont stockées au format [Cooklang](https://cooklang.org/) dans ce repo, qui est la source de vérité unique.

**Utilisateur unique** (le propriétaire du repo). Pas de comptes, pas de backend, pas d'édition en ligne. Les recettes s'éditent au texte dans un éditeur, se commit, et apparaissent partout.

**Objectif à terme : ~100+ recettes.**

## Décisions architecturales

### Monorepo unique avec une source de vérité

Le repo contient la donnée (recettes + images), le site web, les scripts de build, et à terme l'app Android. Une seule PR met à jour une recette qui apparaît partout. La source de vérité sont les fichiers `recipes/*.cook` — tout le reste est dérivé.

### Pipeline de build & déploiement

Deux workflows GitHub Actions dans `.github/workflows/` :

- **`ci.yml`** (PR + push main) : `pnpm validate` (validateur `.cook`) → tests unit → tests e2e Playwright.
- **`deploy.yml`** (push main) : mêmes checks, puis `pnpm build` avec `DEPLOY_BASE=/cuisine`, upload-pages-artifact, deploy-pages.

Étapes de build détaillées :

1. `tooling/src/validate-cook.ts` — valide chaque `.cook`. Errors : metadata requises, difficulty enum, servings entier positif, aucune étape parsée, image référencée absente. Warnings : tags/source absents, unités d'ingrédient/timer hors liste autorisée. Exit 1 sur error.
2. `tooling/src/build-index.ts` — parse chaque `.cook`, génère `web/src/generated/index.json` (métadonnées + tokens de recherche) + `web/src/generated/recipes/{slug}.json` (AST complet).
3. `tooling/src/build-images.ts` — via `sharp`, convertit `recipes/images/*.{png,jpg,jpeg,webp}` en `web/public/images/{slug}.webp` (1024×768) + `{slug}.thumb.webp` (320×240), fit cover + q82.
4. `astro build` produit `web/dist/`.
5. `actions/deploy-pages@v4` publie sur GitHub Pages.

Astro `base` est piloté par `DEPLOY_BASE` env (défaut `/` en dev, `/cuisine` en CI deploy). Les liens internes passent par le helper `withBase(import.meta.env.BASE_URL, path)`.

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

**Mobile (≤ 760px)** : stack. Titre, métadonnées, hero-thumb, portions, puis onglets Ingrédients / Étapes / Ustensiles (un seul panel visible à la fois, switch via nav.tabs role=tablist).

**Desktop / tablette (> 760px)** : deux colonnes.

- **Gauche (270px, sticky)** : image, portions, ingrédients, ustensiles. Reste visible pendant qu'on scrolle.
- **Droite (flex)** : étapes par section, numérotation continue à travers les sections (1-5 Préparation, 6-10 Cuisson, etc.), timers inline en pills, aside Astuces en bas.

**Très grand écran** (non implémenté) : potentielle 3ᵉ colonne à gauche avec la liste des recettes.

### Mode cuisson dédié

Route `/cuisson/[slug]`, plein écran, une étape à la fois (Précédent/Suivant), typo clamp 22–32px, cibles tactiles ≥ 56px. Déclenché par le bouton "Mode cuisson" en haut à droite de chaque recette.

**Wake Lock** : `navigator.wakeLock.request("screen")` appelé au chargement, feature-detected. Le browser relâche à l'unload.

### Timers globaux

Timer tray fixe (bas-droite), monté dans `Base.astro`, visible sur toutes les pages dès qu'il y a ≥ 1 timer actif. Click sur n'importe quelle pill `.step-timer[data-seconds]` (page détail OU mode cuisson) crée un timer dans `localStorage["active-timers"]`. Idempotent par id stable (`slug:sectionIdx:stepIdx:tokenIdx`) — cliquer la même pill sur les deux pages ne double pas.

À l'expiration (remaining ≤ 0) : animation `timer-blink` (alterne tint ↔ accent, 0.9s infinite), son `web/public/audio/timer-beep.mp3` joué **une fois** par transition (Set `notified` en mémoire session). Dismiss via ✕ retire l'item du storage et du Set notified.

### Timers Android (système)

Côté Android, le tray Compose n'est qu'un miroir de l'état persisté — la **vérité** vit dans `AlarmManager`. `TimerRegistry.start(t)` appelle `TimerScheduler.schedule(t)` qui programme une alarme exacte (`setExactAndAllowWhileIdle`, RTC_WAKEUP) ciblée sur `TimerExpirationReceiver`. Le timer survit donc au kill de l'app : l'OS réveille le device et tire le receiver, qui poste une notification high-importance via `TimerNotifier` sur le channel `timers` (son `R.raw.timer_beep`, vibration). Le son n'est plus joué côté Compose pour éviter le double bip — la notif système est l'unique source.

Permissions : `SCHEDULE_EXACT_ALARM` (déclarée au manifest, non auto-grantée sur targetSdk 31+, l'écran Réglages propose un bouton qui ouvre `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` ; fallback sur `setAndAllowWhileIdle` inexact si refusée). `POST_NOTIFICATIONS` est demandée à `MainActivity.onCreate` sur Android 13+.

Pas de **foreground service** : pas besoin, AlarmManager est conçu pour ce cas. Le service foreground a été écarté comme overkill.

### Checklist ingrédients

Click sur un `<li>` d'ingrédient toggle `.is-checked` (strike-through + dim 45%, check pill corail). Persisté par recette (`localStorage["ingredients-checked:{slug}"]`). Bouton "Tout décocher" dans le panel-header. Indépendant du changement de portions (décoche pas automatiquement).

### Accessibilité et détails à respecter

- Cibles tactiles ≥ 44px.
- Contraste AA minimum.
- Mains sales : pas d'interactions fines, gros boutons en mode cuisson.
- Dark mode : auto via `prefers-color-scheme`, override toggle persisté en localStorage.

## État actuel du code

### Ce qui fonctionne

- Scaffold pnpm workspace (root, `tooling/`, `web/`).
- Parser Cooklang (`tooling/src/parser.ts`) — métadonnées, sections, ingrédients, ustensiles, timers. Agrège les duplications.
- Build-index (`tooling/src/build-index.ts`) — `recipes/*.cook` → `web/src/generated/index.json` + `web/src/generated/recipes/{slug}.json`.
- Validator étendu (`tooling/src/validate-cook.ts`) — errors : metadata requises, difficulty enum, servings > 0, aucune étape, image absente. Warnings : tags/source absents, unités hors liste, pluriels (pour guider vers singulier).
- Image pipeline (`tooling/src/build-images.ts`) — sharp convertit `recipes/images/*` en `web/public/images/{slug}.webp` + `{slug}.thumb.webp`, sortie gitignorée.
- Accueil (`web/src/pages/index.astro`) — liste avec vignettes thumb, **recherche MiniSearch câblée** (titre ×3, ingrédients ×2, tags ×2, cuisine ×1, prefix+fuzzy), **chips fonctionnels** (all/favoris/rapide/vege/asiatique/francais/dessert, AND avec la recherche), **cœur favoris** sur chaque carte.
- Vue recette (`web/src/pages/[slug].astro`) — deux colonnes desktop, image héro, ingrédients cochables avec "Tout décocher" (persisté par slug), ustensiles, étapes numérotées avec timer pills cliquables, aside Astuces, **portions dynamiques** (+/− scale en live), bouton Mode cuisson câblé. **Onglets sur mobile** (Ingrédients / Étapes / Ustensiles).
- Mode cuisson (`web/src/pages/cuisson/[slug].astro`) — plein écran pas-à-pas, pills timer statiques (cliquables pour alimenter le tray global), Wake Lock.
- **Timer tray global** (`Base.astro` + `web/src/lib/timers.ts`) — click sur une pill démarre un timer persisté en localStorage avec id stable (`slug:sectionIdx:stepIdx:tokenIdx`), tick 1s, expire → blink + son `web/public/audio/timer-beep.mp3` une fois, dismiss via ✕, visible cross-page.
- Libs `web/src/lib/` : `scale`, `search`, `chips`, `cuisson` (flatten/timer/wake lock), `url` (withBase), `format` (formatUnit/formatQty avec pluralisation, pluralizeName), `timers` (addTimer/removeTimer/remainingSeconds/isExpired), `favorites` (read/write/toggle/isFavorite, key `favorites` en localStorage). Toutes testées unit.
- Tests : **Vitest** pour le unit (~52 tests côté web + 24 tooling), **Playwright** pour le e2e (18 specs). `pnpm test`, `pnpm test:e2e`.
- **CI + deploy** : workflows GitHub Actions (`ci.yml`, `deploy.yml`). Deploy sur push main vers `/cuisine/` (piloté par `DEPLOY_BASE`).
- **Dark mode** : auto via `prefers-color-scheme`, override manuel par bouton (`data-theme` persisté en localStorage, script inline dans `<head>` anti-flash).
- Design tokens + composants CSS dans `web/src/styles/global.css`.
- Une recette de référence : `recipes/porc-bigorre-caramel.cook` + image `recipes/images/porc-bigorre-caramel.png`.

### Ce qui ne fonctionne pas encore

- **Ustensiles dans la sidebar** : liste texte en ligne, pourrait mériter mieux.
- **PWA / offline** : pas fait. Pas retenu pour l'instant (Android native choisi).
- **App Android** : rien (dossier `android/` à créer).

### Limites connues du parser

- Pas de block-comments `[- ... -]`.
- Pas de bloc `--` pour les notes de recette officielles de Cooklang.
- Pas de shopping list ni scaling natif — fait à la main côté UI.
- La somme des quantités dupliquées ne marche que si l'unité est identique.

## Roadmap

Faits (tout le périmètre web) :

- [x] Portions dynamiques.
- [x] MiniSearch câblé sur l'accueil.
- [x] Filtres chips fonctionnels.
- [x] Mode cuisson plein écran + Wake Lock.
- [x] Workflow GitHub Actions (CI + deploy Pages).
- [x] Validateur `.cook` (metadata, enum, units, pluriels, image, empty).
- [x] Dark mode (auto + toggle manuel persisté).
- [x] Pipeline d'images au build (sharp, WebP + thumb).
- [x] Refactor pluralisation (data singulier, UI pluralise).
- [x] Onglets mobile sur la vue recette.
- [x] Checklist ingrédients (cochable, persisté par slug).
- [x] Timer tray global, stack, click-to-start depuis détail ou cuisson, persistance cross-page, son + blink à l'expiration.
- [x] App Android Kotlin + Compose Material3 en parité fonctionnelle : liste (vignette Coil + cuisine/durée/pers/difficulté), recherche, chips, détail avec hero collapsing, onglets Ingrédients/Étapes/Ustensiles, portions sticky, checklist, mode cuisson pas-à-pas avec Wake Lock, timer tray global avec son + clignotement, dark mode auto + toggle dans Settings, cache Room avec fallback offline.
- [x] Release pipeline Android : workflow `android-release.yml` déclenché sur tag `v*.*.*`, APK signé uploadé en GitHub Release.
- [x] Timers Android fiables : AlarmManager + BroadcastReceiver + notification système. Survivent au kill de l'app. Onboarding `SCHEDULE_EXACT_ALARM` dans Réglages.
- [x] Favoris (web + Android) : `FavoritesStore` SharedPrefs côté Android (clé `favorites`, mirroir de `ChecklistStore`), `lib/favorites.ts` localStorage côté web. Cœur sur la carte d'accueil et la page détail, chip "Favoris" intégré au filtrage AND avec la recherche.

Reste à faire :

1. **Liste de courses** agrégée multi-recettes.
2. **PWA offline** — si l'utilisateur change d'avis.

### Setup release Android (one-time)

Générer la keystore localement :

```bash
keytool -genkey -v \
  -keystore cuisine-release.keystore \
  -alias cuisine -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 cuisine-release.keystore
```

Ajouter les 4 secrets sur GitHub (Settings → Secrets and variables → Actions) :

- `SIGNING_KEYSTORE_B64` : sortie du `base64 -w 0 …`
- `SIGNING_KEYSTORE_PASSWORD`
- `SIGNING_KEY_ALIAS` (ex. `cuisine`)
- `SIGNING_KEY_PASSWORD`

Puis `git tag v0.1.0 && git push --tags` déclenche le workflow et publie l'APK.

## Conventions de code

- **TDD strict en double-loop** : pour toute nouvelle feature, commencer par un test d'intégration Playwright rouge, puis descendre en unit rouge → vert → refactor. Le rouge doit échouer pour la bonne raison (assertion sur le comportement absent, pas un import manquant).
- TypeScript strict partout côté JS.
- Pas de framework dans les pages Astro — Astro components + petits scripts vanilla pour l'interactivité client. On ajoutera React/Svelte seulement si nécessaire.
- Formatage : pas encore fixé. Prettier par défaut si besoin.
- Git : commits conventionnels (`feat:`, `fix:`, `chore:`, etc.) bienvenus mais pas obligatoires. Petits commits > PR géante.

### Stack de test

- **Vitest** pour le unit (`pnpm test` au root, relaye aux workspaces `tooling/` et `web/`). Config minimale : `web/vitest.config.ts` exclut `e2e/`.
- **Playwright** pour le e2e (`pnpm test:e2e` au root, chaîne `build-index` + `playwright test`). Config : `web/playwright.config.ts`, webServer `astro dev --port 4321`, reuse en local, pas en CI.
- Un test d'intégration par fonctionnalité utilisateur (recherche, chips, portions, cuisson nav, cuisson timers, wake lock, entry button).

### Recettes

- `recipes/*.cook` sont **la donnée du propriétaire**. Jamais inventer de recette pour du test — utiliser des fixtures inline dans les specs à la place.

## Références

- Format Cooklang : https://cooklang.org/docs/spec/
- Parser officiel (fallback possible) : https://github.com/cooklang/cooklang-ts
- Astro : https://docs.astro.build/
- MiniSearch : https://lucaong.github.io/minisearch/

## Pour toi, Claude Code

Quand tu prends la main :

1. **Lis ce fichier, `README.md`, et `CONVENTIONS.md`** avant de toucher au code.
2. **Vérifie que la chaîne tourne** : `pnpm install`, `pnpm validate`, `pnpm test`, `pnpm test:e2e`. Règle les soucis avant d'attaquer une feature.
3. **TDD strict, double-loop** : nouveau feature = Playwright rouge d'abord, puis unit rouge-vert-refactor. Voir §"Conventions de code".
4. **Respecte la direction de design** : Fraunces + Manrope, palette corail/papier, pas d'ombres portées ou de gradients, tout doit sentir le livre de cuisine et pas l'app SaaS générique.
5. **Mets à jour ce fichier** quand tu prends une décision structurante : changement de stack, nouveau dossier, alternative retenue vs parser custom, etc.
6. **Demande avant de grosses décisions** : changer le parser, ajouter une dépendance lourde (React, Tailwind, etc.), restructurer le monorepo.
7. **Petits commits.** Une feature par commit avec un message clair > une PR géante.
8. **Ne jamais inventer de recette.** `recipes/` = contenu du propriétaire, fixtures de test restent inline dans les specs.
