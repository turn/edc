/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener to changes in Consul cache
 *
 * @see ConsulCache
 *
 * @author tshiou
 */
public class ConsulCacheListener implements ConsulCache.Listener<ServiceHealthKey, ServiceHealth> {

	private static final Logger LOG = LoggerFactory.getLogger(ConsulCacheListener.class);

	private final Consul consul;
	private final DiscoveryListener listener;
	// TODO: We can make this GC free by keeping the old reference and swapping

	public ConsulCacheListener(Consul consul, DiscoveryListener listener) {
		this.consul = consul;
		this.listener = listener;
	}

	/**
	 * Updates the cache size distribution
	 *
	 */
	@Override
	public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
		LOG.debug("Service change notification received");
		List<CacheInstance> newList = Lists.newArrayList();

		for (ServiceHealth instance : newValues.values()) {
			// Get cache instance host/port from consul health key
			HostAndPort hostAndPort = HostAndPort.fromParts(
					instance.getNode().getAddress(), instance.getService().getPort());
			String cacheInstanceString = hostAndPort.toString() + "-";

			// Try getting cache size from kv-store
			Optional<String> sizeLookup = consul.keyValueClient().getValueAsString(hostAndPort.toString());
			if (sizeLookup.isPresent()) {
				cacheInstanceString += sizeLookup.get();
			}

			newList.add(CacheInstance.fromString(cacheInstanceString));
		}
		LOG.debug("New instance list created: {}", newList);

		this.listener.update(newList);
	}
}
