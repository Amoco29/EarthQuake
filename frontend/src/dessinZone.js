// Éléments fixes de la scène : dalle de fondation, échafaudage, nœuds (boulons et plaques).
import { texturer } from './dessinMateriaux.js'
import { metresVersPixels } from './geometrie.js'

const PAS = 2.5 // m, pas de la grille de nœuds
const DEBORD = 0.35 // m : l'échafaudage entoure la zone sans toucher les liens

/** Dalle de béton sous le sol (Y = 0), un peu plus large que la zone. */
export function dessinerDalle(ctx, niveau, echelle, origine) {
  const gauche = metresVersPixels(-0.6, 0, niveau, echelle, origine)
  const droite = metresVersPixels(niveau.largeurZone + 0.6, 0, niveau, echelle, origine)
  const epaisseur = Math.min(36, echelle * 0.5)
  const largeur = droite.x - gauche.x
  ctx.save()
  ctx.translate(gauche.x, gauche.y)
  ctx.fillStyle = '#9b9b94'
  ctx.fillRect(0, 0, largeur, epaisseur)
  ctx.translate(0, epaisseur / 2)
  texturer(ctx, 'BETON', largeur, epaisseur)
  ctx.translate(0, -epaisseur / 2)
  ctx.fillStyle = 'rgba(255, 255, 255, 0.35)' // arête haute éclairée
  ctx.fillRect(0, 0, largeur, 3)
  ctx.strokeStyle = 'rgba(40, 40, 40, 0.45)' // joints de dilatation
  ctx.lineWidth = 1
  for (let m = PAS; m < niveau.largeurZone + 1.2; m += PAS) {
    ctx.beginPath()
    ctx.moveTo(m * echelle, 3)
    ctx.lineTo(m * echelle, epaisseur)
    ctx.stroke()
  }
  ctx.restore()
}

/** Échafaudage en pointillés : deux montants et une traverse tous les 2,5 m. */
export function dessinerEchafaudage(ctx, niveau, echelle, origine) {
  const x0 = -DEBORD
  const x1 = niveau.largeurZone + DEBORD
  const trait = (xa, ya, xb, yb) => {
    const a = metresVersPixels(xa, ya, niveau, echelle, origine)
    const b = metresVersPixels(xb, yb, niveau, echelle, origine)
    ctx.moveTo(a.x, a.y)
    ctx.lineTo(b.x, b.y)
  }
  ctx.save()
  ctx.strokeStyle = 'rgba(45, 45, 45, 0.55)'
  ctx.lineWidth = 2
  ctx.setLineDash([9, 6])
  ctx.beginPath()
  trait(x0, 0, x0, niveau.hauteurZone)
  trait(x1, 0, x1, niveau.hauteurZone)
  for (let y = PAS; y <= niveau.hauteurZone + 1e-9; y += PAS) trait(x0, y, x1, y)
  ctx.stroke()
  ctx.restore()
}

function polygone(ctx, x, y, rayon, cotes, decalage = 0) {
  ctx.beginPath()
  for (let i = 0; i < cotes; i++) {
    const angle = decalage + (i * 2 * Math.PI) / cotes
    ctx[i === 0 ? 'moveTo' : 'lineTo'](x + rayon * Math.cos(angle), y + rayon * Math.sin(angle))
  }
  ctx.closePath()
}

function boulon(ctx, x, y, rayon) {
  ctx.fillStyle = '#4a5056'
  polygone(ctx, x, y, rayon, 6)
  ctx.fill()
  ctx.fillStyle = 'rgba(255, 255, 255, 0.5)'
  ctx.beginPath()
  ctx.arc(x - rayon * 0.25, y - rayon * 0.25, rayon * 0.3, 0, Math.PI * 2)
  ctx.fill()
}

/** Nœud : plaque métallique ronde et tête de boulon ; ancrage au sol : platine carrée à quatre boulons. */
export function dessinerNoeud(ctx, x, y, rayon, estAncrage) {
  ctx.save()
  const metal = ctx.createRadialGradient(x - rayon * 0.4, y - rayon * 0.4, rayon * 0.1, x, y, rayon * 1.4)
  metal.addColorStop(0, '#e4e8ec')
  metal.addColorStop(1, '#7b8288')
  ctx.fillStyle = metal
  ctx.strokeStyle = '#2f3438'
  ctx.lineWidth = 2
  if (estAncrage) {
    const demi = rayon * 1.6
    ctx.beginPath()
    ctx.roundRect(x - demi, y - demi * 0.6, demi * 2, demi * 1.2, 2)
    ctx.fill()
    ctx.stroke()
    for (const sx of [-1, 1]) for (const sy of [-1, 1]) boulon(ctx, x + sx * demi * 0.75, y + sy * demi * 0.3, rayon * 0.3)
    boulon(ctx, x, y, rayon * 0.6)
  } else {
    ctx.beginPath()
    ctx.arc(x, y, rayon * 1.25, 0, Math.PI * 2)
    ctx.fill()
    ctx.stroke()
    boulon(ctx, x, y, rayon * 0.7)
  }
  ctx.restore()
}
