/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.client;

import com.turn.edc.discovery.ServiceDiscovery;
import com.turn.edc.discovery.impl.ConsulServiceDiscovery;
import com.turn.edc.discovery.impl.CuratorServiceDiscovery;
import com.turn.edc.router.StoreEventRouter;
import com.turn.edc.selection.CacheInstanceSelector;

import com.google.common.net.HostAndPort;

/**
 * Add class description
 *
 * @author tshiou
 */
public class EDCClient {

	private StoreEventRouter router;

	private CacheInstanceSelector selector;

	private ServiceDiscovery discovery;

	private EDCClient() {
		this.router = new StoreEventRouter();
		this.selector = new CacheInstanceSelector();


	}

	public String get() {
		return "";
	}

	public HostAndPort put() {
		return null;
	}

	public static Builder.EDCStorageBuilder builder() {
		return new Builder.EDCStorageBuilder();
	}

	public static class Builder {

		private final 	StorageType storageType;
		private final ServiceDiscovery serviceDiscovery;
		private final String serviceName;

		Builder(
				StorageType storageType,
		        ServiceDiscovery serviceDiscovery,
		        String serviceName
		) {
			this.storageType = storageType;
			this.serviceDiscovery = serviceDiscovery;
			this.serviceName = serviceName;
		}

		public EDCClient build() {
			return new EDCClient();
		}

		public enum StorageType {
			REDIS,
			MEMCACHED
		}

		/**
		 * Add class description
		 *
		 * @author tshiou
		 */
		public static class EDCStorageBuilder {


			public EDCServiceDiscoveryBuilder withRedisStorage() {
				return new EDCServiceDiscoveryBuilder(StorageType.REDIS);
			}

			public EDCServiceDiscoveryBuilder withMemcachedStorage() {
				return new EDCServiceDiscoveryBuilder(StorageType.MEMCACHED);
			}
		}

		/**
		 * Add class description
		 *
		 * @author tshiou
		 */
		public static class EDCServiceDiscoveryBuilder {
			private final StorageType storageType;

			EDCServiceDiscoveryBuilder(StorageType storageType) {
				this.storageType = storageType;
			}

			public ZkServiceDiscoveryBuilder withZkServiceDiscovery(String zkConnectionString) {
				return new ZkServiceDiscoveryBuilder(this.storageType, zkConnectionString);
			}

			public ConsulServiceDiscoveryBuilder withConsulServiceDiscovery(String consulURL) {
				return new ConsulServiceDiscoveryBuilder(this.storageType, consulURL);
			}
		}

		/**
		 * Add class description
		 *
		 * @author tshiou
		 */
		public static class ZkServiceDiscoveryBuilder {
			private final StorageType storageType;
			private final String zkConnectionString;

			ZkServiceDiscoveryBuilder(
					StorageType storageType,
					String zkConnectionString
			) {
				this.storageType = storageType;
				this.zkConnectionString = zkConnectionString;
			}

			public EDCClient.Builder withServiceName(String serviceName) {
				return new EDCClient.Builder(
						this.storageType,
						new CuratorServiceDiscovery(this.zkConnectionString),
						serviceName
				);
			}
		}

		/**
		 * Add class description
		 *
		 * @author tshiou
		 */
		public static class ConsulServiceDiscoveryBuilder {
			private final StorageType storageType;
			private final String consulURL;

			ConsulServiceDiscoveryBuilder(
					StorageType storageType,
					String consulURL
			) {
				this.storageType = storageType;
				this.consulURL = consulURL;
			}

			public EDCClient.Builder forService(String serviceName) {
				return new EDCClient.Builder(
						this.storageType,
						new ConsulServiceDiscovery(this.consulURL),
						serviceName
				);
			}
		}
	}
}
