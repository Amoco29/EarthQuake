package fr.ubo.earthquakefaker.erreur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(GestionnaireErreursTests.ControleurFactice.class)
@Import({ GestionnaireErreurs.class, GestionnaireErreursTests.ControleurFactice.class })
class GestionnaireErreursTests {

	@RestController
	static class ControleurFactice {

		@GetMapping("/test/metier")
		String metier() {
			throw new ExceptionMetier(CodeErreur.BUD_001, "Le coût total (320) dépasse le budget (250).");
		}

		@GetMapping("/test/inattendu")
		String inattendu() {
			throw new IllegalStateException("détail interne secret");
		}
	}

	@Autowired
	private MockMvc mvc;

	@Test
	void exceptionMetierRenvoieSonCodeSonStatutEtSonMessage() throws Exception {
		mvc.perform(get("/test/metier"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.codeErreur").value("BUD-001"))
				.andExpect(jsonPath("$.contexte").value("Validation du budget"))
				.andExpect(jsonPath("$.message").value("Le coût total (320) dépasse le budget (250)."));
	}

	@Test
	void timestampEstUnInstantIso8601() throws Exception {
		MvcResult resultat = mvc.perform(get("/test/metier")).andReturn();
		String json = resultat.getResponse().getContentAsString();
		String timestamp = com.jayway.jsonpath.JsonPath.read(json, "$.timestamp");

		assertThat(Instant.parse(timestamp)).isNotNull();
	}

	@Test
	void exceptionInattendueRenvoieSys001SansFuiterLeDetail() throws Exception {
		mvc.perform(get("/test/inattendu"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.codeErreur").value("SYS-001"))
				.andExpect(jsonPath("$.message").value("Une erreur interne est survenue."))
				.andExpect(jsonPath("$.trace").doesNotExist());

		String corps = mvc.perform(get("/test/inattendu")).andReturn().getResponse().getContentAsString();
		assertThat(corps).doesNotContain("détail interne secret");
	}

	@Test
	void routeInexistanteGardeSonStatut404AvecLeFormatStandard() throws Exception {
		mvc.perform(get("/test/inexistante"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.codeErreur").value("SYS-002"))
				.andExpect(jsonPath("$.timestamp").exists())
				.andExpect(jsonPath("$.contexte").exists())
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void tousLesCodesRespectentLeFormatContexteTiretCause() {
		for (CodeErreur code : CodeErreur.values()) {
			assertThat(code.getCode()).matches("[A-Z]{3}-([0-9]{3}|[A-Z]+)");
		}
	}

	@Test
	void tousLesCodesSontDocumentesDansCodesErreursMd() throws Exception {
		// Maven s'exécute dans backend/ : le fichier est à la racine du dépôt.
		String doc = Files.readString(Path.of("..", "CODES_ERREURS.md"));
		for (CodeErreur code : CodeErreur.values()) {
			assertThat(doc).as("code %s absent de CODES_ERREURS.md", code.getCode())
					.contains("**" + code.getCode() + "**");
		}
	}
}
