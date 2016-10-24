/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage.impl;

import com.turn.edc.service.storage.StorageAdmin;

import java.io.IOException;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Storage admin using Jedis redis java client
 *
 * @author tshiou
 */
public class JedisStorageAdmin implements StorageAdmin {

	private static final Logger LOG = LoggerFactory.getLogger(JedisStorageAdmin.class);

	private final Jedis jedis;

	private final static String MAXMEMORY_KEY = "maxmemory:";
	private final static String REDIS_INFO_SEPARATOR = ":";

	public JedisStorageAdmin(String host, int port) {
		this.jedis = new Jedis(host, port, 10);
	}

	@Override
	public int getMaxSizeInMb() throws IOException {
		String jedisInfo = jedis.info();

		if (Strings.isNullOrEmpty(jedisInfo)) {
			throw new IOException("Could not get redis instance info");
		}

		LOG.debug(jedis.info());
		try {
			for (String line : Splitter.on('\n').split(jedis.info())) {
				if (line.contains(MAXMEMORY_KEY)) {
					// returned in bytes
					return (int) (Long.parseLong(
							line.split(REDIS_INFO_SEPARATOR)[1].trim()) / 1000 / 1000);
				}
			}
		} catch (JedisConnectionException jce) {
			throw new IOException(jce);
		} catch (NumberFormatException nfe) {
			LOG.error("Failed to parse redis info");
			LOG.error(ExceptionUtils.getStackTrace(nfe));
		}
		return 0;
	}

	@Override
	public boolean isHealthy() {
		try {
			// check DB is up
			jedis.ping();

			// check if we can write
			jedis.set("_EDC_TEST_KEY", "_");
		} catch (Exception e) {
			return false;
		} finally {
			try {
				jedis.del("_EDC_TEST_KEY");
			} catch (Exception e) {
				// ignore if this fails
			}
		}

		return true;
	}

	@Override
	public void stop() {
		jedis.close();
	}
}
