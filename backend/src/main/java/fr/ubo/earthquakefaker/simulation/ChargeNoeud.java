package fr.ubo.earthquakefaker.simulation;

/**
 * Charge appliquée à un nœud pendant le séisme, en newtons.
 * {@code forceY} est négative (poids, vers le bas) ; {@code forceX} est la poussée latérale du séisme.
 */
public record ChargeNoeud(double masse, double forceX, double forceY) {
}
