package com.examples;

import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OnlineAlgorithm;
/**
 * Scheduling example1, only adopts one algorithm and producing requests in
 * the essential way.
 * @author Minxian
 *
 */
public class FlexCloudExample3 {
	public static void main(String[] args){
		LoadBalanceFactory lbf = new LoadBalanceFactory();
		AlgorithmItem ai = new AlgorithmItem();
		OnlineAlgorithm oa = new OnlineAlgorithm();
                ai.setRandomAlgortihm(true);
		ai.setRoundRobinAlgorithm(false);
                ai.setListScheduling(false);
                
		Iterator algorithmIterator = ai.createIterator();
		
                lbf.iniPrinter();
		lbf.createVM(new CreateVM());
		
		while(algorithmIterator.hasNext()){
			oa = (OnlineAlgorithm)algorithmIterator.next();
			System.out.println(oa.getDescription());
			lbf = new LoadBalanceFactory();
			
			lbf = new LoadBalanceFactory();
			lbf.bootPM(new PMBootor());
			lbf.generateReuquest();
			lbf.allocate(oa);//Allocated PM ID
			lbf.showIndex();
		}
		LoadBalanceFactory.print.closeFile();
	}
}

