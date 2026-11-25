import { useState } from 'react'
import { positionDecor } from './geometrie.js'
import { useTaille } from './useTaille.js'

const COUCHES = ['ciel', 'fumee', 'immeubles']

/** Décor : ciel, fumée, immeubles. Le haut de l'herbe est à `sol` pixels du haut de `espace` ({ largeur, hauteur }). */
export default function Decor({ espace, sol, secousse = false }) {
  if (espace.largeur === 0) return null
  const { hauteur, haut } = positionDecor(espace.largeur, sol)
  // Au-dessus de l'image : couleur du ciel ; en dessous : couleur du sol.
  const fond = `linear-gradient(#4c7c9a ${haut + hauteur}px, #2f3a2f ${haut + hauteur}px)`
  return (
    <div className="decor" aria-hidden="true" style={{ background: fond }}>
      {COUCHES.map((c) => (
        <div key={c} className={`decor-${c}${secousse ? ' secousse' : ''}`} style={{ top: haut, height: hauteur }} />
      ))}
    </div>
  )
}

/** Écran plein (titre, carte) : mesure son conteneur et place l'herbe aux 85 % de la hauteur. */
export function EcranAvecDecor({ className, children }) {
  const [element, setElement] = useState(null)
  const espace = useTaille(element)
  return (
    <main className={className} ref={setElement}>
      <Decor espace={espace} sol={espace.hauteur * 0.85} />
      {children}
    </main>
  )
}
