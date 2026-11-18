package fr.ubo.earthquakefaker.simulation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.ubo.earthquakefaker.niveau.NiveauService;

@RestController
@RequestMapping("/api/niveaux")
public class SimulationController {

	private final ServiceSimulation service;

	public SimulationController(ServiceSimulation service) {
		this.service = service;
	}

	@PostMapping("/{id}/simuler")
	public ResultatSimulation simuler(@PathVariable String id, @RequestBody DemandeSimulation demande) {
		return service.simuler(NiveauService.lireId(id), demande.versStructure());
	}
}
