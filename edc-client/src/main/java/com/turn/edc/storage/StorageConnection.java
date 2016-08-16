/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.router.StoreRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.EventBus;

/**
 * Add class description
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

	public byte[] get(String key, int timeout) throws IOException, TimeoutException, KeyNotFoundException {
		return this.connector.get(key, timeout);
	}

	public void post(StoreRequest request) {
		this.storeRequestBus.post(request);
	}

	public void close() {
		this.connector.close();
	}
}
