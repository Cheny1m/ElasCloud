package com.datacenter;

import java.util.ArrayList;

/**
 * 
 * @author Minxian The level of datacenter that operating rack level. A
 *         datacenter may consist of more than one racks. A datacenter would
 *         have a unique id and the time delay of datacenters may vary.
 */
public class DataCenter implements Comparable<DataCenter> {

	//数据中心id与时延;
	private int d_id;
	private int d_timeDelay;

	LoadBalanceFactory lbf;

	//一个数据中心由多个机架组成
	//资源list
	ArrayList<LoadBalanceFactory> arr_lbf = new ArrayList<LoadBalanceFactory>();
	//机架list
	ArrayList<Rack> arr_rack = new ArrayList<Rack>();

	;
	//构造方法
	public DataCenter(int d_id, int d_timeDelay) {
		// TODO Auto-generated constructor stub
		this.d_id = d_id;
		this.d_timeDelay = d_timeDelay;

		initialRack();
	}

	/**
	 * Initialise the racks information in a datacenter, for further
	 * implementation, the information would be gathered from user interface.
	 */

	public void initialRack() {
		Rack rack1 = new Rack(0, 50, 0, 0, 0);
		arr_rack.add(rack1);
	}

	public void initalAllResourse() {
		for (Rack rack : arr_rack) {
			lbf = new LoadBalanceFactory(rack);
			arr_lbf.add(lbf);
		}
	}

	public float getDataCenterLoad() {
		float dataCenterAvgUtilization = 0.0f;
		float tempDataCenterAvgUtilization;
		float effectiveRack = 0;
		for (LoadBalanceFactory lbf : arr_lbf) {
			tempDataCenterAvgUtilization = lbf.getRackLoad();
			if (tempDataCenterAvgUtilization != 0) {
				dataCenterAvgUtilization += tempDataCenterAvgUtilization;
				effectiveRack++;
			}
		}
		if (effectiveRack == 0) {
			return 0.0f;
		} else {
			return dataCenterAvgUtilization /= effectiveRack;
		}
	}

	public float getDataCenterCapacityMakespan() {
		float dataCenterCapacityMakespan = 0.0f;
		float tempDataCenterCapacityMakespan;
		for (LoadBalanceFactory lbf : arr_lbf) {
			tempDataCenterCapacityMakespan = lbf.getRackCapacityMakespan();
			if (tempDataCenterCapacityMakespan > dataCenterCapacityMakespan) {
				dataCenterCapacityMakespan = tempDataCenterCapacityMakespan;
			}
		}
		return dataCenterCapacityMakespan;
	}

	public int getD_id() {
		return d_id;
	}

	public int getD_timeDelay() {
		return d_timeDelay;
	}

	public ArrayList<Rack> getArr_rack() {
		return arr_rack;
	}

	public void setArr_rack(ArrayList<Rack> arr_rack) {
		this.arr_rack = arr_rack;
	}

	public ArrayList<LoadBalanceFactory> getArr_lbf() {
		return arr_lbf;
	}

	@Override
	public int compareTo(DataCenter o) {
		if (this.getDataCenterLoad() > o.getDataCenterLoad())
			return 1;
		return 0;
	}

}
