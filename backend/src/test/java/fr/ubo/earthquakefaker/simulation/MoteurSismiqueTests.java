package fr.ubo.earthquakefaker.simulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;
import fr.ubo.earthquakefaker.structure.ValidateurStructure;

/** Colonne contreventée de deux étages sur une grille de 2,5 m (ancrages 1 et 2, sommet à 5 m). Hauteur requise 5, budget 1000. */
class MoteurSismiqueTests {

	private final MoteurSismique moteur = new MoteurSismique(new ValidateurStructure(), new CalculateurCharges(),
			new CalculateurStress());

	private Map<String, Materiau> catalogue(double resistance) {
		return Map.of("BOIS", new Materiau("BOIS", "Bois", "#8B5A2B", 10, 2, resistance, resistance, 1));
	}

	private Niveau niveau() {
		Niveau niveau = new Niveau(1L, 1, "Test", "prairie", 1000, 5, 3, 5, 10);
		niveau.ajouterNoeud(new Noeud(1L, 0, 0, true));
		niveau.ajouterNoeud(new Noeud(2L, 2.5, 0, true));
		niveau.ajouterNoeud(new Noeud(3L, 0, 2.5, false));
		niveau.ajouterNoeud(new Noeud(4L, 2.5, 2.5, false));
		niveau.ajouterNoeud(new Noeud(5L, 0, 5, false));
		niveau.ajouterNoeud(new Noeud(6L, 2.5, 5, false));
		niveau.autoriserMateriau("BOIS");
		return niveau;
	}

	/** Deux cellules empilées, chacune avec ses deux poteaux, sa traverse et une diagonale : rigide. */
	private Structure tour() {
		return new Structure(List.of(
				new Lien(1L, 3L, "BOIS"), new Lien(2L, 4L, "BOIS"), new Lien(3L, 4L, "BOIS"), new Lien(1L, 4L, "BOIS"),
				new Lien(3L, 5L, "BOIS"), new Lien(4L, 6L, "BOIS"), new Lien(5L, 6L, "BOIS"), new Lien(3L, 6L, "BOIS")));
	}

	@Test
	void uneStructureSolideReussitAvecUnBonScore() {
		ResultatSimulation resultat = moteur.simulerSeisme(niveau(), catalogue(1000), tour());

		assertThat(resultat.estValide()).isTrue();
		assertThat(resultat.scoreSoliditeGlobal()).isBetween(90.0, 100.0);
		assertThat(resultat.messageBilan()).contains("résisté");
		assertThat(resultat.detailsLiens()).hasSize(8).noneMatch(ResultatLien::estCasse);
	}

	@Test
	void uneStructureFragileEstPerdueEtLesLiensCassesSontSignales() {
		ResultatSimulation resultat = moteur.simulerSeisme(niveau(), catalogue(0.01), tour());

		assertThat(resultat.estValide()).isFalse();
		assertThat(resultat.messageBilan()).contains("lien(s) sur 8");
		assertThat(resultat.detailsLiens()).anyMatch(ResultatLien::estCasse);
	}

	@Test
	void lEffondrementAUnMessageDedie() {
		// un poteau de deux segments sans contreventement : mécanisme
		ResultatSimulation resultat = moteur.simulerSeisme(niveau(), catalogue(1000),
				new Structure(List.of(new Lien(1L, 3L, "BOIS"), new Lien(3L, 5L, "BOIS"))));

		assertThat(resultat.estValide()).isFalse();
		assertThat(resultat.messageBilan()).contains("effondrée");
	}

	@Test
	void unePlusGrosseMarginDonneUnMeilleurScore() {
		double solide = moteur.simulerSeisme(niveau(), catalogue(1000), tour()).scoreSoliditeGlobal();
		double limite = moteur.simulerSeisme(niveau(), catalogue(0.5), tour()).scoreSoliditeGlobal();

		assertThat(solide).isGreaterThan(limite);
	}

	@Test
	void uneStructureQuiViolesLesReglesDeConstructionLeveUneErreurMetier() {
		assertThatThrownBy(() -> moteur.simulerSeisme(niveau(), catalogue(1000), new Structure(List.of())))
				.isInstanceOf(ExceptionMetier.class)
				.extracting(e -> ((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.STR_001);
	}
}
