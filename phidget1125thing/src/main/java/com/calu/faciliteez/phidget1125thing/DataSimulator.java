/**
 * 
 */
package com.calu.faciliteez.phidget1125thing;

import java.util.Random;

/**
 * @author Faye
 *
 */
public class DataSimulator {
	private int simulated_data;
	private int rate_change;
	private int max_value;
	private int min_value;
	private int target_value;
	private boolean target_hit;

	public DataSimulator(int rate_change, int max_value, int min_value, int target_value) {
		super();
		this.rate_change = rate_change;
		this.max_value = max_value;
		this.min_value = min_value;
		this.target_value = target_value;

		target_hit = true;
		simulated_data = this.target_value;
	}

	public void ChangeTarget(int target, int rate, int min, int max) {
		this.target_value = target;
		this.rate_change = rate;
		this.min_value = min;
		this.max_value = max;
		target_hit = false;
	}

	public void ChangeTarget(int target) {
		this.ChangeTarget(target, this.rate_change, this.min_value,
				this.max_value);
	}

	public int getNewValue() {
		int delta = 0;
		if (target_hit) {
			delta = (rate_change * direction());
		} else {
			int t = target_value - simulated_data;
			if (t > 0) {
				delta = -1 * rate_change;
			} else if (t < 0) {
				delta = rate_change;
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

		if (simulated_data > max_value) {
			simulated_data = max_value;
		} else if (simulated_data < min_value) {
			simulated_data = min_value;
		}

		return simulated_data;
	}

	private int direction() {
		final Random rnd = new Random();
		int t = rnd.nextInt();
		int rt = 0;
		if ((t % 3) == 0) {
			rt = 0;
		} else if ((t % 3) == 1) {
			rt = 1;
		} else if ((t % 3) == 2) {
			rt = -1;
		}
		return rt;
	}

}
