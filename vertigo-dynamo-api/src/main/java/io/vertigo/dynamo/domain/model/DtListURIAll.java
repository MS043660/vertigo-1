package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Une URI d'une DtListe représentant la liste complète des objets.
 * Elle est entièrement définie par
 *  - la dtDefinition de l'objet
 *
 * exemple :
 * - ALL_DT_PERSONNE.
 *
 * @author npiedeloup
 * @version $Id: DtListURIAll.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public final class DtListURIAll extends DtListURI {
	private static final long serialVersionUID = -1227046775032730925L;

	/**
	 * Constructeur.
	 * @param dtDefinition ID de la Définition de DT
	 */
	public DtListURIAll(final DtDefinition dtDefinition) {
		super(dtDefinition);
	}
}