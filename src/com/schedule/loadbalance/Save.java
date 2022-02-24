package com.schedule.loadbalance;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.PMBootor;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * Random scheduling algorithm: round-robin generating a index and try to
 * allocate VM to the generated index. If failed, try another index.
 *
 * @author Minxian
 *
 */
public class Save extends OnlineAlgorithm {

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

    double highThreshold = 0.95;
    double lowThreshold = 0.2;
    int NumofMigrate = 0;

    int pmQueueOneSize;
    int pmQueueTwoSize;
    int pmQueueThreeSize;

    boolean allocationStatus = true;


    public Save() {
        // System.out.println(getDescription());
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return description + "-Save Algorithm-----";
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
                if(allocationStatus) Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne(),new SortByDEPMUtility());
                allocationStatus = false;
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
        if (checkResourceAvailble(vm2, pm2) && pm2.getAvgCpuUtility() < highThreshold) {
            DataCenterFactory.print.println("尝试分配...");
            pm2.vms.add(vm2);
            if(pm2.getAvgCpuUtility() < highThreshold){
                DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " " + "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM" + pm2.getNo());
                deleteQueue.add(vm2);
                vmQueue.remove(vm2);
                //pm2.vms.add(vm2);
                vm2.setPmNo(pm2.getNo());
                vm2.setRackNo(rackNo);
                vm2.setDataCenterNo(dataCenterNo);

                updateResource(vm2, pm2, decrease);

                vmId = 0;
                triedAllocationTimes = 0;
                checkVmIdAvailable();
                DataCenterFactory.print.println("检查是否有物理机上的任务需要迁移...");
                checkMigration(arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex).getPmQueueOne());
                index = 0;
                allocationStatus = true;
            }
            else{
                //先移除，尝试下一台物理机
                DataCenterFactory.print.println("尝试下一台物理机...");
                pm2.vms.remove(vm2);
                if (triedAllocationTimes == pmTotalNum) {
                    System.out.println("VM number is too large, PM number is not enough");
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
        } else {
            DataCenterFactory.print.println("不能完整分配，尝试下一台物理机...");
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


    //判断是否需要迁移
    private void checkMigration(ArrayList<PhysicalMachine> pmQueue){
        for(int i = 0 ; i < pmQueue.size() ; i ++){
            if(pmQueue.get(i).getCurrentUtility(currentTime) <= lowThreshold && pmQueue.get(i).getCurrentUtility(currentTime) > 0){
                double MigrationUtilization = calMigrationUtilization(pmQueue.get(i).getCurrentUtility(currentTime));
                DataCenterFactory.print.println("当前迁移概率为：" + MigrationUtilization);
                double r = Math.random() / 2.0 + 0.5;
                DataCenterFactory.print.println("当前随机数为：" + r);
                if(r < MigrationUtilization){
                    DataCenterFactory.print.println("执行迁移...");
                    for(int j = 0 ; j < pmQueue.get(i).vms.size() ; j++){
                        boolean status = false;
                        if(pmQueue.get(i).vms.get(j).getStartTime() <= currentTime && pmQueue.get(i).vms.get(j).getEndTime() > currentTime){
                            //执行迁移
                            VirtualMachine vmOld = pmQueue.get(i).vms.get(j);
                            VirtualMachine vmNew1 = new VirtualMachine(vmOld.getVmNo(),vmOld.getStartTime(),currentTime,vmOld.getVmType());
                            VirtualMachine vmNew2 = new VirtualMachine(vmOld.getVmNo(),currentTime,vmOld.getEndTime(),vmOld.getVmType());
                            for(int k = 0 ; k < pmQueue.size() ; k++){
                                if(pmQueue.get(k).getCurrentUtility(currentTime) > 0 && checkResourceAvailble(vmNew2, pmQueue.get(k))){
                                    NumofMigrate++;
                                    pmQueue.get(k).vms.add(vmNew2);
                                    pmQueue.get(i).vms.remove(vmOld);
                                    pmQueue.get(i).vms.add(vmNew1);
                                    DataCenterFactory.print.println("当前迁移数为：" + NumofMigrate);
                                    status = true;
                                    break;
                                }
                            }
                        }
                    if(status) break;
                    }
                }
            }
        }
    }


    //计算迁移概率
    private double calMigrationUtilization(double cpuUtility){
        double definiteIntegral = 0.166666667;
        return (-(cpuUtility * (1 - cpuUtility)/ definiteIntegral)) / 3.0 + 1;
    }
    /**
     * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
     */
    private void checkVmIdAvailable() {
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
     * @param vm4
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
}


