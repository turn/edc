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
import com.google.inject.Inject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.x.discovery.ServiceCache;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CuratorServiceDiscovery implements ServiceDiscovery {

	@Inject
	private CuratorFramework curator;

	private org.apache.curator.x.discovery.ServiceDiscovery serviceDiscovery;

	private ServiceCache serviceCache;

	private CuratorCacheListener cacheListener;

	private AtomicReference<List<CacheInstance>> servicesListReference;

	@SuppressWarnings("unchecked")
	public CuratorServiceDiscovery(String zkConnectionString) {

		if (curator.getState() == CuratorFrameworkState.LATENT) {
			curator.start();
		}

		JsonInstanceSerializer<CacheInstance> serializer =
				new JsonInstanceSerializer<>(CacheInstance.class);

		List<CacheInstance> initialMap = Lists.newArrayList();
		this.servicesListReference = new AtomicReference<>(initialMap);

		this.serviceDiscovery = ServiceDiscoveryBuilder.builder(CacheInstance.class)
				.client(curator)
				.basePath("/service-discovery/edc")
				.serializer(serializer)
				.build();

		try {
			this.serviceDiscovery.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Service discovery provider
		ServiceProvider provider = serviceDiscovery.serviceProviderBuilder()
				.serviceName(CacheInstance.EDC_SERVICE_NAME)
				.providerStrategy(new RandomStrategy<CacheInstance>())
				.build();

		try {
			provider.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			serviceDiscovery.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.serviceCache = serviceDiscovery.serviceCacheBuilder()
				.name(CacheInstance.EDC_SERVICE_NAME)
				.build();

		// Could throw Exception
		try {
			this.serviceCache.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.cacheListener = new CuratorCacheListener(this.serviceCache, this.servicesListReference);
		this.serviceCache.addListener(this.cacheListener);

	}

	@Override
	public void initialize(StoreEventRouter router, CacheInstanceSelector selector) {

	}

	public List<CacheInstance> getAvailableInstances() {
		return servicesListReference.get();
	}
}
