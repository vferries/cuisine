# Notification timer → deep-link étape — design

Date : 2026-07-16
Statut : validé par l'owner

## Problème

Quand un minuteur de cuisson sonne, la notification Android n'a aucun
`contentIntent` : taper dessus ne fait rien. On veut que le tap ouvre
l'écran cuisson de la bonne recette, directement sur l'étape où le timer
a été lancé.

## Décisions

- **Cible du tap** : écran cuisson à l'étape du timer (celle où il a été
  démarré), avec le back stack complet **Home → Recette → Cuisson**.
- **App déjà ouverte** : le tap navigue d'office, quel que soit l'écran
  courant (comportement standard des notifications).
- **Approche retenue (B)** : PendingIntent vers `MainActivity` avec extras
  (slug, sectionIdx, stepIdx) + navigation manuelle dans le NavHost.
  Rejetées : navDeepLink Compose (back stack synthétique d'un graphe plat
  retombe sur home, pas de pile Recette) et NavDeepLinkBuilder (pensé
  graphes XML, mal adapté au DSL Compose).

## Composants

### 1. Parsing de l'id timer (`data/timers/`)

L'id encode déjà la cible : `slug:sectionIdx:stepIdx:tokIdx` (posé dans
`CuissonScreen`). Fonction pure `parseTimerId(id: String): TimerTarget?`
(slug, sectionIdx, stepIdx, tokIdx) — `null` si malformé, loggé.

### 2. Notification (`TimerNotifier`)

`notifyExpired` parse l'id ; si cible valide, attache un `contentIntent` :
`PendingIntent.getActivity` vers `MainActivity`, extras slug/sectionIdx/
stepIdx, `requestCode = id.hashCode()` (un PendingIntent par timer, pas
d'écrasement), `FLAG_IMMUTABLE`. Id malformé → notification sans
contentIntent (comportement actuel).

### 3. Réception (`MainActivity`, `CuisineNavHost`)

- `launchMode="singleTop"` dans le manifest ; `onNewIntent` (app ouverte)
  et `onCreate` (app fermée) lisent les extras vers un état
  `TimerDeepLink?` observé par le NavHost.
- `LaunchedEffect` dans `CuisineNavHost` : `popUpTo(home)` puis
  `navigate("recipe/$slug")` puis `navigate("cuisson/$slug?section=S&step=T")`.
  L'état est remis à null après consommation (pas de re-navigation en
  recomposition).

### 4. Étape ciblée (`CuissonScreen`)

Route `cuisson/{slug}` : deux arguments optionnels `section`/`step`
(défaut −1 = comportement actuel, étape 0). Résolution par fonction pure
`flatIndexOf(steps, sectionIdx, stepIdx): Int` (−1 si absent → étape 0 via
le clamp existant), appliquée une fois la recette chargée.

## Cas limites

- **Recette hors cache / offline** : `CuissonScreen` affiche déjà l'état
  `Error` ; le back stack reste cohérent.
- **Recette modifiée depuis** (étape disparue) : `flatIndexOf` → −1 →
  étape 0.
- **Id malformé** : pas de contentIntent, log.
- **Permission notifs refusée** : inchangé (aucune notification, déjà le
  cas aujourd'hui).

## Tests

Double-loop, unit sur les fonctions pures :

- `parseTimerId` : id valide, id malformé (segments manquants, indices
  non numériques).
- `flatIndexOf` : étape trouvée, absente.
- Les branches notification/navigation restent fines et déclaratives ;
  AlarmManager et le tap réel ne sont pas testables en JVM → vérification
  manuelle sur device.

## Découpage en commits

1. `feat(android)` : `parseTimerId` + contentIntent sur la notification.
2. `feat(android)` : route `section`/`step` + navigation deep-link
   (MainActivity, NavHost, CuissonScreen).
3. `docs` : AGENTS.md (idée cochée, mécanique décrite).
