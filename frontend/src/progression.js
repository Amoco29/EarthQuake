// Le niveau 0 est le tutoriel : il ne compte pas dans la progression.
export const estTutoriel = (niveau) => niveau.numero === 0

/** Un niveau s'ouvre quand le précédent est réussi ; le tutoriel et le niveau qui le suit sont ouverts dès le départ. */
export function niveauOuvert(niveaux, index, scores) {
  const precedent = niveaux[index - 1]
  return !precedent || estTutoriel(precedent) || estTutoriel(niveaux[index]) || scores[precedent.id] !== undefined
}
