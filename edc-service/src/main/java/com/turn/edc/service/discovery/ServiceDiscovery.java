/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.discovery;

/**
 * Add class description
 *
 * @author tshiou
 */
public interface ServiceDiscovery {

	void register(int cacheSize) throws Exception;

	void unregister();

	void start();

	void close();

	boolean isRegistered();
}
