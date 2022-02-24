
package com.schedule.energysaving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.*;
import javax.swing.*;


/**
 * The energy saving series algorithm,based on CMP(Capacity_Makespan Partition). Requests would
 * be divided by the p_value = max(0.25(L1, L2)). L1 is the max CM value of all PMs, L2 is the average CM
 * value of all PMs. Then requests would be partitioned by p_value with several parts.
 * Each part would be viewed as a new request to be allocated.
 * @author Yueming Chen
 *
 */
public class EnergySavingCMP extends OfflineAlgorithm {

	int dataCenterIndex; // Selected data center ID
	int rackIndex; // Selected rack ID
	int index = 0;    //Allocated PM ID
	int currentTime = 0;
	int vmId = 0;    //vmId is the id in sorted in vmQueue
	int vmID = 0;   //vmID is the Id in the new partition queue
	int pmTotalNum;
	int increase = 1;
	int decrease = -1;
	int triedAllocationTimes = 0;
	int rejectedVM;
	VirtualMachine vm;

	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
//	ArrayList<PhysicalMachine> pmQueueOne = new ArrayList<PhysicalMachine>();
//	ArrayList<PhysicalMachine> pmQueueTwo = new ArrayList<PhysicalMachine>();
//	ArrayList<PhysicalMachine> pmQueueThree = new ArrayList<PhysicalMachine>();

	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();

	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

	int pmQueueOneSize;
	int pmQueueTwoSize;
	int pmQueueThreeSize;


	public EnergySavingCMP() {
		//	System.out.println(getDescription());
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description + "-Prepartition(k=4) Algorithm---";
	}

	@Override
	public void createVM(DataCenterFactory dcf) {
		//dcf.createVM(new CreateLLNLRequests());
		//dcf.createVM(new CreateVM());
	}

	/**
	 * Generate the random index and try to allocate VM to the PM with generated
	 * index.
	 */


	@Override
	public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
		// TODO Auto-generated method stub
		DataCenterFactory.print.println(getDescription());



		//CMP core methods
		//this.vmQueue = p_vmQueue;
		this.arr_dc = p_arr_dc;

		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();

		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
		int allocatedDataCenterID;
		int allocatedRackID;

		//完成预分割的所有动作；
		this.vmQueue = processCMP(p_vmQueue, pmTotalNum);
		int NumbZones = vmQueue.size() - p_vmQueue.size();
		DataCenterFactory.print.println("分区数为： " + NumbZones);

		//将VMQueue按照Capacity_Makespan降序排列
		Collections.sort(vmQueue,new SortByRequestCapacityMakespan());
//
//		for(int i = 0 ; i < vmQueue.size() ; i++){
//			vmQueue.get(i).setVmNo(i);
//		}
//		System.out.println("新请求按CM从大到小排序结果：");
//		for(int i = 0 ; i < vmQueue.size() ; i++){
//			System.out.println(vmQueue.get(i).getVmNo()+"  "+ vmQueue.get(i).getStartTime()+"  "+vmQueue.get(i).getEndTime()+" "+vmQueue.get(i).getCpuTotal());
//		}

		//Collections.sort(vmQueue,new SortByRequestCapacityMakespan());

		DataCenterFactory.print.println("===currentTime:" + currentTime + "===");
		while (!vmQueue.isEmpty()) {
			//查找当前时间可以分配的请求（VM）
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				//如果有此请求，则开始分配
				vm = vmQueue.get(vmId);
			} else {
				//如果排在前面的vm当前时间还未开始请求，则继续寻找
				vmId++;
				triedAllocationTimes = 0;
				checkVmIdAvailable();
				continue;
			}

			//分配过程，首先对数据中心PM的CM进行降序
			Collections.sort(arr_dc, new SortByDataCenterCapacityMakespan());
			dataCenterIndex = 0;
			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();

			Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf(), new SortByRackCapacityMakespan());
			rackIndex = 0;
			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getLbf_ID();

			//PM优先级
			//index = 0;
			index %= pmTotalNum;
			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
				//按PM排序;此处平均利用率最大，即代表着平均的CM容量最大；因为各个PM的CM相同，总工作时间也相同
				//Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByPMUtility());
				//优先分配给CM最低的PM
				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByCapacityMakespan());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
				//Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortByPMUtility());
				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortByCapacityMakespan());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
			} else {
				//Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortByPMUtility());
				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortByCapacityMakespan());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
			}
		}
		//sortAllPMsInOrder(p_arr_dc);
		DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
	}

	//老方法
//	@Override
//	public void allocate(ArrayList<VirtualMachine> p_vmQueue,
//						 ArrayList<PhysicalMachine> p_pmQueueOne,
//						 ArrayList<PhysicalMachine> p_pmQueueTwo,
//						 ArrayList<PhysicalMachine> p_pmQueueThree) {
//		// TODO Auto-generated method stub
//		LoadBalanceFactory.print.println(getDescription());
//		//CMP core methods
//
//		this.vmQueue = processCMP(p_vmQueue, p_pmQueueThree.size());
//
//		this.pmQueueOne = p_pmQueueOne;
//		this.pmQueueTwo = p_pmQueueTwo;
//		this.pmQueueThree = p_pmQueueThree;
//
//		//	System.out.println(pmQueueOne.get(0).resource.get(0).getCpuUtility());
//		//	System.out.println(pmQueueOne.get(0).resource.get(0).getMemUtility());
//		//	System.out.println(pmQueueOne.get(0).resource.get(0).getStoUtility());
//
//		pmTotalNum = pmQueueOne.size() + pmQueueTwo.size() + pmQueueThree.size();
//		while(!vmQueue.isEmpty()){
//			//Pick out the vm with startTime less than currentTime to allocate
//			if(currentTime >= vmQueue.get(vmId).getStartTime()){
//				vm = vmQueue.get(vmId);
//			}
//			else{
//				vmId++;
//				//Refactored method, see details in definition.
//				checkVmIdAvailable();
//				continue;
//			}
//			//Always the first PM after sorted.
//			index = 0;
//			LoadBalanceFactory.print.println("CMP-Index:"+index);
//			//Three queues should be decided which queue would be added.
//			if( vm.getVmType() > 0 && vm.getVmType() <= 3){
//				allocateVm(vm, pmQueueThree.get(index));
//			}
//			else if( vm.getVmType() > 3 && vm.getVmType() <= 6 ){
//				allocateVm(vm, pmQueueTwo.get(index));
//			}
//			else{
//				allocateVm(vm, pmQueueOne.get(index));
//			}
//		}
//		LoadBalanceFactory.print.println(LoadBalanceFactory.FINISHEDINFO);
//	}

	/**
	 * Key scheduling procedure for algorithm. Main procedures are as below:
	 * 1. Check whether resource of a PM is available.
	 * 2. If resource available, output success information. Put the VM to
	 * deleteQueue, and remove that VM from vmQueue.
	 * 3. Update available resource of PM.
	 *
	 * @param vm2
	 * @param pm2
	 */

	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2, PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		//判断那一时刻该PM资源是否可用
		if (checkResourceAvailble(vm2, pm2)) {
			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " " + "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM" + pm2.getNo());
			//将已分配任务添加至待删除队列
			deleteQueue.add(vm2);
			//从待分配队列中删除已分配任务
			vmQueue.remove(vm2);
			//将该任务加入此物理机上的任务队列
			pm2.vms.add(vm2);
			//设置此任务分配至的物理机NO；机架NO；数据中心NO
			vm2.setPmNo(pm2.getNo());
			vm2.setRackNo(rackNo);
			vm2.setDataCenterNo(dataCenterNo);

			//更新PM资源
			updateResource(vm2, pm2, decrease);

			//因为移除了VM，不需要改动VMID
			//vmId++;
			vmId = 0;
			triedAllocationTimes = 0;
			checkVmIdAvailable();
			index = 0;
		} else {
			//否则尝试其它物理机
			if (triedAllocationTimes == pmTotalNum) {
				System.out.println("VM number is too large, PM number is not enough  --  " + getDescription());
				JOptionPane.showMessageDialog(null, "VM number is too large, PM number is not enough", "Error", JOptionPane.OK_OPTION);
				throw new IllegalArgumentException("PM too less");
			} else {
				triedAllocationTimes++;
				DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
				index++; // Try another PM
			}
		}
	}
	//old mothed
//	private void allocateVm(VirtualMachine vm2, PhysicalMachine pm2) {
//		// TODO Auto-generated method stub
//		if(checkResourceAvailble(vm2, pm2)){
//		LoadBalanceFactory.print.println("Allocate:VM" + vm2.getVmNo()+ " to PM" + pm2.getNo());
//		deleteQueue.add(vm2);
//		vmQueue.remove(vm2);
//		pm2.vms.add(vm2);
//		vm2.setPmNo(pm2.getNo());
//		updateResource(vm2, pm2, decrease);
//                sortPM(vm2);
//
//		vmId++;
//		checkVmIdAvailable();
//		}
//		else{
//                        vmQueue.remove(vm2); //Though the request can not be allocated, that VM should be removed.
//                        LoadBalanceFactory.print.println("VM is rejected");
//                        rejectedVM = pm2.getRejectedNum();
//                        pm2.setRejectedNum(rejectedVM + 1);
//
//                        vmId++;
//                        checkVmIdAvailable();
//		}
//	}
//

	/**
	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
	 */
	private void checkVmIdAvailable() {
		//如果虚拟机序号大于请求队列，则时间加一；从头开始循环寻找
		if (vmId >= vmQueue.size()) {
			currentTime++;
			vmId = 0;
			triedAllocationTimes = 0;
			DataCenterFactory.print.println("===currentTime:" + currentTime + "===");
			processDeleteQueue(currentTime, deleteQueue);
		}
	}

	/**
	 * Check whether the left resource are available
	 *
	 * @param vm3
	 * @param pm3
	 * @return
	 */
	private boolean checkResourceAvailble(VirtualMachine vm3, PhysicalMachine pm3) {
		boolean allocateSuccess = true;
		boolean oneSlotAllocation;
		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
			//在那一时刻资源够用的话
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() >= vm3.getCpuTotal())
					&& (pm3.resource.get(t).getMemUtility() >= vm3.getMemTotal())
					&& (pm3.resource.get(t).getStoUtility() >= vm3.getStorageTotal());
			allocateSuccess = allocateSuccess && oneSlotAllocation;

			if (false == allocateSuccess) {
				// If allocated failed, return exactly.
				return allocateSuccess;
			}
		}
		return allocateSuccess;
	}

	/**
	 * Update the available resource. When parameter 3 equals to increase, available resource
	 * would increased, else resource would be decreased.
	 *
	 * @param vm4
	 * @param pm4
	 * @param incOrDec
	 */
	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4, int incOrDec) {
		//如果是刚完成分配
		if (incOrDec == decrease) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				//将对应物理机每个时隙的资源进行更新
				pm4.resource.get(t).setCpuUtility(pm4.resource.get(t).getCpuUtility() - vm4.getCpuTotal());
				pm4.resource.get(t).setMemUtility(pm4.resource.get(t).getMemUtility() - vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(pm4.resource.get(t).getStoUtility() - vm4.getStorageTotal());
			}
			DataCenterFactory.print.println("Resource is updated(dec)");
		}

		//如果是任务结束
		if (incOrDec == increase) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				pm4.resource.get(t).setCpuUtility(pm4.resource.get(t).getCpuUtility() + vm4.getCpuTotal());
				pm4.resource.get(t).setMemUtility(pm4.resource.get(t).getMemUtility() + vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(pm4.resource.get(t).getStoUtility() + vm4.getStorageTotal());
			}
			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo() + " from DataCenter" + vm4.getDataCenterNo() + " Rack" + vm4.getRackNo() + " PM" + pm4.getNo());
			DataCenterFactory.print.println("Resource is updated(inc)");

		}
	}

	/**
	 * After the VM has been added to deleteQueue, if end time comes, that VM should
	 * be removed from deleteQueue. Available resource should also be updated.
	 *
	 * @param p_currentTime
	 * @param p_deleteQueue
	 */
	private void processDeleteQueue(int p_currentTime, ArrayList<VirtualMachine> p_deleteQueue) {
		// TODO Auto-generated method stub
		VirtualMachine vm5;
		int pmNo;
		int dataCenterNo;
		int rackNo;

		for (int i = 0; i < p_deleteQueue.size(); i++) {
			vm5 = p_deleteQueue.get(i);
			dataCenterNo = vm5.getDataCenterNo();
			rackNo = vm5.getRackNo();
			pmNo = vm5.getPmNo();

			if (p_currentTime >= vm5.getEndTime()) {
				if (pmNo >= 0 && pmNo < pmQueueOneSize) {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueOne().get(pmNo), increase);
				} else if (pmNo >= pmQueueOneSize && pmNo < pmQueueOneSize + pmQueueTwoSize) {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueTwo().get(pmNo - pmQueueOneSize), increase);
				} else {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueThree().get(pmNo - pmQueueOneSize - pmQueueTwoSize), increase);
				}
				p_deleteQueue.remove(vm5);
			}
		}
	}


	/**
	 * Capacity_makespan core methods. Partitioning the requests into parts and create
	 * a new VM queue with new capacity_makespan value. The start time and end time
	 * may be different from the previous ones.
	 *
	 * @param p_vmQueue
	 * @param m
	 * @return
	 */
	private ArrayList<VirtualMachine> processCMP(ArrayList<VirtualMachine> p_vmQueue, int m) {
		//CM值
		int capacity_makespan = 0;
		//最大CM
		int maxCapacity_makespan = 0;
		//平均CM
		int averageCapacity_makespan = 0;
		int partitionCapacity_makespan = 0;
		VirtualMachine vm2;
		//The new vm queue after partition
		ArrayList<VirtualMachine> cmp_vmQueue = new ArrayList<VirtualMachine>();

		/*
		 * Calculate 0.25 * max(L1, L2)；计算分割之后的CM长度
		 */
		for (int i = 0; i < p_vmQueue.size(); i++) {
			vm2 = p_vmQueue.get(i);
			//计算单个VM的最大CM值
			capacity_makespan = (int) ((vm2.getEndTime() - vm2.getStartTime()) * vm2.getCpuTotal());
//			if (capacity_makespan > maxCapacity_makespan) {
//				maxCapacity_makespan = capacity_makespan;
//			}
			averageCapacity_makespan += capacity_makespan;
		}
		//根据pm个数，计算平均的cm
		averageCapacity_makespan /= m;
		//DataCenterFactory.print.println("理想负载均衡值的上限为（每个PM上理论的Capacity_Makespan值)： " + averageCapacity_makespan);
		//计算分割之后的CM值，k值选4;
		int k = 4;
		//partitionCapacity_makespan = maxCapacity_makespan > averageCapacity_makespan ? maxCapacity_makespan : averageCapacity_makespan;
		//定义vm可以在pm上连续运行的最大时间长度
		partitionCapacity_makespan =(int) Math.ceil(averageCapacity_makespan*1.0 / k );
		//partitionCapacity_makespan =(int) Math.ceil(averageCapacity_makespan);

		//partitionCapacity_makespan = averageCapacity_makespan;
		DataCenterFactory.print.println("理想负载均衡值的上限为（每个PM上理论的Capacity_Makespan值)： " + partitionCapacity_makespan);

		//DataCenterFactory.print.println("每个VM可以在PM上运行的最大CM=" + partitionCapacity_makespan);

		/*
		 * Recombine the requests;重新组合请求
		 */
		for (int i = 0; i < p_vmQueue.size(); i++) {
			vm2 = p_vmQueue.get(i);
			regenVM(vm2, partitionCapacity_makespan, cmp_vmQueue,k);
		}
		//DataCenterFactory.print.println("Partition Times:" + (cmp_vmQueue.size() - p_vmQueue.size()));
		//输出新vm结果
		System.out.println("新分区结果为：");
		for( int i =0 ; i<cmp_vmQueue.size() ; i++ ){
			System.out.println(cmp_vmQueue.get(i).getVmNo()+" "+ cmp_vmQueue.get(i).getStartTime()+" "+cmp_vmQueue.get(i).getEndTime()+ " " + cmp_vmQueue.get(i).getVmType());
		}
		System.out.println("——————————————");
		//Results have been tested to be right
		//System.out.println(cmp_vmQueue.size());
		return cmp_vmQueue;
	}


	/**
	 * Recursive method to generate new VM queue, if a VM capacity_makespan is less
	 * than the partitionCapacity_makespan, add the VM to new queue. If not, divide
	 * the vm into more than one vms and put them to the new queue.
	 */
	public void regenVM(VirtualMachine vm2, int partitionCapacity_makespan, ArrayList<VirtualMachine> cmp_vmQueue,int k) {

		int capacity_makespan = 0;
		VirtualMachine vm3, vm4;
		int duration;
		int durNum;
		//double duration;
		//获得此VM的Capacity_makespan
		capacity_makespan = (int) ((vm2.getEndTime() - vm2.getStartTime()) * vm2.getCpuTotal());
		if (capacity_makespan <= partitionCapacity_makespan) {
			vm3 = new VirtualMachine(vmID++, vm2.getStartTime(), vm2.getEndTime(), vm2.getVmType());
			cmp_vmQueue.add(vm3);
		} else {
			//如果CM过大，需要进行预分区
			//如果CM > p0/k,那么将其划分为若干个p0/k的子区间
			//durNum代表分区数量
			durNum =(int) Math.ceil(capacity_makespan*1.0 / (partitionCapacity_makespan));
			//durNum =(int) Math.ceil(capacity_makespan*1.0 / (Math.ceil(partitionCapacity_makespan * 1.0 / k)));
			//duration代表分区后的每个新请求的时间跨度
			duration = (int) Math.ceil(capacity_makespan*1.0 / vm2.getCpuTotal() / durNum);
			for(int i =0 ;i < durNum ; i++ ){
				if(i == 0){
					vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration,vm2.getStartTime()+(i+1)*duration,vm2.getVmType());
				}
				else if(i == durNum -1){
					vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration+1,vm2.getEndTime(),vm2.getVmType());
				}
				else{
					vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration+1,vm2.getStartTime()+(i+1)*duration,vm2.getVmType());
				}
				cmp_vmQueue.add(vm3);
			}
			//算法问题
//			duration = (int) ((capacity_makespan - partitionCapacity_makespan) / vm2.getCpuTotal());
//			//duration = Math.ceil((capacity_makespan - partitionCapacity_makespan) / vm2.getCpuTotal());
//			vm3 = new VirtualMachine(vmID++, vm2.getStartTime(),
//					vm2.getStartTime() + duration, vm2.getVmType());
//			cmp_vmQueue.add(vm3);
//			vm4 = new VirtualMachine(vmID, vm3.getEndTime() + 1, vm2.getEndTime(), vm2.getVmType());
//			//recursive method
//			regenVM(vm4, partitionCapacity_makespan, cmp_vmQueue);
		}
	}

	public void sortAllPMsInOrder(ArrayList<DataCenter> p_arr_dc) {
		Collections.sort(p_arr_dc, new SortByDataCenterID());
		for (DataCenter dc : p_arr_dc) {
			Collections.sort(dc.getArr_lbf(), new SortByRackID());
			for (LoadBalanceFactory lbf : dc.getArr_lbf()) {
				Collections.sort(lbf.getPmQueueOne(), new SortByPMID());
				Collections.sort(lbf.getPmQueueTwo(), new SortByPMID());
				Collections.sort(lbf.getPmQueueThree(), new SortByPMID());
			}
		}
	}
}

class SortByDataCenterCapacityMakespan implements Comparator<DataCenter> {

	@Override
	public int compare(DataCenter o1, DataCenter o2) {
		DataCenter dc1 = (DataCenter) o1;
		DataCenter dc2 = (DataCenter) o2;
		if (dc1.getDataCenterCapacityMakespan() > dc2.getDataCenterCapacityMakespan()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class SortByRackCapacityMakespan implements Comparator<LoadBalanceFactory> {

	@Override
	public int compare(LoadBalanceFactory p_lbf1, LoadBalanceFactory p_lbf2) {
		LoadBalanceFactory lbf1 = p_lbf1;
		LoadBalanceFactory lbf2 = p_lbf2;
		if (lbf1.getRackCapacityMakespan() > lbf2.getRackCapacityMakespan()) {
			return 1;
		}
		return 0;
	}
}

class SortByDataCenterID implements Comparator<DataCenter> {

	@Override
	public int compare(DataCenter o1, DataCenter o2) {
		DataCenter dc1 = (DataCenter) o1;
		DataCenter dc2 = (DataCenter) o2;
		if (dc1.getD_id() > dc2.getD_id()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class SortByRackID implements Comparator<LoadBalanceFactory> {

	@Override
	public int compare(LoadBalanceFactory p_lbf1, LoadBalanceFactory p_lbf2) {
		LoadBalanceFactory lbf1 = p_lbf1;
		LoadBalanceFactory lbf2 = p_lbf2;
		if (lbf1.getLbf_ID() > lbf2.getLbf_ID()) {
			return 1;
		}
		return 0;
	}
}

class SortByPMID implements Comparator<PhysicalMachine> {

	@Override
	public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
		PhysicalMachine pm1 = p_pm1;
		PhysicalMachine pm2 = p_pm2;
		if (pm1.getNo() > pm2.getNo()) {
			return 1;
		}
		return 0;
	}
}

class SortByPMUtility implements Comparator<PhysicalMachine> {

	@Override
	public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
		PhysicalMachine pm1 = p_pm1;
		PhysicalMachine pm2 = p_pm2;
		if (pm1.getAvgUtility() > pm2.getAvgUtility()) {
			return 1;
		}
		return 0;
	}
}


class SortByStartTime implements Comparator<VirtualMachine> {

	@Override
	public int compare(VirtualMachine p_vm1, VirtualMachine p_vm2) {
		VirtualMachine vm1 = p_vm1;
		VirtualMachine vm2 = p_vm2;
		if (vm1.getStartTime() > vm2.getStartTime()) {
			return 1;
		}
		return 0;
	}
}

//已分配资源从大到小排序；剩余资源有小到大

//按VM所需要的CM从大到小排序
class SortByRequestCapacityMakespan implements Comparator<VirtualMachine> {

	public int compare(VirtualMachine v_vm1, VirtualMachine v_vm2) {
		VirtualMachine vm1 = v_vm1;
		VirtualMachine vm2 = v_vm2;
		if (vm1.getVmDuration() * vm1.getCpuTotal() < vm2.getVmDuration() * vm2.getCpuTotal()) {
			return 1;
		}
		return 0;
	}
}

