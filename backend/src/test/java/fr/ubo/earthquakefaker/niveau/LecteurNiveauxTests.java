package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

class LecteurNiveauxTests {

	private static final Set<String> MATERIAUX = Set.of("BOIS", "BETON", "ACIER", "AMORTISSEUR");

	private final LecteurNiveaux lecteur = new LecteurNiveaux(new ObjectMapper(), new ValidateurNiveaux());

	private List<Niveau> lireFichierDuJeu() throws IOException {
		try (InputStream flux = new ClassPathResource("niveaux.json").getInputStream()) {
			return lecteur.lire(flux, MATERIAUX);
		}
	}

	@Test
	void leFichierDuJeuContientSeptNiveauxValides() throws IOException {
		List<Niveau> niveaux = lireFichierDuJeu();

		assertThat(niveaux).extracting(Niveau::getId).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L);
		assertThat(niveaux).extracting(Niveau::getHauteurZone).containsExactly(10.0, 12.0, 12.0, 12.0, 15.0, 15.0, 15.0);
		assertThat(niveaux).extracting(Niveau::getHauteurMinRequise).containsExactly(5.0, 7.0, 9.0, 9.0, 10.0, 11.0, 12.0);
		assertThat(niveaux).allSatisfy(n -> {
			assertThat(n.getLargeurZone()).isEqualTo(5);
			assertThat(n.getNoeuds()).anyMatch(Noeud::isEstAncrageSol);
		});
	}

	@Test
	void lAmortisseurNEstDisponibleQuAuPort() throws IOException {
		List<Niveau> niveaux = lireFichierDuJeu();

		assertThat(niveaux.get(0).getMateriauxAutorises()).doesNotContain("AMORTISSEUR");
		assertThat(niveaux.get(3).getMateriauxAutorises()).contains("AMORTISSEUR");
	}

	@Test
	void lesNoeudsDuFichierDuJeuOntDesIdsUniquesSurTousLesNiveaux() throws IOException {
		List<Long> ids = lireFichierDuJeu().stream().flatMap(n -> n.getNoeuds().stream()).map(Noeud::getId).toList();

		assertThat(ids).doesNotHaveDuplicates().hasSize(12 + 15 + 15 + 14 + 17 + 18 + 16);
	}

	@Test
	void rejetteUnJsonMalForme() {
		InputStream flux = new ByteArrayInputStream("[ { \"id\": ".getBytes(StandardCharsets.UTF_8));

		assertThatThrownBy(() -> lecteur.lire(flux, MATERIAUX))
				.isInstanceOf(ExceptionMetier.class)
				.satisfies(e -> assertThat(((ExceptionMetier) e).getCodeErreur()).isEqualTo(CodeErreur.NIV_002))
				.hasMessageContaining("JSON illisible");
	}

	@Test
	void rejetteUnFichierValideSiUnMateriauNexistePasDansLeCatalogue() {
		assertThatThrownBy(() -> {
			try (InputStream flux = new ClassPathResource("niveaux.json").getInputStream()) {
				lecteur.lire(flux, Set.of("BOIS"));
			}
		}).isInstanceOf(ExceptionMetier.class).hasMessageContaining("matériau autorisé inconnu");
	}
}
