import { describe, expect, it } from 'vitest'
import { carresSansDiagonale, conseilBudget, conseilsEchec, etoiles, lignesResultat, lienLePlusCharge, libelleLien, phraseDuPremierLienCasse, phraseMargeDeSecurite, premiersLiensCasses } from './bilan.js'

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

describe('conseils d\'échec', () => {
  const niveau = {
    longueurMaxLien: 2.5 * Math.SQRT2,
    hauteurMinRequise: 2.5,
    materiauxAutorises: ['BOIS'],
    noeuds: [
      { id: 1, posX: 0, posY: 0, estAncrageSol: true },
      { id: 2, posX: 2.5, posY: 0, estAncrageSol: true },
      { id: 3, posX: 0, posY: 2.5, estAncrageSol: false },
      { id: 4, posX: 2.5, posY: 2.5, estAncrageSol: false },
    ],
  }
  const lien = (a, b) => ({ noeudDepartId: a, noeudArriveeId: b, materiauId: 'BOIS' })
  const carre = [lien(1, 2), lien(3, 4), lien(1, 3), lien(2, 4)]

  it('repère un carré sans diagonale, mais pas avec une diagonale', () => {
    expect(carresSansDiagonale(niveau, carre)).toEqual([[1, 2, 3, 4]])
    expect(carresSansDiagonale(niveau, [...carre, lien(1, 4)])).toEqual([])
  })

  it('conseille une diagonale pour un carré qui s\'effondre', () => {
    const details = carre.map((l) => detail(l.noeudDepartId, l.noeudArriveeId, 99, 0.1))
    const [premier] = conseilsEchec({ niveau, liens: carre, details })
    expect(premier).toMatch(/diagonale dans le carré des nœuds 1, 2, 3, 4/)
  })

  it('conseille un lien en plus quand un lien casse sans carré ouvert', () => {
    const liens2 = [lien(1, 3)]
    const conseils = conseilsEchec({ niveau, liens: liens2, details: [detail(1, 3, 1.2, 0.5)] })
    expect(conseils[0]).toMatch(/ajoute un lien à côté/)
    expect(conseils[1]).toMatch(/n'atteint plus/)
  })

  it('ne dit rien quand rien n\'a cassé', () => {
    expect(conseilsEchec({ niveau, liens: [lien(1, 3)], details: [detail(1, 3, 0.5)] })).toEqual([])
  })

  it('signale un budget dépassé', () => {
    expect(conseilBudget(260, 250)).toMatch(/10/)
    expect(conseilBudget(250, 250)).toBeNull()
  })
})

describe('etoiles', () => {
  it('donne 1 à 3 étoiles selon les seuils 40 et 70', () => {
    expect([0, 39, 40, 69, 70, 100].map(etoiles)).toEqual([1, 1, 2, 2, 3, 3])
  })
})

describe('lignesResultat', () => {
  const niveau = {
    longueurMaxLien: 2.5 * Math.SQRT2,
    hauteurMinRequise: 2.5,
    materiauxAutorises: ['BOIS'],
    noeuds: [
      { id: 1, posX: 0, posY: 0, estAncrageSol: true },
      { id: 3, posX: 0, posY: 2.5, estAncrageSol: false },
    ],
  }
  const lienBois = [{ noeudDepartId: 1, noeudArriveeId: 3, materiauId: 'BOIS' }]

  it('donne deux lignes en cas de victoire', () => {
    const resultat = { estValide: true, detailsLiens: [detail(1, 4, 0.5)], messageBilan: '' }
    const lignes = lignesResultat({ resultat, niveau, liens, materiaux, suivantExiste: true })
    expect(lignes).toHaveLength(2)
    expect(lignes[1]).toMatch(/suivant/)
  })

  it('désigne le lien cassé puis donne un conseil en cas d\'échec', () => {
    const resultat = { estValide: false, detailsLiens: [detail(1, 3, 1.4, 3)], messageBilan: 'raté' }
    const lignes = lignesResultat({ resultat, niveau, liens: lienBois, materiaux, suivantExiste: true })
    expect(lignes[0]).toMatch(/1–3/)
    expect(lignes[1]).toMatch(/lâché/)
  })

  it('retombe sur le message du serveur sans lien cassé', () => {
    const resultat = { estValide: false, detailsLiens: [detail(1, 3, 0.5)], messageBilan: 'Trop bas' }
    expect(lignesResultat({ resultat, niveau, liens: lienBois, materiaux, suivantExiste: false })[0]).toBe('Trop bas')
  })
})
