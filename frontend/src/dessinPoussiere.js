// Poussière du séisme : fonction pure du temps, comme le reste de l'animation.
import { aleatoire } from './dessinMateriaux.js'

const VIE = 1.8 // secondes
const ROUGE_TAUX = 'rgba(176, 156, 128, '

/** `nb` nuages nés à l'instant `t0` autour de (x, y), répartis sur `etendue` pixels. Chaque nuage monte, grossit et s'efface. */
function nuages(ctx, { graine, t0, x, y, etendue, nb, echelle, temps }) {
  const hasard = aleatoire(graine)
  for (let i = 0; i < nb; i++) {
    const depart = t0 + hasard() * 0.6
    const dx = (hasard() - 0.5) * etendue
    const derive = (hasard() - 0.5) * echelle * 0.8
    const taille = (0.12 + hasard() * 0.18) * echelle
    const p = (temps - depart) / VIE
    if (p < 0 || p > 1) continue
    ctx.fillStyle = `${ROUGE_TAUX}${(0.38 * (1 - p)).toFixed(3)})`
    ctx.beginPath()
    ctx.arc(x + dx + derive * p, y - p * echelle * 0.9, taille * (0.6 + p), 0, Math.PI * 2)
    ctx.fill()
  }
}

/**
 * Poussière qui monte du sol pendant tout le séisme, plus une bouffée à chaque rupture.
 * `sol` : { x, y, largeur } (pixels), `ruptures` : [{ x, y, temps }] (temps de rupture en secondes d'animation).
 */
export function dessinerPoussiere(ctx, { temps, duree, echelle, sol, ruptures }) {
  ctx.save()
  for (let k = 0; k < 14; k++) {
    nuages(ctx, { graine: 100 + k, t0: (k / 14) * duree * 0.9, x: sol.x, y: sol.y, etendue: sol.largeur, nb: 4, echelle, temps })
  }
  ruptures.slice(0, 8).forEach((r, k) => {
    nuages(ctx, { graine: 500 + k, t0: r.temps, x: r.x, y: r.y, etendue: echelle * 0.8, nb: 7, echelle, temps })
  })
  ctx.restore()
}
