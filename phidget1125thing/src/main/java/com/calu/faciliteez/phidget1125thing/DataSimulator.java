/**
 * 
 */
package com.calu.faciliteez.phidget1125thing;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A data simulator that simulates a number within a given range with a
 * sudorandom change in value every time a value is requested.
 * 
 * @author Faye Bickerton
 */
public class DataSimulator {
	/**
	 * The Main Class's Logger
	 */
	private static final Logger	LOG	= LoggerFactory.getLogger(Main.class);
	/**
	 * Holds the current data value
	 */
	private int					simulated_data;
	/**
	 * The maximum delta the simulated data can change
	 */
	private int					rate_change;
	/**
	 * The ceiling value the simulated data can be
	 */
	private int					max_value;
	/**
	 * The floor value the simulated data can be
	 */
	private int					min_value;
	/**
	 * A value that when changed the data simulator goes to before it starts to
	 * simulated the data in the min and max range
	 */
	private int					target_value;
	/**
	 * A flag that tells us if we need to go to a new target value.
	 */
	private boolean				target_hit;
	/**
	 * The Random object we use to generate random values
	 */
	private final Random		rnd;

	/**
	 * Constructor for the data simulator
	 * 
	 * Sets the initial values the data simulator starts at.
	 * 
	 * @param rate_change
	 *            The maximum delta the simulated data can change
	 * @param max_value
	 *            The maximum value the simulated data can be
	 * @param min_value
	 *            The minimum value the simulated data can be
	 * @param target_value
	 *            The starting value the simulator will be at
	 */
	public DataSimulator(int rate_change, int max_value, int min_value, int target_value) {
		super();
		// Initilize the values
		this.rate_change = rate_change;
		this.max_value = max_value;
		this.min_value = min_value;
		this.target_value = target_value;
		rnd = new Random();

		target_hit = true;
		simulated_data = this.target_value;
	}

	/**
	 * Changes the range the simulator is to simulated data in
	 * 
	 * @param target
	 *            The new target value the simulator will have to reach before
	 *            randomly simulating again
	 * @param rate
	 *            The new maximum delta the data can change at
	 * @param min
	 *            The new minimum value
	 * @param max
	 *            The new maximum value
	 */
	public void ChangeTarget(int target, int rate, int min, int max) {
		// only set a new target if the data is given is different from the
		// current values
		if (this.target_value != target || this.rate_change != rate || this.min_value != min || this.max_value != max) {
			this.target_value = target;
			this.rate_change = rate;
			this.min_value = min;
			this.max_value = max;
			target_hit = false;
			LOG.info("Changing Target to {}, {}, {}, {}", target, min, max, rate);
		}
		else {
			LOG.info("Target already changed");
		}

	}

	public void ChangeTarget(int target) {
		this.ChangeTarget(target, this.rate_change, this.min_value, this.max_value);
	}

	/**
	 * Handles generating the new simulated value
	 * 
	 * If target_hit is false this function will only generate new values that
	 * move towards the target_value.
	 * 
	 * Otherwise it generates a new value within the Min and Max values using
	 * the rate_change as a max value of change in both the positive and
	 * negative directions
	 * 
	 * @return Returns the newly generated simulated_data
	 */
	public int getNewValue() {
		// Initialize our delta
		int delta = 0;

		if (target_hit) {
			// generate a random delta in a random direction based on the
			// rate_change
			delta = (sudorandom_rate() * direction());
		}
		else {
			// If we need to go to a new target value
			// subtract the old simulated value with the target value to get
			// what direction we need to go.
			int t = target_value - simulated_data;
			if (t < 0) {
				// if t is negative we need to head down
				delta = -1 * sudorandom_rate();
			}
			else if (t > 0) {
				// else if t is positive we head up
				delta = sudorandom_rate();
			}
			else {
				// if we're lucky and t is 0 then we hit our target the last
				// time we generated a new value
				target_hit = true;
				delta = 0;
			}
			// check if we are going to pass up the target value with delta
			// added to the old simulated data
			if (delta > 0) {
				if ((simulated_data + delta) > target_value)
					target_hit = true;
			}
			else if (delta < 0) {
				if ((simulated_data + delta) < target_value)
					target_hit = true;
			}
		}

		// Add the generated delta to the old simulated data to get the new
		// simulated data value
		simulated_data = simulated_data + delta;

		// check if the new value is within the min and max range if not set the
		// new data to the exceeded value
		if (simulated_data > max_value && target_hit == true) {
			simulated_data = max_value;
		}
		else if (simulated_data < min_value && target_hit == true) {
			simulated_data = min_value;
		}

		// return the new simulated data to the caller
		return simulated_data;
	}

	/**
	 * @return Returns a 1 or -1 based on a random number generated by our rnd
	 *         variable
	 */
	private int direction() {
		int t = rnd.nextInt(2);

		if (t == 1) {
			return 1;
		}
		else {
			return -1;
		}

	}

	/**
	 * 
	 * @return Returns a random number from 0 to rate_change inclusive
	 */
	private int sudorandom_rate() {
		return rnd.nextInt(rate_change + 1);
	}

}
