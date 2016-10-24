/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link RetryLoop}
 *
 * @author tshiou
 */
@Test
public class RetryLoopTest {

	/**
	 * Tests healthy attempts, as well as a healthy state change
	 *
	 * @throws NumberOfFailedAttemptsException Should not happen
	 */
	@Test
	public void testSuccessfultAttempt() throws NumberOfFailedAttemptsException {

		RetryLoop loop = new RetryLoop(new FixedAttemptRetryPolicy());

		// Healthy attempt
		Assert.assertEquals(loop.attempt(new SuccessfulRetryAttempt()), 0L);

		// State change to healthy
		Assert.assertEquals(loop.attempt(new FailedRetryAttempt()), 10L);
		Assert.assertEquals(loop.attempt(new SuccessfulRetryAttempt()), 0L);

	}

	/**
	 * Tests multiple failed attempts
	 *
	 * @throws NumberOfFailedAttemptsException expected
	 */
	@Test(expectedExceptions = NumberOfFailedAttemptsException.class)
	public void testFailedAttempts() throws NumberOfFailedAttemptsException {

		RetryLoop loop = new RetryLoop(new FixedAttemptRetryPolicy());

		FailedRetryAttempt failAttempt = new FailedRetryAttempt();
		// Fail attempt
		loop.attempt(failAttempt);
		// Check that the stateChangeToProbation is called
		Assert.assertTrue(failAttempt.probation);
		// This attempt should throw NumberOfFailedAttemptsException
		try {
			loop.attempt(failAttempt);
		} finally {
			// Check that the stateChangeToEjected is called
			Assert.assertTrue(failAttempt.ejected);
		}
	}

	/**
	 * Tests an unsuccessful state change to healthy state
	 *
	 * @throws NumberOfFailedAttemptsException expected
	 */
	@Test(expectedExceptions = NumberOfFailedAttemptsException.class)
	public void testFailedHealthyStateChangeAttempt() throws NumberOfFailedAttemptsException {

		RetryLoop loop = new RetryLoop(new FixedAttemptRetryPolicy());

		// Healthy attempt
		Assert.assertEquals(loop.attempt(new SuccessfulRetryAttempt()), 0L);

		// Fail attempt
		Assert.assertEquals(loop.attempt(new FailedRetryAttempt()), 10L);
		// This attempt should throw NumberOfFailedAttemptsException
		loop.attempt(new FailedHealthyStateChangeAttempt());
	}

	/**
	 * Retry attempt that always succeeds
	 */
	private class SuccessfulRetryAttempt extends RetryAttempt {

		// Always succeed
		@Override
		public boolean attempt() {
			return true;
		}

		@Override
		public boolean stateChangeToHealthy() {
			return true;
		}

		@Override
		public void stateChangeToProbation() {
		}

		@Override
		public void stateChangeToEjected() {
		}
	}

	/**
	 * Retry attempt that always fails
	 */
	private class FailedRetryAttempt extends RetryAttempt {

		protected boolean probation = false;
		protected boolean ejected = false;

		// Always fail
		@Override
		public boolean attempt() {
			return false;
		}

		@Override
		public boolean stateChangeToHealthy() {
			return false;
		}

		@Override
		public void stateChangeToProbation() {
			probation = true;
		}

		@Override
		public void stateChangeToEjected() {
			ejected = true;
		}
	}

	/**
	 * Retry attempt that succeeds but fails the state change
	 */
	private class FailedHealthyStateChangeAttempt extends RetryAttempt {

		// Successful attempt
		@Override
		public boolean attempt() {
			return true;
		}

		// But unsuccessful state change
		@Override
		public boolean stateChangeToHealthy() {
			return false;
		}

		@Override
		public void stateChangeToProbation() {
		}

		@Override
		public void stateChangeToEjected() {
		}
	}

	/**
	 * Fixed max attempts retry policy with a simple "next try" policy of multiplying by 10
	 */
	private class FixedAttemptRetryPolicy implements RetryPolicy {

		@Override
		public boolean shouldContinue(int currentNumberOfFailures) {
			return currentNumberOfFailures < 2;
		}

		@Override
		public long nextTryMs(int currentNumberOfFailures) {
			return currentNumberOfFailures * 10L;
		}
	}
}
