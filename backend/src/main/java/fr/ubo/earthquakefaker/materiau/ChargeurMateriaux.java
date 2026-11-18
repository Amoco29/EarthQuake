package fr.ubo.earthquakefaker.materiau;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

/** Charge materiaux.json dans H2 au démarrage. Passe avant les niveaux, qui référencent les matériaux. */
@Component
@Order(1)
public class ChargeurMateriaux implements ApplicationRunner {

	private final LecteurMateriaux lecteur;
	private final MateriauRepository materiaux;
	private final ResourceLoader chargeurDeRessources;
	private final String emplacement;

	public ChargeurMateriaux(LecteurMateriaux lecteur, MateriauRepository materiaux,
			ResourceLoader chargeurDeRessources,
			@Value("${earthquakefaker.materiaux:classpath:materiaux.json}") String emplacement) {
		this.lecteur = lecteur;
		this.materiaux = materiaux;
		this.chargeurDeRessources = chargeurDeRessources;
		this.emplacement = emplacement;
	}

	@Override
	public void run(ApplicationArguments args) {
		try (InputStream flux = chargeurDeRessources.getResource(emplacement).getInputStream()) {
			materiaux.saveAll(lecteur.lire(flux));
		} catch (IOException e) {
			throw arretDuDemarrage(new ExceptionMetier(CodeErreur.NIV_004,
					"materiaux.json introuvable ou illisible : " + e.getMessage()));
		} catch (ExceptionMetier e) {
			throw arretDuDemarrage(e);
		}
	}

	// Au démarrage il n'y a pas de réponse HTTP : on met le code dans le message pour le retrouver dans les logs.
	private static IllegalStateException arretDuDemarrage(ExceptionMetier e) {
		return new IllegalStateException(e.getCodeErreur().getCode() + " : " + e.getMessage(), e);
	}
}
