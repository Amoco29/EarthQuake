// Direct vers le port 8080 (pas de proxy Vite) pour tester vraiment le CORS du backend.
export const URL_API = 'http://localhost:8080'

export class ErreurApi extends Error {
  constructor({ status = null, codeErreur = null, contexte = null, message }) {
    super(message)
    this.name = 'ErreurApi'
    this.status = status
    this.codeErreur = codeErreur
    this.contexte = contexte
  }
}

async function appeler(chemin, options, fetchImpl) {
  let reponse
  try {
    const url = `${URL_API}${chemin}`
    reponse = await (options ? fetchImpl(url, options) : fetchImpl(url))
  } catch {
    throw new ErreurApi({
      message: `Backend injoignable sur ${URL_API} (lancé ? CORS refusé ?)`,
    })
  }

  if (reponse.ok) {
    return reponse.json()
  }

  // Les erreurs du backend sont en JSON { codeErreur, contexte, message } ; sinon (ex : 403 CORS) on reste générique.
  let corps = null
  try {
    corps = await reponse.json()
  } catch {
    // corps non JSON
  }
  throw new ErreurApi({
    status: reponse.status,
    codeErreur: corps?.codeErreur ?? null,
    contexte: corps?.contexte ?? null,
    message: corps?.message ?? `Réponse inattendue du backend (HTTP ${reponse.status})`,
  })
}

export const chargerNiveau = (id, fetchImpl = fetch) =>
  appeler(`/api/niveaux/${encodeURIComponent(id)}`, null, fetchImpl)

export const chargerNiveaux = (fetchImpl = fetch) => appeler('/api/niveaux', null, fetchImpl)

export const chargerMateriaux = (fetchImpl = fetch) => appeler('/api/materiaux', null, fetchImpl)

export const simulerNiveau = (id, liens, fetchImpl = fetch) =>
  appeler(
    `/api/niveaux/${encodeURIComponent(id)}/simuler`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ liens }),
    },
    fetchImpl,
  )
