package fr.ubo.earthquakefaker.erreur;

import org.springframework.http.HttpStatus;

/**
 * Catalogue unique des codes d'erreur ([CONTEXTE]-[CAUSE]).
 * Tout ajout ici doit être reporté dans CODES_ERREURS.md (un test le vérifie).
 */
public enum CodeErreur {

	NIV_001("NIV-001", "Niveau introuvable", HttpStatus.NOT_FOUND),
	NIV_002("NIV-002", "Chargement des niveaux", HttpStatus.INTERNAL_SERVER_ERROR),
	NIV_003("NIV-003", "Identifiant de niveau invalide", HttpStatus.BAD_REQUEST),
	NIV_004("NIV-004", "Chargement des matériaux", HttpStatus.INTERNAL_SERVER_ERROR),

	STR_001("STR-001", "Validation de la structure", HttpStatus.BAD_REQUEST),
	STR_002("STR-002", "Validation de la structure", HttpStatus.BAD_REQUEST),
	STR_003("STR-003", "Validation de la structure", HttpStatus.BAD_REQUEST),
	STR_004("STR-004", "Validation de la hauteur", HttpStatus.BAD_REQUEST),

	BUD_001("BUD-001", "Validation du budget", HttpStatus.BAD_REQUEST),

	MAT_001("MAT-001", "Validation des matériaux", HttpStatus.BAD_REQUEST),
	MAT_002("MAT-002", "Validation des matériaux", HttpStatus.BAD_REQUEST),

	SIM_001("SIM-001", "Simulation sismique", HttpStatus.UNPROCESSABLE_ENTITY),

	SYS_001("SYS-001", "Erreur interne", HttpStatus.INTERNAL_SERVER_ERROR),
	SYS_002("SYS-002", "Requête HTTP invalide", HttpStatus.BAD_REQUEST);

	private final String code;
	private final String contexte;
	private final HttpStatus statut;

	CodeErreur(String code, String contexte, HttpStatus statut) {
		this.code = code;
		this.contexte = contexte;
		this.statut = statut;
	}

	public String getCode() {
		return code;
	}

	public String getContexte() {
		return contexte;
	}

	public HttpStatus getStatut() {
		return statut;
	}
}
