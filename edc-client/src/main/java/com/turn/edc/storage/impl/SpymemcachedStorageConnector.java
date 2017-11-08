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
	private final String host;
	private final int port;

	public SpymemcachedStorageConnector(String host, int port, int timeout) throws IOException {
		this.host = host;
		this.port = port;
		this.client = new MemcachedClient(new InetSocketAddress(this.host, this.port));
		// TODO: Sanity check host and port
	}


	@Override
	public void set(String key, byte[] value, int ttl, int timeout) throws IOException {
		set(key, "", value, ttl, timeout);
	}

	// not supported
	@Override
	public void set(String key, String subkey, byte[] value, int ttl, int timeout) throws IOException {
		try {
			if (client.set(key + ":" + subkey, ttl, value).get() == false) {
				throw new IOException("Load failed");
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new IOException(e);
		}
	}

	// not supported
	@Override
	public byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
		return get(key, "", timeout);
	}

	@Override
	public byte[] get(String key, String subkey, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
		return (byte[]) client.get(key + ":" + subkey);
	}
	
	@Override
	public boolean setTTL(String key, int ttl, int timeout) throws IOException {
		return false;
	}

	@Override
	public void close() {

	}

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

}
