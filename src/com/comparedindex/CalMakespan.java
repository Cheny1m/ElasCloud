package com.comparedindex;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
/**
 * This class is used to calculate each level(PMs, racks, datacenters and whole system) of the highest 
 * average utility, namely makespan.
 * 
 * @author Minxian
 */
public class CalMakespan extends ComparisonIndex{
	private float makespan = 0.0f;
	public static float makespanSum = 0.0f;
	
	private float rackMakespan = 0.0f;
	private float dataCenterMakespan = 0.0f;
	public static float wholeSystemMakespan = 0.0f;
	
	ArrayList<PhysicalMachine> pq1, pq2, pq3;
	
	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;
	public CalMakespan(CalAverageUtility ca){
		calQueueMakespan(ca.pq1);
		calQueueMakespan(ca.pq2);
		calQueueMakespan(ca.pq3);
	}

	public CalMakespan(ArrayList<DataCenter> p_arr_dc) {
		this.arr_dc = p_arr_dc;
		for (DataCenter dc : arr_dc) {
			arr_lbf = dc.getArr_lbf();
			for (LoadBalanceFactory lbf : arr_lbf) {
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();
				makespan = 0.0f;
				
				calQueueMakespan(pq1);
				calQueueMakespan(pq2);
				calQueueMakespan(pq3);
				
				calRackMakespan(makespan);
			}
			calDataCenterMakespan(rackMakespan);
		}
		wholeSystemMakespan = dataCenterMakespan;
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Makespan: ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return wholeSystemMakespan;
	}
	
	public float getRackMakespan() {
		return rackMakespan;
	}

	public float getDataCenterMakespan() {
		return dataCenterMakespan;
	}

	public float getWholeSystemMakespan() {
		return wholeSystemMakespan;
	}

	private void calQueueMakespan(ArrayList<PhysicalMachine> pq1){
		for(int i = 0; i < pq1.size(); i++){
//		if(pq1.get(i).getAvgUtility() > makespan){
//			makespan = pq1.get(i).getAvgUtility();
//		}

//		if(pq1.get(i).getTotalMakespan() > makespan){
//			makespan = pq1.get(i).getTotalMakespan();
//		}

			if(pq1.get(i).getEffectiveSlot() > makespan){
				makespan = pq1.get(i).getEffectiveSlot();
			}
		makespanSum += pq1.get(i).getEffectiveSlot();


//			if(pq1.get(i).getAvgUtility() * pq1.get(i).getTotalTime() > makespan){
//				//makespan = pq1.get(i).getTotalMakespan();
//				makespan = pq1.get(i).getAvgUtility() * pq1.get(i).getTotalTime();
//			}
		}
	}
	
	private void calRackMakespan(float p_makespan){
		if(p_makespan > rackMakespan){
			rackMakespan = p_makespan;
		}
	}
	
	private void calDataCenterMakespan(float p_rack_makespan){
		if(p_rack_makespan > dataCenterMakespan){
			dataCenterMakespan = p_rack_makespan;
		}
	}
}
