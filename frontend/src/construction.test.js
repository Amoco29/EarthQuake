import { describe, expect, it } from 'vitest'
import { ajouterLien, coutTotal, hauteurAtteinte, supprimerLien } from './construction.js'

// Ancrages 1 (0,0) et 2 (5,0) ; nœuds 3 (0,5), 4 (5,5), 5 (0,10) ; 6 (2,2) isolé.
const niveau = {
  noeuds: [
    { id: 1, posX: 0, posY: 0, estAncrageSol: true },
    { id: 2, posX: 5, posY: 0, estAncrageSol: true },
    { id: 3, posX: 0, posY: 5, estAncrageSol: false },
    { id: 4, posX: 5, posY: 5, estAncrageSol: false },
    { id: 5, posX: 0, posY: 10, estAncrageSol: false },
  ],
}
const materiaux = { BOIS: { coutUnitaire: 10 }, ACIER: { coutUnitaire: 40 } }
const lien = (a, b, m = 'BOIS') => ({ noeudDepartId: a, noeudArriveeId: b, materiauId: m })

describe('ajouterLien', () => {
  it('ajoute un lien sans modifier la liste d\'origine', () => {
    const origine = []
    const apres = ajouterLien(origine, 1, 3, 'BOIS')
    expect(apres).toEqual([lien(1, 3)])
    expect(origine).toEqual([])
  })

  it('ignore une boucle', () => {
    expect(ajouterLien([], 3, 3, 'BOIS')).toEqual([])
  })

  it('remplace le matériau d\'une paire déjà reliée, même dans l\'autre sens', () => {
    const apres = ajouterLien([lien(1, 3, 'BOIS')], 3, 1, 'ACIER')
    expect(apres).toEqual([lien(1, 3, 'ACIER')])
  })
})

describe('supprimerLien', () => {
  it('retire le lien quel que soit le sens', () => {
    expect(supprimerLien([lien(1, 3), lien(2, 4)], 3, 1)).toEqual([lien(2, 4)])
  })
})

describe('coutTotal', () => {
  it('somme coût au mètre × longueur', () => {
    // 1-3 : 5 m de bois = 50 ; 3-5 : 5 m d'acier = 200
    expect(coutTotal([lien(1, 3), lien(3, 5, 'ACIER')], niveau, materiaux)).toBeCloseTo(250)
  })

  it('vaut 0 sans lien', () => {
    expect(coutTotal([], niveau, materiaux)).toBe(0)
  })
})

describe('hauteurAtteinte', () => {
  it('suit les liens depuis le sol', () => {
    expect(hauteurAtteinte([lien(1, 3), lien(3, 5)], niveau)).toBe(10)
  })

  it('ignore une pièce non reliée au sol', () => {
    expect(hauteurAtteinte([lien(1, 3), lien(4, 5)], niveau)).toBe(5)
  })

  it('vaut 0 sans lien ou sans ancrage relié', () => {
    expect(hauteurAtteinte([], niveau)).toBe(0)
    expect(hauteurAtteinte([lien(3, 5)], niveau)).toBe(0)
  })
})
