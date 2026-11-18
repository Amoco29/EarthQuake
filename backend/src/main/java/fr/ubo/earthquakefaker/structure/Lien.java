package fr.ubo.earthquakefaker.structure;

/**
 * Un lien posé par le joueur entre deux nœuds. C'est une donnée d'entrée, immuable :
 * l'état de la simulation (stress, rupture) vit dans {@code ResultatLien}, pas ici.
 */
public record Lien(Long noeudDepartId, Long noeudArriveeId, String materiauId) {
}
