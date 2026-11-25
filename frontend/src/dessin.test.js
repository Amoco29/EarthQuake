import { describe, expect, it } from 'vitest'
import { dessinerScene } from './dessin.js'
import { calculerVue } from './geometrie.js'

// Faux contexte 2D : accepte tout appel et compte les tracés, pour vérifier que la scène se dessine sans erreur.
function faussContexte() {
  const appels = { fill: 0, stroke: 0 }
  const ctx = new Proxy(
    {},
    {
      get: (_, nom) => {
        if (nom === 'fill' || nom === 'stroke') return () => appels[nom]++
        if (nom === 'createLinearGradient' || nom === 'createRadialGradient') return () => ({ addColorStop() {} })
        return () => {}
      },
      set: () => true,
    },
  )
  return { ctx, appels }
}

const niveau = {
  largeurZone: 5,
  hauteurZone: 10,
  hauteurMinRequise: 5,
  noeuds: [
    { id: 1, posX: 0, posY: 0, estAncrageSol: true },
    { id: 2, posX: 2.5, posY: 0, estAncrageSol: true },
    { id: 3, posX: 0, posY: 2.5, estAncrageSol: false },
  ],
}
const materiaux = { BOIS: { couleurHex: '#a0642d', longueurMax: 3.54 }, ACIER: { couleurHex: '#888', longueurMax: 3.54 } }
const liens = [
  { noeudDepartId: 1, noeudArriveeId: 3, materiauId: 'BOIS' },
  { noeudDepartId: 2, noeudArriveeId: 3, materiauId: 'ACIER' },
]
const espace = { largeur: 1280, hauteur: 720 }
const { echelle, origine } = calculerVue(niveau, espace)
const base = { niveau, echelle, origine, espace, materiaux, liens }

describe('dessinerScene', () => {
  it('dessine la construction pour chaque état de Félix', () => {
    for (const etatFelix of ['inquiet', 'content', 'decourage']) {
      const { ctx, appels } = faussContexte()
      dessinerScene(ctx, { ...base, temps: 0, etatFelix })
      expect(appels.fill).toBeGreaterThan(0)
    }
  })

  it('dessine le séisme, un lien cassé en deux et la poussière à chaque instant', () => {
    const resultat = {
      detailsLiens: [
        { noeudDepartId: 1, noeudArriveeId: 3, tauxStress: 1.4, estCasse: true, tempsRupture: 2 },
        { noeudDepartId: 2, noeudArriveeId: 3, tauxStress: 0.4, estCasse: false, tempsRupture: null },
      ],
    }
    for (const temps of [0, 1, 2.5, 3.5, 5, 8, 9]) {
      const { ctx } = faussContexte()
      expect(() => dessinerScene(ctx, { ...base, resultat, temps, surlignes: [resultat.detailsLiens[0]] })).not.toThrow()
    }
  })
})
