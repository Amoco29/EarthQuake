package fr.ubo.earthquakefaker.simulation;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.materiau.MateriauRepository;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.NiveauRepository;
import fr.ubo.earthquakefaker.structure.Structure;

/** Charge le niveau et les matériaux depuis H2 puis lance le moteur. Le contrôleur POST arrivera au jalon 4. */
@Service
public class ServiceSimulation {

	private final NiveauRepository niveaux;
	private final MateriauRepository materiaux;
	private final MoteurSismique moteur;

	public ServiceSimulation(NiveauRepository niveaux, MateriauRepository materiaux, MoteurSismique moteur) {
		this.niveaux = niveaux;
		this.materiaux = materiaux;
		this.moteur = moteur;
	}

	@Transactional(readOnly = true) // Niveau.noeuds est LAZY
	public ResultatSimulation simuler(long niveauId, Structure structure) {
		Niveau niveau = niveaux.findById(niveauId)
				.orElseThrow(() -> new ExceptionMetier(CodeErreur.NIV_001, "Le niveau " + niveauId + " n'existe pas."));
		Map<String, Materiau> catalogue = materiaux.findAll().stream().collect(Collectors.toMap(Materiau::getId, m -> m));
		return moteur.simulerSeisme(niveau, catalogue, structure);
	}
}
