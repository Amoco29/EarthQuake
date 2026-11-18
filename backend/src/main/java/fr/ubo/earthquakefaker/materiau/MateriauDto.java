package fr.ubo.earthquakefaker.materiau;

/** Ce que le front a besoin de savoir d'un matériau pour le dessiner et chiffrer la construction (pas les résistances). */
public record MateriauDto(String id, String nom, String couleurHex, double coutUnitaire, double masseLineique) {

	static MateriauDto de(Materiau m) {
		return new MateriauDto(m.getId(), m.getNom(), m.getCouleurHex(), m.getCoutUnitaire(), m.getMasseLineique());
	}
}
