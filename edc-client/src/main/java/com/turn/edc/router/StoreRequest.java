/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

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

}
