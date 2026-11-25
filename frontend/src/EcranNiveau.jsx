import { useCallback, useEffect, useMemo, useState } from 'react'
import { chargerNiveau, simulerNiveau } from './api.js'
import { conseilBudget, EXPLICATION_SOLIDITE, etoiles, lignesResultat, premiersLiensCasses } from './bilan.js'
import { ajouterLien, coutTotal, hauteurAtteinte, supprimerLien } from './construction.js'
import MessageErreur from './MessageErreur.jsx'
import { calculerVue } from './geometrie.js'
import Decor from './Decor.jsx'
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
  const [passe, setPasse] = useState([]) // états précédents de `liens`, pour annuler
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

  const vue = useMemo(() => (niveau && espace.hauteur > 0 ? calculerVue(niveau, espace) : null), [niveau, espace])

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

  // Toute modification des liens passe par ici : elle garde l'état précédent pour l'annulation.
  function modifierLiens(f) {
    setPasse((p) => [...p, liens])
    setLiens(f(liens))
  }

  function annuler() {
    if (passe.length === 0) return
    setLiens(passe[passe.length - 1])
    setPasse(passe.slice(0, -1))
  }

  // Ctrl+Z annule le dernier changement (hors séisme)
  useEffect(() => {
    if (resultat) return undefined
    const touche = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') {
        e.preventDefault()
        annuler()
      }
    }
    window.addEventListener('keydown', touche)
    return () => window.removeEventListener('keydown', touche)
  }) // sans dépendances : `annuler` voit toujours l'état courant

  function revoirTuto() {
    setTuto(etatInitial)
    setLiens([])
    setPasse([])
    recommencer()
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
    <main className="jeu" data-tuto-actif={etapeTuto?.cible} ref={setZone}>
      {vue && (
        <>
          <Decor espace={espace} sol={vue.origine.y + niveau.hauteurZone * vue.echelle} secousse={Boolean(resultat) && !animationFinie} />
          <NiveauCanvas
            niveau={niveau}
            echelle={vue.echelle}
            origine={vue.origine}
            espace={espace}
            materiaux={parId}
            liens={liens}
            piece={piece}
            resultat={resultat}
            termine={animationFinie}
            surlignes={surlignes}
            onLien={(a, b, materiauId) => {
              setAvertissement(null)
              evenement('lienPose')
              modifierLiens((l) => ajouterLien(l, a, b, materiauId, niveau, parId[materiauId]))
            }}
            onPieceFinie={() => setPiece(null)}
            onRefus={setAvertissement}
            onSupprimer={(a, b) => {
              setAvertissement(null)
              modifierLiens((l) => supprimerLien(l, a, b))
            }}
            onFin={finAnimation}
          />
        </>
      )}

      <header className="boite hud-titre">
        <button onClick={onRetour}>← Carte</button>
        <h1>
          Niveau {niveau.numero} : {niveau.nom}
        </h1>
        {estTutoriel(niveau) && !etapeTuto && <button onClick={revoirTuto}>Revoir le tutoriel</button>}
      </header>

      <div className="boite hud-indicateurs indicateurs">
        <span data-tuto="budget" className={budgetDepasse ? 'mauvais' : ''}>
          Budget : {cout.toFixed(0)} / {niveau.budgetMax}
        </span>
        <span className={hauteurOk ? 'bon' : ''}>
          Hauteur : {hauteur.toFixed(1)} / {niveau.hauteurMinRequise} m
        </span>
      </div>

      <div className="hud-bas">
        <PanneauMateriaux
          materiaux={materiaux}
          autorises={niveau.materiauxAutorises}
          onPrendre={(materiauId, e) => {
            if (resultat) return
            setPiece({ materiauId, clientX: e.clientX, clientY: e.clientY })
            evenement('materiauChoisi')
          }}
        />

        <div className="hud-messages">
          {etapeTuto && (
            <Tutoriel
              etape={etapeTuto}
              numero={tuto.index + 1}
              total={etapesTuto.length}
              onSuivant={() => evenement('suivant')}
              onPasser={() => setTuto({ index: etapesTuto.length, compte: 0 })}
            />
          )}
          {avertissement && (
            <p role="status" className="boite erreur">
              {avertissement}
            </p>
          )}
          {erreurSimulation && (
            <div className="boite">
              <MessageErreur erreur={erreurSimulation} />
              {erreurSimulation.codeErreur === 'BUD-001' && conseilBudget(cout, niveau.budgetMax) && (
                <p className="aide">{conseilBudget(cout, niveau.budgetMax)}</p>
              )}
            </div>
          )}
          {resultat && animationFinie && (
            <div className={`boite bilan ${resultat.estValide ? 'gagne' : 'perdu'}`}>
              {resultat.estValide && (
                <p className="etoiles" title={EXPLICATION_SOLIDITE} aria-label={`Solidité : ${etoiles(resultat.scoreSoliditeGlobal)} étoiles sur 3`}>
                  {[1, 2, 3].map((n) => (
                    <span key={n} className={n <= etoiles(resultat.scoreSoliditeGlobal) ? 'pleine' : 'vide'}>
                      ★
                    </span>
                  ))}
                </p>
              )}
              {lignesResultat({ resultat, niveau, liens, materiaux: parId, suivantExiste: Boolean(suivantId) }).map((ligne) => (
                <p key={ligne}>{ligne}</p>
              ))}
              <div className="actions">
                <button onClick={recommencer}>Réessayer</button>
                {resultat.estValide && suivantId && <button onClick={() => onChoisirNiveau(suivantId)}>Niveau suivant →</button>}
              </div>
            </div>
          )}
        </div>

        {!resultat && (
          <div className="boite actions hud-actions">
            <button data-tuto="bouton-seisme" onClick={simuler} disabled={enCours || liens.length === 0}>
              Lancer le séisme
            </button>
            <button onClick={annuler} disabled={passe.length === 0} title="Ctrl+Z">
              Annuler
            </button>
            <button onClick={() => modifierLiens(() => [])} disabled={liens.length === 0}>
              Tout effacer
            </button>
          </div>
        )}
      </div>
    </main>
  )
}
