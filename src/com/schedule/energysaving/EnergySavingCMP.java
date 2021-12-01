package com.schedule.energysaving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
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
	int index;    //Allocated PM ID
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
		return description + "-CMP Algorithm---";
	}

	@Override
	public void createVM(LoadBalanceFactory lbf) {
		lbf.createVM(new CreateLLNLRequests());
	}

	/**
	 * Generate the random index and try to allocate VM to the PM with generated
	 * index.
	 */


	@Override
	public void allocate(ArrayList<VirtualMachine> p_vmQueue,
						 ArrayList<DataCenter> p_arr_dc) {
		// TODO Auto-generated method stub
		DataCenterFactory.print.println(getDescription());
		//CMP core methods
		//this.vmQueue = p_vmQueue;
		this.arr_dc = p_arr_dc;

		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueOne().size();
		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueTwo().size();
		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueThree().size();

		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
		int allocatedDataCenterID;
		int allocatedRackID;

		//完成预分割的所有动作；
		this.vmQueue = processCMP(p_vmQueue, pmTotalNum);

		while (!vmQueue.isEmpty()) {
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				vm = vmQueue.get(vmId);
			} else {
				vmId++;
				triedAllocationTimes = 0;
				checkVmIdAvailable();
				continue;
			}

			Collections.sort(arr_dc, new SortByDataCenterCapacityMakespan());
			dataCenterIndex = 0;
			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();

			Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf(),
					new SortByRackCapacityMakespan());
			rackIndex = 0;
			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf()
					.get(rackIndex).getLbf_ID();

			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueOne(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueOne().get(index));
			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueTwo(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueTwo().get(index));
			} else {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueThree(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueThree().get(index));
			}
		}
		sortAllPMsInOrder(p_arr_dc);
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

	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2,
							PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		if (checkResourceAvailble(vm2, pm2)) {
			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " "
					+ "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM"
					+ pm2.getNo());
			deleteQueue.add(vm2);
			vmQueue.remove(vm2);
			pm2.vms.add(vm2);
			vm2.setPmNo(pm2.getNo());
			vm2.setRackNo(rackNo);
			vm2.setDataCenterNo(dataCenterNo);

			updateResource(vm2, pm2, decrease);

			vmId++;
			triedAllocationTimes = 0;
			checkVmIdAvailable();
			index = 0;
		} else {
			if (triedAllocationTimes == pmTotalNum) {
				System.out
						.println("VM number is too large, PM number is not enough");
				JOptionPane.showMessageDialog(null,
						"VM number is too large, PM number is not enough",
						"Error", JOptionPane.OK_OPTION);
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
	 * Check whether the vmId has surpassed bound, if yes, reset vmId
	 * as 0.
	 */
	private void checkVmIdAvailable() {
		if (vmId >= vmQueue.size()) {
			currentTime++;
			vmId = 0;
			triedAllocationTimes = 0;
			DataCenterFactory.print.println("===currentTime:" + currentTime
					+ "===");
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
	private boolean checkResourceAvailble(VirtualMachine vm3,
										  PhysicalMachine pm3) {
		boolean allocateSuccess = true;
		boolean oneSlotAllocation;
		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() > vm3
					.getCpuTotal())
					&& (pm3.resource.get(t).getMemUtility() > vm3.getMemTotal())
					&& (pm3.resource.get(t).getStoUtility() > vm3
					.getStorageTotal());
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
	 * @param vm4s
	 * @param pm4
	 * @param incOrDec
	 */
	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4,
								int incOrDec) {
		if (incOrDec == decrease) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				pm4.resource.get(t)
						.setCpuUtility(
								pm4.resource.get(t).getCpuUtility()
										- vm4.getCpuTotal());
				pm4.resource.get(t)
						.setMemUtility(
								pm4.resource.get(t).getMemUtility()
										- vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(
						pm4.resource.get(t).getStoUtility()
								- vm4.getStorageTotal());
			}
			DataCenterFactory.print.println("Resource is updated(dec)");
		}
		if (incOrDec == increase) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				pm4.resource.get(t)
						.setCpuUtility(
								pm4.resource.get(t).getCpuUtility()
										+ vm4.getCpuTotal());
				pm4.resource.get(t)
						.setMemUtility(
								pm4.resource.get(t).getMemUtility()
										+ vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(
						pm4.resource.get(t).getStoUtility()
								+ vm4.getStorageTotal());
			}
			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo()
					+ " from DataCenter" + vm4.getDataCenterNo() + " Rack"
					+ vm4.getRackNo() + " PM" + pm4.getNo());
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
	private void processDeleteQueue(int p_currentTime,
									ArrayList<VirtualMachine> p_deleteQueue) {
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
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf()
							.get(rackNo).getPmQueueOne().get(pmNo), increase);
				} else if (pmNo >= pmQueueOneSize
						&& pmNo < pmQueueOneSize + pmQueueTwoSize) {
					updateResource(
							vm5,
							arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo)
									.getPmQueueTwo().get(pmNo - pmQueueOneSize),
							increase);
				} else {
					updateResource(
							vm5,
							arr_dc.get(dataCenterNo)
									.getArr_lbf()
									.get(rackNo)
									.getPmQueueThree()
									.get(pmNo - pmQueueOneSize
											- pmQueueThreeSize), increase);
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
	 * @param pmQueueOneSize
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
		for (int i = 0; i < pmTotalNum; i++) {
			vm2 = p_vmQueue.get(i);
			//计算单个VM的最大CM值
			capacity_makespan = (int) ((vm2.getEndTime() - vm2.getStartTime()) * vm2.getCpuTotal());
			if (capacity_makespan > maxCapacity_makespan) {
				maxCapacity_makespan = capacity_makespan;
			}
			averageCapacity_makespan += capacity_makespan;
		}
		//根据pm个数，计算平均的cm
		averageCapacity_makespan /= m;
		//计算分割之后的CM值，k值选1/4;
		partitionCapacity_makespan = maxCapacity_makespan > averageCapacity_makespan ?
				maxCapacity_makespan : averageCapacity_makespan;
		partitionCapacity_makespan /= 4;

		DataCenterFactory.print.println("PCM:" + partitionCapacity_makespan);

		/*
		 * Recombine the requests;重新组合请求
		 */
		for (int i = 0; i < p_vmQueue.size(); i++) {
			vm2 = p_vmQueue.get(i);
			regenVM(vm2, partitionCapacity_makespan, cmp_vmQueue);
		}
		DataCenterFactory.print.println("Partition Times:" +
				(cmp_vmQueue.size() - p_vmQueue.size()));
		//Results have been tested to be right
		//System.out.println(cmp_vmQueue.size());
		return cmp_vmQueue;
	}


	/**
	 * Recursive method to generate new VM queue, if a VM capacity_makespan is less
	 * than the partitionCapacity_makespan, add the VM to new queue. If not, divide
	 * the vm into more than one vms and put them to the new queue.
	 */
	public void regenVM(VirtualMachine vm2,
						int partitionCapacity_makespan, ArrayList<VirtualMachine> cmp_vmQueue) {

		int capacity_makespan = 0;
		VirtualMachine vm3, vm4;
		int duration;
		capacity_makespan = (int) ((vm2.getEndTime() - vm2.getStartTime()) * vm2.getCpuTotal());
		if (capacity_makespan <= partitionCapacity_makespan) {
			vm3 = new VirtualMachine(
					vmID++, vm2.getStartTime(), vm2.getEndTime(), vm2.getVmType());
			cmp_vmQueue.add(vm3);
		} else {
			duration = (int) ((capacity_makespan - partitionCapacity_makespan) / vm2.getCpuTotal());
			vm3 = new VirtualMachine(vmID++, vm2.getStartTime(),
					vm2.getStartTime() + duration, vm2.getVmType());
			cmp_vmQueue.add(vm3);
			vm4 = new VirtualMachine(vmID, vm3.getEndTime() + 1, vm2.getEndTime(), vm2.getVmType());
			//recursive method
			regenVM(vm4, partitionCapacity_makespan, cmp_vmQueue);

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



