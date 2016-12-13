/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

import java.lang.ref.WeakReference;

/**
 * An immutable object representing a cache store request
 *
 * @author tshiou
 */
public class StoreRequest {
	private final String key;
	private final String subkey;
	private final byte[] payload;
	private final int ttl;

	// Weak reference since most of the time we don't need the string representation
	private WeakReference<String> toString = new WeakReference<>(null);

	public StoreRequest(String key, String subkey, byte[] payload, int ttl) {
		this.key = key;
		this.subkey = subkey;
		this.payload = payload;
		this.ttl = ttl;
	}

	public String getKey() {
		return this.key;
	}

	public String getSubkey() {
		return this.subkey;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public int getTtl() {
		return this.ttl;
	}

	@Override
	public String toString() {
		if (this.toString.get() == null) {
			this.toString = new WeakReference<String>(
					(new StringBuilder())
					.append("key=").append(key)
					.append(" subkey=").append(subkey)
					.append(" ttl=").append(ttl)
					.append(" payload_length=").append(payload.length)
					.toString()
			);
		}
		return this.toString.get();
	}

}
