/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.storage.StorageConnection;
import com.turn.edc.storage.ConnectionFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.orbitz.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Routing layer for edc requests
 *
 * @author tshiou
 */
public class RequestRouter extends DiscoveryListener {
	private static final Logger logger = LoggerFactory.getLogger(RequestRouter.class);

	@Inject
	private ConnectionFactory connectorFactory;

	private volatile Map<Integer, StorageConnection> routingMap = Maps.newConcurrentMap();
	private final static int TIMEOUT = 10;

	public RequestRouter() {
	}

	public byte[] get(CacheInstance source, String key) throws KeyNotFoundException, TimeoutException, IOException {
		StorageConnection connection = routingMap.get(source.hashCode());

		if (connection == null) {
			throw new IOException("Data source not registered!");
		}

		return connection.get(key, TIMEOUT);
	}

	public boolean store(CacheInstance destination, StoreRequest request) {

		StorageConnection connection = routingMap.get(destination.hashCode());

		if (connection == null) {
			return false;
		}

		connection.post(request);

		return true;
	}

	/**
	 * Closes all connections and clears the routing map
	 */
	public void close() {
		for(Iterator<StorageConnection> it = routingMap.values().iterator(); it.hasNext(); ) {
			StorageConnection conn = it.next();
			conn.close();
			it.remove();
		}
	}

	@Override
	public void update(List<CacheInstance> availableInstances) {
		Map<Integer, StorageConnection> newRoutingMap = Maps.newConcurrentMap();

		for (CacheInstance instance : availableInstances) {
			if (this.routingMap.containsKey(instance.hashCode())) {
				newRoutingMap.put(instance.hashCode(), this.routingMap.get(instance.hashCode()));
			} else {
				StorageConnection newConnection;
				try {
					newConnection = connectorFactory.create(
							instance.getHostAndPort().getHostText(),
							Integer.toString(instance.getHostAndPort().getPort()),
							TIMEOUT
					);
					newRoutingMap.put(instance.hashCode(), newConnection);
				} catch (IOException ioe) {
					logger.error("Cache instance {} found but connection was not able to be established", instance.toString());
					logger.debug(ExceptionUtils.getStackTrace(ioe));
					// TODO: need to remove from selection scheme
				}
			}
		}

		Map<Integer, StorageConnection> tempReference = this.routingMap;

		this.routingMap = newRoutingMap;

		for (StorageConnection conn : tempReference.values()) {
			conn.close();
		}
	}

}
