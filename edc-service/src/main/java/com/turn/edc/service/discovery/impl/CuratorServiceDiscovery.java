/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.discovery.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.CuratorSerializer;
import com.turn.edc.service.discovery.ServiceDiscovery;

import com.google.common.net.HostAndPort;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Curator (zookeeper) service discovery connector
 *
 * @author tshiou
 */
public class CuratorServiceDiscovery implements ServiceDiscovery {
	private static final Logger LOG = LoggerFactory.getLogger(CuratorServiceDiscovery.class);

	private static final String BASE_PATH = "/edc";

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

		this.serviceRegistry = ServiceDiscoveryBuilder.builder(CacheInstance.class)
				.client(this.curator)
				.basePath(BASE_PATH)
				.serializer(new CuratorSerializer())
				.build();

	}

	@Override
	public void start() {
		this.curator.start();
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
			LOG.error("Unable to register instance!", e);
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
