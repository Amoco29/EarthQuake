package fr.ubo.earthquakefaker.simulation;

import java.util.List;

import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;

/** Corps de POST /api/niveaux/{id}/simuler. Une liste absente est traitée comme une structure vide (STR-001). */
public record DemandeSimulation(List<Lien> liens) {

	Structure versStructure() {
		return new Structure(liens);
	}
}
