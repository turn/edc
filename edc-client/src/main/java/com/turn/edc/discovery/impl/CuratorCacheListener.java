/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.ServiceCacheListener;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CuratorCacheListener implements ServiceCacheListener {

	private final ServiceCache<CacheInstance> serviceCache;
	private AtomicReference<List<CacheInstance>> servicesListReference;

	public CuratorCacheListener(ServiceCache<CacheInstance> serviceCache, AtomicReference<List<CacheInstance>> servicesMapReference) {
		this.serviceCache = serviceCache;
		this.servicesListReference = servicesMapReference;
	}

	@Override
	public void cacheChanged() {
		List<CacheInstance> newMap = Lists.newArrayList();

		List<ServiceInstance<CacheInstance>> instances = this.serviceCache.getInstances();
		for (ServiceInstance<CacheInstance> instance : instances) {
			newMap.add(instance.getPayload());
		}

		servicesListReference.getAndSet(newMap);
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {

	}
}
