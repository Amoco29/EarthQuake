package fr.ubo.earthquakefaker.materiau;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

/** Lit et valide materiaux.json. Sépare du chargeur pour être testable sans Spring ni base. */
@Component
public class LecteurMateriaux {

	// Doubles (et non double) : un champ absent donne null, qu'on peut signaler, au lieu d'un 0 silencieux.
	private record MateriauJson(String id, String nom, String couleurHex, Double coutUnitaire, Double masseLineique,
			Double resistanceTractionMax, Double resistanceCompressionMax, Double moduleElasticite, Double longueurMax) {
	}

	private final ObjectMapper mapper;

	public LecteurMateriaux(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public List<Materiau> lire(InputStream flux) {
		List<MateriauJson> bruts;
		try {
			bruts = mapper.readValue(flux, mapper.getTypeFactory().constructCollectionType(List.class, MateriauJson.class));
		} catch (JsonProcessingException e) {
			throw erreur("JSON illisible : " + e.getOriginalMessage());
		} catch (IOException e) {
			throw erreur("lecture impossible : " + e.getMessage());
		}
		if (bruts == null || bruts.isEmpty()) {
			throw erreur("aucun matériau défini");
		}

		List<Materiau> materiaux = new ArrayList<>();
		Set<String> ids = new HashSet<>();
		for (MateriauJson m : bruts) {
			valider(m, ids);
			materiaux.add(new Materiau(m.id(), m.nom(), m.couleurHex(), m.coutUnitaire(), m.masseLineique(),
					m.resistanceTractionMax(), m.resistanceCompressionMax(), m.moduleElasticite(), m.longueurMax()));
		}
		return materiaux;
	}

	private void valider(MateriauJson m, Set<String> ids) {
		if (m == null || m.id() == null || m.id().isBlank()) {
			throw erreur("un matériau n'a pas d'id");
		}
		if (!ids.add(m.id())) {
			throw erreur("id en double : " + m.id());
		}
		if (m.nom() == null || m.nom().isBlank()) {
			throw erreur(m.id() + " : nom manquant");
		}
		if (m.couleurHex() == null || !m.couleurHex().matches("#[0-9A-Fa-f]{6}")) {
			throw erreur(m.id() + " : couleurHex doit être de la forme #RRGGBB");
		}
		exigerPositif(m.id(), "coutUnitaire", m.coutUnitaire());
		exigerPositif(m.id(), "masseLineique", m.masseLineique());
		exigerPositif(m.id(), "resistanceTractionMax", m.resistanceTractionMax());
		exigerPositif(m.id(), "resistanceCompressionMax", m.resistanceCompressionMax());
		exigerPositif(m.id(), "moduleElasticite", m.moduleElasticite());
		exigerPositif(m.id(), "longueurMax", m.longueurMax());
	}

	private void exigerPositif(String id, String champ, Double valeur) {
		if (valeur == null) {
			throw erreur(id + " : " + champ + " manquant");
		}
		if (!(valeur > 0)) { // écrit ainsi pour rejeter aussi NaN
			throw erreur(id + " : " + champ + " doit être strictement positif (valeur : " + valeur + ")");
		}
	}

	private ExceptionMetier erreur(String detail) {
		return new ExceptionMetier(CodeErreur.NIV_004, "materiaux.json invalide : " + detail);
	}
}
