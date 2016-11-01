/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.discovery.ServiceDiscovery;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.cache.ServiceHealthCache;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.health.ServiceHealth;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

	private final String serviceName;
	private final String consulURL;
	private final int consulPort;

	private Consul consul;
	private ServiceHealthCache servicesCache;
	private KVCache kvCache;
	private List<CacheInstance> liveInstances = Lists.newArrayList();
	private List<DiscoveryListener> listeners = Lists.newLinkedList();

	public ConsulServiceDiscovery(String consulURL, int consulPort, String serviceName) {
		this.serviceName = serviceName;
		this.consulURL = consulURL;
		this.consulPort = consulPort;
	}

	@Override
	public void start() throws IOException {
		this.consul = Consul.builder().withUrl(new URL("http", consulURL, consulPort, "")).build();
		this.liveInstances = getLiveInstances(this.consul, this.serviceName);
		for (DiscoveryListener listener : this.listeners) {
			listener.update(this.liveInstances);
		}

		this.kvCache = KVCache.newCache(this.consul.keyValueClient(), serviceName);
		try {
			this.kvCache.start();
		} catch (Exception e) {
			LOG.warn("Consul key-value cache failed to start so cache sizes will not be taken into account in the selection layer");
			LOG.debug(ExceptionUtils.getStackTrace(e));

		}
		this.servicesCache = ServiceHealthCache.newCache(consul.healthClient(), serviceName);
		this.servicesCache.addListener(new ConsulCacheListener(this.kvCache, this));
		try {
			this.servicesCache.start();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void shutdown() {
		try {
			servicesCache.stop();
		} catch (Exception e) {
			LOG.error("Failed to stop consul service cache: " + ExceptionUtils.getStackTrace(e));
		}
		try {
			kvCache.stop();
		} catch (Exception e) {
			LOG.error("Failed to stop consul key-value cache: " + ExceptionUtils.getStackTrace(e));
		}
	}

	public List<CacheInstance> getAvailableInstances() {
		return liveInstances;
	}

	@Override
	public void attachListeners(DiscoveryListener... listeners) {
		for (DiscoveryListener listener : listeners) {
			this.listeners.add(listener);
			listener.update(this.liveInstances);
		}
	}

	@Override
	public void update(List<CacheInstance> instances) {
		this.liveInstances = instances;
		for (DiscoveryListener listener : this.listeners) {
			listener.update(this.liveInstances);
		}
	}


	private List<CacheInstance> getLiveInstances(Consul consul, String serviceName) {
		List<CacheInstance> newList = Lists.newArrayList();
		ConsulResponse<List<ServiceHealth>> consulRequest =
				consul.healthClient().getAllServiceInstances(serviceName);

		for (ServiceHealth instance : consulRequest.getResponse()) {
			// Get cache instance host/port from consul health key
			HostAndPort hostAndPort = HostAndPort.fromParts(
					instance.getService().getAddress(), instance.getService().getPort());

			// Try getting cache size from kv-store
			Optional<String> sizeLookup = consul.keyValueClient().getValueAsString(hostAndPort.toString());
			int cacheSize = Integer.parseInt(sizeLookup.or("0"));

			newList.add(new CacheInstance(hostAndPort, cacheSize));
		}

		return newList;
	}

}
