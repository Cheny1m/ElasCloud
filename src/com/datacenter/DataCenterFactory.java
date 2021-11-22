package com.datacenter;

import cloudscheinterface.DataCenterADDJFrame;
import com.generaterequest.CreateVM;
import com.generaterequest.RequestGenerator;
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
    ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
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
        DataCenterFactory.print.println("Now Generating VMs......");
        StringBuilder sb = cv.generaterequest();
        cv.writeToTxt(sb);
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

    public void iniDataCenters() {
//        arr_dc = DataCenterADDJFrame.arr_dc;
//        for (DataCenter dc : arr_dc) {
//            dc.initalAllResourse();
//        }
    	arr_dc.clear();
//    	/**Test Case #2
        DataCenter dc1 = new DataCenter(0, 1000);
        ArrayList<Rack> arr_rack = new ArrayList<Rack>();
        arr_rack.add(new Rack(0, 50, 0, 0, 0));
        arr_rack.add(new Rack(1, 50, 0, 0, 100));
        dc1.setArr_rack(arr_rack);
        dc1.initalAllResourse();
        arr_dc.add(dc1);
        
        DataCenter dc2 = new DataCenter(1, 200);
        ArrayList<Rack> arr_rack1 = new ArrayList<Rack>();
        arr_rack1.add(new Rack(0, 50, 0, 0, 0));
        arr_rack1.add(new Rack(1, 50, 0, 0, 100));
        dc2.setArr_rack(arr_rack1);
        dc2.initalAllResourse();
        arr_dc.add(dc2);
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
    public void allocate(OnlineAlgorithm onla) {
        DataCenterFactory.print.println("Starting allocating requests......");
        onla.allocate(vmQueue, arr_dc);
        ii = new IndexItem(arr_dc);
        DataCenterFactory.print.println("Allocation finished......");
    }

    public void allocate(OfflineAlgorithm ofla) {
    }

    /**
     * Generating the VM requests from the creating VM source file
     */
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
}
