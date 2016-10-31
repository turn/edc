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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CuratorCacheListener implements ServiceCacheListener {

	private static final Logger LOG = LoggerFactory.getLogger(CuratorCacheListener.class);

	private final ServiceCache<CacheInstance> serviceCache;
	private final DiscoveryListener listener;

	public CuratorCacheListener(ServiceCache<CacheInstance> serviceCache, DiscoveryListener listener) {
		this.serviceCache = serviceCache;
		this.listener = listener;
	}

	@Override
	public void cacheChanged() {
		LOG.debug("Curator discovery changed. Updating local cache...");
		List<CacheInstance> newList = Lists.newArrayList();

		List<ServiceInstance<CacheInstance>> instances = this.serviceCache.getInstances();
		LOG.debug("Found {} instances", this.serviceCache.getInstances().size());
		for (ServiceInstance<CacheInstance> instance : instances) {
			newList.add(instance.getPayload());
		}

		LOG.debug("Committing new cache of size: {}", newList.size());
		this.listener.update(newList);
	}

	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
	}
}
