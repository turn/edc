/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.discovery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import com.google.common.net.HostAndPort;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.InstanceSerializer;

/**
 * Add class description
 *
 * @author tshiou
 */
public class CuratorSerializer implements InstanceSerializer<CacheInstance> {
	@Override
	public byte[] serialize(ServiceInstance<CacheInstance> instance) throws Exception {
		CacheInstance payload = instance.getPayload();
		ObjectOutput out;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			out = new ObjectOutputStream(bos);
			out.writeObject(instance.getName());
			out.writeObject(payload.getHostAndPort().getHostText());
			out.writeInt(payload.getHostAndPort().getPort());
			out.writeInt(payload.getCacheSize());
			out.flush();
			return bos.toByteArray();
		}
	}

	@Override
	public ServiceInstance<CacheInstance> deserialize(byte[] bytes) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		String name;
		String host;
		int port;
		int cacheSize;
		try (ObjectInput in = new ObjectInputStream(bis)) {
			name = (String) in.readObject();
			host = (String) in.readObject();
			port = in.readInt();
			cacheSize = in.readInt();
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		return ServiceInstance.<CacheInstance>builder()
				.name(name)
				.payload(new CacheInstance(HostAndPort.fromParts(host, port), cacheSize))
				.build();
	}
}
