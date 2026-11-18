// Règles de construction côté front : elles ne servent qu'au retour immédiat (coût, hauteur).
// Le backend revalide tout à la simulation, c'est lui qui fait foi.

export const cleLien = (a, b) => (a < b ? `${a}-${b}` : `${b}-${a}`)

export function indexerNoeuds(niveau) {
  return Object.fromEntries(niveau.noeuds.map((n) => [n.id, n]))
}

/**
 * Un lien ne relie que des nœuds voisins : longueur ≤ niveau.longueurMaxLien (règle STR-005 du backend, qui fait foi).
 * Sans cette valeur (ancien backend), aucune limite n'est appliquée ici.
 */
export function lienAutorise(departId, arriveeId, niveau) {
  const max = niveau.longueurMaxLien ?? Infinity
  const noeuds = indexerNoeuds(niveau)
  const a = noeuds[departId]
  const b = noeuds[arriveeId]
  const longueur = Math.hypot(a.posX - b.posX, a.posY - b.posY)
  if (longueur <= max) return { ok: true, longueur, message: null }
  const limite = max.toLocaleString('fr-FR', { maximumFractionDigits: 2 })
  return { ok: false, longueur, message: `Lien trop long : ${limite} m maximum entre deux nœuds.` }
}

/**
 * Ajoute un lien ; sur une paire déjà reliée, remplace le matériau ; ignore une boucle.
 * Si `niveau` est fourni, refuse (sans rien changer) un lien trop long.
 */
export function ajouterLien(liens, departId, arriveeId, materiauId, niveau = null) {
  if (departId === arriveeId) return liens
  if (niveau && !lienAutorise(departId, arriveeId, niveau).ok) return liens
  const cle = cleLien(departId, arriveeId)
  if (liens.some((l) => cleLien(l.noeudDepartId, l.noeudArriveeId) === cle)) {
    return liens.map((l) => (cleLien(l.noeudDepartId, l.noeudArriveeId) === cle ? { ...l, materiauId } : l))
  }
  return [...liens, { noeudDepartId: departId, noeudArriveeId: arriveeId, materiauId }]
}

export function supprimerLien(liens, departId, arriveeId) {
  const cle = cleLien(departId, arriveeId)
  return liens.filter((l) => cleLien(l.noeudDepartId, l.noeudArriveeId) !== cle)
}

export function longueurLien(lien, noeuds) {
  const a = noeuds[lien.noeudDepartId]
  const b = noeuds[lien.noeudArriveeId]
  return Math.hypot(a.posX - b.posX, a.posY - b.posY)
}

/** materiaux : { BOIS: { coutUnitaire, ... }, ... } */
export function coutTotal(liens, niveau, materiaux) {
  const noeuds = indexerNoeuds(niveau)
  return liens.reduce((somme, l) => somme + (materiaux[l.materiauId]?.coutUnitaire ?? 0) * longueurLien(l, noeuds), 0)
}

/** Hauteur du nœud le plus haut relié au sol par un chemin de liens (même règle que STR-004). */
export function hauteurAtteinte(liens, niveau) {
  const noeuds = indexerNoeuds(niveau)
  const voisins = {}
  for (const l of liens) {
    ;(voisins[l.noeudDepartId] ??= []).push(l.noeudArriveeId)
    ;(voisins[l.noeudArriveeId] ??= []).push(l.noeudDepartId)
  }
  const vus = new Set(Object.keys(voisins).map(Number).filter((id) => noeuds[id].estAncrageSol))
  const file = [...vus]
  while (file.length > 0) {
    for (const suivant of voisins[file.shift()] ?? []) {
      if (!vus.has(suivant)) {
        vus.add(suivant)
        file.push(suivant)
      }
    }
  }
  return Math.max(0, ...[...vus].map((id) => noeuds[id].posY))
}
