import { describe, expect, it } from 'vitest'
import { dimensionsCanvas, metresVersPixels } from './geometrie.js'

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
