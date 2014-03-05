package io.vertigo.dynamo.search;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.dynamo.collections.facet.model.Facet;
import io.vertigo.dynamo.collections.facet.model.FacetValue;
import io.vertigo.dynamo.collections.facet.model.FacetedQuery;
import io.vertigo.dynamo.collections.facet.model.FacetedQueryResult;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.IndexDefinition;
import io.vertigo.dynamo.search.model.Index;
import io.vertigo.dynamo.search.model.SearchQuery;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.kernel.Home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author  npiedeloup
 * @version $Id: AbstractSearchManagerTest.java,v 1.5 2014/01/28 18:53:45 pchretien Exp $
 */
public abstract class AbstractSearchManagerTest extends AbstractTestCaseJU4 {
	public static final String FCT_CAR_SUFFIX = "_CAR";

	//Query sans facette
	public static final String QRY_CAR = "QRY_CAR";

	//Query avec facette sur le constructeur
	public static final String QRY_CAR_FACET = "QRY_CAR_FACET";

	/** Manager de recherche. */
	@Inject
	protected SearchManager searchManager;
	@Inject
	private CollectionsManager collectionsManager;

	/** IndexDefinition. */
	protected IndexDefinition carIndexDefinition;
	private FacetedQueryDefinition carQueryDefinition;
	private FacetedQueryDefinition carFacetQueryDefinition;
	private CarDataBase carDataBase;

	private String facetSuffix;

	/**
	 * Initialise l'index.
	 * @param indexName Nom de l'index
	 */
	protected final void init(final String indexName) {
		//On construit la BDD des voitures
		carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		facetSuffix = FCT_CAR_SUFFIX;
		carIndexDefinition = Home.getDefinitionSpace().resolve(indexName, IndexDefinition.class);
		carQueryDefinition = Home.getDefinitionSpace().resolve(QRY_CAR, FacetedQueryDefinition.class);
		carFacetQueryDefinition = Home.getDefinitionSpace().resolve(QRY_CAR_FACET, FacetedQueryDefinition.class);
		clean(carIndexDefinition);
	}

	/**
	 * @param indexDefinition Definition de l'index
	 */
	private final void clean(final IndexDefinition indexDefinition) {
		final ListFilter removeQuery = new ListFilter("*:*");
		searchManager.getSearchServices().remove(indexDefinition, removeQuery);
	}

	/**
	 * Test de création nettoyage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testClean() {
		clean(carIndexDefinition);
	}

	/**
	 * Test de création de n enregistrements dans l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndex() {
		index(false);
		index(true);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexQuery() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testIndexAllQuery() {
		index(true);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testQuery() {
		index(false);
		long size;
		size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);

		size = query("MAKE:Peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), (int) size);

		size = query("MAKE:peugeot"); //Les constructeur sont des mots clés donc sensible à la casse
		Assert.assertEquals(0L, size);

		size = query("MAKE:Vol*"); //On compte les volkswagen
		Assert.assertEquals(carDataBase.getByMake("volkswagen").size(), (int) size);

		size = query("YEAR:[* TO 2005]"); //On compte les véhicules avant 2005
		Assert.assertEquals(carDataBase.before(2005), size);

		size = query("DESCRIPTION:panoRAmique");
		Assert.assertEquals(size, carDataBase.containsDescription("panoramique"));

		size = query("DESCRIPTION:clim");
		Assert.assertEquals(size, carDataBase.containsDescription("clim"));

		size = query("DESCRIPTION:avenir");
		Assert.assertEquals(size, carDataBase.containsDescription("avenir"));

		size = query("DESCRIPTION:l'avenir");
		Assert.assertEquals(size, carDataBase.containsDescription("l'avenir"));
	}

	/**
	 * Test de requétage de l'index avec tri.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testSortedQuery() {
		index(false);
		Car firstCar;

		firstCar = doQueryAndGetFirst("*:*", "MAKE", true);
		Assert.assertEquals("Audi", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "MAKE", false);
		Assert.assertEquals("Volkswagen", firstCar.getMake());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", true);
		Assert.assertEquals(1998, firstCar.getYear().intValue());

		firstCar = doQueryAndGetFirst("*:*", "YEAR", false);
		Assert.assertEquals(2010, firstCar.getYear().intValue());

	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByRange() {
		index(false);
		final FacetedQueryResult<Car, SearchQuery> result = facetQuery("*:*");
		testFacetResultByRange(result);
	}

	private void testFacetResultByRange(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette date
		final Facet yearFacet = getFacetByName(result, "FCT_YEAR" + facetSuffix);
		Assert.assertNotNull(yearFacet);
		Assert.assertEquals(true, yearFacet.getDefinition().isRangeFacet());

		boolean found = false;
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains("avant")) {
				found = true;
				Assert.assertEquals(carDataBase.before(2000), entry.getValue().longValue());
			}
		}
		Assert.assertEquals(true, found);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testFacetQueryByTerm() {
		index(false);
		final FacetedQueryResult<Car, SearchQuery> result = facetQuery("*:*");
		testFacetResultByTerm(result);
	}

	private void testFacetResultByTerm(final FacetedQueryResult<Car, ?> result) {
		Assert.assertEquals(carDataBase.size(), result.getCount());

		//On vérifie qu'il y a le bon nombre de facettes.
		Assert.assertEquals(3, result.getFacets().size());

		//On recherche la facette constructeur
		final Facet makeFacet = getFacetByName(result, "FCT_MAKE" + facetSuffix);
		Assert.assertNotNull(makeFacet);
		//On vérifie que l'on est sur le champ Make
		Assert.assertEquals("MAKE", makeFacet.getDefinition().getDtField().getName());
		Assert.assertEquals(false, makeFacet.getDefinition().isRangeFacet());

		//On vérifie qu'il existe une valeur pour peugeot et que le nombre d'occurrences est correct
		boolean found = false;
		final String make = "peugeot";
		for (final Entry<FacetValue, Long> entry : makeFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().equals(make)) {
				found = true;
				//System.out.println("make" + entry.getKey().getLabel().getDisplay());
				Assert.assertEquals(carDataBase.getByMake(make).size(), entry.getValue().intValue());
			}
		}
		Assert.assertEquals(true, found);
	}

	private Facet getFacetByName(final FacetedQueryResult<Car, ?> result, final String facetName) {
		for (final Facet facet : result.getFacets()) {
			if (facetName.equals(facet.getDefinition().getName())) {
				return facet;
			}
		}
		return null;
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemove() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//On en supprime 2 
		remove(2);
		final long resize = query("*:*");
		Assert.assertEquals(carDataBase.size() - 2, resize);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveByQuery() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//on compte les Peugeots
		int nbPeugeot = 0;
		for (final Car car : carDataBase) {
			if ("Peugeot".equals(car.getMake())) {
				nbPeugeot++;
			}
		}
		//On supprime toute les Peugeots 
		remove("MAKE:Peugeot");
		final long resize = query("*:*");
		Assert.assertEquals(carDataBase.size() - nbPeugeot, resize);
	}

	/**
	 * Test de requétage de l'index.
	 * La création s'effectue dans une seule transaction.
	 */
	@Test
	public void testRemoveAll() {
		index(false);
		final long size = query("*:*");
		Assert.assertEquals(carDataBase.size(), size);
		//On supprime tout 
		remove("*:*");
		final long resize = query("*:*");
		Assert.assertEquals(0L, resize);
	}

	/**
	 * Test le facettage par range d'une liste.
	 */
	@Test
	public void testFacetListByRange() {
		final DtList<Car> cars = new DtList<>(Car.class);
		for (final Car car : carDataBase) {
			cars.add(car);
		}
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		testFacetResultByRange(result);
	}

	/**
	 * Test le facettage par range d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByRange() {
		final DtList<Car> cars = new DtList<>(Car.class);
		for (final Car car : carDataBase) {
			cars.add(car);
		}
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		//on applique une facette
		final FacetedQuery query = addFacetQuery("FCT_YEAR" + facetSuffix, "avant", result);
		final FacetedQueryResult<Car, DtList<Car>> resultFiltered = collectionsManager.facetList(result.getSource(), query);
		Assert.assertEquals(carDataBase.before(2000), resultFiltered.getCount());
	}

	private FacetedQuery addFacetQuery(final String facetName, final String facetValueLabel, final FacetedQueryResult<Car, ?> result) {
		FacetValue facetValue = null; //pb d'initialisation, et assert.notNull ne suffit pas 
		final Facet yearFacet = getFacetByName(result, facetName);
		for (final Entry<FacetValue, Long> entry : yearFacet.getFacetValues().entrySet()) {
			if (entry.getKey().getLabel().getDisplay().toLowerCase().contains(facetValueLabel)) {
				facetValue = entry.getKey();
				break;
			}
		}
		if (facetValue == null) {
			throw new IllegalArgumentException("Pas de FacetValue contenant " + facetValueLabel + " dans la facette " + facetName);
		}
		final FacetedQuery previousQuery = result.getFacetedQuery();
		final List<ListFilter> queryFilters = new ArrayList<>(previousQuery.getListFilters());
		queryFilters.add(facetValue.getListFilter());
		return new FacetedQuery(previousQuery.getDefinition(), queryFilters);
	}

	/**
	 * Test le facettage par term d'une liste.
	 */
	@Test
	public void testFacetListByTerm() {
		final DtList<Car> cars = new DtList<>(Car.class);
		for (final Car car : carDataBase) {
			cars.add(car);
		}
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		testFacetResultByTerm(result);
	}

	/**
	 * Test le facettage par term d'une liste.
	 * Et le filtrage par une facette.
	 */
	@Test
	public void testFilterFacetListByTerm() {
		final DtList<Car> cars = new DtList<>(Car.class);
		for (final Car car : carDataBase) {
			cars.add(car);
		}
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		final FacetedQueryResult<Car, DtList<Car>> result = collectionsManager.facetList(cars, facetedQuery);
		//on applique une facette
		final FacetedQuery query = addFacetQuery("FCT_MAKE" + facetSuffix, "peugeot", result);
		final FacetedQueryResult<Car, DtList<Car>> resultFiltered = collectionsManager.facetList(result.getSource(), query);
		Assert.assertEquals(carDataBase.getByMake("peugeot").size(), (int) resultFiltered.getCount());
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------

	private long query(final String query) {
		return doQuery(query);

	}

	private FacetedQueryResult<Car, SearchQuery> facetQuery(final String query) {
		return doFacetQuery(query);

	}

	private void index(final boolean all) {
		doIndex(all);

	}

	private void remove(final int count) {
		doRemove(count);

	}

	private void remove(final String query) {
		doRemove(query);

	}

	private static URI<Car> createURI(final Car car) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		return new URI<>(dtDefinition, DtObjectUtil.getId(car));
	}

	private void doIndex(final boolean all) {
		if (all) {
			final List<Index<Car, Car>> indexes = new ArrayList<>();
			for (final Car car : carDataBase) {
				indexes.add(Index.createIndex(carIndexDefinition, createURI(car), car, car));
			}
			searchManager.getSearchServices().putAll(carIndexDefinition, indexes);
		} else {
			//Indexation unitaire
			//Indexation des cars de la base 
			for (final Car car : carDataBase) {
				final Index<Car, Car> index = Index.createIndex(carIndexDefinition, createURI(car), car, car);
				searchManager.getSearchServices().put(carIndexDefinition, index);
			}
		}
	}

	private void doRemove(final int count) {
		//Suppression de n voitures 
		for (long id = 0; id < count; id++) {
			searchManager.getSearchServices().remove(carIndexDefinition, createURI(id));
		}
	}

	private void doRemove(final String query) {
		final ListFilter removeQuery = new ListFilter(query);
		searchManager.getSearchServices().remove(carIndexDefinition, removeQuery);
	}

	private long doQuery(final String query) {
		//recherche 
		final ListFilter listFilter = new ListFilter(query);
		final SearchQuery searchQuery = SearchQuery.createSearchQuery(carIndexDefinition, listFilter);
		return doQuery(searchQuery).getCount();
	}

	private <D extends DtObject> D doQueryAndGetFirst(final String query, final String sortField, final boolean sortAsc) {
		//recherche 
		final ListFilter listFilter = new ListFilter(query);
		final SearchQuery searchQuery = SearchQuery.createSearchQuery(carIndexDefinition, listFilter, carIndexDefinition.getIndexDtDefinition().getField(sortField), sortAsc);
		final DtList<D> dtList = (DtList<D>) doQuery(searchQuery).getDtList();
		return dtList.get(0);
	}

	private FacetedQueryResult<DtObject, SearchQuery> doQuery(final SearchQuery searchQuery) {
		//recherche 
		final FacetedQuery facetedQuery = new FacetedQuery(carQueryDefinition, Collections.<ListFilter> emptyList());
		return searchManager.getSearchServices().loadList(searchQuery, facetedQuery);
	}

	private FacetedQueryResult<Car, SearchQuery> doFacetQuery(final String query) {
		final ListFilter listFilter = new ListFilter(query);
		final SearchQuery searchQuery = SearchQuery.createSearchQuery(carIndexDefinition, listFilter);
		final FacetedQuery facetedQuery = new FacetedQuery(carFacetQueryDefinition, Collections.<ListFilter> emptyList());
		return searchManager.getSearchServices().loadList(searchQuery, facetedQuery);
	}

	//		//recherche 
	//		final SearchResult<Car> searchResult = searchManager.getSearchServices().loadList(carQueryDefinition, "*:*", Collections.<String> emptyList());
	//		Assert.assertEquals(expectedCount, searchResult.getCount());
	//		System.out.println(">>>count = " + searchResult.getCount());
	//		final DtList<Car> cars = searchResult.getDtList();
	//		System.out.println(">>>voitures = " + cars.size());
	//		for (final Car car : cars) {
	//			System.out.println(">>>voiture : " + car);
	//		}
	//		System.out.println(">>>Nombre de facettes : " + searchResult.getFacets().size());
	//		System.out.println(">>>Facettes de type fieldValue");
	//		for (final Facet facet : searchResult.getFacets()) {
	//			for (final Entry<FacetFilter, Long> fieldValueEntry : facet.getFacetValues().entrySet()) {
	//				System.out.println(">>>------fieldValues : " + fieldValueEntry.getKey().getLabel() + "::" + fieldValueEntry.getValue());
	//			}
	//		}
	//		System.out.println(">>>Facettes de type range");
	//		System.out.println(">>>Facettes de type date");
	//	}

	private URI<DtObject> createURI(final long id) {
		return new URI<>(DtObjectUtil.findDtDefinition(Car.class), id);
	}
}