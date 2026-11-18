import { useEffect, useState } from 'react'

/** Taille en pixels (largeur, hauteur) de l'élément donné, mise à jour quand la fenêtre change. */
export function useTaille(element) {
  const [taille, setTaille] = useState({ largeur: 0, hauteur: 0 })

  useEffect(() => {
    if (!element) return undefined
    const mesurer = () => setTaille({ largeur: element.clientWidth, hauteur: element.clientHeight })
    mesurer()
    const observateur = new ResizeObserver(mesurer)
    observateur.observe(element)
    return () => observateur.disconnect()
  }, [element])

  return taille
}
