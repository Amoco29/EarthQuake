// Textes et calculs du bilan, à partir des détails renvoyés par POST /simuler (le contrat ne change pas).
import { cleLien, hauteurAtteinte, indexerNoeuds } from './construction.js'

/** Taux affiché par le backend quand une partie instable s'effondre (CalculateurStress.TAUX_EFFONDREMENT). */
export const TAUX_EFFONDREMENT = 99

export const EXPLICATION_SOLIDITE =
  'Solidité : 100 moins la charge moyenne des liens. 100 = structure presque au repos, 0 = tous les liens au bord de la rupture. Un niveau perdu vaut 0.'

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

/** Carrés de la grille dont les 4 côtés sont posés mais sans aucune diagonale : ils se déforment. Renvoie les ids des 4 coins. */
export function carresSansDiagonale(niveau, liens) {
  const noeuds = indexerNoeuds(niveau)
  const pas = niveau.longueurMaxLien ? niveau.longueurMaxLien / Math.SQRT2 : 2.5
  const idEn = (x, y) => niveau.noeuds.find((n) => Math.abs(n.posX - x) < 1e-6 && Math.abs(n.posY - y) < 1e-6)?.id
  const poses = new Set(liens.map((l) => cleLien(l.noeudDepartId, l.noeudArriveeId)))
  const relies = (a, b) => poses.has(cleLien(a, b))
  return Object.values(noeuds).flatMap(({ posX: x, posY: y }) => {
    const [bg, bd, hg, hd] = [idEn(x, y), idEn(x + pas, y), idEn(x, y + pas), idEn(x + pas, y + pas)]
    if ([bg, bd, hg, hd].includes(undefined)) return []
    const cotes = relies(bg, bd) && relies(hg, hd) && relies(bg, hg) && relies(bd, hd)
    return cotes && !relies(bg, hd) && !relies(bd, hg) ? [[bg, bd, hg, hd]] : []
  })
}

/** Conseils après un niveau raté, pour tous les niveaux (liste vide si rien de précis à dire). */
export function conseilsEchec({ niveau, liens, details }) {
  const conseils = []
  const casses = details.filter((d) => d.estCasse)
  const carres = carresSansDiagonale(niveau, liens)
  if (carres.length > 0) {
    conseils.push(`Un carré sans diagonale se déforme : ajoute une diagonale dans le carré des nœuds ${carres[0].join(', ')}.`)
  }
  if (casses.length > 0 && carres.length === 0) {
    const plusSolide = niveau.materiauxAutorises.length > 1
    conseils.push(
      plusSolide
        ? 'Un lien a lâché : remplace-le par un matériau plus solide (le plus cher résiste le mieux) ou ajoute un lien à côté pour partager l\'effort.'
        : 'Un lien a lâché : ajoute un lien à côté ou une diagonale pour partager l\'effort.',
    )
  }
  const intacts = liens.filter((l) => !casses.some((d) => cleLien(d.noeudDepartId, d.noeudArriveeId) === cleLien(l.noeudDepartId, l.noeudArriveeId)))
  if (casses.length > 0 && hauteurAtteinte(intacts, niveau) < niveau.hauteurMinRequise) {
    conseils.push(`Après la rupture, la structure n'atteint plus les ${niveau.hauteurMinRequise} m demandés : il faut que les liens intacts montent jusque-là.`)
  }
  return conseils
}

/** Conseil quand la structure dépasse le budget (erreur BUD-001 : pas de simulation). */
export function conseilBudget(cout, budgetMax) {
  if (cout <= budgetMax) return null
  return `Tu dépasses le budget de ${Math.ceil(cout - budgetMax)} : retire un lien ou choisis un matériau moins cher.`
}
