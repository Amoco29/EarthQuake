import { describe, expect, it, vi } from 'vitest'
import { chargerMateriaux, chargerNiveau, chargerNiveaux, ErreurApi, simulerNiveau } from './api.js'

const reponse = (status, corps, json = true) => ({
  ok: status >= 200 && status < 300,
  status,
  json: json ? () => Promise.resolve(corps) : () => Promise.reject(new SyntaxError('pas du JSON')),
})

describe('chargerNiveau', () => {
  it('appelle la bonne URL et renvoie le niveau', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponse(200, { id: 1, nom: 'La prairie' }))

    const niveau = await chargerNiveau(1, fetchSimule)

    expect(fetchSimule).toHaveBeenCalledWith('http://localhost:8080/api/niveaux/1')
    expect(niveau.nom).toBe('La prairie')
  })

  it('transmet le code, le contexte et le message d\'une erreur du backend', async () => {
    const corps = { codeErreur: 'NIV-001', contexte: 'Niveau', message: 'Niveau 99 introuvable' }
    const fetchSimule = vi.fn().mockResolvedValue(reponse(404, corps))

    await expect(chargerNiveau(99, fetchSimule)).rejects.toMatchObject({
      name: 'ErreurApi',
      status: 404,
      codeErreur: 'NIV-001',
      contexte: 'Niveau',
      message: 'Niveau 99 introuvable',
    })
  })

  it('reste lisible quand l\'erreur n\'est pas du JSON (ex : 403 CORS)', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponse(403, null, false))

    const erreur = await chargerNiveau(1, fetchSimule).catch((e) => e)

    expect(erreur).toBeInstanceOf(ErreurApi)
    expect(erreur.status).toBe(403)
    expect(erreur.codeErreur).toBeNull()
    expect(erreur.message).toContain('403')
  })

  it('signale un backend injoignable', async () => {
    const fetchSimule = vi.fn().mockRejectedValue(new TypeError('Failed to fetch'))

    const erreur = await chargerNiveau(1, fetchSimule).catch((e) => e)

    expect(erreur).toBeInstanceOf(ErreurApi)
    expect(erreur.status).toBeNull()
    expect(erreur.message).toContain('injoignable')
  })

  it('encode l\'identifiant saisi', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponse(400, { codeErreur: 'NIV-003', message: 'x' }))

    await chargerNiveau('a/b', fetchSimule).catch(() => {})

    expect(fetchSimule).toHaveBeenCalledWith('http://localhost:8080/api/niveaux/a%2Fb')
  })
})

describe('autres appels', () => {
  it('liste les niveaux et les matériaux', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponse(200, []))

    await chargerNiveaux(fetchSimule)
    await chargerMateriaux(fetchSimule)

    expect(fetchSimule).toHaveBeenNthCalledWith(1, 'http://localhost:8080/api/niveaux')
    expect(fetchSimule).toHaveBeenNthCalledWith(2, 'http://localhost:8080/api/materiaux')
  })

  it('envoie les liens en POST JSON et renvoie le résultat', async () => {
    const fetchSimule = vi.fn().mockResolvedValue(reponse(200, { estValide: true }))
    const liens = [{ noeudDepartId: 1, noeudArriveeId: 4, materiauId: 'BOIS' }]

    const resultat = await simulerNiveau(1, liens, fetchSimule)

    const [url, options] = fetchSimule.mock.calls[0]
    expect(url).toBe('http://localhost:8080/api/niveaux/1/simuler')
    expect(options.method).toBe('POST')
    expect(options.headers['Content-Type']).toBe('application/json')
    expect(JSON.parse(options.body)).toEqual({ liens })
    expect(resultat.estValide).toBe(true)
  })

  it('remonte le code d\'erreur d\'une simulation refusée', async () => {
    const corps = { codeErreur: 'BUD-001', message: 'Budget dépassé' }
    const fetchSimule = vi.fn().mockResolvedValue(reponse(400, corps))

    await expect(simulerNiveau(1, [], fetchSimule)).rejects.toMatchObject({ status: 400, codeErreur: 'BUD-001' })
  })
})
