/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.discovery.ServiceDiscovery;

import java.io.IOException;
import java.util.List;

import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.agent.Agent;

/**
 * Consul-based service discovery connector
 *
 * Uses Orbitz's Consul HTTP API
 *
 * @author tshiou
 */
public class ConsulServiceDiscovery extends DiscoveryListener implements ServiceDiscovery {

	private final Consul consul;
	private final ServiceHealthCache servicesCache;
	private List<CacheInstance> liveInstances;


	public ConsulServiceDiscovery(String consulURL, String serviceName) {

		this.consul = Consul.builder().build(); // connect to Consul on localhost
		HealthClient healthClient = consul.healthClient();

		this.servicesCache = ServiceHealthCache.newCache(healthClient, serviceName);

		attachListeners(this);
	}

	@Override
	public void start() throws IOException {
		try {
			servicesCache.start();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void shutdown() {
	}

	public List<CacheInstance> getAvailableInstances() {
		return liveInstances;
	}

	@Override
	public void attachListeners(DiscoveryListener... listeners) {
		for (DiscoveryListener listener : listeners) {
			this.servicesCache.addListener(new ConsulCacheListener(consul, listener));
		}
	}

	@Override
	public void update(List<CacheInstance> instances) {
		this.liveInstances = instances;
	}
}
