// Félix Fixe Junior : dessiné avec des formes, trois états ('inquiet', 'content', 'decourage').

const POSITIONS = { ecartMetres: 1.7, hauteurMetres: 2.2, tailleMax: 150 }

/** Où se tient Félix : à droite de la zone, les pieds sur le sol. Renvoie { x, sol, taille } en pixels. */
export function positionFelix(niveau, echelle, origine, espace) {
  const taille = Math.min(POSITIONS.tailleMax, POSITIONS.hauteurMetres * echelle)
  const x = origine.x + (niveau.largeurZone + POSITIONS.ecartMetres) * echelle
  return { x: Math.min(x, espace.largeur - taille / 2 - 10), sol: origine.y + niveau.hauteurZone * echelle, taille }
}

function membre(ctx, x1, y1, x2, y2, epaisseur, couleur) {
  ctx.strokeStyle = couleur
  ctx.lineWidth = epaisseur
  ctx.lineCap = 'round'
  ctx.beginPath()
  ctx.moveTo(x1, y1)
  ctx.lineTo(x2, y2)
  ctx.stroke()
}

/** `temps` (secondes) anime le tremblement pendant le séisme ; 0 hors séisme. */
export function dessinerFelix(ctx, { x, sol, taille }, etat, temps = 0) {
  const u = taille / 10
  const tremble = etat === 'inquiet' && temps > 0 ? Math.sin(temps * 40) * 0.12 * u : 0
  const saut = etat === 'content' ? -Math.abs(Math.sin(temps * 6)) * 0.4 * u : 0
  const affaisse = etat === 'decourage' ? 0.5 * u : 0
  ctx.save()
  ctx.translate(x + tremble, sol + saut)
  const peau = '#f2c79a'
  const gilet = etat === 'decourage' ? '#c9893f' : '#ff8c1a'

  membre(ctx, -0.9 * u, -3 * u, -0.9 * u, 0, 1.3 * u, '#26358a') // jambes
  membre(ctx, 0.9 * u, -3 * u, 0.9 * u, 0, 1.3 * u, '#26358a')
  ctx.fillStyle = '#222'
  ctx.fillRect(-1.7 * u, -0.4 * u, 1.6 * u, 0.5 * u) // chaussures
  ctx.fillRect(0.1 * u, -0.4 * u, 1.6 * u, 0.5 * u)

  ctx.fillStyle = gilet // corps et bande réfléchissante
  ctx.beginPath()
  ctx.roundRect(-2 * u, -6.4 * u + affaisse, 4 * u, 3.6 * u, 0.6 * u)
  ctx.fill()
  ctx.fillStyle = '#f4f1c4'
  ctx.fillRect(-2 * u, -4.9 * u + affaisse, 4 * u, 0.5 * u)

  // bras : en l'air quand il est content, devant lui et tremblants quand il est inquiet, ballants quand il est découragé
  const bras = { content: [-3.4, -8.4, 3.4, -8.4], inquiet: [-3.2, -6.2, 3.2, -6.2], decourage: [-2.6, -3.4, 2.6, -3.4] }[etat]
  membre(ctx, -2 * u, -6 * u + affaisse, bras[0] * u, bras[1] * u + affaisse, 1 * u, gilet)
  membre(ctx, 2 * u, -6 * u + affaisse, bras[2] * u, bras[3] * u + affaisse, 1 * u, gilet)
  ctx.fillStyle = peau
  for (const [bx, by] of [[bras[0], bras[1]], [bras[2], bras[3]]]) {
    ctx.beginPath()
    ctx.arc(bx * u, by * u + affaisse, 0.65 * u, 0, Math.PI * 2)
    ctx.fill()
  }

  const tete = { x: 0, y: -8 * u + affaisse * 1.6 }
  ctx.fillStyle = peau
  ctx.beginPath()
  ctx.arc(tete.x, tete.y, 1.7 * u, 0, Math.PI * 2)
  ctx.fill()
  ctx.fillStyle = '#ffd400' // casque de chantier
  ctx.beginPath()
  ctx.arc(tete.x, tete.y - 0.2 * u, 1.85 * u, Math.PI, 0)
  ctx.fill()
  ctx.fillRect(tete.x - 2.3 * u, tete.y - 0.3 * u, 4.6 * u, 0.4 * u)

  ctx.fillStyle = '#222' // yeux
  const yeuxY = tete.y + 0.3 * u + affaisse * 0.2
  const rayonYeux = etat === 'inquiet' ? 0.3 * u : 0.22 * u
  for (const sx of [-0.65, 0.65]) {
    ctx.beginPath()
    ctx.arc(tete.x + sx * u, yeuxY, rayonYeux, 0, Math.PI * 2)
    ctx.fill()
  }
  ctx.strokeStyle = '#222'
  ctx.lineWidth = Math.max(1.5, 0.18 * u)
  ctx.lineCap = 'round'
  ctx.beginPath() // sourcils : relevés au milieu quand il s'inquiète, tombants quand il est découragé
  const pente = etat === 'inquiet' ? -0.35 : etat === 'decourage' ? 0.3 : 0
  for (const s of [-1, 1]) {
    ctx.moveTo(tete.x + s * 0.25 * u, yeuxY - (0.55 + pente * 0.4) * u)
    ctx.lineTo(tete.x + s * 1.05 * u, yeuxY - (0.55 - pente * 0.4) * u)
  }
  ctx.stroke()
  ctx.beginPath() // bouche
  const boucheY = tete.y + 1.05 * u + affaisse * 0.2
  if (etat === 'content') ctx.arc(tete.x, boucheY - 0.2 * u, 0.75 * u, 0.15 * Math.PI, 0.85 * Math.PI)
  else if (etat === 'decourage') ctx.arc(tete.x, boucheY + 0.5 * u, 0.6 * u, 1.15 * Math.PI, 1.85 * Math.PI)
  else ctx.ellipse(tete.x, boucheY, 0.35 * u, 0.25 * u, 0, 0, Math.PI * 2)
  ctx.stroke()
  if (etat !== 'content') { // goutte : sueur ou larme
    ctx.fillStyle = '#7fc8ff'
    ctx.beginPath()
    ctx.ellipse(tete.x + 1.9 * u, tete.y - 0.2 * u + (etat === 'decourage' ? 1 * u : 0), 0.28 * u, 0.45 * u, 0, 0, Math.PI * 2)
    ctx.fill()
  }
  ctx.restore()
}
