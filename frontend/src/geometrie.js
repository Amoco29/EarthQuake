// Conversion mètres -> pixels. Le backend ne parle qu'en mètres (Y = 0 au sol, vers le haut) ;
// le canvas a son origine en haut à gauche, donc Y est inversé ici et seulement ici.
export const PIXELS_PAR_METRE = 40 // échelle par défaut des tests ; le jeu la calcule avec calculerVue
export const ECHELLE_MAX = 80
export const MARGE = 30
// Coin haut-gauche de la zone de construction (X = 0, Y = hauteurZone) en pixels, quand rien d'autre n'est précisé.
const ORIGINE_DEFAUT = { x: MARGE, y: MARGE }
// Plein écran : place réservée au HUD (haut) et à l'étagère + la dalle (bas).
export const RESERVE_HAUT = 70
export const RESERVE_BAS = 170

/**
 * Échelle et origine pour un canvas plein écran de taille `espace` : la zone est centrée en largeur,
 * son sol (Y = 0) est posé juste au-dessus de la réserve du bas.
 */
export function calculerVue(niveau, espace) {
  const sol = espace.hauteur - RESERVE_BAS
  const parHauteur = (sol - RESERVE_HAUT - MARGE) / niveau.hauteurZone
  const parLargeur = (espace.largeur * 0.6) / niveau.largeurZone
  const echelle = Math.max(1, Math.min(ECHELLE_MAX, parHauteur, parLargeur))
  return { echelle, origine: { x: (espace.largeur - niveau.largeurZone * echelle) / 2, y: sol - niveau.hauteurZone * echelle } }
}

export function metresVersPixels(posX, posY, niveau, echelle = PIXELS_PAR_METRE, origine = ORIGINE_DEFAUT) {
  return {
    x: origine.x + posX * echelle,
    y: origine.y + (niveau.hauteurZone - posY) * echelle,
  }
}

/** Inverse de metresVersPixels : position en mètres d'un point du canvas. */
export function pixelsVersMetres(px, py, niveau, echelle = PIXELS_PAR_METRE, origine = ORIGINE_DEFAUT) {
  return { x: (px - origine.x) / echelle, y: niveau.hauteurZone - (py - origine.y) / echelle }
}

/** Nœud dont le centre est à moins de `rayon` pixels du point, le plus proche s'il y en a plusieurs. */
export function noeudSous(px, py, niveau, echelle = PIXELS_PAR_METRE, rayon = 14, origine = ORIGINE_DEFAUT) {
  let meilleur = null
  let distanceMin = rayon
  for (const noeud of niveau.noeuds) {
    const { x, y } = metresVersPixels(noeud.posX, noeud.posY, niveau, echelle, origine)
    const d = Math.hypot(px - x, py - y)
    if (d <= distanceMin) {
      meilleur = noeud
      distanceMin = d
    }
  }
  return meilleur
}

/** Lien dont le segment passe à moins de `seuil` pixels du point. */
export function lienSous(px, py, liens, niveau, echelle = PIXELS_PAR_METRE, seuil = 7, origine = ORIGINE_DEFAUT) {
  const parId = Object.fromEntries(niveau.noeuds.map((n) => [n.id, n]))
  let meilleur = null
  let distanceMin = seuil
  for (const lien of liens) {
    const a = metresVersPixels(parId[lien.noeudDepartId].posX, parId[lien.noeudDepartId].posY, niveau, echelle, origine)
    const b = metresVersPixels(parId[lien.noeudArriveeId].posX, parId[lien.noeudArriveeId].posY, niveau, echelle, origine)
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

/**
 * Paire de nœuds voisins (distance <= longueurMaxLien) sur laquelle aimanter une pièce tenue au point (px, py).
 * Candidates : celles dont le milieu est à moins de `rayon` pixels (par défaut une demi-maille de la grille).
 * On prend celle dont le segment passe le plus près du pointeur : les deux diagonales d'un carré ont le même milieu,
 * c'est la position du pointeur qui les départage. Le milieu sert seulement à départager les ex æquo.
 */
export function cibleDePiece(px, py, niveau, echelle = PIXELS_PAR_METRE, rayon = 1.25 * echelle, origine = ORIGINE_DEFAUT) {
  const maxGrille = niveau.longueurMaxLien ?? Infinity
  const { noeuds } = niveau
  let meilleure = null
  let scoreMin = Infinity
  for (let i = 0; i < noeuds.length; i++) {
    for (let j = i + 1; j < noeuds.length; j++) {
      const a = noeuds[i]
      const b = noeuds[j]
      if (Math.hypot(a.posX - b.posX, a.posY - b.posY) > maxGrille + 1e-9) continue
      const pa = metresVersPixels(a.posX, a.posY, niveau, echelle, origine)
      const pb = metresVersPixels(b.posX, b.posY, niveau, echelle, origine)
      const dMilieu = Math.hypot(px - (pa.x + pb.x) / 2, py - (pa.y + pb.y) / 2)
      if (dMilieu > rayon) continue
      const dx = pb.x - pa.x
      const dy = pb.y - pa.y
      const t = Math.max(0, Math.min(1, ((px - pa.x) * dx + (py - pa.y) * dy) / (dx * dx + dy * dy)))
      const score = Math.hypot(px - (pa.x + t * dx), py - (pa.y + t * dy)) + 0.05 * dMilieu
      if (score < scoreMin) {
        meilleure = { departId: a.id, arriveeId: b.id }
        scoreMin = score
      }
    }
  }
  return meilleure
}

// Les images de décor font 1376×768 ; le haut de l'herbe est à 660 px du haut.
export const RATIO_DECOR = 768 / 1376
export const SOL_DECOR = 660 / 768

/** Calque de décor (pleine largeur) décalé pour que le haut de l'herbe tombe à `sol` pixels du haut. Renvoie { hauteur, haut }. */
export function positionDecor(largeur, sol) {
  const hauteur = largeur * RATIO_DECOR
  return { hauteur, haut: sol - SOL_DECOR * hauteur }
}
