package io.vertigo.dynamo.environment.oom;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.kernel.Home;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de lecture d'un OOM.
 *
 * @author pchretien
 * @version $Id: TestParserOOM.java,v 1.3 2014/01/20 17:51:47 pchretien Exp $
 */
public class TestParserOOM extends AbstractTestCaseJU4 {
	@Override
	protected String getManagersXmlFileName() {
		return "./managers-test-assoc.xml";
	}

	/*
	 * Conventions de nommage utilisées pour les tests ci dessous.
	 * - Relation de A vers B
	 * - Cardinalité notée 	1 ou n
	 * - Navigabilité notée v 
	 */

	private AssociationDefinition getAssociationDefinition(final String urn) {
		return Home.getDefinitionSpace().resolve(urn, AssociationDefinition.class);
	}

	/**
	 * Test d'une relation A1 - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1Bnv() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_1");
		Assert.assertNotNull(association);
		/* "0..1" */
		Assert.assertEquals(false, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R1A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R1B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(false, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());

	}

	/**
	 * Test d'une relation A1v - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBnv() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_2");
		/* "0..1" */
		Assert.assertEquals(false, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R2A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R2B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation A1v - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationA1vBn() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_3");
		/* "0..1" */
		Assert.assertEquals(false, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R3A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R3B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(false, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnB1v() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_4");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..1" */
		Assert.assertEquals(false, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R4A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R4B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(false, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - B1.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_5");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(false, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R5A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R5B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(false, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - B1v.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvB1v() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_6");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..1" */
		Assert.assertEquals(false, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R6A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R6B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBnv() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_7");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R7A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R7B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(false, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - Bnv.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBnv() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_8");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R8A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R8B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(true, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation An - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnBn() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_9");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R9A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R9B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(false, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(false, association.getAssociationNodeB().isNavigable());
	}

	/**
	 * Test d'une relation Anv - Bn.
	 * @throws Exception si erreur
	 */
	@Test
	public void testAssoctationAnvBn() {
		final AssociationDefinition association = getAssociationDefinition("A_CHA_CHI_10");
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeA().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeA().isNotNull());
		/* "0..*" */
		Assert.assertEquals(true, association.getAssociationNodeB().isMultiple());
		Assert.assertEquals(false, association.getAssociationNodeB().isNotNull());

		Assert.assertEquals("R10A", association.getAssociationNodeA().getRole());
		Assert.assertEquals("R10B", association.getAssociationNodeB().getRole());

		Assert.assertEquals(true, association.getAssociationNodeA().isNavigable());
		Assert.assertEquals(false, association.getAssociationNodeB().isNavigable());
	}
}