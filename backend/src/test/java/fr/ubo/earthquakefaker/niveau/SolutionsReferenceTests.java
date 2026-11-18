package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.materiau.MateriauRepository;
import fr.ubo.earthquakefaker.niveau.VerificateurSolution.LienJson;
import fr.ubo.earthquakefaker.niveau.VerificateurSolution.SolutionJson;

/**
 * Chaque niveau de niveaux.json a une solution de référence cohérente (règles de construction et budget).
 * La tenue au séisme de ces solutions sera vérifiée au jalon 2, avec le moteur.
 */
@SpringBootTest
@Transactional // Niveau.noeuds est LAZY
class SolutionsReferenceTests {

	@Autowired
	private NiveauRepository niveaux;

	@Autowired
	private MateriauRepository materiaux;

	@Autowired
	private ObjectMapper mapper;

	private List<SolutionJson> solutions() throws IOException {
		try (InputStream flux = new ClassPathResource("solutions-reference.json").getInputStream()) {
			return mapper.readValue(flux,
					mapper.getTypeFactory().constructCollectionType(List.class, SolutionJson.class));
		}
	}

	private Map<String, Materiau> catalogue() {
		return materiaux.findAll().stream().collect(Collectors.toMap(Materiau::getId, m -> m));
	}

	private Niveau niveau(long id) {
		return niveaux.findById(id).orElseThrow();
	}

	private List<LienJson> liensDe(long niveauId) throws IOException {
		return solutions().stream().filter(s -> s.niveauId() == niveauId).findFirst().orElseThrow().liens();
	}

	// --- les solutions du jeu ---

	@Test
	void chaqueNiveauAUneSolutionDeReference() throws IOException {
		List<Long> idsNiveaux = niveaux.findAll().stream().map(Niveau::getId).sorted().toList();
		List<Long> idsSolutions = solutions().stream().map(SolutionJson::niveauId).sorted().toList();

		assertThat(idsSolutions).isEqualTo(idsNiveaux).doesNotHaveDuplicates();
	}

	@Test
	void chaqueSolutionDeReferenceRespecteLesReglesDeConstruction() throws IOException {
		Map<String, Materiau> catalogue = catalogue();

		for (SolutionJson solution : solutions()) {
			List<String> problemes = VerificateurSolution.verifier(niveau(solution.niveauId()), catalogue, solution.liens());

			assertThat(problemes).as("niveau %d", solution.niveauId()).isEmpty();
		}
	}

	@Test
	void lesCoutsDesSolutionsDeReferenceSontCeuxDocumentes() throws IOException {
		Map<String, Materiau> catalogue = catalogue();

		assertThat(VerificateurSolution.cout(niveau(1), catalogue, liensDe(1))).isCloseTo(386.8, within(0.1));
		assertThat(VerificateurSolution.cout(niveau(2), catalogue, liensDe(2))).isCloseTo(542.7, within(0.1));
		assertThat(VerificateurSolution.cout(niveau(3), catalogue, liensDe(3))).isCloseTo(866.3, within(0.1));
	}

	// --- le contrôle échoue vraiment quand on casse une solution ---

	private List<String> verifierAvec(long niveauId, java.util.function.UnaryOperator<List<LienJson>> modification)
			throws IOException {
		List<LienJson> liens = modification.apply(new ArrayList<>(liensDe(niveauId)));
		return VerificateurSolution.verifier(niveau(niveauId), catalogue(), liens);
	}

	@Test
	void detecteUneStructureVide() throws IOException {
		assertThat(verifierAvec(1, l -> List.of())).anyMatch(p -> p.startsWith("STR-001"));
	}

	@Test
	void detecteUnNoeudDUnAutreNiveau() throws IOException {
		assertThat(verifierAvec(1, l -> {
			l.add(new LienJson(1L, 101L, "BOIS"));
			return l;
		})).anyMatch(p -> p.startsWith("STR-002"));
	}

	@Test
	void detecteUnLienEnDoubleEtUneBoucle() throws IOException {
		assertThat(verifierAvec(1, l -> {
			l.add(new LienJson(6L, 4L, "BOIS")); // même paire que 4-6, sens inverse
			l.add(new LienJson(5L, 5L, "BOIS"));
			return l;
		})).anyMatch(p -> p.contains("lien en double")).anyMatch(p -> p.contains("vers lui-même"));
	}

	@Test
	void detecteUnMateriauInconnuEtUnMateriauVerrouille() throws IOException {
		assertThat(verifierAvec(1, l -> {
			l.add(new LienJson(2L, 5L, "PLASTIQUE"));
			l.add(new LienJson(5L, 8L, "AMORTISSEUR")); // verrouillé au niveau 1
			return l;
		})).anyMatch(p -> p.startsWith("MAT-001")).anyMatch(p -> p.startsWith("MAT-002"));
	}

	@Test
	void detecteUnBudgetDepasse() throws IOException {
		assertThat(verifierAvec(1, l -> {
			l.add(new LienJson(2L, 5L, "ACIER")); // +100 : dépasse 400
			return l;
		})).anyMatch(p -> p.startsWith("BUD-001"));
	}

	@Test
	void detecteUnePieceFlottante() throws IOException {
		assertThat(verifierAvec(1, l -> {
			l.add(new LienJson(10L, 11L, "BOIS")); // deux nœuds du haut reliés entre eux, pas au sol
			return l;
		})).anyMatch(p -> p.startsWith("pièce flottante"));
	}

	@Test
	void detecteUneStructureTropBasseOuSansAncrage() throws IOException {
		assertThat(verifierAvec(1, l -> List.of(new LienJson(1L, 4L, "BOIS")))).anyMatch(p -> p.startsWith("STR-004"));
		assertThat(verifierAvec(1, l -> List.of(new LienJson(7L, 8L, "BOIS")))).anyMatch(p -> p.startsWith("SIM-001"));
	}
}
