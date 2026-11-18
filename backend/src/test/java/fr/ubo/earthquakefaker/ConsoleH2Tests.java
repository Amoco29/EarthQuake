package fr.ubo.earthquakefaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

// La console H2 est une servlet, pas un contrôleur MVC : MockMvc ne la voit pas, d'où le vrai serveur.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConsoleH2Tests {

	@Autowired
	private TestRestTemplate http;

	@Test
	void consoleH2EstAccessible() {
		ResponseEntity<String> reponse = http.getForEntity("/h2-console", String.class);

		assertThat(reponse.getStatusCode().is4xxClientError()).isFalse();
		assertThat(reponse.getStatusCode().is5xxServerError()).isFalse();
	}

}
