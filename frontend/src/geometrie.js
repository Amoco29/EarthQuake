// Conversion mètres -> pixels. Le backend ne parle qu'en mètres (Y = 0 au sol, vers le haut) ;
// le canvas a son origine en haut à gauche, donc Y est inversé ici et seulement ici.
export const PIXELS_PAR_METRE = 40 // échelle par défaut des tests ; le jeu la calcule avec calculerEchelle
export const ECHELLE_MAX = 80
export const MARGE = 30

export function dimensionsCanvas(niveau, echelle = PIXELS_PAR_METRE, marge = MARGE) {
  return {
    largeur: niveau.largeurZone * echelle + 2 * marge,
    hauteur: niveau.hauteurZone * echelle + 2 * marge,
  }
}

/**
 * Échelle (pixels par mètre) pour que la zone de construction et ses marges tiennent dans l'espace disponible,
 * sans dépasser ECHELLE_MAX. `espace` : { largeur, hauteur } en pixels.
 */
export function calculerEchelle(niveau, espace, echelleMax = ECHELLE_MAX, marge = MARGE) {
  const parHauteur = (espace.hauteur - 2 * marge) / niveau.hauteurZone
  const parLargeur = (espace.largeur - 2 * marge) / niveau.largeurZone
  return Math.max(1, Math.min(echelleMax, parHauteur, parLargeur))
}

export function metresVersPixels(posX, posY, niveau, echelle = PIXELS_PAR_METRE, marge = MARGE) {
  return {
    x: marge + posX * echelle,
    y: marge + (niveau.hauteurZone - posY) * echelle,
  }
}

/** Nœud dont le centre est à moins de `rayon` pixels du point, le plus proche s'il y en a plusieurs. */
export function noeudSous(px, py, niveau, echelle = PIXELS_PAR_METRE, rayon = 14) {
  let meilleur = null
  let distanceMin = rayon
  for (const noeud of niveau.noeuds) {
    const { x, y } = metresVersPixels(noeud.posX, noeud.posY, niveau, echelle)
    const d = Math.hypot(px - x, py - y)
    if (d <= distanceMin) {
      meilleur = noeud
      distanceMin = d
    }
  }
  return meilleur
}

/** Lien dont le segment passe à moins de `seuil` pixels du point. */
export function lienSous(px, py, liens, niveau, echelle = PIXELS_PAR_METRE, seuil = 7) {
  const parId = Object.fromEntries(niveau.noeuds.map((n) => [n.id, n]))
  let meilleur = null
  let distanceMin = seuil
  for (const lien of liens) {
    const a = metresVersPixels(parId[lien.noeudDepartId].posX, parId[lien.noeudDepartId].posY, niveau, echelle)
    const b = metresVersPixels(parId[lien.noeudArriveeId].posX, parId[lien.noeudArriveeId].posY, niveau, echelle)
    const dx = b.x - a.x
    const dy = b.y - a.y
    const t = Math.max(0, Math.min(1, ((px - a.x) * dx + (py - a.y) * dy) / (dx * dx + dy * dy)))
    const d = Math.hypot(px - (a.x + t * dx), py - (a.y + t * dy))
    if (d <= distanceMin) {
      meilleur = lien
      distanceMin = d
    }
  }
  return meilleur
}
