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

/** Magnitude 3 → accélération = 3 × 0,1 × 9,81 = 2,943 m/s². Bois à 2 kg/m, acier à 10 kg/m. */
class CalculateurChargesTests {

	private final CalculateurCharges calculateur = new CalculateurCharges();

	private final Map<String, Materiau> catalogue = Map.of(
			"BOIS", new Materiau("BOIS", "Bois", "#8B5A2B", 10, 2, 1, 1, 1),
			"ACIER", new Materiau("ACIER", "Acier", "#888888", 40, 10, 1, 1, 1));

	private Niveau niveau(double magnitude) {
		Niveau niveau = new Niveau(1L, 1, "Test", "prairie", 1000, 5, magnitude, 5, 10);
		niveau.ajouterNoeud(new Noeud(1L, 0, 0, true));
		niveau.ajouterNoeud(new Noeud(2L, 0, 5, false));
		niveau.ajouterNoeud(new Noeud(3L, 0, 10, false));
		return niveau;
	}

	private Structure structure(Lien... liens) {
		return new Structure(List.of(liens));
	}

	@Test
	void repartitLaMasseDUnLienMoitieMoitieSurSesDeuxNoeuds() {
		Map<Long, ChargeNoeud> charges = calculateur.calculer(niveau(3), catalogue, structure(new Lien(1L, 2L, "BOIS")));

		// 5 m × 2 kg/m = 10 kg → 5 kg par nœud
		assertThat(charges.get(1L).masse()).isCloseTo(5, within(1e-9));
		assertThat(charges.get(2L).masse()).isCloseTo(5, within(1e-9));
	}

	@Test
	void unNoeudCumuleLaMoitieDeChaqueLienQuiLeTouche() {
		Map<Long, ChargeNoeud> charges = calculateur.calculer(niveau(3), catalogue,
				structure(new Lien(1L, 2L, "BOIS"), new Lien(2L, 3L, "ACIER")));

		// nœud 2 : 5 (bois) + 25 (acier : 5 m × 10 / 2)
		assertThat(charges.get(2L).masse()).isCloseTo(30, within(1e-9));
		assertThat(charges.get(3L).masse()).isCloseTo(25, within(1e-9));
	}

	@Test
	void appliqueLePoidsVersLeBasEtLaPousseeLateraleProportionnelleALaMasse() {
		ChargeNoeud charge = calculateur.calculer(niveau(3), catalogue, structure(new Lien(1L, 2L, "BOIS"))).get(2L);

		assertThat(charge.forceY()).isCloseTo(-5 * 9.81, within(1e-9));
		assertThat(charge.forceX()).isCloseTo(5 * 2.943, within(1e-9));
	}

	@Test
	void laPousseeAugmenteAvecLaMagnitude() {
		Structure s = structure(new Lien(1L, 2L, "BOIS"));

		double faible = calculateur.calculer(niveau(3), catalogue, s).get(2L).forceX();
		double forte = calculateur.calculer(niveau(6), catalogue, s).get(2L).forceX();

		assertThat(forte).isCloseTo(2 * faible, within(1e-9));
	}

	@Test
	void neChargeQueLesNoeudsTouchesParUnLien() {
		Map<Long, ChargeNoeud> charges = calculateur.calculer(niveau(3), catalogue, structure(new Lien(1L, 2L, "BOIS")));

		assertThat(charges).containsOnlyKeys(1L, 2L);
	}
}
