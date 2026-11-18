# Référentiel des Codes Erreurs — EarthQuake Faker

Format : `[CONTEXTE]-[NUMERO]`. Les erreurs sont renvoyées en JSON :
`{ timestamp, codeErreur, contexte, message }`.

Le catalogue est dans `backend/.../erreur/CodeErreur.java` : tout code ajouté là doit l'être ici
(un test le vérifie). La colonne « État » indique si le code est déjà levé par le backend.

## 1. Contexte `NIV` (Niveaux & Initialisation)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **NIV-001** | 404 | Niveau introuvable | L'ID demandé dans l'URL n'existe pas en base H2. | défini |
| **NIV-002** | — (démarrage) | Erreur de chargement JSON | Le fichier `niveaux.json` est mal formaté ou contient des coordonnées hors grille (X > 5m ou Y > hauteur du niveau). | défini |
| **NIV-003** | 400 | Identifiant de niveau invalide | L'ID dans l'URL n'est pas un nombre (ex : `/api/niveaux/abc`). | défini |
| **NIV-004** | — (démarrage) | Erreur de chargement des matériaux | Le fichier `materiaux.json` est mal formaté ou contient des valeurs invalides (négatives, id en double). | défini |

## 2. Contexte `STR` (Structure & Géométrie)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **STR-001** | 400 | Structure vide | Le joueur a lancé la simulation sans poser aucun lien. | levé par `POST /api/niveaux/{id}/simuler` |
| **STR-002** | 400 | Nœud invalide | Un lien référence un ID de nœud qui n'appartient pas au niveau courant. | levé par `POST /api/niveaux/{id}/simuler` |
| **STR-003** | 400 | Lien identique / boucle | Le nœud de départ et d'arrivée sont identiques, ou deux liens sont superposés. | levé par `POST /api/niveaux/{id}/simuler` |
| **STR-004** | 400 | Hauteur minimale non atteinte | Aucun nœud relié au sol n'atteint la hauteur requise par le niveau. | levé par `POST /api/niveaux/{id}/simuler` |
| **STR-005** | 400 | Lien trop long | Le lien relie deux nœuds non voisins : sa longueur dépasse le pas de la grille × √2 (≈ 3,54 m pour une grille de 2,5 m). | levé par `POST /api/niveaux/{id}/simuler` |
| **STR-006** | 400 | Lien sur un nœud intermédiaire | Le lien passe exactement sur un autre nœud (ou se superpose en partie à un autre lien) : il faut le remplacer par plusieurs liens. | levé par `POST /api/niveaux/{id}/simuler` |

## 3. Contexte `BUD` (Budget)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **BUD-001** | 400 | Budget dépassé | La somme des coûts des liens posés est supérieure au `budgetMax` du niveau. | levé par `POST /api/niveaux/{id}/simuler` |

## 4. Contexte `MAT` (Matériaux)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **MAT-001** | 400 | Matériau inconnu | L'identifiant du matériau envoyé par React n'existe pas dans le système. | levé par `POST /api/niveaux/{id}/simuler` |
| **MAT-002** | 400 | Matériau verrouillé | Le joueur tente d'utiliser un matériau non autorisé dans ce niveau (ex : Amortisseur au niveau 1). | levé par `POST /api/niveaux/{id}/simuler` |
| **MAT-003** | 400 | Lien trop long pour ce matériau | Le lien dépasse la longueur maximale du matériau (`longueurMax` dans `materiaux.json`) : par exemple une diagonale en béton (2,5 m maximum). | levé par `POST /api/niveaux/{id}/simuler` |

## 5. Contexte `SIM` (Moteur Sismique)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **SIM-001** | 422 | Aucun ancrage au sol | La structure n'est reliée à aucun nœud ayant `estAncrageSol = true`. | levé par `POST /api/niveaux/{id}/simuler` |

## 6. Contexte `SYS` (Technique)
| Code | HTTP | Signification | Cause fréquente | État |
| :--- | :--- | :--- | :--- | :--- |
| **SYS-001** | 500 | Erreur interne | Exception non prévue. Le détail est dans les logs du backend, jamais dans la réponse. | défini |
| **SYS-002** | 4xx | Requête HTTP invalide | Route inexistante (404), méthode non autorisée (405), corps JSON illisible (400)... Le statut est celui de l'erreur d'origine. | défini |
