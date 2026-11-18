import { describe, expect, it, vi } from 'vitest'
import { chargerNiveau, ErreurApi } from './api.js'

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
