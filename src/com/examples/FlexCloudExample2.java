package com.examples;


import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByEndTime;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OnlineAlgorithm;
/**
 * Scheduling example2, only adopts two algorithm and producing requests by
 * descending order by end time.
 * @author Minxian
 *
 */
public class FlexCloudExample2 {
	public static void main(String[] args){
		LoadBalanceFactory lbf = new LoadBalanceFactory();
		AlgorithmItem ai = new AlgorithmItem();
		OnlineAlgorithm oa = new OnlineAlgorithm();
		Iterator algorithmIterator = ai.createIterator();
		
		lbf.iniPrinter();
		lbf.createVM(new CreateVMByEndTime(new CreateVM()));
		while(algorithmIterator.hasNext()){
			oa = (OnlineAlgorithm)algorithmIterator.next();
			LoadBalanceFactory.print.println(oa.getDescription());
			
			lbf = new LoadBalanceFactory();
			lbf.bootPM(new PMBootor());
			lbf.generateReuquest();
			lbf.allocate(oa); 
			lbf.showIndex();
		}
		LoadBalanceFactory.print.closeFile();
	}
}
