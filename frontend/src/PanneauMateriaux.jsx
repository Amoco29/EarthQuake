/** Panneau des matériaux : on prend une pièce en appuyant dessus, puis on la glisse sur la structure. Seuls les matériaux autorisés dans le niveau sont affichés. */
export default function PanneauMateriaux({ materiaux, autorises, onPrendre }) {
  return (
    <aside className="panneau-materiaux" data-tuto="panneau-materiaux" aria-label="Matériaux">
      <h2>Matériaux</h2>
      {materiaux
        .filter((m) => autorises.includes(m.id))
        .map((m) => (
          <button key={m.id} className="materiau" onPointerDown={(e) => onPrendre(m.id, e)}>
            <span className="pastille" style={{ background: m.couleurHex }} />
            {m.nom}
            <small>
              {m.coutUnitaire} / m · max {m.longueurMax} m
            </small>
          </button>
        ))}
      <p className="aide">
        Glisse une pièce sur deux nœuds voisins : ses bouts s&apos;aimantent sur les nœuds. Clique sur un lien pour l&apos;enlever.
      </p>
      <p className="aide">Astuce : un carré se déforme, un triangle tient. Ajoute des diagonales.</p>
    </aside>
  )
}
