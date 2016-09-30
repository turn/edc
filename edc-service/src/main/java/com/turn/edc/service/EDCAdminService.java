/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service;

import com.turn.edc.service.admin.EDCHealthCheck;
import com.turn.edc.service.retry.NumberOfFailedAttemptsException;
import com.turn.edc.service.retry.RetryLoop;
import com.turn.edc.service.retry.RetryPolicy;
import com.turn.edc.service.retry.impl.ExponentialRetryPolicy;
import com.turn.edc.service.discovery.ServiceDiscovery;
import com.turn.edc.service.discovery.impl.ConsulServiceDiscovery;
import com.turn.edc.service.discovery.impl.CuratorServiceDiscovery;
import com.turn.edc.service.storage.StorageAdmin;
import com.turn.edc.service.storage.impl.JedisStorageAdmin;
import com.turn.edc.service.storage.impl.SpymemcachedStorageAdmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EDC cache instance admin service
 *
 * This is the main entrypoint for the EDC service. It is responsible for:
 *
 * - Checking cache health
 * - Registering the instance for service discovery
 *
 * To create an instance of an EDC admin service use the builder, then call start().
 *
 * For example:
 *
 * <pre>
 * {@code
 *		EDCAdminService service =
 *				EDCAdminService.builder()
 *						.withRedisStorage("localhost", 6379)
 *						.withZkServiceDiscovery("localhost:2181")
 *						.forServiceType("my-edc-service")
 *						.build();
 *		service.start();
 * }
 * </pre>

 *
 * @author tshiou
 */
public class EDCAdminService {

	private static final Logger logger = LoggerFactory.getLogger(EDCAdminService.class);

	private final AdminThread adminThread;
	private final ServiceDiscovery serviceDiscovery;

	private EDCAdminService(StorageAdmin admin, ServiceDiscovery discovery, RetryPolicy retryPolicy) {
		this.adminThread = new AdminThread(retryPolicy, admin, discovery);
		this.serviceDiscovery = discovery;
	}

	/**
	 * Non-blocking call to start EDC admin thread.
	 */
	public void start() {
		logger.info("Starting thread...");
		this.serviceDiscovery.start();
		Thread t = new Thread(this.adminThread);
		t.start();
	}

	/**
	 * Shuts down the EDC admin service.
	 */
	public void stop() {
		this.adminThread.notify();
		this.adminThread.shutdown();
	}

	private static class AdminThread implements Runnable {

		private boolean shutdown = false;
		private final RetryLoop retryLoop;
		private final EDCHealthCheck healthCheck;

		AdminThread(RetryPolicy retryPolicy, StorageAdmin storageAdmin, ServiceDiscovery serviceDiscovery) {
			this.retryLoop = new RetryLoop(retryPolicy);
			this.healthCheck = new EDCHealthCheck(storageAdmin, serviceDiscovery);
		}

		@Override
		public void run() {

			while (!shutdown) {
				try {
					Thread.sleep(this.retryLoop.attempt(this.healthCheck));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (NumberOfFailedAttemptsException e) {
					logger.error("Storage is dead and ejected from cluster. Shutting down admin now.");
					break;
				}
			}
			// Eject from discovery
			logger.info("Ejecting instance from discovery");
			this.healthCheck.stateChangeToEjected();
		}

		public void shutdown() {
			this.shutdown = true;
		}
	}

	public static Builder.EDCStorageBuilder builder() {
		return new Builder.EDCStorageBuilder();
	}

	/***************************************** Builder *******************************************/

	public static class Builder {

		private final StorageAdmin admin;
		private final ServiceDiscovery discovery;
		private RetryPolicy retryPolicy = new ExponentialRetryPolicy(2000, 8);

		Builder(StorageAdmin admin, ServiceDiscovery discovery) {
			this.admin = admin;
			this.discovery = discovery;
		}

		public Builder withRetryPolicy(RetryPolicy retryPolicy) {
			this.retryPolicy = retryPolicy;
			return this;
		}

		public EDCAdminService build() {
			return new EDCAdminService(this.admin, this.discovery, this.retryPolicy);
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
