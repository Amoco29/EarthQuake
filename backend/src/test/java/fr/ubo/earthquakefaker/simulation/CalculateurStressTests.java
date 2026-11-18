package fr.ubo.earthquakefaker.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;

/**
 * Triangle : ancrages 1 (0,0) et 2 (5,0), sommet 3 (2,5 ; 5). Chaque barre mesure L = hypot(2,5 ; 5)
 * et monte avec un sinus de 5/L. Nœud 4 (0, 5) : pour les poteaux seuls et les pièces flottantes.
 */
class CalculateurStressTests {

	private static final double L = Math.hypot(2.5, 5);

	private final CalculateurCharges charges = new CalculateurCharges();
	private final CalculateurStress calculateur = new CalculateurStress();

	private Map<String, Materiau> catalogue(double traction, double compression) {
		return Map.of(
				"BOIS", new Materiau("BOIS", "Bois", "#8B5A2B", 10, 2, traction, compression, 1),
				"AMORTISSEUR", new Materiau("AMORTISSEUR", "Amortisseur", "#00AA00", 60, 2, traction, compression, 1));
	}

	private Niveau niveau(double magnitude) {
		Niveau niveau = new Niveau(1L, 1, "Test", "prairie", 1000, 5, magnitude, 5, 10);
		niveau.ajouterNoeud(new Noeud(1L, 0, 0, true));
		niveau.ajouterNoeud(new Noeud(2L, 5, 0, true));
		niveau.ajouterNoeud(new Noeud(3L, 2.5, 5, false));
		niveau.ajouterNoeud(new Noeud(4L, 0, 5, false));
		niveau.ajouterNoeud(new Noeud(5L, 5, 5, false));
		return niveau;
	}

	private List<ResultatLien> simuler(double magnitude, Map<String, Materiau> catalogue, Lien... liens) {
		Niveau niveau = niveau(magnitude);
		Structure structure = new Structure(List.of(liens));
		return calculateur.calculer(niveau, catalogue, structure, charges.calculer(niveau, catalogue, structure));
	}

	private Lien[] triangle(String materiau) {
		return new Lien[] { new Lien(1L, 3L, materiau), new Lien(2L, 3L, materiau) };
	}

	@Test
	void sansSeismeLesDeuxBarresDuTriangleSontEnCompressionEgale() {
		// Poids du sommet : m = L × 2 kg/m, W = m × g. Chaque barre porte W / (2 × sinus) = W × L / 10.
		double effort = 2 * L * 9.81 * L / 10;

		List<ResultatLien> resultats = simuler(0, catalogue(100, 0.2), triangle("BOIS"));

		assertThat(resultats).allSatisfy(r -> assertThat(r.tauxStress()).isCloseTo(effort / 200, within(1e-6)));
	}

	@Test
	void lePoidsComprimeLesBarresEtLeSeismeSurchargeUnCote() {
		List<ResultatLien> calme = simuler(0, catalogue(100, 100), triangle("BOIS"));
		List<ResultatLien> secoue = simuler(5, catalogue(100, 100), triangle("BOIS"));

		double maxCalme = calme.stream().mapToDouble(ResultatLien::tauxStress).max().orElseThrow();
		double maxSecoue = secoue.stream().mapToDouble(ResultatLien::tauxStress).max().orElseThrow();
		assertThat(maxSecoue).isGreaterThan(maxCalme);
	}

	@Test
	void lesDeuxSensDuSeismeDonnentLeMemeVerdictSurUneStructureSymetrique() {
		List<ResultatLien> resultats = simuler(4, catalogue(100, 100), triangle("BOIS"));

		assertThat(resultats.get(0).tauxStress()).isCloseTo(resultats.get(1).tauxStress(), within(1e-6));
	}

	@Test
	void unLienTropFaibleCasseAvecUnTempsDeRupture() {
		List<ResultatLien> resultats = simuler(3, catalogue(0.01, 0.01), triangle("BOIS"));

		assertThat(resultats).allSatisfy(r -> {
			assertThat(r.estCasse()).isTrue();
			assertThat(r.tauxStress()).isGreaterThan(1);
			assertThat(r.tempsRupture()).isCloseTo(10 / r.tauxStress(), within(1e-9));
		});
	}

	@Test
	void unLienSolideTientSansTempsDeRupture() {
		List<ResultatLien> resultats = simuler(3, catalogue(1000, 1000), triangle("BOIS"));

		assertThat(resultats).allSatisfy(r -> {
			assertThat(r.estCasse()).isFalse();
			assertThat(r.tempsRupture()).isNull();
		});
	}

	@Test
	void uneStructureInstableSEffondreEnEntier() {
		// Un poteau seul est un mécanisme : le sommet est libre de partir sur le côté.
		List<ResultatLien> resultats = simuler(3, catalogue(1000, 1000), new Lien(1L, 4L, "BOIS"));

		assertThat(resultats).singleElement().satisfies(r -> {
			assertThat(r.estCasse()).isTrue();
			assertThat(r.tauxStress()).isEqualTo(99);
		});
	}

	@Test
	void seulLaPartieInstableSEffondreEtLeResteGardeSesVraisTaux() {
		// Le triangle 1-2-3 est stable ; la barre 3-4 ne tient que par le sommet : le nœud 4 est libre, elle tombe.
		List<ResultatLien> resultats = simuler(3, catalogue(1000, 1000),
				new Lien(1L, 3L, "BOIS"), new Lien(2L, 3L, "BOIS"), new Lien(3L, 4L, "BOIS"));

		assertThat(resultats.get(0).estCasse()).isFalse();
		assertThat(resultats.get(0).tauxStress()).isBetween(0.0, 1.0);
		assertThat(resultats.get(1).estCasse()).isFalse();
		assertThat(resultats.get(2).estCasse()).isTrue();
		assertThat(resultats.get(2).tauxStress()).isEqualTo(99);
	}

	@Test
	void unCadreSansDiagonaleSEffondreEnEntier() {
		// Deux poteaux et une traverse : un parallélogramme articulé, sans rien pour le rigidifier.
		List<ResultatLien> resultats = simuler(3, catalogue(1000, 1000),
				new Lien(1L, 4L, "BOIS"), new Lien(2L, 5L, "BOIS"), new Lien(4L, 5L, "BOIS"));

		assertThat(resultats).allMatch(ResultatLien::estCasse);
	}

	@Test
	void uneLiaisonPosseeAuSommetNEstPasTouchePourUnePieceFlottante() {
		List<ResultatLien> resultats = simuler(3, catalogue(1000, 1000),
				new Lien(1L, 3L, "BOIS"), new Lien(2L, 3L, "BOIS"), new Lien(4L, 5L, "BOIS"));

		ResultatLien flottant = resultats.get(2);
		assertThat(flottant.tauxStress()).isZero();
		assertThat(flottant.estCasse()).isFalse();
	}

	@Test
	void lAmortisseurReduitLaPousseeSurSesNoeuds() {
		double avecBois = simuler(5, catalogue(100, 100), triangle("BOIS"))
				.stream().mapToDouble(ResultatLien::tauxStress).max().orElseThrow();
		double avecAmortisseur = simuler(5, catalogue(100, 100), triangle("AMORTISSEUR"))
				.stream().mapToDouble(ResultatLien::tauxStress).max().orElseThrow();

		assertThat(avecAmortisseur).isLessThan(avecBois);
	}
}
