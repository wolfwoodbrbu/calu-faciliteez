package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

public class Main extends BaseEdgeServer {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	public static final String THING_NAME = "-p1125-thing";
	public static final String THING_DESCRIPTION = "Phidget RH and Tempature Sensor";
	private static final Level INFO = ch.qos.logback.classic.Level.INFO;
	private static final Level DEBUG = ch.qos.logback.classic.Level.DEBUG;

	public static void main(String[] args) {
		setLoggingLevel(INFO);
		try {
			parseArguments(args);
			client = getEdgeClient();
			client.bindThing(new SensorThing(thingName + THING_NAME, THING_DESCRIPTION,
					simulated, client));
			LOG.debug("Connecting to " + address + " using key " + appKey);
			client.start();
			monitorThings();
		} catch (Exception e) {
			LOG.error("Server shutdown due to an exception.", e);
		}
	}
	
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
	    ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
	    root.setLevel(level);
	}

}
