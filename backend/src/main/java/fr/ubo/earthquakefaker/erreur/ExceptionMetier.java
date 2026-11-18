package fr.ubo.earthquakefaker.erreur;

public class ExceptionMetier extends RuntimeException {

	private final CodeErreur codeErreur;

	public ExceptionMetier(CodeErreur codeErreur, String message) {
		super(message);
		this.codeErreur = codeErreur;
	}

	public CodeErreur getCodeErreur() {
		return codeErreur;
	}
}
