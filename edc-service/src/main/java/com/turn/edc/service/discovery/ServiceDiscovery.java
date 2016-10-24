/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.discovery;

/**
 * Service discovery interface
 *
 * Provides all interfaces for edc service registration. One service instance should have one
 * instance of this class.
 *
 * @author tshiou
 */
public interface ServiceDiscovery {

	/**
	 * Register service with cache size
	 *
	 * @param cacheSize max size of cache
	 *
	 * @throws Exception
	 */
	void register(int cacheSize) throws Exception;

	/**
	 * Unregister service
	 */
	void unregister();

	/**
	 * Start service discovery layer
	 */
	void start();

	/**
	 * Close all resources for discovery layer
	 */
	void close();

	/**
	 * Returns service discovery state
	 *
	 * @return true if registered
	 */
	boolean isRegistered();
}
