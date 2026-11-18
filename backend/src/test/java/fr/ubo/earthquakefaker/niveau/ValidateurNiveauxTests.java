package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

class ValidateurNiveauxTests {

	private static final Set<String> MATERIAUX = Set.of("BOIS", "BETON", "ACIER", "AMORTISSEUR");

	private final ValidateurNiveaux validateur = new ValidateurNiveaux();

	// --- jeux de données : un niveau valide (zone 5 x 10, hauteur requise 5), qu'on abîme règle par règle ---

	private static List<NoeudJson> noeudsValides(long base) {
		return List.of(
				new NoeudJson(base, 0.0, 0.0, true),
				new NoeudJson(base + 1, 5.0, 0.0, true),
				new NoeudJson(base + 2, 0.0, 5.0, false),
				new NoeudJson(base + 3, 5.0, 5.0, false));
	}

	private static NiveauJson niveau(long id, int numero, double largeur, double hauteurZone, double hauteurMin,
			List<String> materiaux, List<NoeudJson> noeuds) {
		return new NiveauJson(id, numero, "Test", "prairie", 400.0, hauteurMin, 3.0, largeur, hauteurZone,
				materiaux, noeuds);
	}

	private static NiveauJson niveauValide() {
		return niveau(1, 1, 5, 10, 5, List.of("BOIS", "BETON"), noeudsValides(1));
	}

	private static NiveauJson avecNoeuds(List<NoeudJson> noeuds) {
		return niveau(1, 1, 5, 10, 5, List.of("BOIS"), noeuds);
	}

	private void assertRejete(List<NiveauJson> niveaux, String fragmentMessage) {
		assertThatThrownBy(() -> validateur.valider(niveaux, MATERIAUX))
				.isInstanceOf(ExceptionMetier.class)
				.satisfies(e -> assertThat(((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.NIV_002))
				.hasMessageContaining(fragmentMessage);
	}

	// --- cas valide ---

	@Test
	void accepteDesNiveauxValides() {
		NiveauJson deux = niveau(2, 2, 5, 15, 9, List.of("ACIER", "AMORTISSEUR"), noeudsValides(101).stream()
				.map(n -> n.posY() == 5.0 ? new NoeudJson(n.id(), n.posX(), 10.0, false) : n).toList());

		assertThatCode(() -> validateur.valider(List.of(niveauValide(), deux), MATERIAUX)).doesNotThrowAnyException();
	}

	// --- règles du niveau ---

	@Test
	void rejetteUneListeVide() {
		assertRejete(List.of(), "aucun niveau");
	}

	@Test
	void rejetteUnIdDeNiveauEnDouble() {
		assertRejete(List.of(niveauValide(), niveau(1, 2, 5, 10, 5, List.of("BOIS"), noeudsValides(101))),
				"id de niveau en double");
	}

	@Test
	void rejetteUnNumeroEnDouble() {
		assertRejete(List.of(niveauValide(), niveau(2, 1, 5, 10, 5, List.of("BOIS"), noeudsValides(101))), "numero");
	}

	@Test
	void rejetteUneLargeurDifferenteDeCinq() {
		assertRejete(List.of(niveau(1, 1, 6, 10, 5, List.of("BOIS"), noeudsValides(1))), "largeurZone");
	}

	@Test
	void rejetteUneHauteurDeZoneHorsDeDixAQuinze() {
		assertRejete(List.of(niveau(1, 1, 5, 16, 5, List.of("BOIS"), noeudsValides(1))), "hauteurZone");
		assertRejete(List.of(niveau(1, 1, 5, 9, 5, List.of("BOIS"), noeudsValides(1))), "hauteurZone");
	}

	@Test
	void rejetteUneHauteurRequiseSuperieureALaZone() {
		assertRejete(List.of(niveau(1, 1, 5, 10, 11, List.of("BOIS"), noeudsValides(1))), "hauteurMinRequise");
	}

	@Test
	void rejetteUnBudgetNegatifOuAbsent() {
		NiveauJson sansBudget = new NiveauJson(1L, 1, "Test", "prairie", null, 5.0, 3.0, 5.0, 10.0,
				List.of("BOIS"), noeudsValides(1));
		assertRejete(List.of(sansBudget), "budgetMax");
	}

	// --- matériaux ---

	@Test
	void rejetteUnMateriauAutoriseInconnu() {
		assertRejete(List.of(niveau(1, 1, 5, 10, 5, List.of("BOIS", "PLASTIQUE"), noeudsValides(1))),
				"matériau autorisé inconnu : PLASTIQUE");
	}

	@Test
	void rejetteUneListeDeMateriauxVide() {
		assertRejete(List.of(niveau(1, 1, 5, 10, 5, List.of(), noeudsValides(1))), "aucun matériau autorisé");
	}

	// --- nœuds ---

	@Test
	void rejetteUnNoeudAuDelaDeLaLargeur() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, 5.1, 5.0, false)))), "posX 5.1 hors de la zone");
	}

	@Test
	void rejetteUnNoeudAuDessusDeLaHauteurDeZone() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, 2.5, 10.5, false)))), "posY 10.5 hors de la zone");
	}

	@Test
	void rejetteUnNoeudAvecUneCoordonneeNegative() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, -1.0, 5.0, false)))), "hors de la zone");
	}

	@Test
	void rejetteUnNiveauSansAncrage() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, false), new NoeudJson(2L, 5.0, 5.0, false)))), "aucun nœud d'ancrage");
	}

	@Test
	void rejetteUnAncrageEnHauteur() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, 5.0, 2.5, true)))), "posY = 0");
	}

	@Test
	void rejetteUnIdDeNoeudEnDoubleDansLeMemeNiveau() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(1L, 5.0, 5.0, false)))), "id de nœud en double");
	}

	@Test
	void rejetteUnIdDeNoeudEnDoubleEntreDeuxNiveaux() {
		NiveauJson autre = niveau(2, 2, 5, 10, 5, List.of("BOIS"), noeudsValides(1)); // mêmes ids 1 à 4
		assertRejete(List.of(niveauValide(), autre), "id de nœud en double");
	}

	@Test
	void rejetteDeuxNoeudsAlaMemePosition() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, 0.0, 0.0, false)))), "même position");
	}

	@Test
	void rejetteUnNiveauDontAucunNoeudAtteintLaHauteurRequise() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, 5.0, 4.9, false)))), "infaisable");
	}

	@Test
	void rejetteUnNoeudAvecUnChampManquant() {
		assertRejete(List.of(avecNoeuds(List.of(
				new NoeudJson(1L, 0.0, 0.0, true), new NoeudJson(2L, null, 5.0, false)))), "obligatoires");
	}

	@Test
	void rejetteUnNiveauSansNoeud() {
		assertRejete(List.of(avecNoeuds(List.of())), "aucun nœud défini");
	}
}
