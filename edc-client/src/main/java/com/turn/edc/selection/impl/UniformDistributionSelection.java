package com.turn.edc.selection.impl;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.exception.InvalidParameterException;
import com.turn.edc.selection.SelectionProvider;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

/**
 * Selection provider that has a uniform distribution across the instances
 *
 * @author tshiou
 */
public class UniformDistributionSelection implements SelectionProvider {
	private final List<CacheInstance> instances;
	private final int nInstances;

	public UniformDistributionSelection(List<CacheInstance> serviceInstances) {
		this.instances = serviceInstances;
		this.nInstances = serviceInstances.size();
	}

	@Override
	public Collection<CacheInstance> allInstances() {
		return this.instances;
	}

	@Override
	public Collection<CacheInstance> selectInstances(int n) throws InvalidParameterException {
		if (nInstances < 1) {
			throw new InvalidParameterException("Not enough instances to select from!");
		}

		if (n == 0) {
			return Lists.newArrayList();
		}

		int[] indices = new Random().ints(0, nInstances).distinct().limit(Math.min(n, nInstances)).toArray();

		List<CacheInstance> selected = Lists.newArrayListWithCapacity(indices.length);

		for (Integer i : indices) {
			selected.add(instances.get(i));
		}

		return selected;
	}
}
