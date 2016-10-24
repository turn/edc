/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage;

import java.io.IOException;

/**
 * Storage admin interface
 *
 * @author tshiou
 */
public interface StorageAdmin {
	/**
	 * Get size of cache in MB
	 *
	 * @return size of allocated cache size in MB
	 *
	 * @throws IOException If the storage layer is unreachable
	 */
	int getMaxSizeInMb() throws IOException;

	boolean isHealthy();

	void stop();
}
