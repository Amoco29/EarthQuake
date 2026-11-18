package fr.ubo.earthquakefaker.niveau;

/** Une ligne de GET /api/niveaux : ce qu'il faut pour dessiner la carte du monde. */
public record NiveauResumeDto(Long id, int numero, String nom, String themeDecor, double budgetMax,
		double hauteurMinRequise) {

	static NiveauResumeDto de(Niveau niveau) {
		return new NiveauResumeDto(niveau.getId(), niveau.getNumero(), niveau.getNom(), niveau.getThemeDecor(),
				niveau.getBudgetMax(), niveau.getHauteurMinRequise());
	}
}
