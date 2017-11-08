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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConnectionFactory layer connector interface
 *
 * @author tshiou
 */
public abstract class StorageConnector {
	private static final Logger LOG = LoggerFactory.getLogger(StorageConnector.class);

	public abstract void set(String key, byte[] value, int ttl, int timeout) throws IOException;

	public abstract void set(String key, String subkey, byte[] value, int ttl, int timeout) throws IOException;

	public abstract byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException;

	public abstract byte[] get(String key, String subkey, int timeout) throws KeyNotFoundException, TimeoutException, IOException;
	
	public abstract boolean setTTL(String key, int ttl, int timeout) throws IOException;

	public abstract void close();

	@Subscribe
	public void handleStoreRequest(StoreRequest request) throws IOException {
		LOG.debug("Storing {}", request.toString());
		this.set(request.getKey(), request.getSubkey(), request.getPayload(), request.getTtl(), 10);
	}
}
