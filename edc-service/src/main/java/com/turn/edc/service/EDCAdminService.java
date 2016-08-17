/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service;

import com.turn.edc.service.discovery.ServiceDiscovery;
import com.turn.edc.service.discovery.impl.ConsulServiceDiscovery;
import com.turn.edc.service.discovery.impl.CuratorServiceDiscovery;
import com.turn.edc.service.storage.StorageAdmin;
import com.turn.edc.service.storage.impl.JedisStorageAdmin;
import com.turn.edc.service.storage.impl.SpymemcachedStorageAdmin;

/**
 * Add class description
 *
 * @author tshiou
 */
public class EDCAdminService {

	private final StorageAdmin admin;
	private final ServiceDiscovery discovery;

	private EDCAdminService(StorageAdmin admin, ServiceDiscovery discovery) {
		this.admin = admin;
		this.discovery = discovery;
	}

	public void start() {

	}


	public static Builder.EDCStorageBuilder builder() {
		return new Builder.EDCStorageBuilder();
	}

	/***************************************** Builder *******************************************/

	public static class Builder {

		private final StorageAdmin admin;
		private final ServiceDiscovery discovery;

		Builder(StorageAdmin admin, ServiceDiscovery discovery) {
			this.admin = admin;
			this.discovery = discovery;
		}

		public EDCAdminService build() {
			return new EDCAdminService(this.admin, this.discovery);
		}

		public static class EDCStorageBuilder {

			EDCStorageBuilder() {}

			public StorageBuilder withRedisStorage(String host, int port) {
				return new RedisStorageBuilder(host, port);
			}

			public StorageBuilder withMemcachedStorage(String host, int port) {
				return new MemcachedStorageBuilder(host, port);
			}
		}

		public static abstract class StorageBuilder {
			protected final String host;
			protected final int port;

			StorageBuilder(String host, int port) {
				this.host = host;
				this.port = port;
			}

			abstract StorageAdmin createStorageAdmin();

			public ServiceDiscoveryBuilder withZkServiceDiscovery(String zkConnectionString) {
				return new ZkServiceDiscoveryBuilder(
						createStorageAdmin(),
						this.host,
						this.port,
						zkConnectionString
				);
			}

			public ServiceDiscoveryBuilder withConsulServiceDiscovery(String consulURL) {
				return new ConsulServiceDiscoveryBuilder(
						createStorageAdmin(),
						this.host,
						this.port,
						consulURL
				);
			}
		}

		static class RedisStorageBuilder extends StorageBuilder  {

			RedisStorageBuilder(String host, int port) {
				super(host, port);
			}

			StorageAdmin createStorageAdmin() {
				return new JedisStorageAdmin(this.host, this.port);
			}

		}

		static class MemcachedStorageBuilder extends StorageBuilder {
			MemcachedStorageBuilder(String host, int port) {
				super(host, port);
			}

			StorageAdmin createStorageAdmin() {
				return new SpymemcachedStorageAdmin(this.host, this.port);
			}
		}

		public static abstract class ServiceDiscoveryBuilder {
			private final StorageAdmin storage;
			protected final String serviceHost;
			protected final int servicePort;

			ServiceDiscoveryBuilder(
					StorageAdmin storage,
			        String serviceHost,
			        int servicePort
			) {
				this.storage = storage;
				this.serviceHost = serviceHost;
				this.servicePort = servicePort;
			}

			abstract ServiceDiscovery provideServiceDiscovery(String serviceName);

			public EDCAdminService.Builder forServiceType(String serviceName) {
				return new Builder(
						this.storage,
						provideServiceDiscovery(serviceName)
				);
			}
		}

		static class ZkServiceDiscoveryBuilder extends ServiceDiscoveryBuilder {
			private final String zkConnectionString;

			ZkServiceDiscoveryBuilder(StorageAdmin storage,
			                          String serviceHost,
			                          int servicePort,
			                          String zkConnectionString) {
				super(storage, serviceHost, servicePort);
				this.zkConnectionString = zkConnectionString;
			}

			@Override
			ServiceDiscovery provideServiceDiscovery(String serviceType) {
				return new CuratorServiceDiscovery(
						this.zkConnectionString, serviceType, this.serviceHost, this.servicePort);
			}
		}

		static class ConsulServiceDiscoveryBuilder extends ServiceDiscoveryBuilder {
			private final String consulURL;

			ConsulServiceDiscoveryBuilder(StorageAdmin storage,
			                              String serviceHost,
			                              int servicePort,
			                              String consulURL) {
				super(storage, serviceHost, servicePort);
				this.consulURL = consulURL;
			}

			@Override
			ServiceDiscovery provideServiceDiscovery(String serviceName) {
				return new ConsulServiceDiscovery(
						consulURL, serviceName, this.serviceHost, this.servicePort);
			}
		}

	}
}
