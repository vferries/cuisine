# Skill `/import-recette` — design

Date : 2026-07-15. Statut : validé par l'owner.

## Objectif

Formaliser en skill de repo le workflow d'import d'une recette (photo de livre/magazine, texte collé ou URL) vers un fichier `recipes/*.cook` validé et committé. Objectif ~100+ recettes : chaque import doit être rapide, fidèle et sans commit de correction après coup.

Le skill capture ce qui n'est écrit nulle part ailleurs : l'ordre des opérations, les questions à poser avant de transcrire, et les pièges appris lors des imports manuels. Les règles de format restent dans `CONVENTIONS.md` et le validateur — **le skill les référence, ne les duplique pas** (approche validée : skill léger).

## Emplacement et déclenchement

- Fichier : `.claude/skills/import-recette/SKILL.md` (versionné dans le repo).
- Invocation explicite : `/import-recette <photos | texte | url>`.
- Déclenchement automatique via la description du frontmatter : demande de convertir/importer une recette, photo de recette fournie, mention cooklang.
- Plusieurs recettes par invocation possibles (ex. deux photos = deux recettes) : la boucle complète est déroulée par recette, un commit chacune.

## Workflow (contenu du skill)

1. **Lire les entrées et les références.** Photos via lecture d'image, URL via fetch, texte tel quel. Puis lire `CONVENTIONS.md` et une recette récente de `recipes/` comme référence de style. Ne jamais transcrire de mémoire des conventions.
2. **Poser les questions préalables — une seule salve, puis autonomie totale.** Uniquement ce qui manque ou est douteux :
   - `source` absente (magazine, livre, personne) ;
   - `servings` absent ;
   - `difficulty` non indiquée → proposer une estimation à valider ;
   - passage illisible ou quantité manquante ;
   - incohérence entre liste d'ingrédients et étapes (ingrédient listé jamais utilisé → proposer un placement).
   Règle dure : **jamais inventer** — ni quantité, ni ingrédient, ni étape (`recipes/` = contenu du propriétaire).
3. **Transcrire en `.cook`.**
   - Slug selon la règle de nommage de `CONVENTIONS.md`.
   - Unités converties vers la liste autorisée du validateur (`cl`→`ml`, etc.), au singulier.
   - Fractions en décimal (`½`→`0.5`) : le parser ne somme et ne scale que des nombres.
   - Ingrédients dupliqués entre sous-préparations : tagger chaque occurrence avec sa quantité, le parser somme.
   - Timers nommés (`~mijotage{1%h}`), un par durée explicite.
   - Sections : structure du document d'origine si multi-composantes, sinon `Préparation`/`Cuisson`/`Dressage`. Conseils du chef → section `Astuces`.
   - Pas de metadata `image` sans photo du plat (une photo de page de livre n'est pas une photo du plat).
4. **Auditer les tags diététiques.** Repasser la liste d'ingrédients :
   - `végé` : refuser si viande, poisson, ou caché — nuoc-mâm, dashi, bouillon de volaille/bœuf, gélatine, lardons/charcuterie.
   - `sans gluten` : refuser si blé/seigle/orge ou caché — sauce soja standard, panure, farine. Le nuoc-mâm est normalement sans gluten.
   - Littéraux exacts `végé` et `sans gluten` (attendus par les chips). Dans le doute : ne pas tagger.
5. **Vérifier.**
   - `pnpm validate` : zéro erreur ET zéro warning sur la nouvelle recette.
   - `pnpm build-index`, puis contrôler les totaux agrégés (sommes des duplications) contre la liste d'ingrédients d'origine.
6. **Committer.** Un commit par recette, `feat(recipes): <titre court>`, corps de 2-4 lignes max, pas de trailer Co-Authored-By, pas de push automatique — proposer le push une fois toutes les recettes de la session importées. Si une photo du plat est fournie : la déposer en `recipes/images/<slug>.<ext>` et renseigner `>> image:`.

## Gestion d'erreurs

- Entrée partielle ou illisible → question à l'owner, jamais de devinette silencieuse.
- URL inaccessible (paywall, rendu JS) → demander un copier-coller du texte.
- Validation rouge → corriger avant de committer ; ne jamais committer une recette invalide.

## Hors périmètre

- Pas de script outillé dédié (`import-check`) : les commandes existent, le skill les enchaîne.
- Pas de duplication des règles de `CONVENTIONS.md` dans le skill.
- L'ajout de notes post-cuisson à une recette existante n'est pas ce skill : ça reste une édition simple de la section `Astuces`.

## Vérification du skill

- Rédaction avec `superpowers:writing-skills` (structure et qualité des skills).
- Critère d'acceptation : dry-run sur la prochaine recette réelle importée par l'owner — le déroulé doit aller de la photo au commit sans correction après coup.
