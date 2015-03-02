package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends BaseEdgeServer {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	public static final String THING_NAME = "phidget1125thing";
	public static final String THING_DESCRIPTION = "Phidget RH and Tempature Sensor";

	public static void main(String[] args) {
		LOG.info("Hello World!");

		try {
			parseArguments(args);
			client = getEdgeClient();
		} catch (Exception e) {
			LOG.error("Server shutdown due to an exception.", e);
		}
	}

}
