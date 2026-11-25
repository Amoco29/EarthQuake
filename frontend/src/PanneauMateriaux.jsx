import { useEffect, useRef } from 'react'
import { texturer } from './dessinMateriaux.js'

const PX_PAR_METRE = 28
const EPAISSEUR = 16

/** Une pièce posée sur l'étagère : la même texture que sur les liens. */
function Piece({ materiau }) {
  const ref = useRef(null)
  const longueur = materiau.longueurMax * PX_PAR_METRE
  useEffect(() => {
    const ctx = ref.current.getContext('2d')
    ctx.clearRect(0, 0, ref.current.width, ref.current.height)
    ctx.translate(2, EPAISSEUR / 2 + 2)
    ctx.fillStyle = materiau.couleurHex
    ctx.fillRect(0, -EPAISSEUR / 2, longueur, EPAISSEUR)
    texturer(ctx, materiau.id, longueur, EPAISSEUR)
    ctx.setTransform(1, 0, 0, 1, 0, 0)
  }, [materiau, longueur])
  return <canvas ref={ref} width={longueur + 4} height={EPAISSEUR + 4} aria-hidden="true" />
}

/** Étagère de matériaux : on prend une pièce en appuyant dessus, puis on la glisse sur la structure. Seuls les matériaux autorisés dans le niveau sont affichés. */
export default function PanneauMateriaux({ materiaux, autorises, onPrendre }) {
  return (
    <aside className="etagere" data-tuto="panneau-materiaux" aria-label="Matériaux">
      {materiaux
        .filter((m) => autorises.includes(m.id))
        .map((m) => (
          <button key={m.id} className="materiau" onPointerDown={(e) => onPrendre(m.id, e)} title={`${m.nom} : max ${m.longueurMax} m`}>
            <Piece materiau={m} />
            <span>{m.nom}</span>
            <small>{m.coutUnitaire} / m</small>
          </button>
        ))}
    </aside>
  )
}
