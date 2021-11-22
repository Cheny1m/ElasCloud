/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.examples;

import com.datacenter.LoadBalanceFactory;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OfflineAlgorithm;

/**
 * This examlpe is used to show the results for Offline algorithm, only LPT 
 * algorithm is selected.
 * @author LukeXu
 */
public class FlexCloudExample4 {
 	public static void main(String[] args){
		LoadBalanceFactory lbf = new LoadBalanceFactory();
		AlgorithmItem ai = new AlgorithmItem();
		OfflineAlgorithm ofa = new OfflineAlgorithm();
                ai.setLPT(true);
                ai.setEdf(true);
		Iterator algorithmIterator = ai.createOfflineIterator();
		
//		lbf.createVM(new CreateVM());
//                lbf.createVM(new CreateVMByPorcessTime(new CreateVM()));
		lbf.iniPrinter();
		while(algorithmIterator.hasNext()){
			ofa = (OfflineAlgorithm)algorithmIterator.next();
			LoadBalanceFactory.print.println(ofa.getDescription());
			lbf = new LoadBalanceFactory();
			ofa.createVM(lbf);
			lbf.bootPM(new PMBootor());
			lbf.generateReuquest();
			lbf.allocate(ofa);//Allocated PM ID
			lbf.showIndex();
		}
		LoadBalanceFactory.print.closeFile();
	}
}
    