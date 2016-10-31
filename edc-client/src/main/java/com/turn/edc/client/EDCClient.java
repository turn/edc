/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.client;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.ServiceDiscovery;
import com.turn.edc.discovery.impl.ConsulServiceDiscovery;
import com.turn.edc.discovery.impl.CuratorServiceDiscovery;
import com.turn.edc.exception.InvalidParameterException;
import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.router.RequestRouter;
import com.turn.edc.router.StoreRequest;
import com.turn.edc.selection.CacheInstanceSelector;
import com.turn.edc.storage.ConnectionFactory;
import com.turn.edc.storage.StorageType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * EDC client main class
 *
 * Create an instance of the EDC client by using the builder. During the build phase, the following
 * is required to be configured:
 * 1. storage layer
 * 2. service discovery layer
 * 3. service name
 *
 * For example:
 *
 * 	EDCClient client = EDCClient.builder()
 *      .withRedisStorage()
 *      .withConsulServiceDiscovery("localhost")
 *      .withServiceName("redis")
 *      .build();
 *
 * After building the client, initialize it by calling start(). This sets up the service discovery
 * client and initializes the routing layer
 *
 * @author tshiou
 */
public class EDCClient {

	@Inject
	private RequestRouter router;

	@Inject
	private CacheInstanceSelector selector;

	@Inject
	private ServiceDiscovery discovery;

	private EDCClient(EDCClientModule module) {
		Guice.createInjector(module).injectMembers(this);

		this.discovery.attachListeners(this.router, this.selector);
	}

	public void start() throws Exception {
		this.discovery.start();
	}

	public void close() {
		this.discovery.shutdown();
		this.router.close();
	}

	public byte[] get(HostAndPort hostAndPort, String key)
			throws IOException, TimeoutException, KeyNotFoundException, InvalidParameterException {
		if (hostAndPort.getHostText() == null || hostAndPort.getHostText().isEmpty()) {
			throw new InvalidParameterException("hostAndPort", hostAndPort.toString(), "Host cannot be empty");
		}

		if (hostAndPort.getPort() == -1) {
			throw new InvalidParameterException("hostAndPort", hostAndPort.toString(), "Port cannot be empty");
		}

		return router.get(new CacheInstance(hostAndPort, -1), key);
	}

	public Collection<String> put(int replication, String key, byte[] value, int ttl) throws InvalidParameterException {
		if (replication < 1) {
			throw new InvalidParameterException("replication", Integer.toString(replication),
					"Value should be greater than 0");
		}

		List<String> ret = Lists.newArrayListWithCapacity(replication);
		Collection<CacheInstance> selectedDestinations = selector.select(replication);
		for (CacheInstance selectedDestination : selectedDestinations) {
			router.store(selectedDestination, new StoreRequest(key, value, ttl));
			ret.add(selectedDestination.getHostAndPort().toString());
		}

		return ret;
	}

	/**
	 * Provides a builder for EDCClient.
	 *
	 * Builds in the following order: storage layer, service discovery layer, service-name
	 */
	public static Builder.EDCStorageBuilder builder() {
		return new Builder.EDCStorageBuilder();
	}

	/***************************************** Builder *******************************************/

	/**
	 * EDC Client builder
	 */
	public static class Builder {

		private final ConnectionFactory connectorFactory;
		private final ServiceDiscovery serviceDiscovery;

		Builder(
				ConnectionFactory connectorFactory,
		        ServiceDiscovery serviceDiscovery
		) {
			this.connectorFactory = connectorFactory;
			this.serviceDiscovery = serviceDiscovery;
		}

		public EDCClient build() {
			return new EDCClient(
					new EDCClientModule(this.connectorFactory, this.serviceDiscovery)
			);
		}

		/**
		 * Builder for storage layer
		 *
		 * Supports Redis or Memecached
		 *
		 * @author tshiou
		 */
		public static class EDCStorageBuilder {

			EDCStorageBuilder() {}

			public EDCServiceDiscoveryBuilder usingRedisStorage() {
				return new EDCServiceDiscoveryBuilder(
						new ConnectionFactory(StorageType.REDIS)	);
			}

			public EDCServiceDiscoveryBuilder usingMemcachedStorage() {
				return new EDCServiceDiscoveryBuilder(
						new ConnectionFactory(StorageType.MEMCACHED));
			}
		}

		/**
		 * Builder for service discovery layer.
		 *
		 * Support zookeeper or consul
		 *
		 * @author tshiou
		 */
		public static class EDCServiceDiscoveryBuilder {
			private final ConnectionFactory connectorFactory;

			EDCServiceDiscoveryBuilder(ConnectionFactory connectorFactory) {
				this.connectorFactory = connectorFactory;
			}

			public ZkServiceDiscoveryBuilder usingZkServiceDiscovery(String zkConnectionString) {
				return new ZkServiceDiscoveryBuilder(this.connectorFactory, zkConnectionString);
			}

			public ConsulServiceDiscoveryBuilder usingConsulServiceDiscovery(String consulURL) {
				return new ConsulServiceDiscoveryBuilder(this.connectorFactory, consulURL);
			}

			public ConsulServiceDiscoveryBuilder usingConsulServiceDiscovery() {
				return usingConsulServiceDiscovery("localhost");
			}
		}

		/**
		 * Zookeeper-specific builder for service discovery
		 *
		 * @author tshiou
		 */
		public static class ZkServiceDiscoveryBuilder {
			private final ConnectionFactory connectorFactory;
			private final String zkConnectionString;

			ZkServiceDiscoveryBuilder(
					ConnectionFactory connectorFactory,
					String zkConnectionString
			) {
				this.connectorFactory = connectorFactory;
				this.zkConnectionString = zkConnectionString;
			}

			public EDCClient.Builder withServiceName(String serviceName) {
				return new EDCClient.Builder(
						this.connectorFactory,
						new CuratorServiceDiscovery(this.zkConnectionString, serviceName)
				);
			}
		}

		/**
		 * Consul-specific builder for service discovery
		 *
		 * @author tshiou
		 */
		public static class ConsulServiceDiscoveryBuilder {
			private final ConnectionFactory connectorFactory;
			private final String consulURL;
			private int consulPort = 8500;

			ConsulServiceDiscoveryBuilder(
					ConnectionFactory connectorFactory,
					String consulURL
			) {
				this.connectorFactory = connectorFactory;
				this.consulURL = consulURL;
			}

			public ConsulServiceDiscoveryBuilder withConsulClientPort(int port) {
				this.consulPort = port;
				return this;
			}

			public EDCClient.Builder forServiceName(String serviceName) {
				return new EDCClient.Builder(
						this.connectorFactory,
						new ConsulServiceDiscovery(this.consulURL, this.consulPort, serviceName)
				);
			}
		}
	}
}
