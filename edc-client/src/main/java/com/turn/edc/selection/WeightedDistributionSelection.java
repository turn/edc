/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.selection;

import com.turn.edc.discovery.CacheInstance;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Add class description
 *
 * @author tshiou
 */
public class WeightedDistributionSelection {

	// Collection of query instances
	private List<CacheInstance> instances = Lists.newArrayList();

	// Probability of alias in each column
	private double[] probability;

	// Alias index of each column
	private int[] alias;

	public WeightedDistributionSelection(List<CacheInstance> serviceInstances) {

		this.instances = serviceInstances;

		// Get total heap size
		int total = 0;
		for (CacheInstance instance : serviceInstances) {
			total += instance.getCacheSize();
		}

		// Get normalized average
		final double average = 1.0 / this.instances.size();

		// Probabilities
		double[] probabilities = new double[this.instances.size()];

		// Work lists, contains index of probabilities table
		LinkedList<Integer> small = Lists.newLinkedList();
		LinkedList<Integer> large = Lists.newLinkedList();

		// Populate the tables with the input probabilities.
		for (int i = 0; i < this.instances.size(); ++i) {
			probabilities[i] = (double) this.instances.get(i).getCacheSize() / total;
			// If the probability is below the average probability, then we add
			// it to the small list; otherwise we add it to the large list.
			if (probabilities[i] >= average) {
				large.addLast(i);
			} else {
				small.addLast(i);
			}
		}

		// Allocate probability and alias tables
		probability = new double[this.instances.size()];
		alias = new int[this.instances.size()];

		 /* As a note: in the mathematical specification of the algorithm, we
         * will always exhaust the small list before the big list.  However,
         * due to floating point inaccuracies, this is not necessarily true.
         * Consequently, this inner loop (which tries to pair small and large
         * elements) will have to check that both lists aren't empty.
         */
		while (!small.isEmpty() && !large.isEmpty()) {

			// Get the index of the small and the large probabilities.
			int less = small.removeLast();
			int more = large.removeLast();

			// Scale up probabilities so that 1/n is weight 1.0
			probability[less] = probabilities[less] * probabilities.length;
			alias[less] = more;

			// Decrease the probability of the larger one by the appropriate amount.
			probabilities[more] = (probabilities[more] + probabilities[less]) - average;

			// If the new probability is less than average, then add it to the small list
			if (probabilities[more] >= average) {
				large.addLast(more);
			} else {
				small.addLast(more);
			}
		}

		 /* At this point, everything is in one list, which means that the
	     * remaining probabilities should all be 1/n.  Based on this, set them
	     * appropriately.  Due to numerical issues, we can't be sure which
	     * stack will hold the entries, so we empty both.
	     */
		while (!small.isEmpty())
			probability[small.removeLast()] = 1.0;
		while (!large.isEmpty())
			probability[large.removeLast()] = 1.0;
	}

	private int selectIndex() {
		if (this.instances == null || this.probability == null || this.alias == null) {
			return -1;
		}

		// Generate a uniform distribution over columns to inspect
		int column = ThreadLocalRandom.current().nextInt(this.instances.size());

		// Generate biased coin toss to determine whether to select alias
		boolean selectAlias = ThreadLocalRandom.current().nextDouble() > probability[column];

		return selectAlias ? alias[column] : column;
	}

	public Collection<CacheInstance> selectInstances(int n) {

		if (n <= this.instances.size()) {
			return this.instances;
		}

		Set<CacheInstance> selections = Sets.newHashSetWithExpectedSize(n);

		for (int i = 0 ; i < n ; i++) {
			CacheInstance selected = this.instances.get(selectIndex());
			while (!selections.contains(selected)) {
				selected = this.instances.get(selectIndex());
			}
			selections.add(selected);
		}

		return selections;
	}
}
