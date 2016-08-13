/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import com.google.common.net.HostAndPort;

/**
 * Immutable representation of a cache instance
 *
 * @author tshiou
 */

public class CacheInstance {
	public static final String EDC_SERVICE_NAME = "EDC-CLUSTER";

	private final HostAndPort hostAndPort;
	private final int cacheSize;
	private final int hashCode;

	// Instance to represent null object (null design pattern)
	public static final CacheInstance NULL_CACHE_INSTANCE = new CacheInstance(
			HostAndPort.fromParts("", 0), -1);

	public CacheInstance(HostAndPort hostAndPort, int cacheSize) {
		this.hostAndPort = hostAndPort;
		this.cacheSize = cacheSize;
		this.hashCode = this.hostAndPort.hashCode();
	}

	public HostAndPort getHostAndPort() {
		return this.getHostAndPort();
	}

	public int getCacheSize() {
		return this.cacheSize;
	}

	public static CacheInstance fromString(String str) {
		if (str == null || str.isEmpty()) {
			return NULL_CACHE_INSTANCE;
		}
		HostAndPort hap = HostAndPort.fromString(str.split("-")[0]);
		int cacheSize = Integer.parseInt(str.split("-")[1]);
		return new CacheInstance(hap, cacheSize);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof CacheInstance) {
			CacheInstance that = (CacheInstance) o;
			return this.hostAndPort.equals(that.hostAndPort);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.hashCode;
	}

	@Override
	public String toString() {
		return hostAndPort.toString() + "-" + Integer.toString(cacheSize);
	}
}
