/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

/**
 * Exception when a retry attempt has exceeded the maximum number of attempts
 *
 * @author tshiou
 */
public class NumberOfFailedAttemptsException extends Exception {
	public NumberOfFailedAttemptsException(
			Class<? extends RetryAttempt> retryAttemptClass, int numberOfAttempts ) {
		super(retryAttemptClass.getSimpleName() + " exceeded the maximum number of retries at " + numberOfAttempts);
	}
}
