import { describe, expect, it } from 'vitest'
import { animationTerminee, couleurStress, DUREE_ANIMATION, ECHELLE_TEMPS, estCasseA, MONTEE, secousse, tauxAffiche } from './animation.js'

describe('couleurStress', () => {
  it('va du vert au rouge', () => {
    expect(couleurStress(0)).toBe('hsl(120, 80%, 42%)')
    expect(couleurStress(0.5)).toBe('hsl(60, 80%, 42%)')
    expect(couleurStress(1)).toBe('hsl(0, 80%, 42%)')
  })

  it('reste rouge au-delà de 1 et vert en dessous de 0', () => {
    expect(couleurStress(3)).toBe(couleurStress(1))
    expect(couleurStress(-1)).toBe(couleurStress(0))
  })
})

describe('tauxAffiche', () => {
  it('monte progressivement jusqu\'au taux final', () => {
    const detail = { tauxStress: 0.8 }
    expect(tauxAffiche(detail, 0)).toBe(0)
    expect(tauxAffiche(detail, MONTEE / 2)).toBeCloseTo(0.4)
    expect(tauxAffiche(detail, MONTEE * 3)).toBeCloseTo(0.8)
  })
})

describe('estCasseA', () => {
  it('ne casse jamais un lien sans temps de rupture', () => {
    expect(estCasseA({ tauxStress: 0.5, tempsRupture: null }, 100)).toBe(false)
  })

  it('casse au temps de rupture ramené à l\'échelle de l\'animation', () => {
    const detail = { tauxStress: 2, tempsRupture: 5 }
    expect(estCasseA(detail, 5 * ECHELLE_TEMPS - 0.01)).toBe(false)
    expect(estCasseA(detail, 5 * ECHELLE_TEMPS)).toBe(true)
  })
})

describe('secousse', () => {
  it('bouge pendant l\'animation et s\'arrête à la fin', () => {
    const { dx, dy } = secousse(0.3)
    expect(Math.abs(dx) + Math.abs(dy)).toBeGreaterThan(0)
    expect(secousse(DUREE_ANIMATION)).toEqual({ dx: 0, dy: 0 })
  })

  it('s\'amortit : l\'amplitude diminue avec le temps', () => {
    const max = (t0) => Math.max(...Array.from({ length: 200 }, (_, i) => Math.abs(secousse(t0 + i * 0.001).dx)))
    expect(max(6)).toBeLessThan(max(0.5))
  })
})

describe('animationTerminee', () => {
  it('est vraie à la durée prévue', () => {
    expect(animationTerminee(DUREE_ANIMATION - 0.1)).toBe(false)
    expect(animationTerminee(DUREE_ANIMATION)).toBe(true)
  })
})
