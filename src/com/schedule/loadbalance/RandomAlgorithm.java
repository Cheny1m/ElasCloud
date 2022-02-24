package com.schedule.loadbalance;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.PMBootor;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JOptionPane;

/**
 * Random scheduling algorithm: round-robin generating a index and try to
 * allocate VM to the generated index. If failed, try another index.
 *
 * @author Minxian
 *
 */
public class RandomAlgorithm extends OnlineAlgorithm {

	int dataCenterIndex = 0; // Selected data center ID
	int rackIndex = 0; // Selected rack ID
	int index; 	//Allocated PM ID
	int currentTime = 0;
	int vmId = 0;  	//vmId is the id in sorted in vmQueue
	int pmTotalNum;
	int increase = 1;
	int decrease = -1;
	int triedAllocationTimes = 0;
	VirtualMachine vm;

	//利用数据中心的方式
	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();

	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

	int pmQueueOneSize;
	int pmQueueTwoSize;
	int pmQueueThreeSize;
	int Saq = 0;

	String indexInfo = "RoundRobinIndex: ";

	ArrayList<PhysicalMachine> pmQueueOne = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueTwo = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueThree = new ArrayList<PhysicalMachine>();


	public RandomAlgorithm() {
		// System.out.println(getDescription());
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description + "-RoundRobin Algorithm-----";
	}

	/**
	 * Generate the random index and try to allocate VM to the PM with generated
	 * index.
	 */
	@Override
	public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
		// TODO Auto-generated method stub
		DataCenterFactory.print.println(getDescription());
		this.vmQueue = p_vmQueue;
		this.arr_dc = p_arr_dc;

		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();

		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
		int allocatedDataCenterID;
		int allocatedRackID;

		DataCenterFactory.print.println("===currentTime:" + currentTime + "===");

		while (!vmQueue.isEmpty()) {
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				vm = vmQueue.get(vmId);
			} else {
				vmId++;
				triedAllocationTimes = 0;
				checkVmIdAvailable();
				continue;
			}

			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();

			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getLbf_ID();

			index %= pmTotalNum;
			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
				//按PM排序;此处平均利用率最大，即代表着平均的CM容量最大；因为各个PM的CM相同，总工作时间也相同
				index %= pmQueueOneSize;
				allocateVm(allocatedDataCenterID,allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
				index %= pmQueueTwoSize;
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
			} else {
				index %= pmQueueThreeSize;
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
			}

		}
		//DataCenterFactory.print.println("拒绝个数为："+Saq+"  拒绝率为：" + Saq/pmTotalNum);
		DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
	}

	/**
	 * Key scheduling procedure for algorithm. Main procedures are as below: 1.
	 * Check whether resource of a PM is available. 2. If resource available,
	 * output success information. Put the VM to deleteQueue, and remove that VM
	 * from vmQueue. 3. Update available resource of PM.
	 *
	 * @param vm2
	 * @param pm2
	 */
	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2, PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		if (checkResourceAvailble(vm2, pm2)) {
			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " " + "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM" + pm2.getNo());
			deleteQueue.add(vm2);
			vmQueue.remove(vm2);
			pm2.vms.add(vm2);
			vm2.setPmNo(pm2.getNo());
			vm2.setRackNo(rackNo);
			vm2.setDataCenterNo(dataCenterNo);

			updateResource(vm2, pm2, decrease);

			vmId = 0;
			triedAllocationTimes = 0;
			checkVmIdAvailable();
			//index = 0;
			index++ ;
		} else {
			if (triedAllocationTimes == pmTotalNum) {
				System.out.println("VM number is too large, PM number is not enough");
//                Saq++;
//                vmQueue.remove(vm2);
//                vmId = 0;
//                triedAllocationTimes = 0;
//                checkVmIdAvailable();
//                index = 0;
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

	/**
	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
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
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() >= vm3
					.getCpuTotal())
					&& (pm3.resource.get(t).getMemUtility() >= vm3.getMemTotal())
					&& (pm3.resource.get(t).getStoUtility() >= vm3
					.getStorageTotal());
			allocateSuccess = allocateSuccess && oneSlotAllocation;
			// System.out.println(pm3.resource.get(t).getCpuUtility() +" "+
			// vm3.getCpuTotal());
			// System.out.println(pm3.resource.get(t).getMemUtility() +" "+
			// vm3.getMemTotal());
			// System.out.println(pm3.resource.get(t).getStoUtility() +" "+
			// vm3.getStorageTotal());

			if (false == allocateSuccess) {
				// If allocated failed, return exactly.
				return allocateSuccess;
			}
		}
		return allocateSuccess;
	}

	/**
	 * Update the available resource. When parameter 3 equals to increase,
	 * available resource would increased, else resource would be decreased.
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
			System.out.println("Resource is updated(dec)");
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
					+ " from PM" + pm4.getNo());
			DataCenterFactory.print.println("Resource is updated(inc)");

		}
	}

	/**
	 * After the VM has been added to deleteQueue, if end time comes, that VM
	 * should be removed from deleteQueue. Available resource should also be
	 * updated.
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
											- pmQueueTwoSize), increase);
				}
				p_deleteQueue.remove(vm5);
			}
		}
	}

}


////package com.schedule.loadbalance;
////
////import java.util.ArrayList;
////import java.util.Collections;
////import java.util.Comparator;
////
////import com.datacenter.DataCenter;
////import com.datacenter.DataCenterFactory;
////import com.datacenter.LoadBalanceFactory;
////import com.generaterequest.CreateLLNLRequests;
////import com.generaterequest.CreateVM;
////import com.generaterequest.CreateVMByPorcessTime;
////import com.generaterequest.PMBootor;
////import com.resource.PhysicalMachine;
////import com.resource.VirtualMachine;
////import com.schedule.loadbalance.*;
////import javax.swing.*;
/////*
////@author Yueming Chen
//// */
////
////public class RandomAlgorithm extends OnlineAlgorithm {
////	int dataCenterIndex; // Selected data center ID
////	int rackIndex; // Selected rack ID
////	int index; 	//Allocated PM ID
////	int currentTime = 0;
////	int vmId = 0;  	//vmId is the id in sorted in vmQueue
////	int pmTotalNum;
////	int increase = 1;
////	int decrease = -1;
////	int triedAllocationTimes = 0;
////	VirtualMachine vm;
////
////	float lowbound = 0.3f;
////	//利用数据中心的方式
////	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
////	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
////
////	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();
////
////	int pmQueueOneSize;
////	int pmQueueTwoSize;
////	int pmQueueThreeSize;
////
////	public RandomAlgorithm(){
////		//	System.out.println(getDescription());
////	}
////
////	@Override
////	public String getDescription() {
////		// TODO Auto-generated method stub
////		return description + "-GreedyBucketMigration Algorithm---";
////	}
////
////	/**
////	 * Generate the random index and try to allocate VM to the PM with generated
////	 * index.
////	 */
////
////	@Override
////	public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
////		// TODO Auto-generated method stub
////		DataCenterFactory.print.println(getDescription());
////
////
////		this.vmQueue = p_vmQueue;
////		this.arr_dc = p_arr_dc;
////
////		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
////		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
////		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();
////
////		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
////		int allocatedDataCenterID;
////		int allocatedRackID;
////
////		DataCenterFactory.print.println("===currentTime:" + currentTime + "===");
////		while (!vmQueue.isEmpty()) {
////			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
////				vm = vmQueue.get(vmId);
////			} else {
////				vmId++;
////				triedAllocationTimes = 0;
////				checkVmIdAvailable();
////				continue;
////			}
////
////			//随机寻找pm分配
////			//对数据中心按CM排序
////			Collections.sort(arr_dc, new SortByDataCenterCapacityMakespan());
////			dataCenterIndex = 0;
////			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();
////
////			//机架级别的排序
////			Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf(), new SortByRackCapacityMakespan());
////			rackIndex = 0;
////			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getLbf_ID();
////
////			index %= pmTotalNum;
////			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
////				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(),new SortByPMID());
////				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
////			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
////				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
////			} else {
////				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
////			}
////		}
////		DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
////	}
////
////	/**
////	 * Key scheduling procedure for algorithm. Main procedures are as below:
////	 * 1. Check whether resource of a PM is available.
////	 * 2. If resource available, output success information. Put the VM to deleteQueue, and remove that VM from vmQueue.
////	 * 3. Update available resource of PM.
////	 *
////	 * @param vm2
////	 * @param pm2
////	 */
////	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2, PhysicalMachine pm2) {
////		// TODO Auto-generated method stub
////		if (checkResourceAvailble(vm2, pm2)) {
////			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " " + "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM" + pm2.getNo());
////			deleteQueue.add(vm2);
////			vmQueue.remove(vm2);
////			pm2.vms.add(vm2);
////			vm2.setPmNo(pm2.getNo());
////			vm2.setRackNo(rackNo);
////			vm2.setDataCenterNo(dataCenterNo);
////
////			updateResource(vm2, pm2, decrease);
////
////			vmId = 0;
////			triedAllocationTimes = 0;
////			checkVmIdAvailable();
////			index = 0;
////		} else {
////			if (triedAllocationTimes == pmTotalNum) {
////				System.out.println("VM number is too large, PM number is not enough");
////				JOptionPane.showMessageDialog(null,
////						"VM number is too large, PM number is not enough",
////						"Error", JOptionPane.OK_OPTION);
////				throw new IllegalArgumentException("PM too less");
////			} else {
////				triedAllocationTimes++;
////				DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
////				index++; // Try another PM
////			}
////		}
////	}
////
////	/**
////	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
////	 */
////	private void checkVmIdAvailable() {
////		if (vmId >= vmQueue.size()) {
////			currentTime++;
////			vmId = 0;
////			triedAllocationTimes = 0;
////			DataCenterFactory.print.println("===currentTime:" + currentTime + "===");
////			//先对已经分配的再分配
////			DataCenterFactory.print.println("Re-allocationlowUtilPM......");
////			MiglowUtilPM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(),currentTime);
////			//在删除到期VM
////			processDeleteQueue(currentTime, deleteQueue);
////		}
////	}
////
////	/**
////	 * Check whether the left resource are available
////	 * @param vm3
////	 * @param pm3
////	 * @return
////	 */
////	private boolean checkResourceAvailble(VirtualMachine vm3,
////										  PhysicalMachine pm3) {
////		boolean allocateSuccess = true;
////		boolean oneSlotAllocation;
////		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
////			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() > vm3.getCpuTotal())
////					&& (pm3.resource.get(t).getMemUtility() > vm3.getMemTotal())
////					&& (pm3.resource.get(t).getStoUtility() > vm3.getStorageTotal());
////			allocateSuccess = allocateSuccess && oneSlotAllocation;
////
////			if (false == allocateSuccess) {
////				// If allocated failed, return exactly.
////				return allocateSuccess;
////			}
////		}
////		return allocateSuccess;
////	}
////
////	/**
////	 * Update the available resource. When parameter 3 equals to increase, available resource would increased, else resource would be decreased.
////	 * @param vm4
////	 * @param pm4
////	 * @param incOrDec
////	 */
////	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4,
////								int incOrDec) {
////		if (incOrDec == decrease) {
////			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
////				pm4.resource.get(t)
////						.setCpuUtility(
////								pm4.resource.get(t).getCpuUtility()
////										- vm4.getCpuTotal());
////				pm4.resource.get(t)
////						.setMemUtility(
////								pm4.resource.get(t).getMemUtility()
////										- vm4.getMemTotal());
////				pm4.resource.get(t).setStoUtility(
////						pm4.resource.get(t).getStoUtility()
////								- vm4.getStorageTotal());
////			}
////			DataCenterFactory.print.println("Resource is updated(dec)");
////		}
////		if (incOrDec == increase) {
////			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
////				pm4.resource.get(t)
////						.setCpuUtility(
////								pm4.resource.get(t).getCpuUtility()
////										+ vm4.getCpuTotal());
////				pm4.resource.get(t)
////						.setMemUtility(
////								pm4.resource.get(t).getMemUtility()
////										+ vm4.getMemTotal());
////				pm4.resource.get(t).setStoUtility(
////						pm4.resource.get(t).getStoUtility()
////								+ vm4.getStorageTotal());
////			}
////			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo()
////					+ " from DataCenter" + vm4.getDataCenterNo() + " Rack"
////					+ vm4.getRackNo() + " PM" + pm4.getNo());
////			DataCenterFactory.print.println("Resource is updated(inc)");
////
////		}
////	}
////
////	/**
////	 * After the VM has been added to deleteQueue, if end time comes, that VM should
////	 * be removed from deleteQueue. Available resource should also be updated.
////	 * @param p_currentTime
////	 * @param p_deleteQueue
////	 */
////	private void processDeleteQueue(int p_currentTime,
////									ArrayList<VirtualMachine> p_deleteQueue) {
////		// TODO Auto-generated method stub
////		VirtualMachine vm5;
////		int pmNo;
////		int dataCenterNo;
////		int rackNo;
////
////		for (int i = 0; i < p_deleteQueue.size(); i++) {
////			vm5 = p_deleteQueue.get(i);
////			dataCenterNo = vm5.getDataCenterNo();
////			rackNo = vm5.getRackNo();
////			pmNo = vm5.getPmNo();
////
////			if (p_currentTime >= vm5.getEndTime()) {
////				if (pmNo >= 0 && pmNo < pmQueueOneSize) {
////					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf()
////							.get(rackNo).getPmQueueOne().get(pmNo), increase);
////				} else if (pmNo >= pmQueueOneSize
////						&& pmNo < pmQueueOneSize + pmQueueTwoSize) {
////					updateResource(
////							vm5,
////							arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo)
////									.getPmQueueTwo().get(pmNo - pmQueueOneSize),
////							increase);
////				} else {
////					updateResource(
////							vm5,
////							arr_dc.get(dataCenterNo)
////									.getArr_lbf()
////									.get(rackNo)
////									.getPmQueueThree()
////									.get(pmNo - pmQueueOneSize
////											- pmQueueTwoSize), increase);
////				}
////				p_deleteQueue.remove(vm5);
////			}
////		}
////	}
////
////	public void MiglowUtilPM(ArrayList<PhysicalMachine> pmQueue,int time){
////		PhysicalMachine pm1, pm2;
////		VirtualMachine vm1;
////		boolean MigTorF = false;
////		for (int i = 0 ; i < pmQueue.size(); i++) {
////			//当前利用率从小到大
////			Collections.sort(pmQueue, new SortByCurrentUtility(time));
////			if (pmQueue.get(i).getCurrentUtility(time) > 0 && pmQueue.get(i).getCurrentUtility(time) <= lowbound) {
////				//如果有利用率小的
////				for (int j = pmTotalNum - 1; j > i; j--) {
////					if (arr_dc.get(0).getArr_lbf().get(0).getPmQueueOne().get(j).getCurrentUtility(time) <= 1 - lowbound) {
////						pm1 = pmQueue.get(i);
////						pm2 = pmQueue.get(j);
////						for(int k =0 ;k<pm1.vms.size();k++){
////							vm1 = pm1.vms.get(k);
////							if(vm1.getStartTime()<=time && vm1.getEndTime()>time){
////								//将剩下的区间迁移
////								VirtualMachine vm2 = new VirtualMachine(vm1.getVmNo(),time,vm1.getEndTime(),vm1.getVmType());
////								//原来的部分
////								VirtualMachine vm3= new VirtualMachine(vm1.getVmNo(),vm1.getStartTime(),time,vm1.getVmType());
////								if (checkResourceAvailble(vm2, pm2)) {
////									//指针更新
////									k--;
////									//移除原来的VM
////									pm1.vms.remove(vm1);
////									updateResource(vm1, pm1, increase);
////									deleteQueue.remove(vm1);
////									//添加
////									pm1.vms.add(vm3);
////									//deleteQueue.add(vm3);
////									vm3.setPmNo(pm1.getNo());
////									vm3.setRackNo(0);
////									vm3.setDataCenterNo(0);
////									//updateResource(vm3, pm1, decrease);
////									pm2.vms.add(vm2);
////									deleteQueue.add(vm2);
////									vm2.setPmNo(pm2.getNo());
////									vm2.setRackNo(0);
////									vm2.setDataCenterNo(0);
////									updateResource(vm2, pm2, decrease);
////
////									vmId = 0;
////									triedAllocationTimes = 0;
////									checkVmIdAvailable();
////									index = 0;
////								} else {
////									DataCenterFactory.print.println("Migration Failed!");
////								}
////							}
////						}
////						DataCenterFactory.print.println("This PM " + arr_dc.get(0).getArr_lbf().get(0).getPmQueueOne().get(j).getNo() + "Migration Finish!");
////						MigTorF = true;
////						break;
////					}
////				}
////			}
//////			else{
//////				if(pmQueue.get(i).getCurrentUtility(time) != 0){
//////					DataCenterFactory.print.println("Migration Finish!");
//////					break;
//////				}
//////			}
////		}
////		if(MigTorF == true) DataCenterFactory.print.println("Migration Finish!");
////		else DataCenterFactory.print.println("No PM need to be Migration!");
////	}
////
////}
//
////------------------------------------------------
//
//
//package com.schedule.loadbalance;
//
//import com.datacenter.DataCenter;
//import com.datacenter.DataCenterFactory;
//import com.resource.PhysicalMachine;
//import com.resource.VirtualMachine;
//import java.util.ArrayList;
//import java.util.Random;
//import javax.swing.JOptionPane;
//
///**
// * Random scheduling algorithm: randomly generating an index and try to allocate
// * VM to the generated index. If failed, try another index.
// *
// * @author Minxian
// *
// */
//public class RandomAlgorithm extends OnlineAlgorithm {
//
//	//数据中心索引
//	int dataCenterIndex; // Selected data center ID
//	//机架索引
//	int rackIndex; // Selected rack ID
//	//物理机索引
//	int index; // Allocated PM ID
//	//当前时间
//	int currentTime = 0;
//	//虚拟机ID
//	int vmId = 0; // vmId is the the id in sorted in vmQueue
//	//物理机总数
//	int pmTotalNum;
//	int increase = 1;
//	int decrease = -1;
//	//
//	int triedAllocationTimes = 0;
//	//内置随机
//	Random random = new Random();
//	VirtualMachine vm;
//	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
//	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
//	//移除
//	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();
//	int pmQueueOneSize;
//	int pmQueueTwoSize;
//	int pmQueueThreeSize;
//
//	public RandomAlgorithm() {
//
//	}
//
//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return description + "-Random Algorithm-----";
//	}
//
//	/**
//	 * Generate the random index and try to allocate VM to the PM with generated
//	 * index.
//	 */
//	@Override
//	public void allocate(ArrayList<VirtualMachine> vmQueue,
//			ArrayList<DataCenter> arr_dc) {
//
//		DataCenterFactory.print.println(getDescription());
//		this.vmQueue = vmQueue;
//		this.arr_dc = arr_dc;
//		//获取
//		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
//		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
//		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();
//		// Dangerous codes
//		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
//
//		while (!vmQueue.isEmpty()) {
//			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
//				vm = vmQueue.get(vmId);
//			} else {
//				vmId++;
//				triedAllocationTimes = 0;
//				checkVmIdAvailable();
//				continue;
//			}
//			dataCenterIndex = random.nextInt(arr_dc.size());
//			rackIndex = random.nextInt(arr_dc.get(dataCenterIndex).getArr_lbf().size());
//			index = random.nextInt(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmSize());
//			DataCenterFactory.print.println("本次随机值为：" + index);
//
//			if (index >= 0 && index < pmQueueOneSize) {
//				allocateVm(dataCenterIndex, rackIndex, vm,
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueOne().get(index));
//			} else if (index >= pmQueueOneSize
//					&& index < pmQueueTwoSize + pmQueueOneSize) {
//				allocateVm(dataCenterIndex, rackIndex, vm,
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueTwo().get(index));
//			} else {
//				allocateVm(dataCenterIndex, rackIndex, vm,
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueThree().get(index));
//			}
//		}
//		DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
//	}
//
//	/**
//	 * Key scheduling procedure for algorithm. Main procedures are as below: 1.
//	 * Check whether resource of a PM is available. 2. If resource available,
//	 * output success information. Put the VM to deleteQueue, and remove that VM
//	 * from vmQueue. 3. Update available resource of PM.
//	 *
//	 * @param vm2
//	 * @param pm2
//	 */
//	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2,
//			PhysicalMachine pm2) {
//		// TODO Auto-generated method stub
//		if (checkResourceAvailble(vm2, pm2)) {
//			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " "
//					+ "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM"
//					+ pm2.getNo());
//			deleteQueue.add(vm2);
//			vmQueue.remove(vm2);
//			pm2.vms.add(vm2);
//			vm2.setPmNo(pm2.getNo());
//			vm2.setRackNo(rackNo);
//			vm2.setDataCenterNo(dataCenterNo);
//
//			updateResource(vm2, pm2, decrease);
//
//			//vmId++;
//			vmId = 0;
//			triedAllocationTimes = 0;
//			checkVmIdAvailable();
//		} else {
//			if (triedAllocationTimes == pmTotalNum) {
//				System.out
//						.println("VM number is too large, PM number is not enough");
//				JOptionPane.showMessageDialog(null,
//						"VM number is too large, PM number is not enough",
//						"Error", JOptionPane.OK_OPTION);
//				throw new IllegalArgumentException("PM too less");
//			} else {
//				triedAllocationTimes++;
//				DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
//			}
//		}
//	}
//
//	/**
//	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
//	 */
//	private void checkVmIdAvailable() {
//		if (vmId >= vmQueue.size()) {
//			currentTime++;
//			vmId = 0;
//			triedAllocationTimes = 0;
//			DataCenterFactory.print.println("===currentTime:" + currentTime
//					+ "===");
//			processDeleteQueue(currentTime, deleteQueue);
//		}
//	}
//
//	/**
//	 * Check whether the left resource are available
//	 *
//	 * @param vm3
//	 * @param pm3
//	 * @return
//	 */
//	private boolean checkResourceAvailble(VirtualMachine vm3,
//			PhysicalMachine pm3) {
//		boolean allocateSuccess = true;
//		boolean oneSlotAllocation;
//		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
//			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() > vm3
//					.getCpuTotal())
//					&& (pm3.resource.get(t).getMemUtility() > vm3.getMemTotal())
//					&& (pm3.resource.get(t).getStoUtility() > vm3
//							.getStorageTotal());
//			allocateSuccess = allocateSuccess && oneSlotAllocation;
//
//			if (false == allocateSuccess) {
//				// If allocated failed, return exactly.
//				return allocateSuccess;
//			}
//		}
//		return allocateSuccess;
//	}
//
//	/**
//	 * Update the available resource. When parameter 3 equals to increase,
//	 * available resource would increased, else resource would be decreased.
//	 *
//	 * @param vm4s
//	 * @param pm4
//	 * @param incOrDec
//	 */
//	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4,
//			int incOrDec) {
//		if (incOrDec == decrease) {
//			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
//				pm4.resource.get(t)
//						.setCpuUtility(
//								pm4.resource.get(t).getCpuUtility()
//										- vm4.getCpuTotal());
//				pm4.resource.get(t)
//						.setMemUtility(
//								pm4.resource.get(t).getMemUtility()
//										- vm4.getMemTotal());
//				pm4.resource.get(t).setStoUtility(
//						pm4.resource.get(t).getStoUtility()
//								- vm4.getStorageTotal());
//			}
//			DataCenterFactory.print.println("Resource is updated(dec)");
//		}
//		if (incOrDec == increase) {
//			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
//				pm4.resource.get(t)
//						.setCpuUtility(
//								pm4.resource.get(t).getCpuUtility()
//										+ vm4.getCpuTotal());
//				pm4.resource.get(t)
//						.setMemUtility(
//								pm4.resource.get(t).getMemUtility()
//										+ vm4.getMemTotal());
//				pm4.resource.get(t).setStoUtility(
//						pm4.resource.get(t).getStoUtility()
//								+ vm4.getStorageTotal());
//			}
//			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo()
//					+ " from DataCenter" + vm4.getDataCenterNo() + " Rack"
//					+ vm4.getRackNo() + " PM" + pm4.getNo());
//			DataCenterFactory.print.println("Resource is updated(inc)");
//
//		}
//	}
//
//	/**
//	 * After the VM has been added to deleteQueue, if end time comes, that VM
//	 * should be removed from deleteQueue. Available resource should also be
//	 * updated.
//	 *
//	 * @param p_currentTime
//	 * @param p_deleteQueue
//	 */
//	private void processDeleteQueue(int p_currentTime,
//			ArrayList<VirtualMachine> p_deleteQueue) {
//		// TODO Auto-generated method stub
//		VirtualMachine vm5;
//		int pmNo;
//		int dataCenterNo;
//		int rackNo;
//
//		for (int i = 0; i < p_deleteQueue.size(); i++) {
//			vm5 = p_deleteQueue.get(i);
//			dataCenterNo = vm5.getDataCenterNo();
//			rackNo = vm5.getRackNo();
//			pmNo = vm5.getPmNo();
//
//			if (p_currentTime >= vm5.getEndTime()) {
//				if (pmNo >= 0 && pmNo < pmQueueOneSize) {
//					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf()
//							.get(rackNo).getPmQueueOne().get(pmNo), increase);
//				} else if (pmNo >= pmQueueOneSize
//						&& pmNo < pmQueueOneSize + pmQueueTwoSize) {
//					updateResource(
//							vm5,
//							arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo)
//									.getPmQueueTwo().get(pmNo - pmQueueOneSize),
//							increase);
//				} else {
//					updateResource(
//							vm5,
//							arr_dc.get(dataCenterNo)
//									.getArr_lbf()
//									.get(rackNo)
//									.getPmQueueThree()
//									.get(pmNo - pmQueueOneSize
//											- pmQueueThreeSize), increase);
//				}
//				p_deleteQueue.remove(vm5);
//			}
//		}
//	}
//}
