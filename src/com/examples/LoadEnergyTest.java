package com.examples;

import cloudscheinterface.*;
import com.comparedindex.*;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import cloudscheinterface.DataCenterADDJFrame;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateLLNLRequests;
import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.iterator.AlgorithmItem;
import com.iterator.IndexItem;
import com.iterator.Iterator;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import java.io.*;


/**
 * Scheduling example1, only adopts one algorithm and producing requests in
 * the essential way.
 *
 */

public class LoadEnergyTest {

    public static void main(String[] args) throws IOException {
        int NumbofAlgorithm = 6;
        int NumbofIndices = 9;
        float[][] Result = new float[NumbofAlgorithm][NumbofIndices];

        int NumbofCirculation = 1;
        for(int i = 0;i < NumbofCirculation ; i++){
            clearPreviousData();
            Iterator algorithmIterator;
            AlgorithmItem ai = new AlgorithmItem();
            OfflineAlgorithm ofa = new OfflineAlgorithm();

            DataCenterFactory dcf = new DataCenterFactory();
            //ai.setRR(true);
            //ai.setLPT(true);
            //ai.setFFD(true);
            //ai.setCMP(true);
            ai.setLey(true);
            ai.setMbfd(true);
            //ai.setLeynom(true);
            //ai.setNewley(true);

            algorithmIterator = ai.createOfflineIterator();

            dcf.iniPrinter();
            //修改配置文件的值
            PropertiesDemo.update("StartTime","5");
            PropertiesDemo.update("MinSpan","100");
            PropertiesDemo.update("RequestNum","400");

            dcf.createVM(new CreateLLNLRequests());
//            CreateVM createVM = new CreateVM();
//            createVM.setDistribution("ExponentialServiceTime");
//            //createVM.setDistribution("NormalDistri");
//            dcf.createVM(createVM);

            int j = 0;
            //当在在线算法的迭代器中存有在线算法时
            while (algorithmIterator.hasNext()) {
                ofa = (OfflineAlgorithm) algorithmIterator.next();
                DataCenterFactory.print.println(ofa.getDescription());
                //selectedAlgorithms.add(oa.getDescription());

                dcf.iniDataCenters();
                dcf.generateReuquest();
                //分配
                dcf.allocate(ofa);
                //获取对比条件
                //confSelectedIndices(selectedIndices);
                dcf.getIndexItem().setAverageUility(true);
                dcf.getIndexItem().setImbalanceDegree(true);
                dcf.getIndexItem().setMakespan(true);
                dcf.getIndexItem().setCapacity_makespan(true);
                dcf.getIndexItem().setTotalTurnTime(true);
                dcf.getIndexItem().setCombined(true);
                dcf.getIndexItem().setEnergyConsumption(true);

                dcf.showIndex();
                setDataSet(dcf.getIndexValues(), ofa.getDescription(), dcf.getIndexNames());
                DataCenterADDJFrame.algorithmDataCenterMap.put(ofa.getDescription(), dcf.getArr_dc());

                //获取本次指标值
                Result[j][0] += CalAverageUtility.wholeSystemAverageUtility;
                Result[j][1] += CalImbalanceDegree.wholeSystemImbalanceDegree;
                Result[j][2] += CalMakespan.wholeSystemMakespan;
                Result[j][3] += CalCapacityMakespan.wholeSystemCapacityMakespan;
                //理想负载
                Result[j][4] += CalCombined.Opt2/CalCombined.alg2;
                Result[j][5] += CalTotalTurnTime.wholeSystemTurnonTime;
                //理想节能
                Result[j][6] += CalCombined.Opt1/CalCombined.alg1;
                Result[j][7] += CalCombined.CombinedResults;
                Result[j][8] += CalEnergyConsumption.wholeSystemEnergyConsumption;
                CalAverageUtility.wholeSystemAverageUtility = 0;
                CalImbalanceDegree.wholeSystemImbalanceDegree = 0;
                CalMakespan.wholeSystemMakespan = 0 ;
                CalCapacityMakespan.wholeSystemCapacityMakespan = 0;
                CalTotalTurnTime.wholeSystemTurnonTime = 0;
                CalCombined.CombinedResults = 0;
                CalEnergyConsumption.wholeSystemEnergyConsumption = 0;
                j++;
            }
            DataCenterFactory.print.closeFile();
            //new DrawResults(getDataSet());
            //new DrawResults(getDataset1(), "LargeValue");
        }
        for(int i = 0;i < NumbofAlgorithm ;i++){
            System.out.println("算法" + i +":");
            for(int j = 0; j < NumbofIndices ; j++){
                System.out.println(Result[i][j]/NumbofCirculation);
                //归0
                Result[i][j] = 0;
            }
        }
    }

    public static class PropertiesDemo {
        public static final Properties p = new Properties();
        public static final String path = "src/com/generaterequest/requestPro.pro";

        /**
         * 修改或者新增key
         *
         * @param key
         * @param value
         */
        public static void update(String key, String value) {
            p.setProperty(key, value);
            FileOutputStream oFile = null;
            try {
                oFile = new FileOutputStream(path);
                //将Properties中的属性列表（键和元素对）写入输出流
                p.store(oFile, "");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    oFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    static DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    static DefaultCategoryDataset dataset1 = new DefaultCategoryDataset(); //Show the ratio of some indices with large values
    public static void setDataSet(ArrayList<Float> arrf, String string, ArrayList<String> arrs) {

        for (int index = 0; index < arrf.size(); index++) {
            if (arrf.get(index) <= 1) {
                dataset.addValue(arrf.get(index), string, arrs.get(index));
            } else {
                dataset1.addValue(arrf.get(index), string, arrs.get(index));
            }
        }
    }

    public static CategoryDataset getDataSet() {
        return dataset;
    }

    public static CategoryDataset getDataset1() {
        return dataset1;
    }

    public static void clearPreviousData() {
        dataset.clear();
        dataset1.clear();
    }
}