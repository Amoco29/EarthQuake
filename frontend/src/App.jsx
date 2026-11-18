import { useState } from 'react'
import { chargerNiveau } from './api.js'
import NiveauCanvas from './NiveauCanvas.jsx'

function App() {
  const [id, setId] = useState('1')
  const [niveau, setNiveau] = useState(null)
  const [erreur, setErreur] = useState(null)

  async function charger() {
    setNiveau(null)
    setErreur(null)
    try {
      setNiveau(await chargerNiveau(id))
    } catch (e) {
      setErreur(e)
    }
  }

  return (
    <main>
      <h1>EarthQuake Faker</h1>
      <p>Interface de test du backend.</p>

      <label>
        Niveau :{' '}
        <input value={id} onChange={(e) => setId(e.target.value)} size={6} />
      </label>{' '}
      <button onClick={charger}>Charger</button>

      {erreur && (
        <p role="alert" style={{ color: 'crimson' }}>
          {erreur.codeErreur ? `[${erreur.codeErreur}] ` : ''}
          {erreur.message}
          {erreur.status ? ` (HTTP ${erreur.status})` : ''}
        </p>
      )}

      {niveau && <NiveauCanvas niveau={niveau} />}

      {niveau && <pre>{JSON.stringify(niveau, null, 2)}</pre>}
    </main>
  )
}

export default App
