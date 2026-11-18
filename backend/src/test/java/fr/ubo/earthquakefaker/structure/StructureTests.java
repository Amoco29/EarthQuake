package fr.ubo.earthquakefaker.structure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class StructureTests {

	@Test
	void uneListeAbsenteDonneUneStructureVide() {
		assertThat(new Structure(null).liens()).isEmpty();
	}

	@Test
	void laStructureNeSuitPasLaListeDOrigine() {
		List<Lien> origine = new ArrayList<>(List.of(new Lien(1L, 4L, "BOIS")));
		Structure structure = new Structure(origine);

		origine.add(new Lien(4L, 7L, "BOIS"));

		assertThat(structure.liens()).hasSize(1);
	}

	@Test
	void lesLiensDeLaStructureSontImmuables() {
		Structure structure = new Structure(List.of(new Lien(1L, 4L, "BOIS")));

		assertThatThrownBy(() -> structure.liens().add(new Lien(4L, 7L, "BOIS")))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
