package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

/**
 * The runnable Class for this program
 * 
 * Derived from the example temperature thing main class wreichardt wrote.
 * 
 * @author Faye Bickerton
 */
public class Main extends BaseEdgeServer {
	/**
	 * Main's Logger
	 */
	private static final Logger	LOG					= LoggerFactory.getLogger(Main.class);
	/**
	 * The string that is appended to the 3rd argument of the program
	 */
	public static final String	THING_NAME			= "-p1125-thing";
	/**
	 * A description of the phidget1125thing
	 */
	public static final String	THING_DESCRIPTION	= "Phidget RH and Tempature Sensor";
	private static final Level	INFO				= ch.qos.logback.classic.Level.INFO;
	private static final Level	DEBUG				= ch.qos.logback.classic.Level.DEBUG;

	/**
	 * Main routine that starts up the remote thing.
	 * 
	 * @param args
	 *            There are 3 required arguments and 1 optional argument that
	 *            has to be passed to the server.<br>
	 * <br>
	 *            Argument 1: (required)<br>
	 *            The address of your ThingWorx server<br>
	 *            Note this should be a ws: or wss: (for HTTPS) url. for example<br>
	 *            wss://maker01.ptcmanaged.com:443/Thingworx/WS<br>
	 *            would be used to contact a server deployed at<br>
	 *            https://maker01.ptcmanaged.com/Thingworx<br>
	 * <br>
	 *            Argument 2: (required)<br>
	 *            The application key that is generated in ThingWorx so the <br>
	 *            application can authenticate with ThingWorx.<br>
	 * <br>
	 *            Argument 3: (required)<br>
	 *            A unique name for this Remote Thing. Will have "-p1125-thing" <br>
	 *            appended to the end of this unique name.<br>
	 *            This combined name will be the name of your thing in
	 *            ThingWorx.<br>
	 * <br>
	 *            Argument 4: (optional)<br>
	 *            Put "simulated" as the 4 argument to simulate data instead of<br>
	 *            connecting to a Phidget InterfaceKit with an attached P1125<br>
	 *            Temperature/RH sensor.<br>
	 */
	public static void main(String[] args) {
		// Set the logging level
		setLoggingLevel(INFO);

		try {
			// parse the arguments
			parseArguments(args);

			// Create an Edge Client
			client = getEdgeClient();

			// Make a new SensorThing and bind it to the client
			client.bindThing(new SensorThing(thingName + THING_NAME, THING_DESCRIPTION, simulated, client));
			LOG.debug("Connecting to " + address + " using key " + appKey);

			// Start the Micro Edge Client server
			client.start();

			// Call the infinite loop the program stays in.
			monitorThings();
		}
		// Catch any errors that may be thrown in initialization
		catch (Exception e) {
			LOG.error("Server shutdown due to an exception.", e);
		}
	}

	/**
	 * Sets the Logging level that our Logger outputs
	 * 
	 * @param level
	 *            The logback level.
	 */
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

}
