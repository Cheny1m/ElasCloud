package com.schedule.loadbalance;

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
public class  PrepartitionON2 extends OnlineAlgorithm {

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
    VirtualMachine vm;

    //设置f
    double f = 0.125;
    double CMb = 0.67;
    float CurrVMCapacityMakespn;

    ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
    ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
    ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

    //当前活跃物理机数量
    ArrayList<PhysicalMachine> EffePMQueue = new ArrayList<PhysicalMachine>();

    int pmQueueOneSize;
    int pmQueueTwoSize;
    int pmQueueThreeSize;
    private float CM_oldmin1;
    private float CM_oldmin2;
    private float CM_newmin;

    //当前时刻开启的物理机
    int x;

    public PrepartitionON2() {
        //	System.out.println(getDescription());
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return description + "-PrepartitionON2 Algorithm---";
    }

    @Override
    public void allocate(ArrayList<VirtualMachine> p_vmQueue, ArrayList<DataCenter> p_arr_dc) {
        // TODO Auto-generated method stub
        DataCenterFactory.print.println(getDescription());
        this.arr_dc = p_arr_dc;

        pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().size();
        pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().size();
        pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().size();

        pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
        int allocatedDataCenterID;
        int allocatedRackID;

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

            //计算此VM的CM值
            CurrVMCapacityMakespn = vm.getVmDuration() * vm.getCpuTotal();

            //PM优先级
            //index = 0;
            index %= pmTotalNum;
            if (vm.getVmType() > 0 && vm.getVmType() < 4) {
                //按PM排序;此处平均利用率最大，即代表着平均的CM容量最大；因为各个PM的CM相同，总工作时间也相同
                //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByPMUtility());
                //优先分配给CM最低的PM
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(), new SortByCapacityMakespan());
                CM_newmin = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index+1).getTotalCapacityMakespan();
                x = NumberOfEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(),currentTime);

                //计算最小的CM值
                CM_oldmin1 = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index).getTotalCapacityMakespan();
                CM_oldmin2 = CM_oldmin1 + CurrVMCapacityMakespn;
                //计算当前最小的CM
                if(CM_oldmin2 < CM_newmin) CM_newmin = CM_oldmin2;

                if((CM_oldmin2/CM_newmin) > (1+f) || CM_oldmin2 > CMb){
                    //计算开启的物理机数量
                    FlatVM(vm,EffePMQueue);
                    //清空活跃物理机队列
                    EffePMQueue.clear();
                }
                else allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne().get(index));
            } else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
                //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortByPMUtility());
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(), new SortByCapacityMakespan());
                CM_newmin = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index+1).getTotalCapacityMakespan();
                x = NumberOfEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo(),currentTime);
                allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueTwo().get(index));
            } else {
                //Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortByPMUtility());
                Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(), new SortByCapacityMakespan());
                CM_newmin = arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index+1).getTotalCapacityMakespan();
                x = NumberOfEffePM(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree(),currentTime);
                allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueThree().get(index));
            }
        }
        //sortAllPMsInOrder(p_arr_dc);
        DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
    }


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
                System.out.println("VM number is too large, PM number is not enough");
                JOptionPane.showMessageDialog(null, "VM number is too large, PM number is not enough", "Error", JOptionPane.OK_OPTION);
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

    private void FlatVM(VirtualMachine vm2,ArrayList<PhysicalMachine> Effe_PMQueue){
        VirtualMachine vm3;
        int duration;
        duration = (int) Math.ceil(vm2.getVmDuration()*1.0 / x);
        for(int i =0 ;i < x ; i++ ){
            if(i == 0){
                vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration,vm2.getStartTime()+(i+1)*duration,vm2.getVmType());
            }
            else if(i == x -1){
                vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration+1,vm2.getEndTime(),vm2.getVmType());
            }
            else{
                vm3 = new VirtualMachine(vmID++ , vm2.getStartTime()+i*duration+1,vm2.getStartTime()+(i+1)*duration,vm2.getVmType());
            }
            allocateVm(0, 0, vm3, Effe_PMQueue.get(i));
        }
    }


    public int NumberOfEffePM(ArrayList<PhysicalMachine> pmQueue,int currTime){
        int count = 0;
        for(int i=0 ; i < pmQueue.size() ; i++){
            //如果此时活跃，活跃物理机加一
            if(pmQueue.get(i).resource.get(currTime).getCpuUtility() != pmQueue.get(i).getCpuTotal()){
                EffePMQueue.add(pmQueue.get(i));
                count++;
            }
        }
        return count;
    }
}


class SortByCapacityMakespan implements Comparator<PhysicalMachine> {
    public int compare(PhysicalMachine o1, PhysicalMachine o2){
        PhysicalMachine pm1 = o1;
        PhysicalMachine pm2 = o2;
        if(pm1.getTotalCapacityMakespan() > pm2.getTotalCapacityMakespan())
            return 1;
        return 0;
    }
}


