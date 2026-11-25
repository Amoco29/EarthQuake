import { describe, expect, it } from 'vitest'
import { calculerVue, cibleDePiece, ECHELLE_MAX, lienSous, metresVersPixels, noeudSous, pixelsVersMetres, positionDecor, RATIO_DECOR, RESERVE_BAS, RESERVE_HAUT, SOL_DECOR } from './geometrie.js'

const niveau = { largeurZone: 5, hauteurZone: 10 }

describe('metresVersPixels', () => {
  it('place le sol (Y = 0) en bas de la zone', () => {
    expect(metresVersPixels(0, 0, niveau, 10, { x: 5, y: 5 })).toEqual({ x: 5, y: 105 })
  })

  it('place le haut de la zone (Y = hauteur) en haut, sous la marge', () => {
    expect(metresVersPixels(5, 10, niveau, 10, { x: 5, y: 5 })).toEqual({ x: 55, y: 5 })
  })

  it('inverse bien l\'axe Y : plus haut en mètres = plus petit en pixels', () => {
    const bas = metresVersPixels(2.5, 2.5, niveau, 10, { x: 5, y: 5 })
    const haut = metresVersPixels(2.5, 5, niveau, 10, { x: 5, y: 5 })
    expect(haut.y).toBeLessThan(bas.y)
    expect(haut.x).toBe(bas.x)
  })
})

describe('noeudSous et lienSous', () => {
  const niveauComplet = {
    largeurZone: 5,
    hauteurZone: 10,
    noeuds: [
      { id: 1, posX: 0, posY: 0 },
      { id: 2, posX: 5, posY: 0 },
    ],
  }
  const p1 = metresVersPixels(0, 0, niveauComplet)

  it('trouve le nœud sous le pointeur, avec une marge', () => {
    expect(noeudSous(p1.x + 5, p1.y - 5, niveauComplet).id).toBe(1)
  })

  it('ne trouve rien trop loin d\'un nœud', () => {
    expect(noeudSous(p1.x + 80, p1.y, niveauComplet)).toBeNull()
  })

  it('trouve un lien au milieu de son segment et pas à côté', () => {
    const liens = [{ noeudDepartId: 1, noeudArriveeId: 2, materiauId: 'BOIS' }]
    const milieu = metresVersPixels(2.5, 0, niveauComplet)
    expect(lienSous(milieu.x, milieu.y + 3, liens, niveauComplet)).toBe(liens[0])
    expect(lienSous(milieu.x, milieu.y + 40, liens, niveauComplet)).toBeNull()
  })
})

describe('calculerVue', () => {
  const niveau = { largeurZone: 5, hauteurZone: 10 }

  it('ne dépasse jamais l\'échelle maximale et centre la zone', () => {
    const { echelle, origine } = calculerVue(niveau, { largeur: 5000, hauteur: 5000 })
    expect(echelle).toBe(ECHELLE_MAX)
    expect(origine.x).toBe((5000 - 5 * ECHELLE_MAX) / 2)
  })

  it('pose le sol au-dessus de la réserve du bas et le haut sous le HUD', () => {
    const { echelle, origine } = calculerVue(niveau, { largeur: 5000, hauteur: 700 })
    expect(origine.y + 10 * echelle).toBe(700 - RESERVE_BAS)
    expect(origine.y).toBeGreaterThanOrEqual(RESERVE_HAUT)
  })

  it('ne tombe jamais sous 1 px par mètre', () => {
    expect(calculerVue(niveau, { largeur: 0, hauteur: 0 }).echelle).toBe(1)
  })
})

describe('noeudSous et lienSous avec une autre échelle', () => {
  const niveau = { largeurZone: 5, hauteurZone: 10, noeuds: [{ id: 1, posX: 2, posY: 3 }, { id: 2, posX: 4, posY: 3 }] }

  it('retrouve un nœud à l\'échelle donnée', () => {
    const { x, y } = metresVersPixels(2, 3, niveau, 80)
    expect(noeudSous(x + 3, y, niveau, 80).id).toBe(1)
    expect(noeudSous(x + 3, y, niveau, 40)).toBeNull()
  })

  it('retrouve un lien à l\'échelle donnée', () => {
    const liens = [{ noeudDepartId: 1, noeudArriveeId: 2, materiauId: 'BOIS' }]
    const milieu = metresVersPixels(3, 3, niveau, 80)
    expect(lienSous(milieu.x, milieu.y + 2, liens, niveau, 80)).toBe(liens[0])
  })
})

describe('pixelsVersMetres', () => {
  it('est l\'inverse de metresVersPixels', () => {
    const niveau = { largeurZone: 5, hauteurZone: 10 }
    const { x, y } = metresVersPixels(3.5, 7.25, niveau, 64)
    const retour = pixelsVersMetres(x, y, niveau, 64)
    expect(retour.x).toBeCloseTo(3.5)
    expect(retour.y).toBeCloseTo(7.25)
  })
})

describe('cibleDePiece', () => {
  const grille = {
    largeurZone: 5,
    hauteurZone: 10,
    longueurMaxLien: 3.54,
    noeuds: [
      { id: 1, posX: 0, posY: 0 },
      { id: 2, posX: 2.5, posY: 0 },
      { id: 3, posX: 0, posY: 5 },
    ],
  }

  it('aimante sur la paire voisine dont le milieu est le plus proche', () => {
    const milieu = metresVersPixels(1.25, 0, grille, 10, { x: 30, y: 30 })
    expect(cibleDePiece(milieu.x, milieu.y, grille, 10)).toEqual({ departId: 1, arriveeId: 2 })
  })

  it('ignore les paires trop éloignées pour la grille', () => {
    const milieu = metresVersPixels(0, 2.5, grille, 10, { x: 30, y: 30 }) // milieu des nœuds 1 et 3, distance 5 m
    expect(cibleDePiece(milieu.x, milieu.y, grille, 10)).toBeNull()
  })

  it('ne cible rien quand la pièce est loin de la structure', () => {
    expect(cibleDePiece(500, 500, grille, 10)).toBeNull()
  })

  it('départage les deux diagonales d\'un carré selon la position du pointeur', () => {
    const carre = {
      ...grille,
      noeuds: [
        { id: 1, posX: 0, posY: 0 },
        { id: 2, posX: 2.5, posY: 0 },
        { id: 3, posX: 0, posY: 2.5 },
        { id: 4, posX: 2.5, posY: 2.5 },
      ],
    }
    const versBasGauche = metresVersPixels(1, 1, carre, 10, { x: 30, y: 30 }) // sur la diagonale 1-4
    const versBasDroite = metresVersPixels(1.5, 1, carre, 10, { x: 30, y: 30 }) // sur la diagonale 2-3
    expect(cibleDePiece(versBasGauche.x, versBasGauche.y, carre, 10)).toEqual({ departId: 1, arriveeId: 4 })
    expect(cibleDePiece(versBasDroite.x, versBasDroite.y, carre, 10)).toEqual({ departId: 2, arriveeId: 3 })
  })
})

describe('positionDecor', () => {
  it("place le haut de l'herbe à la hauteur demandée", () => {
    const { hauteur, haut } = positionDecor(1376, 500)
    expect(hauteur).toBeCloseTo(1376 * RATIO_DECOR)
    expect(haut + SOL_DECOR * hauteur).toBeCloseTo(500)
  })
})
