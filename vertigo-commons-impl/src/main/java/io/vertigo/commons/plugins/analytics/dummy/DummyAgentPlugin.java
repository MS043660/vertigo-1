package io.vertigo.commons.plugins.analytics.dummy;

import io.vertigo.commons.impl.analytics.AnalyticsAgentPlugin;

/**
 * Impl�mentation dummy de l'agent de collecte.
 * Cette impl�mentation ne fait RIEN.
 * @author pchretien
 * @version $Id: DummyAgentPlugin.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
 */
public final class DummyAgentPlugin implements AnalyticsAgentPlugin {
	/** {@inheritDoc} */
	public void startProcess(final String processType, final String processName) {
		//
	}

	/** {@inheritDoc} */
	public void incMeasure(final String measureType, final double value) {
		//
	}

	/** {@inheritDoc} */
	public void setMeasure(final String measureType, final double value) {
		//
	}

	/** {@inheritDoc} */
	public void addMetaData(final String metaDataName, final String value) {
		//
	}

	/** {@inheritDoc} */
	public void stopProcess() {
		//
	}
}