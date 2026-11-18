package fr.ubo.earthquakefaker.materiau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

class LecteurMateriauxTests {

	private final LecteurMateriaux lecteur = new LecteurMateriaux(new ObjectMapper());

	private static String materiau(String id, String couleur, String cout) {
		return """
				{ "id": "%s", "nom": "Test", "couleurHex": "%s", "coutUnitaire": %s, "masseLineique": 5,
				  "resistanceTractionMax": 20, "resistanceCompressionMax": 15, "moduleElasticite": 11, "longueurMax": 3 }
				""".formatted(id, couleur, cout);
	}

	private List<Materiau> lire(String json) {
		return lecteur.lire(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
	}

	private void assertRejete(String json, String fragmentMessage) {
		assertThatThrownBy(() -> lire(json))
				.isInstanceOf(ExceptionMetier.class)
				.satisfies(e -> assertThat(((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.NIV_004))
				.hasMessageContaining(fragmentMessage);
	}

	@Test
	void litUnFichierValide() {
		List<Materiau> lus = lire("[" + materiau("BOIS", "#8B5A2B", "10") + "," + materiau("ACIER", "#4A6FA5", "40") + "]");

		assertThat(lus).extracting(Materiau::getId).containsExactly("BOIS", "ACIER");
		assertThat(lus.get(0).getCoutUnitaire()).isEqualTo(10);
		assertThat(lus.get(0).getCouleurHex()).isEqualTo("#8B5A2B");
	}

	@Test
	void litLaLongueurMaximale() {
		assertThat(lire("[" + materiau("BOIS", "#8B5A2B", "10") + "]").get(0).getLongueurMax()).isEqualTo(3);
	}

	@Test
	void rejetteUneLongueurMaximaleAbsenteOuNonPositive() {
		String sans = materiau("BOIS", "#8B5A2B", "10").replace(", \"longueurMax\": 3", "");
		assertRejete("[" + sans + "]", "longueurMax manquant");
		assertRejete("[" + materiau("BOIS", "#8B5A2B", "10").replace("\"longueurMax\": 3", "\"longueurMax\": 0") + "]", "longueurMax doit être strictement positif");
	}

	@Test
	void rejetteUnJsonMalForme() {
		assertRejete("[ { \"id\": ", "JSON illisible");
	}

	@Test
	void rejetteUneListeVide() {
		assertRejete("[]", "aucun matériau");
	}

	@Test
	void rejetteUnIdEnDouble() {
		assertRejete("[" + materiau("BOIS", "#8B5A2B", "10") + "," + materiau("BOIS", "#8B5A2B", "12") + "]",
				"id en double : BOIS");
	}

	@Test
	void rejetteUneValeurNegative() {
		assertRejete("[" + materiau("BOIS", "#8B5A2B", "-1") + "]", "coutUnitaire");
	}

	@Test
	void rejetteUneValeurNulle() {
		assertRejete("[" + materiau("BOIS", "#8B5A2B", "0") + "]", "coutUnitaire");
	}

	@Test
	void rejetteUnChampManquant() {
		String sansMasse = """
				[ { "id": "BOIS", "nom": "Bois", "couleurHex": "#8B5A2B", "coutUnitaire": 10,
				    "resistanceTractionMax": 20, "resistanceCompressionMax": 15, "moduleElasticite": 11, "longueurMax": 3 } ]
				""";
		assertRejete(sansMasse, "masseLineique manquant");
	}

	@Test
	void rejetteUneCouleurInvalide() {
		assertRejete("[" + materiau("BOIS", "marron", "10") + "]", "couleurHex");
	}
}
