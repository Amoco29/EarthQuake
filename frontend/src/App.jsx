import { useCallback, useEffect, useState } from 'react'
import { chargerMateriaux, chargerNiveaux } from './api.js'
import EcranCarte from './EcranCarte.jsx'
import EcranNiveau from './EcranNiveau.jsx'
import MessageErreur from './MessageErreur.jsx'

function App() {
  const [donnees, setDonnees] = useState(null) // { niveaux, materiaux }
  const [erreur, setErreur] = useState(null)
  const [niveauOuvert, setNiveauOuvert] = useState(null) // null = on est sur la carte
  const [scores, setScores] = useState({}) // état du jeu en mémoire : perdu au rechargement de la page

  const charger = useCallback(() => {
    setErreur(null)
    Promise.all([chargerNiveaux(), chargerMateriaux()])
      .then(([niveaux, materiaux]) => setDonnees({ niveaux, materiaux }))
      .catch(setErreur)
  }, [])

  useEffect(() => {
    charger()
  }, [charger])

  const enregistrerVictoire = useCallback(
    (id, score) => setScores((s) => ({ ...s, [id]: Math.max(s[id] ?? 0, score) })),
    [],
  )

  if (erreur) {
    return (
      <main>
        <h1>EarthQuake Faker</h1>
        <MessageErreur erreur={erreur} />
        <button onClick={charger}>Réessayer</button>
      </main>
    )
  }
  if (!donnees) return <main>Chargement…</main>

  if (niveauOuvert === null) {
    return <EcranCarte niveaux={donnees.niveaux} scores={scores} onChoisir={setNiveauOuvert} />
  }

  const index = donnees.niveaux.findIndex((n) => n.id === niveauOuvert)
  return (
    <EcranNiveau
      key={niveauOuvert}
      id={niveauOuvert}
      materiaux={donnees.materiaux}
      suivantId={donnees.niveaux[index + 1]?.id ?? null}
      onRetour={() => setNiveauOuvert(null)}
      onVictoire={enregistrerVictoire}
      onChoisirNiveau={setNiveauOuvert}
    />
  )
}

export default App
