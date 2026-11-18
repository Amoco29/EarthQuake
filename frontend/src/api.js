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

export async function chargerNiveau(id, fetchImpl = fetch) {
  let reponse
  try {
    reponse = await fetchImpl(`${URL_API}/api/niveaux/${encodeURIComponent(id)}`)
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
