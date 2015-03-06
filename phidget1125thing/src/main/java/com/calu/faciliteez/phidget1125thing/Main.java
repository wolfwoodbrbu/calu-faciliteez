package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends BaseEdgeServer {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	public static final String THING_NAME = "phidget1125thing";
	public static final String THING_DESCRIPTION = "Phidget RH and Tempature Sensor";

	public static void main(String[] args) {

		try {
			parseArguments(args);
			client = getEdgeClient();
			client.bindThing(new SensorThing(THING_NAME, THING_DESCRIPTION,
					simulated, client));
			LOG.debug("Connecting to " + address + " using key " + appKey);
			client.start();
			monitorThings();
		} catch (Exception e) {
			LOG.error("Server shutdown due to an exception.", e);
		}
	}

}
