import { positionDecor } from './geometrie.js'

const COUCHES = ['ciel', 'fumee', 'immeubles']

/** Décor derrière le canvas : ciel, fumée, immeubles. Le sol du décor est aligné sur le Y = 0 du niveau. */
export default function Decor({ espace, hauteurCanvas, secousse }) {
  if (espace.largeur === 0) return null
  const { hauteur, haut } = positionDecor(espace, hauteurCanvas)
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
