/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

/**
 * Add class description
 *
 * @author tshiou
 */
public class StoreRequest {
	private final String key;
	private final byte[] payload;
	private final int ttl;

	public StoreRequest(String key, byte[] payload, int ttl) {
		this.key = key;
		this.payload = payload;
		this.ttl = ttl;
	}

	public String getKey() {
		return this.key;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public int getTtl() {
		return this.ttl;
	}

}
