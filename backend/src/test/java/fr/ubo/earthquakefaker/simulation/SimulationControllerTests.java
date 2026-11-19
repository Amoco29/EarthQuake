package fr.ubo.earthquakefaker.simulation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class SimulationControllerTests {

	private static final String SOLUTION_NIVEAU_1 = """
			{"liens":[
			 {"noeudDepartId":1,"noeudArriveeId":4,"materiauId":"BOIS"},
			 {"noeudDepartId":2,"noeudArriveeId":5,"materiauId":"BOIS"},
			 {"noeudDepartId":4,"noeudArriveeId":5,"materiauId":"BOIS"},
			 {"noeudDepartId":1,"noeudArriveeId":5,"materiauId":"BOIS"},
			 {"noeudDepartId":4,"noeudArriveeId":7,"materiauId":"BOIS"},
			 {"noeudDepartId":5,"noeudArriveeId":8,"materiauId":"BOIS"},
			 {"noeudDepartId":7,"noeudArriveeId":8,"materiauId":"BOIS"},
			 {"noeudDepartId":5,"noeudArriveeId":7,"materiauId":"BOIS"}]}""";

	@Autowired
	private MockMvc mvc;

	private MockHttpServletRequestBuilder simuler(String id, String corps) {
		return post("/api/niveaux/" + id + "/simuler").contentType(MediaType.APPLICATION_JSON).content(corps);
	}

	@Test
	void simuleLaSolutionDeReferenceEtRenvoieLeContrat() throws Exception {
		mvc.perform(simuler("1", SOLUTION_NIVEAU_1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estValide").value(true))
				.andExpect(jsonPath("$.scoreSoliditeGlobal").isNumber())
				.andExpect(jsonPath("$.messageBilan").isString())
				.andExpect(jsonPath("$.detailsLiens.length()").value(8))
				.andExpect(jsonPath("$.detailsLiens[0].noeudDepartId").value(1))
				.andExpect(jsonPath("$.detailsLiens[0].tauxStress").isNumber())
				.andExpect(jsonPath("$.detailsLiens[0].estCasse").value(false));
	}

	@Test
	void unePerteDansLeSeismeResteUneReponse200() throws Exception {
		// Sans diagonales : mécanisme, la structure s'effondre mais la requête est valide.
		mvc.perform(simuler("1", """
				{"liens":[{"noeudDepartId":1,"noeudArriveeId":4,"materiauId":"BOIS"},
				          {"noeudDepartId":2,"noeudArriveeId":5,"materiauId":"BOIS"},
				          {"noeudDepartId":4,"noeudArriveeId":5,"materiauId":"BOIS"},
				          {"noeudDepartId":4,"noeudArriveeId":7,"materiauId":"BOIS"},
				          {"noeudDepartId":5,"noeudArriveeId":8,"materiauId":"BOIS"},
				          {"noeudDepartId":7,"noeudArriveeId":8,"materiauId":"BOIS"}]}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estValide").value(false))
				.andExpect(jsonPath("$.detailsLiens[0].estCasse").value(true))
				.andExpect(jsonPath("$.detailsLiens[0].tempsRupture").isNumber());
	}

	@Test
	void structureVideEtListeAbsenteDonnentStr001() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[]}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("STR-001"));
		mvc.perform(simuler("1", "{}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("STR-001"));
	}

	@Test
	void unLienTropLongDonneStr005() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":1,\"noeudArriveeId\":6,\"materiauId\":\"BOIS\"}]}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("STR-005"));
	}

	@Test
	void uneDiagonaleEnBetonDonneMat003() throws Exception {
		// niveau 2 : 101 (0 ; 0) à 105 (2,5 ; 2,5) : 3,54 m, au-delà des 2,5 m du béton
		mvc.perform(simuler("2", "{\"liens\":[{\"noeudDepartId\":101,\"noeudArriveeId\":105,\"materiauId\":\"BETON\"}]}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("MAT-003"));
	}

	@Test
	void unLienQuiTraverseUnNoeudDonneStr006() throws Exception {
		// 1 (0 ; 0) à 7 (0 ; 5) passe sur le nœud 4 (0 ; 2,5)
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":1,\"noeudArriveeId\":7,\"materiauId\":\"BOIS\"}]}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("STR-006"));
	}

	@Test
	void unNoeudDUnAutreNiveauDonneStr002() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":1,\"noeudArriveeId\":101,\"materiauId\":\"BOIS\"}]}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("STR-002"));
	}

	@Test
	void unMateriauVerrouilleDonneMat002() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":1,\"noeudArriveeId\":4,\"materiauId\":\"AMORTISSEUR\"}]}"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("MAT-002"));
	}

	@Test
	void unBudgetDepasseDonneBud001() throws Exception {
		// niveau 2 (budget 500) : 30 m de béton à 25 le mètre = 750
		mvc.perform(simuler("2", """
				{"liens":[{"noeudDepartId":101,"noeudArriveeId":104,"materiauId":"BETON"},
				          {"noeudDepartId":104,"noeudArriveeId":107,"materiauId":"BETON"},
				          {"noeudDepartId":107,"noeudArriveeId":110,"materiauId":"BETON"},
				          {"noeudDepartId":110,"noeudArriveeId":113,"materiauId":"BETON"},
				          {"noeudDepartId":102,"noeudArriveeId":105,"materiauId":"BETON"},
				          {"noeudDepartId":105,"noeudArriveeId":108,"materiauId":"BETON"},
				          {"noeudDepartId":108,"noeudArriveeId":111,"materiauId":"BETON"},
				          {"noeudDepartId":111,"noeudArriveeId":114,"materiauId":"BETON"},
				          {"noeudDepartId":104,"noeudArriveeId":105,"materiauId":"BETON"},
				          {"noeudDepartId":107,"noeudArriveeId":108,"materiauId":"BETON"},
				          {"noeudDepartId":110,"noeudArriveeId":111,"materiauId":"BETON"},
				          {"noeudDepartId":113,"noeudArriveeId":114,"materiauId":"BETON"}]}"""))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("BUD-001"));
	}

	@Test
	void uneStructureNonRelieeAuSolDonneSim001EnStatut422() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":4,\"noeudArriveeId\":5,\"materiauId\":\"BOIS\"}]}"))
				.andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codeErreur").value("SIM-001"));
	}

	@Test
	void niveauInconnuDonne404EtIdentifiantInvalideDonne400() throws Exception {
		mvc.perform(simuler("99", SOLUTION_NIVEAU_1)).andExpect(status().isNotFound()).andExpect(jsonPath("$.codeErreur").value("NIV-001"));
		mvc.perform(simuler("abc", SOLUTION_NIVEAU_1)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("NIV-003"));
	}

	@Test
	void unCorpsIllisibleDonneUneErreurJsonLisible() throws Exception {
		mvc.perform(simuler("1", "pas du json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codeErreur").value("SYS-002"));
	}
}
