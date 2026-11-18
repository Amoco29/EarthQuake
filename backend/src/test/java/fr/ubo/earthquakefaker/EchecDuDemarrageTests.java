package fr.ubo.earthquakefaker;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Un fichier de données invalide doit empêcher l'application de démarrer, avec le code d'erreur dans le message. */
class EchecDuDemarrageTests {

	private static SpringApplicationBuilder application(String... proprietes) {
		return new SpringApplicationBuilder(EarthQuakeFakerApplication.class)
				.web(WebApplicationType.NONE)
				// base à part : une base H2 nommée est partagée dans la JVM avec les autres contextes de test
				.properties("spring.datasource.url=jdbc:h2:mem:echec-demarrage")
				.properties(proprietes);
	}

	@Test
	void niveauxInvalidesEmpechentLeDemarrageAvecNiv002() {
		assertThatThrownBy(() -> application(
				"earthquakefaker.niveaux=classpath:donnees-invalides/niveaux-hors-zone.json").run())
				.hasStackTraceContaining("NIV-002")
				.hasStackTraceContaining("posX 7.0 hors de la zone");
	}

	@Test
	void materiauxInvalidesEmpechentLeDemarrageAvecNiv004() {
		assertThatThrownBy(() -> application(
				"earthquakefaker.materiaux=classpath:donnees-invalides/materiaux-negatif.json").run())
				.hasStackTraceContaining("NIV-004")
				.hasStackTraceContaining("coutUnitaire");
	}

	@Test
	void fichierIntrouvableEmpecheLeDemarrage() {
		assertThatThrownBy(() -> application("earthquakefaker.niveaux=classpath:n-existe-pas.json").run())
				.hasStackTraceContaining("NIV-002");
	}
}
