import { describe, expect, it } from 'vitest'
import { lienLePlusCharge, libelleLien, phraseDuPremierLienCasse, phraseMargeDeSecurite, premiersLiensCasses } from './bilan.js'

const detail = (a, b, tauxStress, tempsRupture = null) => ({
  noeudDepartId: a,
  noeudArriveeId: b,
  tauxStress,
  estCasse: tempsRupture !== null,
  tempsRupture,
})
const liens = [
  { noeudDepartId: 4, noeudArriveeId: 9, materiauId: 'BOIS' },
  { noeudDepartId: 1, noeudArriveeId: 4, materiauId: 'BETON' },
]
const materiaux = { BOIS: { nom: 'Bois' }, BETON: { nom: 'Béton' } }

describe('premiersLiensCasses', () => {
  it('ne renvoie rien quand tout tient', () => {
    expect(premiersLiensCasses([detail(1, 4, 0.5)])).toEqual([])
  })

  it('renvoie le lien au plus petit temps de rupture', () => {
    const premiers = premiersLiensCasses([detail(1, 4, 1.2, 8), detail(4, 9, 2, 5), detail(2, 5, 0.3)])
    expect(premiers.map((d) => d.noeudArriveeId)).toEqual([9])
  })

  it('garde tous les ex æquo', () => {
    expect(premiersLiensCasses([detail(1, 4, 99, 0.1), detail(4, 9, 99, 0.1), detail(2, 5, 1.2, 8)])).toHaveLength(2)
  })
})

describe('phrases', () => {
  it('nomme le premier lien cassé avec son matériau et son taux', () => {
    const phrase = phraseDuPremierLienCasse([detail(4, 9, 1.18, 8.4)], liens, materiaux)
    expect(phrase).toBe('Premier lien cassé : 4–9 (Bois), chargé à 118 % de sa résistance.')
  })

  it('parle de zone fragile en cas d\'effondrement', () => {
    const phrase = phraseDuPremierLienCasse([detail(4, 9, 99, 0.1), detail(1, 4, 99, 0.1)], liens, materiaux)
    expect(phrase).toContain('Zone fragile')
    expect(phrase).toContain('2 lien(s)')
  })

  it('ne dit rien quand rien n\'a cassé', () => {
    expect(phraseDuPremierLienCasse([detail(4, 9, 0.4)], liens, materiaux)).toBeNull()
  })

  it('libelle un lien sans matériau connu', () => {
    expect(libelleLien(detail(7, 8, 0.1), liens, materiaux)).toBe('7–8')
  })
})

describe('marge de sécurité', () => {
  it('donne la charge du lien le plus sollicité', () => {
    const details = [detail(1, 4, 0.23), detail(4, 9, 0.59)]
    expect(lienLePlusCharge(details).noeudArriveeId).toBe(9)
    expect(phraseMargeDeSecurite(details)).toBe('Lien le plus chargé : 59 % de sa résistance.')
  })

  it('n\'invente pas de marge pour un effondrement', () => {
    expect(phraseMargeDeSecurite([detail(1, 4, 99, 0.1)])).toBeNull()
  })

  it('renvoie null sans lien', () => {
    expect(lienLePlusCharge([])).toBeNull()
  })
})
