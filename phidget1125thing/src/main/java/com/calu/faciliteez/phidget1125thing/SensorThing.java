package com.calu.faciliteez.phidget1125thing;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.PropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.collections.AspectCollection;
import com.thingworx.types.constants.Aspects;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.constants.DataChangeType;
import com.thingworx.types.primitives.BooleanPrimitive;
import com.thingworx.types.primitives.IntegerPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.types.primitives.StringPrimitive;
import com.thingworx.types.properties.Property;

/**
 * This is where all the data is gathered or simulated. It is a virtual
 * representation of a Thing in ThingWorx
 * 
 * @author Faye Bickerton
 */
public class SensorThing extends VirtualThing {

	private static final long			serialVersionUID		= 7413736479873474805L;
	/**
	 * Main Class's Logger
	 */
	private static final Logger			LOG						= LoggerFactory.getLogger(Main.class);

	/**
	 * The name of this thing
	 */
	private final String				name;
	/**
	 * The description of this thing
	 */
	private final String				description;
	/**
	 * A string that contains the 4th argument of the program
	 */
	private final String				simulated;
	/**
	 * The Micro Edge Client
	 */
	private final ConnectedThingClient	client;
	/**
	 * The Phidget InterfaceKit this thing will use
	 */
	private InterfaceKitPhidget			ik						= null;
	/**
	 * The index of where the Temperature Sensor is plugged into the
	 * InterfaceKit
	 */
	private final int					TEMP_INDEX				= 0;
	/**
	 * The index of where the Humidity Sensor is plugged into the InterfaceKit
	 */
	private final int					RH_INDEX				= 1;

	/**
	 * A DataSimulator for our simulated Temperature
	 */
	private DataSimulator				temperatureSimData		= null;
	/**
	 * A DataSimulator for our simulated Humidity
	 */
	private DataSimulator				humiditySimData			= null;

	/**
	 * A constant we have to multiply the temperature sensor value by to convert
	 * it to celsius temperature scale
	 */
	final BigDecimal					TEMPERATURE_TRANSLATE_1	= new BigDecimal(0.22222);
	/**
	 * A constant we have to subtract from the converted value to shift it to
	 * the real temperature in celsuis
	 */
	final BigDecimal					TEMPERATURE_TRANSLATE_2	= new BigDecimal(61.11);
	/**
	 * A constant we have to multiply the humidity sensor value by to convert it
	 * to a percentage
	 */
	final BigDecimal					HUMIDITY_TRANLATE_1		= new BigDecimal(0.1906);
	/**
	 * A constant we have to subtract from the converted value to shift it to
	 * the real percentage
	 */
	final BigDecimal					HUMIDITY_TRANLATE_2		= new BigDecimal(40.2);

	/**
	 * Constructor for SensorThing. All four parameters are brought in incase
	 * they are needed.
	 * 
	 * @param name
	 *            The name of the this virtual thing
	 * @param description
	 *            A description of this virtual thing
	 * @param simulated
	 *            Whether Data is being Simulated or not
	 * @param client
	 *            The client connection to ThingWorx
	 */
	public SensorThing(String name, String description, String simulated, ConnectedThingClient client) {
		// Initialize member variables
		super(name, description, client);
		this.name = name;
		this.description = description;
		this.simulated = simulated;
		this.client = client;

		// Create new property definitions for both the Temprature and Humidity
		PropertyDefinition P1125_RH = new PropertyDefinition("P1125_RH", "The current relative humidity of the Phidget 1125 sensor", BaseTypes.NUMBER);
		PropertyDefinition P1125_Temp = new PropertyDefinition("P1125_Temp", "The current temperature of the Phidget 1125 sensor", BaseTypes.NUMBER);

		// This holds the aspects we will apply to our Temperature and Humidity
		// properties
		AspectCollection aspects = new AspectCollection();

		// Add our aspects to the aspects variable
		aspects.put(Aspects.ASPECT_DATACHANGETYPE, new StringPrimitive(DataChangeType.ALWAYS.name()));
		aspects.put(Aspects.ASPECT_DATACHANGETHRESHOLD, new NumberPrimitive(0.0));
		aspects.put(Aspects.ASPECT_CACHETIME, new IntegerPrimitive(-1));
		aspects.put(Aspects.ASPECT_ISPERSISTENT, new BooleanPrimitive(false));
		aspects.put(Aspects.ASPECT_ISREADONLY, new BooleanPrimitive(false));
		aspects.put("pushType", new StringPrimitive(DataChangeType.ALWAYS.name()));
		aspects.put(Aspects.ASPECT_DEFAULTVALUE, new NumberPrimitive(0.0));

		LOG.debug(aspects.toString());

		// Add the aspects to the Temp and Humidity properties
		P1125_RH.setAspects(aspects);
		P1125_Temp.setAspects(aspects);

		// Add the Temperature and Humidity properties to this Thing
		this.defineProperty(P1125_RH);
		this.defineProperty(P1125_Temp);

		// Adds any other Thing properties/services that are defined by
		// annotations
		initializeFromAnnotations();

		// If we are simulating data initialize our DataSimulators
		if (simulated != null && simulated.equals("simulated")) {
			LOG.info("{}: Simulating data!", name);
			temperatureSimData = new DataSimulator(1, 380, 365, 375);
			humiditySimData = new DataSimulator(2, 369, 315, 342);

		}
		else {
			// Else connect to our InterfaceKit
			try {
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

				ik.setSensorChangeTrigger(TEMP_INDEX, 2);
				ik.setSensorChangeTrigger(RH_INDEX, 2);
			}
			catch (PhidgetException | InterruptedException e) {
				LOG.error("Something went wrong connecting to the InterfaceKit", e);
			}

		}

	}

	/**
	 * This allows ThingWorx to change what temperature values are being
	 * simulated for this thing. This remote service is defined in the
	 * ThingShape in ThingWorx so all of the Things defined by that shape can
	 * access this service.
	 * 
	 * @param target
	 *            Target Value you want the Data Simulator(DS) to go to. Default
	 *            375 (72F)
	 * @param max
	 *            Max Value you want the DS to go to. Default 380 (74F)
	 * @param min
	 *            Min Value you want the DS to go to. Default 365 (68F)
	 * @param rate
	 *            The max rate in which the DS can change the Temp. Default 1
	 * @throws Exception
	 */
	@ThingworxServiceDefinition(name = "SetSimulationTempRange", description = "Sets the Temperature Range the Data Simulator Simulates at.")
	@ThingworxServiceResult(name = CommonPropertyNames.PROP_RESULT, description = "", baseType = "NOTHING")
	public void SetSimulationTempRange(@ThingworxServiceParameter(name = "Target", description = "Target Value you want the Data Simulator(DS) to go to. Default 375 (72F)", baseType = "INTEGER")
	Integer target, @ThingworxServiceParameter(name = "Max", description = "Max Value you want the DS to go to. Default 380 (74F)", baseType = "INTEGER")
	Integer max, @ThingworxServiceParameter(name = "Min", description = "Min Value you want the DS to go to. Default 365 (68F)", baseType = "INTEGER")
	Integer min, @ThingworxServiceParameter(name = "Rate", description = "The max rate in which the DS can change the Temp. Default 1", baseType = "INTEGER")
	Integer rate) throws Exception {
		if (temperatureSimData != null) {
			temperatureSimData.ChangeTarget(target, rate, min, max);
		}
		else {
			LOG.info("{}: Data Simulator not being used or not ready", name);
		}
	}

	/**
	 * This allows ThingWorx to change what Humidity values are being simulated
	 * for this thing. This remote service is defined in the ThingShape in
	 * ThingWorx so all of the Things defined by that shape can access this
	 * service. (2, 369, 315, 342);
	 * 
	 * @param target
	 *            Target Value you want the Data Simulator(DS) to go to. Default
	 *            342 (24.99%)
	 * @param max
	 *            Max Value you want the DS to go to. Default 369 (30.13%)
	 * @param min
	 *            Min Value you want the DS to go to. Default 315 (19.84%)
	 * @param rate
	 *            The max rate in which the DS can change the Temp. Default 2
	 * @throws Exception
	 */
	@ThingworxServiceDefinition(name = "SetSimulationHumidityRange", description = "Sets the Humidity Range the Data Simulator Simulates at.")
	@ThingworxServiceResult(name = CommonPropertyNames.PROP_RESULT, description = "", baseType = "NOTHING")
	public void SetSimulationHumidityRange(
		@ThingworxServiceParameter(name = "Target", description = "Target Value you want the Data Simulator(DS) to go to. Default 342 (24.99%)", baseType = "INTEGER")
		Integer target, @ThingworxServiceParameter(name = "Max", description = "Max Value you want the DS to go to. Default 369 (30.13%)", baseType = "INTEGER")
		Integer max, @ThingworxServiceParameter(name = "Min", description = "Min Value you want the DS to go to. Default 315 (19.84%)", baseType = "INTEGER")
		Integer min, @ThingworxServiceParameter(name = "Rate", description = "The max rate in which the DS can change the Temp. Default 2", baseType = "INTEGER")
		Integer rate) throws Exception {
		if (humiditySimData != null) {
			humiditySimData.ChangeTarget(target, rate, min, max);
		}
		else {
			LOG.info("{}: Data Simulator not being used or not ready", name);
		}
	}

	/**
	 * This is the function that is run everytime the base edge server loops. It
	 * retrieves the sensor data and sends it to ThingWorx
	 */
	@Override
	public void processScanRequest() {
		try {
			super.processScanRequest();
		}
		catch (Exception e) {
			LOG.error("Error in super.processScanRequest", e);
		}

		// Get the new values for the Temperature and humidity
		Double currentTemperatureF = getTemperature("F");
		Double currentHumidity = getHumidity();

		LOG.info("{}: P1125_Temp = {}", name, currentTemperatureF);
		LOG.info("{}: P1125_RH   = {}", name, currentHumidity);
		try {
			// Try to set the new property values and send them to the ThingWorx
			// server
			setProperty("P1125_Temp", currentTemperatureF);
			setProperty("P1125_RH", currentHumidity);
			updateSubscribedProperties(2000);
		}
		catch (Exception e) {
			LOG.error("Something went wrong with setting Properties", e);
		}

	}

	/**
	 * Gets the humidity data from either the live sensor or the Data Simulator
	 * and translates that into a human readable value
	 * 
	 * @return The translated humidity value
	 */
	private Double getHumidity() {
		int sensorValue = 0;
		// We are using BigDecimal for more accurate calculations.
		BigDecimal humidity = null;

		if (simulated != null && simulated.equals("simulated")) {
			// If we're simulating data get a new value from the data simulator.
			sensorValue = humiditySimData.getNewValue();
		}
		else {
			// else get the new value from the Humidity Sensor
			sensorValue = getSensorValue(RH_INDEX);
		}
		LOG.debug("RH Raw Value: {}", sensorValue);
		// convert the sensor value into a BigDecimal
		humidity = new BigDecimal(sensorValue);
		
		// do our calculation on the data
		humidity = humidity.multiply(HUMIDITY_TRANLATE_1);
		humidity = humidity.subtract(HUMIDITY_TRANLATE_2);
		
		// round the value to 2 decimal places
		humidity = round(humidity, 2);
		
		// return this value as a double
		return humidity.doubleValue();
	}

	/**
	 * Gets the temperature data from either the live sensor or the Data
	 * Simulator and translates that into a human readable value
	 * 
	 * @param Scale
	 *            If "F" this will return a value in Fahrenheit
	 * @return The translated temperature value
	 */
	private Double getTemperature(String Scale) {
		int sensorValue = 0;
		// everything is like getHumidity except for if Scale is "F" 
		// we do additional calculations to the value to convert
		// the celsius value to ferinheight
		BigDecimal temperature = null;

		if (simulated != null && simulated.equals("simulated")) {
			sensorValue = temperatureSimData.getNewValue();
		}
		else {
			sensorValue = getSensorValue(TEMP_INDEX);
		}
		LOG.debug("Temp Raw Value: {}", sensorValue);
		temperature = new BigDecimal(sensorValue);

		temperature = temperature.multiply(TEMPERATURE_TRANSLATE_1);
		temperature = temperature.subtract(TEMPERATURE_TRANSLATE_2);

		// convert to F
		if (Scale.equals("F")) {
			temperature = temperature.multiply(new BigDecimal(9));
			temperature = temperature.divide(new BigDecimal(5));
			temperature = temperature.add(new BigDecimal(32));
		}

		temperature = round(temperature, 2);

		return temperature.doubleValue();
	}

	/**
	 * Asks the Interface Kit for the sensor value at the given Index
	 * 
	 * @param index The index at which the sensor is aton the InterfaceKit
	 * @return The value from the sensor
	 */
	private int getSensorValue(int index) {
		int sensorValue = 0;

		try {
			sensorValue = ik.getSensorValue(index);
		}
		catch (PhidgetException e) {
			LOG.error("Error retreviving Sensor data from Index: " + index, e);
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
	@Deprecated
	protected void setDefaultPropertyValue(String propertyName) throws Exception {
		Property p = getProperty(propertyName);
		PropertyDefinition d = p.getPropertyDefinition();
		Object obj = d.getDefaultValue().getValue();

		setProperty(propertyName, obj);
	}

	/**
	 * Rounds the given BigDecimal Value to the given Places Throws an exception
	 * if the places is set to anything less then 0
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static BigDecimal round(BigDecimal value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = value;
		bd = bd.setScale(places, RoundingMode.HALF_EVEN);
		return bd;
	}

}
