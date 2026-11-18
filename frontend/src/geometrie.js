// Conversion mètres -> pixels. Le backend ne parle qu'en mètres (Y = 0 au sol, vers le haut) ;
// le canvas a son origine en haut à gauche, donc Y est inversé ici et seulement ici.
export const PIXELS_PAR_METRE = 40
export const MARGE = 30

export function dimensionsCanvas(niveau, echelle = PIXELS_PAR_METRE, marge = MARGE) {
  return {
    largeur: niveau.largeurZone * echelle + 2 * marge,
    hauteur: niveau.hauteurZone * echelle + 2 * marge,
  }
}

export function metresVersPixels(posX, posY, niveau, echelle = PIXELS_PAR_METRE, marge = MARGE) {
  return {
    x: marge + posX * echelle,
    y: marge + (niveau.hauteurZone - posY) * echelle,
  }
}
