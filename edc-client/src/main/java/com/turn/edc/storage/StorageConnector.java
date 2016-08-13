/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.client.KeyNotFoundException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Storage layer connector interface
 *
 * @author tshiou
 */
public interface StorageConnector {

	void store(String key, byte[] value, int ttl, int timeout) throws IOException;

	byte[] get(String key, int timeout) throws KeyNotFoundException, TimeoutException, IOException;

	void close();
}
