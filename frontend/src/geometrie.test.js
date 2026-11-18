import { describe, expect, it } from 'vitest'
import { dimensionsCanvas, lienSous, metresVersPixels, noeudSous } from './geometrie.js'

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
