import { couleurStress, etatRupture, secousse, tauxAffiche } from './animation.js'
import { cleLien, indexerNoeuds, noeudsCompatibles } from './construction.js'
import { metresVersPixels } from './geometrie.js'

export const POLICE = "12px 'Pokemon', 'Press Start 2P', monospace"
const largeurLien = (echelle) => Math.min(10, Math.max(5, echelle * 0.12))
const rayonNoeud = (echelle) => Math.min(11, Math.max(6, echelle * 0.13))

function pixels(noeud, niveau, echelle, origine) {
  return metresVersPixels(noeud.posX, noeud.posY, niveau, echelle, origine)
}

/** Un lien = un rectangle de la longueur entre les nœuds, pivoté selon l'angle. */
function dessinerRectangle(ctx, a, b, de, vers, couleur, largeur, bois = false) {
  const dx = b.x - a.x
  const dy = b.y - a.y
  ctx.save()
  ctx.translate(a.x + dx * de, a.y + dy * de)
  ctx.rotate(Math.atan2(dy, dx))
  ctx.fillStyle = couleur
  const longueur = Math.hypot(dx, dy) * (vers - de)
  ctx.fillRect(0, -largeur / 2, longueur, largeur)
  if (bois) texturerBois(ctx, longueur, largeur)
  ctx.restore()
}

/** Bois très léger, dans le style des illustrations : liseré brun, reflet sur le dessus, deux veines. Sur la couleur déjà posée. */
function texturerBois(ctx, longueur, largeur) {
  ctx.fillStyle = 'rgba(255, 255, 255, 0.18)'
  ctx.fillRect(0, -largeur / 2, longueur, largeur * 0.25)
  ctx.strokeStyle = 'rgba(70, 25, 5, 0.18)'
  ctx.lineWidth = 1
  for (const [y, debut, fin] of [[-0.12, 0.08, 0.7], [0.22, 0.3, 0.92]]) {
    ctx.beginPath()
    ctx.moveTo(longueur * debut, largeur * y)
    ctx.quadraticCurveTo(longueur * ((debut + fin) / 2), largeur * (y + 0.12), longueur * fin, largeur * y)
    ctx.stroke()
  }
  ctx.strokeStyle = 'rgba(70, 25, 5, 0.55)'
  ctx.lineWidth = 1.5
  ctx.strokeRect(0, -largeur / 2, longueur, largeur)
}

const estBois = (materiauId) => materiauId === 'BOIS'

/** Un lien cassé tombe en tournant autour de son centre (dy en mètres, angle en radians) et s'efface (alpha). */
function dessinerLien(ctx, a, b, couleur, largeur, echelle, etat = { dy: 0, angle: 0, alpha: 1 }, bois = false) {
  if (etat.alpha <= 0) return
  const centre = { x: (a.x + b.x) / 2, y: (a.y + b.y) / 2 }
  ctx.save()
  ctx.globalAlpha = etat.alpha
  ctx.translate(centre.x, centre.y + etat.dy * echelle)
  ctx.rotate(etat.angle)
  ctx.translate(-centre.x, -centre.y)
  dessinerRectangle(ctx, a, b, 0, 1, couleur, largeur, bois)
  ctx.restore()
}

/**
 * scene : { niveau, materiaux, liens, drag, resultat, temps }
 * `drag` : pièce tenue par le joueur { materiauId, x, y, cible: { departId, arriveeId, ok } | null }
 * `resultat` + `temps` : mode séisme (couleurs de stress, secousse, liens cassés).
 */
export function dessinerScene(ctx, { niveau, echelle, origine, espace, materiaux, liens, drag, resultat, temps, surlignes = [] }) {
  ctx.clearRect(0, 0, espace.largeur, espace.hauteur)

  ctx.save()
  if (resultat) {
    const { dx, dy } = secousse(temps)
    ctx.translate(dx, dy)
  }

  // Zone de construction
  const coin = metresVersPixels(0, niveau.hauteurZone, niveau, echelle, origine)
  const oppose = metresVersPixels(niveau.largeurZone, 0, niveau, echelle, origine)
  ctx.fillStyle = 'rgba(255,255,255,0.55)' // voile : le décor reste visible, la structure lisible
  ctx.fillRect(coin.x, coin.y, oppose.x - coin.x, oppose.y - coin.y)
  ctx.strokeStyle = '#888'
  ctx.strokeRect(coin.x, coin.y, oppose.x - coin.x, oppose.y - coin.y)

  // Ligne de hauteur à atteindre
  const gauche = metresVersPixels(0, niveau.hauteurMinRequise, niveau, echelle, origine)
  const droite = metresVersPixels(niveau.largeurZone, niveau.hauteurMinRequise, niveau, echelle, origine)
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
    const a = pixels(noeuds[lien.noeudDepartId], niveau, echelle, origine)
    const b = pixels(noeuds[lien.noeudArriveeId], niveau, echelle, origine)
    const detail = details[cleLien(lien.noeudDepartId, lien.noeudArriveeId)]
    if (detail) {
      const etat = etatRupture(detail, temps)
      dessinerLien(ctx, a, b, couleurStress(etat.casse ? 1 : tauxAffiche(detail, temps)), largeurLien(echelle), echelle, etat, estBois(lien.materiauId))
    } else {
      dessinerLien(ctx, a, b, materiaux[lien.materiauId]?.couleurHex ?? '#000', largeurLien(echelle), echelle, undefined, estBois(lien.materiauId))
    }
  }

  // Nœuds : cercle, ou triangle pour un ancrage au sol
  for (const noeud of niveau.noeuds) {
    const { x, y } = pixels(noeud, niveau, echelle, origine)
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

  if (drag) dessinerPiece(ctx, drag, noeuds, materiaux, niveau, echelle, origine)
  dessinerSurlignage(ctx, surlignes, noeuds, niveau, echelle, origine, temps)
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
  dessinerRectangle(ctx, a, b, 0, 1, materiau?.couleurHex ?? '#000', largeurLien(echelle), estBois(drag.materiauId))
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

/** Halo rouge qui clignote (2 fois par seconde) sur les liens fragiles et leurs deux nœuds. */
function dessinerSurlignage(ctx, surlignes, noeuds, niveau, echelle, origine, temps) {
  if (surlignes.length === 0) return
  ctx.save()
  ctx.globalAlpha = 0.25 + 0.55 * (0.5 + 0.5 * Math.sin(temps * Math.PI * 4))
  ctx.strokeStyle = '#ff1744'
  ctx.lineCap = 'round'
  for (const s of surlignes) {
    const a = pixels(noeuds[s.noeudDepartId], niveau, echelle, origine)
    const b = pixels(noeuds[s.noeudArriveeId], niveau, echelle, origine)
    ctx.lineWidth = largeurLien(echelle) + 10
    ctx.beginPath()
    ctx.moveTo(a.x, a.y)
    ctx.lineTo(b.x, b.y)
    ctx.stroke()
    ctx.lineWidth = 3
    for (const p of [a, b]) {
      ctx.beginPath()
      ctx.arc(p.x, p.y, rayonNoeud(echelle) + 6, 0, Math.PI * 2)
      ctx.stroke()
    }
  }
  ctx.restore()
}
