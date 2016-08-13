/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.router;

import com.turn.edc.discovery.CacheInstance;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

/**
 * Routing layer for edc store requests
 *
 * @author tshiou
 */
public class StoreEventRouter {

	private volatile Map<Integer, EventBus> routingMap = Maps.newConcurrentMap();
	private final static int TIMEOUT = 10;

	public StoreEventRouter() {
	}

	public boolean store(CacheInstance destination, StoreRequest request) {

		EventBus bus = routingMap.get(destination.hashCode());

		if (bus == null) {
			return false;
		}

		bus.post(request);

		return true;
	}

	public void updateRoutingMap(List<CacheInstance> availableInstances) {
		Map<Integer, EventBus> newMap = Maps.newConcurrentMap();

		for (CacheInstance instance : availableInstances) {
			EventBus eventBus = new EventBus();
			StoreRequestSubscriber subscriber = new StoreRequestSubscriber(
					instance.getHostAndPort().getHostText(),
					Integer.toString(instance.getHostAndPort().getPort()),
					TIMEOUT
			);
			eventBus.register(subscriber);
			newMap.put(instance.hashCode(), eventBus);
		}

		synchronized (routingMap) {
			 routingMap = newMap;
		}
	}

}
