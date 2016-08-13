/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.selection;

import com.turn.edc.discovery.CacheInstance;

import java.util.List;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CacheInstanceSelector {

	private volatile WeightedDistributionSelection provider;

	public CacheInstanceSelector() {
	}

	public void updateInstanceProvider(List<CacheInstance> availableInstances) {
		WeightedDistributionSelection newProvider =
				new WeightedDistributionSelection(availableInstances);
		this.provider = newProvider;
	}
}
