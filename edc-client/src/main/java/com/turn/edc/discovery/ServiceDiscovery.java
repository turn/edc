/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import java.io.IOException;
import java.util.List;

/**
 * Add class description
 *
 * @author tshiou
 */
public interface ServiceDiscovery {

	void start() throws IOException;

	void shutdown();

	List<CacheInstance> getAvailableInstances();

	void attachListeners(DiscoveryListener... listeners);
}
