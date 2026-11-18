package fr.ubo.earthquakefaker.structure;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;

/**
 * Mini-niveau : zone 5 x 10, hauteur requise 5, budget 100.
 * Ancrages 1 (0,0) et 2 (5,0) ; nœuds 3 (0,5), 4 (5,5), 5 (0,10), 6 (2,2) flottant.
 * Le bois coûte 10 le mètre : un lien vertical de 5 m coûte 50.
 */
class ValidateurStructureTests {

	private final ValidateurStructure validateur = new ValidateurStructure();

	private final Map<String, Materiau> catalogue = Map.of(
			"BOIS", new Materiau("BOIS", "Bois", "#8B5A2B", 10, 1, 1, 1, 1),
			"ACIER", new Materiau("ACIER", "Acier", "#888888", 40, 1, 1, 1, 1),
			"AMORTISSEUR", new Materiau("AMORTISSEUR", "Amortisseur", "#00AA00", 60, 1, 1, 1, 1));

	private Niveau niveau() {
		Niveau niveau = new Niveau(1L, 1, "Test", "prairie", 100, 5, 3, 5, 10);
		niveau.ajouterNoeud(new Noeud(1L, 0, 0, true));
		niveau.ajouterNoeud(new Noeud(2L, 5, 0, true));
		niveau.ajouterNoeud(new Noeud(3L, 0, 5, false));
		niveau.ajouterNoeud(new Noeud(4L, 5, 5, false));
		niveau.ajouterNoeud(new Noeud(5L, 0, 10, false));
		niveau.ajouterNoeud(new Noeud(6L, 2, 2, false));
		niveau.autoriserMateriau("BOIS");
		niveau.autoriserMateriau("ACIER");
		return niveau;
	}

	private Structure structure(Lien... liens) {
		return new Structure(Arrays.asList(liens));
	}

	private void assertCode(Structure structure, CodeErreur attendu) {
		assertThatThrownBy(() -> validateur.valider(niveau(), catalogue, structure))
				.isInstanceOf(ExceptionMetier.class)
				.extracting(e -> ((ExceptionMetier) e).getCodeErreur()).isEqualTo(attendu);
	}

	@Test
	void accepteUneStructureValide() {
		assertThatCode(() -> validateur.valider(niveau(), catalogue, structure(new Lien(1L, 3L, "BOIS")))).doesNotThrowAnyException();
	}

	@Test
	void refuseUneStructureVide() {
		assertCode(structure(), CodeErreur.STR_001);
	}

	@Test
	void refuseUnNoeudInconnuOuNul() {
		assertCode(structure(new Lien(1L, 99L, "BOIS")), CodeErreur.STR_002);
		assertCode(structure(new Lien(null, 3L, "BOIS")), CodeErreur.STR_002);
	}

	@Test
	void refuseUneBoucle() {
		assertCode(structure(new Lien(3L, 3L, "BOIS")), CodeErreur.STR_003);
	}

	@Test
	void refuseUnLienEnDoubleMemeDansLAutreSens() {
		assertCode(structure(new Lien(1L, 3L, "BOIS"), new Lien(3L, 1L, "BOIS")), CodeErreur.STR_003);
	}

	@Test
	void refuseUnMateriauInconnu() {
		assertCode(structure(new Lien(1L, 3L, "PLASTIQUE")), CodeErreur.MAT_001);
		assertCode(structure(new Lien(1L, 3L, null)), CodeErreur.MAT_001);
	}

	@Test
	void refuseUnMateriauVerrouille() {
		assertCode(structure(new Lien(1L, 3L, "AMORTISSEUR")), CodeErreur.MAT_002);
	}

	@Test
	void refuseUnBudgetDepasse() {
		// 5 m d'acier = 200 > 100
		assertCode(structure(new Lien(1L, 3L, "ACIER")), CodeErreur.BUD_001);
	}

	@Test
	void accepteUnCoutExactementEgalAuBudget() {
		// 2 liens de 5 m en bois = 100
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 3L, "BOIS"), new Lien(2L, 4L, "BOIS")))).doesNotThrowAnyException();
	}

	@Test
	void refuseUneStructureSansAncrageRelie() {
		assertCode(structure(new Lien(3L, 4L, "BOIS")), CodeErreur.SIM_001);
	}

	@Test
	void refuseUneStructureTropBasse() {
		assertCode(structure(new Lien(1L, 6L, "BOIS")), CodeErreur.STR_004);
	}

	@Test
	void neCompteQueLaHauteurDesNoeudsReliesAuSol() {
		// 3-5 monte à 10 m mais n'est pas relié au sol ; la partie reliée (1-6) reste trop basse.
		assertCode(structure(new Lien(1L, 6L, "BOIS"), new Lien(3L, 5L, "BOIS")), CodeErreur.STR_004);
	}

	@Test
	void accepteUnePieceFlottanteSiLeResteAtteintLaHauteur() {
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 3L, "BOIS"), new Lien(4L, 6L, "BOIS")))).doesNotThrowAnyException();
	}
}
