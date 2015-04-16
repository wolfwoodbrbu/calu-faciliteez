/**
 * 
 */
package com.calu.faciliteez.phidget1125thing;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Faye
 *
 */
public class DataSimulator {
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private int simulated_data;
	private int rate_change;
	private int max_value;
	private int min_value;
	private int target_value;
	private boolean target_hit;
	private final Random rnd;

	public DataSimulator(int rate_change, int max_value, int min_value,
			int target_value) {
		super();
		this.rate_change = rate_change;
		this.max_value = max_value;
		this.min_value = min_value;
		this.target_value = target_value;
		rnd = new Random();

		target_hit = true;
		simulated_data = this.target_value;
	}

	public void ChangeTarget(int target, int rate, int min, int max) {
		if (this.target_value != target || this.rate_change != rate
				|| this.min_value != min || this.max_value != max) {
			this.target_value = target;
			this.rate_change = rate;
			this.min_value = min;
			this.max_value = max;
			target_hit = false;
			LOG.info("Changing Target to {}, {}, {}, {}", target, min, max,
					rate);
		} else {
			LOG.info("Target already changed");
		}

	}

	public void ChangeTarget(int target) {
		this.ChangeTarget(target, this.rate_change, this.min_value,
				this.max_value);
	}

	public int getNewValue() {
		int delta = 0;
		if (target_hit) {
			delta = (sudorandom_rate() * direction());
		} else {
			int t = target_value - simulated_data;
			if (t < 0) {
				delta = -1 * sudorandom_rate();
			} else if (t > 0) {
				delta = sudorandom_rate();
			} else {
				target_hit = true;
				delta = 0;
			}
			if (delta > 0) {
				if ((simulated_data + delta) > target_value)
					target_hit = true;
			} else if (delta < 0) {
				if ((simulated_data + delta) < target_value)
					target_hit = true;
			}
		}

		simulated_data = simulated_data + delta;

		if (simulated_data > max_value && target_hit == true) {
			simulated_data = max_value;
		} else if (simulated_data < min_value && target_hit == true) {
			simulated_data = min_value;
		}

		return simulated_data;
	}

	private int direction() {
		int t = rnd.nextInt(2);

		if (t == 1) {
			return 1;
		} else {
			return -1;
		}

	}

	private int sudorandom_rate() {
		return rnd.nextInt(rate_change + 1);
	}

}
