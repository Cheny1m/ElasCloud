package com.examples;

import com.datacenter.DataCenterFactory;
import com.generaterequest.CreateVM;
import com.iterator.AlgorithmItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OnlineAlgorithm;

/**
 * Scheduling example1, only adopts one algorithm and producing requests in the
 * essential way.
 * 
 * @author Minxian
 * 
 */
public class FlexCloudExample6 {
	public static void main(String[] args) {
		DataCenterFactory dcf = new DataCenterFactory();
		AlgorithmItem ai = new AlgorithmItem();
		OnlineAlgorithm oa = new OnlineAlgorithm();
		ai.setRandomAlgortihm(true);
		ai.setDRS(true);
		ai.setOlrsa(true);
		ai.setSAE(true);
		ai.setIW(true);
		dcf.iniPrinter();
		
		Iterator algorithmIterator = ai.createIterator();

		dcf.createVM(new CreateVM());

		while (algorithmIterator.hasNext()) {
			oa = (OnlineAlgorithm) algorithmIterator.next();
			System.out.println(oa.getDescription());

			dcf.iniDataCenters();
			dcf.generateReuquest();
			dcf.allocate(oa);// Allocated PM ID
			dcf.showIndex();
		}
		DataCenterFactory.print.closeFile();
	}
}
