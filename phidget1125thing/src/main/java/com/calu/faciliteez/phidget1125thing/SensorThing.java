package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;

@ThingworxPropertyDefinitions(

properties = {
		@ThingworxPropertyDefinition(name = "P1125_Temp", description = "The current temperature of the Phidget 1125 sensor", baseType = "NUMBER", aspects = {
				"dataChangeType:ALWAYS", "dataChangeThreshold:0",
				"cacheTime:0", "isPersistent:FALSE", "isReadOnly:TRUE",
				"pushType:VALUE", "defaultValue:0" }),
		@ThingworxPropertyDefinition(name = "P1125_RH", description = "The current relative humidity of the Phidget 1125 sensor", baseType = "NUMBER", aspects = {
				"dataChangeType:ALWAYS", "dataChangeThreshold:0",
				"cacheTime:0", "isPersistent:FALSE", "isReadOnly:TRUE",
				"pushType:VALUE", "defaultValue:0" }), })
public class SensorThing extends VirtualThing {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	private static final String PI_HOME = "/home/pi";

	private final String name;
	private final String description;
	private final String simulated;
	private final ConnectedThingClient client;

	/**
	 * @param name
	 * @param description
	 * @param simulated
	 * @param client
	 */
	public SensorThing(String name, String description, String simulated,
			ConnectedThingClient client) {
		super();
		this.name = name;
		this.description = description;
		this.simulated = simulated;
		this.client = client;

		initializeFromAnnotations();
		try {
			setDefaultPropertyValue("P1125_Temp");
			setDefaultPropertyValue("P1125_RH");
		} catch (Exception localException) {
			LOG.error("Failed to set default value.", localException);
		}
	}
	
	@Override
	public void processScanRequest() throws Exception {
		super.processScanRequest();
		
		LOG.info("Looping");
		
	}

	/**
	 * Sets the current value of a property to the default value provided in its
	 * annotation.
	 * 
	 * @param propertyName
	 * @throws Exception
	 */
	protected void setDefaultPropertyValue(String propertyName)
			throws Exception {
		setProperty(propertyName, getProperty(propertyName)
				.getPropertyDefinition().getDefaultValue().getValue());
	}

}
