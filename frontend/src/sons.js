// Sons du jeu (fichiers dans public/sons). Renvoie l'élément audio pour pouvoir l'arrêter ; un navigateur qui refuse la lecture n'est pas une erreur.
export function jouer(nom, volume = 0.6) {
  try {
    const son = new Audio(`/sons/${nom}`)
    son.volume = volume
    son.play().catch(() => {})
    return son
  } catch {
    return null
  }
}
