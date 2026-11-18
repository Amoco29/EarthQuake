package fr.ubo.earthquakefaker.niveau;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Noeud {

	// Id explicite (niveaux.json), unique sur tous les niveaux : un lien ne peut pas viser le nœud d'un autre niveau.
	@Id
	private Long id;

	private double posX;
	private double posY;
	private boolean estAncrageSol;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "niveau_id")
	private Niveau niveau;

	protected Noeud() {
		// requis par JPA
	}

	public Noeud(Long id, double posX, double posY, boolean estAncrageSol) {
		this.id = id;
		this.posX = posX;
		this.posY = posY;
		this.estAncrageSol = estAncrageSol;
	}

	void setNiveau(Niveau niveau) {
		this.niveau = niveau;
	}

	public Long getId() {
		return id;
	}

	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}

	public boolean isEstAncrageSol() {
		return estAncrageSol;
	}

	public Niveau getNiveau() {
		return niveau;
	}
}
