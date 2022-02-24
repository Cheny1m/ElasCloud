

/*

在线版本于2022年1月25日

截止至第四周周报


本算法是兼顾负载均衡与节能算法的迁移版本
算法在离线环境中运行，核心思想是利用已知的虚拟机VM信息，计算出利用率阈值f
只要任一物理机PM在区间内的利用率未超过阈值，那么就可以继续接受虚拟机

其中利用率阈值采用全区间方式计算

他的10次模拟结果为：
算法0:
0.5986203
0.018060606
1789.9
15607.3
0.69274014
13957.5
0.669713
0.68122655
 */

package com.schedule.loadbalance;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByPorcessTime;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.OfflineAlgorithm;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
预先设定所有物理机在调度考虑时长范围内任何一天（24小时）的利用率上限为85%，
只要在任何时间区间内利用率未超过85%该物理机就可继续接收虚拟机，但若有超过85%的区间，
就选择一个介于该区间的任务进行迁移（迁移的原则是尽量使得该区间的利用率最大化接近85%）
*/
public class SAEAlgorithm extends OnlineAlgorithm {
	int dataCenterIndex; // Selected data center ID
	int rackIndex; // Selected rack ID
	int index = 0;    //Allocated PM ID
	//    int SumPMOne = 1; //开始首先开启一台PM;记录当前已经开启的PM数量；新分配物理机+1；当物理机中没有程序使，关闭物理机
//    int SumPMTwo = 1;
//    int SumPMThree = 1;
	int[] SumPM = new int[]{1,1,1};

	int allocatedDataCenterID;
	int allocatedRackID;

	int aday = 288;

	//创建活跃PM列表与空闲PM列表
	ArrayList<PhysicalMachine> ActiveOneQueue = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> FreeOneQueue = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> ActiveTwoQueue = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> FreeTwoQueue = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> ActiveThreeQueue = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> FreeThreeQueue = new ArrayList<PhysicalMachine>();

	ArrayList<Integer> EffePMID = new ArrayList<Integer>();
	ArrayList<Integer> EffePMIndex = new ArrayList<Integer>();


	int currentTime = 0;
	int vmId = 0;    //vmId is the id in sorted in vmQueue
	int pmTotalNum;//PM总量
	int triedAllocationTimes = 0;
	int increase = 1;
	int decrease = -1;
	VirtualMachine vm;

	//设置阈值
	double f = 0.9;

	//某一个任务区间内的活跃物理机数量
	int EffeNumb = 0;
	//总迁移数
	int MigNumb;
	int TotalMigNumb = 0;
	boolean StatusChange = true;

	int CMBound;
	int NumberofAllocate = 0;

	int vmQueueSize;

	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

	int pmQueueOneSize, pmQueueTwoSize, pmQueueThreeSize;

	public SAEAlgorithm() {
	}

	@Override
	public String getDescription() {
		return description + "-Load&Energy Algorithm---";
	}


	@Override
	//分配
	public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
		DataCenterFactory.print.println(getDescription());
//		//计算总负载
//		int NumbOfPM = 0;
//		int SlotLoad = 0;
//		int TotalLoad = 0;
//		for(int i = CreateVM.minValue ; i < CreateVM.maxValue ; i++ ){
//			for(int j = 0 ; j < p_vmQueue.size() ; j++) {
//				if(p_vmQueue.get(j).getStartTime() <= i && p_vmQueue.get(j).getEndTime() > i){
//					SlotLoad += p_vmQueue.get(j).getCpuTotal();
//				}
//			}
//			if(SlotLoad > NumbOfPM) NumbOfPM = SlotLoad;
//			TotalLoad += SlotLoad;
//			SlotLoad = 0;
//		}
//		NumbOfPM = (int)(Math.ceil(NumbOfPM * 1.0 / p_arr_dc.get(0).getArr_lbf().get(0).getPmQueueOne().get(0).getCpuTotal()));
//		DataCenterFactory.print.println("至少需要的物理机数量为：" + NumbOfPM);
//
//		double threshold = TotalLoad * 1.0 / (NumbOfPM * (CreateVM.maxValue - CreateVM.minValue) * p_arr_dc.get(0).getArr_lbf().get(0).getPmQueueOne().get(0).getCpuTotal());
//		DataCenterFactory.print.println("阈值为：" + threshold);
//
//		f = threshold;
////        if(f < 0.5) f = 0.5;
//		CMBound =(int) Math.ceil(f * (CreateVM.maxValue - CreateVM.minValue) * p_arr_dc.get(0).getArr_lbf().get(0).getPmQueueOne().get(0).getCpuTotal());
//		DataCenterFactory.print.println("每个PM上的最大负载为：" + CMBound);

		//先处理长时间的
		//Collections.sort(p_vmQueue,new SortByProcessingTime());
		//同开始时间，先处理容量多的
		this.vmQueue = p_vmQueue;
		vmQueueSize = vmQueue.size();
		this.arr_dc = p_arr_dc;
		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();
		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;


		//初始化每个PM对列的第一台物理机为打开状态
		//arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index).setTurnStatus(true);

		DataCenterFactory.print.println("===currentTime:" + currentTime + "===");

		//按静态阈值依次分配PM
		while (!vmQueue.isEmpty()) {
			//查找当前时间可以分配的请求（VM）
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				//如果有此请求，则开始分配
				vm = vmQueue.get(vmId);
			} else {
				//如果排在前面的vm当前时间还未开始请求，则继续寻找
				vmId++;
				triedAllocationTimes = 0;
				//查看虚拟机队列是否已经找完
				checkVmIdAvailable();
				//如果找完了，当前时间+1，从头查找VM；如果没有继续查找下个VM
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
                /*
                //按此任务区间中，剩余的CM排序
                //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByRemainCPU());
                if(StatusChange == true){
                    //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortDEByCapacityMakespan());
                    //按PM利用率排序会拖慢程序速度
                    //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByDEPMUtility());
                    Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByDEPMCPUUtility());
                    //记录在此区间内活跃的物理机数量
                    //EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne());
                    EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(),currentTime);

                    if(EffeNumb == 0){
                        index = 0;
                        EffePMID.add(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(0).getNo());
                        EffePMIndex.add(0);
                        EffeNumb = 1;
                    }

                }
                StatusChange = true;
                */

//                if(ActiveOneQueue.isEmpty()){
//                    //初始化PM活跃队列
//                    ActiveOneQueue.add(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
//                }
//                //对活跃队列按照当前利用率排序
//                Collections.sort(ActiveOneQueue,new SortByPMCPUUtility());
				//查找当前第一个活跃物理机
//                while(!EffePMID.contains(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index).getNo())){
//                    index++;
//                }
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortByPMCPUUtility());
				EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
			} else {
				Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortByPMCPUUtility());
				EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
			}
		}

	}

	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2, PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		//判断那一时刻该PM资源是否够用且满足静态阈值
		if (checkResourceAvailble(vm2, pm2)) {
			if(checkCMBoundAvailble(vm2,pm2)){
				//如果资源能满足，直接分配
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

				//处理下一台虚拟机
				//vmId++;
				//将计数器清零
				vmId = 0;
				triedAllocationTimes = 0;
				//检查
				checkVmIdAvailable();
				index = 0;
			}
			//1.无迁移
			//2.超过整个负载的界限，实现迁移，同时关闭该物理机
			//3.方案3，超过当前负载的界限，实现迁移
			else{
				//首先采用方案2
				if(pm2.getTurnStatus()){
					DataCenterFactory.print.println("超过此物理机24小时负载的界限，实现迁移，同时暂时关闭物理机 PM " + pm2.getNo());
					MigrateAllocatebyCMBound(vm2,pm2);
				}
				else{
					if (triedAllocationTimes == pmTotalNum) {
						//当数据中心的物理机已经不够用时；抛出异常
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
		} else {
			//否则尝试其它已经开启的物理机
			if (triedAllocationTimes == pmTotalNum) {
				//当数据中心的物理机已经不够用时；抛出异常
				System.out.println("VM number is too large, PM number is not enough  --  " + getDescription());
				JOptionPane.showMessageDialog(null, "VM number is too large, PM number is not enough", "Error", JOptionPane.OK_OPTION);
				throw new IllegalArgumentException("PM too less");
			} else {
				//如果当前还有活跃物理机能接纳此任务
//                if(triedAllocationTimes < EffeNumb - 1){
				triedAllocationTimes++;
				DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
				index++; // Try another PM
				StatusChange = false;
//                }
//                else{
//                    //否则开启新物理机，然后将此区间按照尽量满足85%迁移
//                    DataCenterFactory.print.println("所有活跃物理机都不能接纳，进行迁移...");
//                    //查找如何迁移才能使该区间利用率最大化？；按理说为了负载能够更加均衡，应该尽量将可分配区间留给负载较小的物理机；||或者查找能接受此区间的最大值，尽量保持节能
//                    int BestIndex = longestIntervalAllocate(vm2,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne());
//                    //实现迁移
//                    DataCenterFactory.print.println("最适合迁移的PM编号为：" + arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(BestIndex).getNo());
//                    MigNumb += MigrateAllocate(vm2,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(BestIndex));
//                    //MigNumb += MigrateAllocate(vm2,pm2);
//                    TotalMigNumb = MigNumb;
//                    DataCenterFactory.print.println("当前总迁移数： " + TotalMigNumb);
//                    //调整VMID
//                    //DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
//                    //index++; // Try another PM
//                    //如果当前pm索引超过了当前物理机开启的PM总量;采用迁移方式
////                if(index >= SumPM[ActivePMNo] ){
////                    //开启相应物理机队列的新物理机，同时更新开启列表
////                    SumPM[ActivePMNo]++;
////                    System.out.println("活跃的物理机已经用完，开启新的物理机");
////                    //将此任务分配给此新物理机
////                }
//                }
			}
		}
	}

	private void checkVmIdAvailable() {
		//如果虚拟机序号大于请求队列，则时间加一（时间累加器）；从头开始循环寻找
		if (vmId >= vmQueue.size()) {
			currentTime++;
			//如果刚好满一天
			if(currentTime % aday == 0 ){
				for(int i = 0 ; i < arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();i++){
					arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(i).setTurnStatus(true);
				}
				DataCenterFactory.print.println("----设置所有物理机为开启状态----");
			}
			vmId = 0;
			triedAllocationTimes = 0;
			DataCenterFactory.print.println("===currentTime:" + currentTime + "===");
			//查找此时间结束的任务
			processDeleteQueue(currentTime, deleteQueue);
		}
	}

	private boolean checkResourceAvailble(VirtualMachine vm3, PhysicalMachine pm3) {
		boolean allocateSuccess = true;
		boolean oneSlotAllocation;
		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
			//资源分配后未超过85%的话
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility()  - vm3.getCpuTotal() >= 0);
			//oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() - vm3.getCpuTotal() >= pm3.getCpuTotal() * (1-f));
			allocateSuccess = allocateSuccess && oneSlotAllocation;

			if (false == allocateSuccess) {
				// If allocated failed, return exactly.
				return allocateSuccess;
			}
		}
		return allocateSuccess;
	}

	private boolean checkCMBoundAvailble(VirtualMachine vm3,PhysicalMachine pm3){
		boolean allocateSuccess = true;
		int starttime = 0;
//		if(pm3.vms.size() == 0 ) starttime = currentTime;
//		else starttime = pm3.vms.get(0).getStartTime();

		double TimebyDay = Math.ceil((currentTime - starttime)  * 1.0 / aday) ;
		if(TimebyDay == 0 ) TimebyDay = 1;

		CMBound = (int) Math.ceil(f * TimebyDay * aday * pm3.getCpuTotal());
		if(pm3.getCurrtimeLoad() + (vm3.getVmDuration() * vm3.getCpuTotal()) > CMBound){
			//如果分配超载
			allocateSuccess = false;
		}
		return allocateSuccess;
	}

	private void MigrateAllocatebyCMBound(VirtualMachine vm3, PhysicalMachine pm3){
		//可分配的区间，向下取整
		int RemainingDration = (int)(Math.floor((CMBound - pm3.getCurrtimeLoad()) / vm3.getCpuTotal()));
		//如果可分配区间为0
		if(RemainingDration == 0 ){
			//将vm3尝试下一个PM
			index++;
			triedAllocationTimes++;
			//DataCenterFactory.print.println("撤销关闭！");
			DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
		}
		else{
			if(RemainingDration == vm3.getVmDuration()){
				//直接分配所有
				DataCenterFactory.print.println("直接分配整个VM");
				allocateVm(allocatedDataCenterID,allocatedRackID,vm3,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
			}
			else{
				NumberofAllocate++;
				DataCenterFactory.print.println("当前迁移数为：" + NumberofAllocate);
				//剩余的新区间
				VirtualMachine vm4 = new VirtualMachine(vmQueueSize + NumberofAllocate,vm3.getStartTime() + RemainingDration,vm3.getEndTime(),vm3.getVmType());
				//分配当前区间
				vm3.setEndTime(vm3.getStartTime() + RemainingDration);
				vmQueue.add(vm4);
				allocateVm(allocatedDataCenterID,allocatedRackID,vm3,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
			}
			pm3.setTurnStatus(false);
		}
	}


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
				//将对应物理机每个时隙的资源进行更新
				pm4.resource.get(t).setCpuUtility(pm4.resource.get(t).getCpuUtility() + vm4.getCpuTotal());
				pm4.resource.get(t).setMemUtility(pm4.resource.get(t).getMemUtility() + vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(pm4.resource.get(t).getStoUtility() + vm4.getStorageTotal());
			}
			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo() + " from DataCenter" + vm4.getDataCenterNo() + " Rack" + vm4.getRackNo() + " PM" + pm4.getNo());
			DataCenterFactory.print.println("Resource is updated(inc)");

		}
	}

	private void processDeleteQueue(int p_currentTime, ArrayList<VirtualMachine> p_deleteQueue) {
		// TODO Auto-generated method stub
		VirtualMachine vm5;
		int pmNo;
		int dataCenterNo;
		int rackNo;

		//查找正在运行的VM是否在当前结束会结束
		for (int i = 0; i < p_deleteQueue.size(); i++) {
			//获得队列中的VM
			vm5 = p_deleteQueue.get(i);
			dataCenterNo = vm5.getDataCenterNo();
			rackNo = vm5.getRackNo();
			pmNo = vm5.getPmNo();

			//如果此VM在此刻结束
			if (p_currentTime >= vm5.getEndTime()) {
				//查找此VM是分配给那个队列的PM
				if (pmNo >= 0 && pmNo < pmQueueOneSize) {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueOne().get(pmNo), increase);
				} else if (pmNo >= pmQueueOneSize && pmNo < pmQueueOneSize + pmQueueTwoSize) {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueTwo().get(pmNo - pmQueueOneSize), increase);
				} else {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo).getPmQueueThree().get(pmNo - pmQueueOneSize - pmQueueTwoSize), increase);
				}
				//从运行队列中移除VM
				p_deleteQueue.remove(vm5);
			}
		}
	}




	//迁移分段
	private void VMSub(int dataCenterNo, int rackNo, PhysicalMachine pm6, VirtualMachine vm6,int SubTime){
		//将此虚拟机分成两部分，后一部分代替此虚拟机；更新资源；前一部分更新此虚拟机结束时间
		//后一部分，迁移的统一用-1作为VMNO
		VirtualMachine vm7 = new VirtualMachine(-1,SubTime,vm6.getEndTime(),vm6.getVmType());
		deleteQueue.add(vm7);
		pm6.vms.add(vm7);
		vm7.setPmNo(pm6.getNo());
		vm7.setRackNo(rackNo);
		vm7.setDataCenterNo(dataCenterNo);
		updateResource(vm7, pm6, decrease);
		//vmId不变
		//物理机的索引加1
		index++;
		//前一部分重新加入循环
		vm6.setEndTime(SubTime);
	}

	//剩余资源由小到大排序
	class SortByRemainCPU implements Comparator<PhysicalMachine> {
		@Override
		public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
			PhysicalMachine pm1 = p_pm1;
			PhysicalMachine pm2 = p_pm2;
			//按剩余资源对PM进行升序排列
			if (pm1.getRemainCPU(vm.getStartTime(), vm.getEndTime()) > pm2.getRemainCPU(vm.getStartTime(), vm.getEndTime())) {
				return 1;
			}
			return 0;
		}
	}

	//当前CPU利用率降序排序；多的先分配
	class SortByPMCPUUtility implements Comparator<PhysicalMachine> {

		@Override
		public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
			PhysicalMachine pm1 = p_pm1;
			PhysicalMachine pm2 = p_pm2;
			if (pm1.getAvgCpuUtility() < pm2.getAvgCpuUtility()) {
				return 1;
			}
			return 0;
		}
	}

	private int NumberofSpanEffePM(ArrayList<PhysicalMachine> pmQueue,int t){
		EffePMID.clear();
		EffePMIndex.clear();
		int count = 0;
		for(int i=0 ; i < pmQueue.size() ; i++){
			//如果活跃，活跃物理机加一
//			if(pmQueue.get(i).getRemainCPU(vm.getStartTime(), vm.getEndTime()) < pmQueue.get(i).getCpuTotal() * (vm.getEndTime() - vm.getStartTime())){
//				count++;
//			}
			if(pmQueue.get(i).resource.get(t).getCpuUtility() != pmQueue.get(i).getCpuTotal()){
				//添加此活跃物理机ID
				EffePMID.add(pmQueue.get(i).getNo());
				//添加此活跃物理机的索引
				EffePMIndex.add(i);
				count++;
			}
		}
		return count;
	}

	private int NumberofSpanEffePM(ArrayList<PhysicalMachine> pmQueue){
		int count = 0;
		for(int i=0 ; i < pmQueue.size() ; i++){
			//如果活跃，活跃物理机加一
			if(pmQueue.get(i).getRemainCPU(vm.getStartTime(), vm.getEndTime()) < pmQueue.get(i).getCpuTotal() * (vm.getEndTime() - vm.getStartTime())){
				count++;
			}
		}
		return count;
	}

	class SortByDEPMUtility implements Comparator<PhysicalMachine> {

		@Override
		public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
			PhysicalMachine pm1 = p_pm1;
			PhysicalMachine pm2 = p_pm2;
			if (pm1.getAvgUtility() < pm2.getAvgUtility()) {
				return 1;
			}
			return 0;
		}
	}

	class SortByDEPMCPUUtility implements Comparator<PhysicalMachine> {

		@Override
		public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
			PhysicalMachine pm1 = p_pm1;
			PhysicalMachine pm2 = p_pm2;
			//if (pm1.getAvgUtility(currentTime - CreateVM.minValue) < pm2.getAvgUtility(currentTime - CreateVM.minValue)) {
			if (pm1.getAvgUtility(currentTime) < pm2.getAvgUtility(currentTime)) {
				return 1;
			}
			return 0;
		}
	}
}

class SortByDEPMUtility implements Comparator<PhysicalMachine> {

	@Override
	public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
		PhysicalMachine pm1 = p_pm1;
		PhysicalMachine pm2 = p_pm2;
		if (pm1.getAvgUtility() < pm2.getAvgUtility()) {
			return 1;
		}
		return 0;
	}
}


//package com.schedule.loadbalance;
//
//import com.datacenter.DataCenter;
//import com.datacenter.DataCenterFactory;
//import com.datacenter.LoadBalanceFactory;
//import com.resource.PhysicalMachine;
//import com.resource.VirtualMachine;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Random;
//import javax.swing.JOptionPane;
//
///**
// * SAE algorithm is based Sina load balancing algorithms. The scheduling
// * process of SAE algorithm is allocating the VM to the PM with lowest load.
// *
// * @author Minxian
// *
// */
//public class SAEAlgorithm extends OnlineAlgorithm {
//
//	int dataCenterIndex; // Selected data center ID
//	int rackIndex; // Selected rack ID
//	int index; // Allocated PM ID
//	int currentTime = 0;
//	int vmId = 0; // vmId is the the id in sorted in vmQueue
//	int pmTotalNum;
//	int increase = 1;
//	int decrease = -1;
//	int triedAllocationTimes = 0;
//	Random random = new Random();
//	VirtualMachine vm;
//	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
//	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
//	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();
//	int pmQueueOneSize;
//	int pmQueueTwoSize;
//	int pmQueueThreeSize;
//
//	public SAEAlgorithm() {
//
//	}
//
//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return description + "-SAE Algorithm-----";
//	}
//
//	/**
//	 * Generate the random index and try to allocate VM to the PM with generated
//	 * index.
//	 */
//	@Override
//	public void allocate(ArrayList<VirtualMachine> vmQueue,
//			ArrayList<DataCenter> p_arr_dc) {
//		DataCenterFactory.print.println(getDescription());
//		this.vmQueue = vmQueue;
//		this.arr_dc = p_arr_dc;
//		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf()
//				.get(rackIndex).getPmQueueOne().size();
//		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf()
//				.get(rackIndex).getPmQueueTwo().size();
//		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf()
//				.get(rackIndex).getPmQueueThree().size();
//		// Dangerous codes
//		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
//		int allocatedDataCenterID;
//		int allocatedRackID;
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
//
//			Collections.sort(arr_dc, new SortByDataCenterUtility());
//			dataCenterIndex = 0;
//			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();
//
//			Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf(),
//					new SortByRackUtility());
//			rackIndex = 0;
//			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf()
//					.get(rackIndex).getLbf_ID();
//
//			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
//				Collections.sort(
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueOne(), new SortByPMUtility());
//				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
//						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//						.getPmQueueOne().get(index));
//			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
//				Collections.sort(
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueTwo(), new SortByPMUtility());
//				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
//						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//						.getPmQueueTwo().get(index));
//			} else {
//				Collections.sort(
//						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//								.getPmQueueThree(), new SortByPMUtility());
//				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
//						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
//						.getPmQueueThree().get(index));
//			}
//		}
//		sortAllPMsInOrder(p_arr_dc);
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
//			vmId++;
//			triedAllocationTimes = 0;
//			checkVmIdAvailable();
//			index = 0;
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
//				index++; // Try another PM
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
//
//	/**
//	 * Sorting the PM sequences to be the initial sequence.
//	 *
//	 * @param p_arr_dc
//	 */
//	public void sortAllPMsInOrder(ArrayList<DataCenter> p_arr_dc) {
//		Collections.sort(p_arr_dc, new SortByDataCenterID());
//		for (DataCenter dc : p_arr_dc) {
//			Collections.sort(dc.getArr_lbf(), new SortByRackID());
//			for (LoadBalanceFactory lbf : dc.getArr_lbf()) {
//				Collections.sort(lbf.getPmQueueOne(), new SortByPMID());
//				Collections.sort(lbf.getPmQueueTwo(), new SortByPMID());
//				Collections.sort(lbf.getPmQueueThree(), new SortByPMID());
//			}
//		}
//	}
//}
