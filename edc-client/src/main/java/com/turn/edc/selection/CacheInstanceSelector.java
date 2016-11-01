/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.selection;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.exception.InvalidParameterException;
import com.turn.edc.selection.impl.UniformDistributionSelection;
import com.turn.edc.selection.impl.WeightedDistributionSelection;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache instance selector that uses a {@link SelectionProvider}
 *
 * @author tshiou
 */
public class CacheInstanceSelector extends DiscoveryListener {
	private static final Logger logger = LoggerFactory.getLogger(CacheInstanceSelector.class);

	// Initializes with an empty list
	private volatile SelectionProvider provider;

	public CacheInstanceSelector() {
		provider = new UniformDistributionSelection(Lists.newArrayList());
	}

	public Collection<CacheInstance> allInstances() {
		return provider.allInstances();
	}

	public Collection<CacheInstance> select(int n) throws InvalidParameterException {
		return provider.selectInstances(n);
	}

	@Override
	public void update(List<CacheInstance> availableInstances) {
		logger.debug("Updating selection layer with {} instances", availableInstances.size());
		SelectionProvider newProvider = new UniformDistributionSelection(availableInstances);
		this.provider = newProvider;
	}
}
