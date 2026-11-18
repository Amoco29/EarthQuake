package fr.ubo.earthquakefaker.niveau;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
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
import fr.ubo.earthquakefaker.simulation.ResultatLien;
import fr.ubo.earthquakefaker.simulation.ResultatSimulation;
import fr.ubo.earthquakefaker.simulation.ServiceSimulation;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;

/**
 * Chaque niveau de niveaux.json a une solution de référence, passée dans le vrai moteur sismique.
 * Les règles de construction (budget, matériaux, ancrage, hauteur) sont celles de ValidateurStructure :
 * une solution qui les viole fait échouer la simulation avec le code d'erreur correspondant.
 */
@SpringBootTest
@Transactional // Niveau.noeuds est LAZY
class SolutionsReferenceTests {

	record LienJson(Long noeudDepartId, Long noeudArriveeId, String materiauId) {
	}

	record SolutionJson(Long niveauId, String description, List<LienJson> liens) {
	}

	@Autowired
	private NiveauRepository niveaux;

	@Autowired
	private MateriauRepository materiaux;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceSimulation simulation;

	private List<SolutionJson> solutions() throws IOException {
		try (InputStream flux = new ClassPathResource("solutions-reference.json").getInputStream()) {
			return mapper.readValue(flux,
					mapper.getTypeFactory().constructCollectionType(List.class, SolutionJson.class));
		}
	}

	private static Structure structure(List<LienJson> liens) {
		return new Structure(liens.stream().map(l -> new Lien(l.noeudDepartId(), l.noeudArriveeId(), l.materiauId())).toList());
	}

	@Test
	void chaqueNiveauAUneSolutionDeReference() throws IOException {
		List<Long> idsNiveaux = niveaux.findAll().stream().map(Niveau::getId).sorted().toList();
		List<Long> idsSolutions = solutions().stream().map(SolutionJson::niveauId).sorted().toList();

		assertThat(idsSolutions).isEqualTo(idsNiveaux).doesNotHaveDuplicates();
	}

	@Test
	void chaqueSolutionDeReferenceResisteAuSeisme() throws IOException {
		for (SolutionJson solution : solutions()) {
			ResultatSimulation resultat = simulation.simuler(solution.niveauId(), structure(solution.liens()));

			assertThat(resultat.estValide()).as("niveau %d : %s", solution.niveauId(), resultat.messageBilan()).isTrue();
			assertThat(resultat.detailsLiens()).hasSameSizeAs(solution.liens());
		}
	}

	@Test
	void lesSolutionsDeReferenceRestentAssezSerreesPourQueLeSeismeCompte() throws IOException {
		// Le lien le plus sollicité d'une solution gagnante ne doit pas être quasi à vide : sinon le niveau est trop facile.
		for (SolutionJson solution : solutions()) {
			double pire = simulation.simuler(solution.niveauId(), structure(solution.liens())).detailsLiens().stream()
					.mapToDouble(ResultatLien::tauxStress).max().orElseThrow();

			assertThat(pire).as("niveau %d", solution.niveauId()).isBetween(0.15, 1.0);
		}
	}

	/** Coût recalculé ici, indépendamment du validateur : somme (coût au mètre x longueur) des liens. */
	private double cout(Niveau niveau, List<LienJson> liens) {
		Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
		Map<String, Materiau> catalogue = materiaux.findAll().stream().collect(Collectors.toMap(Materiau::getId, m -> m));
		return liens.stream().mapToDouble(l -> {
			Noeud a = noeuds.get(l.noeudDepartId());
			Noeud b = noeuds.get(l.noeudArriveeId());
			return catalogue.get(l.materiauId()).getCoutUnitaire() * Math.hypot(a.getPosX() - b.getPosX(), a.getPosY() - b.getPosY());
		}).sum();
	}

	@Test
	void chaqueSolutionDeReferenceGagneSousLeBudget() throws IOException {
		for (SolutionJson solution : solutions()) {
			Niveau niveau = niveaux.findById(solution.niveauId()).orElseThrow();
			double cout = cout(niveau, solution.liens());

			assertThat(cout).as("niveau %d : coût %.1f pour un budget de %.1f", niveau.getNumero(), cout, niveau.getBudgetMax())
					.isLessThanOrEqualTo(niveau.getBudgetMax());
			assertThat(simulation.simuler(niveau.getId(), structure(solution.liens())).estValide())
					.as("niveau %d", niveau.getNumero()).isTrue();
		}
	}

	@Test
	void sansSesDiagonalesChaqueSolutionSEffondre() throws IOException {
		for (SolutionJson solution : solutions()) {
			Niveau niveau = niveaux.findById(solution.niveauId()).orElseThrow();
			Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
			// une diagonale change à la fois de colonne et de rangée
			List<LienJson> sans = solution.liens().stream().filter(l -> {
				Noeud a = noeuds.get(l.noeudDepartId());
				Noeud b = noeuds.get(l.noeudArriveeId());
				return a.getPosX() == b.getPosX() || a.getPosY() == b.getPosY();
			}).toList();

			ResultatSimulation resultat = simulation.simuler(solution.niveauId(), structure(sans));

			assertThat(resultat.estValide()).as("niveau %d", solution.niveauId()).isFalse();
			assertThat(resultat.messageBilan()).contains("effondrée");
		}
	}
}
