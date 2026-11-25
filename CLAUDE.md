# EarthQuake Faker (Feinteur de Séisme)

Jeu navigateur de réflexion et de construction inspiré de Poly Bridge : le joueur (Félix Fixe Junior) construit des structures qui doivent résister à un séisme.
Specs complètes : `docs/IHM.md` (à lire avant toute tâche de conception).
Équipe : Ilann, Waed. Projet universitaire (UBO).

## Stack

- Backend : Java 17+, Spring Boot 3.x, H2 en mémoire (dossier `/backend`)
- Frontend : React + `<canvas>` (dossier `/frontend`)
- Images : seulement le décor (ciel, immeubles, fumée) dans `frontend/public/images/decor/` ; liens, nœuds et matériaux restent dessinés avec des formes colorées (voir Rendu). Sons : quelques-uns sont intégrés (séisme, choix de pièce, pose, recommencer) ; les autres ressources restent dans `docs/utilitaire/son/`.

## Règles du jeu (résumé)

- Chaque niveau impose un budget maximum et une hauteur minimale à atteindre.
- Zone de construction : largeur fixe 5 m (X de 0 à 5), hauteur 10 à 15 m selon le niveau (Y de 0 au sol).
- Les nœuds sont fixes et prédéfinis (pas de placement libre). Certains sont des ancrages au sol (`estAncrageSol`).
- Le joueur relie des nœuds avec des liens (barres) choisis dans un panneau de matériaux (bois, béton, acier, amortisseur) en glisser-déposer.
- Le séisme est simulé quand budget et hauteur sont respectés. Un lien dont le stress dépasse sa résistance casse et le niveau est perdu.
- Les niveaux et leurs nœuds sont chargés depuis `backend/src/main/resources/niveaux.json` au démarrage et persistés dans H2.
- Chaque niveau doit avoir au moins une solution gagnante (voir Tests).
- Les nœuds sont sur une grille de 2,5 m (X ∈ {0 ; 2,5 ; 5}, pas vertical 2,5 m). Un lien ne relie que des nœuds voisins : longueur ≤ pas × √2 ≈ 3,54 m (`STR-005`), sans passer sur un autre nœud (`STR-006`).
- Chaque matériau a une longueur maximale par lien (`longueurMax` dans `materiaux.json` : bois et acier 3,54 m, béton et amortisseur 2,5 m, donc pas de diagonale en béton) : `MAT-003`.
- Une structure instable (un carré sans diagonale se déforme) s'effondre ; seule la partie instable tombe, le reste garde ses vrais taux de stress. La hauteur compte avec les seuls liens intacts reliés au sol.

## API

- `GET /api/niveaux` : liste des niveaux pour la carte (`id`, `numero`, `nom`, `themeDecor`, `budgetMax`, `hauteurMinRequise`).
- `GET /api/niveaux/{id}` : paramètres du niveau (`budgetMax`, `hauteurMinRequise`, `themeDecor`, `largeurZone`, `hauteurZone`, `longueurMaxLien`, `materiauxAutorises`) et nœuds (`id`, `posX`, `posY`, `estAncrageSol`). Sans `magnitudeSeisme` : elle sert au moteur.
- `GET /api/materiaux` : matériaux (`id`, `nom`, `couleurHex`, `coutUnitaire`, `masseLineique`, `longueurMax`), du moins cher au plus cher. Les résistances ne sont pas exposées.
- `POST /api/niveaux/{id}/simuler` : reçoit `{ "liens": [{ "noeudDepartId", "noeudArriveeId", "materiauId" }] }`, renvoie `{ estValide, scoreSoliditeGlobal, messageBilan, detailsLiens: [{ noeudDepartId, noeudArriveeId, tauxStress, estCasse, tempsRupture }] }`. `tempsRupture` (secondes, `null` si le lien tient) sert à l'animation. Une structure qui casse est un résultat normal (200, `estValide: false`) ; une structure qui viole une règle de construction est une erreur (400 ou 422).
- Un `id` de niveau non numérique donne `NIV-003`.
- CORS autorisé pour `localhost:5173` et `localhost:3000` (GET, POST, OPTIONS sur `/api/**`). Console H2 activée sur `/h2-console`.

## Gestion des erreurs

- Toutes les exceptions passent par un `@RestControllerAdvice` global et renvoient ce JSON : `{ timestamp, codeErreur, contexte, message }`.
- Format du code : `[CONTEXTE]-[CAUSE]`, avec les contextes `NIV`, `STR`, `BUD`, `MAT`, `SIM`, `SYS` (erreurs techniques) et une cause à 3 chiffres ou un mot-clé explicite (ex : `BUD-001`).
- Codes de construction : `STR-001` à `STR-006`, `BUD-001`, `MAT-001` à `MAT-003`, `SIM-001`. Le statut HTTP de chaque code est défini dans `CodeErreur` ; la liste complète est dans `CODES_ERREURS.md`.
- Un test vérifie que chaque `CodeErreur` figure dans `CODES_ERREURS.md`. Skill du projet : `.claude/skills/ajouter-code-erreur`.
- Tout nouveau code d'erreur doit être ajouté dans `CODES_ERREURS.md` (signification + cause fréquente) dans le même commit. Le modèle de départ est dans `docs/IHM.md`, section 4.

## Rendu frontend

- Décor : `Decor.jsx` empile trois images (`sol-ciel`, `fumee`, `fond-batiment`) derrière le canvas, qui est transparent avec un voile blanc sur la zone de construction. Le haut de l'herbe est aligné sur le Y = 0 du niveau (`positionDecor`). Le même décor sert à l'écran titre et à la carte (`EcranAvecDecor`). Fumée et tremblement du décor en CSS, coupés par `prefers-reduced-motion`. Image absente : dégradé de couleur de repli.
- Écran titre affiché une fois par chargement (état en mémoire, pas de `localStorage`).
- Lien : rectangle de longueur = distance entre les deux nœuds, pivoté selon l'angle, couleur du matériau (`couleurHex`) avec une texture par matériau dessinée dans le canvas (`dessinMateriaux.js`, partagée avec l'étagère), sans image.
- Nœud : plaque métallique ronde avec une tête de boulon. Ancrage au sol : platine carrée à quatre boulons, posée sur la dalle de fondation. Zone de construction : dalle de béton sous le sol et échafaudage en pointillés (pas de rectangle).
- Pendant le séisme : teinte du lien interpolée de vert à rouge selon `tauxStress` (par-dessus la texture), l'écran et le décor tremblent, de la poussière monte du sol et à chaque rupture, et un grondement est joué (`sons.js`, fichiers dans `frontend/public/sons/`, copiés de `docs/utilitaire/son/`).
- Félix Fixe Junior (`dessinFelix.js`) se tient à droite de la structure, 3 états : inquiet (construction et séisme), content (victoire), découragé (échec). Les messages d'aide, d'avertissement et d'erreur sont dans une bulle (`BulleFelix.jsx`) au-dessus de lui. Règle graduée à gauche de l'échafaudage, drapeau à la hauteur cible.
- Cadrage : l'échelle dépend de `hauteurUtile` (nœud le plus haut ou hauteur cible, plus 1,2 m), pas de `hauteurZone`.
- Police de l'UI : une seule police (système, `--police` dans `index.css`) pour toute l'interface, 16 px minimum, y compris dans le canvas. La police Pokémon de `frontend/public/fonts/pokemon/` (fournie par Ilann) n'est plus utilisée : ne pas la supprimer sans demander.
- Mise en page : le canvas occupe toute la fenêtre (décor derrière, HUD en boîtes de dialogue `.boite` par-dessus, étagère de matériaux en bas) ; `calculerVue` donne l'échelle (jusqu'à 80 px/m) et l'origine en pixels selon l'espace disponible.
- Aide au glisser : anneau vert sur les nœuds où la pièce tenue peut se poser (`noeudsCompatibles`). Annulation par bouton et Ctrl+Z.
- Rupture : le lien cassé se coupe en deux, les moitiés tombent puis disparaissent en fondu. À l'échec, le point de rupture du premier lien cassé est entouré d'un cercle rouge qui pulse. Résultat : boîte de dialogue de 2 lignes (ce qui s'est passé + un conseil), solidité en 1 à 3 étoiles (seuils 40 et 70) à la victoire, boutons Réessayer / Niveau suivant.
- Les coordonnées métriques du backend sont converties en pixels côté front uniquement.

## Décisions de conception (validées par Ilann)

1. Moteur sismique : calcul statique côté Spring (`MoteurSismique`), pas de physique côté front. Charges par nœud (poids + poussée latérale proportionnelle à la masse), treillis à nœuds articulés résolu par la méthode des rigidités, séisme dans les deux sens (on garde le pire). Un lien casse quand son effort dépasse sa résistance. Pas de redistribution après rupture. Valeurs de jeu réglables : `FACTEUR_SISMIQUE` (`CalculateurCharges`), résistances et masses (`materiaux.json`), amortisseur (`CalculateurStress`).
2. `ResultatSimulation` : `estValide`, `scoreSoliditeGlobal`, `messageBilan` et `detailsLiens` (liste de `ResultatLien` : ids, `tauxStress`, `estCasse`, `tempsRupture`). Score = 100 moins la moyenne des taux de stress (chacun plafonné à 1) ; un niveau perdu vaut toujours 0.
3. `Lien` est un record d'entrée immuable ; l'état de simulation vit dans `ResultatLien`.
4. Matériaux pilotés par les données (`materiaux.json`), pas de hiérarchie de classes ; ids texte (`BOIS`, `BETON`, `ACIER`, `AMORTISSEUR`). Seul l'amortisseur a un comportement dédié (il retire 30 % de la poussée latérale sur les nœuds qu'il touche, 90 % au plus).
5. Hauteur (`STR-004`), ancrage (`SIM-001`) et bilan contrôlent la connexité : seuls comptent les nœuds reliés au sol par un chemin de liens (intacts, pour le bilan).

Toute nouvelle décision de ce type : proposer un choix, attendre la validation d'Ilann.

## Conventions de code

- Java : packages par fonctionnalité (`niveau`, `structure`, `materiau`, `simulation`, `erreur`). Noms de classes et méthodes en français comme dans l'UML (`Niveau`, `Noeud`, `Lien`, `MoteurSismique`).
- Pas de logique métier dans les contrôleurs : contrôleur, service, moteur.
- React : composants fonctionnels et hooks. Pas de `localStorage` pour l'état du jeu, pas de nouvelle dépendance sans demander.
- Code et commentaires : noms en français pour le domaine métier, anglais accepté pour le technique. Commentaires courts, seulement quand le pourquoi n'est pas évident.

## Tests

- Chaque règle du moteur sismique a un test JUnit (structure vide, ancrage manquant, budget dépassé, lien trop long, lien sur un nœud, longueur max du matériau, rupture, succès).
- Chaque niveau de `niveaux.json` a une solution de référence (`backend/src/test/resources/solutions-reference.json`) qui doit gagner sous le budget dans un test automatique (`SolutionsReferenceTests`).
- Lancer les tests avant chaque commit.

## Workflow

- Les 5 jalons ci-dessous sont terminés. Pour toute évolution : proposer un plan d'abord, attendre l'accord, puis coder. Historique des jalons :
  1. Backend : `niveaux.json`, H2, `GET /api/niveaux/{id}`, gestion d'erreurs
  2. Moteur sismique et ses tests, sans front
  3. Front : rendu des nœuds et des liens, glisser-déposer
  4. `POST /simuler` et animation du séisme
  5. Carte du monde et enchaînement des niveaux
- Pour chaque jalon : proposer un plan d'abord, attendre mon accord, puis coder.
- Un commit par étape cohérente, message en français à l'impératif (ex : `Ajouter le chargement de niveaux.json`).
- Rester dans le périmètre de la tâche demandée. Signaler les problèmes repérés ailleurs sans les corriger.
- Ilann est étudiant en informatique et veut comprendre le code : expliquer brièvement les choix non évidents.

## Commandes

- Backend : `cd backend && ./mvnw spring-boot:run` / `./mvnw test` (relancer le serveur après une mise à jour : un ancien processus sur le port 8080 ne connaît pas les nouvelles routes)
- Frontend : `cd frontend && npm install && npm run dev` (Node 18+ ; tests `npm test`, lint `npm run lint`)
- Console H2 : `http://localhost:8080/h2-console`
