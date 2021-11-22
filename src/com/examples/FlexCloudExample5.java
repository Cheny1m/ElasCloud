/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.examples;

import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OfflineAlgorithm;

/**
 * This examlpe is used to show the results for Offline algorithm, LPT and CMP
 * WriteExcel Operation are added here.
 * algorithm are compared here.
 * @author LukeXu
 */
public class FlexCloudExample5 {
 	public static void main(String[] args){
		LoadBalanceFactory lbf = new LoadBalanceFactory();
                LoadBalanceFactory.writeToExcel.openExcel(
                            "src/com/output/PMdata.xls", "Sheet One", 0);
                lbf.iniPrinter();
		AlgorithmItem ai = new AlgorithmItem();
		OfflineAlgorithm ofa = new OfflineAlgorithm();
                ai.setLPT(true);
                ai.setCMP(true);
                ai.setMIG(true);
		Iterator algorithmIterator = ai.createOfflineIterator();
		
		while(algorithmIterator.hasNext()){
			ofa = (OfflineAlgorithm)algorithmIterator.next();
			LoadBalanceFactory.print.println(ofa.getDescription());
                        
                        LoadBalanceFactory.writeToExcel.writeLabel(
                                0, 
                                LoadBalanceFactory.writeToExcel.getRowNumber(), 
                                ofa.getDescription());
                        LoadBalanceFactory.writeToExcel.setRowNumber(
                                LoadBalanceFactory.writeToExcel.getRowNumber() + 1);
                        
			lbf = new LoadBalanceFactory();
			new CreateLLNLRequests();
			ofa.createVM(lbf);
			lbf.bootPM(new PMBootor());
			lbf.generateReuquest();
                        
                                                LoadBalanceFactory.writeToExcel.writeLabel(
                                1, 
                                LoadBalanceFactory.writeToExcel.getRowNumber() - 1,
                                "VM number: " + lbf.getVmSize());
                        //Write PM number to excel
                        LoadBalanceFactory.writeToExcel.writeLabel(
                                2, 
                                LoadBalanceFactory.writeToExcel.getRowNumber() - 1,
                               "PM number: " + lbf.getPmSize());
                        
			lbf.allocate(ofa);//Allocated PM ID
			lbf.showIndex();
		}
                 LoadBalanceFactory.writeToExcel.closeExcel();
		LoadBalanceFactory.print.closeFile();
	}
}
    