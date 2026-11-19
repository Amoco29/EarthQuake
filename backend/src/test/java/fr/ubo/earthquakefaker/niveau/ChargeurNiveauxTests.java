package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

// Si ChargeurNiveaux passait avant ChargeurMateriaux, la validation rejetterait les matériaux et ce contexte
// ne démarrerait pas : ces tests prouvent donc aussi l'ordre de chargement.
@SpringBootTest
@Transactional // les collections de Niveau sont LAZY : il faut une session ouverte pour les lire
class ChargeurNiveauxTests {

	@Autowired
	private NiveauRepository niveaux;

	@Autowired
	private ChargeurNiveaux chargeur;

	@Test
	void chargeLesSeptNiveauxEtLeursNoeudsAuDemarrage() {
		assertThat(niveaux.count()).isEqualTo(7);
		assertThat(niveaux.findById(1L).orElseThrow().getNoeuds()).hasSize(12);
		assertThat(niveaux.findById(2L).orElseThrow().getNoeuds()).hasSize(15);
		assertThat(niveaux.findById(3L).orElseThrow().getNoeuds()).hasSize(15);
	}

	@Test
	void chargeLesProprietesEtLesMateriauxAutorises() {
		Niveau prairie = niveaux.findById(1L).orElseThrow();

		assertThat(prairie.getNom()).isEqualTo("La prairie");
		assertThat(prairie.getThemeDecor()).isEqualTo("prairie");
		assertThat(prairie.getBudgetMax()).isEqualTo(350);
		assertThat(prairie.getHauteurMinRequise()).isEqualTo(5);
		assertThat(prairie.getMateriauxAutorises()).containsExactly("BOIS");
	}

	@Test
	void unFichierInvalideLeveNiv002SansRienModifier() {
		String horsZone = """
				[ { "id": 9, "numero": 9, "nom": "X", "themeDecor": "x", "budgetMax": 1, "hauteurMinRequise": 5,
				    "magnitudeSeisme": 1, "largeurZone": 5, "hauteurZone": 10, "materiauxAutorises": ["BOIS"],
				    "noeuds": [ { "id": 900, "posX": 0, "posY": 0, "estAncrageSol": true },
				                { "id": 901, "posX": 9, "posY": 5, "estAncrageSol": false } ] } ]
				""";

		assertThatThrownBy(() -> chargeur.charger(new ByteArrayInputStream(horsZone.getBytes(StandardCharsets.UTF_8))))
				.isInstanceOf(ExceptionMetier.class)
				.satisfies(e -> assertThat(((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.NIV_002));
		assertThat(niveaux.count()).isEqualTo(7);
		assertThat(niveaux.findById(9L)).isEmpty();
	}
}
