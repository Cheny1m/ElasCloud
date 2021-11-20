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
import com.specification.*;
import com.datacenter.*;
/**
 * This class is the base class for creating VM requests.
 * The starting time span, requests number, average span are loaded from
 *  requestPro.pro. Starting time is a random number in predefined span. 
 *  Average span in a random value in predefined scope.
 *  End time should be larger than starting time. 
 * @author Minxian
 *
 */
public class CreateVM implements RWCreateVM{
	
	int vmSize;
	int startTime;
	int minSpan;
	float[] l_arrayPorba;
	String distribution = "com.distribute.NormalDistri";
        
	Properties property;
	InputStream is;
	VmInfo vmInfo;
	Random vmRandom = new Random();
	FileOutputStream fos = null;
	StringBuilder sb;
	
	
	/**
	 * Load the property file
	 */
	public void initialzeSetting(){
		try{
			property = new Properties();
			is = new BufferedInputStream(new FileInputStream(
					"src/com/generaterequest/requestPro.pro"));
		
			property.load(is);
			if(is != null)
				is.close();
                        
                        
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
			/*
			 * Load the value from the propertyFile;
			 */
			vmSize = getProperties("RequestNum");
			startTime = getProperties("StartTime");
			minSpan = getProperties("MinSpan");
			LoadBalanceFactory.MAXTIME = startTime + minSpan; 
		}
	
	public int getProperties(String propertyName){
		String s = property.getProperty(propertyName);
		return Integer.parseInt(s);
	}
	
	/**
	 * 
	 * @return StringBulider stores the contents of VM requests
	 */
	public StringBuilder generaterequest(){
		initialzeSetting();
		int start = 0;
		int end = 0;
		int vmType = 1;
		float tempvmType = 0.0f;;
		sb = new StringBuilder();
		vmInfo = new VmInfo();
		l_arrayPorba = vmInfo.getVmTypeProbabilitySpan();

		
		/*
		 * Producing VM requests
		 * Producing VM type, start time, end time in order,
		 * and end time should be bigger than start time.
		 * The VM type would be decide by the predefined probability
		 */		
		for(int i = 0;i < vmSize;i++){
			//Using reflex methods to substitute the old methods.
			try {
		           Class<?> distribute = Class.forName(distribution);
		           Class<?> paraType[] = new Class[1];
		           paraType[0] = Integer.TYPE;
		           Method meth = distribute.getMethod("nextInt", paraType);

		           start =(Integer) meth.invoke(distribute.newInstance(), startTime);
		           end =(Integer) meth.invoke(distribute.newInstance(), startTime);
		           if(end <= start){
		        	   end = startTime + (Integer) meth.invoke(distribute.newInstance(), minSpan);
		           }
		           tempvmType = ((Integer) meth.invoke(distribute.newInstance(), 1000))/1000f;
		       } catch (Throwable e) {
		           System.err.println(e);
		       }		
			
			if((tempvmType >= 0)&&(tempvmType<= l_arrayPorba[0])) 
				vmType=1;
			else if((tempvmType > l_arrayPorba[0])&&(tempvmType <= l_arrayPorba[1])) 
				vmType=2;
			else if((tempvmType > l_arrayPorba[1])&&(tempvmType <= l_arrayPorba[2])) 
				vmType=3;
			else if((tempvmType > l_arrayPorba[2])&&(tempvmType <= l_arrayPorba[3])) 
				vmType=1;
			else if((tempvmType > l_arrayPorba[3])&&(tempvmType <= l_arrayPorba[4])) 
				vmType=2;
			else if((tempvmType > l_arrayPorba[4])&&(tempvmType <= l_arrayPorba[5])) 
				vmType=3;
			else if((tempvmType > l_arrayPorba[5])&&(tempvmType <= l_arrayPorba[6])) 
				vmType=1;
			else vmType=2;

			sb.append(String.valueOf(i)+" "+ start+" "+end+" " +vmType+"\n");
		}
                DataCenterFactory.print.println(sb.toString());
		return sb;
	}
	/*
	 * Write the content of sb into txt file 
	 */
	public void writeToTxt(StringBuilder sb){
		String txtContent;
		txtContent = sb.toString();
		File txt1=new File("src/com/generaterequest/vmRequest.txt");
		if(!txt1.exists()){
			try {
				txt1.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		byte bytes1[]=new byte[1024];
		bytes1=txtContent.getBytes();   
		int b1=txtContent.length();  
		try {
			fos=new FileOutputStream(txt1);
			fos.write(bytes1,0,b1);
			fos.close();
		} 
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public int getVmSize(){
		return vmSize;
	}
	
        public  void setDistribution(String distribution){
            this.distribution = "com.distribute." + distribution;
            
        }
        
        public String getVMInfo(){
            return "Distribution:" + distribution + " VM number: "+ vmSize;
        }
	public static void main(String[] args){
		CreateVM cv = new CreateVM();
                cv.setDistribution("NormalDistri");
		StringBuilder sb = cv.generaterequest();
		cv.writeToTxt(sb);
	}


}
