/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.router.StoreRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.Subscribe;

/**
 * ConnectionFactory layer connector interface
 *
 * @author tshiou
 */
public abstract class StorageConnector {

	public abstract void store(String key, byte[] value, int ttl, int timeout) throws IOException;

	public abstract byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException;

	public abstract void close();

	@Subscribe
	public void handleStoreRequest(StoreRequest request) {
		try {
			this.store(request.getKey(), request.getPayload(), request.getTtl(), 10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
