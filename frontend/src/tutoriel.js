// Étapes du tutoriel (niveau 0). `cible` = valeur de l'attribut data-tuto de l'élément à mettre en valeur.
// `condition.nombre` = nombre d'occurrences de l'événement à cumuler avant de passer à l'étape suivante.
export const etapesTuto = [
  { id: 'materiau', cible: 'panneau-materiaux', texte: 'Prends une pièce de bois dans le panneau.', condition: { evenement: 'materiauChoisi', nombre: 1 } },
  { id: 'lien-vertical', cible: 'zone-construction', texte: 'Glisse-la entre les deux nœuds de gauche : un lien vertical suffit pour atteindre les 2,5 m demandés.', condition: { evenement: 'lienPose', nombre: 1 } },
  { id: 'diagonales', cible: 'zone-construction', texte: 'Un carré sans diagonale se déforme. Ferme les deux carrés avec une diagonale chacun.', condition: { evenement: 'lienPose', nombre: 4 } },
  { id: 'budget', cible: 'budget', texte: 'Surveille ton budget : chaque lien coûte de l\'argent.', condition: { evenement: 'suivant', nombre: 1 } },
  { id: 'seisme', cible: 'bouton-seisme', texte: 'Lance le séisme pour tester ta structure !', condition: { evenement: 'simulationLancee', nombre: 1 } },
]

export const etatInitial = { index: 0, compte: 0 }

export const estTermine = (etat) => etat.index >= etapesTuto.length

/** Fonction pure : renvoie le nouvel état après un événement (inchangé s'il ne concerne pas l'étape courante). */
export function avancer(etat, evenement) {
  const etape = etapesTuto[etat.index]
  if (!etape || etape.condition.evenement !== evenement) return etat
  const compte = etat.compte + 1
  return compte >= etape.condition.nombre ? { index: etat.index + 1, compte: 0 } : { ...etat, compte }
}
