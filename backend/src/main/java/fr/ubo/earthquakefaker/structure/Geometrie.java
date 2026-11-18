package fr.ubo.earthquakefaker.structure;

import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;

/** Calculs géométriques partagés (mètres). */
public final class Geometrie {

	private Geometrie() {
	}

	public static double longueur(Noeud a, Noeud b) {
		return Math.hypot(a.getPosX() - b.getPosX(), a.getPosY() - b.getPosY());
	}

	/**
	 * Plus longue barre autorisée : la diagonale d'une cellule de la grille, soit pas × √2. Le pas est la plus petite
	 * distance entre deux nœuds du niveau, ce qui évite de le dupliquer dans niveaux.json.
	 */
	public static double longueurMaxLien(Niveau niveau) {
		double pas = Double.MAX_VALUE;
		for (Noeud a : niveau.getNoeuds()) {
			for (Noeud b : niveau.getNoeuds()) {
				double distance = longueur(a, b);
				if (distance > 1e-9) {
					pas = Math.min(pas, distance);
				}
			}
		}
		return pas == Double.MAX_VALUE ? 0 : pas * Math.sqrt(2) + 1e-9;
	}

	/** Un nœud autre que les extrémités qui se trouve exactement sur le segment [a, b], ou {@code null}. */
	public static Noeud noeudTraverse(Niveau niveau, Noeud a, Noeud b) {
		double dx = b.getPosX() - a.getPosX();
		double dy = b.getPosY() - a.getPosY();
		double carre = dx * dx + dy * dy;
		for (Noeud n : niveau.getNoeuds()) {
			if (n.getId().equals(a.getId()) || n.getId().equals(b.getId())) {
				continue;
			}
			double px = n.getPosX() - a.getPosX();
			double py = n.getPosY() - a.getPosY();
			double alignement = Math.abs(dx * py - dy * px) / Math.sqrt(carre); // distance du nœud à la droite
			double position = (px * dx + py * dy) / carre; // 0 en a, 1 en b
			if (alignement < 1e-9 && position > 1e-9 && position < 1 - 1e-9) {
				return n;
			}
		}
		return null;
	}
}
