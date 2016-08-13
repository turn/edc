/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

	private Consul consul;
	private AtomicReference<List<CacheInstance>> servicesListReference;
	// TODO: We can make this GC free by keeping the old reference and swapping

	public ConsulCacheListener(Consul consul, AtomicReference<List<CacheInstance>> servicesListReference) {
		this.consul = consul;
		this.servicesListReference = servicesListReference;
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

		servicesListReference.getAndSet(newList);
	}
}
