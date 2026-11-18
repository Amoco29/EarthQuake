package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class NiveauRepositoryTests {

	@Autowired
	private NiveauRepository niveaux;

	@Autowired
	private TestEntityManager em;

	@Test
	void sauvegardeEtRelitUnNiveauAvecSesNoeuds() {
		Niveau niveau = new Niveau(1L, 1, "Prairie", "prairie", 250, 6, 3.5, 5, 10);
		niveau.ajouterNoeud(new Noeud(1L, 0, 0, true));
		niveau.ajouterNoeud(new Noeud(2L, 5, 0, true));
		niveau.ajouterNoeud(new Noeud(3L, 2.5, 5, false));
		niveaux.save(niveau);
		em.flush();
		em.clear(); // force une vraie relecture en base

		Niveau relu = niveaux.findById(1L).orElseThrow();

		assertThat(relu.getNom()).isEqualTo("Prairie");
		assertThat(relu.getThemeDecor()).isEqualTo("prairie");
		assertThat(relu.getBudgetMax()).isEqualTo(250);
		assertThat(relu.getHauteurMinRequise()).isEqualTo(6);
		assertThat(relu.getMagnitudeSeisme()).isEqualTo(3.5);
		assertThat(relu.getLargeurZone()).isEqualTo(5);
		assertThat(relu.getHauteurZone()).isEqualTo(10);
		assertThat(relu.getNoeuds()).hasSize(3);
		assertThat(relu.getNoeuds())
				.extracting(Noeud::getId, Noeud::getPosX, Noeud::getPosY, Noeud::isEstAncrageSol)
				.containsExactlyInAnyOrder(
						org.assertj.core.groups.Tuple.tuple(1L, 0.0, 0.0, true),
						org.assertj.core.groups.Tuple.tuple(2L, 5.0, 0.0, true),
						org.assertj.core.groups.Tuple.tuple(3L, 2.5, 5.0, false));
	}

	@Test
	void chaqueNoeudConnaitSonNiveau() {
		Niveau niveau = new Niveau(2L, 2, "Montagne", "montagne", 400, 8, 4.5, 5, 12);
		niveau.ajouterNoeud(new Noeud(10L, 0, 0, true));
		niveaux.save(niveau);
		em.flush();
		em.clear();

		Niveau relu = niveaux.findById(2L).orElseThrow();

		assertThat(relu.getNoeuds().get(0).getNiveau().getId()).isEqualTo(2L);
	}

	@Test
	void conserveLesMateriauxAutorises() {
		Niveau niveau = new Niveau(3L, 3, "Ville", "ville", 900, 9, 6, 5, 15);
		niveau.ajouterNoeud(new Noeud(20L, 0, 0, true));
		niveau.autoriserMateriau("BOIS");
		niveau.autoriserMateriau("ACIER");
		niveaux.save(niveau);
		em.flush();
		em.clear();

		assertThat(niveaux.findById(3L).orElseThrow().getMateriauxAutorises()).containsExactlyInAnyOrder("BOIS", "ACIER");
	}

	@Test
	void idInexistantRenvoieVide() {
		assertThat(niveaux.findById(99L)).isEmpty();
	}
}
