import { describe, expect, it } from 'vitest'
import { calculerEchelle, dimensionsCanvas, ECHELLE_MAX, lienSous, metresVersPixels, noeudSous, pixelsVersMetres } from './geometrie.js'

const niveau = { largeurZone: 5, hauteurZone: 10 }

describe('metresVersPixels', () => {
  it('place le sol (Y = 0) en bas de la zone', () => {
    expect(metresVersPixels(0, 0, niveau, 10, 5)).toEqual({ x: 5, y: 105 })
  })

  it('place le haut de la zone (Y = hauteur) en haut, sous la marge', () => {
    expect(metresVersPixels(5, 10, niveau, 10, 5)).toEqual({ x: 55, y: 5 })
  })

  it('inverse bien l\'axe Y : plus haut en mètres = plus petit en pixels', () => {
    const bas = metresVersPixels(2.5, 2.5, niveau, 10, 5)
    const haut = metresVersPixels(2.5, 5, niveau, 10, 5)
    expect(haut.y).toBeLessThan(bas.y)
    expect(haut.x).toBe(bas.x)
  })
})

describe('dimensionsCanvas', () => {
  it('englobe la zone et ses deux marges', () => {
    expect(dimensionsCanvas(niveau, 10, 5)).toEqual({ largeur: 60, hauteur: 110 })
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

describe('calculerEchelle', () => {
  const niveau = { largeurZone: 5, hauteurZone: 10 }

  it('ne dépasse jamais l\'échelle maximale', () => {
    expect(calculerEchelle(niveau, { largeur: 5000, hauteur: 5000 })).toBe(ECHELLE_MAX)
  })

  it('se règle sur la hauteur disponible quand la fenêtre est basse', () => {
    // (700 - 2 x 30) / 10 = 64
    expect(calculerEchelle(niveau, { largeur: 5000, hauteur: 700 })).toBe(64)
  })

  it('se règle sur la largeur disponible quand la fenêtre est étroite', () => {
    // (230 - 60) / 5 = 34
    expect(calculerEchelle(niveau, { largeur: 230, hauteur: 5000 })).toBe(34)
  })

  it('reste positive même sans espace mesuré', () => {
    expect(calculerEchelle(niveau, { largeur: 0, hauteur: 0 })).toBe(1)
  })

  it('donne un canvas qui tient dans la hauteur disponible', () => {
    const echelle = calculerEchelle(niveau, { largeur: 5000, hauteur: 500 })
    expect(dimensionsCanvas(niveau, echelle).hauteur).toBeLessThanOrEqual(500)
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
