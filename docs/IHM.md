Développeur: Ilann \- Waed \- Claude  
lien gitlab : [gitlab](https://gitlab-depinfo.univ-brest.fr/users/sign_in)  
Compte Rendu:  
Sujet:   
Nom : EarthQuake Faker ( feinteur de séisme)

C'est un jeu sur le navigateur qui a pour objectif de construire des bâtiments résistants à un séisme. Le jeu sera composé de plusieurs niveaux, chaque niveau proposera des difficultés croissantes ou des nouveautés apportées au gameplay. Le décor du jeu pourra changer selon le niveau dans lequel le joueur se trouve. 

le joueur devra choisir des objets / materiaux sur un panel de selection qui se situera sur la gauche de l’ecran, puis il placera l’objet selectionner en drag and drop sur des positions sécurisé, pas de choix libre sur la position pour facilité la jouabilité. 

La charte graphique: 

le jeu sera dans un thème coloré, vert/marron/bleu pour l’environnement et des couleurs plus varié pour les materiaux. l’ensemble des image seraient  les plus simple possible et fluide pour une meilleure jouabilité

la police d’ecriture : celle de pokemon

Grande carte avec un chemin en terre qui va de gauche vers la droite pour suivre la progression du joueur qui passe les niveaux, chaque niveau passe fait progresser le joueur sur le chemin jusqu’au prochain niveau. le joueur sera représenté par un bricoleur (felix fixe junior) 

- premier jet 

**Équipe de développement :** Ilann, Waed, Claude   
**Nom du projet :** EarthQuake Faker (Feinteur de Séisme)

### **1\. Concept et Objectif du Jeu**

**EarthQuake Faker** est un jeu de réflexion et de construction sur navigateur inspiré des mécaniques de *Poly Bridge*. Le joueur incarne **Félix Fixe Junior**, un bricoleur dont la mission est d'ériger des structures capables de résister à des tremblements de terre.

Le jeu est découpé en plusieurs niveaux à la difficulté croissante. Chaque niveau impose deux contraintes obligatoires pour être validé :

1. **Un budget limité** à ne pas dépasser pour acheter les matériaux.  
2. **Une hauteur minimale** que le bâtiment doit impérativement atteindre.  
   

L'objectif est d'offrir un équilibre entre réalisme physique et accessibilité : les réactions des matériaux doivent paraître naturelles et logiques, tout en laissant au joueur une marge de manœuvre suffisante pour toujours trouver une solution gagnante.

### **2\. Déroulement d'une Partie (Gameplay)**

**Phase 1 : La construction**

* À l'écran, une zone de construction présente des **nœuds (points d'ancrage)** déjà visibles et positionnés à des endroits fixes pour garantir une prise en main simple et rapide.  
* Sur la gauche de l'écran se trouve un panneau de sélection regroupant les différents matériaux disponibles (bois, acier, amortisseurs, etc.), chacun ayant son propre coût, son poids et sa résistance.  
* Le joueur sélectionne un matériau et le place par glisser-déposer (*drag and drop*) entre les nœuds disponibles pour former l'ossature de son bâtiment.

**Phase 2 : Le test du séisme**

* Une fois la hauteur minimale atteinte et le budget respecté, le joueur lance le séisme.  
* Le jeu analyse la solidité de l'ensemble de la structure en tenant compte de la géométrie du bâtiment et des propriétés réelles des matériaux utilisés (poids en hauteur, souplesse, résistance).  
* **Déroulement visuel :** L'écran se met à trembler pour simuler la secousse. Les éléments de la structure changent progressivement de couleur (du vert au rouge) selon le niveau de tension qu'ils subissent, créant ainsi du suspense. Si un élément dépasse sa limite de résistance, il se brise visuellement et fait échouer le niveau.


**Phase 3 : Progression**

* Si le bâtiment tient bon jusqu'à la fin de la secousse, le niveau est validé.  
* Le joueur retourne sur la **grande carte du monde** : son personnage (Félix Fixe Junior) avance le long d'un chemin en terre, de la gauche vers la droite, pour débloquer l'étape suivante avec un nouveau décor et de nouveaux défis.

### **3\. Direction Artistique et Ambiance**

* **Style visuel :** Des graphismes simples, épurés et colorés pour assurer une lisibilité maximale et une grande fluidité pendant les phases de construction et de tremblement.  
* **Palette de couleurs :** Des tons naturels (vert, marron, bleu) pour les décors et l'environnement, contrastés par des couleurs vives et distinctes pour identifier facilement chaque matériau.  
* **Typographie :** Une police d'écriture inspirée de l'univers *Pokémon* pour renforcer l'aspect ludique et rétro.  
* **Évolution des décors :** L'environnement visuel change au fil de l'avancée sur la carte (prairie, montagne, ville, etc.).

### **4\. Vision et Évolution du Projet**

* **Étape 1 (Version jouable rapide) :** Se concentrer sur l'essentiel avec un système de construction fonctionnel, quelques matériaux de base, le calcul de résistance au séisme et un premier enchaînement de niveaux jouables.  
* **Étape 2 (Enrichissement) :** Ajouter progressivement de nouveaux matériaux aux comportements variés, des niveaux plus complexes, de nouveaux décors, des effets sonores (craquements, grondement du séisme, musique) et renforcer la mise en scène du suspense lors des tests.

 

![][image1]  
![][image2]

Voici le document technique complet et prêt à être remis à **Claude** (ainsi qu'à l'équipe) pour démarrer le développement du backend **EarthQuake Faker**. Il intègre toutes tes contraintes : dimensions de la grille, chargement via niveaux.json, base H2, contrat d'API et convention stricte de gestion des erreurs.

# **Spécifications Techniques de Démarrage — EarthQuake Faker (Backend)**

**Équipe :** Ilann, Waed, Claude

**Stack imposée :** Java 17+ / Spring Boot 3.x / Base de données **H2** (en mémoire pour le MVP)

### **1\. Système de coordonnées et initialisation des niveaux**

* **Dimensions de la zone de construction (en mètres logiques) :**  
* 

  * **Largeur fixe :** 5 mètres (axe $X$ allant de 0 à 5).  
  *   
  * **Hauteur variable :** entre 10 mètres et 15 mètres selon le niveau (axe $Y$ allant de 0 au sol jusqu'à 10 ou 15 au sommet).  
  *   
  * *Note : Le front-end React se chargera de convertir ces coordonnées métriques en pixels.*  
  *   
* **Source de données au démarrage :**  
* 

  * Au lancement de l'application Spring Boot, les niveaux et leurs nœuds prédéfinis sont chargés depuis un fichier src/main/resources/niveaux.json et persistés dans la base **H2**.  
  *   
  * Activer la console H2 (/h2-console) et configurer **CORS** pour autoriser les requêtes provenant du front-end React (localhost:5173 / localhost:3000).  
  * 

### **2\. Contrat d'API (Échanges React $\leftrightarrow$ Spring Boot)**

#### **Récupérer un niveau (**GET /api/niveaux/{id}**)**

Renvoie les paramètres du niveau (budget, hauteur requise, décor, dimensions de la grille, longueur maximale d'un lien `longueurMaxLien`, matériaux autorisés) ainsi que la liste des nœuds fixes disponibles (id, posX, posY, estAncrageSol).

#### **Lister les niveaux (**GET /api/niveaux**)**

Renvoie, pour la carte du monde, un résumé de chaque niveau : id, numero, nom, themeDecor, budgetMax, hauteurMinRequise.

#### **Lister les matériaux (**GET /api/materiaux**)**

Renvoie id, nom, couleurHex, coutUnitaire, masseLineique et longueurMax (longueur maximale d'un lien dans ce matériau), du moins cher au plus cher.

#### **Lancer la simulation (**POST /api/niveaux/{id}/simuler**)**

* **Requête envoyée par React :**  
* 

JSON

```

{
  "liens": [
    { "noeudDepartId": 1, "noeudArriveeId": 4, "materiauId": "BETON" },
    { "noeudDepartId": 4, "noeudArriveeId": 5, "materiauId": "BOIS" }
  ]
}

```

* **Réponse attendue en cas de succès (**200 OK**) :**  
* 

JSON

```

{
  "estValide": false,
  "scoreSoliditeGlobal": 0.0,
  "messageBilan": "Échec : rupture détectée au 1er étage.",
  "detailsLiens": [
    { "noeudDepartId": 1, "noeudArriveeId": 4, "tauxStress": 0.45, "estCasse": false, "tempsRupture": null },
    { "noeudDepartId": 4, "noeudArriveeId": 5, "tauxStress": 1.18, "estCasse": true, "tempsRupture": 8.4 }
  ]
}

```

`tempsRupture` est l'instant de la rupture en secondes (sur 10 s de séisme), `null` si le lien tient ; il sert à l'animation. Une structure qui casse est un résultat normal (200, `estValide: false`) ; `scoreSoliditeGlobal` (0 à 100) vaut toujours 0 quand le niveau est perdu ; une structure qui viole une règle de construction est une erreur (voir les codes ci-dessous).

### **3\. Convention de Gestion des Erreurs**

Toutes les exceptions métiers et techniques doivent être interceptées globalement (via @RestControllerAdvice) et renvoyer un format JSON standardisé contenant un **code erreur structuré**.

#### **Format du Code Erreur :** \[CONTEXTE\]-\[CAUSE\]

1. **Le Contexte (Où l'erreur a eu lieu) :**  
2. 

   * NIV : Chargement ou validation d'un niveau (niveaux.json, niveau introuvable).  
   *   
   * STR : Construction de la structure (nœuds invalides, lien en doublon, hauteur insuffisante).  
   *   
   * BUD : Gestion financière (dépassement du budget autorisé).  
   *   
   * MAT : Matériaux (matériau inconnu ou non débloqué pour ce niveau).  
   *   
   * SIM : Moteur de calcul sismique (erreur mathématique, structure vide).  
   *   
3. **La Cause (Pourquoi l'erreur a eu lieu) :**  
4. 

   * Numéro à 3 chiffres ou mot-clé explicite (ex: 001 ou DEPASSEMENT).  
   * 

#### **Exemple de réponse JSON en cas d'erreur (**400 Bad Request**) :**

JSON

```

{
  "timestamp": "2026-11-16T08:35:00Z",
  "codeErreur": "BUD-001",
  "contexte": "Validation du budget",
  "message": "Le coût total de la structure (320) dépasse le budget maximum du niveau (250)."
}

```

#### **Livrable obligatoire :** CODES\_ERREURS.md

Claude devra créer et maintenir à la racine du projet (ou dans /docs) un fichier CODES\_ERREURS.md répertoriant chaque code erreur créé, sa signification exacte et comment le résoudre.

### **4\. Modèle de départ pour** CODES\_ERREURS.md **(à copier dans le dépôt)**

Markdown

```

# Référentiel des Codes Erreurs — EarthQuake Faker

Format : `[CONTEXTE]-[NUMERO]`

## 1. Contexte `NIV` (Niveaux & Initialisation)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **NIV-001** | Niveau introuvable | L'ID demandé dans l'URL n'existe pas en base H2. |
| **NIV-002** | Erreur de chargement JSON | Le fichier `niveaux.json` est mal formaté ou contient des coordonnées hors grille (X > 5m ou Y > 15m). |
| **NIV-003** | Identifiant de niveau invalide | L'ID dans l'URL n'est pas un nombre entier. |
| **NIV-004** | Erreur de chargement des matériaux | Le fichier `materiaux.json` est mal formaté ou contient des valeurs invalides. |

## 2. Contexte `STR` (Structure & Géométrie)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **STR-001** | Structure vide | Le joueur a lancé la simulation sans poser aucun lien. |
| **STR-002** | Nœud invalide | Un lien référence un ID de nœud qui n'appartient pas au niveau courant. |
| **STR-003** | Lien identique / boucle | Le nœud de départ et d'arrivée sont identiques, ou deux liens sont superposés. |
| **STR-004** | Hauteur minimale non atteinte | Aucun nœud relié au sol n'atteint la hauteur requise par le niveau. |
| **STR-005** | Lien trop long | Le lien relie deux nœuds non voisins (longueur > pas de la grille × √2, soit 3,54 m). |
| **STR-006** | Lien sur un nœud intermédiaire | Le lien passe exactement sur un autre nœud, ou se superpose en partie à un autre lien. |

## 3. Contexte `BUD` (Budget)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **BUD-001** | Budget dépassé | La somme des coûts des liens posés est supérieure au `budgetMax` du niveau. |

## 4. Contexte `MAT` (Matériaux)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **MAT-001** | Matériau inconnu | L'identifiant du matériau envoyé par React n'existe pas dans le système. |
| **MAT-002** | Matériau verrouillé | Le joueur tente d'utiliser un matériau non autorisé dans ce niveau (ex: Amortisseur au niveau 1). |
| **MAT-003** | Lien trop long pour ce matériau | Le lien dépasse la longueur maximale du matériau (ex : diagonale en béton, 2,5 m maximum). |

## 5. Contexte `SIM` (Moteur Sismique)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **SIM-001** | Aucun ancrage au sol | La structure n'est reliée à aucun nœud ayant `estAncrageSol = true`. |

## 6. Contexte `SYS` (Technique)
| Code | Signification | Cause fréquente |
| :--- | :--- | :--- |
| **SYS-001** | Erreur interne | Exception inattendue côté serveur (le détail n'est que dans les logs). |
| **SYS-002** | Requête HTTP invalide | Route inconnue, corps JSON illisible, méthode non supportée. |

La liste à jour, avec le statut HTTP de chaque code, est dans `CODES_ERREURS.md` à la racine.

```

ANNEXE :  
**Note (état final) :** l'implémentation n'utilise pas de hiérarchie de classes pour les matériaux : une seule classe `Materiau` pilotée par `materiaux.json` (id texte), `Lien` est un record d'entrée immuable, et le résultat d'une simulation est porté par `ResultatSimulation` et `ResultatLien`. Les sous-classes `Bois`, `Beton`, `Acier` et `Amortisseur` ci-dessous sont la conception initiale.  
@startuml  
skinparam classAttributeIconSize 0

 class Materiau {  
    \# id : String  
    \# nom : String  
    \# couleurHex : String  
    \# coutUnitaire : double  
    \# masseLineique : double  
    \# resistanceTractionMax : double  
    \# resistanceCompressionMax : double  
    \# moduleElasticite : double  
    \--  
    \+ calculerPoids(longueur : double) : double  
    \+ calculerCout(longueur : double) : double  
    \+ {abstract} calculerContrainteSubie(forceExercee : double, longueur : double) : double  
    \+ estEnRupture(contrainteSubie : double) : boolean  
}

class Bois extends Materiau {  
    \- tauxHumidite : double  
    \--  
    \+ calculerContrainteSubie(forceExercee : double, longueur : double) : double  
}

class Beton extends Materiau {  
    \- estArme : boolean  
    \--  
    \+ calculerContrainteSubie(forceExercee : double, longueur : double) : double  
}

class Amortisseur extends Materiau {  
    \- coefficientAmortissement : double  
    \- courseMax : double  
    \--  
    \+ absorberEnergie(forceSecousse : double) : double  
    \+ calculerContrainteSubie(forceExercee : double, longueur : double) : double  
}

class Acier extends Materiau {  
    \- limiteElastique : double  
    \--  
    \+ calculerContrainteSubie(forceExercee : double, longueur : double) : double  
}

@enduml

Code uml global du logiciel

@startuml  
skinparam classAttributeIconSize 0  
skinparam linetype ortho

package "Modèle Métier & Niveaux" {  
    class Niveau {  
        \- id : Long  
        \- numero : int  
        \- nom : String  
        \- themeDecor : String  
        \- budgetMax : double  
        \- hauteurMinRequise : double  
        \- magnitudeSeisme : double  
        \- largeurZone : double  
        \- hauteurZone : double  
    }

    class Structure {  
        \- id : Long  
        \--  
        \+ ajouterLien(lien : Lien) : void  
        \+ supprimerLien(lien : Lien) : void  
        \+ calculerCoutTotal() : double  
        \+ calculerHauteurMax() : double  
    }

    class Noeud {  
        \- id : Long  
        \- posX : double  
        \- posY : double  
        \- estAncrageSol : boolean  
        \--  
        \+ calculerDistance(autre : Noeud) : double  
    }

    class Lien <<record>> {  
        \- noeudDepartId : Long  
        \- noeudArriveeId : Long  
        \- materiauId : String  
    }  
}

package "Hiérarchie des Matériaux" {  
 class Materiau {  
        \# id : String  
        \# nom : String  
        \# couleurHex : String  
        \# coutUnitaire : double  
        \# masseLineique : double  
        \# resistanceTractionMax : double  
        \# resistanceCompressionMax : double  
        \# moduleElasticite : double  
        \# longueurMax : double  
    }

    note right of Materiau
      Implémentation : une seule classe pilotée par
      materiaux.json (ids BOIS, BETON, ACIER, AMORTISSEUR).
      Seul l'amortisseur a un comportement dédié, dans CalculateurStress.
    end note

    class Bois extends Materiau {  
        \- tauxHumidite : double  
    }

    class Beton extends Materiau {  
        \- estArme : boolean  
    }

    class Amortisseur extends Materiau {  
        \- coefficientAmortissement : double  
        \- courseMax : double  
        \--  
        \+ absorberEnergie(...)  
    }

    class Acier extends Materiau {  
        \- limiteElastique : double  
    }  
}

package "Simulation & Moteur Physique" {  
    class MoteurSismique {  
        \--  
        \+ simulerSeisme(niveau : Niveau, catalogue, structure : Structure) : ResultatSimulation  
        ' s'appuie sur ValidateurStructure, CalculateurCharges et CalculateurStress  
    }

    class ResultatSimulation <<record>> {  
        \- estValide : boolean  
        \- scoreSoliditeGlobal : double  
        \- messageBilan : String  
        \- detailsLiens : List<ResultatLien>  
    }

    class ResultatLien <<record>> {  
        \- noeudDepartId : Long  
        \- noeudArriveeId : Long  
        \- tauxStress : double  
        \- estCasse : boolean  
        \- tempsRupture : Double  
    }  
}

' Relations  
Niveau "1" \*-- "1..\*" Noeud : propose des points fixes \>  
Niveau "1" \-- "1" Structure : évalue \>  
Structure "1" o-- "0..\*" Lien : est composée de \>  
Lien "0..\*" \--\> "1" Noeud : noeudDepart  
Lien "0..\*" \--\> "1" Noeud : noeudArrivee  
Lien "0..\*" \--\> "1" Materiau : est fait de \>

MoteurSismique ..\> Structure : analyse  
MoteurSismique ..\> Niveau : utilise paramètres  
MoteurSismique ..\> ResultatSimulation : produit
ResultatSimulation "1" *-- "0..*" ResultatLien : detailsLiens

@enduml  


[image1]: <data:image/png;base64,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>

[image2]: <data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAloAAAGACAIAAAD6dFtpAABCSElEQVR4Xu2dz4s7W3ree5doqZ2SbGSSXXq88Qxtcx06jsfT4xEDY2GwyUJgM+KmnR80BHK1yEAG3Ek64S4mWKadcINvGBlGw8WDnO2ltZoEQ1BEdiKQlZZa1F/Qeaaeq/f79ilJrZJUqlOl50MjSqdOlapO65yPTtV7Tl29CiGEEBfPVZgghBBCXB6XosPn5+cwSQghhFjzQYeLxeIq5fr62mV4Q7CKb7GJT/SsViusfXl54du7u7tms/m6c5ONYENsAqXtOLZ39zkajcIkIYQQIuWDQlqt1tPT06uTHGk0GuYwrkIK3WN5Hh8ffTauBZPJpNPprD/hF1a7ublBot8c+/RvvS9tP9iQb5kZx2kZkB+J8/nc7wQLDw8PQYrtTQghhAgIDUHlcMFSAh1iod/vW56sb/wC1MXNX53VAPpqs9ns/v6eq/C2nbKx+4gNvaphbvQ7odWXFGQej8d8tW2pQ9vta9r99fsUQgghjDfWQR/Lv/Usl0tbhorcmq/Ytq3fMCBJkuAtU3Zs4jFl+o/Gtkw3+dluXzMXe4UQQggS9g7rjb9yK4QQQhhf6ZBXHS8Bu8QqdvPmayKEEHXnsnqHYn/wuyFMEkKI+iIdis2ogyiEuCikQ7EZ6VAIcVFIh2Iz0qEQ4qLYoMOnpycOThCXhv8aSIdCiIuihjr8+kff+HT8o8++/Pz29jZcJ3bivwbSoRDioqibDn/zu9/8Yj7B37d+99t4xdswh9iO/xpIh0KIi6JuOqQLr66uuIA/NOu2Ful4vbu7+7DBdlar1Xw+D1Nrjf8aSIdCiIuinjr82+2/Yzq8vr62tTQcH6yBtw8PD0zEa6/Xe3x8ZDZucpU+Q8O2BTc3N1xAeqfTCTa0t26LiuG/BtKhEOKiqKcOfe8w6AtChHxcVLJW13K55Cra0S8Mh0MugPv7e0uH/6BDboidMN32U13810A6FEJcFHXT4Y9//hNa8OkvPsXrT2c/C3Ocmkp3BwP810A6FEJcFHXT4Wq1+uHzH9OI/+nP/4QXMMWe+K+BdCiEuCjqpkNxDP5rIB0KIS4K6VB8wH8NpEMhxEUhHYoP+K+BdCiEuCikQ/EB/zWQDoUQF4V0KD7gvwbSoRDiopAOxQf810A6FEJcFO/oMJiWZU/a7TZep9NpuGILzE/u7+/dmkN4d3K14z9iG71ebzabvbhp4XLBIYy755DzB8+cvvQ20u/3bXn3ufuvgXQohLgo3tEhpx/jHC5Gq9Xya4nfCi1po9HwiWiRqSh7ykSz2bRZzagBZgjaa6zihGrEZlzjAvZvo+AHgwEX/ORqzIbj7Ha7NgbRPgKJPB7/EQaOudPpcHmxWPgTtAKxT7dzwSamQ3zQeDzmzvFZfkO+hTv51s/3NhqNbBOWCc+Cn2UHj11Rh1baOGBfvFZW23Ro/0fL4L8G0qEQ4qLYS4fJ2xnIrM21ScsAGnFbJmivLZENLvoxti0aa9un9YeQwbfdSeoAL+Pgo7MfSmxyNWbD683NjX2cfQQSucPA96STwoXk7Wdldejfet1CS5YZ8rNlTn8azOvGzeE2fzzYGw+Sa3nw3JXZOkl35XWI4rVlpNsOffFaBtuP/xpIh0KIi+IdHRr7TMjpL1Fav8cSN17AxG7RMaK0shksxV93DbJl553J7sdAJ8+Wg2z8CC/4LNzEDmbHBwVwEyvDfTbkJpYz2CT4d1hpB/itgk02HoP/GkiHQoiLYl8dikvAfw2kQyHERVGgDoMLiTvIBoPYbS1xTvzXQDoUQlwU7+iQt5egK4vLoKhWq5U5jFfqLDTG4lN424+J2Ip7YMBLECqSFee2q3+iUPzXQDoUQlwUe+mw0WjYTTWLv7CID8aYQHh2d8qHfjARKT74xYeKQKvZoQWTySRIEWfAfw2kQyHERbGXDt9lOp1uHKuwJ9k4Hf/cXXE2/NdAOhRCXBTv6FBcFP5rIB0KIS6KYnX47jVPDoPbPVXKNjgmMui/bhuJKPbBfw2kQyHERfGODjlJSpIGv1A/nU7H3/lrNBompG63OxwOGSzTarXG4zFeOWFKL4XyQwqvrGJXNzc32D/SkYL8q9UKr0n6udwz3tqGlsiPuLu7y15lTTaNRBR74r8G0qEQ4qJ4R4cWLOM7Yf7GHu1F4DYzJQNnhilMsUnU/N44HwptR7ctU7iW2bhhkGhbZdnoSLEP/msgHQohLop3dJis7QK9bZzHxNg2f8r+mA75mu3kbUzk5J/G/vOGiyz+ayAdCiEuivd1KC4H/zWQDoUQF8UGHYJHcXmgY+2/A9KhEOKi2KxDERWtVitMKh7pUAhxUUiHFUA6FEKIopEOxWakQyHERSEdis1Ih0KIi0I6rAC6WCqEEEUjHVYA6VAIIYpGOhSbkQ6FEBeFdCg2s02H7Xb7pZqMRqP5fB6ejxBCpEiHFeDp6SlMKp6XLTrclu7Z3zrBVDhF02w2wyQhhEiRDitAVPcOmX519YtvTqPRwGu3272/v39NJzPq9/tYmEwmt7e3WOj1eni9vr5erVbcnJvw1bJhb90Un3k8HnOTh4cHLpC7uzvkRx78SrCt+Mq9PT8/YyeWOBgMcBjL5RIf5/cjhBAe6bACRNg7TNKnnbTbbSxPp1NaECmdTocLVBE8xGXqE/CxJ9zQsnHtVYrPTJhuidDha6pSFAsSKUtuhVcYcTab8fli3ASfaMsAXvxqv0II4bgUHVojjl4FmksmLhYLLvBZGb4TI3brkNBq6OpRNiYtvKKn+JrqEPaCsewqJeRkG1o2LEBjeA0yE6QjJdAh3lKHXGY69sbEx/Q5nViAFylg9hctsxBCBFxQ0zAcDq2blbbbH84dyzcpliL20WEVeX5+DpOEEOJydMjuCHoMr2k8BfqFvKT2mt6Zow6n0ylvPsVGhPcOKw3vdAohhOdSdFhpotIhOtnTioNTeKkgjD8SQhSEdCg287JFh9vSRdHorqcQhaIKJjazTXvb0kXRSIdCFIoqWAWI6mLptnRRNNKhEIWiClYBYtZhIgrDBgIR6VCIQlEFE5upqA4HgwF+PeA1XFFNfFFLh0IUiipYyXCSlHEKp2g5jJPPQFZFHf745z/5Yj7h329/7zvh6grii1o6FKJQVMFKA17heMfXdGw4wDKD6Tn8ka1hu932s7TY7CroAHH56ekJefDWjMh0phwcnV85HaLQYMGvf/QNlAYKFstIsbVI4ev9/f2HbaLHF7V0KEShqIKVBlyV1eFrOrDPN3y8+me3kTiTABpKZDY19vt9PxUn0y0FO7RV+1M5HX725edQIEXIP6TY2ru7u+l0irUoK6rx8fERvxV4WRX/C7yi3PDa6/Vsq9LxRS0dClEoqmDRwUbQnpEE/3FiTx9YsXFu1WwiU4JWdU9269Cie8L2uzw+Hf8o0CFSbC10yPnE+dMBIBE9RZQtFsbjMcpZOhTiklEFqzOtFC4/PT1h2WZt9auyy8hm2gtW+XQQtt/lwYul3//k46sULP/ab31ka6HDJL1YCh1yIUnNhw4iFprNJnSIHxx4Kx0KcZmoglWACB/wZF3VsP0ula/9yi9b1/Dh4SFcXUF8yUuHQhSKKlgFsP7ZOdmtQyNsv8VJ8UUtHQpRKKpgH5hOp1yI7RlA0uHF4otaOhSiUFTBPsDHHZTingiRDmPAF7V0KEShqIK9AS7kSAYhHcaAL2rpUIhCqVgFe3x8fCqSwWAQJp2a8JT2oJQO68E67Ha7YZI4FF/U0qEQhVKxCha2FhXkACNWS4ccugBWq5VPx08Zptzc3Nzd3XG52Wz6PCLAF7V0KEShVKyCha2FYzKZfPyDP/rsy8/91FwRcoAOS+FgHbbb7TApZTQaceHh4YFj4bEc1cjFCPFFLR0KUSgVq2Bha7EGHQ6ONvvW736bC5x2C8zn8+l0+jb7G56fn8OktGczHo+TLWuT7envUnsd7g/+L+od7sYXtXQoRKFUrIKFrcUaKpBzkfDvn/zgn3KVn2SEs3B1u10k2jBt9FTQR0EinIouC3SFZbPdcDgcDAbocT4+PppikYitIAbOB43+0GKxSNKJUWazWZLqmTmzHKDDal0sFSfEF7V0KEShVKyCha3FmqwOx//rL7kK5rMJuqBAGBF2QeePakxSHTIDZMa5uwgvumKtXX21TbBPpEOBTKEUKUKzb6fT4UKAdCj2xxe1dChEoVSsgoWtxZqsDoPeIezFBfTw4EL05+xBP1iFZd7Kog7hOTT61NsOHSIzPWp9QWxoQj2hDg/Y5HgO0yGKxSbCtmJppnDZytDuGgYRNyxSQXxRS4dCFErFKljYWqyxySrNiHZhMzZKcdsBHKZD/Ajgrwd/ORoKtF8Jlrgt4olTbAvii1o6FKJQKlbBwtbCAf99/5OPP/vy869/9I1wXUwcoMMDNjmew3QoTosvaulQiEKpWAULW4sKcoDbdO/wYvFFLR0KUSgVq2CP1Uc6JNPpVLcJ38UXtXQoRKFUrIKFrUUFOUCHpXCYDvv9PqNmeI+Q8TI2asWiZjhzTaBDzVaTxRe1dChEoVSsgoWtxVvm83mSdjt84sHj5Qui3jrsdDqcsxTZknW8DPrEXMvwXZuqzQJ6CWer4bY209uF44taOhSiUCpWwXxLgZ4EzMcx9RxHz+nB4Buk2DiK4XCYrKU4Ho9tgmm218zPPBwDgGUbO2j5OWZgMBjsGF+/JwfosJYXS3eg2WoMX9TSoRCFUrEKxjYCxkKLCTnBiBxTb5fd2EHkcHumcICgLVugv2cymXCUITo3vnNp+bE3qHHb2IBcSIdif3xRS4dCFErFKphvKeAtOMzG1Ptx9FCOCRJKQw8PKcnb2aXNbaPRyNLR1sN8dn2V6fgI7BzZytJhKRymwz2LCJ0/P3neu+BDOU/epY1K9EUtHQpRKBWrYGFrUUEuQYeQFi9ZM9EmoOGPEg+z+YlpgmdccNXL+m4if+VY17/20Te+qKVDIQqlYhUsbC12Yk+l8PjImmyUzct6bra88O6jx+JHgk85QIcVulhKHXY6HXuckyUm7hlPyTriiSk+oGZH/xI+4NR3dsW79s+K8kUtHQpRKBWrYL6lQBdhsVigfWSfgz0GLjPKxj+VwiSHZYiKq7DMSBnmD8JkbtePp+A+oYEgg/VpkBNdHB+Dg2UkMj+v71mHpt469ASTkRLe3PUwGx8Jkl1rvPszRToUQhxDxSoY2wjIBm2oxY4m61uG7DpYlE2y7mogxUQFOVnDSlFZfu6EDgseT4FVfH5FtrvJrZDTx+DgLT8aaxnLY9E9B+jwgE2O53gdiuPxRS0dClEoFatg1kzAPYwp5QU0uMqPVENis9lECl3FKAyu4iMX7BYXt2J+LnATLnMr5OdzEJHZX+5jhwavDAzBPm1vfIt0bMireVjmBdVS3HYAxenQ3/87eHzhjmuqdcIXtXQoRKFUrIKFrUUFOUCHB2xyPAfokFG+9oAnHDbv6rE3z4uivfXjJ3mBmj8pkvQ3BDKYHa2rzZ1wW7scap1vG2nK9Prhi1o6FKJQKlbBwtbiINDUsnl9TglXvyUbj/PuJrs5wG0VuncIn1FUMJaVG7rUq3SwCt/ymjZ0SJnRiNRh9v4fd8JtrUdoOqQ+fZe9Zviilg6FKJSKVTDfUsAraCt9VyNZ3/ljlA2W2Yxiwc87k6Tj7pO019Lv91/WMTLDFGZ4Wc9ZQ/nhs7rdLmelQR6bnsaicvan3jok2YgY/v6wfl6Av+26DdunLfjgmuwn1gNf1NKhEIVSsQrGNsKG2LOLgAaaD4tI1t0F9D/QPkKWaEHY1KI/wXuB/sJaPwViQx72MHaP8rY7jlzgpwR53uUAHZbCMToUp8IXtXQoRKFUrIIFjYVdPWMsDKxG/zHKhtE0SepITlvDeBbksSEQ/XTAuMXImA4toMbicewyoNdhzz3zfU+kQw+vkdrvG2IBvcewcSd5/1ml44taOhSiUCpWwcLWYj/83GynBe24zQm+JwfosEIXS+33BK9kWuHwGineWoivpaOVRzHyhmKS7gEmYzaG+3InFmWDtcjsr5Sa+fgpFrPDzaFA+1DO58flJzdvTrT4opYOhSiUilWwsLWoIPXWIUTFzjR72zZxgcXRZPtn7B2aDtnnTtIQG+ujJ+43DXXIZT/21OBn8fI4F+xD0a2328OVCMDxRS0dClEoFatgYWtRQQ7QYSkcpsPiyNsLrwe+qKVDIQqlYhUsbC0qiHQo9scXtXQoRKFUrII9VZ+gjduHCl0sfZf7+/ts+K6/h4eT9fd683YKN+a3i6XZj44cX9TSoRCFogpWAaqiw14KlfP4+Hh7ezsYDMbjcS8dNY/lJBWSjQeluphhOBxyMKjdbsQeGo1Gp9OxmRB4O5D7xyruMHFhwPi1wfAZbIuPsPlxkMhoGmzL3TJ//Piilg6FKBRVMLGZA3QITFGz2ewqJXGTyCSpkCwQxrxoYS9GMIWNvbX9bAuEQc/S9mnz4/hP95njxxe1dChEoaiCic0cpkPDBJZsmVM0O8sMJ0yw6dz8FDZ+b0Z2DgQbfZHduS1smxknTnxRS4dCFIoqWAV4KiP65kgdipPgi1o6FKJQvqpgvK4l4qTVaoVJxXNzc/P2q/IVgQ5bokh8UV9Jh0IUiSpYBSild7gN9Q7PiS9q6VCIQlEFE/k4WIeM6gxT149wepeN2xbHZDLJ3ps8P76opUMhCkUVTORjmw45DTfdxonQOI7i+fmZwwptwlLm5EOy7JFb2ITZ+Jitp/SJWtwWLJdLbss9t1ot7pyb83leHKfB5eCxwNwKq7hbDkNEIvdvTwdL1vOdGvwgZjNtdzod7McGR/JTcC7IwKOyGclf1o8JQx47BqbYgm3FOFsUi8XBJtKhEGdEFawCBPeQymWjDu2OY5IahTOX0jp+bAPSIR60+JABG30bL+EfeWibcAH7wd78pKb0DT8CH8eZS6ETWw7mMg0Gcth4ROyfxxM8UiNJTXaVzi2erA+DPrbjtI+wgf/IxlP2n2XA1gx8tWldiW2FAkG6d2EiHQpxRlTBKkD8Okxcl4htPbt6yds5u6k0GAVuYLvfaDRWKVCUPUiZ+kFXiduiaxXokI/r4kfwkiZcBbPaso3lT9KpwJHTpIUNabLeempvHI+N0LARHTwkfhCzoTvIHh4yv6wfGe3nGbf+on26HyVpa7GtnzqH6eyVSodClIgqmMjHNh2KIvBFLR0KUSiqYCIf0uHZQNfTF7V0KEShqIJVgJgvlhZHp9MJk0ql9P+CdChEoaiCVYDSG2LP2XSYHYdeLqUfjHQoRKGogol8SIdlIR0KUSiqYCIfherw/v7eGv3YLpaWbqPSD0CIeqMKVgFinqTt5ETb6Kt3KES9UQWrAKU3xJ5L1uFVSrjCsW3e85Ow+6OFEEeiClYBLqp3GC3QYafTabfbfAs5zWYzLo/HYxQL1poOV6sVR/fz7UmQDoUoFFUwkY9L1qF/e3d3x4Xb21u8zufzZrPpddjtdtd5T4N0KEShqIKJfEiHZSEdClEoqmAVoPSG2CMdloV0KEShqIJVgNIbYo90WBbSoRCFogom8iEdloV0KEShqIKJfFygDpvN5qt0KETdUQWrAKU3xJ4L1CEp/b8gHQpRKKpgFaD0hthzgTrkA4pL/y9Ih0IUiiqYyMcF6pBIh0LUG1UwkQ/psCykQyEKRRWsAmiSthiQDoWoN6pgFaD0htgTjw4f6850OvXnKx0KUSiqYBVAvcMsKJPkAvCnLB0KUSiqYCIf9dPhX/7VX37/k48/+/Lzm1//1XBd2fhTlg6FKBRVMJGPmunwD/7ZH34xn+DvW7/7bbz+dPazMEep+FOWDoUoFFWwCqB7h1lOpUO6EKbhAv4+GXxia5+fn/GK8u92u4PB4ObmZjwecxUWcAztdnuxWHAZiXxFZtvDkfhTlg6FKBRVsAogHWYpTodBB/H29vbu7o4LeH18fGT6dDrF6/39PRNNgdjVw8MDl4/Hn7J0KEShqIKJfNReh//q3wxsLU52NpvBecvlkjpEd3A4HHIBa9Ff5DL2wE16vZ5tfjz+lKVDIQpFFawc+v0+2lAssHnFW3uQ+mva8KFHiE7JYrHg/NHxUDMdfu1XfpkWfPqLT7kAwkzl4U9ZOhSiUFTBysE3bVcpg8GAbzudjk9/1cXSTZxKhwD++/gHf/TZl5+z/xcV/pSlQyEKRRUsOtgIol/ItzFMHu2pnw5jxp+ydChEoaiCiXzEo8NwEpfaMRqN/ClLh0IUiiqYyEc8Ogx7UnXEn7J0KEShqIJVAE3SlsXr8HE9+IExn8Z8Pvdvq4g/ZelQiEJRBasAuneYxeuw0+kk6RB4G+SwWCwSN+YBvmw2m7e3t6PRCG8bjQaHEsaPP2XpUIhCUQWrAOodZjEdQmzQIQfFB2P+/Nt+v2+BoxXqNfpTlg6FKBRVMJGP2HSYrHuHsEW2d4hE+BKv6CCaDpF+woljCsWfsnQoRKGogol8RKjDvECQJ5xWtFD8KUuHQhSKKlgF0L3DLMfosEL4U5YOhSgUVbAKIB1mkQ6FEKdFFUzko1wdTqdTLkiHQojTogom8lGuDl/TvvLt7a2NNczChy55+v1+kGJkMyfps5wYnrObHQ+v4IiOfdh9F9OfuHQoRKGogol8lK5DTnG+cRg+A0ohv2azuVqtmAjfcJQhEr/awMHM9tbGI5oOOZyRUanUKgd1JBkd2ltmm81mfi0fBcWjgtFtudFo+GwB/sSlQyEKRRVM5KN0HZKsDmELGgg2ulo/fTBJPWQjMSzRCDJbP9J0yH4edRh0Gbf1DpltuVwG6cPhkIncG5fb7XaQzeNPWToUolBUwUQ+ItRhAMcUsu8VjLi3jl1AkB5cwLSO5g7gNp9t2wclbw8p6EFm8acsHQpRKKpgIh/x67BO+FOWDoUoFFUwkY8Idfj8/OwMcjjbLn4a+8TXnPYZwv6UpUMhCkUVTOQjQh3avUNL6Xa71FI7JRs+ymCWJI2UsUQfC+PT8ZYhNtChhd7gIxgdQwaDARfsLiOvnQZhOMQupeLAxuMxcz48PGAr7NzfdPSnLB0KUSiqYCIf0eowcWEpEBUl1Gg0sBA8+ClxfTg/IsLr0KfTgrZg3t048anp0Ky2MQyHwKnmP3w68gTxPv6UpUMhCkUVTOQjQh0GMFYFEmKgykZpebY94GJberIzUsazf3zNtlX+lKVDIQpFFUzkI34d1gl/ytKhEIWiCibyEaEON4bSbLw4GcArkzvmhdk4VJH0+31em7XbkJ6N+3y3n5rFn7J0KEShqIKJfESoQ9479JPLwBwWzEK8n+we4Xg85oLNYhPM/UYdIjEYejibzaBD7sfGDlo0TZLes7QJaPytTQvq4WQ3ln8b/pSlQyEKRRVM5CNaHVJdnKeNOvQhmj4K1LDJ0rZFxzDdh9VYioWbTiYTW2vAfDYBje3TB/XsOampP2XpUIhCUQUT+YhQh2VB4WXDVk+IP2XpUIhCUQUT+ZAOz4k/ZelQiEJRBRP5iFCHB4fScETgjniZjTD/9fV1uGLnJdDsnUIb+7jjUP0pS4dCFIoqmMhHhDo8OJTmag3fzmYzH0pj08pYIhWI9Pl8bjvEVkjJPqfJhhLywLgfC0P18+ZwMhomcq3hT1k6FKJQVMFEPqLVIVyySknWOkxSV+GAF4uFOQ+WslhQ6x1iq+Vyib4d5MR7gQzJYWZLtKAY5MfHYUNuNRgMuCsvYAuxoeSgQ9iUh8Hj4Sb39/c4VBo6G5XjT1k6FKJQVMFEPiLUYTzc3Nxk50c9Bn/K0qEQhaIKJvIhHZ4Tf8rSoRCFogom8hGJDsFj8QwGgzDpjEynU3++0qEQhaIKJvIRjw6LZrFY2GsMSIdCFIoqmMjH5eiQnbOgi1Yi0qEQhaIKJvJxOTpU71CIi0IVTOTjQnRI97RarXBFeUiHQhSKKpjIh3R4PM/Pz2HSHkiHQhSKKpjIx+XocLVaQYdY6Ha7TGy325PJBG9vbm4Gg0G/339Nr6be3d3NZrPr62u8ReJ4PKZHe71eo9GwVUzETprNJhZub2+52+l0yl0hG/bPxCzSoRCFogom8nEhOiQQGLRnb+9SUAJQmiXSUnyF+UajERTIVewF2qpOp2M7gQL5ihTkXy6XTAHrHYdIh0IUiiqYyMel6dC/hc/QL2ymvK67dzAZ7Difz5nodXh/f49lW8W9cYbS17SbyGwwJaWLzNg/E7NIh0IUiiqYyMcl6zAvpy0r6VCIQlEFE/k4bRMfOUfq8LRIh0IUiiqYyId0WBbSoRCFogom8iEdloV0KEShqIKJfNRVh/1+fzQa2dter9ftdqVDIS4HVTCRj7rqcOPcpNKhEJeDKpjIR1112Ol0OFj+NR0ayAXpUIjLQRVM5KOuOtyIdCjE5aAKdtFcpYSpO8nqcLVaBSlZHh8fbTmYsXOxWGy8UBkD0qEQl4Mq2EUzGAyskbVLha/rlne5XOL15ubG3kJd0CHsxYlX5vP5a6pDZJil2OOQ4D9syLdYi+XhcEhxQodBy05BYp+2H6/PEpEOhbgcVMEuHZuf2uvQpgqDAm3mMNqRc1j7PiIk52faTJLkNRWJzVV2e3uLPYxGI656eHgIeoTQIcTMSTstkZn3JKkUh8leOhSiUFTBxFd4He4ge7F0I37QwhkIhZMf+JgTdocrCkA6FCJCVMFEPvbU4ZkJhZOT3/zuN7+YT/j345//BP3dMMdJkQ6FiBBVMJGP+unw6x99AxbEa7vdhnIoRVvb6/Xw2u12P2xwNNKhEBGiCibyUT8dfvbl5/CfiXCjDjudTpJqbDqd3t7ejkajyeRDnrxIh0JEiCqYyEf9dPjp+Ef76BAnDo09PT1Bh0yxPHmRDoWIEFUwkY/66RB6g/++/8nHVylY/sGf/tDWQodIpPywMBwOpUMhaokqmMhH/XQI/vm//BfWL7y5uQlXnxrpUIgIUQUT+ailDs+MdChEhKiCiXxIh8cjHQoRIapgIh/S4fFIh0JEiCqYyId0eDzSoRARogom8lEVHWYHzjMiNAakQyEiRBVM5KMSOnx6emo0GlyeTqdcoA6bzSZeHx4ebm5u7u7uOB8bE8+GdChEhKiCiXxUQofj8bjdbgeJ1CGkkqSjCWHEfr+/XC7xttVqBZkLRToUIkJUwUQ+KqHD3Vh/0ZAOhRCqYCIfNdBh6UiHQkSIKpjIxyXosNvtzudze3twDI5t2O/3fbp0KESEqIKJfESuQ3tUIZTDZYbMMNH8tFgsuABsLQNq4MJ2u/38/MxEvOVWSOx0OpYncRddkch7kPzQ6+vrJL096XXoo3WkQyEiRBVM5CNyHVJLYDQacYEhM1ymn2YpTElc141RNmQ4HHIBNuVWjUYDOvR5DEvkhzJ/oEO/oXQoRISogol8RK7DSiAdChEhqmAiH9Lh8UiHQkSIKpjIRxV1OJlMVquVj455l23hM8c85tCQDoWIEFUwkY/IdcjwGT6z1xKDG36z2Yxq5Cs3sUnd+NbCZ5rN5iolSYcnQof2Eck6JMdH6+yDdChEhKiCiXxErsMkDfiE8MbjMcNqYCzGzrCPiMTRaIQFqI6v3ARruTm3og7v7++hSUaZYj8QEnuH/Iinpyda0Efr7IN0KESEqIKJfMSvw/iRDoWIEFUwkQ/p8HikQyEiRBVM5CNOHbYqhXQoRISogol8xKnDsP8VN9KhEBGiCibyUWkd+sloDmA+n1vEzbvYNG9ZpEMhIkQVTOQjch3agIebm5unpydLabVaNvso4LSiwxRmxsLDwwMTk/W4i/F4zPzYFUV4f3/PFJv1FG6j+bBz6JY7SdZPGOYy9mMDObhJeAJ7IB0KUSiqYCIfMevwag3fQoTT6XQwGEB+lKJ/AjCHTPT7fXYZe72eTb2NDLQavMVdJakyk7dzfxNuHuyEy8l6FCM+xTSZSIdCRIkqmMhHzDpMXO+QM24nqfzQ4ZvP5yZLKA1rmY3jBfEW3T6mQ2PL5ZIZ4DabhoZiM9vZtVCkmGXhOeyEm/ipALBsn5hIh0JEiSqYyEfkOjTa7fb+dwo5GP9sSIdCRIgqmMhHVXS4EXtCIbEenj3OKUnjZfw10t3siJfZgXQoRISogol8RK7Dd0NpGAtjoTRJekOx1+thLV6DwFGcLBcOiJfZgXQoRISogol8xKxDdP6gK+sCdjodHC3cY5OL+lAaLNtTeSFCqLHdbm+bids/DQNb2R1EypXxNVjes1spHQoRIapgIh8x6zB5L5SGOrRQGogNy81mk2Ev9rQKw2JhsvEy3MOOeJkdSIdCRIgqmMhH5Do0coXSnBnpUIgIUQUT+aiKDrdhI+uzBF1Dj4+1CdjzAqlHOhQiQlTBRD4i16FdLIXAms0m3TOdTm9ubkajEYNrkvWlVBt3j7XL5ZKXQ+2Cp424h0GRB9tSpeY/7IGXWLE8mUyCsNUdSIdCRIgqmMhHzDp8N5RmMBhwFUNjTIdcyyH5di+QcG/+HqHvDmL//lMsfTfSoRARogom8hGzDpP3QmlmsxmtBmsig01Dg7VYxd6kWc16h4y1sfAZm6qG8TVcRp4dF1QDpEMhIkQVTOQjch0aCqURQuRCFUzkoyo6zEUQDuNHGXo2Xg49YGIa6VCICFEFE/mIXIe7Z6VZLpd80AQcdnt7u1qt8DocDu3iZ5IGxfj9vKwnpsEOOekMsx0zMY10KESEqIKJfMSsw6s1fJt9wBNWUYecTYY5kcIYUYgNOek22wnhdVfshOkWlZqsH+REggc5bUM6FCJCVMFEPmLWYfK2V8fIFxgRHT74D11DXgVFp5DSgr2QQh3Sasg5Go2S9dMNk/W8bgCbQ0js/GHn2CFjZ5BoF1f3fDKGdChEhKiCiXxErkMDFmSk6A6QB6cDzwXp2+4dngrpUIgIUQUT+YhTh1mlxczT01N4AnsgHQpRKKpgIh9x6nB/Wq1WmLSdXJmLRjoUolBUwUQ+pMOykA6FKBRVMJEP6bAspEMhCkUVTOSjKjqczWY+YmWxWHBhm+EajUaz2bS3zL8t82ua398CXC6X4/HYrT890qEQhaIKJvJRFR1SHqPRiG+73e5wOHzdbjjo079l/m2ZX9P8Xp8v6Vzebv3pkQ6FKBRVMJGPquiQJrNOYZCehdN8B4nbMr+m+c/spzN/nBCXhiqYyEdVdPi6yYWvOw2XJVfmopEOhSgUVTCRjwrpcCO5DJcrc9Fs02Gj0biqLOHJCFEe+jqKfEiHZbFNHpX+j0wmkzBJiJLYXMGE2EalG9/XnIbLlblodujw7u7uNY3ueXp6smzT6fRNvgzX19fBQkC73Q6TUvgRSTpfa7jOhS+RXq/n3wZs3IMQpaDvosiHdFgW28xBHY7H46cUZuOlSHiOb/mkDnuLV0iLb5nNNuE+7+/vsQwdPj4+tlPwttFocC1zEny037nlZ7bXVId8EoilcIGBuEFArxAlsrmCCbGNOHW4Wq2srd8BWu1chsuVuWi2nV22d4gFXoSk52BKPo6Db2mgJH1w42u6WyzYJoSfRQsys+2Na7FDlPkvfJjxnOUn0CES+agQLGCrV7fn1/SfYpmFKJHNFUyIbZSow4eHB/Rabm9vX9MG9yV9GAXbaDSyWPWatvWDwQCNL9MXiwXydLtd5qmrDsOk9SM+7G0whiQ7pOQ1swnKjQvMjNLzaz1B+sad74AuF6J0NlcwIbaxsfE9AxQbmlqzApwH+fGt6fB13Y5Th8/Pz8gAQXLV5ehQCJGXzRVMiG3E0PgG/Y9sr4XXTjd2U3IZLlfmotmmw6enp5eK02g0wiQhiieoSpsrmBDbyH6HqkUuw+XKXDTbdFj1/4gQZRHUnc0VTIhtVL3xpeGCCWu2zb4tHQpRY6RDcRQxN76M+H9NNTafz+E8xt28pi6ZTqftdhurhsMhZ9/GMjd5fn5+XV+DRcpkMkFO5JEOhagx0qE4ipgbX4smfU3lcZWOiuNo9Kv1sLlWigXXWMRNMwWZ7xzS4W440lHsCX91iXiQDsVRlNj4HgA7fHZpNEmSdw1ngTmr1erdzOckBh3yIVkE7TvHZoj9cWUpykc6FEdxzsa3CHIZLlfmoolBh36o/vl1iB8o/+7z//aT//v/8Pev/+ufcyhktXBlKcpHOhRHcc7GtwhyGS5XZoMXXe2tzUPGWWO43O/3X9edV7SSls6pXl43zTgqHf75//4/EOFvfO93/tYv/RIW8DbMET2uLEX5SIfiKM7Z+BZBLsPlymzwtiWjdV5TC/IZTK/pTUomctKAx8dHJlp6ks6LfZVOtBbM5xmDDnlsXD6zDgeDAfuFPAYu+wzX19d4RbH7xNh4W5yiZKRDcRTnbHyLIJfhcmXeBlx4c3PDUB2b9hM6fHh44FTXcKGl4xOv0hlBucoTgw5fXbf1zDpEEe3WIUCpdjodZkZH9vb2Nkk9end3xwxMwetVOkG52/RMvC1LUTLSoTiKEza+Pi7j1fWcCiWX4XJlLppIdPiaFgsEj65t2NgXCU6TCvzP//Ov8fqdP/jDrA6TtG8N+fHYrtIZw7EwGo0obxw51iKRE4uf34hhUYpSkQ7FUZyw8Q0e/bqtuT8JyfrhfHsajvn3zHwetpXPCf8je4JiMcGck9/43u/QiPzD2zBH9IRFKUpFOhRHccLGN9DhxilGT0guHb6m+ffPfAYi0aF14s+vQ3D7j/7hn/2Pv/4Pk/9+8+u/Gq6rAm/LUpSMdCiO4syN76ngoy32H0rI/BYOEwOR6NAoRYdVJyxEUSrSoTiKshrfk/Dw8NBKCVdsgjO67Zn5DEiHNSAsRFEq0qE4irIa31MBvWWH9G1kNBrtn/kMSIc1ICxEUSrSoTiKshrfU5Grt8cJwSMhch2uVqvn52cuD4dDv0oYYSGKUpEOxVGcvPENG4yCwfGHSdvJlfnkPDw8+IKKVocva2jB29vbXq/HVcE8asiAVYPBAL8zuNYMeiGEhShKRToUR3Haxvf8F9xw/NfX1zYuO3J8WUWrw0aj0Ww2YcF+v4+DRIrpMGA+nyfrsfCdToeZL4qwEEWpSIfiKE7b+J5Zh91u94v5xP7inwPal1W0OoTeOJ4dOlwsFlCj6RBvuXCVjo5HTrjTdLhcLtEDZoYLISxEUSrSoTiK0za+Z9YhLXiVgoWn//IfbRVb8Jf3ro5u6/cUhC+raHV4MLPZDD9QwtRaExaiKBXpUBzFaRvf45vU/UGnBAr8/T/6x3/yV8/WQbS1nLWLOnx8fETHZTgcTiaT6XSapN3KJH3uhHRoZENp/NvdIPN4PPYpZ57yrSzCQhSlIh2Kozht43t+HX6z+62fzn62UYdJOgc0RMi3/s7Wzc2NpVviGfBlFa0O22tardZgMEjSu4mcSpvFaOazwBmG0iTpbKL4v+AV+bmAnyB+0u2aERaiKBXpUBzFaRvfc+owyVws/bd/9u/DHCmM+ADsGtrb2Wz2VY5z4csqWh1aKI3dCLQSS9aBMxthh9JuJV5CiE1YiKJUpENxFKdtfM+sw9/+3nesX/iFQmmOIxtKYzpkyIwtcyFxvUM+AJILWR3WOMQmLERRKtKhOIrTNr5n1mGigRano7j/XY1DbMJCFKUiHYqjOG3jW1yTWg98WcWsQ/SzbUxFln6/HyY5Xt6L5m21Wux9WrhNdWUZFqIoFelQHMVpG1/pcDe+rKLVoYXS4Ah5ORTeajab6IUnaTQNdNhoNBhlg1WUGWNt4NHJ5BcBTUifTqfMw4vY2bgb9OmXyyXjbpJUioy7GY/HZw5xOpiwEEWpSIfiKE7b+EqHu/FlFa0OLZSGoUaMP4ICvQ7v7+/ZtzMdGi8pSOcOt8XRwJHYCZepQy4wv3QoDkA6FEdx2sbX69BayaBpq0pLVwS+rKLVoYXS4D9IOeEVelssFjAldYhXBtfAkSYz8pJeLMUmw+GQm1Oc2bgbM67XIfqLkLGZMnLCQhSlIh2Kozht4+t1iKYN7Vqy9h87GfaWF9BarRbjD9HCovWMPzT0SHxZRatDsT9hIYpSkQ7FUZy28Q10uPHCF99yFPxtCjOPRiPp8PXU/5H9CXR4/P/isKdbHLZVWYSFKEpFOhRHcdrGN9vD8CO4A7Krjm+CI8eXVbQ6fFkzHo/5Y4VXNTl2EJ14Xkol+JcNU/i22+1iQwbLINGe/WRXzi1g9fHxkebzO0/SwY4Wa4MDiDzoNCxEUSrSoTiK0za+WR0Kjy+raHUISzE6lNe3eXsPfXoLpfE39vgLxg+9gPCQEyZDIm2KM80Ow/dTAtnOuZy43WY3jIqwEEWpSIfiKE7b+O6jw2azGQRf7CDy1jAvvqyi1eG7oTSUHP6PXJs4HSID9oPX0WhkOlwulzapjV0Lhf9Mlrbz5O3sshawEy1hIYpSkQ7FUZy28fU6ZGOXpJfF8GPf2rjEXRS1+BpsyLuJSRpfY30Ff11uH9dGji+raHUo9scXIOSNPrFPEWdGOhRHcdrGd6MO0VHgAoUHwzHi1OMfD2TxNVy2dNtPdfFlFbkOc93Hzd4GNnI96WmjkmMOrkG5cdKAsDRFGUiH4ihO2/gGOrRWkm2r9f92YJtkF4LlKuLLKlodvqyBh/BPhM94K5ERLujQY4GvSTqzTPI2eNgia9jFpw6xHyTapW8fI/OyntTNRvQzuMZ+IWErC9VBol1RiAG5MCqkQ3EUp218N/66F4Yvq2h1mH3AE8UGD6GDzhuBfH1Zzz4TjKXp9/u2qtPpUGA+XsYmtfHgt45lxh4sAzKbMjduWCK+AHWxtHSkQ3EUp21899HhtviIyEPqT4Ivq2h1aKE0gQ4ZIMMIGoujuUpnn2H8iz1XhJE1yINVDJvCMvL4eBn7DthNZZvghsE19taCd7icvdJeImEhilKRDsVRnLbx3XjvMAil8Tel/BTPaB+5gFbYegCr9cyWXBX0QiqHL6todVgQVZl3LRdhIYpSkQ7FUZy28d2owyCUJouF2HABzvPjK3jfaNu21cKXVeQ63BZKkyuwZbomXLET3iHe8YSpPTnDZdWwEEWpSIfiKE7b+G7UoTB8WUWrQ3vAU6vVGo/HjfWznCaTyWw26/f7zWaTc8fghw6Uw380BynitwvjbhopyfpaKHfFzJaHD3hK1g+QStJfTljFu4w8Hts/7MjgHfOxrcIH2UhWe9oUsjHxMX3UFNeenLAQRalIh+IoTtv4Fn3Brer4sopWh0EojUWvwC7ot0GHcBhkyTuFUJTNL4NVdknctmIKd4VNkNnnSdwDpJiIT4EOX9axM9wkSe8+MsWw/fh4Y7sDjbXY1Yt71FQRhIUoSkU6FEdx2sZ3R7vDto9zWobr3mO1Wvkf+H5KsGR7N3T/uW/Ohi+raHUYhNJY2At6ePhHsHdINeIUsJVdk6Qp6S2LefE6ZIyM5eGACkvnTqhDLtsqfme4lfUO0V/kvx6v1hPlPx2dS3yKBebYOI2TExaiKBXpUBzFaRtfr0M2XtZ1oA7xaiGF+OGfvUGF/BZ8aHtjk8odomE1HXJztolowR/TmJ0kvT6GthutYRCAg52jN2MBkOfHl1W0OhT7ExaiKBXpUBzFaRtf36RSQryQBT/xVpOtTTKdPKqLc11iK2zOPsoqBZvzOhgO2DbknSfqEKJFn4BdByxYRwHdSl6gQ2bs3Nv0/PiyilyH2V8qJFcoTZLecTxm8oSNH8com33uCBbt+LAQRalIh+IoTtv4nqT1QTOHo6pHKGmAL6todfiyBirqpUPL+UMEPzLgoeFwiH8Q0tH/5qVv9vuDyWg4fYw9qsnSB4PB7XqmG3v8k30QDccPso9L1j+tOEtOsv4B5B8mhb3ZF+ZlfTXefkLxoy1cGWdql0/v7u720eo2wkIUpRLUnc0VTIhtnLbxPYkOa4wvq2h1aA94Yje6v37KErWHt7AL+o685kxvcUMfSoNlZLC3XEB/3W70Xq+faMH7kbbJaj3S1D7OduJHLuKQoDp7mBQWgn4kr9JbJA4+2o4zSfWZpD+8oORjLp6HhShKRToUR3HaxhdNaktsx5dVtDpsNBo0EC3oHyqCXh38BP0gEXJCh+zq7WQ0FtiJDLM1cI9d6M7qMHEfhMx2dZ2v3CdV50ejwpoc2sFr4AyF5Spb4OFx2XqxSXpB3k6K19K5fABhIYpSkQ7FUZy28VXvcDe+rKLV4dkwb1WXsBBFqUiH4ihO2/i+26RaPP2OFPxg912E/dkW/bEDftC79ymf3HCCgB1B/NlNfFlFrsMDCjNZd7ayJ042xsXsj+32yP2ckLAQRalIh+IoTtv4+iaVjdd4PGYngCEPNiyMeZ7SJx4kLqIB2Zi/0WhwDwyp4NU2rIW6LKQCO+cncoccocE8vLAGV7XSiA9m83ETNgaDg7UTNwzOgkGY0yIyiJ88DJtgD5zDmin8oI0nm1REhy9rWM4Me/EnYr062yQISOGJsxBQdAwStrgYbo5tfZxLAPMEBYtE7pP3GjdueGbCQhSlIh2Kozht42vt41UKmlFrJRny0Fk/wYegQWSKj2hgU3h/f8+Bgz6AAquwW0vhrpAC+WH/TGcetpVoTG23G+MmkAEKpA75lntL3vbtuBWtYNCd2CQ4Iy5cpSM9sIrHxkRfVtHq0EJprJxx/BaQYnfjkvXJ+oJFNmjPfgfgP95KB+9n42JIUD7E8gQFyy8GQ2/s31QuYSGKUpEOxVGctvHN9g7RLeDAQYY8sKEkaDfpjORtRAN6AC9pzEUnnfqEu0Jm9ud8O4idIyc7EGjBm82m5WH7i1WM+GB+/ylM5LZsf3k81h3Bzm32cN+IB1LspRNs8vId98B06pzLtokvq2h1GITSJK4fhmMz3/uTtYJl7Ax/x/A/jgWUpPXqsLldhrV+dlCk9hFBwVqvFPuxf1O5hIUoSkU6FEdx2sb33XuHF44vq2h1KPYnLERRKtKhOIrTNr7xxDhECLo4vqykwxoQFqIoFelQHEVZja+QDmtAWIiiVKRDcRRlNb5COqwBYSGKUpEOxVGU1fiKKuowO8iy1WoFKdv2k83pCdYGM8X4wS1RERaiKBXpUBxFWY2viFmHjCm1SE4Pwz4tsBN5OAowSZ/A5UNwk3SAio3WsDlO+XY0GmHbYG2SRhFnR1AcNifAGQgLUZSKdCiO4vn5OUwSZ6HRaIRJKTHo0J73aykcoGlvl8slR0f4hzf5ESn0HB8RzBSIkOMIbfwJ13LEi+//BU5Ntog5BsJCFKUiHYqjSNwsJOKchP+JNTtWFcrGi5y0lB8Oz5TAgr73Zpm3del2TInHbbNjCrftqnTCQhSlEtQd6VCIahOVDsVuwkIUpSIdClErYtDhxvGjNgeeTwwuou7JaDQKk9b4Ofmy7H42oZ9a1sjeicyy7WKsn0RpI2EhilKRDoWoFTHokBPsWQovjVJUVylMh5ygw2Ambv98RJs5lgv+I5rNpi23Wi0TEj/F9naVTuPHtfZ84MRdULXrqNh5o9HgbqFAm3KWj2a0PNyQCrfruvbp2IpzCibpUXG2uSQjWrNyWIiiVKRDIWpFPDr0fThIhRGkviNlc4cuFgtL9xm4jDzsovkd+niZ2xQu81OQnzcpvQ47KdtuPfKhxNytfWKS7tzGh9gBBDuxT2cGrr1NZ821nP6ALZg2LERRKtKhELUiBh0ewz4XJzmjd1nsvh6bi7AQRalIh0LUiqrr8KIIC1GUinQoRK0oS4fgcQu9Xi9MEilhCYpSkQ6FqBUl6nAb0GGYdElc+OlXCOlQiFoRmw55SbDEC4PtlDD1XOiiaIWQDoWoFbHpkCq6WCHxxEs8ALE/0qEQtSI2HZbbO7TPLfcASvw1IPZHOhSiVkSlw7IkZJiHShFS6TIWuZAOhagVUemQtFqtMEmI+JAOhagV0qEQhyEdClErpEMhDkM6FKJWSIdCHIZ0KEStkA6FOAzpUIhaIR0KcRjSoRC1QjoU4jCkQyFqhXQoxGFIh0LUCulQiMOQDoWoFdKhEIchHQpRK6RDIQ5DOhSiVkiHQhyGdChErZAOhTgM6VCIWiEdlgKfqnghLBaL8PxrgXQoRK2QDkshNMah4N+H4hoMBuGKyAjPvxZIh0LUCumwFEJdHESn0/liPuHfj3/+k9VqFeaIhvD8a4F0KEStkA5LIdRFfu7u7ijCqxQs/PD5j32G6XTq35ZLeP61QDoUolZIh6UQ6iI/n335ORT469/+B9ZBxJ/PwM4iCvP+/r7X6y0Wi6enp/F4vFwukYjl0WjEbJPJZDab9ft9vDYaDbv02u12k7QP6vZ6IOH51wLpUIhaEVTpsBmLDDTi/mizIEO4TRmEh5Uh3CA/n45/BP99s/utjTqEAm9vb+fz+cPDA1MeHx/RicQC0i2RQHjIORwO8YoNkZPp1OFJCM//LZB0uEGU4Dj9YUuHQtSKaukQLbU/2izWlJdLeFgZwg3yA6sFF0t/8Kc/9GvxinSvQ3QQ0fnDMhO5PJ1O0VlEHxGZ8Yp+JNPx9iT9QhKe/1si+RGzD/6wpUMhasVJdIhmFG0xW2csFBfTcSodfu1Xftl6VEFX6SSEh5Uh3OAgfvO737SzqHQozcE6xP/OSgD/03B1AfjDlg6FqBXH6xCtsHVT/u7f/3tc9hmazSZvU5H7+3u3Mh8n0SHN/fWPvmFXGu/u7mwtzgIHbG/7/b4t7094WBnCDepOeP5vOUyH2WAi3z9O0n+lf3vMF8/why0dClErjtehXbWDReynuhdMkgZ0WBwHBIOch90uOokOrQ21o/10/CNbi9PhPTPsqtFo3NzcwOVY4JVDrAoa2Y2Eh5Uh3KDuhOf/lsN0OPzi+Yv3gonAcDicTH6Rji+eBQodfB3YH7Z0KEStOF6H19fXbIn+RuNvWquERJ8H/qNFIBu0SpAib1DlpSAdfvbl57aWd91QLI8p9Pp8PrcM+/QXw8PKEG5Qd8Lzf8thOvzxz3/yRRpM9NPZz7I6ZDARO45MwT/OBwodhj9s6VCIWnG8DrEHE4w5BomWAYnoYFkcB3uH+/SxspxQh9//5GM7WiqQIBE9Qi7gOCFCLCCD+Vs6PIDw/N9ymA5/7bc+yn7xbC3/p7PZDL+9ePUb/zgLFDoYf9jSoRC14ngdgt/7/d+zn+f4Q6MT5jgRJ9HharVix4J/JxxOYISHlSHcYAtBJ3sH3ugbOcmdM0+u/3J4/m85TIdJeu3a/o/nCSbyhy0dClErTqJDcpcSpp6Uk+iQDAYDyMb3Yk9IeFgZwg3S68nJegiEmYb91CQ92mTdsQ5WGc/Pz5QiXnnbLEl3yETTIS/88pV5IBXbG3biI4mCfyjzMwMOEgtMsWLHW34Q0+1EwvN/y8E6TNKLE/g/nm3WVn/Y0qEQteKEOjwDJ9RhoYSHlSHcYB0GGXS5zFIMzYUO7SKzHx8CLQ2Hw1arRfPRDcvlMkl3yER/jZfGarfbzJO4vT2uR+uTjVeG7VA3HgyOJFl/uhGe/1uO0eGZ8YctHQpRK6TDIggPK4PPvFgsuMBZRiGS3Rf99p+MdGNO3ztMtuTZmOixzXccqj+R8PzfIh0KIcpHOiyC8LAyhBvUnfD83yIdCiHK5zAd8prey3s33nJFW3gsMCS4WHeMDnmFsNvtvnvYxxMeVoZwg/3gTNw7OmSeHYW/8RJooYTn/xavw20jAn0okJ1a9kQOuHu9o6CyB+MPWzoUolbso8PZbJasJ3Rmc8P7RnbryG5QJes4C4uksAaF6dyJXYtDO8ibTNBYq9ViIKXfmw8eYTZ/tFm8DtnI8uCTdXuKVwu1zwaeWMvoldNut/1J4ThtlW9JfWxLeFgZLKfhQ2ks2JUlbAEvvXScgBU7r7Jmu1YW6oLSsFAX7pOhLlaqRUTVbiQ8/7ds06EvatNhL4WF4HXI67emQ/tqJelpWhkyhW/fLSgcTFBQ/rClQyFqxW4dstFhHAebe9Nh4nqHXmBUmsVxWOvmAzQMm5sGH3GbkmR06DfMpcNgZjg2ncMUJmYDTzZiBmIeOzayMZwkPKwMltOw+JTkrVmXKVzmWjbcs5Tk7ecS25WVAPLYPnH6QamegfD837JNh76ovfnsV0hwIvjhYpv7aY9w7hv/v+8WFPYWFJQ/bOlQiFqxW4exkUuHJRIeVoZwg7oTnv9bsh3caPGHLR0KUSukwyIIDytDuEHdCc//LdKhEKJ8gir9GD3+aLOEuUsiPKwMrQsjPP+3QIdhCUbJaDTyhy0dClEr1DssgvCwMoQb1J3w/N+i3qEQonykwyIIDytDuMF+PD8/h0lpmO67o+YD/AM6EjfFWnGE5/+WjecVIYvFwh+2dChErQiqtDgPYUObOomBlOPxmBOHYuFp/ZBIrMICEqmux/RZjEhkRK6NAGEYpK3FJliwsQf2tL8sfmBJEYTnXwukQyFqhXRYCpQEehvWt4O0bIgkrLZMHwlJvdkQFI69w1a8lcVEG43w8PDQ7/df1k9qZLrvCO542l/e/mVewvOvBdKhELVCOiyFUBepAukqdAGX6SP6kGIPiaTbbPj5VfosRiZepSRrHQZrsYkNTOy5p/1ZN/HI5//tSXj+tUA6FKJWSIelEOqiMNDpPNvUMzsIz78WSIdC1ArpsBRCXdSa8Xgcnn8tkA6FqBXSoRCHIR0KUSukQyEOQzoUolZIh0IchnQoRK2QDoU4DOlQiFrR6/VehBD5abfbvipJh0IIIYR0KIQQQkiHQgghxKt0KIQQQoD/D1dKDk3B0ysIAAAAAElFTkSuQmCC>