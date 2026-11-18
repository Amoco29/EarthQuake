package fr.ubo.earthquakefaker.structure;

import java.util.List;

/** L'ensemble des liens posés par le joueur sur un niveau. */
public record Structure(List<Lien> liens) {

	public Structure {
		liens = liens == null ? List.of() : List.copyOf(liens);
	}
}
