package com.datacenter;

import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByEndTime;
import com.generaterequest.CreateVMByPorcessTime;
import com.generaterequest.PMBootor;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;
/**
 * This class implements the interface DataCenterFactory, that is to say, 
 * LoadBalanceFactory would follow the defined process of. While for the instance
 * of EnergySavingFactory, it can produce different composition for different requests
 * creating approaches, scheduling algorithms, comparison indices.
 * @author Minxian
 *
 */
public class EnergySavingFactory implements DataCenterImp{
	String description;	
	public EnergySavingFactory(){
			description ="EnergySavingFactory";
		}
	public void allocate(OnlineAlgorithm aa) {
		// TODO Auto-generated method stub
		
	}
	public void allocate(OfflineAlgorithm ofla) {
		// TODO Auto-generated method stub
		
	}
	public void bootPM(PMBootor pmb) {
		// TODO Auto-generated method stub
		
	}
	public void createVM(CreateVM cv) {
		// TODO Auto-generated method stub
		
	}
	public void createVM(CreateVMByEndTime cvbe) {
		// TODO Auto-generated method stub
		
	}
	public void createVM(CreateVMByPorcessTime cvbpt) {
		// TODO Auto-generated method stub
		
	}
	public void generateReuquest() {
		// TODO Auto-generated method stub
		
	}

	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	public void showIndex() {
		// TODO Auto-generated method stub
		
	}

}
