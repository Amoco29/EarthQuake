package fr.ubo.earthquakefaker.niveau;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/niveaux")
public class NiveauController {

	private final NiveauService service;

	public NiveauController(NiveauService service) {
		this.service = service;
	}

	@GetMapping("/{id}")
	public NiveauDto obtenir(@PathVariable String id) {
		return service.trouver(id);
	}
}
