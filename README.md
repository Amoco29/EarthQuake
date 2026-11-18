# EarthQuake
Jeu navigateur de construction résistant aux séismes (Poly Bridge-like). Projet UBO.

## Lancer le jeu

Prérequis : Java 17+ et Node 18+.

1. Backend : `cd backend && ./mvnw spring-boot:run` (écoute sur `http://localhost:8080`). Après une mise à jour du code, relancer le serveur : un ancien processus répond « No static resource » sur les nouvelles routes.
2. Frontend : `cd frontend && npm install && npm run dev`, puis ouvrir `http://localhost:5173`.

## Jouer

- **Carte :** choisir un niveau. Le niveau suivant s'ouvre quand le précédent est réussi (la progression est gardée en mémoire : elle se perd au rechargement de la page).
- **Construire :** choisir un matériau dans le panneau, puis glisser d'un nœud à un autre pour poser un lien. Cliquer sur un lien le supprime. Les ancrages au sol sont les triangles ; la ligne rouge est la hauteur à atteindre. Budget et hauteur s'affichent en direct.
- **Séisme :** « Lancer le séisme » envoie la structure au backend. L'écran tremble, les liens passent du vert au rouge selon leur stress, et un lien qui dépasse sa résistance casse.

## API

| Route | Rôle |
|---|---|
| `GET /api/niveaux` | liste des niveaux (carte) |
| `GET /api/niveaux/{id}` | niveau, zone et nœuds |
| `GET /api/materiaux` | matériaux (couleur, coût, masse) |
| `POST /api/niveaux/{id}/simuler` | simule le séisme sur `{ "liens": [...] }` |

Les erreurs ont le format `{ timestamp, codeErreur, contexte, message }` (voir `CODES_ERREURS.md`). Console H2 : `http://localhost:8080/h2-console` (URL JDBC `jdbc:h2:mem:earthquakefaker`, utilisateur `sa`, mot de passe vide).

## Tests

`cd backend && ./mvnw test` et `cd frontend && npm test`.
