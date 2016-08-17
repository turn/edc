/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.service.discovery.ServiceDiscovery;

import com.google.common.net.HostAndPort;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CuratorServiceDiscovery implements ServiceDiscovery {
	private final String serviceType;
	private final String serviceHost;
	private final int servicePort;

	private final org.apache.curator.x.discovery.ServiceDiscovery<CacheInstance> serviceRegistry;
	private final CuratorFramework curator;

	private ServiceInstance<CacheInstance> serviceInstance = null;

	public CuratorServiceDiscovery(
			String zkConnectionString,
			String serviceType,
			String serviceHost,
			int servicePort) {
		this.serviceType = serviceType;
		this.serviceHost = serviceHost;
		this.servicePort = servicePort;

		this.curator = CuratorFrameworkFactory.newClient(zkConnectionString, new ExponentialBackoffRetry(1000, 3));
		JsonInstanceSerializer<CacheInstance> serializer =
				new JsonInstanceSerializer<>(CacheInstance.class);

		this.serviceRegistry = ServiceDiscoveryBuilder.builder(CacheInstance.class)
				.client(this.curator)
				.basePath("/service-discovery/edc")
				.serializer(serializer)
				.build();

	}


	@Override
	public void register(int cacheSize) throws Exception {

		CacheInstance instance = new CacheInstance(
				HostAndPort.fromParts(this.serviceHost, this.servicePort), cacheSize);

		serviceInstance = ServiceInstance.<CacheInstance> builder()
				.name(this.serviceType)
				.payload(instance)
				.build();

		serviceRegistry.registerService(serviceInstance);

		serviceRegistry.start();
	}

	@Override
	public void unregister() {
		if (isRegistered() == false) {
			return;
		}


		try {
			serviceRegistry.unregisterService(serviceInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		unregister();

		CloseableUtils.closeQuietly(serviceRegistry);
		CloseableUtils.closeQuietly(curator);
	}

	@Override
	public boolean isRegistered() {
		ServiceInstance instance;
		try {
			instance = serviceRegistry.queryForInstance(
					this.serviceInstance.getName(), this.serviceInstance.getId());
		} catch (Exception e) {
			return false;
		}

		return instance != null;
	}
}
