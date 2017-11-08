/*
 * Copyright (C) 2016-2017 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.router.StoreRequest;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Object that represents a connection to a storage instance. Contains an eventbus that will handle
 * store/get requests.
 *
 * @author tshiou
 */
public class StorageConnection {

	private final StorageConnector connector;
	private final EventBus storeRequestBus;

	public StorageConnection(StorageConnector connector, SubscriberExceptionHandler subscriberExceptionHandler,
			boolean async, int asyncQueueCapacity) {
		this.connector = connector;

		this.storeRequestBus = async ? new AsyncEventBus(
				new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>(
								asyncQueueCapacity > 0 ? asyncQueueCapacity : Integer.MAX_VALUE)),
				subscriberExceptionHandler) : new EventBus(subscriberExceptionHandler);
		this.storeRequestBus.register(connector);
	}

	public byte[] get(String key, String subkey, int timeout) throws IOException, TimeoutException, KeyNotFoundException {
		return subkey == null || subkey.isEmpty()
				? this.connector.get(key, timeout)
				: this.connector.get(key, subkey, timeout);
	}

	public void post(StoreRequest request) {
		this.storeRequestBus.post(request);
	}
	
	public boolean setTTL(String key, int ttl, int timeOut)
			throws TimeoutException, IOException {
		return this.connector.setTTL(key, ttl, timeOut);
	}

	public void close() {
		this.connector.close();
	}
}
