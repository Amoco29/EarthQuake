package fr.ubo.earthquakefaker.simulation;

/**
 * Résultat de la simulation pour un lien.
 *
 * @param tauxStress  contrainte subie / résistance du matériau ; au-delà de 1 le lien casse
 * @param tempsRupture instant de la rupture en secondes depuis le début du séisme, pour l'animation ;
 *                     {@code null} si le lien tient
 */
public record ResultatLien(Long noeudDepartId, Long noeudArriveeId, double tauxStress, boolean estCasse,
		Double tempsRupture) {
}
