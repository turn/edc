/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.admin;

import com.turn.edc.service.discovery.ServiceDiscovery;
import com.turn.edc.service.retry.RetryAttempt;
import com.turn.edc.service.storage.StorageAdmin;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add class description
 *
 * @author tshiou
 */
public class EDCHealthCheck extends RetryAttempt {

	private static final Logger LOG = LoggerFactory.getLogger(EDCHealthCheck.class);

	private final StorageAdmin storageAdmin;
	private final ServiceDiscovery serviceDiscovery;

	public EDCHealthCheck(StorageAdmin storageAdmin, ServiceDiscovery discovery) {
		this.storageAdmin = storageAdmin;
		this.serviceDiscovery = discovery;
	}

	@Override
	public boolean attempt() {
		return this.storageAdmin.isHealthy();
	}

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

	@Override
	public void stateChangeToProbation() {
		this.serviceDiscovery.unregister();
	}

	@Override
	public void stateChangeToEjected() {
		this.serviceDiscovery.unregister();
	}
}
