/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comparedindex;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;

/**
 * This class is used to calculate the number of PMs that are turned on.
 * 
 * @author LukeXu
 */
public class CalEffectivePM extends ComparisonIndex {
	public static float effectivePM;
	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;
	ArrayList<PhysicalMachine> pq1, pq2, pq3;

	public CalEffectivePM(CalAverageUtility ca) {
		effectivePM = ca.getEffectivePM();
	}

	public CalEffectivePM(ArrayList<DataCenter> p_arr_dc) {
		this.arr_dc = p_arr_dc;
		effectivePM = 0.0f;
		for (DataCenter dc : arr_dc) {
			arr_lbf = dc.getArr_lbf();
			for (LoadBalanceFactory lbf : arr_lbf) {
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();

				
				calEffectivePM(pq1);
				calEffectivePM(pq2);
				calEffectivePM(pq3);
			}
		}
	}

	public float getEffectivePM(){return effectivePM;}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Effective PM: ";
	}

	public void calEffectivePM(ArrayList<PhysicalMachine> pmQueue){
    	for(PhysicalMachine pm : pmQueue){
    		if(pm.getAvgUtility()> 0){
    			effectivePM++;
    		}
    	}
    }

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return effectivePM;
	}
}
