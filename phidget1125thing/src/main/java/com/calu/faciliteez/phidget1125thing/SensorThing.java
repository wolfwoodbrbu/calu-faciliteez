package com.calu.faciliteez.phidget1125thing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phidgets.*;
import com.phidgets.event.*;
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
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private final String name;
	private final String description;
	private final String simulated;
	private final ConnectedThingClient client;
	public InterfaceKitPhidget ik;

	/**
	 * @param name
	 * @param description
	 * @param simulated
	 * @param client
	 * @throws PhidgetException
	 */
	public SensorThing(String name, String description, String simulated,
			ConnectedThingClient client) throws Exception {
		super(name, description, client);
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

		if (simulated != null && simulated.equals("simulated")) {
			LOG.debug("Simulating data!");
		} else {
			ik = new InterfaceKitPhidget();
			ik.addAttachListener(new AttachListener() {
				public void attached(AttachEvent ae) {
					LOG.debug("[Attachment]: " + ae);
				}
			});
			ik.openAny();
			LOG.debug("Waiting for InterfaceKit attachment...");
			ik.waitForAttachment();
			LOG.debug(ik.getDeviceName());

			Thread.sleep(500);

		}

	}

	@Override
	public void processScanRequest() throws Exception {
		super.processScanRequest();

		Double currentTemperatureF = getTemperature();
		Double currentHumidity = getHumidity();
		LOG.debug("P1125_Temp" + "=" + currentTemperatureF);
		LOG.debug("P1125_RH" + "=" + currentHumidity);
		setProperty("P1125_Temp", currentTemperatureF);
		setProperty("P1125_RH", currentHumidity);
		updateSubscribedProperties(2000);

	}

	private Double getHumidity() {
		final Double HUMIDITY_TRANLATE_1 = 0.1906;
		final Double HUMIDITY_TRANLATE_2 = 40.2;
		Double humidity = 0.00;

		if (simulated != null && simulated.equals("simulated")) {
			humidity = 20.5;
		} else {
			try {
				humidity = (ik.getSensorValue(1) * HUMIDITY_TRANLATE_1)
						- HUMIDITY_TRANLATE_2;
			} catch (PhidgetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		humidity = humidity * 100;
		Long l = Math.round(humidity);
		humidity = l.doubleValue();
		humidity = humidity / 100;
		
		return humidity;
	}

	private Double getTemperature() {
		final double TEMPERATURE_TRANSLATE_1 = 0.22222;
		final double TEMPERATURE_TRANSLATE_2 = 61.11;
		Double temperature = 0.00;

		if (simulated != null && simulated.equals("simulated")) {
			temperature = 70.5;
		} else {
			try {
				temperature = (ik.getSensorValue(1) * TEMPERATURE_TRANSLATE_1)
						- TEMPERATURE_TRANSLATE_2;
				temperature = (temperature * 9 / 5) + 32;
			} catch (PhidgetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		temperature = temperature * 100;
		Long l = Math.round(temperature);
		temperature = l.doubleValue();
		temperature = temperature / 100;
		
		return temperature;
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
