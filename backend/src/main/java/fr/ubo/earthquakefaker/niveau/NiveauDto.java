package fr.ubo.earthquakefaker.niveau;

import java.util.Comparator;
import java.util.List;

import fr.ubo.earthquakefaker.structure.Geometrie;

/**
 * Réponse de GET /api/niveaux/{id}. Volontairement sans magnitudeSeisme : le joueur n'a pas à la connaître,
 * elle sert au moteur sismique. {@code longueurMaxLien} permet au front de refuser tout de suite un lien trop long.
 */
public record NiveauDto(Long id, int numero, String nom, String themeDecor, double budgetMax,
		double hauteurMinRequise, double largeurZone, double hauteurZone, double longueurMaxLien,
		List<String> materiauxAutorises, List<NoeudDto> noeuds) {

	static NiveauDto de(Niveau niveau) {
		return new NiveauDto(niveau.getId(), niveau.getNumero(), niveau.getNom(), niveau.getThemeDecor(),
				niveau.getBudgetMax(), niveau.getHauteurMinRequise(), niveau.getLargeurZone(), niveau.getHauteurZone(),
				Geometrie.longueurMaxLien(niveau),
				List.copyOf(niveau.getMateriauxAutorises()),
				niveau.getNoeuds().stream()
						.sorted(Comparator.comparing(Noeud::getId)) // l'ordre de la base n'est pas garanti
						.map(NoeudDto::de)
						.toList());
	}
}
