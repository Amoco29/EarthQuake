package fr.ubo.earthquakefaker.materiau;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChargeurMateriauxTests {

	@Autowired
	private MateriauRepository materiaux;

	@Test
	void chargeLesQuatreMateriauxAuDemarrage() {
		assertThat(materiaux.findAll()).extracting(Materiau::getId)
				.containsExactlyInAnyOrder("BOIS", "BETON", "ACIER", "AMORTISSEUR");
	}

	@Test
	void lesProprietesDuFichierSontPersistees() {
		Materiau acier = materiaux.findById("ACIER").orElseThrow();

		assertThat(acier.getNom()).isEqualTo("Acier");
		assertThat(acier.getCouleurHex()).isEqualTo("#4A6FA5");
		assertThat(acier.getCoutUnitaire()).isEqualTo(40);
		assertThat(acier.getModuleElasticite()).isEqualTo(200);
	}
}
