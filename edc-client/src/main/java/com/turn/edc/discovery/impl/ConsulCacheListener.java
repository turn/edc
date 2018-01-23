/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
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

	private final KVCache kvCache;
	private final DiscoveryListener listener;
	// TODO: We can make this GC free by keeping the old reference and swapping

	public ConsulCacheListener(KVCache kvCache, DiscoveryListener listener) {
		this.kvCache = kvCache;
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
					instance.getService().getAddress(), instance.getService().getPort());

			// Try getting cache size from kv cache, otherwise 0
			int cacheSize = 0;
			try {
				Optional<String> lookup =
						this.kvCache.getMap()
								.get(hostAndPort.toString())
								.getValueAsString();
				cacheSize = Integer.parseInt(lookup.orElse("0"));
			} catch (Exception e) {
				LOG.debug("KV lookup failed, default cache size of 0 will be used for {}",
						hostAndPort.toString());
			}

			newList.add(new CacheInstance(hostAndPort, cacheSize));
		}
		LOG.debug("New instance list created: {}", newList);

		this.listener.update(newList);
	}
}
