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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Consul-based service discovery connector
 *
 * Uses Orbitz's Consul HTTP API
 *
 * @author tshiou
 */
public class ConsulServiceDiscovery extends DiscoveryListener implements ServiceDiscovery {

	private static final Logger LOG = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

	private static final int AWAIT_INITIALIZATION_SEC = 10;

	private final Consul consul;
	private final ServiceHealthCache servicesCache;
	private List<CacheInstance> liveInstances;
	private List<DiscoveryListener> listeners = Lists.newArrayList();

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
			servicesCache.awaitInitialized(AWAIT_INITIALIZATION_SEC, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new IOException(e);
		}
		initializeInstances(servicesCache);
		for (DiscoveryListener listener : this.listeners) {
			listener.update(this.liveInstances);
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
			this.listeners.add(listener);
		}
	}

	@Override
	public void update(List<CacheInstance> instances) {
		this.liveInstances = instances;
	}

	private void initializeInstances(ServiceHealthCache initializedCache) {
		List<CacheInstance> newList = Lists.newArrayList();

		Map<ServiceHealthKey, ServiceHealth> instances = initializedCache.getMap();
		for (ServiceHealthKey serviceKey : instances.keySet()) {
			// Get cache instance host/port from consul health key
			HostAndPort hostAndPort = HostAndPort.fromParts(serviceKey.getHost(), serviceKey.getPort());
			String cacheInstanceString = hostAndPort.toString() + "-";

			// Try getting cache size from kv-store
			Optional<String> sizeLookup = consul.keyValueClient().getValueAsString(hostAndPort.toString());
			if (sizeLookup.isPresent()) {
				cacheInstanceString += sizeLookup.get();
			}

			newList.add(CacheInstance.fromString(cacheInstanceString));
		}

		update(newList);
	}
}
