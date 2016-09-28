/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage.impl;

import com.turn.edc.service.storage.StorageAdmin;

import java.io.IOException;

import com.google.common.net.HostAndPort;

/**
 * Add class description
 *
 * @author tshiou
 */
public class SpymemcachedStorageAdmin extends StorageAdmin {
	private final HostAndPort hostAndPort;

	public SpymemcachedStorageAdmin(String host, int port) {
		this.hostAndPort = HostAndPort.fromParts(host, port);
	}

	@Override
	public int getMaxSizeInMb() throws IOException {
		return 0;
	}

	@Override
	public boolean isHealthy() {
		return false;
	}

	@Override
	public void stop() {

	}
}
