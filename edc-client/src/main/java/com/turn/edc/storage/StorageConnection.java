/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.router.StoreRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.EventBus;

/**
 * Object that represents a connection to a storage instance. Contains an eventbus that will handle
 * store/get requests.
 *
 * @author tshiou
 */
public class StorageConnection {

	private final StorageConnector connector;
	private final EventBus storeRequestBus;

	public StorageConnection(StorageConnector connector) {
		this.connector = connector;

		this.storeRequestBus = new EventBus();
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
			throws KeyNotFoundException, TimeoutException, IOException {
		return this.connector.setTTL(key, ttl, timeOut);
	}

	public void close() {
		this.connector.close();
	}
}
