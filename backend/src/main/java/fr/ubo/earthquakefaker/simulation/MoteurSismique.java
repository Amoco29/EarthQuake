package fr.ubo.earthquakefaker.simulation;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
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
		return bilan(details);
	}

	/**
	 * Score de solidité (0 à 100) : 100 moins la moyenne des taux de stress, chaque taux plafonné à 1.
	 * Tous les liens à vide donnent 100 ; tous au bord de la rupture, ou cassés, donnent 0.
	 */
	private ResultatSimulation bilan(List<ResultatLien> details) {
		double moyenne = details.stream().mapToDouble(r -> Math.min(r.tauxStress(), 1)).average().orElse(0);
		double score = Math.round(100 * (1 - moyenne) * 10) / 10.0;
		long casses = details.stream().filter(ResultatLien::estCasse).count();
		double pire = details.stream().mapToDouble(ResultatLien::tauxStress).max().orElse(0);

		if (casses == 0) {
			return new ResultatSimulation(true, score,
					"La structure a résisté au séisme. Lien le plus sollicité : %.0f %% de sa résistance.".formatted(pire * 100),
					details);
		}
		String message = pire >= CalculateurStress.TAUX_EFFONDREMENT
				? "La structure s'est effondrée : elle n'est pas assez rigide, ajoute des diagonales."
				: "%d lien(s) sur %d ont cassé. Renforce les liens les plus sollicités.".formatted(casses, details.size());
		return new ResultatSimulation(false, score, message, details);
	}
}
