package com.comparedindex;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;

/**
 * This class is used to calculate average utility of each level, like PMs, racks, datacenters.
 * 
 * @author Minxian
 * 
 */
public class CalAverageUtility extends ComparisonIndex {
	private float effectivePM = 0.0f;
	private float averageUtility = 0.0f;
	private float rackAverageUtility = 0.0f;
	private float dataCenterAverageUtility = 0.0f;
	private float wholeSystemAverageUtility = 0.0f;
	
	ArrayList<PhysicalMachine> pq1;
	ArrayList<PhysicalMachine> pq2;
	ArrayList<PhysicalMachine> pq3;
	
	ArrayList<DataCenter> arr_dc;
	ArrayList<LoadBalanceFactory> arr_lbf;
	
	int pmQueueNo = 0;
	private int totalPmQueueSize = 0;

	public CalAverageUtility(ArrayList<DataCenter> p_arr_dc) {
		this.arr_dc = p_arr_dc;
		for(DataCenter dc : arr_dc){
			DataCenterFactory.print.println("###DataCenter" + dc.getD_id() + "###");
			arr_lbf = dc.getArr_lbf();
			for(LoadBalanceFactory lbf : arr_lbf){
				this.pq1 = lbf.getPmQueueOne();
				this.pq2 = lbf.getPmQueueTwo();
				this.pq3 = lbf.getPmQueueThree();
				DataCenterFactory.print.println("###Rack" + lbf.getLbf_ID() + "###");
				rackAverageUtility = 0.0f;
				
				calQueueUtility(pq1);
				calQueueUtility(pq2);
				calQueueUtility(pq3);
				
				totalPmQueueSize = pq1.size() + pq2.size() + pq3.size();
				
				rackAverageUtility = calUtilityDividedByNumbers(rackAverageUtility, totalPmQueueSize);
				dataCenterAverageUtility += rackAverageUtility;
			}
			dataCenterAverageUtility = calUtilityDividedByNumbers(dataCenterAverageUtility, arr_lbf.size());
                        DataCenterFactory.print.println("dataCenterAverageUtility" + dataCenterAverageUtility);
			wholeSystemAverageUtility += dataCenterAverageUtility;
		}
		wholeSystemAverageUtility = calUtilityDividedByNumbers(wholeSystemAverageUtility, arr_dc.size());
                DataCenterFactory.print.println("wholeSystemAverageUtility" + wholeSystemAverageUtility);
	}

	@Override
	public float getIndexValue() {
		// TODO Auto-generated method stub
		return wholeSystemAverageUtility;
	}

	public float getRackAverageUtility() {
		return rackAverageUtility;
	}

	public float getDataCenterAverageUtility() {
		return dataCenterAverageUtility;
	}
	
	public float getWholeSystemAverageUtility() {
		return wholeSystemAverageUtility;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Average Utility: ";
	}

	/**
	 * Refactoring methods used to modify utility with related numbers
	 * @param utility
	 * @param numbers
	 * @return
	 */
	public float calUtilityDividedByNumbers(float utility, int numbers) {
		if(numbers == 0)
			return 0.0f;
		else 
			return utility / numbers;
	}
	
	public float getEffectivePM() {
		return effectivePM;
	}

	/**
	 * Refactored method calculating averageCPUUtility, averageMemUtility,
	 * averageMemUtility for each queue.
	 * 
	 * @param pq1
	 */
	private void calQueueUtility(ArrayList<PhysicalMachine> pq1) {
//		int row, col;
//
//		row = LoadBalanceFactory.writeToExcel.getRowNumber();
//		col = 0;
//		// Write lable
//		LoadBalanceFactory.writeToExcel.writeLabel(col, row, "PM Queue "
//				+ ++pmQueueNo);
//		row++;
//		LoadBalanceFactory.writeToExcel.writeLabel(col, row, "PM number");
//		LoadBalanceFactory.writeToExcel.writeLabel(col + 1, row,
//				"CPU utilization");
//		LoadBalanceFactory.writeToExcel.writeLabel(col + 2, row,
//				"Memory utilization");
//		LoadBalanceFactory.writeToExcel.writeLabel(col + 3, row,
//				"Storage utilization");
//		LoadBalanceFactory.writeToExcel.writeLabel(col + 4, row,
//				"Average utilization");
		// Tab to the next row
//		row++;

		for (int i = 0; i < pq1.size(); i++) {
			DataCenterFactory.print.println("PM" + pq1.get(i).getNo() + " "
					+ pq1.get(i).getAvgCpuUtility() + " "
					+ pq1.get(i).getAvgMemUtility() + " "
					+ pq1.get(i).getAvgStoUtility() + " "
					+ pq1.get(i).getAvgUtility());
			// Write data to excel, (row + i) is the row number
//			LoadBalanceFactory.writeToExcel.writeData(col, row, i);
//			LoadBalanceFactory.writeToExcel.writeData(
//					col + 1, row, pq1.get(i).getAvgCpuUtility());
//			LoadBalanceFactory.writeToExcel.writeData(
//					col + 2, row, pq1.get(i).getAvgMemUtility());
//			LoadBalanceFactory.writeToExcel.writeData(
//					col + 3, row, pq1.get(i).getAvgStoUtility());
//			LoadBalanceFactory.writeToExcel.writeData(
//					col + 4, row, pq1.get(i).getAvgUtility());
//			// tab to the next row
//			row++;
			if (pq1.get(i).getAvgUtility() != 0) {
				effectivePM++;
				averageUtility += pq1.get(i).getAvgUtility();
				rackAverageUtility = averageUtility;
			}
		}
		// tab another 5 rows to sperate section
//		row += 5;
//		LoadBalanceFactory.writeToExcel.setRowNumber(row);
	}
}
