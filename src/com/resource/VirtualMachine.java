package com.resource;

import com.specification.VmInfo;

/**
 * This class defines the attributes and operations of Virtual Machine
 * @author yuanliang, Minxian
 *
 */

//继承至Server对象且实现comparable接口用于自定义vm排序
public class VirtualMachine extends Server implements Comparable<VirtualMachine> {

    private int startTime;
    private int endTime;
    private float powerConsumption;
    private int pmNo;
    //虚拟机类型；亚马逊标准的1-8？
    private int vmType;
    private int pmType;
    //虚拟机编号
    private int vmNo;
    //资源调度中心的数据中心ID
    private int dataCenterNo;
    //数据中心的机架ID
    private int rackNo;
    //VmInfo类用来提取虚拟机的xml配置信息
    VmInfo vmInfo = new VmInfo();

    public VirtualMachine(int no, int startTime, int endTime, int vmType) {
        this.vmNo = no;
        this.startTime = startTime;
        this.endTime = endTime;
        //获取对应的虚拟机类型配置
        this.cpuTotal = vmInfo.getCpu(vmType);
        this.memTotal = vmInfo.getMem(vmType);
        this.storageTotal = vmInfo.getStorage(vmType);
        //pm类型
        this.pmType = (vmType - 1) / 3 + 1;
        this.vmType = vmType;
    }

    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() { return endTime; }
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    //任务时长
    public int getVmDuration() { return endTime - startTime; }

    public float getPowerConsumption() {
        return powerConsumption;
    }
    public void setPowerConsumption(float powerConsumption) {
        this.powerConsumption = powerConsumption;
    }

    /**
     * The VM should be corresponding to an allocated PM
     */
    public int getPmNo() {
        return pmNo;
    }
    public void setPmNo(int pmNo) {
        this.pmNo = pmNo;
    }

    public int getVmType() {
        return vmType;
    }
    public void setVmType(int vmType) {
        this.vmType = vmType;
    }

    public int getPmType() {
        return pmType;
    }
    public void setPmType(int pmType) {
        this.pmType = pmType;
    }

    public int getVmNo() {
        return vmNo;
    }
    public void setVmNo(int vmNo) {
        this.vmNo = vmNo;
    }

    public int getDataCenterNo() {
        return dataCenterNo;
    }
    public void setDataCenterNo(int dataCenterNo) {
        this.dataCenterNo = dataCenterNo;
    }

    public int getRackNo() {
        return rackNo;
    }
    public void setRackNo(int rackNo) {
        this.rackNo = rackNo;
    }

    //按照vm开始时间从小到大排序
    public int compareTo(VirtualMachine vm) {
        // TODO Auto-generated method stub
        if (this.startTime > vm.startTime) {
            return 1;
        } else if (this.startTime < vm.startTime) {
            return -1;
        } else {
            return 0;
        }
    }
}
