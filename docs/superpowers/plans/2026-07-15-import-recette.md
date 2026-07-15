# Skill /import-recette — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Créer `.claude/skills/import-recette/SKILL.md`, le skill de repo qui formalise l'import photo/texte/URL → `.cook` validé et committé.

**Architecture:** Skill léger (approche A de la spec) : le SKILL.md porte le workflow, les questions préalables et les pièges appris ; les règles de format restent dans `CONVENTIONS.md` et `tooling/src/validate-cook.ts`, référencés mais jamais dupliqués. Création en TDD-pour-skills : baseline sans skill (RED), rédaction (GREEN), re-test, fermeture des trous (REFACTOR).

**Tech Stack:** Markdown (SKILL.md + frontmatter YAML), subagents Claude Code pour les tests, git worktree jetable pour isoler les runs de test.

## Global Constraints

- Spec de référence : `docs/superpowers/specs/2026-07-15-import-recette-design.md` (validée par l'owner).
- **Jamais inventer de recette** : les scénarios de test réutilisent le texte réel du « Porc à la sauce arachide » (source : photo du livre fournie par l'owner le 15/07/2026, déjà importée en `recipes/porc-a-la-sauce-arachide.cook` qui sert de référence de comparaison).
- Les runs de test se font dans un git worktree jetable, jamais dans le working tree principal.
- Commits : messages courts (sujet + 2-4 lignes), pas de Co-Authored-By, pas de push.
- Littéraux exacts attendus par les filtres : `végé`, `sans gluten`.

## Fixture commune aux tâches 1 et 3

Texte source du scénario (transcription fidèle de la photo, colonne de droite du livre — c'est l'entrée « texte collé » réaliste) :

```text
PORC À LA SAUCE ARACHIDE
DIFFICULTÉ : FACILE — PRÉPARATION : 30 MIN — CUISSON : 1 H 10 — REPOS : 30 MIN — COÛT : ABORDABLE — POUR 6 PERSONNES

1,2 kg de poitrine de porc / 2 cl d'huile végétale / 2 c. à soupe de cacahuètes grillées / ¼ de botte de coriandre
Pour la marinade : 2 gousses d'ail / 2 cm de gingembre / 1 bâton de citronnelle / 1 c. à café de piment en poudre / 1 c. à soupe de miel / 3 c. à soupe de pâte d'arachide / 3 c. à soupe de sauce soja
Pour la sauce : 1 botte d'oignons nouveaux / 2 gousses d'ail / 2 cm de gingembre / 60 cl de bouillon de légumes / 40 g de fumbwa (feuilles de gazelle, facultatif) / 2 c. à soupe de pâte d'arachide / Sel, poivre

Réalisez la marinade. Émincez finement l'ail, le gingembre et la citronnelle.
Dans un bol, mélangez l'ail, le gingembre, la citronnelle, le piment, le miel, la pâte d'arachide et la sauce soja. Ajoutez 2 c. à soupe d'eau chaude pour obtenir une consistance homogène et mélangez à nouveau.
Coupez la poitrine de porc en morceaux moyens. Badigeonnez-les de marinade et réservez au frais pendant 30 min.
Dans une sauteuse, versez l'huile et saisissez les morceaux de poitrine de porc de chaque côté. Réservez.
Réalisez la sauce. Émincez les oignons nouveaux, l'ail et le gingembre. Faites-les suer dans la sauteuse où vous avez saisi les morceaux de porc. Versez le bouillon de légumes, le reste de marinade s'il y en a, le fumbwa et les morceaux de poitrine de porc grillés. Faites mijoter à feu moyen pendant 1 h. Goûtez et rectifiez l'assaisonnement si nécessaire.
Servez avec du riz blanc parsemé de cacahuètes concassées et de coriandre ciselée.
LES CONSEILS DU CHEF ANTO : Le fumbwa n'est pas obligatoire. Si vous ne mangez pas de viande de porc, vous pouvez la remplacer par de la viande bovine, de la volaille ou du poisson blanc.
```

## Grille de scoring (utilisée en tâches 1, 3 et 4)

| # | Critère observable | Attendu |
|---|---|---|
| 1 | Lit `CONVENTIONS.md` (ou une recette existante) avant de transcrire | oui |
| 2 | Pose ses questions AVANT la transcription, en une seule salve | oui (ici : au minimum la `source`, absente du texte) |
| 3 | N'invente rien (pas de quantité/étape ajoutée sans signalement) | oui |
| 4 | Unités converties : `cl`→`ml`, `1,2`→`1.2`, `½`/`¼`→décimal | oui |
| 5 | Ingrédients dupliqués taggés à chaque occurrence (ail, gingembre, pâte d'arachide) | oui |
| 6 | Timers nommés (`~marinade{30%min}`, `~mijotage{1%h}`) | oui |
| 7 | Conseils du chef → section `Astuces` | oui |
| 8 | Audit diététique : ni `végé` ni `sans gluten` (porc + sauce soja) | oui |
| 9 | `pnpm validate` lancé, zéro erreur/warning sur la nouvelle recette | oui |
| 10 | `pnpm build-index` lancé + totaux agrégés contrôlés | oui |
| 11 | Commit `feat(recipes): …`, corps court, sans Co-Authored-By | oui |
| 12 | Pas de push | oui |

---

### Task 1: Baseline sans skill (RED)

**Files:**
- Create: `/tmp/claude-1000/-home-vincent-projects-cuisine/*/scratchpad/baseline-notes.md` (notes de scoring, hors repo)
- Aucun fichier du repo principal modifié.

**Interfaces:**
- Produces: liste des échecs baseline (critères ratés + rationalisations verbatim du subagent), consommée par la tâche 2 pour cibler le contenu du skill.

- [ ] **Step 1: Créer le worktree jetable et retirer la recette de référence**

```bash
git worktree add /tmp/claude-1000/import-skill-baseline -b test/import-baseline
cd /tmp/claude-1000/import-skill-baseline
git rm -q recipes/porc-a-la-sauce-arachide.cook
git commit -q -m "test: retire la recette pour scénario baseline"
```

- [ ] **Step 2: Dispatcher le subagent baseline (sans skill)**

Prompt exact du subagent (type `general-purpose`, cwd = le worktree) :

> Tu travailles dans /tmp/claude-1000/import-skill-baseline (repo de recettes Cooklang).
> Importe la recette suivante dans le projet, au format attendu par le repo, jusqu'au commit inclus. Débrouille-toi seul autant que possible.
> [FIXTURE — coller ici le texte complet de la section « Fixture commune »]

Ne PAS mentionner le skill, la spec, ni la grille. Ne pas guider.

- [ ] **Step 3: Scorer le résultat contre la grille**

Comparer le `.cook` produit à `recipes/porc-a-la-sauce-arachide.cook` de main (référence) + passer les 12 critères. Noter verbatim les rationalisations (ex. « j'ai estimé la difficulté à moyenne » sans question). Écrire `baseline-notes.md` : critères KO, citations.

- [ ] **Step 4: Vérifier que le test échoue (RED)**

Attendu : plusieurs critères KO (typiquement 2, 8, 9, 11 — questions non posées, tags non audités, warnings tolérés, format de commit). Si la baseline passe TOUT : le skill est inutile tel quel — STOP, retour à l'owner avec ce constat avant d'écrire quoi que ce soit.

- [ ] **Step 5: Nettoyer le worktree**

```bash
cd /home/vincent/projects/cuisine
git worktree remove --force /tmp/claude-1000/import-skill-baseline
git branch -D test/import-baseline
```

---

### Task 2: Écrire SKILL.md (GREEN)

**Files:**
- Create: `.claude/skills/import-recette/SKILL.md`

**Interfaces:**
- Consumes: échecs baseline de la tâche 1 — si un échec observé n'est pas couvert par le contenu ci-dessous, ajouter le contre-point (table des pièges).
- Produces: le skill complet, testé en tâche 3.

- [ ] **Step 1: Écrire le fichier avec ce contenu exact** (ajusté des échecs baseline si nécessaire)

```markdown
---
name: import-recette
description: Use when the owner provides a recipe to import or convert for this repo — photo of a cookbook or magazine page, pasted recipe text, or recipe URL — or mentions adding a recipe in cooklang/.cook format.
---

# Import de recette

Transforme une recette source (photo, texte collé, URL) en `recipes/<slug>.cook` validé et committé, fidèle au document d'origine. Plusieurs recettes fournies = dérouler tout le workflow pour chacune, un commit par recette.

**Règle dure : ne jamais inventer.** Ni quantité, ni ingrédient, ni étape. `recipes/` est le contenu du propriétaire — un passage illisible ou une donnée manquante se résout par une question, jamais par une estimation silencieuse.

## Workflow

1. **Lire les sources et les références.** Photos via Read, URL via WebFetch — si l'URL résiste (paywall, rendu JS), demander un copier-coller du texte. Puis lire `CONVENTIONS.md` et une recette récente de `recipes/` : les règles de format (metadata, unités, sections, nommage) vivent là-bas et dans `tooling/src/validate-cook.ts`, pas ici. Ne jamais transcrire de mémoire du spec cooklang générique.

2. **Poser toutes les questions en une seule salve** (AskUserQuestion), puis dérouler en autonomie totale jusqu'au commit. À couvrir si manquant ou douteux :
   - `source` (livre, magazine, personne, site) ;
   - `servings` absent ;
   - `difficulty` non indiquée → proposer une estimation à valider ;
   - passage illisible ou quantité manquante ;
   - ingrédient listé mais absent des étapes → proposer un placement.

3. **Transcrire.** Slug selon la règle de `CONVENTIONS.md`. Unités converties vers la liste du validateur (`cl`→`ml`, virgule décimale→point), au singulier. Fractions en décimal (`½`→`0.5`) : le parser ne somme et ne scale que des nombres. Ingrédient répété entre sous-préparations (marinade + sauce) : tagger chaque occurrence avec sa quantité, le parser somme — ne pas déplacer les quantités. Un timer nommé par durée explicite (`~mijotage{1%h}`). Sections : reprendre la structure du document si multi-composantes, sinon `Préparation`/`Cuisson`/`Dressage` ; conseils du chef → `Astuces`. Pas de `>> image:` sans photo du plat — une photo de page de livre n'est pas une photo du plat.

4. **Auditer les tags diététiques** en repassant la liste finale d'ingrédients :
   - `végé` : refuser si viande ou poisson, y compris caché — nuoc-mâm, dashi, bouillon de volaille/bœuf, gélatine, lardons/charcuterie.
   - `sans gluten` : refuser si blé/seigle/orge, y compris caché — sauce soja standard, panure, farine. Le nuoc-mâm est normalement sans gluten.
   - Littéraux exacts `végé` et `sans gluten` (attendus par les chips de filtre). Dans le doute : ne pas tagger.

5. **Vérifier.** `pnpm validate` : zéro erreur ET zéro warning sur la nouvelle recette (les warnings préexistants d'autres recettes ne bloquent pas). `pnpm build-index`, puis comparer les totaux agrégés dans `web/src/generated/recipes/<slug>.json` à la liste d'ingrédients d'origine — c'est là qu'une occurrence oubliée ou une somme fausse se voit.

6. **Committer.** Un commit par recette : `feat(recipes): <titre court>`, corps de 2-4 lignes max, pas de trailer Co-Authored-By. Jamais de push automatique — le proposer une fois toutes les recettes de la session importées.

## Pièges connus

| Piège | Réalité |
|---|---|
| « La quantité est sûrement 200 g » | Illisible = question à l'owner. Jamais d'estimation silencieuse. |
| « Je connais le cooklang, pas besoin des docs » | Les conventions du repo divergent du spec générique (unités autorisées, sections nommées, timers nommés). Lire `CONVENTIONS.md` d'abord. |
| « Un warning de validation, ça passe » | Pas sur une nouvelle recette. Corriger avant de committer. |
| « Ça a l'air végé / sans gluten » | Vérifier les cachés (nuoc-mâm, dashi, bouillon, gélatine ; sauce soja, panure). Dans le doute, pas de tag. |
| « Je committe, on corrigera après » | Les questions se posent AVANT la transcription, en une seule salve. Zéro commit de correction attendu. |
```

- [ ] **Step 2: Vérifier la conformité du fichier**

```bash
head -4 .claude/skills/import-recette/SKILL.md   # frontmatter name + description présents
wc -w .claude/skills/import-recette/SKILL.md     # attendu : < 700 mots
```

---

### Task 3: Re-test avec skill (GREEN check)

**Files:**
- Aucun fichier du repo principal modifié (worktree jetable).

**Interfaces:**
- Consumes: SKILL.md de la tâche 2, fixture et grille communes.
- Produces: verdict par critère ; les critères encore KO alimentent la tâche 4.

- [ ] **Step 1: Recréer le worktree** (mêmes commandes que Task 1 Step 1, branche `test/import-green`) — le SKILL.md committé n'existe pas encore sur main : copier `.claude/skills/` du working tree principal dans le worktree.

- [ ] **Step 2: Dispatcher le même subagent, même prompt**, en ajoutant UNE ligne au début : « Le repo contient un skill d'import : lis .claude/skills/import-recette/SKILL.md et suis-le. »

- [ ] **Step 3: Scorer contre la même grille de 12 critères.**

Attendu : les critères KO de la baseline passent. Le subagent doit notamment s'arrêter pour demander la `source` (critère 2) au lieu de committer sans elle.

- [ ] **Step 4: Nettoyer le worktree** (mêmes commandes que Task 1 Step 5).

---

### Task 4: Fermer les trous (REFACTOR)

**Files:**
- Modify: `.claude/skills/import-recette/SKILL.md`

- [ ] **Step 1:** Si des critères restent KO en tâche 3 : ajouter le contre-point exact dans la table « Pièges connus » (rationalisation verbatim → réalité), ou renforcer l'étape du workflow concernée. Pas de clause de nuance (« sauf si… ») : une exception réelle devient une condition observable.

- [ ] **Step 2:** Re-dérouler la tâche 3 (nouveau worktree, `test/import-refactor`). Boucler jusqu'à 12/12. Si 3 itérations ne suffisent pas : STOP, remonter à l'owner — le problème est structurel (spec ou format du skill), pas rédactionnel.

---

### Task 5: Livrer

**Files:**
- Modify: `AGENTS.md` (roadmap : cocher le skill d'import)
- Create (commit): `.claude/skills/import-recette/SKILL.md`

- [ ] **Step 1: Committer le skill**

```bash
git add .claude/skills/import-recette/SKILL.md
git commit -m "feat: skill /import-recette (photo/texte/URL vers .cook validé)"
```

- [ ] **Step 2: Mettre à jour AGENTS.md** — passer la ligne roadmap de « en cours de design » à fait, avec une ligne décrivant le skill. Committer (`docs: skill import-recette livré`).

- [ ] **Step 3: Annoncer le critère d'acceptation final à l'owner** : dry-run sur sa prochaine recette réelle — de la photo au commit sans commit de correction. Pas de push sans demande explicite.
