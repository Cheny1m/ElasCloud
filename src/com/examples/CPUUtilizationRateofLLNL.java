package com.examples;

import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OnlineAlgorithm;
/**
 * 测试各利用率数量
 * @author Yueming Chen
 *
 */
public class CPUUtilizationRateofLLNL {
    public static void main(String[] args){

        DataCenterFactory dcf = new DataCenterFactory();
        dcf.iniPrinter();
        dcf.createVM(new CreateLLNLRequests());

        dcf.iniDataCenters();
        dcf.generateReuquest();


    }
}

