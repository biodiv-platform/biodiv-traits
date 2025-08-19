package com.strandls.traits;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.ws.rs.core.Application;

@OpenAPIDefinition(info = @Info(title = "Traits Module MicroServices", version = "1.0.0"), servers = {
		@Server(url = "http://localhost:8080/traits-api/api") })
public class ApplicationConfig extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

	public ApplicationConfig() {
		logger.info("Initializing ApplicationConfig...");
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		singletons.add(new ContainerLifecycleListener() {
			@Override
			public void onStartup(Container container) {
				logger.info("Starting container & bridging Guice to HK2...");

				ServiceLocator locator = container.getApplicationHandler().getInjectionManager()
						.getInstance(ServiceLocator.class);

				GuiceBridge.getGuiceBridge().initializeGuiceBridge(locator);
				GuiceIntoHK2Bridge guiceBridge = locator.getService(GuiceIntoHK2Bridge.class);

				Injector injector = null;

				if (container instanceof ServletContainer) {
					ServletContainer servletContainer = (ServletContainer) container;
					injector = (Injector) servletContainer.getServletContext().getAttribute(Injector.class.getName());
				}

				if (injector == null) {
					logger.error(
							"Guice Injector is null! Check GuiceServletConfig and ensure injector is set in ServletContext.");
					throw new IllegalStateException("Guice Injector not found in ServletContext");
				}

				guiceBridge.bridgeGuiceInjector(injector);
			}

			@Override
			public void onShutdown(Container container) {
				logger.info("Container shutdown...");
			}

			@Override
			public void onReload(Container container) {
				logger.info("Container reload...");
			}
		});

		return singletons;
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> classes = new HashSet<>();
		classes.add(OpenApiResource.class); // Add Swagger OpenApiResource only here
		return classes;
	}

}
