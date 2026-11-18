/** Panneau de choix du matériau. Les matériaux non autorisés dans le niveau sont affichés verrouillés. */
export default function PanneauMateriaux({ materiaux, autorises, choisi, onChoisir }) {
  return (
    <aside className="panneau-materiaux" aria-label="Matériaux">
      <h2>Matériaux</h2>
      {materiaux.map((m) => {
        const libre = autorises.includes(m.id)
        return (
          <button
            key={m.id}
            className={`materiau${choisi === m.id ? ' choisi' : ''}`}
            disabled={!libre}
            onClick={() => onChoisir(m.id)}
            aria-pressed={choisi === m.id}
          >
            <span className="pastille" style={{ background: m.couleurHex }} />
            {m.nom}
            <small>{libre ? `${m.coutUnitaire} / m` : 'verrouillé'}</small>
          </button>
        )
      })}
      <p className="aide">
        Choisis un matériau, puis glisse d&apos;un nœud à un autre. Clique sur un lien pour l&apos;enlever.
      </p>
      <p className="aide">Astuce : un carré se déforme, un triangle tient. Ajoute des diagonales.</p>
    </aside>
  )
}
