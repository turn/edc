/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage.impl;

import com.turn.edc.service.storage.StorageAdmin;

import com.google.common.net.HostAndPort;

/**
 * Add class description
 *
 * @author tshiou
 */
public class SpymemcachedStorageAdmin implements StorageAdmin {
	private final HostAndPort hostAndPort;

	public SpymemcachedStorageAdmin(String host, int port) {
		this.hostAndPort = HostAndPort.fromParts(host, port);
	}

	@Override
	public int start() {
		return 0;
	}

	@Override
	public void stop() {

	}
}
