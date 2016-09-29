/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry.impl;

import com.turn.edc.service.retry.RetryPolicy;

/**
 * Retry policy that retries a maximum number of ties, with exponentially increasing
 * delay between retries.
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
	public boolean shouldContinue(int currentNumberOfFailures) {
		return currentNumberOfFailures <= maxRetries;
	}

	@Override
	public long nextTryMs(int currentNumberOfFailures) {
		if (currentNumberOfFailures == 0) {
			return initialSleepTimeMs;
		}
		return (long) Math.pow(2, currentNumberOfFailures - 1) * 1000;
	}
}
