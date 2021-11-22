package com.schedule.loadbalance;

import java.util.ArrayList;

import com.datacenter.DataCenter;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

/**
* This class is the the base class for OnlineAlgortihm series.
* 
* To allocate the VM to the corresponding PM, I have added four parameters here.
* By passing the four parameters here, the specified algorithm would be 
* adopted to calculate efficiency. 
* It seems over-parameterized, may the last three parameters can be packed
* into one? 
* @param vmQueue
* @param pmQueueOne
* @param pmQueueTwo
* @param pmQueueThree

 * @author Minxian
 *
 */
public class OnlineAlgorithm{
	String description = "----Online";	
	public OnlineAlgorithm(){
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
