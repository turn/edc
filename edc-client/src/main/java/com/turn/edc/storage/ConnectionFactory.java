/*
 * Copyright (C) 2016-2017 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.impl.JedisStorageConnector;
import com.turn.edc.storage.impl.SpymemcachedStorageConnector;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.Singleton;

/**
 * {@link StorageConnection} factory
 *
 * @author tshiou
 */
@Singleton
public class ConnectionFactory {

	private final StorageType storageType;
	private final SubscriberExceptionHandler subscriberExceptionHandler;
	private final boolean async;
	private final int asyncQueueSize;

	/**
	  * A factory that would create {@link StorageConnection} that handles requests synchronously
	  * 
	  * @param storageType
	  * @param subscriberExceptionHandler
	 */
	public ConnectionFactory(StorageType storageType, SubscriberExceptionHandler subscriberExceptionHandler) {
		this.storageType = storageType;
		this.subscriberExceptionHandler = subscriberExceptionHandler;
		this.async = false;
		this.asyncQueueSize = 0;
	}

	/**
	  * A factory that would create {@link StorageConnection} that handles requests asynchronously
	  * 
	  * @param storageType
	  * @param subscriberExceptionHandler
	  * @param asyncEventQueueSize
	 */
	public ConnectionFactory(StorageType storageType, SubscriberExceptionHandler subscriberExceptionHandler,
			int asyncEventQueueSize) {
		this.storageType = storageType;
		this.subscriberExceptionHandler = subscriberExceptionHandler;
		this.async = true;
		this.asyncQueueSize = asyncEventQueueSize;
	}

	public StorageConnection create(String host, int port, int timeout) throws IOException {
		StorageConnector connector;
		switch (storageType) {
			case REDIS:
				connector = new JedisStorageConnector(host, port, timeout);
				break;
			case MEMCACHED:
				connector = new SpymemcachedStorageConnector(host, port, timeout);
				break;
			default:
				return NULL_CONNECTION;			
		}

		return new StorageConnection(connector, subscriberExceptionHandler, async, asyncQueueSize);
	}

	private static final StorageConnection NULL_CONNECTION = new StorageConnection(
			new StorageConnector() {

				@Override
				public void initialize() throws IOException {

				}

				@Override
				public boolean isInitialized() {
					return true;
				}

				@Override
				public void set(String key, byte[] value, int ttl, int timeout) throws IOException {

				}

				@Override
				public void set(String key, String subkey, byte[] value, int ttl, int timeout) throws IOException {

				}

				@Override
				public byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
					return new byte[0];
				}

				@Override
				public byte[] get(String key, String subkey, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
					return new byte[0];
				}
				
				@Override
				public boolean setTTL(String key, int ttl, int timeout) throws IOException {
					return false;
				}

				@Override
				public void close() {

				}
			}, new SubscriberExceptionHandler() {

				@Override
				public void handleException(Throwable exception, SubscriberExceptionContext context) {
					// no-op
				}
			}, false, 0);
}
