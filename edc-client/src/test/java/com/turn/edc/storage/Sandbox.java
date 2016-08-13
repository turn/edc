/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.storage;

import com.turn.edc.client.KeyNotFoundException;
import com.turn.edc.client.StoreRequest;
import com.turn.edc.storage.bus.StoreRequestSubscriber;
import com.turn.edc.storage.impl.JedisStorageConnector;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;

/**
 * Add class description
 *
 * @author tshiou
 */
public class Sandbox {

	public static void main(String[] args) {

		LinkedBlockingQueue<StoreRequest> requestsQueue = new LinkedBlockingQueue<>();
		StoreRequest req1 = new StoreRequest("localhost:6379", "KEY1", new byte[1], 120);
		StoreRequest req2 = new StoreRequest("localhost:6379", "KEY2", new byte[1], 120);
		StoreRequest req3 = new StoreRequest("localhost:6380", "KEY3", new byte[1], 120);
		StoreRequest req4 = new StoreRequest("localhost:6381", "KEY4", new byte[1], 120);
		StoreRequest req5 = new StoreRequest("localhost:6380", "KEY5", new byte[1], 120);
		StoreRequest req6 = new StoreRequest("localhost:6379", "KEY6", new byte[1], 120);
		StoreRequest req7 = new StoreRequest("localhost:6381", "KEY7", new byte[1], 120);
		StoreRequest req8 = new StoreRequest("localhost:6379", "KEY8", new byte[1], 120);
		StoreRequest req9 = new StoreRequest("localhost:6380", "KEY9", new byte[1], 120);
		StoreRequest req10 = new StoreRequest("localhost:6380", "KEY10", new byte[1], 120);
		StoreRequest req11 = new StoreRequest("localhost:6381", "KEY11", new byte[1], 120);
		StoreRequest req12 = new StoreRequest("localhost:6380", "KEY12", new byte[1], 120);

		requestsQueue.add(req1);
		requestsQueue.add(req2);
		requestsQueue.add(req3);
		requestsQueue.add(req4);
		requestsQueue.add(req5);
		requestsQueue.add(req6);
		requestsQueue.add(req7);
		requestsQueue.add(req8);
		requestsQueue.add(req9);
		requestsQueue.add(req10);
		requestsQueue.add(req11);
		requestsQueue.add(req12);

		Map<String, EventBus> storeRequestEventBus = Maps.newConcurrentMap();
		for (String hostPort : new String[] {"localhost:6379", "localhost:6380", "localhost:6381"}) {
			EventBus eventBus = new EventBus();
			StoreRequestSubscriber request1Subscriber = new StoreRequestSubscriber(hostPort.split(":")[0], hostPort.split(":")[1], 10);
			eventBus.register(request1Subscriber);

			storeRequestEventBus.put(hostPort, eventBus);
		}

		for (StoreRequest request : requestsQueue) {
			if (storeRequestEventBus.containsKey(request.getHostPort())) {
				storeRequestEventBus.get(request.getHostPort()).post(request);
			}
		}

		for (StoreRequest request : requestsQueue) {
			JedisStorageConnector conn = new JedisStorageConnector(request.getHostPort().split(":")[0], request.getHostPort().split(":")[1], 10);
			try {
				if (conn.get(request.getKey(), 10) != null) {
					System.out.println(request.getKey() + " key found: ");
				}
			} catch (KeyNotFoundException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			conn.close();
		}

		JedisStorageConnector connector = new JedisStorageConnector("localhost", "6380", 10);
		try {
			connector.get("KEY1", 10);
		} catch (KeyNotFoundException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
