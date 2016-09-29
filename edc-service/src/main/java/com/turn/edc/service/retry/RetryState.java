/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

/**
 * Represents a retry state. State machine ordering:
 *
 * INITIAL -> HEALTHY -> PROBATION -> EJECTED
 *                ^---------/
 *
 * The probation state simply represents that the task has failed at least once, but has not
 * exceeded the maximum limit of retries
 *
 * A task in probation may move back to healthy if it succeeds AND makes a successful state
 * change to the healthy state.
 *
 * The ejected state represents a task that has exceeded the maximum limit of retries.
 * This state is terminal.
 *
 * @author tshiou
 */
public enum RetryState {
	INITIAL,
	HEALTHY,
	PROBATION,
	EJECTED
}
