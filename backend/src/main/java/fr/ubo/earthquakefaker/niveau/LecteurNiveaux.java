package fr.ubo.earthquakefaker.niveau;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

/** Lit niveaux.json, le valide, puis le convertit en entités (non sauvegardées). */
@Component
public class LecteurNiveaux {

	private final ObjectMapper mapper;
	private final ValidateurNiveaux validateur;

	public LecteurNiveaux(ObjectMapper mapper, ValidateurNiveaux validateur) {
		this.mapper = mapper;
		this.validateur = validateur;
	}

	public List<Niveau> lire(InputStream flux, Set<String> materiauxConnus) {
		List<NiveauJson> bruts;
		try {
			bruts = mapper.readValue(flux, mapper.getTypeFactory().constructCollectionType(List.class, NiveauJson.class));
		} catch (JsonProcessingException e) {
			throw new ExceptionMetier(CodeErreur.NIV_002, "niveaux.json invalide : JSON illisible : " + e.getOriginalMessage());
		} catch (IOException e) {
			throw new ExceptionMetier(CodeErreur.NIV_002, "niveaux.json invalide : lecture impossible : " + e.getMessage());
		}
		validateur.valider(bruts, materiauxConnus);
		return bruts.stream().map(LecteurNiveaux::convertir).toList();
	}

	private static Niveau convertir(NiveauJson n) {
		Niveau niveau = new Niveau(n.id(), n.numero(), n.nom(), n.themeDecor(), n.budgetMax(), n.hauteurMinRequise(),
				n.magnitudeSeisme(), n.largeurZone(), n.hauteurZone());
		n.materiauxAutorises().forEach(niveau::autoriserMateriau);
		for (NoeudJson noeud : n.noeuds()) {
			niveau.ajouterNoeud(new Noeud(noeud.id(), noeud.posX(), noeud.posY(), noeud.estAncrageSol()));
		}
		return niveau;
	}
}
