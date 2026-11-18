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
 * Mini-niveau sur une grille de 2,5 m : zone 5 x 10, hauteur requise 5, budget 100.
 * Ancrages 1 (0 ; 0) et 2 (2,5 ; 0) ; nœuds 3 (0 ; 2,5), 4 (2,5 ; 2,5), 5 (0 ; 5), 6 (2,5 ; 5).
 * Le bois coûte 10 le mètre : un lien de 2,5 m coûte 25. Longueur maximale d'un lien : 2,5 x racine de 2.
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
		niveau.ajouterNoeud(new Noeud(2L, 2.5, 0, true));
		niveau.ajouterNoeud(new Noeud(3L, 0, 2.5, false));
		niveau.ajouterNoeud(new Noeud(4L, 2.5, 2.5, false));
		niveau.ajouterNoeud(new Noeud(5L, 0, 5, false));
		niveau.ajouterNoeud(new Noeud(6L, 2.5, 5, false));
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
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 3L, "BOIS"), new Lien(3L, 5L, "BOIS")))).doesNotThrowAnyException();
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
	void refuseDeuxLiensSuperposesDeMateriauxDifferents() {
		assertCode(structure(new Lien(1L, 3L, "BOIS"), new Lien(1L, 3L, "ACIER")), CodeErreur.STR_003);
	}

	@Test
	void refuseUnLienTropLong() {
		// 1 (0 ; 0) à 6 (2,5 ; 5) : 5,6 m, et aucun nœud sur le trajet
		assertCode(structure(new Lien(1L, 6L, "BOIS")), CodeErreur.STR_005);
	}

	@Test
	void refuseUnLienQuiPasseSurUnNoeudIntermediaire() {
		// 1 (0 ; 0) à 5 (0 ; 5) traverse le nœud 3 (0 ; 2,5) : plus précis que « trop long »
		assertCode(structure(new Lien(1L, 5L, "BOIS")), CodeErreur.STR_006);
		assertCode(structure(new Lien(5L, 1L, "BOIS")), CodeErreur.STR_006);
	}

	@Test
	void unLienTropLongSansNoeudSurSonTrajetResteStr005() {
		// 1-6 passe entre les nœuds sans en toucher aucun : trop long, pas « traverse »
		assertCode(structure(new Lien(1L, 6L, "BOIS")), CodeErreur.STR_005);
	}

	@Test
	void refuseUnLienQuiSeSuperposeEnPartieAUnAutre() {
		assertCode(structure(new Lien(1L, 3L, "BOIS"), new Lien(1L, 5L, "BOIS")), CodeErreur.STR_006);
	}

	@Test
	void accepteLaDiagonaleDUneCellule() {
		// 1 à 4 : 3,54 m, exactement la longueur maximale ; 4 à 6 monte à 5 m
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 4L, "BOIS"), new Lien(4L, 6L, "BOIS")))).doesNotThrowAnyException();
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
		// deux liens d'acier de 2,5 m = 200 > 100
		assertCode(structure(new Lien(1L, 3L, "ACIER"), new Lien(3L, 5L, "ACIER")), CodeErreur.BUD_001);
	}

	@Test
	void accepteUnCoutExactementEgalAuBudget() {
		// quatre liens de bois de 2,5 m = 100
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 3L, "BOIS"), new Lien(3L, 5L, "BOIS"),
						new Lien(2L, 4L, "BOIS"), new Lien(4L, 6L, "BOIS")))).doesNotThrowAnyException();
	}

	@Test
	void refuseUneStructureSansAncrageRelie() {
		assertCode(structure(new Lien(3L, 4L, "BOIS")), CodeErreur.SIM_001);
	}

	@Test
	void refuseUneStructureTropBasse() {
		assertCode(structure(new Lien(1L, 3L, "BOIS")), CodeErreur.STR_004);
	}

	@Test
	void neCompteQueLaHauteurDesNoeudsReliesAuSol() {
		// 5-6 est à 5 m mais n'est pas relié au sol ; la partie reliée (1-3) reste trop basse.
		assertCode(structure(new Lien(1L, 3L, "BOIS"), new Lien(5L, 6L, "BOIS")), CodeErreur.STR_004);
	}

	@Test
	void accepteUnePieceFlottanteSiLeResteAtteintLaHauteur() {
		assertThatCode(() -> validateur.valider(niveau(), catalogue,
				structure(new Lien(1L, 3L, "BOIS"), new Lien(3L, 5L, "BOIS"), new Lien(4L, 6L, "BOIS"))))
				.doesNotThrowAnyException();
	}
}
