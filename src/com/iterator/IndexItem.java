package com.iterator;

import java.util.ArrayList;

import com.comparedindex.CalAverageUtility;
import com.comparedindex.CalCapacityMakespan;
import com.comparedindex.CalEffectivePM;
import com.comparedindex.CalEnergyConsumption;
import com.comparedindex.CalImbalanceDegree;
import com.comparedindex.CalMakespan;
import com.comparedindex.CalProcessTime;
import com.comparedindex.CalRejectedNum;
import com.comparedindex.CalSkewCapacityMakespan;
import com.comparedindex.CalSkewMakespan;
import com.datacenter.DataCenter;
import com.resource.PhysicalMachine;

/**
 * Comparison indices aggregation, which would add all comparison indices
 * into Iterator according to selection setting from interface.
 * @author Minxian
 *
 */
public class IndexItem {

    private boolean averageUility = false;
    private boolean imbalanceDegree = false;
    private boolean makespan = false;
    private boolean skew_makespan = false;
    private boolean capacity_makespan = false;
    private boolean skew_capaciy_makespan = false;
    private boolean energyConsumption = false;
    private boolean effectivePM = false;
    private boolean rejectedVMNum = false;
    private boolean processTime = false;
    ArrayList<ComparisonIndex> aci = new ArrayList<ComparisonIndex>();
    ArrayList<PhysicalMachine> pq1 = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> pq2 = new ArrayList<PhysicalMachine>();
    ArrayList<PhysicalMachine> pq3 = new ArrayList<PhysicalMachine>();
    ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();

    /**
     * All child indices class can be selected into aggregation group 
     */
    public IndexItem(ArrayList<PhysicalMachine> pq1,
            ArrayList<PhysicalMachine> pq2,
            ArrayList<PhysicalMachine> pq3) {
        //Since all Cal* indices would use CalAverageUtility, here
        //an instance of it earlier to reduce more instances.
        this.pq1 = pq1;
        this.pq2 = pq2;
        this.pq3 = pq3;

    }
    //Online indices
    public IndexItem(ArrayList<DataCenter> arr_dc){
        this.arr_dc = arr_dc;
    }
    
    public boolean isAverageUility() {
        return averageUility;
    }

    public void setAverageUility(boolean averageUility) {
        this.averageUility = averageUility;
    }

    public boolean isImbalanceDegree() {
        return imbalanceDegree;
    }

    public void setImbalanceDegree(boolean imbalanceDegree) {
        this.imbalanceDegree = imbalanceDegree;
    }

    public boolean isMakespan() {
        return makespan;
    }

    public void setMakespan(boolean makespan) {
        this.makespan = makespan;
    }

    public boolean isSkew_makespan() {
        return skew_makespan;
    }

    public void setSkew_makespan(boolean skew_makespan) {
        this.skew_makespan = skew_makespan;
    }

    public boolean isCapacity_makespan() {
        return capacity_makespan;
    }

    public void setCapacity_makespan(boolean capacity_makespan) {
        this.capacity_makespan = capacity_makespan;
    }

    public boolean isSkew_capaciy_makespan() {
        return skew_capaciy_makespan;
    }

    public void setSkew_capaciy_makespan(boolean skew_capaciy_makespan) {
        this.skew_capaciy_makespan = skew_capaciy_makespan;
    }
    //Offline indices

    public boolean isEnergyConsumption() {
        return energyConsumption;
    }

    public void setEnergyConsumption(boolean energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public boolean isEffectivePM() {
        return effectivePM;
    }

    public void setEffectivePM(boolean effectivePM) {
        this.effectivePM = effectivePM;
    }

    public boolean isRejectedVMNum() {
        return rejectedVMNum;
    }

    public void setRejectedVMNum(boolean rejectedVMNum) {
        this.rejectedVMNum = rejectedVMNum;
    }

    public boolean isProcessTime() {
        return processTime;
    }

    public void setProcessTime(boolean processTime) {
        this.processTime = processTime;
    }

    public Iterator createIterator() {
        CalAverageUtility cau = new CalAverageUtility(arr_dc);

        if (isAverageUility()) {
            aci.add(cau);
        }
        if (isImbalanceDegree()) {
            aci.add(new CalImbalanceDegree(arr_dc));
        }
        if (isMakespan()) {
            aci.add(new CalMakespan(arr_dc));
        }
        if (isSkew_makespan()) {
            aci.add(new CalSkewMakespan(cau));
        }
        if (isCapacity_makespan()) {
            aci.add(new CalCapacityMakespan(arr_dc));
        }
        if (isSkew_capaciy_makespan()) {
            aci.add(new CalSkewCapacityMakespan(cau));
        }

        //Offline indices
        if (isEnergyConsumption()) {
            aci.add(new CalEnergyConsumption(pq1, pq2, pq3));
        }
        if (isEffectivePM()) {
            aci.add(new CalEffectivePM(arr_dc));
        }
        if (isRejectedVMNum()) {
            aci.add(new CalRejectedNum(pq1, pq2, pq3));
        }
        if (isProcessTime()) {
            aci.add(new CalProcessTime(arr_dc));
        }
        return new IndexIterator(aci);

    }
}
