package com.comparedindex;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
/**
 * This class is used to calculate the capacity_makespan, which is calculated
 * through the maximum sum of duration multiplies CPU capacity,  of each level, like PMs, racks 
 * and datacenters 
 * @author Minxian
 *
 */
public class CalCapacityMakespan extends ComparisonIndex{
	private float capacity_makespan = 0.0f;
	
	private float rackCapacityMakespan = 0.0f;
	private float dataCenterCapacityMakespan = 0.0f;
	public static float wholeSystemCapacityMakespan = 0.0f;
	public static float CapacityMakespanSum = 0.0f;
	ArrayList<PhysicalMachine> pq1, pq2, pq3;
	
	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;
	
	public CalCapacityMakespan(CalAverageUtility ca){

		calQueueCapacityMakespan(ca.pq1);
		calQueueCapacityMakespan(ca.pq2);
		calQueueCapacityMakespan(ca.pq3);

	}

	public CalCapacityMakespan(ArrayList<DataCenter> p_arr_dc) {
		CapacityMakespanSum = 0;
		this.arr_dc = p_arr_dc;
		for (DataCenter dc : arr_dc) {
			arr_lbf = dc.getArr_lbf();
			for (LoadBalanceFactory lbf : arr_lbf) {
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();
				capacity_makespan = 0.0f;
				
				calQueueCapacityMakespan(pq1);
				calQueueCapacityMakespan(pq2);
				calQueueCapacityMakespan(pq3);
				
				calRackCapacityMakespan(capacity_makespan);
			}
			calDataCenterCapacityMakespan(rackCapacityMakespan);
		}
		wholeSystemCapacityMakespan = dataCenterCapacityMakespan;
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "CapacityMakespan: ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return wholeSystemCapacityMakespan;
	}
	
	public float getRackCapacityMakespan() {
		return rackCapacityMakespan;
	}

	public float getDataCenterCapacityMakespan() {
		return dataCenterCapacityMakespan;
	}

	public float getWholeSystemCapacityMakespan() {
		return wholeSystemCapacityMakespan;
	}

	public float getCapacityMakespanSum(){return CapacityMakespanSum;}

	private void calQueueCapacityMakespan(ArrayList<PhysicalMachine> pq1){
		for(int i = 0; i < pq1.size(); i++){
			if( pq1.get(i).getTotalCapacityMakespan() > capacity_makespan){
				capacity_makespan = pq1.get(i).getTotalCapacityMakespan();
				//capacity_makespan = pq1.get(i).getAvgUtility() * pq1.get(i).getTotalTime()* pq1.get(i).getCpuTotal();
			}
			CapacityMakespanSum += pq1.get(i).getTotalCapacityMakespan();
		}
	}
	
	private void calRackCapacityMakespan(float p_capacityMakespan){
		if(p_capacityMakespan > rackCapacityMakespan){
			rackCapacityMakespan = p_capacityMakespan;
		}
	}
	
	private void calDataCenterCapacityMakespan(float p_rack_capacityMakespan){
		if(p_rack_capacityMakespan > dataCenterCapacityMakespan){
			dataCenterCapacityMakespan = p_rack_capacityMakespan;
		}
	}
}








