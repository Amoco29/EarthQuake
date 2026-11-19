import { useEffect, useMemo, useRef, useState } from 'react'
import { animationTerminee, DUREE_ANIMATION } from './animation.js'
import { dessinerScene, POLICE } from './dessin.js'
import { lienAutorise } from './construction.js'
import { cibleDePiece, dimensionsCanvas, lienSous } from './geometrie.js'

/**
 * Canvas du niveau. En construction : `piece` ({ materiauId, clientX, clientY }) est la pièce que le joueur tient,
 * prise dans le panneau ; elle s'aimante sur deux nœuds voisins et se pose au relâchement (`onLien`, ou `onRefus`),
 * puis `onPieceFinie` la rend. Cliquer sur un lien le supprime. Quand `resultat` est fourni, joue l'animation du séisme puis appelle `onFin` ;
 * ensuite (`termine`), les liens `surlignes` clignotent. Un lien trop long est tracé en rouge et refusé (`onRefus`).
 */
export default function NiveauCanvas({ niveau, echelle, materiaux, liens, piece, resultat, termine, surlignes, onLien, onRefus, onSupprimer, onPieceFinie, onFin }) {
  const ref = useRef(null)
  const [curseur, setCurseur] = useState(null) // position de la pièce tenue, en pixels du canvas
  // Débuts des animations, gardés hors de l'effet : un redimensionnement ne doit pas les relancer.
  const debuts = useRef({ secousse: null, clignote: null })
  const { largeur, hauteur } = dimensionsCanvas(niveau, echelle)

  const drag = useMemo(() => {
    if (!piece || !curseur) return null
    const paire = cibleDePiece(curseur.x, curseur.y, niveau, echelle)
    const cible = paire && { ...paire, ...lienAutorise(paire.departId, paire.arriveeId, niveau, materiaux[piece.materiauId]) }
    return { materiauId: piece.materiauId, ...curseur, cible }
  }, [piece, curseur, niveau, echelle, materiaux])

  useEffect(() => {
    let annule = false
    let image = 0
    const ctx = ref.current.getContext('2d')
    const scene = { niveau, echelle, materiaux, liens, drag, resultat, surlignes }

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
  }, [niveau, echelle, materiaux, liens, drag, resultat, termine, surlignes, onFin])

  const versCanvas = (e) => {
    const rect = ref.current.getBoundingClientRect()
    return { x: ((e.clientX - rect.left) * largeur) / rect.width, y: ((e.clientY - rect.top) * hauteur) / rect.height }
  }

  // Pendant que la pièce est tenue, on suit le pointeur sur toute la fenêtre : le relâchement peut avoir lieu hors du canvas.
  useEffect(() => {
    if (!piece) {
      setCurseur(null)
      return undefined
    }
    setCurseur(versCanvas(piece))
    const deplacer = (e) => setCurseur(versCanvas(e))
    const relacher = (e) => {
      const { x, y } = versCanvas(e)
      const paire = cibleDePiece(x, y, niveau, echelle)
      if (paire) {
        const verdict = lienAutorise(paire.departId, paire.arriveeId, niveau, materiaux[piece.materiauId])
        if (verdict.ok) onLien(paire.departId, paire.arriveeId, piece.materiauId)
        else onRefus?.(verdict.message)
      }
      onPieceFinie()
    }
    window.addEventListener('pointermove', deplacer)
    window.addEventListener('pointerup', relacher)
    window.addEventListener('pointercancel', onPieceFinie)
    return () => {
      window.removeEventListener('pointermove', deplacer)
      window.removeEventListener('pointerup', relacher)
      window.removeEventListener('pointercancel', onPieceFinie)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [piece])

  function enfoncer(e) {
    if (resultat || piece) return
    const { x, y } = versCanvas(e)
    const lien = lienSous(x, y, liens, niveau, echelle)
    if (lien) onSupprimer(lien.noeudDepartId, lien.noeudArriveeId)
  }

  return (
    <canvas
      ref={ref}
      width={largeur}
      height={hauteur}
      style={{ touchAction: 'none', maxWidth: '100%', cursor: 'default' }}
      aria-label={`Niveau ${niveau.nom}`}
      onPointerDown={enfoncer}
    />
  )
}
