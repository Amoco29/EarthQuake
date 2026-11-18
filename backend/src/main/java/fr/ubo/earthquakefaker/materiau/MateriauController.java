package fr.ubo.earthquakefaker.materiau;

import java.util.Comparator;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materiaux")
public class MateriauController {

	private final MateriauRepository materiaux;

	public MateriauController(MateriauRepository materiaux) {
		this.materiaux = materiaux;
	}

	@GetMapping
	public List<MateriauDto> lister() {
		return materiaux.findAll().stream().sorted(Comparator.comparing(Materiau::getCoutUnitaire)).map(MateriauDto::de).toList();
	}
}
