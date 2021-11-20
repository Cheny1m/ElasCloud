package com.generaterequest;

/**
 * This class is used to store the VM request information from 
 * vmRequest.txt file. A simplified class of VirtualMachine
 * @author Minxian
 *
 */
class VmRequest{
	int no;
	int startTime;
	int endTime;
	int vmType;
	VmRequest(int no, int startTime, int endTime, int vmType){
		this.no = no;
		this.startTime = startTime;
		this.endTime = endTime;
		this.vmType = vmType;
		 
	}
	
	public int getNo(){
		return no;
	}
	
	public int getStartTime(){
		return startTime;
	}
	
	public int getEndTime(){
		return endTime;
	}
	
	public int getVmType(){
		return vmType;
	}
	
	public int getProcessTime(){
		return endTime - startTime;
	}
}
