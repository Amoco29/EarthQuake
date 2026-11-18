package fr.ubo.earthquakefaker.structure;

import fr.ubo.earthquakefaker.niveau.Noeud;

/** Calculs géométriques partagés (mètres). */
public final class Geometrie {

	private Geometrie() {
	}

	public static double longueur(Noeud a, Noeud b) {
		return Math.hypot(a.getPosX() - b.getPosX(), a.getPosY() - b.getPosY());
	}
}
