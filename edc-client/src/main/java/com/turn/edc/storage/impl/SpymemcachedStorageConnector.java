/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage.impl;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.StorageConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;

/**
 * Add class description
 *
 * @author tshiou
 */
public class SpymemcachedStorageConnector extends StorageConnector {

	private final MemcachedClient client;

	public SpymemcachedStorageConnector(String host, String port, int timeout) throws IOException {
		this.client = new MemcachedClient(new InetSocketAddress(host, 11211));
		// TODO: Sanity check host and port
	}


	@Override
	public void store(String key, byte[] value, int ttl, int timeout) throws IOException {
		try {
			if (client.set(key, ttl, value).get() == false) {
				throw new IOException("Load failed");
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	@Override
	public byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
		return (byte[]) client.get(key);
	}

	@Override
	public void close() {

	}
}
