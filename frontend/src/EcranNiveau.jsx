import { useCallback, useEffect, useMemo, useState } from 'react'
import { chargerNiveau, simulerNiveau } from './api.js'
import { EXPLICATION_SOLIDITE, phraseDuPremierLienCasse, phraseMargeDeSecurite, premiersLiensCasses } from './bilan.js'
import { ajouterLien, coutTotal, hauteurAtteinte, supprimerLien } from './construction.js'
import MessageErreur from './MessageErreur.jsx'
import { calculerEchelle, PIXELS_PAR_METRE } from './geometrie.js'
import NiveauCanvas from './NiveauCanvas.jsx'
import PanneauMateriaux from './PanneauMateriaux.jsx'
import { estTutoriel } from './progression.js'
import Tutoriel from './Tutoriel.jsx'
import { avancer, estTermine, etapesTuto, etatInitial } from './tutoriel.js'
import { useTaille } from './useTaille.js'

export default function EcranNiveau({ id, materiaux, suivantId, onRetour, onVictoire, onChoisirNiveau }) {
  const [niveau, setNiveau] = useState(null)
  const [erreur, setErreur] = useState(null)
  const [liens, setLiens] = useState([])
  const [piece, setPiece] = useState(null) // pièce tenue : { materiauId, clientX, clientY }
  const [resultat, setResultat] = useState(null)
  const [animationFinie, setAnimationFinie] = useState(false)
  const [erreurSimulation, setErreurSimulation] = useState(null)
  const [enCours, setEnCours] = useState(false)
  const [avertissement, setAvertissement] = useState(null) // ex : lien refusé car trop long
  const [zone, setZone] = useState(null) // élément qui contient le canvas : sa taille donne l'échelle
  const espace = useTaille(zone)
  const [tuto, setTuto] = useState(etatInitial)
  const evenement = useCallback((nom) => setTuto((t) => avancer(t, nom)), [])

  useEffect(() => {
    let annule = false
    chargerNiveau(id)
      .then((n) => {
        if (annule) return
        setNiveau(n)
      })
      .catch((e) => !annule && setErreur(e))
    return () => {
      annule = true
    }
  }, [id])

  const parId = useMemo(() => Object.fromEntries(materiaux.map((m) => [m.id, m])), [materiaux])
  const cout = niveau ? coutTotal(liens, niveau, parId) : 0
  const hauteur = niveau ? hauteurAtteinte(liens, niveau) : 0

  const echelle = niveau && espace.hauteur > 0 ? calculerEchelle(niveau, espace) : PIXELS_PAR_METRE

  // À l'échec, après la secousse : le (ou les) premier lien cassé clignote sur le canvas.
  const surlignes = useMemo(
    () => (resultat && animationFinie && !resultat.estValide ? premiersLiensCasses(resultat.detailsLiens) : []),
    [resultat, animationFinie],
  )

  const finAnimation = useCallback(() => setAnimationFinie(true), [])

  // La victoire est enregistrée une seule fois, quand l'animation est terminée.
  useEffect(() => {
    if (animationFinie && resultat?.estValide) onVictoire(id, resultat.scoreSoliditeGlobal)
  }, [animationFinie, resultat, id, onVictoire])

  async function simuler() {
    setAvertissement(null)
    setErreurSimulation(null)
    setEnCours(true)
    evenement('simulationLancee')
    try {
      setAnimationFinie(false)
      setResultat(await simulerNiveau(id, liens))
    } catch (e) {
      setErreurSimulation(e)
    } finally {
      setEnCours(false)
    }
  }

  function recommencer() {
    setResultat(null)
    setAnimationFinie(false)
  }

  if (erreur) {
    return (
      <main>
        <MessageErreur erreur={erreur} />
        <button onClick={onRetour}>Retour à la carte</button>
      </main>
    )
  }
  if (!niveau) return <main>Chargement…</main>

  const budgetDepasse = cout > niveau.budgetMax
  const hauteurOk = hauteur >= niveau.hauteurMinRequise
  const etapeTuto = estTutoriel(niveau) && !estTermine(tuto) ? etapesTuto[tuto.index] : null

  return (
    <main className="jeu" data-tuto-actif={etapeTuto?.cible}>
      <header className="entete">
        <button onClick={onRetour}>← Carte</button>
        <h1>
          Niveau {niveau.numero} : {niveau.nom}
        </h1>
        <div className="indicateurs">
          <span data-tuto="budget" className={budgetDepasse ? 'mauvais' : ''}>
            Budget : {cout.toFixed(0)} / {niveau.budgetMax}
          </span>
          <span className={hauteurOk ? 'bon' : ''}>
            Hauteur : {hauteur.toFixed(1)} / {niveau.hauteurMinRequise} m
          </span>
        </div>
      </header>

      <div className="plateau">
        <PanneauMateriaux
          materiaux={materiaux}
          autorises={niveau.materiauxAutorises}
          onPrendre={(materiauId, e) => {
            if (resultat) return
            setPiece({ materiauId, clientX: e.clientX, clientY: e.clientY })
            evenement('materiauChoisi')
          }}
        />

        <div className="zone-canvas" data-tuto="zone-construction" ref={setZone}>
          <NiveauCanvas
            niveau={niveau}
            echelle={echelle}
            materiaux={parId}
            liens={liens}
            piece={piece}
            resultat={resultat}
            termine={animationFinie}
            surlignes={surlignes}
            onLien={(a, b, materiauId) => {
              setAvertissement(null)
              evenement('lienPose')
              setLiens((l) => ajouterLien(l, a, b, materiauId, niveau, parId[materiauId]))
            }}
            onPieceFinie={() => setPiece(null)}
            onRefus={setAvertissement}
            onSupprimer={(a, b) => {
              setAvertissement(null)
              setLiens((l) => supprimerLien(l, a, b))
            }}
            onFin={finAnimation}
          />
        </div>

        <section className="colonne-resultat">
          {!resultat && (
            <div className="actions">
              <button data-tuto="bouton-seisme" onClick={simuler} disabled={enCours || liens.length === 0}>
                Lancer le séisme
              </button>
              <button onClick={() => setLiens([])} disabled={liens.length === 0}>
                Tout effacer
              </button>
            </div>
          )}

          {avertissement && (
            <p role="status" className="erreur">
              {avertissement}
            </p>
          )}
          <MessageErreur erreur={erreurSimulation} />

          {resultat && animationFinie && (
            <div className={`bilan ${resultat.estValide ? 'gagne' : 'perdu'}`}>
              <h2>{resultat.estValide ? 'Niveau réussi !' : 'Niveau raté'}</h2>
              <p>{resultat.messageBilan}</p>
              {!resultat.estValide && phraseDuPremierLienCasse(resultat.detailsLiens, liens, parId) && (
                <p>{phraseDuPremierLienCasse(resultat.detailsLiens, liens, parId)}</p>
              )}
              {phraseMargeDeSecurite(resultat.detailsLiens) && <p>{phraseMargeDeSecurite(resultat.detailsLiens)}</p>}
              <p title={EXPLICATION_SOLIDITE}>
                Solidité : <span className="chiffre">{resultat.scoreSoliditeGlobal} / 100</span>
              </p>
              <p className="explication">{EXPLICATION_SOLIDITE}</p>
              <div className="actions">
                <button onClick={recommencer}>Modifier ma structure</button>
                {resultat.estValide && suivantId && (
                  <button onClick={() => onChoisirNiveau(suivantId)}>Niveau suivant →</button>
                )}
              </div>
              {resultat.estValide && !suivantId && <p>Tu as terminé tous les niveaux !</p>}
            </div>
          )}
        </section>
      </div>

      {etapeTuto && (
        <Tutoriel
          etape={etapeTuto}
          numero={tuto.index + 1}
          total={etapesTuto.length}
          onSuivant={() => evenement('suivant')}
          onPasser={() => setTuto({ index: etapesTuto.length, compte: 0 })}
        />
      )}
    </main>
  )
}
