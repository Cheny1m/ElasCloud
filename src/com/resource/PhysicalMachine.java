package com.resource;

import java.util.*;
import com.datacenter.LoadBalanceFactory;
import com.specification.PmInfo;

/**
 * This class defines the basic attributes and operations for Physical
 * Machine
 * @author yuanliang, Minxian
 *
 */
public class PhysicalMachine extends Server implements Comparable<PhysicalMachine>{
	private int no;
	private int pmType;
	private float minPower;
	private float maxPower;
	private int pmTurnOnTime;
	private int pmDuration;
	private float capacity_makeSpan;
	private float proportion = 1.0f;
        private int rejectedNum = 0;
        
	PmInfo pmInfo = new PmInfo();

	Set<Integer> timeSlot = new HashSet<Integer>();
	public ArrayList<Resource> resource = new ArrayList<Resource>();
	
	public ArrayList<VirtualMachine> vms=new ArrayList<VirtualMachine>();
	
	public PhysicalMachine(int no,int pmType){
		super();
		// TODO Auto-generated constructor stub
		this.no=no;
		this.cpuTotal=pmInfo.getCpu(pmType);
		this.memTotal=pmInfo.getMem(pmType);
		this.storageTotal=pmInfo.getStorage(pmType);
		this.minPower=pmInfo.getMinPower(pmType);
		this.maxPower=pmInfo.getMaxPower(pmType);
		this.pmType=pmType;
		for(int i = 0; i < LoadBalanceFactory.MAXTIME; i++){

			resource.add(new Resource(
					cpuTotal * proportion, memTotal * proportion, storageTotal * proportion));
		}
	}
	
	public int getNo(){
		return no;
	}

	public void setNo(int no){
		this.no = no;
	}

	public int getPmType(){
		return pmType;
	}

	public void setPmType(int pmType){
		this.pmType = pmType;
	}

	public float getMaxPower(){
		return maxPower;
	}
	public void setMaxPower(float maxPower){
		this.maxPower = maxPower;
	}

	public float getMinPower(){
		return minPower;
	}
	
	public void setMinPower(float minPower){
		this.minPower = minPower;
	}
        
        public void setRejectedNum(int rejectedNum){
            this.rejectedNum = rejectedNum;
    }
        
        public int getRejectedNum(){
            return rejectedNum;
        }        
        public float getCurrentUtility(int currentTime){
                float currentutility = 0.0f;
                for(int i = 0; i < vms.size(); i++){
                    if( (vms.get(i).getStartTime() <= currentTime)
                            && (vms.get(i).getEndTime() >= currentTime)){
                        currentutility += vms.get(i).getCpuTotal();
                    }
                }
                return currentutility / cpuTotal;
        }
        
	public int compareTo(PhysicalMachine o){
		// TODO Auto-generated method stub
		if(this.no>o.no)
			return 1;
		else if(this.no<o.no)
			return -1;
		else
		return 0;
	}
	
	public int getEffectiveSlot(){
		for(int i = 0; i < vms.size(); i++){
			for(int j = vms.get(i).getStartTime(); j < vms.get(i).getEndTime(); j++){
				timeSlot.add(j);
			}
		}
		return timeSlot.size();
	}
	/**
	 * Calculate the utility of each slot and calculate the average CPU utility of all slots
	 * @return
	 */	
	public float getAvgCpuUtility(){
		float totalCPU = 0;
		for(int i = 0; i < vms.size(); i++){
			totalCPU += vms.get(i).getVmDuration() * vms.get(i).getCpuTotal();
		}
		if(getEffectiveSlot() != 0){
			totalCPU /= (float)getEffectiveSlot();
			totalCPU /= cpuTotal;
			return totalCPU;
		}else{
			return 0.0f;
		}
		
	}
	
	public float getAvgMemUtility(){
		float totalMem = 0;
		for(int i = 0; i < vms.size(); i++){
			totalMem += vms.get(i).getVmDuration() * vms.get(i).getMemTotal();
		}
		if(getEffectiveSlot() != 0){
			totalMem /= (float)getEffectiveSlot();
			totalMem /= memTotal;
			return totalMem;
		}else{
			return 0.0f;
		}
	}
	
	public float getAvgStoUtility(){
		float totalSto = 0;
		for(int i = 0; i < vms.size(); i++){
			totalSto += vms.get(i).getVmDuration() * vms.get(i).getStorageTotal();
		}
		if(getEffectiveSlot() != 0){
			totalSto /= (float) getEffectiveSlot();
			totalSto /= storageTotal;
			return totalSto;
		}else{
			return 0.0f;
		}
	}
	public float getAvgUtility(){
		return	(getAvgCpuUtility() + getAvgMemUtility() + getAvgStoUtility()) / 3;
	}
	public float getPMAvgSlotLoad(){	
		return 0.0f;
	}
	public float getSlotLoad(ArrayList<VirtualMachine> vms){
		return 0.0f;
	}
	
	public void setPMTurnTime(int pmTurnOnTime){
		
	}
	public int getPMTurnTime(){
		return pmTurnOnTime;
	}

	public int setPmDuration(VirtualMachine vm){

		
		return pmDuration;
	}
	
	public int getPmDuration(){
		return pmDuration;
	}
	
	public float setCapacity_Makespan(float p_CM){
		return capacity_makeSpan = p_CM;
	}
	public float getCapacity_Makespan(){
		return capacity_makeSpan;
	}
	/**
	 * Get the value of total Capacity_makespan. Here only calculates the cpu
	 * element. Calculated through CPU capacity value * duration.
	 * @return
	 */
	public float getTotalCapacityMakespan(){
		float totalCapacityMakespan = 0.0f;
		for(int i = 0; i < vms.size(); i++){
			totalCapacityMakespan += vms.get(i).getVmDuration() * vms.get(i).getCpuTotal();
		}
		return totalCapacityMakespan;
	}
	
	public void setEffectiveTimeSlot(VirtualMachine vm){
	}
	
	public int getEffectiveTimeSlot(){
		return 0;
	}
}

