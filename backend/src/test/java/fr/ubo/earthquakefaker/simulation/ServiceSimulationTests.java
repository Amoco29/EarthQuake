package fr.ubo.earthquakefaker.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;

@SpringBootTest
class ServiceSimulationTests {

	@Autowired
	private ServiceSimulation service;

	@Test
	void simuleLaSolutionDeLaPrairieAvecLesDonneesDeH2() {
		// Mêmes liens que la solution de référence du niveau 1 (le verdict est réglé à la tâche 6).
		Structure solution = new Structure(List.of(
				new Lien(1L, 4L, "BOIS"), new Lien(2L, 5L, "BOIS"), new Lien(4L, 5L, "BOIS"), new Lien(1L, 5L, "BOIS"),
				new Lien(4L, 7L, "BOIS"), new Lien(5L, 8L, "BOIS"), new Lien(7L, 8L, "BOIS"), new Lien(5L, 7L, "BOIS")));

		ResultatSimulation resultat = service.simuler(1, solution);

		assertThat(resultat.detailsLiens()).hasSize(8);
		assertThat(resultat.messageBilan()).isNotBlank();
	}

	@Test
	void refuseUnNiveauInconnu() {
		assertThatThrownBy(() -> service.simuler(99, new Structure(List.of())))
				.isInstanceOf(ExceptionMetier.class)
				.extracting(e -> ((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.NIV_001);
	}

	@Test
	void refuseUneStructureQuiViolesLesReglesDuNiveau() {
		// Le poteau 1-4 n'atteint que 2,5 m sur les 5 requis.
		assertThatThrownBy(() -> service.simuler(1, new Structure(List.of(new Lien(1L, 4L, "BOIS")))))
				.isInstanceOf(ExceptionMetier.class)
				.extracting(e -> ((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.STR_004);
	}
}
