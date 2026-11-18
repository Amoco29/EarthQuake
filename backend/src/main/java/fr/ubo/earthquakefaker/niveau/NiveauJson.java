package fr.ubo.earthquakefaker.niveau;

import java.util.List;

/** Niveau tel que lu dans niveaux.json, avant validation. */
public record NiveauJson(Long id, Integer numero, String nom, String themeDecor, Double budgetMax,
		Double hauteurMinRequise, Double magnitudeSeisme, Double largeurZone, Double hauteurZone,
		List<String> materiauxAutorises, List<NoeudJson> noeuds) {
}
