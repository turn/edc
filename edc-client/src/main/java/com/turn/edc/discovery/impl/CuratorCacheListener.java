/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;

import java.util.List;

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
	private final DiscoveryListener listener;

	public CuratorCacheListener(ServiceCache<CacheInstance> serviceCache, DiscoveryListener listener) {
		this.serviceCache = serviceCache;
		this.listener = listener;
	}

	@Override
	public void cacheChanged() {
		List<CacheInstance> newList = Lists.newArrayList();

		List<ServiceInstance<CacheInstance>> instances = this.serviceCache.getInstances();
		for (ServiceInstance<CacheInstance> instance : instances) {
			newList.add(instance.getPayload());
		}

		this.listener.update(newList);
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {

	}
}
