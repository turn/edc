/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.ServiceDiscovery;
import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.router.StoreEventRouter;
import com.turn.edc.selection.CacheInstanceSelector;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.orbitz.consul.Consul;
import com.orbitz.consul.HealthClient;
import com.orbitz.consul.cache.ServiceHealthCache;

/**
 * Add class description
 *
 * @author tshiou
 */
public class ConsulServiceDiscovery implements ServiceDiscovery {

	private final ServiceHealthCache servicesCache;
	private AtomicReference<List<CacheInstance>> servicesListReference;

	public ConsulServiceDiscovery(String consulURL) {

		List<CacheInstance> initialMap = Lists.newArrayList();
		this.servicesListReference = new AtomicReference<>(initialMap);



		Consul consul = Consul.builder().build(); // connect to Consul on localhost
		HealthClient healthClient = consul.healthClient();

		this.servicesCache = ServiceHealthCache.newCache(healthClient, "edc-cluster");
		this.servicesCache.addListener(new ConsulCacheListener(consul, servicesListReference));

		try {
			servicesCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(StoreEventRouter router, CacheInstanceSelector selector) {

	}

	public List<CacheInstance> getAvailableInstances() {
		return servicesListReference.get();
	}

}
