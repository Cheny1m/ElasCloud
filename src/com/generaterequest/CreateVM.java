package com.generaterequest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Random;

import cloudscheinterface.ConfController;
import cloudscheinterface.MainGUI;
import com.distribute.UniformDistri;
import com.specification.*;
import com.datacenter.*;

/**
 * This class is the base class for creating VM requests.
 * The starting time span, requests number, average span are loaded from
 * requestPro.pro. Starting time is a random number in predefined span.
 * Average span in a random value in predefined scope.
 * End time should be larger than starting time.
 *
 * @author Minxian
 */
public class CreateVM implements RWCreateVM {

    int vmSize;
    int startTime;
    int minSpan;
    float[] l_arrayPorba;
    //String distribution = "com.distribute.NormalDistri";
    String distribution = "com.distribute." + MainGUI.s;

    //用于读取配置文件?
    Properties property;
    InputStream is;
    VmInfo vmInfo;
    //随机
    Random vmRandom = new Random();
    //输出流
    FileOutputStream fos = null;
    StringBuilder sb;

    public static int maxValue;
    public static int minValue;

    //Load the property file
    public void initialzeSetting() {
        try {
            property = new Properties();
            //读取VM开始时间、持续时间、任务数
            is = new BufferedInputStream(new FileInputStream("src/com/generaterequest/requestPro.pro"));
            property.load(is);
            if (is != null)
                is.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
            e.printStackTrace();
        }

        //Load the value from the propertyFile;
        vmSize = getProperties("RequestNum");
        startTime = getProperties("StartTime");
        minSpan = getProperties("MinSpan");

        //最大VM结束时间？
        //LoadBalanceFactory.MAXTIME = 4 * (startTime + minSpan);
        LoadBalanceFactory.MAXTIME = 2 * vmSize * startTime +  2 * minSpan;
    }

    public int getProperties(String propertyName) {
        String s = property.getProperty(propertyName);
        return Integer.parseInt(s);
    }

    /**
     * @return StringBulider stores the contents of VM requests
     */
    public StringBuilder generaterequest() {
        initialzeSetting();
        int start = 0;
        int end = 0;
        int vmType = 1;
        float tempvmType = 0.0f;
        sb = new StringBuilder();
        vmInfo = new VmInfo();
        //得到各vmtype产生的比例
        l_arrayPorba = vmInfo.getVmTypeProbabilitySpan();

        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;

//		for (int i=0;i<l_arrayPorba.length;i++){
//			System.out.println(l_arrayPorba[i]);
//		}

        /*
         * Producing VM requests
         * Producing VM type, start time, end time in order,
         * and end time should be bigger than start time.
         * The VM type would be decide by the predefined probability
         */
        int firsttime = 0;
        for (int i = 0; i < vmSize; i++) {
            //Using reflex methods to substitute the old methods.
            //System.out.println("===================================:"+ distribution);
            try {
                //实现类的链接、装载；然后实例化在distribute.newInstance()这里
                //手动加载类接口，初始化distribution类，使jvm可以调用他，同时使其可以指向任意数据类型
                Class<?> distribute = Class.forName(distribution);
                //传入有一个元素的class数组
                Class<?> paraType[] = new Class[1];
                //int类对象
                paraType[0] = Integer.TYPE;
                //从distribute对象中获得nextInt方法；其中paraType=int.class，表示nextInt方法的形参类对象
                Method meth = distribute.getMethod("nextInt", paraType);

                //动态调用；根据分布方法产生开始时间和结束时间
//                start = (Integer) meth.invoke(distribute.newInstance(), startTime);
//                if(start < 0 ) start = 0;
//                if(start > 2 * startTime) start = 2 * startTime;
//                end = (Integer) meth.invoke(distribute.newInstance(), startTime);
//                //end = (Integer) meth.invoke(distribute.newInstance(), minSpan);
//                if(end < 0 ) end = 0;
//                if(end > 2 * startTime) end = 2 * startTime;
//                //end += start;
//                if (end <= start) {
//                    end = 2 * startTime  + (Integer) meth.invoke(distribute.newInstance(), minSpan);
//                }
//                if(end > 4 * startTime ) end = 4 * startTime;

                //使用原作者方法
//                start =(Integer) meth.invoke(distribute.newInstance(), startTime);
//                if(start < 0) start = 0;
//                if(start > 2 * startTime) start = 2 * startTime;
//                end =(Integer) meth.invoke(distribute.newInstance(), startTime);
//                if(end < 0) end = 0;
//                if(end > 2 * startTime) end = 2 * startTime;
//                if(end <= start){
//                    int span = (Integer) meth.invoke(distribute.newInstance(), minSpan);
//                    if(span < 0) span = 0;
//                    if(span > 2 * minSpan) span = 2 * minSpan;
//                    end = start + span;
//                }

                //正态分布
//                start = (Integer)meth.invoke(distribute.newInstance(),startTime);
//                if(start < 0 ) start = 0;
//                if(start > 2 * startTime) start = 2 * startTime;
//
//                end = (Integer) meth.invoke(distribute.newInstance(), minSpan);
//                if(end < 0 ) end = 0;
//                if(end > 2 * minSpan) end = 2 * minSpan;
//
//                end += start;
                // ---------------

                //泊松分布
                start = (Integer)meth.invoke(distribute.newInstance(),startTime);
                firsttime += start;
                start = firsttime;

                end = (Integer) meth.invoke(distribute.newInstance(), minSpan);
                if(end > 2 * minSpan) end = 2 * minSpan;
                end += start;
                // -----------------------


                minValue = Math.min(minValue,start);
                maxValue = Math.max(maxValue,end);


//                //结束时间的测试算法
//                if(start<0) start = 0;
//                end = start+(Integer) meth.invoke(distribute.newInstance(), minSpan);

                //获得不同VM的随机值，后续产生不同类型的VM机;
                //tempvmType = ((Integer) meth.invoke(distribute.newInstance(), 1000)) / 1000f;
                //由于各虚拟机产生的概率应相等，故必须采用随机分布
                tempvmType = UniformDistri.nextInt(1000)/1000f;
            } catch (Throwable e) {
                System.err.println(e);
            }

            if ((tempvmType >= 0) && (tempvmType <= l_arrayPorba[0]))
                vmType = 1;
            else if ((tempvmType > l_arrayPorba[0]) && (tempvmType <= l_arrayPorba[1]))
                vmType = 2;
            else if ((tempvmType > l_arrayPorba[1]) && (tempvmType <= l_arrayPorba[2]))
                vmType = 3;
            else if ((tempvmType > l_arrayPorba[2]) && (tempvmType <= l_arrayPorba[3]))
                vmType = 1;
            else if ((tempvmType > l_arrayPorba[3]) && (tempvmType <= l_arrayPorba[4]))
                vmType = 2;
            else if ((tempvmType > l_arrayPorba[4]) && (tempvmType <= l_arrayPorba[5]))
                vmType = 3;
            else if ((tempvmType > l_arrayPorba[5]) && (tempvmType <= l_arrayPorba[6]))
                vmType = 1;
            else vmType = 2;

            sb.append(String.valueOf(i) + " " + start + " " + end + " " + vmType + "\n");
        }
        //System.out.println("Distribution mode:"+distribution);
        System.out.println(sb.toString());
        //DataCenterFactory.print.println(sb.toString());
        return sb;
    }

    /*
     * Write the content of sb into txt file
     */
    public void writeToTxt(StringBuilder sb) {
        String txtContent;
        txtContent = sb.toString();
        File txt1 = new File("src/com/generaterequest/vmRequest.txt");
        if (!txt1.exists()) {
            try {
                txt1.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        byte bytes1[] = new byte[1024];
        bytes1 = txtContent.getBytes();
        int b1 = txtContent.length();
        try {
            fos = new FileOutputStream(txt1);
            fos.write(bytes1, 0, b1);
            fos.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public int getVmSize() {
        return vmSize;
    }

    public void setDistribution(String distribution) {
        this.distribution = "com.distribute." + distribution;

    }

    public String getVMInfo() {
        return "Distribution:" + distribution + " VM number: " + vmSize;
    }

    public static void main(String[] args) {
        CreateVM cv = new CreateVM();
        cv.setDistribution("NormalDistri");
        StringBuilder sb = cv.generaterequest();
        cv.writeToTxt(sb);
    }
}
