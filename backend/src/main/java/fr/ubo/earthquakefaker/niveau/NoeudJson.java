package fr.ubo.earthquakefaker.niveau;

/** Nœud tel que lu dans niveaux.json. Wrappers (Double, Boolean) : un champ absent donne null et sera signalé. */
public record NoeudJson(Long id, Double posX, Double posY, Boolean estAncrageSol) {
}
