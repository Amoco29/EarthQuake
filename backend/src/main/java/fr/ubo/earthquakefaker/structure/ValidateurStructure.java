package fr.ubo.earthquakefaker.structure;

import org.springframework.stereotype.Component;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;
import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;

/**
 * Règles de construction d'une structure, avant toute simulation. Classe pure : pas d'accès à la base,
 * le niveau et le catalogue de matériaux lui sont donnés. La première règle violée lève une {@link ExceptionMetier}.
 * Un lien ne relie que des nœuds voisins (STR-005) et ne passe sur aucun autre nœud (STR-006, contrôlé en premier :
 * c'est le cas le plus précis, et il couvre aussi les liens qui se superposent en partie). Une pièce non reliée au sol n'est pas une erreur : elle est simplement ignorée pour la hauteur (CLAUDE.md, point 5).
 */
@Component
public class ValidateurStructure {

	public void valider(Niveau niveau, Map<String, Materiau> catalogue, Structure structure) {
		if (structure.liens().isEmpty()) {
			throw new ExceptionMetier(CodeErreur.STR_001, "La structure ne contient aucun lien.");
		}

		Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
		Set<String> paires = new HashSet<>();
		Map<Long, Set<Long>> voisins = new HashMap<>();
		double cout = 0;
		double longueurMax = Geometrie.longueurMaxLien(niveau);

		for (Lien lien : structure.liens()) {
			Noeud depart = noeuds.get(lien.noeudDepartId());
			Noeud arrivee = noeuds.get(lien.noeudArriveeId());
			if (depart == null || arrivee == null) {
				throw new ExceptionMetier(CodeErreur.STR_002, "Le lien %s-%s référence un nœud qui n'appartient pas au niveau %d."
						.formatted(lien.noeudDepartId(), lien.noeudArriveeId(), niveau.getNumero()));
			}
			if (depart.getId().equals(arrivee.getId())) {
				throw new ExceptionMetier(CodeErreur.STR_003, "Le lien part et arrive au nœud " + depart.getId() + ".");
			}
			if (!paires.add(Math.min(depart.getId(), arrivee.getId()) + "-" + Math.max(depart.getId(), arrivee.getId()))) {
				throw new ExceptionMetier(CodeErreur.STR_003,
						"Deux liens relient les nœuds " + depart.getId() + " et " + arrivee.getId() + ".");
			}
			Noeud traverse = Geometrie.noeudTraverse(niveau, depart, arrivee);
			if (traverse != null) {
				throw new ExceptionMetier(CodeErreur.STR_006, "Le lien %d-%d passe sur le nœud %d : pose deux liens à la place."
						.formatted(depart.getId(), arrivee.getId(), traverse.getId()));
			}
			double longueurLien = Geometrie.longueur(depart, arrivee);
			if (longueurLien > longueurMax) {
				throw new ExceptionMetier(CodeErreur.STR_005, "Le lien %d-%d mesure %.1f m : un lien relie seulement des nœuds voisins (%.1f m maximum)."
						.formatted(depart.getId(), arrivee.getId(), longueurLien, longueurMax));
			}
			Materiau materiau = lien.materiauId() == null ? null : catalogue.get(lien.materiauId());
			if (materiau == null) {
				throw new ExceptionMetier(CodeErreur.MAT_001, "Matériau inconnu : " + lien.materiauId() + ".");
			}
			if (!niveau.getMateriauxAutorises().contains(materiau.getId())) {
				throw new ExceptionMetier(CodeErreur.MAT_002,
						"Le matériau " + materiau.getId() + " n'est pas autorisé au niveau " + niveau.getNumero() + ".");
			}
			cout += materiau.getCoutUnitaire() * longueurLien;
			voisins.computeIfAbsent(depart.getId(), k -> new HashSet<>()).add(arrivee.getId());
			voisins.computeIfAbsent(arrivee.getId(), k -> new HashSet<>()).add(depart.getId());
		}

		if (cout > niveau.getBudgetMax()) {
			throw new ExceptionMetier(CodeErreur.BUD_001,
					"Le coût de %.1f dépasse le budget de %.1f.".formatted(cout, niveau.getBudgetMax()));
		}

		Set<Long> relies = reliesAuSol(noeuds, voisins);
		if (relies.isEmpty()) {
			throw new ExceptionMetier(CodeErreur.SIM_001, "La structure n'est reliée à aucun ancrage au sol.");
		}

		double hauteur = relies.stream().mapToDouble(id -> noeuds.get(id).getPosY()).max().orElse(0);
		if (hauteur < niveau.getHauteurMinRequise()) {
			throw new ExceptionMetier(CodeErreur.STR_004, "La structure atteint %.1f m, il en faut %.1f."
					.formatted(hauteur, niveau.getHauteurMinRequise()));
		}
	}

	/** Les nœuds joignables depuis un ancrage au sol en suivant les liens (parcours en largeur). */
	public static Set<Long> reliesAuSol(Map<Long, Noeud> noeuds, Map<Long, Set<Long>> voisins) {
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
}
