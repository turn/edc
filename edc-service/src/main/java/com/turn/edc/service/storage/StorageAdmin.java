/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.service.storage;

import java.io.IOException;

/**
 * Add class description
 *
 * @author tshiou
 */
public abstract class StorageAdmin {
	public abstract int getMaxSizeInMb() throws IOException;

	public abstract boolean isHealthy();

	public abstract void stop();
}
