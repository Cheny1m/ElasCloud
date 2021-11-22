/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comparedindex;

import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
import java.util.ArrayList;

/**
 *
 * @author LukeXu
 */
public class CalEnergyConsumption extends ComparisonIndex{
		private float totalEnergyConsumption = 0.0f;
		ArrayList<PhysicalMachine> pq1;
		ArrayList<PhysicalMachine> pq2;
		ArrayList<PhysicalMachine> pq3;
		public CalEnergyConsumption(ArrayList<PhysicalMachine> pq1, 
						  ArrayList<PhysicalMachine> pq2, 
						  ArrayList<PhysicalMachine> pq3){
			this.pq1 = pq1;
			this.pq2 = pq2;
			this.pq3 = pq3;
			calQueueEnergyConsumption(pq1);
			calQueueEnergyConsumption(pq2);
			calQueueEnergyConsumption(pq3);
			
		}

		@Override
		public float getIndexValue() {
			// TODO Auto-generated method stub
			return totalEnergyConsumption;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return "Total Energy Consumption: ";
		}
		
		/**
		 * Refactored method calculating averageCPUUtility, averageMemUtility,
		 * averageMemUtility for each queue.
		 * @param pq1
		 */
		private void calQueueEnergyConsumption(ArrayList<PhysicalMachine> pq1){
			for(int i = 0; i < pq1.size(); i++){
                            for(int j =0; j < pq1.get(i).vms.size(); j++){
                                totalEnergyConsumption += 
                                        (pq1.get(i).getMaxPower() - pq1.get(i).getMinPower())
                                        * (pq1.get(i).vms.get(j).getEndTime() - pq1.get(i).vms.get(j).getStartTime())
                                        * (pq1.get(i).vms.get(j).getCpuTotal())/ pq1.get(i).getCpuTotal();
				}
			}
		}
}
