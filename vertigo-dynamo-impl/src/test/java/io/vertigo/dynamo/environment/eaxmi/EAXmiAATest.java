package io.vertigo.dynamo.environment.eaxmi;

import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiAssociation;
import io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core.EAXmiLoader;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test de lecture d'un xmi AutoAssociation.
 *
 * @author pchretien
 * @version $Id: EAXmiAATest.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public class EAXmiAATest {
	private static final Logger LOGGER = Logger.getLogger(EAXmiAATest.class);
	private Map<String, EAXmiAssociation> map;

	@Before
	public void setUp() throws Exception {
		final File xmiFile = TestUtil.getFile("data/associationAA.xml", getClass());
		final URL xmiURL = xmiFile.toURI().toURL();

		final EAXmiLoader loader = new EAXmiLoader(xmiURL);
		map = new HashMap<>();
		for (final EAXmiAssociation association : loader.getAssociationList()) {
			map.put(association.getCode(), association);
			LOGGER.trace("> code = " + association.getCode());
		}
		LOGGER.trace(">> nb ass.=" + loader.getAssociationList().size());

	}

	@After
	public void tearDown() {
		map = null;
	}

	/*
	 * Conventions de nommage utilisées pour les tests ci dessous.
	 * - Relation de A vers B
	 * - Cardinalité notée 	1 ou n
	 * - Navigabilité notée v 
	 */

	/**
	 * Test d'une relation A1 - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1Bnv() {
		final EAXmiAssociation association = map.get("CHI_CHI_1");
		Assert.assertEquals("0..1", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R1A", association.getRoleLabelA());
		Assert.assertEquals("R1B", association.getRoleLabelB());

		Assert.assertEquals(false, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBnv() {
		final EAXmiAssociation association = map.get("CHI_CHI_2");
		Assert.assertEquals("0..1", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R2A", association.getRoleLabelA());
		Assert.assertEquals("R2B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation A1v - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBn() {
		final EAXmiAssociation association = map.get("CHI_CHI_3");
		Assert.assertEquals("0..1", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R3A", association.getRoleLabelA());
		Assert.assertEquals("R3B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(false, association.isNavigableB());
	}

	/**
	 * Test d'une relation An - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnB1v() {
		final EAXmiAssociation association = map.get("CHI_CHI_4");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..1", association.getMultiplicityB());

		Assert.assertEquals("R4A", association.getRoleLabelA());
		Assert.assertEquals("R4B", association.getRoleLabelB());

		Assert.assertEquals(false, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1() {
		final EAXmiAssociation association = map.get("CHI_CHI_5");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..1", association.getMultiplicityB());

		Assert.assertEquals("R5A", association.getRoleLabelA());
		Assert.assertEquals("R5B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(false, association.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1v() {
		final EAXmiAssociation association = map.get("CHI_CHI_6");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..1", association.getMultiplicityB());

		Assert.assertEquals("R6A", association.getRoleLabelA());
		Assert.assertEquals("R6B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBnv() {
		final EAXmiAssociation association = map.get("CHI_CHI_7");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R7A", association.getRoleLabelA());
		Assert.assertEquals("R7B", association.getRoleLabelB());

		Assert.assertEquals(false, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBnv() {
		final EAXmiAssociation association = map.get("CHI_CHI_8");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R8A", association.getRoleLabelA());
		Assert.assertEquals("R8B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(true, association.isNavigableB());
	}

	/**
	 * Test d'une relation An - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBn() {
		final EAXmiAssociation association = map.get("CHI_CHI_9");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R9A", association.getRoleLabelA());
		Assert.assertEquals("R9B", association.getRoleLabelB());

		Assert.assertEquals(false, association.isNavigableA());
		Assert.assertEquals(false, association.isNavigableB());
	}

	/**
	 * Test d'une relation Anv - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBn() {
		final EAXmiAssociation association = map.get("CHI_CHI_10");
		Assert.assertEquals("0..*", association.getMultiplicityA());
		Assert.assertEquals("0..*", association.getMultiplicityB());

		Assert.assertEquals("R10A", association.getRoleLabelA());
		Assert.assertEquals("R10B", association.getRoleLabelB());

		Assert.assertEquals(true, association.isNavigableA());
		Assert.assertEquals(false, association.isNavigableB());
	}

}