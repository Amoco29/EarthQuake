package fr.ubo.earthquakefaker.materiau;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Matériau piloté par les données (materiaux.json) plutôt que par une hiérarchie de classes.
 * L'id est un code texte ("BOIS", "BETON"...) : c'est celui qu'envoie le frontend.
 */
@Entity
public class Materiau {

	@Id
	private String id;

	private String nom;
	private String couleurHex;
	private double coutUnitaire; // par mètre de lien
	private double masseLineique; // par mètre de lien
	private double resistanceTractionMax;
	private double resistanceCompressionMax;
	private double moduleElasticite;
	private double longueurMax; // en mètres : un lien de ce matériau ne peut pas être plus long

	protected Materiau() {
		// requis par JPA
	}

	/** Matériau sans limite de longueur (pratique pour les tests qui n'en ont pas besoin). */
	public Materiau(String id, String nom, String couleurHex, double coutUnitaire, double masseLineique,
			double resistanceTractionMax, double resistanceCompressionMax, double moduleElasticite) {
		this(id, nom, couleurHex, coutUnitaire, masseLineique, resistanceTractionMax, resistanceCompressionMax,
				moduleElasticite, Double.MAX_VALUE);
	}

	public Materiau(String id, String nom, String couleurHex, double coutUnitaire, double masseLineique,
			double resistanceTractionMax, double resistanceCompressionMax, double moduleElasticite, double longueurMax) {
		this.id = id;
		this.nom = nom;
		this.couleurHex = couleurHex;
		this.coutUnitaire = coutUnitaire;
		this.masseLineique = masseLineique;
		this.resistanceTractionMax = resistanceTractionMax;
		this.resistanceCompressionMax = resistanceCompressionMax;
		this.moduleElasticite = moduleElasticite;
		this.longueurMax = longueurMax;
	}

	public String getId() {
		return id;
	}

	public String getNom() {
		return nom;
	}

	public String getCouleurHex() {
		return couleurHex;
	}

	public double getCoutUnitaire() {
		return coutUnitaire;
	}

	public double getMasseLineique() {
		return masseLineique;
	}

	public double getResistanceTractionMax() {
		return resistanceTractionMax;
	}

	public double getResistanceCompressionMax() {
		return resistanceCompressionMax;
	}

	public double getModuleElasticite() {
		return moduleElasticite;
	}

	public double getLongueurMax() {
		return longueurMax;
	}
}
