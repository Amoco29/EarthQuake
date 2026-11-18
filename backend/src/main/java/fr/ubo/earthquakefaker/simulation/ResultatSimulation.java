package fr.ubo.earthquakefaker.simulation;

import java.util.List;

/** Verdict d'un séisme sur une structure, avec le détail lien par lien pour l'animation côté React. */
public record ResultatSimulation(boolean estValide, double scoreSoliditeGlobal, String messageBilan,
		List<ResultatLien> detailsLiens) {

	public ResultatSimulation {
		detailsLiens = detailsLiens == null ? List.of() : List.copyOf(detailsLiens);
	}
}
