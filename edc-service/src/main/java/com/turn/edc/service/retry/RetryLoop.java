/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retry loop attempt
 *
 * @author tshiou
 */
public class RetryLoop {

	private static final Logger logger = LoggerFactory.getLogger(RetryLoop.class);

	private final RetryPolicy retryPolicy;
	private int numOfFailedAttempts = 0;

	public RetryLoop(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	/**
	 * Attempts the provided task. Returns the time to wait until executing the next attempt (in ms)
	 *
	 * @param retryAttempt Task to attempt
	 *
	 * @return Time in ms until the next retry
	 *
	 * @throws NumberOfFailedAttemptsException If RetryPolicy.shouldContinue returns false
	 */
	public long attempt(RetryAttempt retryAttempt) throws NumberOfFailedAttemptsException {
		// Attempt the task
		if (retryAttempt.attempt()) {
			// If healthy attempt, make the state change to healthy
			if (retryAttempt.toHealthy()) {
				numOfFailedAttempts = 0;
			} else {
				// If state change failed
				numOfFailedAttempts++;
				logger.debug("{} failed to make state change to HEALTHY",
						retryAttempt.getClass().getSimpleName());
			}
		} else {
			// Increment failed attempt counter and put the task on "probation"
			numOfFailedAttempts++;
			retryAttempt.toProbation();
		}

		// Check if we should continue attempting the task
		if (retryPolicy.shouldContinue(numOfFailedAttempts)) {
			// If we should continue, then return the delay until the next attempt
			return retryPolicy.nextTryMs(numOfFailedAttempts);
		} else {
			// If we're told not to continue anymore then eject the task and throw exception
			retryAttempt.toEjected();
			throw new NumberOfFailedAttemptsException(retryAttempt.getClass(), numOfFailedAttempts);
		}
	}
}
