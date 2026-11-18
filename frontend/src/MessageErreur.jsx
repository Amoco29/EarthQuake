export default function MessageErreur({ erreur }) {
  if (!erreur) return null
  return (
    <p role="alert" className="erreur">
      {erreur.codeErreur ? `[${erreur.codeErreur}] ` : ''}
      {erreur.message}
      {erreur.status ? ` (HTTP ${erreur.status})` : ''}
    </p>
  )
}
