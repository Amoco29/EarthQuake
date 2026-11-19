import { describe, expect, it } from 'vitest'
import { avancer, estTermine, etapesTuto, etatInitial } from './tutoriel.js'

describe('avancer', () => {
  it('passe à l\'étape suivante quand l\'événement attendu arrive', () => {
    expect(avancer(etatInitial, 'materiauChoisi')).toEqual({ index: 1, compte: 0 })
  })

  it('ignore un événement qui ne concerne pas l\'étape courante', () => {
    expect(avancer(etatInitial, 'lienPose')).toBe(etatInitial)
  })

  it('cumule les occurrences quand l\'étape en demande plusieurs', () => {
    const iDiagonales = etapesTuto.findIndex((e) => e.id === 'diagonales')
    let etat = { index: iDiagonales, compte: 0 }
    for (let i = 0; i < 3; i++) etat = avancer(etat, 'lienPose')
    expect(etat).toEqual({ index: iDiagonales, compte: 3 })
    expect(avancer(etat, 'lienPose')).toEqual({ index: iDiagonales + 1, compte: 0 })
  })

  it('parcourt les six étapes dans l\'ordre puis se termine', () => {
    const evenements = ['materiauChoisi', 'lienPose', 'hauteurAtteinte', 'lienPose', 'lienPose', 'lienPose', 'lienPose', 'suivant', 'simulationLancee']
    const etat = evenements.reduce(avancer, etatInitial)
    expect(estTermine(etat)).toBe(true)
    expect(avancer(etat, 'suivant')).toBe(etat)
  })
})
