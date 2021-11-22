package com.schedule.loadbalance;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.datacenter.LoadBalanceFactory;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

/**
* This class is the the base class for OfflineAlgortihm series.
* @author Minxian
 */

public class OfflineAlgorithm{
		public String description = "---Offline";
		public OfflineAlgorithm(){
			
		}
		/**
		 * A different method compared with online algorithms, for offline 
		 * algorithms, the VM requests generation methods may vary.
		 * @param lbf
		 */
		public void createVM(LoadBalanceFactory lbf){
			
		}
		
		public void allocate(ArrayList<VirtualMachine> p_vmQueue,
				ArrayList<PhysicalMachine> p_pmQueueOne,
				ArrayList<PhysicalMachine> p_pmQueueTwo,
				ArrayList<PhysicalMachine> p_pmQueueThree) {
			
		}
		
		public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc){
			
		}
		
		public String getDescription(){
			return description;
		}
}
