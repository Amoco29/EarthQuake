package fr.ubo.earthquakefaker.erreur;

import java.time.Instant;

/** Corps JSON de toute erreur renvoyée par l'API. */
public record ReponseErreur(Instant timestamp, String codeErreur, String contexte, String message) {

	public static ReponseErreur de(CodeErreur code, String message) {
		return new ReponseErreur(Instant.now(), code.getCode(), code.getContexte(), message);
	}
}
