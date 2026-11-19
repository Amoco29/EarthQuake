import { describe, expect, it } from 'vitest'
import { estTutoriel, niveauOuvert } from './progression.js'

const niveaux = [
  { id: 0, numero: 0 },
  { id: 1, numero: 1 },
  { id: 2, numero: 2 },
]

describe('niveauOuvert', () => {
  it('ouvre le tutoriel et le niveau 1 dès le départ', () => {
    expect(niveauOuvert(niveaux, 0, {})).toBe(true)
    expect(niveauOuvert(niveaux, 1, {})).toBe(true)
  })

  it('garde le niveau 2 fermé tant que le niveau 1 n\'est pas réussi', () => {
    expect(niveauOuvert(niveaux, 2, {})).toBe(false)
    expect(niveauOuvert(niveaux, 2, { 0: 90 })).toBe(false)
    expect(niveauOuvert(niveaux, 2, { 1: 80 })).toBe(true)
  })

  it('reconnaît le tutoriel à son numéro', () => {
    expect(estTutoriel(niveaux[0])).toBe(true)
    expect(estTutoriel(niveaux[1])).toBe(false)
  })
})
