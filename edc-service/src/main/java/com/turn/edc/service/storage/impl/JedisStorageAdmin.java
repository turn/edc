/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage.impl;

import com.turn.edc.service.storage.StorageAdmin;

import java.io.IOException;

import com.google.common.base.Splitter;
import redis.clients.jedis.Jedis;

/**
 * Add class description
 *
 * @author tshiou
 */
public class JedisStorageAdmin implements StorageAdmin {

	private final Jedis jedis;

	public JedisStorageAdmin(String host, int port) {
		this.jedis = new Jedis(host, port, 10);
	}

	@Override
	public int start() {




		for (String line : Splitter.on('\n').split(jedis.info())) {
			if (line.contains("maxmemory:")) {
				return (int) (Long.parseLong(line.split(":")[1]) / 1000 / 1000);
			}
		}
		return 0;
	}

	@Override
	public void stop() {

	}
}
