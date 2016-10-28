/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.impl.JedisStorageConnector;
import com.turn.edc.storage.impl.SpymemcachedStorageConnector;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.inject.Singleton;

/**
 * {@link StorageConnection} factory
 *
 * @author tshiou
 */
@Singleton
public class ConnectionFactory {

	private final StorageType storageType;

	public ConnectionFactory(StorageType storageType) {
		this.storageType = storageType;
	}

	public StorageConnection create(String host, String port, int timeout) throws IOException {
		switch (storageType) {
			case REDIS:
				return new StorageConnection(new JedisStorageConnector(host, port, timeout));
			case MEMCACHED:
				return new StorageConnection(new SpymemcachedStorageConnector(host, port, timeout));
		}

		// Should never reach here
		return NULL_CONNECTION;
	}

	private static final StorageConnection NULL_CONNECTION = new StorageConnection(
			new StorageConnector() {
				@Override
				public void store(String key, byte[] value, int ttl, int timeout) throws IOException {

				}

				@Override
				public byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
					return new byte[0];
				}

				@Override
				public void close() {

				}
			}
	);
}
