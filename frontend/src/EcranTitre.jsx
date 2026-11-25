import { EcranAvecDecor } from './Decor.jsx'

export default function EcranTitre({ onJouer }) {
  return (
    <EcranAvecDecor className="ecran-titre">
      <h1 className="titre-jeu">EarthQuake-Faker</h1>
      <button className="bouton-jouer" onClick={onJouer} autoFocus>
        Jouer
      </button>
    </EcranAvecDecor>
  )
}
