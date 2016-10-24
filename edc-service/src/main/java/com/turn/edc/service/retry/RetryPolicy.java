/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

/**
 * Represents a retry policy.
 *
 * @author tshiou
 */
public interface RetryPolicy {

	/**
	 * Returns true if we should continue to retry the task
	 *
	 * @param currentNumberOfFailures The current number of failed attempts
	 *
	 * @return true if we should try again, false if we should give up
	 */
	boolean shouldContinue(int currentNumberOfFailures);

	/**
	 * Returns the delay in ms until the next retry
	 *
	 * @param currentNumberOfFailures The current number of failed attempts
	 *
	 * @return Number of ms until the next retry attempt
	 */
	long nextTryMs(int currentNumberOfFailures);
}
