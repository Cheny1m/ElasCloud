package com.datacenter;

import cloudscheinterface.MainGUI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByEndTime;
import com.generaterequest.CreateVMByPorcessTime;
import com.generaterequest.PMBootor;
import com.generaterequest.RequestGenerator;
import com.iterator.ComparisonIndex;
import com.iterator.IndexItem;
import com.iterator.Iterator;
import com.output.Print;
import com.output.WriteToExcel;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;
import com.schedule.loadbalance.RandomAlgorithm;

/**
 * This class implements the interface DataCenterFactory, that is to say,
 * LoadBalanceFactory would follow the defined processes of scheduling. While
 * for the instance of LoadBalanceFactory, it can produce different composition
 * for different requests creating approaches, scheduling algorithms, comparison
 * indices.
 * 实现接口，完成各类请求、分配与结果展示
 * @author Minxian
 * 
 */
public class LoadBalanceFactory implements DataCenterImp {

	public static int MAXTIME;
	public static final String FINISHEDINFO = "---Allocation Finished---";
	public static final String FAILEDINFO = "---Resource not enough, try another PM---";
	//pm个数
	int pmSize;
	String description;
	//三种物理机队列
	ArrayList<PhysicalMachine> pmQueueOne = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueTwo = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueThree = new ArrayList<PhysicalMachine>();
	//索引
	IndexItem ii;
	//比较索引
	ComparisonIndex ci;
	//vm请求队列
	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	public static Print print;
	public static WriteToExcel writeToExcel = new WriteToExcel();
	//索引名
	ArrayList<String> indexNames = new ArrayList<String>();
	//索引值
	ArrayList<Float> indexValues = new ArrayList<Float>();
	//机架ID
	private int r_id;
	//机架延时，拓展需求
	private int r_timeDelay;
	//各类pm数量
	private int r_pmNum1;
	private int r_pmNum2;
	private int r_pmNum3;

	public LoadBalanceFactory() {
		description = "LoadBalanceFactory";
		// createVM(new CreateVM());
		// // dcf.createVM(new CreateVMByEndTime(new CreateVM()));
		// // dcf.createVM(new CreateVMByPorcessTime(new CreateVM()));
		// bootPM(new PMBootor());
		// generateReuquest();
		// allocate(new RandomAlgorithm());//Allocated PM ID
		// showIndex();
	}

	/**
	 * Another construct method special for multiple datacenters.
	 * 另一种专门用于多个数据中心的构造方法/机架；-----与daracenter方式对比
	 * 
	 * @param r_id
	 * @param r_TimeDelay
	 * @param r_pmNum1
	 * @param r_pmNum2
	 * @param r_pmNum3
	 */
	public LoadBalanceFactory(Rack rack) {
		this.r_id = rack.getR_id();
		this.r_timeDelay = rack.getR_timeDelay();
		this.r_pmNum1 = rack.getR_pmNum1();
		this.r_pmNum2 = rack.getR_pmNum2();
		this.r_pmNum3 = rack.getR_pmNum3();

		bootPM(r_pmNum1, r_pmNum2, r_pmNum3);
	}

	/**
	 * Standard outputs. High coupling, this method must be used, Maybe a bad
	 * design here
	 */
	public void iniPrinter() {
		try {
			print = new Print();
			LoadBalanceFactory.print
					.println("Printer has been initialized......");
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
		LoadBalanceFactory.print.println("Now Generating VMs......");
		StringBuilder sb = cv.generaterequest();
		cv.writeToTxt(sb);
	}

	public void createVM(CreateVMByEndTime cvbe) {
		LoadBalanceFactory.print.println("Now Generating VMs......");
		StringBuilder sb1 = cvbe.generaterequest();
		cvbe.writeToTxt(sb1);
	}

	public void createVM(CreateVMByPorcessTime cvbpt) {
		LoadBalanceFactory.print.println("Now Generating VMs......");
		StringBuilder sb2 = cvbpt.generaterequest();
		cvbpt.writeToTxt(sb2);
	}

	public void createVM(CreateLLNLRequests clr) {
		LoadBalanceFactory.print.println("Now Generating VMs......");
		StringBuilder sb = clr.generaterequest();
		clr.writeToTxt(sb);
	}

	/**
	 * Here listing 3 kinds of PMs, I can do better here for generic goal.
	 */
	public void bootPM(PMBootor pmb) {
		LoadBalanceFactory.print.println("Now starting PMs......");
		PhysicalMachine pm;
		ArrayList<Integer> PMNum = pmb.bootPM();

		for (int i = 0; i < PMNum.get(0); i++) {
			pm = new PhysicalMachine(i, 1);
			pmQueueOne.add(pm);
		}

		for (int i = PMNum.get(0); i < PMNum.get(0) + PMNum.get(1); i++) {
			pm = new PhysicalMachine(i, 2);
			pmQueueTwo.add(pm);
		}

		for (int i = PMNum.get(0) + PMNum.get(1); i < PMNum.get(0)
				+ PMNum.get(1) + PMNum.get(2); i++) {
			pm = new PhysicalMachine(i, 3);
			pmQueueThree.add(pm);
		}
		pmSize = pmQueueOne.size() + pmQueueTwo.size() + pmQueueThree.size();
		LoadBalanceFactory.print.println("PMs have been booted......");
	}

	/**
	 * New created method for multiple datacenters.
	 * 
	 * @param pmNum1
	 * @param pmNum2
	 * @param pmNum3
	 */
	public void bootPM(int pmNum1, int pmNum2, int pmNum3) {
		//产生各类各数量的pm
		PhysicalMachine pm;
		for (int i = 0; i < pmNum1; i++) {
			pm = new PhysicalMachine(i, 1);
			pmQueueOne.add(pm);
		}

		for (int i = pmNum1; i < pmNum1 + pmNum2; i++) {
			pm = new PhysicalMachine(i, 2);
			pmQueueTwo.add(pm);
		}

		for (int i = pmNum1 + pmNum2; i < pmNum1 + pmNum2 + pmNum3; i++) {
			pm = new PhysicalMachine(i, 3);
			pmQueueThree.add(pm);
		}
		pmSize = pmQueueOne.size() + pmQueueTwo.size() + pmQueueThree.size();
	}

	public int getPmSize() {
		return pmSize;
	}

	//生成任务
	public void generateReuquest() {
		LoadBalanceFactory.print.println("Now generating requests......");
		RequestGenerator rg = new RequestGenerator();
		//从配置文件中获取数据，生成请求队列
		vmQueue = rg.generateRequest();
		LoadBalanceFactory.print.println("Requests have been generated......");
	}

	public int getVmSize() {
		return vmQueue.size();
	}

	public void allocate(OnlineAlgorithm onla) {
		LoadBalanceFactory.print.println("Starting online allocating requests......");
		onla.allocate(vmQueue, pmQueueOne, pmQueueTwo, pmQueueThree);
		ii = new IndexItem(pmQueueOne, pmQueueTwo, pmQueueThree);
		LoadBalanceFactory.print.println("Allocation finished......");
	}

	@Override
	public void allocate(OfflineAlgorithm ofla) {
		LoadBalanceFactory.print.println("Starting offline allocating requests......");
		//System.out.println(vmQueue + "  "+pmQueueOne+"  "+pmQueueTwo+" "+pmQueueThree);
		ofla.allocate(vmQueue, pmQueueOne, pmQueueTwo, pmQueueThree);
		ii = new IndexItem(pmQueueOne, pmQueueTwo, pmQueueThree);
		LoadBalanceFactory.print.println("Allocation finished......");

	}

	/**
	 * The following two methods are built with Iterator design pattern.
	 */
	public void showIndex() {

		Iterator indexIterator = ii.createIterator();
		LoadBalanceFactory.print.println("---Outputs:---");
		showIndex(indexIterator);
	}

	private void showIndex(Iterator iterator) {
		LoadBalanceFactory.print
				.println("Collecting comparison indices and algorithms results......");
		while (iterator.hasNext()) {
			ci = (ComparisonIndex) iterator.next();
			LoadBalanceFactory.print.print(ci.getDescription());
			indexNames.add(ci.getDescription());
			LoadBalanceFactory.print
					.println(String.valueOf(ci.getIndexValue()));
			indexValues.add(ci.getIndexValue());
		}
	}

	public int getLbf_ID() {
		return r_id;
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

	public ArrayList<PhysicalMachine> getPmQueueOne() {
		return pmQueueOne;
	}

	public ArrayList<PhysicalMachine> getPmQueueTwo() {
		return pmQueueTwo;
	}

	public ArrayList<PhysicalMachine> getPmQueueThree() {
		return pmQueueThree;
	}

	//机架负载
	public float getRackLoad() {

		float rackAverageUtilization = 0.0f;
		float tempAverageUtilization = 0.0f;
		int effectivePMNumber = 0;
		for (PhysicalMachine pm : pmQueueOne) {
			tempAverageUtilization = pm.getAvgUtility();
			if (tempAverageUtilization != 0) {
				rackAverageUtilization += tempAverageUtilization;
				effectivePMNumber++;
			}
		}

		for (PhysicalMachine pm : pmQueueTwo) {
			tempAverageUtilization = pm.getAvgUtility();
			if (tempAverageUtilization != 0) {
				rackAverageUtilization += tempAverageUtilization;
				effectivePMNumber++;
			}
		}

		for (PhysicalMachine pm : pmQueueThree) {
			tempAverageUtilization = pm.getAvgUtility();
			if (tempAverageUtilization != 0) {
				rackAverageUtilization += tempAverageUtilization;
				effectivePMNumber++;
			}
		}
		if (effectivePMNumber == 0) {
			return 0.0f;
		} else {
			return rackAverageUtilization / effectivePMNumber;
		}
	}

	public float getRackCapacityMakespan() {
		float rackCapacityMakespan = 0.0f;
		float tempCapacityMakespan = 0.0f;
		for (PhysicalMachine pm : pmQueueOne) {
			tempCapacityMakespan = pm.getTotalCapacityMakespan();
			if (tempCapacityMakespan > rackCapacityMakespan) {
				rackCapacityMakespan = tempCapacityMakespan;
			}
		}

		for (PhysicalMachine pm : pmQueueTwo) {
			tempCapacityMakespan = pm.getTotalCapacityMakespan();
			if (tempCapacityMakespan > rackCapacityMakespan) {
				rackCapacityMakespan += tempCapacityMakespan;
			}
		}

		for (PhysicalMachine pm : pmQueueThree) {
			tempCapacityMakespan = pm.getTotalCapacityMakespan();
			if (tempCapacityMakespan > rackCapacityMakespan) {
				rackCapacityMakespan += tempCapacityMakespan;
			}
		}
		return rackCapacityMakespan;
	}

	public static void main(String[] args) {
//		 LoadBalanceFactory dcf = new LoadBalanceFactory();
//		 dcf.createVM(new CreateVM());
//		 // dcf.createVM(new CreateVMByEndTime(new CreateVM()));
//		 // dcf.createVM(new CreateVMByPorcessTime(new CreateVM()));
//		 dcf.bootPM(new PMBootor());
//		 dcf.generateReuquest();
//		 dcf.allocate(new RandomAlgorithm());//Allocated PM ID
//		 dcf.showIndex();
	}
}
