package fr.ubo.earthquakefaker.niveau;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

@Entity
public class Niveau {

	// Pas de @GeneratedValue : l'id vient de niveaux.json, pour que l'URL /api/niveaux/{id} reste stable.
	@Id
	private Long id;

	private int numero;
	private String nom;
	private String themeDecor;
	private double budgetMax;
	private double hauteurMinRequise;
	private double magnitudeSeisme;

	// Dimensions de la zone de construction, en mètres (largeur 5, hauteur 10 à 15).
	private double largeurZone;
	private double hauteurZone;

	@OneToMany(mappedBy = "niveau", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Noeud> noeuds = new ArrayList<>();

	// Ids de matériaux (ex : "BOIS") : MAT-002 sera levé si le joueur en utilise un autre.
	@ElementCollection
	@CollectionTable(name = "niveau_materiau_autorise", joinColumns = @JoinColumn(name = "niveau_id"))
	@Column(name = "materiau_id")
	private List<String> materiauxAutorises = new ArrayList<>();

	protected Niveau() {
		// requis par JPA
	}

	public Niveau(Long id, int numero, String nom, String themeDecor, double budgetMax,
			double hauteurMinRequise, double magnitudeSeisme, double largeurZone, double hauteurZone) {
		this.id = id;
		this.numero = numero;
		this.nom = nom;
		this.themeDecor = themeDecor;
		this.budgetMax = budgetMax;
		this.hauteurMinRequise = hauteurMinRequise;
		this.magnitudeSeisme = magnitudeSeisme;
		this.largeurZone = largeurZone;
		this.hauteurZone = hauteurZone;
	}

	public void ajouterNoeud(Noeud noeud) {
		noeud.setNiveau(this);
		noeuds.add(noeud);
	}

	public void autoriserMateriau(String materiauId) {
		materiauxAutorises.add(materiauId);
	}

	public List<String> getMateriauxAutorises() {
		return materiauxAutorises;
	}

	public Long getId() {
		return id;
	}

	public int getNumero() {
		return numero;
	}

	public String getNom() {
		return nom;
	}

	public String getThemeDecor() {
		return themeDecor;
	}

	public double getBudgetMax() {
		return budgetMax;
	}

	public double getHauteurMinRequise() {
		return hauteurMinRequise;
	}

	public double getMagnitudeSeisme() {
		return magnitudeSeisme;
	}

	public double getLargeurZone() {
		return largeurZone;
	}

	public double getHauteurZone() {
		return hauteurZone;
	}

	public List<Noeud> getNoeuds() {
		return noeuds;
	}
}
