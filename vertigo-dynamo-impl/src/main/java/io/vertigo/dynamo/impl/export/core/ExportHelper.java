package io.vertigo.dynamo.impl.export.core;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe utilitaire pour export.
 * @author pchretien, evernat
 * @version $Id: ExportHelper.java,v 1.6 2014/02/27 10:23:30 pchretien Exp $
 */
public final class ExportHelper {
	private final PersistenceManager persistenceManager;

	/**
	 * Constructeur.
	 */
	public ExportHelper(final PersistenceManager persistenceManager) {
		Assertion.checkNotNull(persistenceManager);
		//---------------------------------------------------------------------
		this.persistenceManager = persistenceManager;
	}

	/**
	 * Retourne le text d'un champs du DTO en utilisant le formateur du domaine, ou l'�l�ment issu de la liste de REF si il y a une d�normalisation � faire.
	 * @param referenceCache Cache des �l�ments de r�f�rence (cl�-libell�), peut �tre vide la premiere fois il sera remplit automatiquement (utilis� pour les champs issus d'association avec une liste de ref)
	 * @param denormCache Cache des colonnes d�normalis�es par field, peut �tre vide la premiere fois il sera remplit automatiquement (utilis� en cas de d�norm sp�cifique)
	 * @param dto Objet m�tier
	 * @param exportColumn Information de la colonne a exporter.
	 * @return Valeur d'affichage de la colonne de l'objet m�tier
	 */
	public String getText(final Map<DtField, Map<Object, String>> referenceCache, final Map<DtField, Map<Object, String>> denormCache, final DtObject dto, final ExportField exportColumn) {
		return (String) getValue(true, referenceCache, denormCache, dto, exportColumn);
	}

	/**
	 * Retourne la valeur d'un champs du DTO, ou l'�l�ment issu de la liste de REF si il y a une d�normalisation � faire.
	 * @param referenceCache Cache des �l�ments de r�f�rence (cl�-libell�), peut �tre vide la premiere fois il sera remplit automatiquement (utilis� pour les champs issus d'association avec une liste de ref)
	 * @param denormCache Cache des colonnes d�normalis�es par field, peut �tre vide la premiere fois il sera remplit automatiquement (utilis� en cas de d�norm sp�cifique)
	 * @param dto Objet m�tier
	 * @param exportColumn Information de la colonne a exporter.
	 * @return Valeur typ�e de la colonne de l'objet m�tier
	 */
	public Object getValue(final Map<DtField, Map<Object, String>> referenceCache, final Map<DtField, Map<Object, String>> denormCache, final DtObject dto, final ExportField exportColumn) {
		return getValue(false, referenceCache, denormCache, dto, exportColumn);
	}

	private Object getValue(final boolean forceStringValue, final Map<DtField, Map<Object, String>> referenceCache, final Map<DtField, Map<Object, String>> denormCache, final DtObject dto, final ExportField exportColumn) {
		final DtField dtField = exportColumn.getDtField();
		Object value;
		try {
			if (dtField.getType() == DtField.FieldType.FOREIGN_KEY && persistenceManager.getMasterDataConfiguration().containsMasterData(dtField.getFkDtDefinition())) {
				Map<Object, String> referenceIndex = referenceCache.get(dtField);
				if (referenceIndex == null) {
					referenceIndex = createReferentielIndex(dtField);
					referenceCache.put(dtField, referenceIndex);
				}
				value = referenceIndex.get(dtField.getDataAccessor().getValue(dto));
			} else if (exportColumn instanceof ExportDenormField) {
				final ExportDenormField exportDenormColumn = (ExportDenormField) exportColumn;
				Map<Object, String> denormIndex = denormCache.get(dtField);
				if (denormIndex == null) {
					denormIndex = createDenormIndex(exportDenormColumn.getDenormList(), exportDenormColumn.getKeyField(), exportDenormColumn.getDisplayField());
					denormCache.put(dtField, denormIndex);
				}
				value = denormIndex.get(dtField.getDataAccessor().getValue(dto));
			} else {
				value = exportColumn.getDtField().getDataAccessor().getValue(dto);
				if (forceStringValue) {
					value = exportColumn.getDtField().getDomain().getFormatter().valueToString(value, exportColumn.getDtField().getDomain().getDataType());
				}
			}
		} catch (final Exception e) {
			// TODO : solution ? => ouvrir pour surcharge de cette gestion
			value = "Non Exportable";
		}
		return value;
	}

	private Map<Object, String> createReferentielIndex(final DtField dtField) {
		//TODO ceci est un copier/coller de KSelectionListBean (qui resemble plus � un helper des MasterData qu'a un bean)
		//La collection n'est pas pr�cis� alors on va la chercher dans le repository du r�f�rentiel
		final DtListURIForMasterData mdlUri = persistenceManager.getMasterDataConfiguration().getDtListURIForMasterData(dtField.getFkDtDefinition());
		final DtList<DtObject> valueList = persistenceManager.getBroker().getList(mdlUri);
		final DtField dtFieldDisplay = mdlUri.getDtDefinition().getDisplayField().get();
		final DtField dtFieldKey = valueList.getDefinition().getIdField().get();
		return createDenormIndex(valueList, dtFieldKey, dtFieldDisplay);
	}

	private static Map<Object, String> createDenormIndex(final DtList<?> valueList, final DtField keyField, final DtField displayField) {
		final Map<Object, String> denormIndex = new HashMap<>(valueList.size());
		for (final DtObject dto : valueList) {
			final String svalue = displayField.getDomain().getFormatter().valueToString(displayField.getDataAccessor().getValue(dto), displayField.getDomain().getDataType());
			denormIndex.put(keyField.getDataAccessor().getValue(dto), svalue);
		}
		return denormIndex;
	}

}