package com.comparedindex;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import java.util.ArrayList;

import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

/**
 * This class is used to calculate the longest process time in the whole system
 * @author Minxian
 *
 */
public class CalProcessTime extends ComparisonIndex {

    private float processTime = 0.0f;
    private float rackProcessTime = 0.0f;
    private float dataCenterProcessTime = 0.0f;
    private float wholeSystemProcessTime = 0.0f;
    ArrayList<PhysicalMachine> pq1, pq2, pq3;
    ArrayList<DataCenter> arr_dc;
    ArrayList<LoadBalanceFactory> arr_lbf;
    float maxProcessTime = 0.0f;
    float tempProcessTime = 0.0f;
    float averageProcessTime = 0.0f;

    public CalProcessTime(CalAverageUtility ca) {

        calQueueProcessTime(ca.pq1);
        calQueueProcessTime(ca.pq2);
        calQueueProcessTime(ca.pq3);

    }

    public CalProcessTime(ArrayList<DataCenter> p_arr_dc) {
        this.arr_dc = p_arr_dc;
        for (DataCenter dc : arr_dc) {
            arr_lbf = dc.getArr_lbf();
            for (LoadBalanceFactory lbf : arr_lbf) {
                this.pq1 = lbf.getPmQueueOne();
                this.pq2 = lbf.getPmQueueTwo();
                this.pq3 = lbf.getPmQueueThree();
                processTime = 0.0f;

                calQueueProcessTime(pq1);
                calQueueProcessTime(pq2);
                calQueueProcessTime(pq3);

                DataCenterFactory.print.println("ProcessTime:" + processTime);
                calRackProcessTime(processTime);
            }
            calDataCenterProcessTime(rackProcessTime);
        }
        wholeSystemProcessTime = dataCenterProcessTime;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return "ProcessTime: ";
    }

    @Override
    public float getIndexValue() {
        // TODO Auto-generated method stub
        return wholeSystemProcessTime;
    }

    public float getRackProcessTime() {
        return rackProcessTime;
    }

    public float getWholeSystemProcessTime() {
        return wholeSystemProcessTime;
    }
    /**
     * Calculate the process time
     * @param pq1
     */
    private void calQueueProcessTime(ArrayList<PhysicalMachine> pq1) {

        VirtualMachine vm1;
        for (int i = 0; i < pq1.size(); i++) {
            for (int j = 0; j < pq1.get(i).vms.size(); j++) {
                vm1 = pq1.get(i).vms.get(j);
                tempProcessTime = vm1.getVmDuration();
                if (tempProcessTime > maxProcessTime) {
                    maxProcessTime = tempProcessTime;
                }
            }
        }
        processTime = maxProcessTime;
    }
   
    public void calRackProcessTime(float p_processTime){
        if(p_processTime > rackProcessTime){
            rackProcessTime = p_processTime;
        }
    }

    public void calDataCenterProcessTime(float p_rack_ProcessTime){
        if(p_rack_ProcessTime > dataCenterProcessTime){
            dataCenterProcessTime = p_rack_ProcessTime;
        }
    }
}
