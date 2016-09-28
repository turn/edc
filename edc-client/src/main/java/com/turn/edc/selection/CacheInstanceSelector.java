/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.selection;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.exception.InvalidParameterException;

import java.util.Collection;
import java.util.List;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CacheInstanceSelector extends DiscoveryListener {

	private volatile WeightedDistributionSelection provider;

	public CacheInstanceSelector() {
	}

	public Collection<CacheInstance> select(int n) throws InvalidParameterException {
		return provider.selectInstances(n);
	}

	@Override
	public void update(List<CacheInstance> availableInstances) {
		WeightedDistributionSelection newProvider =
				new WeightedDistributionSelection(availableInstances);
		this.provider = newProvider;
	}
}
