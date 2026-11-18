// Animation du séisme : fonctions pures du temps t (secondes depuis le début), faciles à tester.

export const DUREE_ANIMATION = 8
/** Le backend donne tempsRupture sur 10 s de séisme ; on le ramène à la durée de l'animation. */
export const ECHELLE_TEMPS = 0.8
/** Le stress monte progressivement pendant ce temps au début de la secousse. */
export const MONTEE = 2

const clamp = (v, min, max) => Math.min(max, Math.max(min, v))

/** Vert (taux 0) vers rouge (taux ≥ 1), en passant par le jaune et l'orange. */
export function couleurStress(taux) {
  const teinte = Math.round(120 * (1 - clamp(taux, 0, 1)))
  return `hsl(${teinte}, 80%, 42%)`
}

export function tauxAffiche(detail, t) {
  return detail.tauxStress * clamp(t / MONTEE, 0, 1)
}

export function estCasseA(detail, t) {
  return detail.tempsRupture != null && t >= detail.tempsRupture * ECHELLE_TEMPS
}

export function animationTerminee(t) {
  return t >= DUREE_ANIMATION
}

/** Décalage en pixels de l'écran, qui s'amortit jusqu'à la fin de l'animation. */
export function secousse(t) {
  if (animationTerminee(t)) return { dx: 0, dy: 0 }
  const amplitude = 5 * (1 - t / DUREE_ANIMATION)
  return { dx: amplitude * Math.sin(t * 35), dy: amplitude * 0.3 * Math.sin(t * 47 + 1) }
}
