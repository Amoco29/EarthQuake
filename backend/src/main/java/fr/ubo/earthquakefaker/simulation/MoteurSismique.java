package fr.ubo.earthquakefaker.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;
import fr.ubo.earthquakefaker.structure.ValidateurStructure;

/**
 * Orchestre une simulation : règles de construction, charges, stress par lien, puis bilan.
 * Une structure qui viole une règle de construction lève une {@code ExceptionMetier} (erreur de requête) ;
 * une structure qui casse pendant le séisme est un résultat normal, avec {@code estValide = false}.
 */
@Component
public class MoteurSismique {

	private final ValidateurStructure validateur;
	private final CalculateurCharges calculateurCharges;
	private final CalculateurStress calculateurStress;

	public MoteurSismique(ValidateurStructure validateur, CalculateurCharges calculateurCharges,
			CalculateurStress calculateurStress) {
		this.validateur = validateur;
		this.calculateurCharges = calculateurCharges;
		this.calculateurStress = calculateurStress;
	}

	public ResultatSimulation simulerSeisme(Niveau niveau, Map<String, Materiau> catalogue, Structure structure) {
		validateur.valider(niveau, catalogue, structure);
		Map<Long, ChargeNoeud> charges = calculateurCharges.calculer(niveau, catalogue, structure);
		List<ResultatLien> details = calculateurStress.calculer(niveau, catalogue, structure, charges);
		return bilan(details, hauteurRestante(niveau, structure, details), niveau.getHauteurMinRequise());
	}

	/**
	 * Score de solidité (0 à 100) : 100 moins la moyenne des taux de stress, chaque taux plafonné à 1.
	 * Tous les liens à vide donnent 100 ; tous au bord de la rupture, ou cassés, donnent 0.
	 */
	private ResultatSimulation bilan(List<ResultatLien> details, double hauteurRestante, double hauteurRequise) {
		double moyenne = details.stream().mapToDouble(r -> Math.min(r.tauxStress(), 1)).average().orElse(0);
		double score = Math.round(100 * (1 - moyenne) * 10) / 10.0;
		long casses = details.stream().filter(ResultatLien::estCasse).count();
		double pire = details.stream().mapToDouble(ResultatLien::tauxStress).max().orElse(0);

		// La hauteur ne compte que si elle tient pendant le séisme : on la mesure avec les liens intacts reliés au sol.
		boolean hauteurTenue = hauteurRestante >= hauteurRequise;
		if (casses == 0 && hauteurTenue) {
			return new ResultatSimulation(true, score,
					"La structure a résisté au séisme. Lien le plus sollicité : %.0f %% de sa résistance.".formatted(pire * 100),
					details);
		}
		long effondres = details.stream().filter(r -> r.tauxStress() >= CalculateurStress.TAUX_EFFONDREMENT).count();
		String message;
		if (effondres == details.size()) {
			message = "La structure s'est effondrée : elle n'est pas assez rigide, forme des triangles avec des diagonales.";
		} else if (effondres > 0) {
			message = "%d lien(s) sur %d se sont effondrés : cette partie n'est pas assez rigide, ajoute des diagonales."
					.formatted(effondres, details.size());
		} else if (casses > 0) {
			message = "%d lien(s) sur %d ont cassé. Renforce les liens les plus sollicités.".formatted(casses, details.size());
		} else {
			message = "Aucun lien n'a cassé, mais la structure n'atteint pas la hauteur demandée.";
		}
		if (!hauteurTenue) {
			message += " Il ne reste que %.1f m de structure reliée au sol sur %.1f m requis.".formatted(hauteurRestante, hauteurRequise);
		}
		return new ResultatSimulation(false, score, message, details);
	}

	/** Hauteur du nœud le plus haut encore relié au sol par des liens qui n'ont pas cassé. */
	private double hauteurRestante(Niveau niveau, Structure structure, List<ResultatLien> details) {
		Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
		Map<Long, Set<Long>> voisins = new HashMap<>();
		for (int i = 0; i < structure.liens().size(); i++) {
			if (!details.get(i).estCasse()) {
				Lien lien = structure.liens().get(i);
				voisins.computeIfAbsent(lien.noeudDepartId(), k -> new HashSet<>()).add(lien.noeudArriveeId());
				voisins.computeIfAbsent(lien.noeudArriveeId(), k -> new HashSet<>()).add(lien.noeudDepartId());
			}
		}
		return ValidateurStructure.reliesAuSol(noeuds, voisins).stream()
				.mapToDouble(id -> noeuds.get(id).getPosY()).max().orElse(0);
	}
}
