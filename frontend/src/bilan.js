// Textes et calculs du bilan, à partir des détails renvoyés par POST /simuler (le contrat ne change pas).
import { cleLien } from './construction.js'

/** Taux affiché par le backend quand une partie instable s'effondre (CalculateurStress.TAUX_EFFONDREMENT). */
export const TAUX_EFFONDREMENT = 99

export const EXPLICATION_SOLIDITE =
  'Solidité : 100 moins la charge moyenne des liens. 100 = structure presque au repos, 0 = tous les liens au bord de la rupture.'

/** Les liens qui ont cassé en premier (plus petit temps de rupture ; tous les ex æquo en cas d'effondrement). */
export function premiersLiensCasses(details) {
  const casses = details.filter((d) => d.estCasse && d.tempsRupture != null)
  if (casses.length === 0) return []
  const premier = Math.min(...casses.map((d) => d.tempsRupture))
  return casses.filter((d) => d.tempsRupture - premier < 1e-6)
}

export function lienLePlusCharge(details) {
  return details.reduce((pire, d) => (pire === null || d.tauxStress > pire.tauxStress ? d : pire), null)
}

export function libelleLien(detail, liens, materiaux) {
  const lien = liens.find((l) => cleLien(l.noeudDepartId, l.noeudArriveeId) === cleLien(detail.noeudDepartId, detail.noeudArriveeId))
  const nom = lien ? materiaux[lien.materiauId]?.nom : null
  return `${detail.noeudDepartId}–${detail.noeudArriveeId}${nom ? ` (${nom})` : ''}`
}

const pourcent = (taux) => `${Math.round(taux * 100)} %`

/** Phrase qui désigne le lien (ou la zone) qui a lâché en premier, ou null si rien n'a cassé. */
export function phraseDuPremierLienCasse(details, liens, materiaux) {
  const premiers = premiersLiensCasses(details)
  if (premiers.length === 0) return null
  if (premiers.some((d) => d.tauxStress >= TAUX_EFFONDREMENT)) {
    return `Zone fragile : ${premiers.length} lien(s) se sont effondrés en premier, la structure est trop souple à cet endroit.`
  }
  if (premiers.length > 1) {
    return `Zone fragile : ${premiers.length} liens ont cédé en même temps (${premiers.map((d) => libelleLien(d, liens, materiaux)).join(', ')}).`
  }
  const [d] = premiers
  return `Premier lien cassé : ${libelleLien(d, liens, materiaux)}, chargé à ${pourcent(d.tauxStress)} de sa résistance.`
}

/** Marge de sécurité : la charge du lien le plus sollicité, en % de sa résistance (null si effondrement). */
export function phraseMargeDeSecurite(details) {
  const pire = lienLePlusCharge(details)
  if (pire === null || pire.tauxStress >= TAUX_EFFONDREMENT) return null
  return `Lien le plus chargé : ${pourcent(pire.tauxStress)} de sa résistance.`
}
