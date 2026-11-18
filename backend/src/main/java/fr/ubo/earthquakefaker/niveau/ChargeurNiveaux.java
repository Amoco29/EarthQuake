package fr.ubo.earthquakefaker.niveau;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.materiau.MateriauRepository;

/**
 * Charge niveaux.json dans H2 au démarrage. S'exécute après ChargeurMateriaux (@Order) car la validation
 * vérifie que les matériaux autorisés existent. Un fichier invalide empêche l'application de démarrer.
 */
@Component
@Order(2)
public class ChargeurNiveaux implements ApplicationRunner {

	private final LecteurNiveaux lecteur;
	private final NiveauRepository niveaux;
	private final MateriauRepository materiaux;
	private final ResourceLoader chargeurDeRessources;
	private final String emplacement;

	public ChargeurNiveaux(LecteurNiveaux lecteur, NiveauRepository niveaux, MateriauRepository materiaux,
			ResourceLoader chargeurDeRessources,
			@Value("${earthquakefaker.niveaux:classpath:niveaux.json}") String emplacement) {
		this.lecteur = lecteur;
		this.niveaux = niveaux;
		this.materiaux = materiaux;
		this.chargeurDeRessources = chargeurDeRessources;
		this.emplacement = emplacement;
	}

	@Override
	public void run(ApplicationArguments args) {
		try (InputStream flux = chargeurDeRessources.getResource(emplacement).getInputStream()) {
			charger(flux);
		} catch (IOException e) {
			throw arretDuDemarrage(new ExceptionMetier(CodeErreur.NIV_002,
					"niveaux.json introuvable ou illisible : " + e.getMessage()));
		} catch (ExceptionMetier e) {
			throw arretDuDemarrage(e);
		}
	}

	/** Valide puis sauvegarde tous les niveaux en une seule transaction (saveAll) : tout ou rien. */
	public void charger(InputStream flux) {
		Set<String> idsMateriaux = materiaux.findAll().stream().map(Materiau::getId).collect(Collectors.toSet());
		niveaux.saveAll(lecteur.lire(flux, idsMateriaux));
	}

	// Au démarrage il n'y a pas de réponse HTTP : on met le code dans le message pour le retrouver dans les logs.
	private static IllegalStateException arretDuDemarrage(ExceptionMetier e) {
		return new IllegalStateException(e.getCodeErreur().getCode() + " : " + e.getMessage(), e);
	}
}
