package com.comparedindex;

import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;

import java.util.ArrayList;

public class CalTotalTurnTime extends ComparisonIndex {
    private float TurnonTime = 0.0f;

    private float addTurnonTime = 0.0f;

    private float rackTurnonTime = 0.0f;
    private float dataCenterTurnonTime = 0.0f;
    public static float wholeSystemTurnonTime = 0.0f;

    ArrayList<PhysicalMachine> pq1, pq2, pq3;

    ArrayList<DataCenter> arr_dc;
    ArrayList<LoadBalanceFactory> arr_lbf;
    public CalTotalTurnTime(CalAverageUtility ca){
        calQueueTotalTurnTime(ca.pq1);
        calQueueTotalTurnTime(ca.pq2);
        calQueueTotalTurnTime(ca.pq3);
    }

    public CalTotalTurnTime(ArrayList<DataCenter> p_arr_dc) {
        this.arr_dc = p_arr_dc;
        for (DataCenter dc : arr_dc) {
            arr_lbf = dc.getArr_lbf();
            for (LoadBalanceFactory lbf : arr_lbf) {
                this.pq1 = lbf.getPmQueueOne();
                this.pq2 = lbf.getPmQueueTwo();
                this.pq3 = lbf.getPmQueueThree();
                TurnonTime = 0.0f;

                calQueueTotalTurnTime(pq1);
                calQueueTotalTurnTime(pq2);
                calQueueTotalTurnTime(pq3);

                rackTurnonTime += TurnonTime;
            }
            dataCenterTurnonTime += rackTurnonTime;
            rackTurnonTime = 0;
        }
        wholeSystemTurnonTime = dataCenterTurnonTime;
    }

    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return "TotalTurnTime: ";
    }

    @Override
    public float getIndexValue() {
        // TODO Auto-generated method stub
        return wholeSystemTurnonTime;
    }

    private void calQueueTotalTurnTime(ArrayList<PhysicalMachine> pq1){
        for(int i = 0; i < pq1.size(); i++){
            TurnonTime += pq1.get(i).getEffectiveSlot();
        }
    }

    private float calAddTurnonTime(float p_TurnonTime){
        return addTurnonTime += p_TurnonTime;
    }

    public float getWholeSystemTurnonTime(){
        return wholeSystemTurnonTime;
    }

}
