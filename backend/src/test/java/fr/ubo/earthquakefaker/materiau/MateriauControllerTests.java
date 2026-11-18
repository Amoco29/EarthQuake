package fr.ubo.earthquakefaker.materiau;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MateriauControllerTests {

	@Autowired
	private MockMvc mvc;

	@Test
	void listeLesQuatreMateriauxDuMoinsCherAuPlusCher() throws Exception {
		mvc.perform(get("/api/materiaux"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4))
				.andExpect(jsonPath("$[0].id").value("BOIS"))
				.andExpect(jsonPath("$[0].couleurHex").value("#8B5A2B"))
				.andExpect(jsonPath("$[0].coutUnitaire").value(10.0))
				.andExpect(jsonPath("$[3].id").value("AMORTISSEUR"))
				.andExpect(jsonPath("$[0].longueurMax").value(3.54))
				.andExpect(jsonPath("$[1].longueurMax").value(2.5));
	}

	@Test
	void neDonnePasLesResistances() throws Exception {
		mvc.perform(get("/api/materiaux"))
				.andExpect(jsonPath("$[0].resistanceTractionMax").doesNotExist());
	}
}
