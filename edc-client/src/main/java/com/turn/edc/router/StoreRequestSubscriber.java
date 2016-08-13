/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

import com.turn.edc.storage.StorageConnector;
import com.turn.edc.storage.impl.JedisStorageConnector;

import java.io.IOException;

import com.google.common.eventbus.Subscribe;

/**
 * Subscriber to the store request event bus
 *
 * @see com.google.common.eventbus.EventBus
 *
 * @author tshiou
 */
public class StoreRequestSubscriber {

	private final StorageConnector connector;

	public StoreRequestSubscriber(String host, String port, int timeout) {
		this.connector = new JedisStorageConnector(host, port, timeout);
	}


	@Subscribe
	public void handleStoreRequest(StoreRequest request) {
		try {
			this.connector.store(request.getKey(), request.getPayload(), request.getTtl(), 10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
