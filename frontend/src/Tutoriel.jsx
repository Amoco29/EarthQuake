/** Bulle d'aide du tutoriel. Le halo sur l'élément visé est fait en CSS via data-tuto-actif (voir index.css). */
export default function Tutoriel({ etape, numero, total, onSuivant, onPasser }) {
  return (
    <div className="bulle-tuto" role="status" aria-live="polite">
      <p className="etape-tuto">
        Étape {numero} / {total}
      </p>
      <p>{etape.texte}</p>
      <div className="actions">
        {etape.condition.evenement === 'suivant' && <button onClick={onSuivant}>Suivant</button>}
        <button onClick={onPasser}>Passer</button>
      </div>
    </div>
  )
}
