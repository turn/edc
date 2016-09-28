/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add class description
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

	public long attempt(RetryAttempt retryAttempt) throws NumberOfFailedAttemptsException {
		if (retryAttempt.attempt()) {
			if (retryAttempt.toHealthy()) {
				numOfFailedAttempts = 0;
			} else {
				logger.debug("{} failed to make state change to HEALTHY",
						retryAttempt.getClass().getSimpleName());
			}
		} else {
			numOfFailedAttempts++;
			retryAttempt.toProbation();
		}

		if (retryPolicy.shouldContinue(numOfFailedAttempts)) {
			return retryPolicy.nextTryMs(numOfFailedAttempts);
		} else {
			retryAttempt.toEjected();
			throw new NumberOfFailedAttemptsException(retryAttempt.getClass(), numOfFailedAttempts);
		}
	}
}
