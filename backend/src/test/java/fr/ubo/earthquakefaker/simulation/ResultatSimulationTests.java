package fr.ubo.earthquakefaker.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

class ResultatSimulationTests {

	@Test
	void unLienQuiTientNAPasDeTempsDeRupture() {
		ResultatLien lien = new ResultatLien(1L, 4L, 0.45, false, null);

		assertThat(lien.estCasse()).isFalse();
		assertThat(lien.tempsRupture()).isNull();
	}

	@Test
	void lesDetailsDesLiensSontImmuablesEtAbsentsDonnentUneListeVide() {
		ResultatSimulation resultat = new ResultatSimulation(false, 42, "Un lien a cassé",
				List.of(new ResultatLien(4L, 5L, 1.18, true, 3.2)));

		assertThat(resultat.detailsLiens()).hasSize(1);
		assertThatThrownBy(() -> resultat.detailsLiens().clear()).isInstanceOf(UnsupportedOperationException.class);
		assertThat(new ResultatSimulation(false, 0, "vide", null).detailsLiens()).isEmpty();
	}
}
