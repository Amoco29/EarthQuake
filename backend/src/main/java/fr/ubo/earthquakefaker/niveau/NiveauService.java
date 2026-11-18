package fr.ubo.earthquakefaker.niveau;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ubo.earthquakefaker.erreur.CodeErreur;
import fr.ubo.earthquakefaker.erreur.ExceptionMetier;

@Service
public class NiveauService {

	private final NiveauRepository niveaux;

	public NiveauService(NiveauRepository niveaux) {
		this.niveaux = niveaux;
	}

	// Transaction ouverte pour toute la conversion : les nœuds et matériaux de Niveau sont chargés à la demande.
	@Transactional(readOnly = true)
	public NiveauDto trouver(String idBrut) {
		Long id = lireId(idBrut);
		Niveau niveau = niveaux.findById(id)
				.orElseThrow(() -> new ExceptionMetier(CodeErreur.NIV_001, "Le niveau " + id + " n'existe pas."));
		return NiveauDto.de(niveau);
	}

	@Transactional(readOnly = true)
	public List<NiveauResumeDto> lister() {
		return niveaux.findAll().stream().sorted(Comparator.comparing(Niveau::getNumero)).map(NiveauResumeDto::de).toList();
	}

	// L'id arrive en texte pour décider nous-mêmes du code d'erreur (NIV-003) au lieu de laisser Spring répondre.
	public static Long lireId(String idBrut) {
		try {
			return Long.valueOf(idBrut);
		} catch (NumberFormatException e) {
			throw new ExceptionMetier(CodeErreur.NIV_003,
					"L'identifiant de niveau '" + idBrut + "' n'est pas un nombre entier.");
		}
	}
}
