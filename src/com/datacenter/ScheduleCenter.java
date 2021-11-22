package com.datacenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByEndTime;
import com.generaterequest.CreateVMByPorcessTime;
import com.generaterequest.RequestGenerator;
import com.iterator.ComparisonIndex;
import com.iterator.IndexItem;
import com.iterator.Iterator;
import com.output.Print;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;
import com.schedule.loadbalance.RandomAlgorithm;

/**
 * The highest scheduling level for the whole system, which can schedule
 * datacenters directly.
 * 
 * @author Minxian
 * 
 */
public class ScheduleCenter {

	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
	ArrayList<Integer> arr_TimeDelay = new ArrayList<Integer>();
	DataCenter dc;
	int d_id = 0;
	
	
	public static Print print;
	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();

	IndexItem ii;
	ComparisonIndex ci;
	
	ArrayList<String> indexNames = new ArrayList<String>();
	ArrayList<Float> indexValues = new ArrayList<Float>();
	
	String description;
	
	public ScheduleCenter() {
		for (int timeDelay : arr_TimeDelay) {
			dc = new DataCenter(d_id++, timeDelay);
			arr_dc.add(dc);
		}
	}
	
	
	/**
	 * Standard outputs. High coupling, this method must be used, Maybe a bad
	 * design here
	 */
	public void iniPrinter() {
		try {
			print = new Print();
			print.println("Printer has been initialized......");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Supporting several approaches to creating VMs and write to file.
	 */
	public void createVM(CreateVM cv) {
		print.println("Now Generating VMs......");
		StringBuilder sb = cv.generaterequest();
		cv.writeToTxt(sb);
	}

	public void createVM(CreateVMByEndTime cvbe) {
		print.println("Now Generating VMs......");
		StringBuilder sb1 = cvbe.generaterequest();
		cvbe.writeToTxt(sb1);
	}

	public void createVM(CreateVMByPorcessTime cvbpt) {
		print.println("Now Generating VMs......");
		StringBuilder sb2 = cvbpt.generaterequest();
		cvbpt.writeToTxt(sb2);
	}

	public void createVM(CreateLLNLRequests clr) {
		print.println("Now Generating VMs......");
		StringBuilder sb = clr.generaterequest();
		clr.writeToTxt(sb);
	}
	
	public void generateReuquest() {
		print.println("Now generating requests......");
		RequestGenerator rg = new RequestGenerator();
		vmQueue = rg.generateRequest();
		print.println("Requests have been generated......");
	}

	public int getVmSize() {
		return vmQueue.size();
	}

	public void allocate(OnlineAlgorithm onla) {
		print.println("Starting allocating requests......");
		onla.allocate(vmQueue, arr_dc);	
		print.println("Allocation finished......");
	}

	public void allocate(OfflineAlgorithm ofla) {
		print.println("Starting allocating requests......");
		ofla.allocate(vmQueue, arr_dc);
		print.println("Allocation finished......");
	}

	/**
	 * The following two methods are built with Iterator design pattern.
	 */
	public void showIndex() {

		Iterator indexIterator = ii.createIterator();
		print.println("---Outputs:---");
		collectIndex(indexIterator);
	}

	/**
	 * Collect the comparison indices description and indices value in iteration.
	 * @param iterator
	 */
	private void collectIndex(Iterator iterator) {
		print.println("Collecting comparison indices and algorithms results......");
		while (iterator.hasNext()) {
			ci = (ComparisonIndex) iterator.next();
			print.print(ci.getDescription());
			
			indexNames.add(ci.getDescription());
			print.println(String.valueOf(ci.getIndexValue()));
			indexValues.add(ci.getIndexValue());
		}
	}

	public ArrayList<String> getIndexNames() {
		return indexNames;
	}

	public ArrayList<Float> getIndexValues() {
		return indexValues;
	}

	public IndexItem getIndexItem() {
		return ii;
	}

	public String getDescription() {
		return description;
	}

	public static void main(String[] args){
		ScheduleCenter sc = new ScheduleCenter();
		sc.iniPrinter();
		sc.createVM(new CreateVM());
//		sc.allocate(new RandomAlgorithm());
		sc.showIndex();
	}
}
