/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

/**
 * Add class description
 *
 * @author tshiou
 */
public interface RetryPolicy {
	boolean shouldContinue(int currentNumberOfAttempts);

	long nextTryMs(int currentNumberOfAttempts);
}
