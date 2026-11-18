package fr.ubo.earthquakefaker.niveau;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.ubo.earthquakefaker.materiau.Materiau;

/**
 * Contrôle de cohérence d'une solution de référence (code de test uniquement).
 * Il vérifie les règles de construction, PAS la tenue au séisme : celle-ci viendra avec le moteur (jalon 2).
 * Chaque problème porte le code d'erreur de l'API qu'il déclencherait.
 */
final class VerificateurSolution {

	record LienJson(Long noeudDepartId, Long noeudArriveeId, String materiauId) {
	}

	record SolutionJson(Long niveauId, String description, List<LienJson> liens) {
	}

	private VerificateurSolution() {
	}

	static double cout(Niveau niveau, Map<String, Materiau> catalogue, List<LienJson> liens) {
		Map<Long, Noeud> noeuds = indexer(niveau);
		double total = 0;
		for (LienJson lien : liens) {
			Noeud a = noeuds.get(lien.noeudDepartId());
			Noeud b = noeuds.get(lien.noeudArriveeId());
			Materiau m = catalogue.get(lien.materiauId());
			if (a != null && b != null && m != null) {
				total += m.getCoutUnitaire() * distance(a, b);
			}
		}
		return total;
	}

	static List<String> verifier(Niveau niveau, Map<String, Materiau> catalogue, List<LienJson> liens) {
		List<String> problemes = new ArrayList<>();
		Map<Long, Noeud> noeuds = indexer(niveau);

		if (liens.isEmpty()) {
			problemes.add("STR-001 : structure vide");
		}

		Set<String> paires = new HashSet<>();
		Map<Long, Set<Long>> voisins = new HashMap<>();
		for (LienJson lien : liens) {
			Long depart = lien.noeudDepartId();
			Long arrivee = lien.noeudArriveeId();
			if (!noeuds.containsKey(depart) || !noeuds.containsKey(arrivee)) {
				problemes.add("STR-002 : nœud inconnu dans le lien " + depart + "-" + arrivee);
				continue;
			}
			if (depart.equals(arrivee)) {
				problemes.add("STR-003 : lien d'un nœud vers lui-même (" + depart + ")");
				continue;
			}
			if (!paires.add(Math.min(depart, arrivee) + "-" + Math.max(depart, arrivee))) {
				problemes.add("STR-003 : lien en double " + depart + "-" + arrivee);
			}
			if (!catalogue.containsKey(lien.materiauId())) {
				problemes.add("MAT-001 : matériau inconnu " + lien.materiauId());
			} else if (!niveau.getMateriauxAutorises().contains(lien.materiauId())) {
				problemes.add("MAT-002 : matériau non autorisé " + lien.materiauId());
			}
			voisins.computeIfAbsent(depart, k -> new HashSet<>()).add(arrivee);
			voisins.computeIfAbsent(arrivee, k -> new HashSet<>()).add(depart);
		}

		double cout = cout(niveau, catalogue, liens);
		if (cout > niveau.getBudgetMax()) {
			problemes.add("BUD-001 : coût " + cout + " > budget " + niveau.getBudgetMax());
		}

		// Connexité : on ne compte que ce qui est relié au sol par un chemin de liens (CLAUDE.md, point 5).
		Set<Long> relies = atteignablesDepuisLesAncrages(noeuds, voisins);
		if (relies.isEmpty()) {
			problemes.add("SIM-001 : aucun ancrage au sol relié");
		}
		Set<Long> flottants = new HashSet<>(voisins.keySet());
		flottants.removeAll(relies);
		if (!flottants.isEmpty() && !relies.isEmpty()) {
			problemes.add("pièce flottante, non reliée au sol : nœuds " + flottants);
		}
		double hauteur = relies.stream().mapToDouble(id -> noeuds.get(id).getPosY()).max().orElse(0);
		if (hauteur < niveau.getHauteurMinRequise()) {
			problemes.add("STR-004 : hauteur atteinte " + hauteur + " < " + niveau.getHauteurMinRequise());
		}
		return problemes;
	}

	private static Set<Long> atteignablesDepuisLesAncrages(Map<Long, Noeud> noeuds, Map<Long, Set<Long>> voisins) {
		Set<Long> vus = new HashSet<>();
		Deque<Long> file = new ArrayDeque<>();
		for (Long id : voisins.keySet()) {
			if (noeuds.get(id).isEstAncrageSol()) {
				vus.add(id);
				file.add(id);
			}
		}
		while (!file.isEmpty()) {
			for (Long suivant : voisins.getOrDefault(file.poll(), Set.of())) {
				if (vus.add(suivant)) {
					file.add(suivant);
				}
			}
		}
		return vus;
	}

	private static Map<Long, Noeud> indexer(Niveau niveau) {
		return niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
	}

	private static double distance(Noeud a, Noeud b) {
		return Math.hypot(a.getPosX() - b.getPosX(), a.getPosY() - b.getPosY());
	}
}
