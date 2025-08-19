package com.strandls.traits;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.glassfish.jersey.servlet.ServletContainer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.strandls.activity.controller.ActivityServiceApi;
import com.strandls.taxonomy.controllers.SpeciesServicesApi;
import com.strandls.taxonomy.controllers.TaxonomyTreeServicesApi;
import com.strandls.traits.controller.TraitsControllerModule;
import com.strandls.traits.dao.TraitsDAOModule;
import com.strandls.traits.services.Impl.TraitsServiceModule;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class TraitsServeletContextListener extends GuiceServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(TraitsServeletContextListener.class);

	@Override
	protected Injector getInjector() {

		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {
				Configuration configuration = new Configuration();
				try {
					for (Class<?> cls : getEntityClassesFromPackage("com")) {
						configuration.addAnnotatedClass(cls);
					}
				} catch (ClassNotFoundException | IOException | URISyntaxException e) {
					logger.error(e.getMessage(), e);
				}

				configuration = configuration.configure();
				SessionFactory sessionFactory = configuration.buildSessionFactory();

				Map<String, String> props = new HashMap<>();
				props.put("jakarta.ws.rs.Application", ApplicationConfig.class.getName());
				props.put("jersey.config.server.provider.packages", "com");
				props.put("jersey.config.server.wadl.disableWadl", "true");

				bind(SessionFactory.class).toInstance(sessionFactory);
				bind(SpeciesServicesApi.class).in(Scopes.SINGLETON);
				bind(TaxonomyTreeServicesApi.class).in(Scopes.SINGLETON);
				bind(ActivityServiceApi.class).in(Scopes.SINGLETON);
				bind(Headers.class).in(Scopes.SINGLETON);
				bind(ServletContainer.class).in(Scopes.SINGLETON);

				serve("/api/*").with(ServletContainer.class, props);
			}
		}, new TraitsControllerModule(), new TraitsServiceModule(), new TraitsDAOModule());
	}

	protected List<Class<?>> getEntityClassesFromPackage(String packageName)
			throws URISyntaxException, IOException, ClassNotFoundException {

		List<String> classNames = getClassNamesFromPackage(packageName);
		List<Class<?>> classes = new ArrayList<>();
		for (String className : classNames) {
			Class<?> cls = Class.forName(className);
			Annotation[] annotations = cls.getAnnotations();
			for (Annotation annotation : annotations) {
				if (annotation instanceof jakarta.persistence.Entity) {
					classes.add(cls);
				}
			}
		}
		return classes;
	}

	private static ArrayList<String> getClassNamesFromPackage(final String packageName)
			throws URISyntaxException, IOException {

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		ArrayList<String> names = new ArrayList<>();
		URL packageURL = classLoader.getResource(packageName);

		if (packageURL == null)
			return names;
		URI uri = new URI(packageURL.toString());
		File folder = new File(uri.getPath());

		try (Stream<Path> files = Files.find(Paths.get(folder.getAbsolutePath()), 999,
				(p, bfa) -> bfa.isRegularFile())) {
			files.forEach(file -> {
				String name = file.toFile().getAbsolutePath().replace(folder.getAbsolutePath() + File.separatorChar, "")
						.replace(File.separatorChar, '.');
				if (name.contains(".")) {
					name = packageName + '.' + name.substring(0, name.lastIndexOf('.'));
					names.add(name);
				}
			});
		}
		return names;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		Injector injector = (Injector) servletContextEvent.getServletContext().getAttribute(Injector.class.getName());
		SessionFactory sessionFactory = injector.getInstance(SessionFactory.class);
		sessionFactory.close();

		super.contextDestroyed(servletContextEvent);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					logger.info("Deregistering JDBC driver {}", driver);
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					logger.error("Error deregistering JDBC driver {}", driver, ex);
				}
			} else {
				logger.trace("Not deregistering JDBC driver {}", driver);
			}
		}
	}
}
