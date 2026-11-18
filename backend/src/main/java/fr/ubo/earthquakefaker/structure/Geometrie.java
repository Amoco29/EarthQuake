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
}
