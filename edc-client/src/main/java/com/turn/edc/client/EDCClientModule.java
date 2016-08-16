/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.client;

import com.turn.edc.discovery.ServiceDiscovery;
import com.turn.edc.storage.ConnectionFactory;

import com.google.inject.AbstractModule;

/**
 * Add class description
 *
 * @author tshiou
 */
class EDCClientModule extends AbstractModule {

	private final ConnectionFactory connectorFactory;
	private final ServiceDiscovery discovery;

	EDCClientModule(
			ConnectionFactory connectorFactory,
			ServiceDiscovery discovery
	) {
		this.connectorFactory = connectorFactory;
		this.discovery = discovery;
	}


	@Override
	protected void configure() {
		bind(ServiceDiscovery.class).toInstance(this.discovery);
		bind(ConnectionFactory.class).toInstance(this.connectorFactory);
	}

}
