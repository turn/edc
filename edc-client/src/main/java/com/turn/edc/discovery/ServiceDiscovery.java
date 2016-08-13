/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import com.turn.edc.router.StoreEventRouter;
import com.turn.edc.selection.CacheInstanceSelector;

import java.util.List;

/**
 * Add class description
 *
 * @author tshiou
 */
public interface ServiceDiscovery {

	void initialize(StoreEventRouter router, CacheInstanceSelector selector);

	List<CacheInstance> getAvailableInstances();
}
