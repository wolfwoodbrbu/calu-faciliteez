package com.calu.faciliteez.phidget1125thing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phidgets.*;
import com.phidgets.event.*;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.types.properties.Property;
import com.thingworx.types.properties.collections.PendingPropertyUpdatesByProperty;

@ThingworxPropertyDefinitions(

properties = {
		@ThingworxPropertyDefinition(name = "P1125_Temp", description = "The current temperature of the Phidget 1125 sensor", baseType = "NUMBER", aspects = {
				"dataChangeType:ALWAYS", "dataChangeThreshold:0",
				"cacheTime:0", "isPersistent:FALSE", "isReadOnly:TRUE",
				"pushType:VALUE", "defaultValue:0" }),
		@ThingworxPropertyDefinition(name = "P1125_RH", description = "The current relative humidity of the Phidget 1125 sensor", baseType = "NUMBER", aspects = {
				"dataChangeType:ALWAYS", "dataChangeThreshold:0",
				"cacheTime:0", "isPersistent:FALSE", "isReadOnly:TRUE",
				"pushType:VALUE", "defaultValue:0" }),
		@ThingworxPropertyDefinition(name = "P1125_TempScale", description = "Which Temperature Scale do we want.", baseType = "STRING", aspects = {
				"dataChangeType:ALWAYS", "dataChangeThreshold:0",
				"cacheTime:0", "isPersistent:TRUE", "isReadOnly:FALSE",
				"pushType:VALUE", "defaultValue:F" }), })
public class SensorThing extends VirtualThing {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7413736479873474805L;
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
			setDefaultPropertyValue("P1125_TempScale");
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

		PendingPropertyUpdatesByProperty pu = getPendingPropertyUpdates();
		Property Scale = getProperty("P1125_TempScale");
		String TempScale = Scale.getValue().getStringValue();
		LOG.debug("P1125_TempScale = " + TempScale);
		Double currentTemperatureF = getTemperature(TempScale);
		Double currentHumidity = getHumidity();
		LOG.debug("P1125_Temp " + "= " + currentTemperatureF);
		LOG.debug("P1125_RH   " + "= " + currentHumidity);
		setProperty("P1125_Temp", currentTemperatureF);
		setProperty("P1125_RH", currentHumidity);
		updateSubscribedProperties(2000);

	}

	private Double getHumidity() {
		final BigDecimal HUMIDITY_TRANLATE_1 = new BigDecimal(0.1906);
		final BigDecimal HUMIDITY_TRANLATE_2 = new BigDecimal(40.2);
		int sensorValue = 0;
		BigDecimal humidity = new BigDecimal(0);

		if (simulated != null && simulated.equals("simulated")) {
			sensorValue = 319;
		} else {
			sensorValue = getSensorValue(1);
		}
		humidity = new BigDecimal(sensorValue);

		humidity = humidity.multiply(HUMIDITY_TRANLATE_1);
		humidity = humidity.subtract(HUMIDITY_TRANLATE_2);

		humidity = round(humidity, 2);

		return humidity.doubleValue();
	}

	private Double getTemperature(String Scale) {
		final BigDecimal TEMPERATURE_TRANSLATE_1 = new BigDecimal(0.22222);
		final BigDecimal TEMPERATURE_TRANSLATE_2 = new BigDecimal(61.11);
		int sensorValue = 0;
		BigDecimal temperature = new BigDecimal(0);

		if (simulated != null && simulated.equals("simulated")) {
			sensorValue = 319;
		} else {
			sensorValue = getSensorValue(0);
		}
		temperature = new BigDecimal(sensorValue);

		temperature = temperature.multiply(TEMPERATURE_TRANSLATE_1);
		temperature = temperature.subtract(TEMPERATURE_TRANSLATE_2);

		if (Scale.equals("F")) {
			temperature = temperature.multiply(new BigDecimal(9));
			temperature = temperature.divide(new BigDecimal(5));
			temperature = temperature.add(new BigDecimal(32));
		}

		temperature = round(temperature, 2);

		return temperature.doubleValue();
	}

	private int getSensorValue(int index) {
		int sensorValue = 0;

		try {
			sensorValue = ik.getSensorValue(index);
		} catch (PhidgetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sensorValue;
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

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static BigDecimal round(BigDecimal value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = value;
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd;
	}

}
