/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import java.util.List;

/**
 * Add class description
 *
 * @author tshiou
 */
public abstract class DiscoveryListener {
	public void update(List<CacheInstance> availableInstances) {
		// no-op
	}
}
