package fr.ubo.earthquakefaker.simulation;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;
import fr.ubo.earthquakefaker.structure.Geometrie;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;

/**
 * Calcul statique simplifié : on ramène la masse de chaque lien sur ses deux nœuds (moitié chacun),
 * puis chaque nœud subit son poids et une poussée latérale proportionnelle à sa masse (F = m × a).
 * Plus un nœud porte de matière, plus il est secoué : c'est ce qui rend les étages hauts et lourds risqués.
 * Suppose une structure déjà validée par {@code ValidateurStructure}.
 */
@Component
public class CalculateurCharges {

	static final double GRAVITE = 9.81;

	/** L'accélération du séisme vaut magnitude × ce facteur × g (magnitude 6 → 0,6 g). Valeur de jeu, à régler. */
	static final double FACTEUR_SISMIQUE = 0.1;

	public Map<Long, ChargeNoeud> calculer(Niveau niveau, Map<String, Materiau> catalogue, Structure structure) {
		Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
		double acceleration = niveau.getMagnitudeSeisme() * FACTEUR_SISMIQUE * GRAVITE;

		Map<Long, Double> masses = new HashMap<>();
		for (Lien lien : structure.liens()) {
			Noeud depart = noeuds.get(lien.noeudDepartId());
			Noeud arrivee = noeuds.get(lien.noeudArriveeId());
			double moitie = catalogue.get(lien.materiauId()).getMasseLineique() * Geometrie.longueur(depart, arrivee) / 2;
			masses.merge(depart.getId(), moitie, Double::sum);
			masses.merge(arrivee.getId(), moitie, Double::sum);
		}

		Map<Long, ChargeNoeud> charges = new HashMap<>();
		masses.forEach((id, masse) -> charges.put(id, new ChargeNoeud(masse, masse * acceleration, -masse * GRAVITE)));
		return charges;
	}
}
