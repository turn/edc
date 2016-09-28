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
public abstract class RetryAttempt {
	private RetryState currentState = RetryState.INITIAL;

	boolean toHealthy() {
		// if not already healthy, set to to healthy status
		if (RetryState.HEALTHY.equals(this.currentState) == false) {
			// change state if the transition is successful
			if (this.stateChangeToHealthy()) {
				this.currentState = RetryState.HEALTHY;
				return true;
			}
			// failed to make state change
			return false;
		}
		// already healthy
		return true;
	}

	void toProbation() {
		if (RetryState.PROBATION.equals(this.currentState) == false) {
			this.stateChangeToProbation();
		}
		this.currentState = RetryState.PROBATION;
	}

	void toEjected() {
		if (RetryState.EJECTED.equals(this.currentState) == false) {
			this.stateChangeToEjected();
		}
		this.currentState = RetryState.EJECTED;
	}

	public abstract boolean attempt();

	public abstract boolean stateChangeToHealthy();

	public abstract boolean stateChangeToProbation();

	public abstract boolean stateChangeToEjected();
}
