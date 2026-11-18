package fr.ubo.earthquakefaker.erreur;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Point unique de conversion des exceptions en JSON { timestamp, codeErreur, contexte, message }.
 * On étend ResponseEntityExceptionHandler pour que les erreurs HTTP standard de Spring
 * (404, 405, corps illisible...) gardent leur statut au lieu de tomber dans le 500 générique.
 */
@RestControllerAdvice
public class GestionnaireErreurs extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreurs.class);

	@ExceptionHandler(ExceptionMetier.class)
	public ResponseEntity<ReponseErreur> gererMetier(ExceptionMetier e) {
		CodeErreur code = e.getCodeErreur();
		return ResponseEntity.status(code.getStatut()).body(ReponseErreur.de(code, e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ReponseErreur> gererInattendue(Exception e) {
		// Détail dans les logs seulement : on ne montre pas la cause interne au client.
		LOG.error("Erreur inattendue", e);
		CodeErreur code = CodeErreur.SYS_001;
		return ResponseEntity.status(code.getStatut())
				.body(ReponseErreur.de(code, "Une erreur interne est survenue."));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
			HttpStatusCode statut, WebRequest requete) {
		ReponseErreur reponse = ReponseErreur.de(CodeErreur.SYS_002, e.getMessage());
		return ResponseEntity.status(statut).headers(headers).body(reponse);
	}
}
