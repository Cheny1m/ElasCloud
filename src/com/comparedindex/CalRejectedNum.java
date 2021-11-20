/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.comparedindex;

import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
import java.util.ArrayList;

/**
 * This class is used to calculate the rejected VM number.
 * @author LukeXu
 */
public class CalRejectedNum extends ComparisonIndex{
                 private float rejectedNum= 0.0f;
		ArrayList<PhysicalMachine> pq1;
		ArrayList<PhysicalMachine> pq2;
		ArrayList<PhysicalMachine> pq3;
		public CalRejectedNum(ArrayList<PhysicalMachine> pq1, 
						  ArrayList<PhysicalMachine> pq2, 
						  ArrayList<PhysicalMachine> pq3){
			this.pq1 = pq1;
			this.pq2 = pq2;
			this.pq3 = pq3;
                        calQueueRejectedNum(pq1);
                        calQueueRejectedNum(pq2);
                        calQueueRejectedNum(pq3);
			
		}

		@Override
		public float getIndexValue() {
			// TODO Auto-generated method stub
			return rejectedNum;
		}

		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return "Rejected VM Number: ";
		}
		
		private void calQueueRejectedNum(ArrayList<PhysicalMachine> pq1){
			for(int i = 0; i < pq1.size(); i++){
                           rejectedNum += pq1.get(i).getRejectedNum();
			}
		}
}
