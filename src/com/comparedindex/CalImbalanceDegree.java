package com.comparedindex;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;

/**
 * This class is used to calculate imbalance degree of each level, like PMs,
 * racks, data centers and whole system.
 * 
 * @author Minxian
 */
public class CalImbalanceDegree extends ComparisonIndex {
	private float averageUtility;
	private float effectivePM = 0;;
	private float pmImbalanceDegree = 0.0f;
	private int totalPmQueueSize = 0;

	private float rackImbalaceDegree = 0.0f;
	private float dataCenterImbalanceDegree = 0.0f;
	public static float wholeSystemImbalanceDegree = 0.0f;

	ArrayList<PhysicalMachine> pq1;
	ArrayList<PhysicalMachine> pq2;
	ArrayList<PhysicalMachine> pq3;

	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;

	public CalImbalanceDegree(CalAverageUtility ca) {
		averageUtility = ca.getIndexValue();
		effectivePM = ca.getEffectivePM();

		calQueueImbalance(ca.pq1);
		calQueueImbalance(ca.pq2);
		calQueueImbalance(ca.pq3);
		if (effectivePM != 0)
			// imbalanceDegree /= effectivePM;
			pmImbalanceDegree /= (ca.pq1.size() + ca.pq2.size() + ca.pq3.size());
	}

	/**
	 * new created construct method to enable multi-layer imbalance degree
	 * calculation
	 * 
	 * @param p_arr_dc
	 */
	public CalImbalanceDegree(ArrayList<DataCenter> p_arr_dc) {
		this.arr_dc = p_arr_dc;
		for (DataCenter dc : arr_dc) {
			dataCenterImbalanceDegree = 0;
			arr_lbf = dc.getArr_lbf();
			for (LoadBalanceFactory lbf : arr_lbf) {
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();
				averageUtility = 0.0f;
				pmImbalanceDegree = 0.0f;

				calQueueUtility(pq1);
				calQueueUtility(pq2);
				calQueueUtility(pq3);
				//平均利用率
				averageUtility = calUtilityDividedByNumbers(averageUtility, effectivePM);

				calQueueImbalance(pq1);
				calQueueImbalance(pq2);
				calQueueImbalance(pq3);

				totalPmQueueSize = pq1.size() + pq2.size() + pq3.size();
				//rackImbalaceDegree = calImbalanceDegreeDividedByNumbers(rackImbalaceDegree, totalPmQueueSize);
				rackImbalaceDegree = calImbalanceDegreeDividedByNumbers(rackImbalaceDegree, (int)effectivePM);

				dataCenterImbalanceDegree += rackImbalaceDegree;
			}
			dataCenterImbalanceDegree = calImbalanceDegreeDividedByNumbers(dataCenterImbalanceDegree, arr_lbf.size());
			wholeSystemImbalanceDegree += dataCenterImbalanceDegree;
		}
		wholeSystemImbalanceDegree = calImbalanceDegreeDividedByNumbers(wholeSystemImbalanceDegree, arr_dc.size());
		//wholeSystemImbalanceDegree = wholeSystemImbalanceDegree * arr_dc.size() * arr_lbf.size() * effectivePM;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Imbalance Degree: ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return wholeSystemImbalanceDegree;
	}

	public float calUtilityDividedByNumbers(float utility, float numbers) {
		if (numbers == 0)
			return 0.0f;
		else
			return utility / numbers;
	}

	private float calImbalanceDegreeDividedByNumbers(float imbalnceDegree, int numbers) {
		if (numbers == 0)
			return 0.0f;
		else
			return imbalnceDegree / numbers;
	}

	private void calQueueImbalance(ArrayList<PhysicalMachine> pq1) {
		for (int i = 0; i < pq1.size(); i++) {
			if(pq1.get(i).getAvgUtility() != 0){
			pmImbalanceDegree += Math.pow(pq1.get(i).getAvgUtility() - averageUtility, 2);
			rackImbalaceDegree = pmImbalanceDegree;
			}
		}

	}

	private void calQueueUtility(ArrayList<PhysicalMachine> pq1) {
		for (PhysicalMachine pm : pq1) {
			if (pm.getAvgUtility() != 0) {
				effectivePM++;
				averageUtility += pm.getAvgUtility();
			}
		}
	}
}
