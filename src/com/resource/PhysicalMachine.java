package com.resource;

import java.util.*;

import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateVM;
import com.specification.PmInfo;

/**
 * This class defines the basic attributes and operations for Physical Machine
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
	private boolean turnStatus = true;
	//pm的最大负载
	private float capacity_makeSpan;
	private float makeSpan;
	//容量？
	private float proportion = 1.0f;
	//拒绝申请数
	private int rejectedNum = 0;
        
	PmInfo pmInfo = new PmInfo();

	//时间间隙/插槽
	Set<Integer> timeSlot = new HashSet<Integer>();
	//PM性能
	//存储每一个时间插槽可用的资源
	public ArrayList<Resource> resource = new ArrayList<Resource>();
	
	public ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();

	//public ArrayList<VirtualMachine> vms = DataCenterFactory.vmQueue;

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
		//添加资源池，即此物理机个时间间隙的资源。 但也不用 LoadBalanceFactory.MAXTIME吧？？  好像是在每一个时间间隙有这么多资源可以用
		for(int i = 0; i < LoadBalanceFactory.MAXTIME; i++){
			resource.add(new Resource(cpuTotal * proportion, memTotal * proportion, storageTotal * proportion));
		}
	}
	
	public int getNo(){ return no; }
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


    //获取当前cpu效用；cpu使用占cpu总量
    public float getCurrentUtility(int currentTime){
		float currentutility = 0.0f;
		for(int i = 0; i < vms.size(); i++){
			if( (vms.get(i).getStartTime() <= currentTime) && (vms.get(i).getEndTime() >= currentTime)){
				//调用VirtualMachine继承自父类server的属性和方法，最后通过VmInfo读取相应的VM资源数据，读取出来的数据为该VM的CPU资源值
				currentutility += vms.get(i).getCpuTotal();
			}
		}
		//在此物理机上的某一时间间隙（插槽）的所有任务的CPU容量占此物理机CPU容量的比例
		return currentutility / cpuTotal;
	}

	//计算PM当前活跃时间与VM的重叠时间
	public int getMinEnergyConsumption(VirtualMachine vm1){
		int t = 0 ;
		for(int j = vm1.getStartTime() ; j < vm1.getEndTime() ; j++){
			if(getCurrentUtility(j) != 0 ){
				t++;
			}
		}
		return t;
	}

	//按PMNO从小到达排序
	public int compareTo(PhysicalMachine o){
		// TODO Auto-generated method stub
		if(this.no>o.no)
			return 1;
		else if(this.no<o.no)
			return -1;
		else
		return 0;
	}

	//计算效用时隙；vm运行的总运行时间（独立） //计算的每个物理机的运行时间
	public int getEffectiveSlot(){
		//该物理机上当前的makespan长度
		for(int i = 0; i < vms.size(); i++){
			//某一VM使用该物理机的时间插槽（间隙）长度；
			for(int j = vms.get(i).getStartTime(); j < vms.get(i).getEndTime(); j++){
				timeSlot.add(j);
			}
		}
		return timeSlot.size();//集合去重
	}

	public int getMaxEndTime(){
		//该物理机上当前的结束时间
		int maxTime = Integer.MIN_VALUE;
		for(int i = 0; i < vms.size(); i++){
			if(vms.get(i).getEndTime() > maxTime) maxTime = vms.get(i).getEndTime();
		}
		return maxTime;
	}

//	public int getEffectiveSlot(){
//		if(vms.size() == 0) return 0;
//		//int firstStartTime = vms.get(0).getStartTime();
//		int firstStartTime = Integer.MAX_VALUE;
//		int lastEndTime = Integer.MIN_VALUE;
//		for(int i = 0 ; i < vms.size() ; i++){
//			if(vms.get(i).getEndTime() > lastEndTime) lastEndTime = vms.get(i).getEndTime();
//			if(vms.get(i).getStartTime() < firstStartTime) firstStartTime = vms.get(i).getStartTime();
//		}
//		return (lastEndTime - firstStartTime);
//	}



	/**
	 * Calculate the utility of each slot and calculate the average CPU utility of all slots
	 * @return
	 */
	//平均CPU效用（每个时间间隙）
	public float getAvgCpuUtility(){
		float totalCPU = 0;
		//当前在此物理机上分配了的虚拟机
		for(int i = 0; i < vms.size(); i++){
			//每个虚拟机需求的cpu总量
			totalCPU += vms.get(i).getVmDuration() * vms.get(i).getCpuTotal();
		}
		//获取当前物理机的总开机时间
		if(getEffectiveSlot() != 0){
			//计算每个时间区间内的平均CPU用量
			totalCPU /= (float)getEffectiveSlot();
			//计算平均CPU利用比例
			totalCPU /= cpuTotal;
			return totalCPU;
		}else{
			return 0.0f;
		}
	}

	public float getAvgUtility(int duration){
		if(getEffectiveSlot() == 0) return 0.0f;
		float totalCPU = 0;
		//int lastEndTime = Integer.MIN_VALUE;
		//int firstStartTime = vms.get(0).getStartTime();
		//当前在此物理机上分配了的虚拟机
		for(int i = 0; i < vms.size(); i++){
			//每个虚拟机需求的cpu总量
			totalCPU += vms.get(i).getVmDuration() * vms.get(i).getCpuTotal();
			//if(vms.get(i).getEndTime() > lastEndTime) lastEndTime = vms.get(i).getEndTime();
		}
		//获取当前物理机的总开机时间
		if(getEffectiveSlot() != 0){
			//计算每个时间区间内的平均CPU用量
			//totalCPU /= duration * 1.0;
			//totalCPU /= (lastEndTime - firstStartTime) * 1.0;
			totalCPU /= getEffectiveSlot() * 1.0;
			//计算平均CPU利用比例
			totalCPU /= cpuTotal;
			return totalCPU;
		}else{
			return 0.0f;
		}
	}

	public float getCurrtimeLoad(){
		float totalCPU = 0;
		//当前在此物理机上分配了的虚拟机
		for(int i = 0; i < vms.size(); i++){
			//每个虚拟机需求的cpu总量
			totalCPU += vms.get(i).getVmDuration() * vms.get(i).getCpuTotal();
		}
		//获取当前物理机的总开机时间
		if(getEffectiveSlot() != 0){
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
	//平均综合效用
	public float getAvgUtility(){ return	(getAvgCpuUtility() + getAvgMemUtility() + getAvgStoUtility()) / 3; }

	public float getPMAvgSlotLoad(){	
		return 0.0f;
	}
	public float getSlotLoad(ArrayList<VirtualMachine> vms){
		return 0.0f;
	}
	
	public void setPMTurnTime(int pmTurnOnTime){this.pmTurnOnTime =  pmTurnOnTime; }
	public int getPMTurnTime(){ return pmTurnOnTime; }

	public int setPmDuration(VirtualMachine vm){ return pmDuration; }
	public int getPmDuration(){ return pmDuration; }
	
	public float setCapacity_Makespan(float p_CM){ return capacity_makeSpan = p_CM; }

	public float getCapacity_Makespan(){ return capacity_makeSpan; }

	public float setMakespan(float CM){ return makeSpan = CM; }

	public float getMakespan(){ return makeSpan; }

	/*
	用于计算物理机某一段时间区间内的剩余资源大小
	 */
	public int getRemainCPU(int start,int end){
		int RemainCPU = 0;
		int UseCPU = 0;
		//当前在此物理机上分配了的虚拟机
		for(int i = 0; i < vms.size(); i++){
			//每个虚拟机需求的cpu总量
			//如果i虚拟机与待分配区间不重叠
			if(vms.get(i).getStartTime() >= end || vms.get(i).getEndTime() <= start){
				continue;
			}
			else if(vms.get(i).getStartTime() < start ){
				if(vms.get(i).getEndTime() <= end){
					UseCPU += (vms.get(i).getEndTime() - start) * vms.get(i).getCpuTotal();
				}
				else{
					UseCPU += (end - start) * vms.get(i).getCpuTotal();
				}
			}
			else{
				if(vms.get(i).getEndTime() <= end ){
					UseCPU += (vms.get(i).getEndTime() - vms.get(i).getStartTime()) * vms.get(i).getCpuTotal();
				}
				else{
					UseCPU += (end - vms.get(i).getStartTime()) * vms.get(i).getCpuTotal();
				}
			}
		}
		RemainCPU = (end - start) * (int)cpuTotal - UseCPU;
		//获取当前物理机的总开机时间
		if(getEffectiveSlot() != 0){
			//如果在此期间是活跃的
			return  RemainCPU;
		}else{
			//如果没开机，就是剩余总量
			return (end - start) * (int)cpuTotal;
		}
	}


	/**
	 * Get the value of total Capacity_makespan. Here only calculates the cpu
	 * element. Calculated through CPU capacity value * duration.
	 * @return
	 */
	//计算最大总负载；以CPU为例

	public float getTotalMakespan(){
		float totalMakespan = 0.0f;
		for(int i = 0; i < vms.size(); i++){
			totalMakespan += vms.get(i).getVmDuration();
		}
		return totalMakespan;
	}

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

	public void setTurnStatus(boolean turnStatus){
		this.turnStatus = turnStatus;
	}

	public boolean getTurnStatus(){
		return turnStatus;
	}

}

