/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

/**
 * A task that should be attempted
 *
 * @author tshiou
 */
public abstract class RetryAttempt {
	// Initial state. This task should never reach this state, only start here
	private RetryState currentState = RetryState.INITIAL;

	/**
	 * Making the state change to healthy
	 *
	 * Will call the abstract method stateChangeToHealthy
	 *
	 * @return true if the state change is successful
	 */
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

	/**
	 * Makes the state change to probation
	 */
	void toProbation() {
		if (RetryState.PROBATION.equals(this.currentState) == false) {
			this.stateChangeToProbation();
		}
		this.currentState = RetryState.PROBATION;
	}

	/**
	 * Makes the state change to ejected
	 */
	void toEjected() {
		if (RetryState.EJECTED.equals(this.currentState) == false) {
			this.stateChangeToEjected();
		}
		this.currentState = RetryState.EJECTED;
	}

	public abstract boolean attempt();

	public abstract boolean stateChangeToHealthy();

	public abstract void stateChangeToProbation();

	public abstract void stateChangeToEjected();
}
