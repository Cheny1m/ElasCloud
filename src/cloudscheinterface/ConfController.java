/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudscheinterface;

import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.ComparisonIndex;
import com.iterator.Iterator;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;
import java.util.ArrayList;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * This class is used to send configuration to backends from interface.
 *
 * @author Minxian
 */
public class ConfController {

	
	
    DataCenterFactory dcf = new DataCenterFactory();
    ArrayList<Boolean> selectedIndices = new ArrayList<Boolean>();
    ArrayList<String> selectedAlgorithms = new ArrayList<String>();
    
    LoadBalanceFactory lbf = new LoadBalanceFactory();
    AlgorithmItem ai = new AlgorithmItem();
    OnlineAlgorithm oa = new OnlineAlgorithm();
    OfflineAlgorithm ofa = new OfflineAlgorithm();
    Iterator algorithmIterator, algorithmIterator2;
    ComparisonIndex ci;
    CreateVM cv;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    DefaultCategoryDataset dataset1 = new DefaultCategoryDataset(); //Show the ratio of some indices with large values
    String distribution;

    public ConfController() {
    }

    public void run() {

        algorithmIterator = ai.createIterator();
        algorithmIterator2 = ai.createOfflineIterator();
        clearPreviousData();
        //Part of excel operation are coded in CalAverageUtility.java

        dcf.iniPrinter();
        dcf.createVM(new CreateVM());
        while (algorithmIterator.hasNext()) {
            oa = (OnlineAlgorithm) algorithmIterator.next();
            DataCenterFactory.print.println(oa.getDescription());
            selectedAlgorithms.add(oa.getDescription());

            dcf.iniDataCenters();
            dcf.generateReuquest();

            dcf.allocate(oa);
            confSelectedIndices(selectedIndices,0);
            dcf.showIndex();
            setDataSet(dcf.getIndexValues(), oa.getDescription(), dcf.getIndexNames());
            DataCenterADDJFrame.algorithmDataCenterMap.put(oa.getDescription(), dcf.getArr_dc());
        }
        
        lbf.iniPrinter();
        while (algorithmIterator2.hasNext()) {
            //May add reflective method in the future.
            ofa = (OfflineAlgorithm) algorithmIterator2.next();
            LoadBalanceFactory.print.println(ofa.getDescription());
            
            lbf = new LoadBalanceFactory();
            new CreateLLNLRequests();
            ofa.createVM(lbf); //Different with online iterator
            
            lbf.bootPM(new PMBootor());
            lbf.generateReuquest();


            lbf.allocate(ofa);//Allocated PM ID
            confSelectedIndices(selectedIndices,1);
            lbf.showIndex();
            setDataSet(lbf.getIndexValues(), ofa.getDescription(), lbf.getIndexNames());
            
			
        }

        DataCenterFactory.print.closeFile();

        new DrawResults(getDataSet());
        new DrawResults(getDataset1(), "LargeValue");
    }

    public void confSelectedAlgorithm(ArrayList<Boolean> selectedAlgortihm) {
        int index = 0;
        //The sequence should be strictly set.
        ai.setRandomAlgortihm(selectedAlgortihm.get(index++));
        ai.setDRS(selectedAlgortihm.get(index++));
        ai.setSAE(selectedAlgortihm.get(index++));
        ai.setOlrsa(selectedAlgortihm.get(index++));
        ai.setIW(selectedAlgortihm.get(index++));
        ai.setEdf(selectedAlgortihm.get(index++));
        ai.setCMP(selectedAlgortihm.get(index++));
        ai.setLPT(selectedAlgortihm.get(index++));
        ai.setMIG(selectedAlgortihm.get(index++));

    }

    public void confSelectedIndices(ArrayList<Boolean> selectedIndices,int f) {
        int index = 0;
        /*
         * Sequence should be strictly set. Find the sequence in the interface.
         */
        if (f == 0){
            dcf.getIndexItem().setAverageUility(selectedIndices.get(index++));
            //lbf.getIndexItem().setAverageUility(selectedIndices.get(index++));
            dcf.getIndexItem().setImbalanceDegree(selectedIndices.get(index++));
            dcf.getIndexItem().setMakespan(selectedIndices.get(index++));
            //lbf.getIndexItem().setSkew_makespan(selectedIndices.get(index++));
            dcf.getIndexItem().setCapacity_makespan(selectedIndices.get(index++));
            //lbf.getIndexItem().setSkew_capaciy_makespan(selectedIndices.get(index++));
//        lbf.getIndexItem().setEffectivePM(selectedIndices.get(index++));
//        lbf.getIndexItem().setEnergyConsumption(selectedIndices.get(index++));
//        lbf.getIndexItem().setRejectedVMNum(selectedIndices.get(index++));
//        dcf.getIndexItem().setProcessTime(selectedIndices.get(index++));
        }
        else {
            lbf.getIndexItem().setAverageUility(selectedIndices.get(index++));
            lbf.getIndexItem().setImbalanceDegree(selectedIndices.get(index++));
            lbf.getIndexItem().setMakespan(selectedIndices.get(index++));
            lbf.getIndexItem().setCapacity_makespan(selectedIndices.get(index++));
        }
    }

    public void setSelectedIndices(ArrayList<Boolean> selectedIndices) {
        this.selectedIndices = selectedIndices;
    }

    public ArrayList<String> getSelectedAlgorithms() {
        return selectedAlgorithms;
    }

    public CategoryDataset getDataSet() {
        return dataset;
    }

    public CategoryDataset getDataset1() {
        return dataset1;
    }

    public void setDataSet(ArrayList<Float> arrf, String string, ArrayList<String> arrs) {

        for (int index = 0; index < arrf.size(); index++) {
            if (arrf.get(index) <= 1) {
                dataset.addValue(arrf.get(index), string, arrs.get(index));
            } else {
                dataset1.addValue(arrf.get(index), string, arrs.get(index));
            }
        }

//        int indexCol = 9;
//        int oriRow = Da.writeToExcel.getOriRowNumber();
//        int indexRow = LoadBalanceFactory.writeToExcel.getCompareRowNumber();
//
//        for (int index = 0; index < arrs.size(); index++) {
//            LoadBalanceFactory.writeToExcel.writeLabel(indexCol + index, oriRow, arrs.get(index));
//        }
//        LoadBalanceFactory.writeToExcel.setIndexRowNumber(indexRow + 1);
//
//        indexRow = LoadBalanceFactory.writeToExcel.getCompareRowNumber();
//        LoadBalanceFactory.writeToExcel.writeLabel(indexCol - 1, indexRow, string);
//        for (int index = 0; index < arrf.size(); index++) {
//            LoadBalanceFactory.writeToExcel.writeData(indexCol + index, indexRow, arrf.get(index));
//        }
        //  LoadBalanceFactory.writeToExcel.setIndexRowNumber(indexRow + 1);
    }

    public void clearPreviousData() {
        dataset.clear();
        dataset1.clear();
    }

    public String getDataSetOutput() {
        StringBuilder sb = new StringBuilder();
        //Put the value of data set into StringBuilder sb
        for (int i = 0; i < dataset.getRowCount(); i++) {
            sb.append(dataset.getRowKey(i));
            sb.append("\n");
            for (int j = 0; j < dataset.getColumnCount(); j++) {
                sb.append(dataset.getColumnKey(j));
                sb.append(dataset.getValue(i, j));
                sb.append("\n");

            }
        }
        //Put the value of data set into StringBuilder sb
        for (int i = 0; i < dataset1.getRowCount(); i++) {
            sb.append(dataset1.getRowKey(i));
            sb.append("\n");
            for (int j = 0; j < dataset1.getColumnCount(); j++) {
                sb.append(dataset1.getColumnKey(j));
                sb.append(dataset1.getValue(i, j));
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    public void setRequestsDistribution(String string) {
        this.distribution = string;
    }

    private String getRequestsDistribution() {
        return distribution;
    }
}
