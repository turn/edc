/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage.impl;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.storage.StorageConnector;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Protocol;

/**
 * ConnectionFactory connector to Redis using Jedis library
 * (https://github.com/xetorthio/jedis)
 *
 * @author tshiou
 */
public class JedisStorageConnector extends StorageConnector {

	private static final String DEFAULT_SUBKEY = "_SINGLEFIELD";

	private static final Long TTL_SET_FAILURE = 0l;

	private static final Long TTL_SET_SUCCESS = 1l;

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
		config.setMaxTotal(4);
		// We prioritize writing the data ASAP so do validation after we attempted the write
		config.setTestOnReturn(true);

		// Set connection timeout to 0 so we can keep idle connects
		this.jedisPool = new JedisPool(config, this.host, this.port, 0, timeout, null, Protocol.DEFAULT_DATABASE, null);

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
			p.hset(key, subkey, Base64.getEncoder().encodeToString(value));
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

		// Jedis instance will be auto-returned to the pool
		try (Jedis jedis = jedisPool.getResource()){
			// Test connectivity, this is cheaper than a full validation (i.e. ping)
			if (!jedis.isConnected()) {
				jedis.connect();
			}

			String res = jedis.hget(key, subkey);
			if (res == null) {
				throw new KeyNotFoundException(key + ":" + subkey);
			}
			return Base64.getDecoder().decode(res);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean setTTL(String key, int ttl, int timeout) throws IOException {
		// Jedis instance will be auto-returned to the pool
		Long status = TTL_SET_FAILURE;
		try (Jedis jedis = jedisPool.getResource()) {
			// Test connectivity, this is cheaper than a full validation (i.e. ping)
			if (!jedis.isConnected()) {
				jedis.connect();
			}

			Pipeline p = jedis.pipelined();
			status = p.expire(key, ttl).get();
			p.sync();
		} catch (Exception e) {
			throw new IOException(e.getCause());
		}
		return status == TTL_SET_SUCCESS;
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
