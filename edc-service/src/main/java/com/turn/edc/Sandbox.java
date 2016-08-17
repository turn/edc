/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc;

import com.turn.edc.service.EDCAdminService;

/**
 * Add class description
 *
 * @author tshiou
 */
public class Sandbox {

	public static void main(String[] args) {
		EDCAdminService service =
				EDCAdminService.builder().withRedisStorage("localhost", 6379).withZkServiceDiscovery("localhost:2181").forServiceType("EDC").build();
		service.start();
	}
}
