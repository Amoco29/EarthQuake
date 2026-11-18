import { useEffect, useRef, useState } from 'react'
import { animationTerminee, DUREE_ANIMATION } from './animation.js'
import { dessinerScene, POLICE } from './dessin.js'
import { lienAutorise, limiteLien } from './construction.js'
import { dimensionsCanvas, lienSous, noeudSous, pixelsVersMetres } from './geometrie.js'

/**
 * Canvas du niveau. En construction : glisser d'un nœud à un autre pose un lien du matériau choisi,
 * cliquer sur un lien le supprime. Quand `resultat` est fourni, joue l'animation du séisme puis appelle `onFin` ;
 * ensuite (`termine`), les liens `surlignes` clignotent. Un lien trop long est tracé en rouge et refusé (`onRefus`).
 */
export default function NiveauCanvas({ niveau, echelle, materiaux, liens, materiauChoisi, resultat, termine, surlignes, onLien, onRefus, onSupprimer, onFin }) {
  const ref = useRef(null)
  const [drag, setDrag] = useState(null)
  // Débuts des animations, gardés hors de l'effet : un redimensionnement ne doit pas les relancer.
  const debuts = useRef({ secousse: null, clignote: null })
  const { largeur, hauteur } = dimensionsCanvas(niveau, echelle)

  useEffect(() => {
    let annule = false
    let image = 0
    const ctx = ref.current.getContext('2d')
    const scene = { niveau, echelle, materiaux, liens, drag, materiauChoisi, resultat, surlignes }

    // La police doit être chargée avant de dessiner du texte dans le canvas.
    const police = document.fonts ? document.fonts.load(POLICE) : Promise.resolve()
    police.catch(() => {}).then(() => {
      if (annule) return
      if (!resultat) {
        debuts.current = { secousse: null, clignote: null }
        dessinerScene(ctx, { ...scene, temps: 0 })
        return
      }
      if (termine && surlignes.length === 0) {
        dessinerScene(ctx, { ...scene, temps: DUREE_ANIMATION })
        return
      }
      const phase = termine ? 'clignote' : 'secousse'
      debuts.current[phase] ??= performance.now()
      const debut = debuts.current[phase]
      const boucle = (maintenant) => {
        if (annule) return
        const ecoule = (maintenant - debut) / 1000
        if (termine) {
          // Séisme fini : image finale, avec le halo qui clignote
          dessinerScene(ctx, { ...scene, temps: DUREE_ANIMATION + ecoule })
          image = requestAnimationFrame(boucle)
          return
        }
        dessinerScene(ctx, { ...scene, temps: ecoule })
        if (animationTerminee(ecoule)) {
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
  }, [niveau, echelle, materiaux, liens, drag, materiauChoisi, resultat, termine, surlignes, onFin])

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
      setDrag({ departId: noeud.id, x, y, refuse: false })
      return
    }
    const lien = lienSous(x, y, liens, niveau, echelle)
    if (lien) onSupprimer(lien.noeudDepartId, lien.noeudArriveeId)
  }

  /** Vrai si la cible du tracé (le nœud survolé, sinon le pointeur) est trop loin du nœud de départ. */
  function tropLoin(departId, x, y) {
    const cible = noeudSous(x, y, niveau, echelle)
    const depart = niveau.noeuds.find((n) => n.id === departId)
    const materiau = materiaux[materiauChoisi]
    if (cible) return cible.id !== departId && !lienAutorise(departId, cible.id, niveau, materiau).ok
    const point = pixelsVersMetres(x, y, niveau, echelle)
    return Math.hypot(point.x - depart.posX, point.y - depart.posY) > limiteLien(niveau, materiau)
  }

  function deplacer(e) {
    if (!drag) return
    const { x, y } = position(e)
    setDrag({ ...drag, x, y, refuse: tropLoin(drag.departId, x, y) })
  }

  function relacher(e) {
    if (!drag) return
    const { x, y } = position(e)
    const noeud = noeudSous(x, y, niveau, echelle)
    if (noeud && noeud.id !== drag.departId) {
      const verdict = lienAutorise(drag.departId, noeud.id, niveau, materiaux[materiauChoisi])
      if (verdict.ok) onLien(drag.departId, noeud.id)
      else onRefus?.(verdict.message)
    }
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
