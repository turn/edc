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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(EDCClient.class);

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

	/**
	 * Starts the EDC client
	 *
	 * @throws Exception Initialization failed
	 */
	public void start() throws Exception {
		logger.info("Starting EDC client...");
		this.discovery.start();
	}

	/**
	 * Shuts down the EDC client
	 */
	public void close() {
		logger.info("Shutting down EDC client...");
		this.discovery.shutdown();
		this.router.close();
	}


	/**
	 * Retrieves the value at key in the provided destination cache
	 *
	 * @param hostAndPort Destination host and port
	 * @param key Top-level key
	 *
	 * @return Value in bytes
	 * @throws IOException Connection error with destination cache
	 * @throws TimeoutException Retrieval timeout
	 * @throws KeyNotFoundException Key was not found at the provided destination
	 * @throws InvalidParameterException If an invalid destination is provided
	 */
	public byte[] get(HostAndPort hostAndPort, String key)
			throws IOException, TimeoutException, KeyNotFoundException, InvalidParameterException {
		return get(hostAndPort, key, "");
	}

	/**
	 * Retrieves the value at key:subkey in the provided destination cache
	 *
	 * @param hostAndPort Destination host and port
	 * @param key Top-level key
	 * @param subkey Subkey, can be empty or null
	 *
	 * @return Value in bytes
	 * @throws IOException Connection error with destination cache
	 * @throws TimeoutException Retrieval timeout
	 * @throws KeyNotFoundException Key (or subkey) was not found at the provided destination
	 * @throws InvalidParameterException If an invalid destination is provided
	 */
	public byte[] get(HostAndPort hostAndPort, String key, String subkey)
			throws IOException, TimeoutException, KeyNotFoundException, InvalidParameterException {
		checkHostAndPort(hostAndPort);

		return router.get(new CacheInstance(hostAndPort, -1), key, subkey);
	}

	/**
	 * Set the value at key with a given replication
	 *
	 * @param replication Desired number of cache instances to store the key
	 * @param key Key
	 * @param value Value
	 * @param ttl TTL (in seconds) for key
	 *
	 * @return Collection of strings representing the selected destinations where the key:value
	 * was stored
	 * @throws InvalidParameterException If replication is less than 1 or no cache instances were found
	 */
	public Collection<String> set(int replication, String key, byte[] value, int ttl)
			throws InvalidParameterException {
		return set(replication, key, "", value, ttl);
	}

	/**
	 * Set the value at key with a given replication
	 *
	 * @param replication Desired number of cache instances to store the key
	 * @param key Top-level key
	 * @param subkey Subkey
	 * @param value Value
	 * @param ttl TTL (in seconds) for key
	 *
	 * @return Collection of strings representing the selected destinations where the key:value
	 * was stored
	 * @throws InvalidParameterException If replication is less than 1 or no cache instances were found
	 */
	public Collection<String> set(int replication, String key, String subkey, byte[] value, int ttl)
			throws InvalidParameterException {
		if (replication < 1) {
			throw new InvalidParameterException("replication", Integer.toString(replication),
					"Value should be greater than 0");
		}

		List<String> ret = Lists.newArrayListWithCapacity(replication);
		Collection<CacheInstance> selectedDestinations;
		try {
			selectedDestinations = selector.select(replication);
		} catch (InvalidParameterException ipe) {
			logger.debug(ExceptionUtils.getMessage(ipe));
			return ret;
		}
		for (CacheInstance selectedDestination : selectedDestinations) {
			router.store(selectedDestination, new StoreRequest(key, subkey, value, ttl));
			ret.add(selectedDestination.getHostAndPort().toString());
		}

		return ret;
	}

	/**
	 * Set the value at key in the provided destination
	 *
	 * @param destination Destination host and port
	 * @param key Top-level key
	 * @param subkey Subkey, can be empty or null
	 * @param value Value to store
	 * @param ttl TTL (in seconds) for the top-level key
	 *
	 * @throws InvalidParameterException If an invalid destination is provided
	 */
	public void set(HostAndPort destination, String key, String subkey, byte[] value, int ttl)
			throws InvalidParameterException {
		checkHostAndPort(destination);

		router.store(new CacheInstance(destination), new StoreRequest(key, subkey, value, ttl));
	}

	/**
	 * Checks if the hostAndPort is valid (host is not empty, and port is > 0)
	 * @throws InvalidParameterException
	 */
	private void checkHostAndPort(HostAndPort hostAndPort) throws InvalidParameterException {
		if (hostAndPort.getHostText() == null || hostAndPort.getHostText().isEmpty()) {
			throw new InvalidParameterException("hostAndPort", hostAndPort.toString(), "Host cannot be empty");
		}

		if (hostAndPort.getPort() < 0) {
			throw new InvalidParameterException("hostAndPort", hostAndPort.toString(), "Invalid port");
		}
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

			public ConsulServiceDiscoveryBuilder usingConsulServiceDiscovery() {
				return new ConsulServiceDiscoveryBuilder(this.connectorFactory);
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
			private String consulURL = "localhost";
			private int consulPort = 8500;

			ConsulServiceDiscoveryBuilder(
					ConnectionFactory connectorFactory
			) {
				this.connectorFactory = connectorFactory;
			}

			public ConsulServiceDiscoveryBuilder withConsulClientURL(String consulURL) {
				this.consulURL = consulURL;
				return this;
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
