/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry.impl;

import com.turn.edc.service.retry.RetryPolicy;

/**
 * Add class description
 *
 * @author tshiou
 */
public class ExponentialRetryPolicy implements RetryPolicy {
	private final int initialSleepTimeMs;
	private final int maxRetries;

	public ExponentialRetryPolicy(int initialSleepTimeMs, int maxRetries) {
		this.initialSleepTimeMs = initialSleepTimeMs;
		this.maxRetries = maxRetries;
	}


	@Override
	public boolean shouldContinue(int currentNumberOfAttempts) {
		return currentNumberOfAttempts <= maxRetries;
	}

	@Override
	public long nextTryMs(int currentNumberOfAttempts) {
		if (currentNumberOfAttempts == 0) {
			return initialSleepTimeMs;
		}
		return (long) Math.pow(2, currentNumberOfAttempts - 1) * 1000;
	}
}
