/*
 * Copyright (C) 2016 Turn Inc. All Rights Reserved.
 * Proprietary and confidential.
 */

package com.turn.edc.exception;

/**
 * Add class description
 *
 * @author tshiou
 */
public class InvalidParameterException extends Exception {

	public InvalidParameterException(String message) {
		super(message);
	}

	public InvalidParameterException(
			String parameterName, String parameterValue, String message) {
		super("Invalid parameter value " + parameterValue + " for parameter " + parameterName + ". " + message);
	}
}
