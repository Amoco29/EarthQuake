package fr.ubo.earthquakefaker.materiau;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

/** Charge materiaux.json dans H2 au démarrage. Passe avant les niveaux, qui référencent les matériaux. */
@Component
@Order(1)
public class ChargeurMateriaux implements ApplicationRunner {

	private final LecteurMateriaux lecteur;
	private final MateriauRepository materiaux;

	public ChargeurMateriaux(LecteurMateriaux lecteur, MateriauRepository materiaux) {
		this.lecteur = lecteur;
		this.materiaux = materiaux;
	}

	@Override
	public void run(ApplicationArguments args) {
		try (InputStream flux = new ClassPathResource("materiaux.json").getInputStream()) {
			materiaux.saveAll(lecteur.lire(flux));
		} catch (IOException e) {
			throw new ExceptionMetier(CodeErreur.NIV_004, "materiaux.json introuvable ou illisible : " + e.getMessage());
		}
	}
}
