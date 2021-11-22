package com.generaterequest;

import com.datacenter.LoadBalanceFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
/**
 * This class is used to acquire the number of each kind of PMs from the local
 * configuration file. The configuration is pmNum.pro containing the numbers of
 * PMs. If the numbers need to be modified, editing that file rather than write 
 * codes.
 * @author Minxian
 *
 */
public class PMBootor {
	
	Properties property;
	InputStream is;
	FileOutputStream fos = null;
	int PMNumOne, PMNumTwo, PMNumThree;
	ArrayList<Integer> PMNum = new ArrayList<Integer>();
	//Path should not be changed.
	final String pmNumFilePath = "src/com/generaterequest/pmNum.pro";
	final String pmNumFilePath1 = "src/com/generaterequest/pmNumOrigin.pro";
	public PMBootor(){

		}
		
	public ArrayList<Integer> bootPM(){

				/*
				 * Load the value from the propertyFile;
				 */
            		try{
			property = new Properties();
//			is = new BufferedInputStream(new FileInputStream(pmNumFilePath));
		
			property.load(new FileInputStream(pmNumFilePath));
			if(is != null)
				is.close();
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
                                 
				PMNumOne = getProperties("type1");
				PMNum.add(PMNumOne);
				PMNumTwo = getProperties("type2");
				PMNum.add(PMNumTwo);
				PMNumThree = getProperties("type3");
				PMNum.add(PMNumThree);
				//System.out.println(PMNum.get(0) + " " + PMNum.get(1) + " " + PMNum.get(2));
				return PMNum;

			}
	
        public ArrayList<Integer> bootPMFromOrig(){
            				/*
				 * Load the value from the  origin propertyFile;
				 */
            		try{
			property = new Properties();
			property.load(new FileInputStream(pmNumFilePath1));
			if(is != null)
				is.close();
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
                                
				PMNumOne = getProperties("type1");
				PMNum.add(PMNumOne);
				PMNumTwo = getProperties("type2");
				PMNum.add(PMNumTwo);
				PMNumThree = getProperties("type3");
				PMNum.add(PMNumThree);
				//System.out.println(PMNum.get(0) + " " + PMNum.get(1) + " " + PMNum.get(2));
				
				return PMNum;
        }
        
	public int getProperties(String propertyName){
			String s = property.getProperty(propertyName);
			//System.out.println(propertyName + ": " + s);
			return Integer.parseInt(s);
	}
	/**
	 * Change the value in the property file by interface settings.
	 */
	public void setProperties(String type, Object num){
		try{
			FileOutputStream fos = new FileOutputStream(pmNumFilePath);

			property.setProperty(type, String.valueOf(num));
			property.store(fos, null);
			fos.close();
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
		

	}
		
}
