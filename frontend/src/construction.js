// Règles de construction côté front : elles ne servent qu'au retour immédiat (coût, hauteur).
// Le backend revalide tout à la simulation, c'est lui qui fait foi.

export const cleLien = (a, b) => (a < b ? `${a}-${b}` : `${b}-${a}`)

export function indexerNoeuds(niveau) {
  return Object.fromEntries(niveau.noeuds.map((n) => [n.id, n]))
}

const formater = (metres) => metres.toLocaleString('fr-FR', { maximumFractionDigits: 2 })

/** Plus longue longueur permise : celle de la grille (STR-005) et celle du matériau (MAT-003), la plus courte des deux. */
export function limiteLien(niveau, materiau = null) {
  return Math.min(niveau.longueurMaxLien ?? Infinity, materiau?.longueurMax ?? Infinity)
}

/**
 * Un lien ne relie que des nœuds voisins (STR-005) et ne dépasse pas la longueur de son matériau (MAT-003).
 * Le backend fait foi : sans ces valeurs (ancien backend), aucune limite n'est appliquée ici.
 */
export function lienAutorise(departId, arriveeId, niveau, materiau = null) {
  const noeuds = indexerNoeuds(niveau)
  const a = noeuds[departId]
  const b = noeuds[arriveeId]
  const longueur = Math.hypot(a.posX - b.posX, a.posY - b.posY)
  const maxGrille = niveau.longueurMaxLien ?? Infinity
  if (longueur > maxGrille) {
    return { ok: false, longueur, message: `Lien trop long : ${formater(maxGrille)} m maximum entre deux nœuds.` }
  }
  const maxMateriau = materiau?.longueurMax ?? Infinity
  if (longueur > maxMateriau + 1e-9) {
    return { ok: false, longueur, message: `${materiau.nom} : ${formater(maxMateriau)} m maximum par lien.` }
  }
  return { ok: true, longueur, message: null }
}

/** Ids des nœuds qui ont au moins un voisin relié par un lien autorisé avec ce matériau (où l'on peut poser une extrémité). */
export function noeudsCompatibles(niveau, materiau = null) {
  const ids = new Set()
  for (const a of niveau.noeuds) {
    for (const b of niveau.noeuds) {
      if (a.id < b.id && lienAutorise(a.id, b.id, niveau, materiau).ok) {
        ids.add(a.id)
        ids.add(b.id)
      }
    }
  }
  return ids
}

/**
 * Ajoute un lien ; sur une paire déjà reliée, remplace le matériau ; ignore une boucle.
 * Si `niveau` est fourni, refuse (sans rien changer) un lien trop long pour la grille ou pour `materiau`.
 */
export function ajouterLien(liens, departId, arriveeId, materiauId, niveau = null, materiau = null) {
  if (departId === arriveeId) return liens
  if (niveau && !lienAutorise(departId, arriveeId, niveau, materiau).ok) return liens
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
