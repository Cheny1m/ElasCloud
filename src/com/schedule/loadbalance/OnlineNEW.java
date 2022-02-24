package com.schedule.loadbalance;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
预先设定所有物理机在调度考虑时长范围内任何一天（24小时）的利用率上限为85%，
只要在任何时间区间内利用率未超过85%该物理机就可继续接收虚拟机，但若有超过85%的区间，
就选择一个介于该区间的任务进行迁移（迁移的原则是尽量使得该区间的利用率最大化接近85%）
*/
public class OnlineNEW extends OfflineAlgorithm {
    int dataCenterIndex; // Selected data center ID
    int rackIndex; // Selected rack ID
    int index = 0;    //Allocated PM ID
    //    int SumPMOne = 1; //开始首先开启一台PM;记录当前已经开启的PM数量；新分配物理机+1；当物理机中没有程序使，关闭物理机
//    int SumPMTwo = 1;
//    int SumPMThree = 1;
    int[] SumPM = new int[]{1,1,1};

    int allocatedDataCenterID;
    int allocatedRackID;

    //创建活跃PM列表与空闲PM列表
    ArrayList<PhysicalMachine> ActiveOneQueue = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> FreeOneQueue = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> ActiveTwoQueue = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> FreeTwoQueue = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> ActiveThreeQueue = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> FreeThreeQueue = new ArrayList<PhysicalMachine>();


    int currentTime = 0;
    int vmId = 0;    //vmId is the id in sorted in vmQueue
    int pmTotalNum;//PM总量
    int triedAllocationTimes = 0;
    int increase = 1;
    int decrease = -1;
    VirtualMachine vm;

    //某一个任务区间内的活跃物理机数量
    int EffeNumb = 0;
    //总迁移数
    int MigNumb;
    int TotalMigNumb = 0;

    ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
    ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
    ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

    int pmQueueOneSize, pmQueueTwoSize, pmQueueThreeSize;

    public OnlineNEW() {
    }

    @Override
    public String getDescription() {
        return description + "-Load&Energy Algorithm---";
    }

    @Override
    public void createVM(DataCenterFactory dcf) {
        //请求按最早开始时间进行排序;同开始时间按最长处理时间排序
        //dcf.createVM(new CreateVMByPorcessTime(new CreateLLNLRequests()));
        //dcf.createVM(new CreateVM());

    }


    @Override
    //分配
    public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
        DataCenterFactory.print.println(getDescription());
        this.vmQueue = p_vmQueue;
        this.arr_dc = p_arr_dc;
        pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
        pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
        pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();
        pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;

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
                //按此任务区间中，剩余的CM排序
                //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByRemainCPU());
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortDEByCapacityMakespan());
                //记录在此区间内活跃的物理机数量
                EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne());
//                if(ActiveOneQueue.isEmpty()){
//                    //初始化PM活跃队列
//                    ActiveOneQueue.add(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
//                }
//                //对活跃队列按照当前利用率排序
//                Collections.sort(ActiveOneQueue,new SortByPMCPUUtility());
                allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
            } else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortDEByCapacityMakespan());
                EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo());
                allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
            } else {
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortDEByCapacityMakespan());
                EffeNumb = NumberofSpanEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree());
                allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
            }
        }

    }

    private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2, PhysicalMachine pm2) {
        // TODO Auto-generated method stub
        //判断那一时刻该PM资源是否够用且满足静态阈值
        if (checkResourceAvailble(vm2, pm2)) {
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
        } else {
            //否则尝试其它已经开启的物理机
            if (triedAllocationTimes == pmTotalNum) {
                //当数据中心的物理机已经不够用时；抛出异常
                System.out.println("VM number is too large, PM number is not enough");
                JOptionPane.showMessageDialog(null, "VM number is too large, PM number is not enough", "Error", JOptionPane.OK_OPTION);
                throw new IllegalArgumentException("PM too less");
            } else {
                //如果当前还有物理机能接纳此任务
                if(index < EffeNumb){
                    triedAllocationTimes++;
                    DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
                    index++; // Try another PM
                }
                else{
                    //否则将此区间按照尽量满足85%迁移
                    MigNumb += MigrateAllocate(vm2,pm2);
                    TotalMigNumb += MigNumb;
                    DataCenterFactory.print.println("当前总迁移数： " + TotalMigNumb);
                    //调整VMID
                    //DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
                    //index++; // Try another PM
                    //如果当前pm索引超过了当前物理机开启的PM总量;采用迁移方式
//                if(index >= SumPM[ActivePMNo] ){
//                    //开启相应物理机队列的新物理机，同时更新开启列表
//                    SumPM[ActivePMNo]++;
//                    System.out.println("活跃的物理机已经用完，开启新的物理机");
//                    //将此任务分配给此新物理机
//                }
                }
            }
        }
    }

    private void checkVmIdAvailable() {
        //如果虚拟机序号大于请求队列，则时间加一（时间累加器）；从头开始循环寻找
        if (vmId >= vmQueue.size()) {
            currentTime++;
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
            oneSlotAllocation = (pm3.resource.get(t).getCpuUtility()  - vm3.getCpuTotal() >= pm3.getCpuTotal() * 0.12);
            allocateSuccess = allocateSuccess && oneSlotAllocation;

            if (false == allocateSuccess) {
                // If allocated failed, return exactly.
                return allocateSuccess;
            }
        }
        return allocateSuccess;
    }


    //返回迁移数
    private int MigrateAllocate(VirtualMachine vm3, PhysicalMachine pm3){
        int MigNumb = -1;
        boolean flag;
        boolean change;//false代表是迁移区间，true代表是可分配区间
        int starttime = vm3.getStartTime();
        //记录第一秒的资源是否够用
        change = (pm3.resource.get(starttime).getCpuUtility()  - vm3.getCpuTotal() >= pm3.getCpuTotal() * 0.12);
        for (int t = vm3.getStartTime(); t <= vm3.getEndTime(); t++) {
            //查找当前slot区间的状态
            flag = (pm3.resource.get(t).getCpuUtility()  - vm3.getCpuTotal() >= pm3.getCpuTotal() * 0.12);
            //如果两者状态相同且不是最后的一秒；那么继续累加区间
            if(flag == change && t != vm.getEndTime()){
                //只要不是把区间分割完成
                continue;
            }
            else{
                //否则出现了迁移断点
                MigNumb++;
                //将此区间作为一个新区间分配
                VirtualMachine vm4 = new VirtualMachine(vm3.getVmNo(),starttime,t,vm3.getVmType());
                if(change){
                    DataCenterFactory.print.println("直接分配部分区间");
                    //如果上个区间是可分配区间，那么就直接分配，同时更新资源
                    AdvanceUpdateResource(pm3,vm4);
                    //allocateVm(allocatedDataCenterID, allocatedRackID, vm4, pm3);
                }
                else{
                    DataCenterFactory.print.println("迁移区间查找新PM分配");
                    //如果上个区间是迁移区间，那么重新查找下一个PM进行分配
                    //迁移数加一
                    //VirtualMachine vm4 = new VirtualMachine(vm3.getVmNo()+MigNumb-1,starttime,t,vm3.getVmType());
                    index++;
                    triedAllocationTimes++;
                    if(pm3.getPmType() == 1 ){
                        allocateVm(allocatedDataCenterID,allocatedRackID,vm4,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
                    }
                    else if (pm3.getPmType() == 2){
                        allocateVm(allocatedDataCenterID,allocatedRackID,vm4,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
                    }
                    else{
                        allocateVm(allocatedDataCenterID,allocatedRackID,vm4,arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
                    }
                    //vmId--;
                    //allocateVm(allocatedDataCenterID, allocatedRackID, vm4, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
                }
                //修改下个区间的是否为迁移区间的标识
                change = !change;
                //重新记录此区间开始时间
                starttime = t;
            }
        }
        //从队列中移除VM
        vmQueue.remove(vm3);

        //处理下一台虚拟机
        //vmId++;
        //将计数器清零
        triedAllocationTimes = 0;
        //检查
        checkVmIdAvailable();
        index = 0;

        return MigNumb;
    }

    private void AdvanceUpdateResource(PhysicalMachine pm3,VirtualMachine vm3) {
        DataCenterFactory.print.println("Allocate:VM" + vm3.getVmNo() + " " +"to" +" PM" + pm3.getNo());
        deleteQueue.add(vm3);
        pm3.vms.add(vm3);
        //设置此任务分配至的物理机NO；机架NO；数据中心NO
        vm3.setPmNo(pm3.getNo());
        vm3.setRackNo(allocatedRackID);
        vm3.setDataCenterNo(allocatedDataCenterID);

        updateResource(vm3, pm3, decrease);
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
}

//从大到小排序
class SortDEByCapacityMakespan implements Comparator<PhysicalMachine> {
    public int compare(PhysicalMachine o1, PhysicalMachine o2){
        PhysicalMachine pm1 = o1;
        PhysicalMachine pm2 = o2;
        if(pm1.getTotalCapacityMakespan() < pm2.getTotalCapacityMakespan())
            return 1;
        return 0;
    }
}






