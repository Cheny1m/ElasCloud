package com.datacenter;

import cloudscheinterface.DataCenterADDJFrame;
import com.generaterequest.*;
import com.iterator.ComparisonIndex;
import com.iterator.IndexItem;
import com.iterator.Iterator;
import com.output.Print;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is the basic class factory to produce factories for LoadBalance
 * Factory and EnergySaving Factory. In this class, the general data center
 * processing flows are included.
 * 1. Requests are produced by CreateVM in particular creating algorithm;
 * 2. Initialize Physical Machines can provide services and resources;
 * 3. Load the VM requests from request file ;
 * 4. Use different algorithm to schedule the requests;
 * 5. Calculate the value of needed compared indices;
 * 6. Output the needed results.
 * In CloudSche, some design patterns are used:
 * Decorator, Abstract Factory, Strategy, Iterator design patterns.
 * 
 * @author Minxian
 *
 */
public class DataCenterFactory {

    public static int MAXTIME;
    public static final String FINISHEDINFO = "---Allocation Finished---";
    public static final String FAILEDINFO = "---Resource not enough, try another PM---";
    public static ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
    //数据中心list  3个pm列表的合成
    ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
    IndexItem ii;
    ComparisonIndex ci;
    ArrayList<String> indexNames = new ArrayList<String>();
    ArrayList<Float> indexValues = new ArrayList<Float>();
    public static Print print;

    /**
     * Creating the VM requests based on configurations
     * @param cv
     */
    public void createVM(CreateVM cv) {
        DataCenterFactory.print.println("Now Generating VMs by synthetic data......");
        StringBuilder sb = cv.generaterequest();
        cv.writeToTxt(sb);
    }

    public void createVM(CreateVMByEndTime cvbe) {
        DataCenterFactory.print.println("Now Generating VMs......");
        StringBuilder sb1 = cvbe.generaterequest();
        cvbe.writeToTxt(sb1);
    }

    public void createVM(CreateVMByPorcessTime cvbpt) {
        DataCenterFactory.print.println("Now Generating VMs......");
        StringBuilder sb2 = cvbpt.generaterequest();
        cvbpt.writeToTxt(sb2);
    }

    public void createVM(CreateLLNLRequests clr) {
        DataCenterFactory.print.println("Now Generating VMs by LLNL data......");
        StringBuilder sb = clr.generaterequest();
        clr.writeToTxt(sb);
    }

    public void iniPrinter() {
        try {
            print = new Print();
            DataCenterFactory.print.println("Printer has been initialized......");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //初始化数据中心；与LoadBalanceFactory不同的点  代替LoadBalanceFactory.bootPM()
    public void iniDataCenters() {
//        arr_dc = DataCenterADDJFrame.arr_dc;
//        for (DataCenter dc : arr_dc) {
//            dc.initalAllResourse();
//        }
    	arr_dc.clear();
//    	/**Test Case #2
        System.out.println("Start initializing data center...");
        //DataCenter dc1 = new DataCenter(0, 1000);
        //暂定延时0；
        DataCenter dc1 = new DataCenter(0, 0);
        ArrayList<Rack> arr_rack = new ArrayList<Rack>();
        //机架未与GUI实现对接
        arr_rack.add(new Rack(0, 60, 0, 0, 0));
        //arr_rack.add(new Rack(1, 60, 0, 0, 100));
        dc1.setArr_rack(arr_rack);
        dc1.initalAllResourse();
        arr_dc.add(dc1);
        
//        DataCenter dc2 = new DataCenter(1, 200);
//        ArrayList<Rack> arr_rack1 = new ArrayList<Rack>();
//        arr_rack1.add(new Rack(0, 60, 0, 0, 0));
//        arr_rack1.add(new Rack(1, 60, 0, 0, 100));
//        dc2.setArr_rack(arr_rack1);
//        dc2.initalAllResourse();
//        arr_dc.add(dc2);
        System.out.println(arr_dc.size() +" Center racks have been generated......");
//      */  
    	
    	/** Test Case #1
        DataCenter dc1 = new DataCenter(0, 1000);
        ArrayList<Rack> arr_rack = new ArrayList<Rack>();
        arr_rack.add(new Rack(0, 50, 0, 0, 0));
        dc1.setArr_rack(arr_rack);
        dc1.initalAllResourse();
        arr_dc.add(dc1);
        */
    	 
    }

    /**
     * Allocate VM requests to corresponding PM
     * @param aa
     */
    //请求；与loadbalance的区别是将3个pm序列合成了数据中心
    public void allocate(OnlineAlgorithm onla) {
        DataCenterFactory.print.println("Starting online allocating requests......");
        //arr_dc 两个数据中心
        onla.allocate(vmQueue, arr_dc);
        ii = new IndexItem(arr_dc);
        DataCenterFactory.print.println("Allocation finished......");
    }

    public void allocate(OfflineAlgorithm ofla) {
        DataCenterFactory.print.println("Starting offline allocating requests......");
        ofla.allocate(vmQueue, arr_dc);
        ii = new IndexItem(arr_dc);
        DataCenterFactory.print.println("Allocation finished......");
    }

    /**
     * Generating the VM requests from the creating VM source file
     */
    //任务生成方式相同
    public void generateReuquest() {
        DataCenterFactory.print.println("Now generating requests......");
        RequestGenerator rg = new RequestGenerator();
        vmQueue = rg.generateRequest();
        DataCenterFactory.print.println("Requests have been generated......");
    }

    /**
     * Show the calculated indices results
     */
    public void showIndex() {
        Iterator indexIterator = ii.createIterator();
        DataCenterFactory.print.println("---Outputs:---");
        showIndex(indexIterator);
    }

    private void showIndex(Iterator iterator) {
        DataCenterFactory.print.println("Collecting comparison indices and algorithms results......");
        while (iterator.hasNext()) {
            ci = (ComparisonIndex) iterator.next();
            DataCenterFactory.print.print(ci.getDescription());
            indexNames.add(ci.getDescription());
            DataCenterFactory.print.println(String.valueOf(ci.getIndexValue()));
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

    public ArrayList<DataCenter> getArr_dc() {
        return arr_dc;
    }

    /**
     * Scheduling description
     * @return
     */
    public String getDescription() {

        return "";
    }

    //好像未添加比较值模块
}
