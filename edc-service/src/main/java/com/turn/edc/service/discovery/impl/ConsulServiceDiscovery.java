/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.discovery.impl;

import com.turn.edc.service.discovery.ServiceDiscovery;

/**
 * Add class description
 *
 * @author tshiou
 */
public class ConsulServiceDiscovery implements ServiceDiscovery {
	private final String consulURL;
	private final String serviceType;
	private final String serviceHost;
	private final int servicePort;

	public ConsulServiceDiscovery(
			String consulURL,
			String serviceType,
	        String serviceHost,
			int servicePort) {
		this.consulURL = consulURL;
		this.serviceType = serviceType;
		this.serviceHost = serviceHost;
		this.servicePort = servicePort;
	}

	@Override
	public void register(int cacheSize) throws Exception {

	}

	@Override
	public void unregister() {

	}

	@Override
	public void close() {

	}

	@Override
	public boolean isRegistered() {
		return false;
	}
}
