import { couleurStress, estCasseA, secousse, tauxAffiche } from './animation.js'
import { cleLien, indexerNoeuds } from './construction.js'
import { dimensionsCanvas, metresVersPixels } from './geometrie.js'

export const POLICE = "12px 'Pokemon', 'Press Start 2P', monospace"
const largeurLien = (echelle) => Math.min(10, Math.max(5, echelle * 0.12))
const rayonNoeud = (echelle) => Math.min(11, Math.max(6, echelle * 0.13))

function pixels(noeud, niveau, echelle) {
  return metresVersPixels(noeud.posX, noeud.posY, niveau, echelle)
}

/** Un lien = un rectangle de la longueur entre les nœuds, pivoté selon l'angle. */
function dessinerRectangle(ctx, a, b, de, vers, couleur, largeur) {
  const dx = b.x - a.x
  const dy = b.y - a.y
  ctx.save()
  ctx.translate(a.x + dx * de, a.y + dy * de)
  ctx.rotate(Math.atan2(dy, dx))
  ctx.fillStyle = couleur
  ctx.fillRect(0, -largeur / 2, Math.hypot(dx, dy) * (vers - de), largeur)
  ctx.restore()
}

function dessinerLien(ctx, a, b, couleur, casse, largeur) {
  if (!casse) {
    dessinerRectangle(ctx, a, b, 0, 1, couleur, largeur)
    return
  }
  // Lien cassé : deux tronçons de chaque côté, avec un trou au milieu ; le second tronçon retombe un peu.
  const decale = (p) => ({ x: p.x, y: p.y + 10 })
  dessinerRectangle(ctx, a, b, 0, 0.35, couleur, largeur)
  dessinerRectangle(ctx, decale(a), decale(b), 0.65, 1, couleur, largeur)
}

/**
 * scene : { niveau, materiaux, liens, drag, materiauChoisi, resultat, temps }
 * `resultat` + `temps` : mode séisme (couleurs de stress, secousse, liens cassés).
 */
export function dessinerScene(ctx, { niveau, echelle, materiaux, liens, drag, materiauChoisi, resultat, temps }) {
  const { largeur, hauteur } = dimensionsCanvas(niveau, echelle)
  ctx.clearRect(0, 0, largeur, hauteur)

  ctx.save()
  if (resultat) {
    const { dx, dy } = secousse(temps)
    ctx.translate(dx, dy)
  }

  // Zone de construction
  const coin = metresVersPixels(0, niveau.hauteurZone, niveau, echelle)
  const oppose = metresVersPixels(niveau.largeurZone, 0, niveau, echelle)
  ctx.fillStyle = '#eef6ff'
  ctx.fillRect(coin.x, coin.y, oppose.x - coin.x, oppose.y - coin.y)
  ctx.strokeStyle = '#888'
  ctx.strokeRect(coin.x, coin.y, oppose.x - coin.x, oppose.y - coin.y)

  // Ligne de hauteur à atteindre
  const gauche = metresVersPixels(0, niveau.hauteurMinRequise, niveau, echelle)
  const droite = metresVersPixels(niveau.largeurZone, niveau.hauteurMinRequise, niveau, echelle)
  ctx.strokeStyle = 'crimson'
  ctx.setLineDash([6, 4])
  ctx.beginPath()
  ctx.moveTo(gauche.x, gauche.y)
  ctx.lineTo(droite.x, droite.y)
  ctx.stroke()
  ctx.setLineDash([])
  ctx.fillStyle = 'crimson'
  ctx.font = POLICE
  ctx.fillText(`${niveau.hauteurMinRequise} m`, gauche.x + 4, gauche.y - 4)

  // Liens
  const noeuds = indexerNoeuds(niveau)
  const details = Object.fromEntries((resultat?.detailsLiens ?? []).map((d) => [cleLien(d.noeudDepartId, d.noeudArriveeId), d]))
  for (const lien of liens) {
    const a = pixels(noeuds[lien.noeudDepartId], niveau, echelle)
    const b = pixels(noeuds[lien.noeudArriveeId], niveau, echelle)
    const detail = details[cleLien(lien.noeudDepartId, lien.noeudArriveeId)]
    if (detail) {
      dessinerLien(ctx, a, b, couleurStress(tauxAffiche(detail, temps)), estCasseA(detail, temps), largeurLien(echelle))
    } else {
      dessinerLien(ctx, a, b, materiaux[lien.materiauId]?.couleurHex ?? '#000', false, largeurLien(echelle))
    }
  }

  // Lien en cours de tracé
  if (drag) {
    const depart = pixels(noeuds[drag.departId], niveau, echelle)
    ctx.strokeStyle = materiaux[materiauChoisi]?.couleurHex ?? '#000'
    ctx.lineWidth = 3
    ctx.setLineDash([4, 4])
    ctx.beginPath()
    ctx.moveTo(depart.x, depart.y)
    ctx.lineTo(drag.x, drag.y)
    ctx.stroke()
    ctx.setLineDash([])
    ctx.lineWidth = 1
  }

  // Nœuds : cercle, ou triangle pour un ancrage au sol
  for (const noeud of niveau.noeuds) {
    const { x, y } = pixels(noeud, niveau, echelle)
    ctx.fillStyle = noeud.estAncrageSol ? '#444' : '#1b6ec2'
    ctx.beginPath()
    if (noeud.estAncrageSol) {
      const r = rayonNoeud(echelle) * 1.3
      ctx.moveTo(x, y - r)
      ctx.lineTo(x - r, y + r * 0.8)
      ctx.lineTo(x + r, y + r * 0.8)
      ctx.closePath()
    } else {
      ctx.arc(x, y, rayonNoeud(echelle), 0, Math.PI * 2)
    }
    ctx.fill()
  }
  ctx.restore()
}
