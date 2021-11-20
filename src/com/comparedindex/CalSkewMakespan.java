package com.comparedindex;

import java.util.ArrayList;

import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
/**
 * This class is used to calculate skew of makespan.
 * Calculated by the minimum average utility over maximum average utility. 
 * @author Minxian
 *
 */
public class CalSkewMakespan extends ComparisonIndex{
	
	private float makespan = 0.0f;
	private float lowerBound = 1.0f;
	private float skew_makespan = 0.0f;
	public CalSkewMakespan(CalAverageUtility ca){
		
		calQueueSkewMakespan(ca.pq1);
		calQueueSkewMakespan(ca.pq2);
		calQueueSkewMakespan(ca.pq3);
		if(makespan != 0.0f)
		skew_makespan = lowerBound / makespan;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Skew(Makespan): ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return skew_makespan;
	}
	
	private void calQueueSkewMakespan(ArrayList<PhysicalMachine> pq1){
		for(int i = 0; i < pq1.size(); i++){
			if(pq1.get(i).getAvgUtility() > makespan){
				makespan = pq1.get(i).getAvgUtility();
			}
			if(pq1.get(i).getAvgUtility() < lowerBound
					&& pq1.get(i).getAvgUtility() != 0){
				lowerBound = pq1.get(i).getAvgUtility();
			}
		}

	}
}


