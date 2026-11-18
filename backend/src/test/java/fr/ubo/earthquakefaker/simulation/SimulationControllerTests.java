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
			 {"noeudDepartId":1,"noeudArriveeId":4,"materiauId":"BETON"},
			 {"noeudDepartId":3,"noeudArriveeId":6,"materiauId":"BETON"},
			 {"noeudDepartId":4,"noeudArriveeId":7,"materiauId":"BOIS"},
			 {"noeudDepartId":6,"noeudArriveeId":9,"materiauId":"BOIS"},
			 {"noeudDepartId":4,"noeudArriveeId":6,"materiauId":"BOIS"},
			 {"noeudDepartId":7,"noeudArriveeId":9,"materiauId":"BOIS"},
			 {"noeudDepartId":1,"noeudArriveeId":6,"materiauId":"BOIS"},
			 {"noeudDepartId":4,"noeudArriveeId":9,"materiauId":"BOIS"}]}""";

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
				{"liens":[{"noeudDepartId":1,"noeudArriveeId":4,"materiauId":"BETON"},
				          {"noeudDepartId":3,"noeudArriveeId":6,"materiauId":"BETON"},
				          {"noeudDepartId":4,"noeudArriveeId":7,"materiauId":"BOIS"},
				          {"noeudDepartId":6,"noeudArriveeId":9,"materiauId":"BOIS"}]}"""))
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
		mvc.perform(simuler("1", """
				{"liens":[{"noeudDepartId":1,"noeudArriveeId":4,"materiauId":"ACIER"},
				          {"noeudDepartId":3,"noeudArriveeId":6,"materiauId":"ACIER"},
				          {"noeudDepartId":4,"noeudArriveeId":7,"materiauId":"ACIER"},
				          {"noeudDepartId":6,"noeudArriveeId":9,"materiauId":"ACIER"},
				          {"noeudDepartId":4,"noeudArriveeId":6,"materiauId":"ACIER"}]}"""))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.codeErreur").value("BUD-001"));
	}

	@Test
	void uneStructureNonRelieeAuSolDonneSim001EnStatut422() throws Exception {
		mvc.perform(simuler("1", "{\"liens\":[{\"noeudDepartId\":4,\"noeudArriveeId\":6,\"materiauId\":\"BOIS\"}]}"))
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
