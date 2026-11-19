package fr.ubo.earthquakefaker.niveau;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Contexte complet : on teste le vrai niveaux.json chargé dans H2, de bout en bout.
@SpringBootTest
@AutoConfigureMockMvc
class NiveauControllerTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void renvoieLeNiveauAvecTousLesChampsDuContrat() throws Exception {
		mvc.perform(get("/api/niveaux/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.numero").value(1))
				.andExpect(jsonPath("$.nom").value("La prairie"))
				.andExpect(jsonPath("$.themeDecor").value("prairie"))
				.andExpect(jsonPath("$.budgetMax").value(350.0))
				.andExpect(jsonPath("$.hauteurMinRequise").value(5.0))
				.andExpect(jsonPath("$.largeurZone").value(5.0))
				.andExpect(jsonPath("$.hauteurZone").value(10.0))
				.andExpect(jsonPath("$.longueurMaxLien").value(org.hamcrest.Matchers.closeTo(3.5355, 0.001)))
				.andExpect(jsonPath("$.materiauxAutorises.length()").value(1))
				.andExpect(jsonPath("$.noeuds.length()").value(12));
	}

	@Test
	void lesNoeudsSontTriesParIdEtOntLesChampsDuContrat() throws Exception {
		mvc.perform(get("/api/niveaux/1"))
				.andExpect(jsonPath("$.noeuds[0].id").value(1))
				.andExpect(jsonPath("$.noeuds[0].posX").value(0.0))
				.andExpect(jsonPath("$.noeuds[0].posY").value(0.0))
				.andExpect(jsonPath("$.noeuds[0].estAncrageSol").value(true))
				.andExpect(jsonPath("$.noeuds[11].id").value(12))
				.andExpect(jsonPath("$.noeuds[11].estAncrageSol").value(false));
	}

	@Test
	void neExposeNiLaMagnitudeNiLesInternesDeLEntite() throws Exception {
		mvc.perform(get("/api/niveaux/1"))
				.andExpect(jsonPath("$.magnitudeSeisme").doesNotExist())
				.andExpect(jsonPath("$.noeuds[0].niveau").doesNotExist());
	}

	@Test
	void chaqueNiveauDuFichierEstAccessible() throws Exception {
		mvc.perform(get("/api/niveaux/2")).andExpect(status().isOk()).andExpect(jsonPath("$.noeuds.length()").value(15));
		mvc.perform(get("/api/niveaux/3")).andExpect(status().isOk()).andExpect(jsonPath("$.noeuds.length()").value(15));
	}

	@Test
	void niveauInexistantRenvoie404Niv001() throws Exception {
		mvc.perform(get("/api/niveaux/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codeErreur").value("NIV-001"))
				.andExpect(jsonPath("$.message").value("Le niveau 99 n'existe pas."));
	}

	@Test
	void identifiantNonNumeriqueRenvoie400Niv003() throws Exception {
		mvc.perform(get("/api/niveaux/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codeErreur").value("NIV-003"));
	}

	@Test
	void identifiantTropGrandRenvoie400Niv003() throws Exception {
		mvc.perform(get("/api/niveaux/99999999999999999999"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codeErreur").value("NIV-003"));
	}

	@Test
	void identifiantNegatifRenvoie404Niv001() throws Exception {
		mvc.perform(get("/api/niveaux/-1"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codeErreur").value("NIV-001"));
	}

	@Test
	void routeInconnueRenvoie404Sys002() throws Exception {
		mvc.perform(get("/api/inconnu"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codeErreur").value("SYS-002"));
	}

	@Test
	void listeLesNiveauxParNumeroPourLaCarte() throws Exception {
		mvc.perform(get("/api/niveaux"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(7))
				.andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].nom").value("La prairie"))
				.andExpect(jsonPath("$[2].numero").value(3))
				.andExpect(jsonPath("$[0].noeuds").doesNotExist());
	}
}
