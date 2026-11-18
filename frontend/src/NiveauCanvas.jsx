import { useEffect, useRef, useState } from 'react'
import { animationTerminee } from './animation.js'
import { dessinerScene, POLICE } from './dessin.js'
import { dimensionsCanvas, lienSous, noeudSous } from './geometrie.js'

/**
 * Canvas du niveau. En construction : glisser d'un nœud à un autre pose un lien du matériau choisi,
 * cliquer sur un lien le supprime. Quand `resultat` est fourni, joue l'animation du séisme puis appelle `onFin`.
 */
export default function NiveauCanvas({ niveau, echelle, materiaux, liens, materiauChoisi, resultat, onLien, onSupprimer, onFin }) {
  const ref = useRef(null)
  const [drag, setDrag] = useState(null)
  const { largeur, hauteur } = dimensionsCanvas(niveau, echelle)

  useEffect(() => {
    let annule = false
    let image = 0
    const ctx = ref.current.getContext('2d')
    const scene = { niveau, echelle, materiaux, liens, drag, materiauChoisi, resultat }

    // La police doit être chargée avant de dessiner du texte dans le canvas.
    const police = document.fonts ? document.fonts.load(POLICE) : Promise.resolve()
    police.catch(() => {}).then(() => {
      if (annule) return
      if (!resultat) {
        dessinerScene(ctx, { ...scene, temps: 0 })
        return
      }
      const debut = performance.now()
      const boucle = (maintenant) => {
        if (annule) return
        const temps = (maintenant - debut) / 1000
        dessinerScene(ctx, { ...scene, temps })
        if (animationTerminee(temps)) {
          onFin?.()
        } else {
          image = requestAnimationFrame(boucle)
        }
      }
      image = requestAnimationFrame(boucle)
    })
    return () => {
      annule = true
      cancelAnimationFrame(image)
    }
  }, [niveau, echelle, materiaux, liens, drag, materiauChoisi, resultat, onFin])

  function position(e) {
    const rect = ref.current.getBoundingClientRect()
    return {
      x: ((e.clientX - rect.left) * largeur) / rect.width,
      y: ((e.clientY - rect.top) * hauteur) / rect.height,
    }
  }

  function enfoncer(e) {
    if (resultat) return
    const { x, y } = position(e)
    const noeud = noeudSous(x, y, niveau, echelle)
    if (noeud) {
      ref.current.setPointerCapture(e.pointerId)
      setDrag({ departId: noeud.id, x, y })
      return
    }
    const lien = lienSous(x, y, liens, niveau, echelle)
    if (lien) onSupprimer(lien.noeudDepartId, lien.noeudArriveeId)
  }

  function deplacer(e) {
    if (drag) setDrag({ ...drag, ...position(e) })
  }

  function relacher(e) {
    if (!drag) return
    const { x, y } = position(e)
    const noeud = noeudSous(x, y, niveau, echelle)
    if (noeud && noeud.id !== drag.departId) onLien(drag.departId, noeud.id)
    setDrag(null)
  }

  return (
    <canvas
      ref={ref}
      width={largeur}
      height={hauteur}
      style={{ touchAction: 'none', maxWidth: '100%', cursor: resultat ? 'default' : 'crosshair' }}
      aria-label={`Niveau ${niveau.nom}`}
      onPointerDown={enfoncer}
      onPointerMove={deplacer}
      onPointerUp={relacher}
      onPointerCancel={() => setDrag(null)}
    />
  )
}
