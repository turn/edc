package com.turn.edc.discovery;

import com.google.common.net.HostAndPort;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for {@link CacheInstance}
 *
 * @author tshiou
 */
@Test
public class CacheInstanceTest {


	@Test
	public void testFromString() {
		// Valid cases
		Assert.assertEquals(CacheInstance.fromString("host.domain:1234-120"),
				new CacheInstance(HostAndPort.fromParts("host.domain", 1234), 120));
		Assert.assertEquals(CacheInstance.fromString("host.domain:1234-"),
				new CacheInstance(HostAndPort.fromParts("host.domain", 1234), 0));
		Assert.assertEquals(CacheInstance.fromString("host.domain:1234"),
				new CacheInstance(HostAndPort.fromParts("host.domain", 1234), 0));

		// Error cases
		Assert.assertEquals(CacheInstance.fromString(null),
				CacheInstance.NULL_CACHE_INSTANCE);
		Assert.assertEquals(CacheInstance.fromString(""),
				CacheInstance.NULL_CACHE_INSTANCE);
		Assert.assertEquals(CacheInstance.fromString("misformattedhost:port"),
				CacheInstance.NULL_CACHE_INSTANCE);
	}
}
