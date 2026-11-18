import { describe, expect, it } from 'vitest'
import { animationTerminee, CHUTE, couleurStress, DUREE_ANIMATION, ECHELLE_TEMPS, etatRupture, FONDU, MONTEE, secousse, tauxAffiche } from './animation.js'

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

describe('etatRupture', () => {
  const lien = { tauxStress: 2, tempsRupture: 5 }
  const tCasse = 5 * ECHELLE_TEMPS

  it('laisse intact un lien sans temps de rupture', () => {
    expect(etatRupture({ tauxStress: 0.5, tempsRupture: null }, 100)).toEqual({ casse: false, dy: 0, angle: 0, alpha: 1 })
  })

  it('laisse le lien intact avant sa rupture', () => {
    expect(etatRupture(lien, tCasse - 0.01)).toEqual({ casse: false, dy: 0, angle: 0, alpha: 1 })
  })

  it('le fait tomber en tournant, encore opaque, juste après la rupture', () => {
    const etat = etatRupture(lien, tCasse + CHUTE / 2)
    expect(etat.casse).toBe(true)
    expect(etat.dy).toBeGreaterThan(0)
    expect(etat.angle).toBeGreaterThan(0)
    expect(etat.alpha).toBe(1)
  })

  it('l\'efface ensuite en fondu, puis il a disparu', () => {
    const enFondu = etatRupture(lien, tCasse + CHUTE + FONDU / 2)
    expect(enFondu.alpha).toBeCloseTo(0.5)
    expect(etatRupture(lien, tCasse + CHUTE + FONDU + 1).alpha).toBe(0)
  })

  it('tombe de plus en plus bas sans jamais reculer', () => {
    const dys = [0.1, 0.3, 0.6, 0.9, 5].map((d) => etatRupture(lien, tCasse + d).dy)
    expect(dys).toEqual([...dys].sort((a, b) => a - b))
    expect(dys.at(-1)).toBe(1)
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
