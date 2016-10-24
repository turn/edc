/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.retry.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test for {@link ExponentialRetryPolicy}
 *
 * @author tshiou
 */
@Test
public class ExponentialRetryPolicyTest {

	@Test
	public void testShouldContinue() {
		int maxRetries = 3;
		ExponentialRetryPolicy retryPolicy = new ExponentialRetryPolicy(0, maxRetries);

		Assert.assertTrue(retryPolicy.shouldContinue(0));
		Assert.assertTrue(retryPolicy.shouldContinue(1));
		Assert.assertTrue(retryPolicy.shouldContinue(3));
		Assert.assertFalse(retryPolicy.shouldContinue(4));
	}

	@Test
	public void testNextTryMs() {
		int initialSleep = 10;

		ExponentialRetryPolicy retryPolicy = new ExponentialRetryPolicy(initialSleep, 5);

		Assert.assertEquals(retryPolicy.nextTryMs(0), initialSleep);
		Assert.assertEquals(retryPolicy.nextTryMs(1), 1000);
		Assert.assertEquals(retryPolicy.nextTryMs(3), 4000);

	}
}
