package fr.ubo.earthquakefaker.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ConfigurationCorsTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void autoriseLeFrontendSurLePort5173() throws Exception {
		mvc.perform(get("/api/niveaux/1").header("Origin", "http://localhost:5173"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
	}

	@Test
	void autoriseLeFrontendSurLePort3000() throws Exception {
		mvc.perform(get("/api/niveaux/1").header("Origin", "http://localhost:3000"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
	}

	@Test
	void refuseUneOrigineInconnue() throws Exception {
		mvc.perform(get("/api/niveaux/1").header("Origin", "http://exemple.com"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@Test
	void repondAuPreflightPourLePost() throws Exception {
		mvc.perform(options("/api/niveaux/1/simuler")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "POST")
						.header("Access-Control-Request-Headers", "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
				.andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,OPTIONS"));
	}

	@Test
	void refuseLePreflightPourUneMethodeNonAutorisee() throws Exception {
		mvc.perform(options("/api/niveaux/1")
						.header("Origin", "http://localhost:5173")
						.header("Access-Control-Request-Method", "DELETE"))
				.andExpect(status().isForbidden());
	}

	@Test
	void neTouchePasALaConsoleH2() throws Exception {
		// Hors /api/** : aucun en-tête CORS ajouté par notre configuration.
		mvc.perform(get("/h2-console").header("Origin", "http://localhost:5173"))
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}
}
