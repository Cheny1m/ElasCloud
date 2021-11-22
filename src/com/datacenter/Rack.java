package com.datacenter;

/**
 * A simplified Rack class including rack id, rack time delay and pm numbers.
 * Generating this class is mainly for passing parameter for LoadBalanceFactory.
 * 
 * notice : maybe I should modify the pm numbers into an uniform class
 * @author Minxina
 * 
 */
public class Rack {

	private int r_id;

	private int r_timeDelay;
	private int r_pmNum1;
	private int r_pmNum2;
	private int r_pmNum3;

	public Rack(int r_id, int r_pmNum1, int r_pmNum2, int r_pmNum3, int r_timeDelay) {
		this.r_id = r_id;
		this.r_timeDelay = r_timeDelay;
		this.r_pmNum1 = r_pmNum1;
		this.r_pmNum2 = r_pmNum2;
		this.r_pmNum3 = r_pmNum3;
	}

	public int getR_id() {
		return r_id;
	}

	public int getR_timeDelay() {
		return r_timeDelay;
	}

	public int getR_pmNum1() {
		return r_pmNum1;
	}

	public int getR_pmNum2() {
		return r_pmNum2;
	}

	public int getR_pmNum3() {
		return r_pmNum3;
	}
}
