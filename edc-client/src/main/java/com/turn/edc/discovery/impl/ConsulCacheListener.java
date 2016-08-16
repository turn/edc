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
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.ServiceHealthKey;
import com.orbitz.consul.model.health.ServiceHealth;

/**
 * Add class description
 *
 * @author tshiou
 */
public class ConsulCacheListener implements ConsulCache.Listener<ServiceHealthKey, ServiceHealth> {

	private final Consul consul;
	private final DiscoveryListener listener;
	// TODO: We can make this GC free by keeping the old reference and swapping

	public ConsulCacheListener(Consul consul, DiscoveryListener listener) {
		this.consul = consul;
		this.listener = listener;
	}

	@Override
	public void notify(Map<ServiceHealthKey, ServiceHealth> newValues) {
		List<CacheInstance> newList = Lists.newArrayList();
		KeyValueClient kvClient = consul.keyValueClient();
		for (ServiceHealthKey serviceKey : newValues.keySet()) {
			HostAndPort hostAndPort = HostAndPort.fromParts(serviceKey.getHost(), serviceKey.getPort());
			Optional<String> lookup = kvClient.getValueAsString(hostAndPort.toString());
			String cacheInstanceString;
			if (lookup.isPresent()) {
				cacheInstanceString = lookup.get();
			} else {
				cacheInstanceString = "";
			}

			newList.add(CacheInstance.fromString(cacheInstanceString));
		}

		this.listener.update(newList);
	}
}
