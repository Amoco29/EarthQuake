/** Bulle d'aide au-dessus de Félix (à droite de la structure). Sans contenu, rien ne s'affiche. */
export default function BulleFelix({ felix, gauche, espace, children }) {
  if (!children) return null
  const g = Math.min(Math.max(felix.x - 150, gauche), espace.largeur - 120)
  const largeur = Math.min(300, espace.largeur - 10 - g)
  const queue = Math.min(Math.max(felix.x - g, 24), largeur - 24)
  return (
    <div className="boite bulle-felix" style={{ left: g, width: largeur, bottom: espace.hauteur - (felix.sol - felix.taille - 16), '--queue': `${queue}px` }}>
      {children}
    </div>
  )
}
