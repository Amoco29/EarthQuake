package fr.ubo.earthquakefaker.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.ubo.earthquakefaker.materiau.Materiau;
import fr.ubo.earthquakefaker.niveau.Niveau;
import fr.ubo.earthquakefaker.niveau.Noeud;
import fr.ubo.earthquakefaker.structure.Geometrie;
import fr.ubo.earthquakefaker.structure.Lien;
import fr.ubo.earthquakefaker.structure.Structure;
import fr.ubo.earthquakefaker.structure.ValidateurStructure;

/**
 * Calcule l'effort dans chaque lien sous les charges de {@link CalculateurCharges}.
 * <p>
 * Modèle : treillis à nœuds articulés (chaque lien ne travaille qu'en traction ou compression), résolu par la
 * méthode des rigidités, ce qui gère aussi les structures sur-contraintes. Les ancrages au sol sont fixes.
 * Le séisme secoue dans les deux sens : on garde le pire des deux. Un lien casse quand son effort dépasse sa
 * résistance (traction ou compression selon le cas). Un seul passage : on ne redistribue pas les efforts après une
 * rupture. Une partie instable (un mécanisme, ex : un carré sans diagonale) s'effondre : les liens qui touchent un
 * nœud libre de bouger tombent, puis on recalcule le reste, qui garde ses vrais taux. Les pièces qui n'ont jamais été
 * reliées au sol ne sont pas chargées : taux 0.
 */
@Component
public class CalculateurStress {

	/** Les résistances de materiaux.json sont en kN : conversion en newtons. */
	static final double NEWTONS_PAR_UNITE_RESISTANCE = 1000;

	/** Rigidité (EA) d'un lien = module d'élasticité × ce facteur. Seuls les rapports entre matériaux comptent. */
	static final double RIGIDITE_PAR_UNITE_MODULE = 1e6;

	/** Chaque amortisseur touchant un nœud retire cette part de la poussée latérale sur ce nœud (au plus 90 % au total). */
	static final double ABSORPTION_PAR_AMORTISSEUR = 0.3;
	static final double ABSORPTION_MAX = 0.9;

	/** Taux d'un lien qui s'effondre avec une partie instable (valeur finie pour rester sérialisable en JSON). */
	static final double TAUX_EFFONDREMENT = 99;

	/** Durée du séisme en secondes : un lien surchargé casse à DUREE / taux (plus il est surchargé, plus tôt). */
	static final double DUREE_SEISME = 10;

	private static final double SEUIL_SINGULARITE = 1e-9;

	public List<ResultatLien> calculer(Niveau niveau, Map<String, Materiau> catalogue, Structure structure,
			Map<Long, ChargeNoeud> charges) {
		Map<Long, Noeud> noeuds = niveau.getNoeuds().stream().collect(Collectors.toMap(Noeud::getId, n -> n));
		List<Lien> liens = structure.liens();
		Map<Long, Integer> amortisseurs = new HashMap<>();
		for (Lien lien : liens) {
			if ("AMORTISSEUR".equals(lien.materiauId())) {
				amortisseurs.merge(lien.noeudDepartId(), 1, Integer::sum);
				amortisseurs.merge(lien.noeudArriveeId(), 1, Integer::sum);
			}
		}

		double[] tauxMax = new double[liens.size()];
		boolean[] effondre = new boolean[liens.size()];
		List<Integer> actifs = new ArrayList<>();
		for (int i = 0; i < liens.size(); i++) {
			actifs.add(i);
		}
		boolean premierTour = true;

		while (!actifs.isEmpty()) {
			Set<Long> relies = reliesAuSol(noeuds, liens, actifs);
			List<Integer> restants = new ArrayList<>();
			for (int i : actifs) {
				if (relies.contains(liens.get(i).noeudDepartId())) {
					restants.add(i);
				} else if (!premierTour) {
					effondre[i] = true; // coupé du sol par l'effondrement : il tombe avec le reste
				}
			}
			premierTour = false;
			actifs = restants;
			if (actifs.isEmpty()) {
				break;
			}

			Map<Long, Integer> indexLibre = new HashMap<>();
			for (Long id : relies) {
				if (!noeuds.get(id).isEstAncrageSol()) {
					indexLibre.put(id, indexLibre.size());
				}
			}
			double[][] rigidite = assembler(noeuds, catalogue, liens, actifs, indexLibre);
			boolean[] mobiles = ddlMobiles(rigidite);
			if (mobiles != null) {
				final List<Integer> courants = actifs;
				for (int i : courants) {
					if (touche(liens.get(i), indexLibre, mobiles)) {
						effondre[i] = true;
					}
				}
				actifs = actifs.stream().filter(i -> !effondre[i]).collect(Collectors.toList());
				continue;
			}

			for (int sens : new int[] { 1, -1 }) {
				double[] deplacements = resoudre(rigidite, forces(charges, amortisseurs, indexLibre, sens));
				for (int i : actifs) {
					tauxMax[i] = Math.max(tauxMax[i], taux(liens.get(i), noeuds, catalogue, indexLibre, deplacements));
				}
			}
			break;
		}

		List<ResultatLien> resultats = new ArrayList<>();
		for (int i = 0; i < liens.size(); i++) {
			Lien lien = liens.get(i);
			double taux = effondre[i] ? TAUX_EFFONDREMENT : tauxMax[i];
			boolean casse = taux > 1;
			resultats.add(new ResultatLien(lien.noeudDepartId(), lien.noeudArriveeId(), taux, casse,
					casse ? DUREE_SEISME / taux : null));
		}
		return resultats;
	}

	private static Set<Long> reliesAuSol(Map<Long, Noeud> noeuds, List<Lien> liens, List<Integer> actifs) {
		Map<Long, Set<Long>> voisins = new HashMap<>();
		for (int i : actifs) {
			Lien lien = liens.get(i);
			voisins.computeIfAbsent(lien.noeudDepartId(), k -> new HashSet<>()).add(lien.noeudArriveeId());
			voisins.computeIfAbsent(lien.noeudArriveeId(), k -> new HashSet<>()).add(lien.noeudDepartId());
		}
		return ValidateurStructure.reliesAuSol(noeuds, voisins);
	}

	/** Vrai si une extrémité du lien est un nœud qui peut bouger librement (selon x ou y). */
	private static boolean touche(Lien lien, Map<Long, Integer> indexLibre, boolean[] mobiles) {
		for (Long id : new Long[] { lien.noeudDepartId(), lien.noeudArriveeId() }) {
			Integer index = indexLibre.get(id);
			if (index != null && (mobiles[2 * index] || mobiles[2 * index + 1])) {
				return true;
			}
		}
		return false;
	}

	/** Matrice de rigidité globale : 2 inconnues (x, y) par nœud libre. */
	private double[][] assembler(Map<Long, Noeud> noeuds, Map<String, Materiau> catalogue, List<Lien> liens,
			List<Integer> actifs, Map<Long, Integer> indexLibre) {
		int n = 2 * indexLibre.size();
		double[][] k = new double[n][n];
		for (int numero : actifs) {
			Lien lien = liens.get(numero);
			Noeud a = noeuds.get(lien.noeudDepartId());
			Noeud b = noeuds.get(lien.noeudArriveeId());
			double longueur = Geometrie.longueur(a, b);
			double c = (b.getPosX() - a.getPosX()) / longueur;
			double s = (b.getPosY() - a.getPosY()) / longueur;
			double raideur = catalogue.get(lien.materiauId()).getModuleElasticite() * RIGIDITE_PAR_UNITE_MODULE / longueur;
			// Degrés de liberté de a puis b ; -1 pour un ancrage (fixe, donc absent du système)
			int[] ddl = { colonne(indexLibre, a, 0), colonne(indexLibre, a, 1), colonne(indexLibre, b, 0), colonne(indexLibre, b, 1) };
			double[] dir = { -c, -s, c, s };
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					if (ddl[i] >= 0 && ddl[j] >= 0) {
						k[ddl[i]][ddl[j]] += raideur * dir[i] * dir[j];
					}
				}
			}
		}
		return k;
	}

	private static int colonne(Map<Long, Integer> indexLibre, Noeud noeud, int axe) {
		Integer index = indexLibre.get(noeud.getId());
		return index == null ? -1 : 2 * index + axe;
	}

	private double[] forces(Map<Long, ChargeNoeud> charges, Map<Long, Integer> amortisseurs,
			Map<Long, Integer> indexLibre, int sens) {
		double[] f = new double[2 * indexLibre.size()];
		indexLibre.forEach((id, index) -> {
			ChargeNoeud charge = charges.get(id);
			double absorption = Math.min(ABSORPTION_MAX, ABSORPTION_PAR_AMORTISSEUR * amortisseurs.getOrDefault(id, 0));
			f[2 * index] = sens * charge.forceX() * (1 - absorption);
			f[2 * index + 1] = charge.forceY();
		});
		return f;
	}

	/** Effort dans le lien / résistance : positif en traction, rapporté à la résistance adaptée au sens. */
	private double taux(Lien lien, Map<Long, Noeud> noeuds, Map<String, Materiau> catalogue,
			Map<Long, Integer> indexLibre, double[] u) {
		Noeud a = noeuds.get(lien.noeudDepartId());
		Noeud b = noeuds.get(lien.noeudArriveeId());
		Materiau materiau = catalogue.get(lien.materiauId());
		double longueur = Geometrie.longueur(a, b);
		double c = (b.getPosX() - a.getPosX()) / longueur;
		double s = (b.getPosY() - a.getPosY()) / longueur;
		double allongement = (deplacement(u, indexLibre, b, 0) - deplacement(u, indexLibre, a, 0)) * c
				+ (deplacement(u, indexLibre, b, 1) - deplacement(u, indexLibre, a, 1)) * s;
		double effort = materiau.getModuleElasticite() * RIGIDITE_PAR_UNITE_MODULE / longueur * allongement;
		double resistance = (effort >= 0 ? materiau.getResistanceTractionMax() : materiau.getResistanceCompressionMax())
				* NEWTONS_PAR_UNITE_RESISTANCE;
		return Math.abs(effort) / resistance;
	}

	private static double deplacement(double[] u, Map<Long, Integer> indexLibre, Noeud noeud, int axe) {
		Integer index = indexLibre.get(noeud.getId());
		return index == null ? 0 : u[2 * index + axe];
	}

	/**
	 * Réduit la matrice (forme échelonnée) et renvoie, pour chaque inconnue, si elle intervient dans un mouvement
	 * possible sans effort (mode de mécanisme). {@code null} si la structure est stable.
	 */
	private static boolean[] ddlMobiles(double[][] matrice) {
		int n = matrice.length;
		if (n == 0) {
			return null;
		}
		double[][] a = new double[n][];
		double echelle = 0;
		for (int i = 0; i < n; i++) {
			a[i] = matrice[i].clone();
			echelle = Math.max(echelle, Math.abs(a[i][i]));
		}
		int rang = 0;
		int[] ligneDuPivot = new int[n];
		boolean[] estPivot = new boolean[n];
		for (int col = 0; col < n && rang < n; col++) {
			int pivot = rang;
			for (int i = rang + 1; i < n; i++) {
				if (Math.abs(a[i][col]) > Math.abs(a[pivot][col])) {
					pivot = i;
				}
			}
			if (Math.abs(a[pivot][col]) < SEUIL_SINGULARITE * echelle) {
				continue;
			}
			double[] ligne = a[rang];
			a[rang] = a[pivot];
			a[pivot] = ligne;
			double valeur = a[rang][col];
			for (int j = 0; j < n; j++) {
				a[rang][j] /= valeur;
			}
			for (int i = 0; i < n; i++) {
				if (i != rang && a[i][col] != 0) {
					double facteur = a[i][col];
					for (int j = 0; j < n; j++) {
						a[i][j] -= facteur * a[rang][j];
					}
				}
			}
			estPivot[col] = true;
			ligneDuPivot[col] = rang;
			rang++;
		}
		if (rang == n) {
			return null;
		}
		// Vecteur du noyau pour chaque colonne libre f : x_f = 1 et x_pivot = -a[ligne][f].
		boolean[] mobiles = new boolean[n];
		for (int f = 0; f < n; f++) {
			if (estPivot[f]) {
				continue;
			}
			mobiles[f] = true;
			for (int c = 0; c < n; c++) {
				if (estPivot[c] && Math.abs(a[ligneDuPivot[c]][f]) > 1e-7) {
					mobiles[c] = true;
				}
			}
		}
		return mobiles;
	}

	/** Pivot de Gauss avec choix du meilleur pivot ; {@code null} si le système est singulier (mécanisme). */
	private static double[] resoudre(double[][] matrice, double[] second) {
		int n = second.length;
		double[][] a = new double[n][];
		for (int i = 0; i < n; i++) {
			a[i] = matrice[i].clone();
		}
		double[] b = second.clone();
		double echelle = 0;
		for (int i = 0; i < n; i++) {
			echelle = Math.max(echelle, Math.abs(a[i][i]));
		}
		for (int col = 0; col < n; col++) {
			int pivot = col;
			for (int i = col + 1; i < n; i++) {
				if (Math.abs(a[i][col]) > Math.abs(a[pivot][col])) {
					pivot = i;
				}
			}
			if (Math.abs(a[pivot][col]) < SEUIL_SINGULARITE * echelle) {
				return null;
			}
			double[] ligne = a[col];
			a[col] = a[pivot];
			a[pivot] = ligne;
			double t = b[col];
			b[col] = b[pivot];
			b[pivot] = t;
			for (int i = col + 1; i < n; i++) {
				double facteur = a[i][col] / a[col][col];
				for (int j = col; j < n; j++) {
					a[i][j] -= facteur * a[col][j];
				}
				b[i] -= facteur * b[col];
			}
		}
		double[] x = new double[n];
		for (int i = n - 1; i >= 0; i--) {
			double somme = b[i];
			for (int j = i + 1; j < n; j++) {
				somme -= a[i][j] * x[j];
			}
			x[i] = somme / a[i][i];
		}
		return x;
	}
}
