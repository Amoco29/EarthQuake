import { estTutoriel, niveauOuvert } from './progression.js'

const DECORS = { prairie: '#8fd18a', montagne: '#b8b8c8', ville: '#e0b878' }

/**
 * Carte du monde : les niveaux sont reliés en chemin. Un niveau s'ouvre quand le précédent est réussi ;
 * le tutoriel (niveau 0) est toujours ouvert et ne compte pas dans la progression.
 * `scores` : { idNiveau: meilleurScore }, tenu en mémoire par App (pas de localStorage pour l'état du jeu).
 */
export default function EcranCarte({ niveaux, scores, onChoisir }) {
  return (
    <main className="ecran-carte">
      <h1>EarthQuake Faker</h1>
      <p>Aide Félix Fixe Junior à construire des structures qui résistent aux séismes.</p>
      <ol className="carte">
        {niveaux.map((n, i) => {
          const ouvert = niveauOuvert(niveaux, i, scores)
          const tuto = estTutoriel(n)
          const reussi = scores[n.id] !== undefined
          return (
            <li key={n.id}>
              <button
                className={`etape${reussi ? ' reussi' : ''}`}
                style={{ background: ouvert ? (DECORS[n.themeDecor] ?? '#ddd') : '#ccc' }}
                disabled={!ouvert}
                onClick={() => onChoisir(n.id)}
              >
                {tuto ? <strong>Tutoriel</strong> : <><strong>{n.numero}</strong> {n.nom}</>}
                <small>
                  {tuto
                    ? 'Apprends les gestes de base'
                    : ouvert
                    ? `Budget ${n.budgetMax} · ${n.hauteurMinRequise} m${reussi ? ` · Meilleur score ${scores[n.id]}` : ''}`
                    : 'Termine le niveau précédent'}
                </small>
              </button>
            </li>
          )
        })}
      </ol>
    </main>
  )
}
