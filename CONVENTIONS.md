# Conventions des fichiers `.cook`

Format : Cooklang canonique (https://cooklang.org/docs/spec/) + extensions listées ci-dessous.

## Nommage des fichiers

- `kebab-case.cook` sans diacritiques. Le nom sans extension est le **slug** : identifiant d'URL sur le web, clé de cache sur Android, primary key de l'index.
- Exemples :
  - `porc-bigorre-caramel.cook`
  - `risotto-champignons.cook`
  - `tarte-tatin-pommes.cook`
- Règle de génération : titre → minuscules → accents supprimés → espaces et ponctuation → `-` → troncature si > 50 caractères.

## Métadonnées (en-tête)

Syntaxe : `>> clé: valeur`, une par ligne, tout en haut du fichier avant toute étape.

### Requises

| Clé | Type | Exemple |
|---|---|---|
| `title` | texte libre | `Porc noir de Bigorre confit au caramel` |
| `servings` | entier | `2` |
| `prep time` | durée | `20 min` |
| `cook time` | durée | `25 min` |
| `difficulty` | enum | `facile` · `moyenne` · `difficile` |
| `cuisine` | texte (féminin singulier) | `vietnamienne`, `italienne`, `française` |

### Optionnelles

| Clé | Usage |
|---|---|
| `source` | Origine de la recette (personne, livre, site) |
| `region` | Précision géographique, affichée en sous-titre |
| `tags` | Liste virgule, nourrit la recherche et les chips de filtre |
| `image` | Nom du fichier image dans `images/`, sans chemin |

## Sections

Syntaxe : `== Nom ==` sur une ligne seule.

Sections standards, dans cet ordre :

1. `Préparation` — tout ce qui précède la cuisson.
2. `Cuisson` — étapes au feu.
3. `Dressage` — mise en assiette et finition.
4. `Astuces` — bloc spécial (voir plus bas).

Une recette simple peut n'avoir aucune section — toutes les étapes sont alors regroupées dans une section implicite "Étapes".

### Section spéciale `Astuces`

Les paragraphes de cette section sont rendus **comme un bloc aside coral à la fin de la recette**, pas comme des étapes numérotées. Utilisée pour les tips généraux qui ne s'attachent pas à une étape précise.

## Ingrédients

Syntaxe : `@nom{qté%unité}`.

- Multi-mot : accolades obligatoires — `@porc noir de Bigorre{}`.
- Quantité sans unité (items comptables) : `@piment{1}`, `@oeuf{3}`.
- Quantité non spécifiée : `@nuoc-mâm{}` — rendu "au goût" en italique dans l'UI.
- **Duplications** : `@sucre{3%càc}` puis `@sucre{3%càc}` plus loin → le parser somme automatiquement et la liste d'ingrédients affichera "sucre : 6 càc".

### Unités

Métriques : `g`, `kg`, `ml`, `l`.

Française courantes :
- `càc` — cuillère à café (5 ml), rendue `c. à c.` dans l'UI.
- `càs` — cuillère à soupe (15 ml), rendue `c. à s.`.
- `pincée`, `brin`, `bouquet`, `sachet`, `gousse` — quantité de comptage, rendue tel quel.

## Ustensiles

Syntaxe : `#nom{}` (ou `#nom{qté}` si compte précis).

- Multi-mot : accolades obligatoires — `#planche à découper{}`.
- Duplications : `#poêle{}` mentionné deux fois → UI affiche "2 poêles".

Ustensiles courants : `#poêle{}`, `#casserole{}`, `#saladier{}`, `#bol{}`, `#cuillère{}`, `#fouet{}`, `#baguettes{}`, `#planche à découper{}`, `#four{}`, `#robot{}`.

## Minuteurs

Syntaxe : `~nom{durée%unité}`.

- **Toujours nommer** le timer : le nom apparaît dans l'UI quand plusieurs timers tournent en parallèle. `~mijotage{15%min}` est plus lisible que `~{15%min}`.
- Unités : `sec`, `min`, `h`.

Le mode cuisson démarre automatiquement le timer au passage sur l'étape concernée. Si l'étape en contient plusieurs, tous démarrent ensemble.

## Commentaires

- `-- commentaire` une ligne, ignoré par le parser.
- `[- commentaire -]` multi-ligne, ignoré par le parser (non implémenté dans le parser custom actuel).

Pour les notes qui doivent apparaître dans l'UI, utiliser la section `Astuces` plutôt que des commentaires.

## Exemple de référence

Voir [`recipes/porc-bigorre-caramel.cook`](./recipes/porc-bigorre-caramel.cook).

## Workflow d'ajout d'une recette

1. Créer `recipes/<slug>.cook` en suivant ce doc.
2. (Optionnel) Déposer `recipes/images/<slug>.webp` (1024×768, ~150 Ko) — les thumbnails 320×240 seront générés automatiquement par le build (pas encore implémenté).
3. `pnpm build-index` pour régénérer `web/src/generated/` localement.
4. `git commit && git push`. À terme, GitHub Actions redéploiera Pages et l'app Android verra la nouvelle recette au prochain sync.

## Validation

À terme, chaque PR déclenchera `tooling/validate-cook.ts` qui vérifiera :

- Parse réussi.
- Métadonnées requises présentes et bien typées.
- Slug cohérent avec le nom de fichier.
- Unités utilisées dans la liste autorisée (avertissement sinon).
- Image référencée existe dans `images/` (si précisée).
