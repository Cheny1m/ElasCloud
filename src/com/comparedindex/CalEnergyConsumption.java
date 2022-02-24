package com.comparedindex;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;

import java.util.ArrayList;

public class CalEnergyConsumption extends ComparisonIndex {
	private float EnergyConsumption = 0.0f;

	private float addEnergyConsumption = 0.0f;

	private float rackEnergyConsumption = 0.0f;
	private float dataCenterEnergyConsumption = 0.0f;
	public static float wholeSystemEnergyConsumption = 0.0f;

	ArrayList<PhysicalMachine> pq1, pq2, pq3;

	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;
	public CalEnergyConsumption(CalAverageUtility ca){
		calQueueEnergyConsumption(ca.pq1);
		calQueueEnergyConsumption(ca.pq2);
		calQueueEnergyConsumption(ca.pq3);
	}

	public CalEnergyConsumption(ArrayList<DataCenter> p_arr_dc) {
		this.arr_dc = p_arr_dc;
		for (DataCenter dc : arr_dc) {
			arr_lbf = dc.getArr_lbf();
			for (LoadBalanceFactory lbf : arr_lbf) {
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();
				EnergyConsumption = 0.0f;

				calQueueEnergyConsumption(pq1);
				calQueueEnergyConsumption(pq2);
				calQueueEnergyConsumption(pq3);

				rackEnergyConsumption += EnergyConsumption;
			}
			dataCenterEnergyConsumption += rackEnergyConsumption;
			rackEnergyConsumption = 0;
		}
		wholeSystemEnergyConsumption = dataCenterEnergyConsumption;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Total Energy Consumption: ";
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return wholeSystemEnergyConsumption;
	}

	private void calQueueEnergyConsumption(ArrayList<PhysicalMachine> pq1){
			for(int i = 0; i < pq1.size(); i++){
                            for(int j =0; j < pq1.get(i).vms.size(); j++){
								EnergyConsumption +=
                                        (pq1.get(i).getMaxPower() - pq1.get(i).getMinPower())
                                        * (pq1.get(i).vms.get(j).getEndTime() - pq1.get(i).vms.get(j).getStartTime())
                                        * (pq1.get(i).vms.get(j).getCpuTotal())/ pq1.get(i).getCpuTotal();
				}
			}
		}

//	private float calAddTurnonTime(float p_TurnonTime){
//		return EnergyConsumption += p_TurnonTime;
//	}

	public float getWholeSystemEnergyConsumption(){
		return wholeSystemEnergyConsumption;
	}

}


///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.comparedindex;
//
//import com.iterator.ComparisonIndex;
//import com.resource.PhysicalMachine;
//import java.util.ArrayList;
//
///**
// *
// * @author LukeXu
// */
//public class CalEnergyConsumption extends ComparisonIndex{
//		private float totalEnergyConsumption = 0.0f;
//		ArrayList<PhysicalMachine> pq1;
//		ArrayList<PhysicalMachine> pq2;
//		ArrayList<PhysicalMachine> pq3;
//		public CalEnergyConsumption(ArrayList<PhysicalMachine> pq1,
//						  ArrayList<PhysicalMachine> pq2,
//						  ArrayList<PhysicalMachine> pq3){
//			this.pq1 = pq1;
//			this.pq2 = pq2;
//			this.pq3 = pq3;
//			calQueueEnergyConsumption(pq1);
//			calQueueEnergyConsumption(pq2);
//			calQueueEnergyConsumption(pq3);
//
//		}
//
//		@Override
//		public float getIndexValue() {
//			// TODO Auto-generated method stub
//			return totalEnergyConsumption;
//		}
//
//		@Override
//		public String getDescription() {
//			// TODO Auto-generated method stub
//			return "Total Energy Consumption: ";
//		}
//
//		/**
//		 * Refactored method calculating averageCPUUtility, averageMemUtility,
//		 * averageMemUtility for each queue.
//		 * @param pq1
//		 */
//		private void calQueueEnergyConsumption(ArrayList<PhysicalMachine> pq1){
//			for(int i = 0; i < pq1.size(); i++){
//                            for(int j =0; j < pq1.get(i).vms.size(); j++){
//                                totalEnergyConsumption +=
//                                        (pq1.get(i).getMaxPower() - pq1.get(i).getMinPower())
//                                        * (pq1.get(i).vms.get(j).getEndTime() - pq1.get(i).vms.get(j).getStartTime())
//                                        * (pq1.get(i).vms.get(j).getCpuTotal())/ pq1.get(i).getCpuTotal();
//				}
//			}
//		}
//}
