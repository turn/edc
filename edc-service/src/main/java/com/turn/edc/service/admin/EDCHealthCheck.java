/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.admin;

import com.turn.edc.service.discovery.ServiceDiscovery;
import com.turn.edc.service.retry.RetryAttempt;
import com.turn.edc.service.retry.RetryLoop;
import com.turn.edc.service.storage.StorageAdmin;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache health check task
 *
 * @see RetryLoop
 *
 * @author tshiou
 */
public class EDCHealthCheck extends RetryAttempt {

	private static final Logger LOG = LoggerFactory.getLogger(EDCHealthCheck.class);

	private final StorageAdmin storageAdmin;
	private final ServiceDiscovery serviceDiscovery;

	/**
	 * @param storageAdmin Storage admin implementation
	 * @param discovery Service discovery implementation
	 */
	public EDCHealthCheck(StorageAdmin storageAdmin, ServiceDiscovery discovery) {
		this.storageAdmin = storageAdmin;
		this.serviceDiscovery = discovery;
	}

	/**
	 * Checks for healthy state via {@link StorageAdmin}
	 *
	 * @return true if the cache is healthy
	 */
	@Override
	public boolean attempt() {
		return this.storageAdmin.isHealthy();
	}

	/**
	 * If the cache is changing state to a healthy state then register it for discovery
	 *
	 * @return true if the state change (i.e. registration) is successful
	 */
	@Override
	public boolean stateChangeToHealthy() {
		if (this.serviceDiscovery.isRegistered() == false) {
			int size = 0;
			try {
				size = this.storageAdmin.getMaxSizeInMb();
				LOG.info("Registering instance with cache size of: " + size + "...");
				this.serviceDiscovery.register(size);
			} catch (Exception e) {
				LOG.error("Failed to register this instance!");
				LOG.error(ExceptionUtils.getStackTrace(e));
				return false;
			}
		}

		return true;
	}

	/**
	 * Probation state means unregistering the service
	 */
	@Override
	public void stateChangeToProbation() {
		this.serviceDiscovery.unregister();
	}

	/**
	 * Ejecting the service from discovery
	 */
	@Override
	public void stateChangeToEjected() {
		this.serviceDiscovery.unregister();
	}
}
