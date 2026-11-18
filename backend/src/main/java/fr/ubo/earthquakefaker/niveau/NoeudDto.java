package fr.ubo.earthquakefaker.niveau;

public record NoeudDto(Long id, double posX, double posY, boolean estAncrageSol) {

	static NoeudDto de(Noeud noeud) {
		return new NoeudDto(noeud.getId(), noeud.getPosX(), noeud.getPosY(), noeud.isEstAncrageSol());
	}
}
