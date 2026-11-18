import { useEffect, useRef } from 'react'
import { dimensionsCanvas, metresVersPixels } from './geometrie.js'

const POLICE = "12px 'Pokemon', 'Press Start 2P', monospace"

function dessiner(ctx, niveau) {
  const { largeur, hauteur } = dimensionsCanvas(niveau)
  ctx.clearRect(0, 0, largeur, hauteur)

  // Zone de construction
  const coin = metresVersPixels(0, niveau.hauteurZone, niveau)
  const opposé = metresVersPixels(niveau.largeurZone, 0, niveau)
  ctx.fillStyle = '#eef6ff'
  ctx.fillRect(coin.x, coin.y, opposé.x - coin.x, opposé.y - coin.y)
  ctx.strokeStyle = '#888'
  ctx.strokeRect(coin.x, coin.y, opposé.x - coin.x, opposé.y - coin.y)

  // Ligne de hauteur à atteindre
  const gauche = metresVersPixels(0, niveau.hauteurMinRequise, niveau)
  const droite = metresVersPixels(niveau.largeurZone, niveau.hauteurMinRequise, niveau)
  ctx.strokeStyle = 'crimson'
  ctx.setLineDash([6, 4])
  ctx.beginPath()
  ctx.moveTo(gauche.x, gauche.y)
  ctx.lineTo(droite.x, droite.y)
  ctx.stroke()
  ctx.setLineDash([])
  ctx.fillStyle = 'crimson'
  ctx.font = POLICE
  ctx.fillText(`${niveau.hauteurMinRequise} m`, gauche.x + 4, gauche.y - 4)

  // Nœuds : cercle, ou triangle pour un ancrage au sol
  for (const noeud of niveau.noeuds) {
    const { x, y } = metresVersPixels(noeud.posX, noeud.posY, niveau)
    ctx.fillStyle = noeud.estAncrageSol ? '#444' : '#1b6ec2'
    ctx.beginPath()
    if (noeud.estAncrageSol) {
      ctx.moveTo(x, y - 9)
      ctx.lineTo(x - 9, y + 7)
      ctx.lineTo(x + 9, y + 7)
      ctx.closePath()
    } else {
      ctx.arc(x, y, 7, 0, Math.PI * 2)
    }
    ctx.fill()
  }
}

export default function NiveauCanvas({ niveau }) {
  const ref = useRef(null)
  const { largeur, hauteur } = dimensionsCanvas(niveau)

  useEffect(() => {
    let annule = false
    const ctx = ref.current.getContext('2d')
    // La police doit être chargée avant de dessiner du texte dans le canvas.
    const police = document.fonts ? document.fonts.load(POLICE) : Promise.resolve()
    police.catch(() => {}).then(() => {
      if (!annule) dessiner(ctx, niveau)
    })
    return () => {
      annule = true
    }
  }, [niveau])

  return <canvas ref={ref} width={largeur} height={hauteur} aria-label={`Niveau ${niveau.nom}`} />
}
