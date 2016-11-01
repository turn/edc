/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage.impl;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.StorageConnector;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.common.io.BaseEncoding;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * ConnectionFactory connector to Redis using Jedis library
 * (https://github.com/xetorthio/jedis)
 *
 * @author tshiou
 */
public class JedisStorageConnector extends StorageConnector {

	private static final String DEFAULT_SUBKEY = "_SINGLEFIELD";

	private final Jedis jedis;

	public JedisStorageConnector(String host, String port, int timeout) throws IOException {

		this.jedis = new Jedis(host, Integer.parseInt(port), timeout);

		try {
			this.jedis.ping();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void set(String key, byte[] value, int ttl, int timeout) throws IOException {
		set(key, DEFAULT_SUBKEY, value, ttl, timeout);
	}

	@Override
	public void set(String key, String subkey, byte[] value, int ttl, int timeout) throws IOException {
		if (subkey == null || subkey.isEmpty()) {
			subkey = DEFAULT_SUBKEY;
		}

		try {
			Pipeline p = jedis.pipelined();
			p.hset(key, subkey, BaseEncoding.base64().encode(value));
			p.expire(key, ttl);
			p.sync();
		} catch (Exception e) {
			throw new IOException(e.getCause());
		}
	}

	@Override
	public byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
		return get(key, DEFAULT_SUBKEY, timeout);
	}

	@Override
	public byte[] get(String key, String subkey, int timeout) throws KeyNotFoundException, TimeoutException, IOException {
		if (subkey == null || subkey.isEmpty()) {
			subkey = DEFAULT_SUBKEY;
		}

		String res;
		try {
			res = jedis.hget(key, subkey);
		} catch (Exception e) {
			throw new IOException(e.getCause());
		}
		if (res == null) {
			throw new KeyNotFoundException(key + ":" + subkey);
		}
		return BaseEncoding.base64().decode(res);
	}

	@Override
	public void close() {
		this.jedis.close();
	}
}
