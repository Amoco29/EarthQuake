# EarthQuake
Jeu navigateur de construction résistant aux séismes (Poly Bridge-like). Projet UBO.

## Tester le backend avec le premier frontend

Prérequis : Java 17+ et Node 18+.

1. Backend : `cd backend && ./mvnw spring-boot:run` (écoute sur `http://localhost:8080`).
2. Frontend : `cd frontend && npm install && npm run dev`, puis ouvrir `http://localhost:5173`.
3. Dans la page, saisir un numéro de niveau et cliquer sur « Charger ».

Résultats attendus :

| Saisie | Résultat |
|---|---|
| `1`, `2`, `3` | Le canvas montre la zone, la ligne rouge de hauteur requise (5, 7 et 9 m), des cercles (nœuds) et des triangles (ancrages au sol). Le JSON reçu s'affiche dessous. |
| `99` | Erreur `[NIV-001]` (HTTP 404) |
| `abc` | Erreur `[NIV-003]` (HTTP 400) |
| backend arrêté | Message « Backend injoignable » |

Autres vérifications :

- Console H2 : `http://localhost:8080/h2-console` (URL JDBC `jdbc:h2:mem:earthquakefaker`, utilisateur `sa`, mot de passe vide).
- Le front appelle directement le port 8080 (sans proxy) : si le CORS est mal configuré, la console du navigateur le signale.
- Tests : `cd backend && ./mvnw test` et `cd frontend && npm test`.

Ce premier front sert uniquement à tester le backend : pas de liens, de glisser-déposer ni de simulation pour l'instant.
