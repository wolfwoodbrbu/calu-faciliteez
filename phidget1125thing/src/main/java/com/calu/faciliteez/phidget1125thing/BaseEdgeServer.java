package com.calu.faciliteez.phidget1125thing;

import com.thingworx.common.RESTAPIConstants;
import com.thingworx.communications.client.ClientConfigurator;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Author: wreichardt
 * Date: 6/13/14
 * Copyright PTC 2014
 */

/**
 * This class simplifies starting an edge server. It manages the command line
 * parameters an the construction of the edge client and monitoring of any
 * Things registered with it.
 */
public abstract class BaseEdgeServer {
	private static final Logger LOG = LoggerFactory
			.getLogger(BaseEdgeServer.class);
	public static final int POLLING_INTERVAL_MILLISECONDS = 10000;
	@SuppressWarnings("restriction")
	protected static SignalHandler oldSigTERM;
	protected static ConnectedThingClient client;
	protected static String address;
	protected static String appKey;
	protected static String simulated;

	/**
	 * Create an edge client configuration that will use the provided API key
	 * for authentication.
	 * 
	 * @return a client configurator.
	 */
	protected static ClientConfigurator getClientConfigurator() {
		ClientConfigurator config = new ClientConfigurator();
		config.setUri(address);
		config.getSecurityClaims().addClaim(RESTAPIConstants.PARAM_APPKEY,
				appKey);
		config.ignoreSSLErrors(true);
		return config;
	}

	/**
	 * Validate command line arguments and put then into fields for use later.
	 * 
	 * @param args
	 *            command line arguments.
	 */
	protected static void parseArguments(String[] args) {
		if (args.length < 2) {
			System.out
					.println("A minimum of two arguments is required. Server Hostname and Application Key. Optionally, if the third argument is 'simulated' then simulated reading will be used instead of real hardware.");
			System.exit(0);
		}

		LOG.debug("EDGE SERVER STARTING....");
		// Note this should be a ws: or wss: (for HTTPS) url. for example
		// wss://maker01.ptcmanaged.com:443/Thingworx/WS
		// would be used to contact a server deployed at
		// https://maker01.ptcmanaged.com/Thingworx
		address = args[0];
		// You must generate an API key within the Thingworx composer and prvide
		// its value on the command line.
		appKey = args[1];

		if (args.length > 2) {
			simulated = args[2];
		}

	}

	/**
	 * Create a client for the specified server using the provided API key.
	 * Attach the shutdown code to the termination signal so that the client
	 * will disconnect when the program is stopped with a CTRL-C.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected static ConnectedThingClient getEdgeClient() throws Exception {
		ConnectedThingClient aClient = new ConnectedThingClient(
				getClientConfigurator(), null);

		attachClientShutdownToSigTerm();
		return aClient;
	}

	/**
	 * Attach client disconnect handlers for when the program gets a signal to
	 * stop running.
	 */
	@SuppressWarnings("restriction")
	protected static void attachClientShutdownToSigTerm() {
		oldSigTERM = Signal.handle(new Signal("TERM"), new SignalHandler() {
			public void handle(Signal signal) {
				try {
					LOG.info("Shutting client down...");
					client.shutdown();
					LOG.info("Successfully shut down client.");
				} catch (Exception e) {
					LOG.error("Failed to properly shutdown client.", e);
				}
				if (oldSigTERM != null) {
					oldSigTERM.handle(signal);
				}
			}
		});

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					LOG.info("Shutting client down...");
					client.shutdown();
					LOG.info("Successfully shut down client.");
				} catch (Exception e) {
					LOG.error("Failed to properly shutdown client.", e);
				}

			}
		});
	}

	/**
	 * Pushes all values of all things being monitored up to the server. This
	 * function will not return. It will repeat pushing values every
	 * POLLING_INTERVAL_MILLISECONDS.
	 * 
	 * @throws InterruptedException
	 */
	protected static void monitorThings() throws InterruptedException {

		// Run the client until it is shutdown with SIG TERM
		while (true) {
			if (client.getEndpoint().isConnected()) {
				for (VirtualThing vt : client.getThings().values()) {
					try {
						vt.processScanRequest();
					} catch (Exception eProcessing) {
						LOG.error(
								"Error Processing Scan Request for ["
										+ vt.getName() + "] ", eProcessing);
					}
				}
			}

			Thread.sleep(POLLING_INTERVAL_MILLISECONDS);
		}
	}

}
