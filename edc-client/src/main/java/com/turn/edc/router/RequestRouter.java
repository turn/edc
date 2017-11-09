/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

import com.turn.edc.exception.KeyNotFoundException;
import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.discovery.DiscoveryListener;
import com.turn.edc.selection.CacheInstanceSelector;
import com.turn.edc.storage.StorageConnection;
import com.turn.edc.storage.ConnectionFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

	@Inject
	private CacheInstanceSelector selectionLayer;

	private volatile Map<Integer, StorageConnection> routingMap = Maps.newConcurrentMap();
	private final static int TIMEOUT = 10;

	public RequestRouter() {
	}

	public byte[] get(CacheInstance source, String key, String subkey)
			throws KeyNotFoundException, TimeoutException, IOException {
		StorageConnection connection = getConnection(source);
		
		return connection.get(key, subkey, TIMEOUT);
	}

	public boolean store(CacheInstance destination, StoreRequest request) {

		StorageConnection connection = routingMap.get(destination.hashCode());

		if (connection == null) {
			logger.debug("Could not route request to {}", destination.toString());
			return false;
		}

		logger.debug("Posting store request {} to destination {}",
				request.toString(), destination.toString());

		connection.post(request);

		return true;
	}
	
	public boolean setTTL(CacheInstance destination, String key, int ttl) 
			throws TimeoutException, IOException{

		StorageConnection connection = getConnection(destination);

		return connection.setTTL(key, ttl, TIMEOUT);
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
		logger.debug("Updating routing layer with {} instances", availableInstances.size());
		Map<Integer, StorageConnection> newRoutingMap = Maps.newConcurrentMap();

		List<CacheInstance> unestablishedConnections = Lists.newArrayList();
		// Update connection map with new list of available instances
		for (CacheInstance instance : availableInstances) {
			// If the connection already exists, reuse old connection
			if (this.routingMap.containsKey(instance.hashCode())) {
				newRoutingMap.put(instance.hashCode(), this.routingMap.get(instance.hashCode()));
			} else {
				// else create a new connection
				StorageConnection newConnection;
				try {
					newConnection = connectorFactory.create(
							instance.getHostAndPort().getHostText(),
							instance.getHostAndPort().getPort(),
							TIMEOUT
					);
					newRoutingMap.put(instance.hashCode(), newConnection);
				} catch (Exception ex) {
					logger.error("Cache instance {} found but connection was not able to be established", instance.toString());
					logger.debug(ExceptionUtils.getStackTrace(ex));
					unestablishedConnections.add(instance);
				}
			}
		}

		// Update the selection layer with the broken connections removed
		if (!unestablishedConnections.isEmpty()) {
			logger.debug("Removing {} unestablished connections from the selection layer",
					unestablishedConnections.size());

			// Remove unestablishable connections from the list of available instances
			availableInstances.removeAll(unestablishedConnections);

			this.selectionLayer.update(availableInstances);
		}

		// Hold on to old map for GC
		Map<Integer, StorageConnection> oldReference = this.routingMap;

		// Update current map reference
		this.routingMap = newRoutingMap;
		logger.debug("New routing map updated with size {}", newRoutingMap.size());

		// Remove live connections from the old map
		newRoutingMap.keySet().forEach(oldReference::remove);

		// Close all remaining (presumed dead) connections in the old connection map
		// TODO: keep live cross-dc connections
		logger.debug("Closing {} old connections", oldReference.size());
		oldReference.values().forEach(StorageConnection::close);
	}

	/*
	 * Get the storage connection
	 * If the connection not found, create a new one
	 * */
	public StorageConnection getConnection(CacheInstance source) 
			throws TimeoutException, IOException {
		StorageConnection connection = routingMap.get(source.hashCode());

		if (connection == null) {
			logger.info("Existing connection not found, creating new connection: {}", source);
			try {
				connection = connectorFactory.create(
						source.getHostAndPort().getHostText(),
						source.getHostAndPort().getPort(),
						TIMEOUT
				);
			} catch (IOException ioe) {
				logger.error("Unable to establish new connection to {}" + source);
				throw ioe;
			}
			routingMap.put(source.hashCode(), connection);
		}
		return connection;
	}
}
