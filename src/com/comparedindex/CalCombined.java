package com.comparedindex;

import cloudscheinterface.ConfController;
import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.iterator.ComparisonIndex;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class CalCombined extends ComparisonIndex {
    float Enge = 0.5f;
    float Load = 1 - Enge;

    public static float alg1;
    //每个时间间隙所需要的机器数的总和
    public static float Opt1;
    //任务所需的最大CM
    public static float alg2;
    //任务总CM/开机的数量
    public static float Opt2;

    private float needCapacity_Makespan;
    private float needTurnOnTime;

    private float TurnonTime = 0.0f;

    private float RackTurnTime = 0;
    private float DataCenterTurnTime = 0;


    public static float CombinedResults;

    ArrayList<PhysicalMachine> pq1, pq2, pq3;

    ArrayList<DataCenter> arr_dc;
    ArrayList<LoadBalanceFactory> arr_lbf;


    public CalCombined(ArrayList<DataCenter> p_arr_dc) {
        alg1 = 0;
        alg2 = 0;
        Opt1 = 0;
        Opt2 = 0;
        CalTotalTurnTime calTotalTurnTime = new CalTotalTurnTime(p_arr_dc);
        alg1 = calTotalTurnTime.getWholeSystemTurnonTime();

        CalCapacityMakespan calCapacityMakespan = new CalCapacityMakespan(p_arr_dc);
        alg2 = calCapacityMakespan.getWholeSystemCapacityMakespan();

        Opt2 = calCapacityMakespan.getCapacityMakespanSum();
        DataCenterFactory.print.println("所有虚拟机的总负载为： " + Opt2);
        //DataCenterFactory.print.println("整体利用率为：" + Opt2/(CreateVM.maxValue-CreateVM.minValue)/16);

        CalEffectivePM calEffectivePM = new CalEffectivePM(p_arr_dc);
        Opt2 /= calEffectivePM.getEffectivePM();
        DataCenterFactory.print.println("开启的物理机数量： " + calEffectivePM.getEffectivePM());


        this.arr_dc = p_arr_dc;
        for (DataCenter dc : arr_dc) {
            arr_lbf = dc.getArr_lbf();
            for (LoadBalanceFactory lbf : arr_lbf) {
                this.pq1 = lbf.getPmQueueOne();
                this.pq2 = lbf.getPmQueueTwo();
                this.pq3 = lbf.getPmQueueThree();

                needTurnOnTime = 0;
                calQueueaOpt1(pq1);
                calQueueaOpt1(pq2);
                calQueueaOpt1(pq3);
                RackTurnTime += needTurnOnTime;

            }
            DataCenterTurnTime += RackTurnTime;
            RackTurnTime = 0;
        }
        Opt1 = DataCenterTurnTime;

        DataCenterFactory.print.println("实际节能： " + alg1);
        DataCenterFactory.print.println("理想节能： " + Opt1);
        DataCenterFactory.print.println("实际负载： " + alg2);
        DataCenterFactory.print.println("理想负载： " + Opt2);
        //DataCenterFactory.print.println("本轮任务开始时间为："+ CreateVM.minValue + "本轮任务结束时间为：" + CreateVM.maxValue);
        CombinedResults = Enge * (Opt1/alg1) + Load * (Opt2/alg2);
    }


    @Override
    public String getDescription() {
        // TODO Auto-generated method stub
        return "Combined: ";
    }

    @Override
    public float getIndexValue() {
        // TODO Auto-generated method stub
        return CombinedResults;
    }

    private void calQueueaOpt1(ArrayList<PhysicalMachine> pq1){
        if(pq1.size() != 0){
            needCapacity_Makespan = 0.0f;
            int j;
            //int minStartTime = FindMinStartTime(vmQueue1);
            //int maxEndTime = FindMaxEndTime(vmQueue1);
            //for(int i = CreateVM.minValue; i< CreateVM.maxValue; i++){
            for(int i = 0 ; i< CreateLLNLRequests.maxEndTime; i++){
                for(j = 0 ; j < pq1.size() ; j++){
                    for(int k = 0 ; k < pq1.get(j).vms.size(); k++){
                        if(pq1.get(j).vms.get(k).getStartTime()<= i && pq1.get(j).vms.get(k).getEndTime() > i){
                            needCapacity_Makespan += pq1.get(j).vms.get(k).getCpuTotal();
                        }
                    }
                }
                needTurnOnTime += (float)Math.ceil(needCapacity_Makespan/pq1.get(0).getCpuTotal());
                //DataCenterFactory.print.println("第 "+ i + "个区间需要的PM数为：" + (float)Math.ceil(needCapacity_Makespan/pq1.get(0).getCpuTotal()));
                needCapacity_Makespan = 0;
                //if(j == pq1.size() - 1) break;
            }
        }
    }

    private int FindMaxEndTime(ArrayList<VirtualMachine> v_vm){
        int res = 0;
        for(int i = 0 ; i < v_vm.size() - 1 ; i++ ){
            res = Math.max(v_vm.get(i).getEndTime(),v_vm.get(i+1).getEndTime());
        }
        return res;
    }

    private int FindMinStartTime(ArrayList<VirtualMachine> v_vm){
        int res = 0;
        for(int i = 0 ; i < v_vm.size() - 1 ; i++ ){
            res = Math.min(v_vm.get(i).getStartTime(),v_vm.get(i+1).getStartTime());
        }
        return res;
    }
}



