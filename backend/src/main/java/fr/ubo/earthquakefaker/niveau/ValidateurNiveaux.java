package fr.ubo.earthquakefaker.niveau;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

/** Règles de cohérence de niveaux.json. Classe pure : pas de base, pas d'I/O. Toute violation lève NIV-002. */
@Component
public class ValidateurNiveaux {

	static final double LARGEUR_ZONE = 5;
	static final double HAUTEUR_ZONE_MIN = 10;
	static final double HAUTEUR_ZONE_MAX = 15;

	public void valider(List<NiveauJson> niveaux, Set<String> materiauxConnus) {
		if (niveaux == null || niveaux.isEmpty()) {
			throw erreur("aucun niveau défini");
		}
		Set<Long> idsNiveaux = new HashSet<>();
		Set<Integer> numeros = new HashSet<>();
		Set<Long> idsNoeuds = new HashSet<>(); // global : un lien ne doit pas viser le nœud d'un autre niveau

		for (NiveauJson n : niveaux) {
			validerNiveau(n, materiauxConnus, idsNiveaux, numeros, idsNoeuds);
		}
	}

	private void validerNiveau(NiveauJson n, Set<String> materiauxConnus, Set<Long> idsNiveaux,
			Set<Integer> numeros, Set<Long> idsNoeuds) {
		if (n == null || n.id() == null) {
			throw erreur("un niveau n'a pas d'id");
		}
		String ref = "niveau " + n.id() + " : ";
		if (!idsNiveaux.add(n.id())) {
			throw erreur("id de niveau en double : " + n.id());
		}
		if (n.numero() == null || n.numero() < 0 || !numeros.add(n.numero())) {
			throw erreur(ref + "numero manquant, négatif ou en double");
		}
		exigerTexte(ref + "nom", n.nom());
		exigerTexte(ref + "themeDecor", n.themeDecor());
		exigerPositif(ref + "budgetMax", n.budgetMax());
		exigerPositif(ref + "hauteurMinRequise", n.hauteurMinRequise());
		exigerPositif(ref + "magnitudeSeisme", n.magnitudeSeisme());

		if (n.largeurZone() == null || n.largeurZone() != LARGEUR_ZONE) {
			throw erreur(ref + "largeurZone doit valoir " + LARGEUR_ZONE);
		}
		if (n.hauteurZone() == null || n.hauteurZone() < HAUTEUR_ZONE_MIN || n.hauteurZone() > HAUTEUR_ZONE_MAX) {
			throw erreur(ref + "hauteurZone doit être comprise entre " + HAUTEUR_ZONE_MIN + " et " + HAUTEUR_ZONE_MAX);
		}
		if (n.hauteurMinRequise() > n.hauteurZone()) {
			throw erreur(ref + "hauteurMinRequise (" + n.hauteurMinRequise() + ") dépasse la hauteur de la zone ("
					+ n.hauteurZone() + ")");
		}
		validerMateriaux(ref, n.materiauxAutorises(), materiauxConnus);
		validerNoeuds(ref, n, idsNoeuds);
	}

	private void validerMateriaux(String ref, List<String> autorises, Set<String> connus) {
		if (autorises == null || autorises.isEmpty()) {
			throw erreur(ref + "aucun matériau autorisé");
		}
		Set<String> vus = new HashSet<>();
		for (String id : autorises) {
			if (id == null || !connus.contains(id)) {
				throw erreur(ref + "matériau autorisé inconnu : " + id);
			}
			if (!vus.add(id)) {
				throw erreur(ref + "matériau autorisé en double : " + id);
			}
		}
	}

	private void validerNoeuds(String ref, NiveauJson n, Set<Long> idsNoeuds) {
		if (n.noeuds() == null || n.noeuds().isEmpty()) {
			throw erreur(ref + "aucun nœud défini");
		}
		Set<String> positions = new HashSet<>();
		boolean aUnAncrage = false;
		boolean hauteurAtteignable = false;

		for (NoeudJson noeud : n.noeuds()) {
			if (noeud == null || noeud.id() == null) {
				throw erreur(ref + "un nœud n'a pas d'id");
			}
			String refNoeud = ref + "nœud " + noeud.id() + " : ";
			if (!idsNoeuds.add(noeud.id())) {
				throw erreur(ref + "id de nœud en double (unique sur tous les niveaux) : " + noeud.id());
			}
			if (noeud.posX() == null || noeud.posY() == null || noeud.estAncrageSol() == null) {
				throw erreur(refNoeud + "posX, posY et estAncrageSol sont obligatoires");
			}
			if (noeud.posX() < 0 || noeud.posX() > n.largeurZone()) {
				throw erreur(refNoeud + "posX " + noeud.posX() + " hors de la zone (0 à " + n.largeurZone() + ")");
			}
			if (noeud.posY() < 0 || noeud.posY() > n.hauteurZone()) {
				throw erreur(refNoeud + "posY " + noeud.posY() + " hors de la zone (0 à " + n.hauteurZone() + ")");
			}
			if (noeud.estAncrageSol() && noeud.posY() != 0) {
				throw erreur(refNoeud + "un ancrage au sol doit avoir posY = 0");
			}
			if (!positions.add(noeud.posX() + ";" + noeud.posY())) {
				throw erreur(refNoeud + "deux nœuds à la même position (" + noeud.posX() + ", " + noeud.posY() + ")");
			}
			aUnAncrage |= noeud.estAncrageSol();
			hauteurAtteignable |= noeud.posY() >= n.hauteurMinRequise();
		}
		if (!aUnAncrage) {
			throw erreur(ref + "aucun nœud d'ancrage au sol");
		}
		if (!hauteurAtteignable) {
			throw erreur(ref + "aucun nœud n'atteint hauteurMinRequise (" + n.hauteurMinRequise() + "), niveau infaisable");
		}
	}

	private void exigerTexte(String champ, String valeur) {
		if (valeur == null || valeur.isBlank()) {
			throw erreur(champ + " manquant");
		}
	}

	private void exigerPositif(String champ, Double valeur) {
		if (valeur == null || !(valeur > 0)) { // !(>) pour rejeter aussi NaN
			throw erreur(champ + " manquant ou non strictement positif");
		}
	}

	private ExceptionMetier erreur(String detail) {
		return new ExceptionMetier(CodeErreur.NIV_002, "niveaux.json invalide : " + detail);
	}
}
