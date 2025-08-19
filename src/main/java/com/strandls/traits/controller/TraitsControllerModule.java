/**
 * 
 */
package com.strandls.traits.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * @author Abhishek Rudra
 *
 */
public class TraitsControllerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TraitsController.class).in(Scopes.SINGLETON);

	}
}
