/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage.impl;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.StorageConnector;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

/**
 * ConnectionFactory connector to Redis using Jedis library
 * (https://github.com/xetorthio/jedis)
 *
 * @author tshiou
 */
public class JedisStorageConnector extends StorageConnector {

	private static final String DEFAULT_SUBKEY = "_SINGLEFIELD";

	private final String host;
	private final int port;
	private final JedisPool jedisPool;

	// Weak reference since most of the time we don't need the string representation
	private WeakReference<String> toString = new WeakReference<>(null);

	public JedisStorageConnector(String host, String port, int timeout) throws IOException {

		this.host = host;
		this.port = Integer.parseInt(port);
		JedisPoolConfig config = new JedisPoolConfig();
		// TODO: make these configurable
		config.setMaxWaitMillis(100);
		config.setMaxTotal(2);
		// We prioritize writing the data ASAP so do validation after we attempted the write
		config.setTestOnReturn(true);
		this.jedisPool = new JedisPool(config, this.host, this.port, 0);

		// Try pinging once
		try (Jedis jedis = jedisPool.getResource()){
			if (!jedis.ping().equals("PONG")) {
				throw new IOException("Could not reach redis instance: " + toString());
			}
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

		// Jedis instance will be auto-returned to the pool
		try (Jedis jedis = jedisPool.getResource()){
			// Test connectivity, this is cheaper than a full validation (i.e. ping)
			if (!jedis.isConnected()) {
				jedis.connect();
			}

			Pipeline p = jedis.pipelined();
			p.hset(key.getBytes(StandardCharsets.UTF_8), subkey.getBytes(StandardCharsets.UTF_8), value);
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

		byte[] res;
		// Jedis instance will be auto-returned to the pool
		try (Jedis jedis = jedisPool.getResource()){
			// Test connectivity, this is cheaper than a full validation (i.e. ping)
			if (!jedis.isConnected()) {
				jedis.connect();
			}

			res = jedis.hget(key.getBytes(StandardCharsets.UTF_8), subkey.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IOException(e.getCause());
		}
		if (res == null) {
			throw new KeyNotFoundException(key + ":" + subkey);
		}
		return res;
	}

	@Override
	public void close() {
		this.jedisPool.destroy();
	}

	@Override
	public String toString() {
		if (this.toString.get() == null) {
			this.toString = new WeakReference<String>(
					(new StringBuilder())
							.append("Jedis ")
							.append(host)
							.append(":")
							.append(port)
							.toString()
			);
		}
		return this.toString.get();
	}
}
