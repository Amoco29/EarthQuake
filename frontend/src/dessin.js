import { couleurStress, DUREE_ANIMATION, ECHELLE_TEMPS, etatRupture, secousse, tauxAffiche } from './animation.js'
import { cleLien, indexerNoeuds, noeudsCompatibles } from './construction.js'
import { texturer } from './dessinMateriaux.js'
import { dessinerFelix, positionFelix } from './dessinFelix.js'
import { dessinerPoussiere } from './dessinPoussiere.js'
import { dessinerDalle, dessinerEchafaudage, dessinerNoeud, dessinerRegle } from './dessinZone.js'
import { metresVersPixels } from './geometrie.js'

export const POLICE = '16px system-ui, sans-serif'
const largeurLien = (echelle) => Math.min(10, Math.max(5, echelle * 0.12))
const rayonNoeud = (echelle) => Math.min(11, Math.max(6, echelle * 0.13))

function pixels(noeud, niveau, echelle, origine) {
  return metresVersPixels(noeud.posX, noeud.posY, niveau, echelle, origine)
}

/** Un lien = un rectangle de la longueur entre les nœuds, pivoté selon l'angle. */
function dessinerRectangle(ctx, a, b, de, vers, couleur, largeur, materiauId, teinte) {
  const dx = b.x - a.x
  const dy = b.y - a.y
  ctx.save()
  ctx.translate(a.x + dx * de, a.y + dy * de)
  ctx.rotate(Math.atan2(dy, dx))
  ctx.fillStyle = couleur
  const longueur = Math.hypot(dx, dy) * (vers - de)
  ctx.fillRect(0, -largeur / 2, longueur, largeur)
  texturer(ctx, materiauId, longueur, largeur)
  if (teinte) {
    ctx.globalAlpha *= 0.5 // la texture reste visible sous la teinte de stress
    ctx.fillStyle = teinte
    ctx.fillRect(0, -largeur / 2, longueur, largeur)
  }
  ctx.restore()
}

/**
 * Un lien intact est un rectangle. À sa rupture il se casse en deux : chaque moitié pivote autour de son nœud
 * et tombe (dy en mètres, angle en radians), puis s'efface (alpha).
 */
function dessinerLien(ctx, a, b, couleur, largeur, echelle, etat = { dy: 0, angle: 0, alpha: 1 }, materiauId, teinte) {
  if (etat.alpha <= 0) return
  ctx.save()
  ctx.globalAlpha = etat.alpha
  if (!etat.casse) {
    dessinerRectangle(ctx, a, b, 0, 1, couleur, largeur, materiauId, teinte)
  } else {
    const sens = b.x >= a.x ? 1 : -1 // fait pencher le bout libre vers le bas, quel que soit le sens du lien
    for (const [pivot, de, vers, signe] of [[a, 0, 0.47, sens], [b, 0.53, 1, -sens]]) {
      ctx.save()
      ctx.translate(pivot.x, pivot.y + etat.dy * echelle * 0.7)
      ctx.rotate(signe * etat.angle * 3)
      ctx.translate(-pivot.x, -pivot.y)
      dessinerRectangle(ctx, a, b, de, vers, couleur, largeur, materiauId, teinte)
      ctx.restore()
    }
  }
  ctx.restore()
}

/**
 * scene : { niveau, materiaux, liens, drag, resultat, temps }
 * `drag` : pièce tenue par le joueur { materiauId, x, y, cible: { departId, arriveeId, ok } | null }
 * `resultat` + `temps` : mode séisme (couleurs de stress, secousse, liens cassés).
 */
export function dessinerScene(ctx, { niveau, echelle, origine, espace, materiaux, liens, drag, resultat, temps, surlignes = [], etatFelix = 'inquiet' }) {
  ctx.clearRect(0, 0, espace.largeur, espace.hauteur)

  ctx.save()
  if (resultat) {
    const { dx, dy } = secousse(temps)
    ctx.translate(dx, dy)
  }

  dessinerDalle(ctx, niveau, echelle, origine)
  dessinerEchafaudage(ctx, niveau, echelle, origine)
  dessinerRegle(ctx, niveau, echelle, origine)

  // Liens
  const noeuds = indexerNoeuds(niveau)
  const details = Object.fromEntries((resultat?.detailsLiens ?? []).map((d) => [cleLien(d.noeudDepartId, d.noeudArriveeId), d]))
  for (const lien of liens) {
    const a = pixels(noeuds[lien.noeudDepartId], niveau, echelle, origine)
    const b = pixels(noeuds[lien.noeudArriveeId], niveau, echelle, origine)
    const detail = details[cleLien(lien.noeudDepartId, lien.noeudArriveeId)]
    if (detail) {
      const etat = etatRupture(detail, temps)
      dessinerLien(ctx, a, b, materiaux[lien.materiauId]?.couleurHex ?? '#000', largeurLien(echelle), echelle, etat, lien.materiauId, couleurStress(etat.casse ? 1 : tauxAffiche(detail, temps)))
    } else {
      dessinerLien(ctx, a, b, materiaux[lien.materiauId]?.couleurHex ?? '#000', largeurLien(echelle), echelle, undefined, lien.materiauId)
    }
  }

  // Nœuds : plaque et boulon, platine pour un ancrage au sol
  for (const noeud of niveau.noeuds) {
    const { x, y } = pixels(noeud, niveau, echelle, origine)
    dessinerNoeud(ctx, x, y, rayonNoeud(echelle), noeud.estAncrageSol)
  }

  // Pièce en main : un anneau vert sur les nœuds où elle peut se poser
  if (drag) {
    ctx.strokeStyle = 'rgba(26, 156, 58, 0.7)'
    ctx.lineWidth = 2
    for (const id of noeudsCompatibles(niveau, materiaux[drag.materiauId])) {
      const { x, y } = pixels(noeuds[id], niveau, echelle, origine)
      ctx.beginPath()
      ctx.arc(x, y, rayonNoeud(echelle) + 5, 0, Math.PI * 2)
      ctx.stroke()
    }
  }

  if (resultat) {
    const sol = metresVersPixels(niveau.largeurZone / 2, 0, niveau, echelle, origine)
    const ruptures = resultat.detailsLiens
      .filter((d) => d.tempsRupture != null)
      .map((d) => {
        const p = pixels(noeuds[d.noeudDepartId], niveau, echelle, origine)
        const q = pixels(noeuds[d.noeudArriveeId], niveau, echelle, origine)
        return { x: (p.x + q.x) / 2, y: (p.y + q.y) / 2, temps: d.tempsRupture * ECHELLE_TEMPS }
      })
    dessinerPoussiere(ctx, { temps, duree: DUREE_ANIMATION, echelle, sol: { ...sol, largeur: (niveau.largeurZone + 1.2) * echelle }, ruptures })
  }
  dessinerFelix(ctx, positionFelix(niveau, echelle, origine, espace), etatFelix, resultat ? temps : 0)
  if (drag) dessinerPiece(ctx, drag, noeuds, materiaux, niveau, echelle, origine)
  dessinerPointRupture(ctx, surlignes, noeuds, niveau, echelle, origine, temps)
  ctx.restore()
}

/**
 * Pièce en cours de glisser : longueur fixe du matériau, avec un nœud virtuel à chaque bout.
 * Sans cible elle suit le pointeur ; avec une cible elle s'aligne sur les deux nœuds (verts si la pose est permise, rouges sinon).
 */
function dessinerPiece(ctx, drag, noeuds, materiaux, niveau, echelle, origine) {
  const materiau = materiaux[drag.materiauId]
  const demi = ((materiau?.longueurMax ?? 2.5) * echelle) / 2
  const { cible } = drag
  const a = cible ? pixels(noeuds[cible.departId], niveau, echelle, origine) : { x: drag.x - demi, y: drag.y }
  const b = cible ? pixels(noeuds[cible.arriveeId], niveau, echelle, origine) : { x: drag.x + demi, y: drag.y }
  ctx.save()
  ctx.globalAlpha = 0.85
  dessinerRectangle(ctx, a, b, 0, 1, materiau?.couleurHex ?? '#000', largeurLien(echelle), drag.materiauId)
  ctx.globalAlpha = 1
  ctx.strokeStyle = !cible ? '#333' : cible.ok ? '#1a9c3a' : '#e60000'
  ctx.lineWidth = 3
  for (const p of [a, b]) {
    ctx.beginPath()
    ctx.arc(p.x, p.y, rayonNoeud(echelle) + 3, 0, Math.PI * 2)
    ctx.fillStyle = 'rgba(255,255,255,0.7)'
    ctx.fill()
    ctx.stroke()
  }
  ctx.restore()
}

/** Point de rupture : un cercle rouge qui pulse autour du milieu de chaque lien cassé en premier. */
function dessinerPointRupture(ctx, surlignes, noeuds, niveau, echelle, origine, temps) {
  ctx.save()
  const pulse = 0.5 + 0.5 * Math.sin(temps * Math.PI * 4)
  for (const s of surlignes) {
    const a = pixels(noeuds[s.noeudDepartId], niveau, echelle, origine)
    const b = pixels(noeuds[s.noeudArriveeId], niveau, echelle, origine)
    const rayon = rayonNoeud(echelle) * 2 + 4 * pulse
    ctx.beginPath()
    ctx.arc((a.x + b.x) / 2, (a.y + b.y) / 2, rayon, 0, Math.PI * 2)
    ctx.strokeStyle = '#fff'
    ctx.lineWidth = 7
    ctx.stroke()
    ctx.strokeStyle = '#d50000'
    ctx.lineWidth = 4
    ctx.stroke()
  }
  ctx.restore()
}
