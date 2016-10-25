package com.turn.edc.selection;

import com.turn.edc.discovery.CacheInstance;
import com.turn.edc.exception.InvalidParameterException;

import java.util.Collection;

/**
 * Provides a selection of cache instances
 *
 * @author tshiou
 */
public interface SelectionProvider {
	Collection<CacheInstance> selectInstances(int n) throws InvalidParameterException;
}
