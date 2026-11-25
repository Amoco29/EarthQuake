// Texture de chaque matériau, dessinée par-dessus la couleur déjà posée. Partagée par les liens du canvas et l'étagère.
// Le repère est celui d'un lien : origine au début de la barre, rectangle de (0, -largeur/2) à (longueur, largeur/2).

/** Petit générateur déterministe : les grains du béton ne bougent pas d'une image à l'autre. */
function aleatoire(graine) {
  let etat = graine
  return () => {
    etat = (etat * 1664525 + 1013904223) % 4294967296
    return etat / 4294967296
  }
}

function texturerBois(ctx, longueur, largeur) {
  ctx.fillStyle = 'rgba(255, 255, 255, 0.18)'
  ctx.fillRect(0, -largeur / 2, longueur, largeur * 0.25)
  ctx.strokeStyle = 'rgba(70, 25, 5, 0.3)'
  ctx.lineWidth = 1
  for (const [y, debut, fin] of [[-0.12, 0.05, 0.6], [0.2, 0.3, 0.95], [0.02, 0.55, 0.9]]) {
    ctx.beginPath()
    ctx.moveTo(longueur * debut, largeur * y)
    ctx.quadraticCurveTo(longueur * ((debut + fin) / 2), largeur * (y + 0.14), longueur * fin, largeur * y)
    ctx.stroke()
  }
  ctx.fillStyle = 'rgba(70, 25, 5, 0.4)' // un nœud du bois
  ctx.beginPath()
  ctx.ellipse(longueur * 0.7, largeur * 0.1, largeur * 0.18, largeur * 0.1, 0, 0, Math.PI * 2)
  ctx.fill()
  contour(ctx, longueur, largeur, 'rgba(70, 25, 5, 0.6)')
}

function texturerBeton(ctx, longueur, largeur) {
  const hasard = aleatoire(7)
  const grains = Math.round((longueur * largeur) / 14)
  for (let i = 0; i < grains; i++) {
    ctx.fillStyle = hasard() < 0.5 ? 'rgba(0, 0, 0, 0.22)' : 'rgba(255, 255, 255, 0.28)'
    ctx.fillRect(hasard() * longueur, -largeur / 2 + hasard() * largeur, 1.5, 1.5)
  }
  contour(ctx, longueur, largeur, 'rgba(40, 40, 40, 0.7)')
}

function texturerAcier(ctx, longueur, largeur) {
  const degrade = ctx.createLinearGradient(0, -largeur / 2, 0, largeur / 2)
  degrade.addColorStop(0, 'rgba(255, 255, 255, 0.45)')
  degrade.addColorStop(0.45, 'rgba(255, 255, 255, 0)')
  degrade.addColorStop(1, 'rgba(0, 0, 0, 0.3)')
  ctx.fillStyle = degrade
  ctx.fillRect(0, -largeur / 2, longueur, largeur)
  const r = Math.max(1.2, largeur * 0.12)
  const rivets = Math.max(2, Math.round(longueur / (largeur * 2.2)))
  for (let i = 0; i < rivets; i++) {
    const x = largeur * 0.5 + (i * (longueur - largeur)) / (rivets - 1)
    ctx.fillStyle = 'rgba(30, 40, 50, 0.65)'
    ctx.beginPath()
    ctx.arc(x, 0, r, 0, Math.PI * 2)
    ctx.fill()
    ctx.fillStyle = 'rgba(255, 255, 255, 0.7)'
    ctx.beginPath()
    ctx.arc(x - r * 0.3, -r * 0.3, r * 0.4, 0, Math.PI * 2)
    ctx.fill()
  }
  contour(ctx, longueur, largeur, 'rgba(20, 30, 40, 0.7)')
}

function texturerAmortisseur(ctx, longueur, largeur) {
  const cap = Math.min(longueur * 0.15, largeur * 1.2)
  ctx.fillStyle = 'rgba(0, 0, 0, 0.25)' // embouts
  ctx.fillRect(0, -largeur / 2, cap, largeur)
  ctx.fillRect(longueur - cap, -largeur / 2, cap, largeur)
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.9)' // ressort en zigzag
  ctx.lineWidth = Math.max(1.5, largeur * 0.14)
  ctx.lineJoin = 'round'
  const spires = 7
  ctx.beginPath()
  ctx.moveTo(cap, 0)
  for (let i = 0; i < spires; i++) {
    const x = cap + ((i + 0.5) * (longueur - 2 * cap)) / spires
    ctx.lineTo(x, i % 2 === 0 ? -largeur * 0.36 : largeur * 0.36)
  }
  ctx.lineTo(longueur - cap, 0)
  ctx.stroke()
  contour(ctx, longueur, largeur, 'rgba(20, 20, 20, 0.7)')
}

function contour(ctx, longueur, largeur, couleur) {
  ctx.strokeStyle = couleur
  ctx.lineWidth = 1.5
  ctx.strokeRect(0, -largeur / 2, longueur, largeur)
}

const TEXTURES = { BOIS: texturerBois, BETON: texturerBeton, ACIER: texturerAcier, AMORTISSEUR: texturerAmortisseur }

/** Texture du matériau `materiauId` (sans effet pour un matériau inconnu : la couleur seule reste). */
export function texturer(ctx, materiauId, longueur, largeur) {
  TEXTURES[materiauId]?.(ctx, longueur, largeur)
}
