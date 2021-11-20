package com.comparedindex;

import java.util.ArrayList;

import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
/**
 * This class is used to calculate the skew of capacity_makespan, which is
 * calculated by minimum sum of capacity_makepan (0 is exclusive) over maximum
 * sum of capacity_makepan in all PMs (Datacenter).  
 * @author Minxian
 *
 */
public class CalSkewCapacityMakespan extends ComparisonIndex{
	private float capacity_makespan = 0.0f;
	private float lowerBound = Float.MAX_VALUE;
	private float skew_capacity_makespan = 0.0f;
	public CalSkewCapacityMakespan(CalAverageUtility ca){
		
		calQueueSkewMakespan(ca.pq1);
		calQueueSkewMakespan(ca.pq2);
		calQueueSkewMakespan(ca.pq3);
		if(capacity_makespan != 0.0f)
		skew_capacity_makespan = lowerBound / capacity_makespan;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Skew(CapacityMakespan): ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return skew_capacity_makespan;
	}
	
	private void calQueueSkewMakespan(ArrayList<PhysicalMachine> pq1){
		for(int i = 0; i < pq1.size(); i++){
			if(pq1.get(i).getTotalCapacityMakespan() > capacity_makespan){
				capacity_makespan = pq1.get(i).getTotalCapacityMakespan();
			}
			if(pq1.get(i).getTotalCapacityMakespan() < lowerBound
					&& pq1.get(i).getTotalCapacityMakespan() != 0){
				lowerBound = pq1.get(i).getTotalCapacityMakespan();
			}
		}

	}
}
