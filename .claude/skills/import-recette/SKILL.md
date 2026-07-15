---
name: import-recette
description: Use when the owner provides a recipe to import or convert for this repo — photo of a cookbook or magazine page, pasted recipe text, or recipe URL — or mentions adding a recipe in cooklang/.cook format.
---

# Import de recette

Transforme une recette source (photo, texte collé, URL) en `recipes/<slug>.cook` validé et committé, fidèle au document d'origine. Plusieurs recettes fournies = dérouler tout le workflow pour chacune, un commit par recette.

**Règle dure : ne jamais inventer.** Ni quantité, ni ingrédient, ni étape, ni metadata déduite. `recipes/` est le contenu du propriétaire — un passage illisible ou une donnée manquante se résout par une question, jamais par une estimation silencieuse. Un choix signalé après commit n'est pas une question posée avant.

## Workflow

1. **Lire les sources et les références.** Photos via Read, URL via WebFetch — si l'URL résiste (paywall, rendu JS), demander un copier-coller du texte. Puis lire `CONVENTIONS.md` et une recette récente de `recipes/` : les règles de format (metadata, unités, sections, nommage) vivent là-bas et dans `tooling/src/validate-cook.ts`, pas ici.

2. **Poser toutes les questions en une seule salve**, puis dérouler en autonomie totale jusqu'au commit. Via AskUserQuestion si l'outil est disponible ; sinon (sub-agent), s'arrêter et retourner la salve comme réponse — le travail reprend une fois les réponses reçues. Aucun commit tant qu'une question est sans réponse. À couvrir si manquant ou douteux :
   - `source` (livre, magazine, personne, site) ;
   - `cuisine` non écrite dans le document → proposer une déduction à valider, ne pas la décider seul ;
   - `servings` absent ;
   - `difficulty` non indiquée → proposer une estimation à valider ;
   - passage illisible ou quantité manquante ;
   - ingrédient listé mais absent des étapes → proposer un placement ;
   - La `saison:` (`toute l'année` ou plage `<mois>-<mois>`) quand elle n'est pas évidente d'après les ingrédients — proposer une valeur dans la salve, jamais la décider seul.

3. **Transcrire.** Slug selon la règle de `CONVENTIONS.md`, appliquée mécaniquement, articles inclus (« Porc à la sauce arachide » → `porc-a-la-sauce-arachide`, pas `porc-sauce-arachide`). Unités converties vers la liste du validateur (`cl`→`ml`, virgule décimale→point), au singulier. Fractions en décimal (`½`→`0.5`) — le parser ne manipule que des nombres. Tout ingrédient des étapes se tague, accompagnement compris (`@riz blanc{}`). Ingrédient répété entre sous-préparations (marinade + sauce) : tagger chaque occurrence avec sa quantité, le parser somme — ne pas déplacer les quantités. Un timer nommé par durée explicite (`~mijotage{1%h}`). Sections : reprendre la structure du document si multi-composantes, sinon `Préparation`/`Cuisson`/`Dressage` ; conseils du chef → `Astuces`. Pas de `>> image:` sans photo du plat — une photo de page de livre n'est pas une photo du plat.

4. **Auditer les tags diététiques** en repassant la liste finale d'ingrédients :
   - `végé` : refuser si viande ou poisson, y compris caché — nuoc-mâm, dashi, bouillon de volaille/bœuf, gélatine, lardons/charcuterie.
   - `sans gluten` : refuser si blé/seigle/orge, y compris caché — sauce soja standard, panure, farine. Le nuoc-mâm est normalement sans gluten.
   - Littéraux exacts `végé` et `sans gluten` (attendus par les chips de filtre). Dans le doute : ne pas tagger.

5. **Vérifier.** `pnpm validate` : zéro erreur ET zéro warning sur la nouvelle recette (les warnings préexistants d'autres recettes ne bloquent pas). `pnpm build-index`, puis comparer les totaux agrégés dans `web/src/generated/recipes/<slug>.json` à la liste d'ingrédients d'origine — c'est là qu'une occurrence oubliée ou une somme fausse se voit.

6. **Committer.** Si une photo du plat est fournie : la déposer en `recipes/images/<slug>.<ext>` (le build convertit en WebP) et renseigner `>> image:` avant de committer. Un commit par recette : `feat(recipes): <titre court>`, corps de 2-4 lignes max, pas de trailer Co-Authored-By. Jamais de push automatique — le proposer une fois toutes les recettes de la session importées.

## Pièges connus

| Piège | Réalité |
|---|---|
| « La quantité est sûrement 200 g » | Illisible = question à l'owner. Jamais d'estimation silencieuse. |
| « Je peux déduire la cuisine/la source du contexte » | Une inférence n'est pas une donnée. La proposer dans la salve, pas la décider seul. |
| « Je connais le cooklang, pas besoin des docs » | Les conventions du repo divergent du spec générique (unités autorisées, sections nommées, timers nommés). Lire `CONVENTIONS.md` d'abord. |
| « Un warning de validation, ça passe » | Pas sur une nouvelle recette. Corriger avant de committer. |
| « Ça a l'air végé / sans gluten » | Vérifier les cachés (nuoc-mâm, dashi, bouillon, gélatine ; sauce soja, panure). Dans le doute, pas de tag. |
| « Je committe, on corrigera après » | Les questions se posent AVANT la transcription, en une seule salve. Zéro commit de correction attendu. |
| « Pas d'outil de question ici, je tranche et je signale » | S'arrêter et retourner la salve de questions est toujours possible. Un rapport post-commit n'est pas une validation. |
