/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.google.common.io.Closeables;
import com.google.common.net.HostAndPort;

/**
 * Immutable representation of a cache instance
 *
 * @author tshiou
 */

public class CacheInstance {
	private final HostAndPort hostAndPort;
	private final int cacheSize;
	private final int hashCode;

	// Instance to represent null object (null design pattern)
	public static final CacheInstance NULL_CACHE_INSTANCE = new CacheInstance(
			HostAndPort.fromParts("", 0), -1);

	public CacheInstance(HostAndPort hostAndPort) {
		this(hostAndPort, 0);
	}

	public CacheInstance(HostAndPort hostAndPort, int cacheSize) {
		this.hostAndPort = hostAndPort;
		this.cacheSize = cacheSize;
		this.hashCode = this.hostAndPort.hashCode();
	}

	public HostAndPort getHostAndPort() {
		return this.hostAndPort;
	}

	public int getCacheSize() {
		return this.cacheSize;
	}

	public static CacheInstance fromString(String str) {
		if (str == null || str.isEmpty()) {
			return NULL_CACHE_INSTANCE;
		}

		HostAndPort hap;
		try {
			hap = HostAndPort.fromString(str.split("-")[0]);
		} catch (Exception e) {
			return NULL_CACHE_INSTANCE;
		}
		int cacheSize;
		try {
			cacheSize = Integer.parseInt(str.split("-")[1]);
		} catch (Exception ignore) {
			cacheSize = 0;
		}
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

	public byte[] serialize() throws IOException {
		ObjectOutput out;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			out = new ObjectOutputStream(bos);
			out.writeObject(this.hostAndPort.getHostText());
			out.writeInt(this.hostAndPort.getPort());
			out.writeInt(this.cacheSize);
			out.flush();
			return bos.toByteArray();
		}
	}

	public static CacheInstance deserialize(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		String host;
		int port;
		int cacheSize;
		try (ObjectInput in = new ObjectInputStream(bis)) {
			cacheSize = in.readInt();
			port = in.readInt();
			host = (String) in.readObject();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return new CacheInstance(HostAndPort.fromParts(host, port), cacheSize);
	}
}
