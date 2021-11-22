/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudscheinterface;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author LukeXu
 */
public class ConfVMSpec {
    Properties property;
	InputStream is;
	FileOutputStream fos = null;
	int startTime, avgDuration, vmNum;
	ArrayList<Integer> vmSpec = new ArrayList<Integer>();
	//Path should not be changed.
	final String requestProFilePath = "src/com/generaterequest/requestPro.pro";
	final String requestProOriginFilePath = "src/com/generaterequest/requestProOrigin.pro";
	public ConfVMSpec(){
		try{
			property = new Properties();
			is = new BufferedInputStream(new FileInputStream(requestProOriginFilePath));
		
			property.load(is);
			if(is != null)
				is.close();
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
		}
		
	public ArrayList<Integer> getVMSpec(){

				/*
				 * Load the value from the propertyFile;
				 */
				startTime = getProperties("StartTime");
				vmSpec.add(startTime);
				avgDuration = getProperties("MinSpan");
				vmSpec.add(avgDuration);
				vmNum = getProperties("RequestNum");
				vmSpec.add(vmNum);
				
				return vmSpec;

			}
	
	public int getProperties(String propertyName){
			String s = property.getProperty(propertyName);
			return Integer.parseInt(s);
	}
	/**
	 * Change the value in the property file by interface settings.
	 */
	public void setProperties(String type, Object num){
		try{
			FileOutputStream fos = new FileOutputStream(requestProFilePath);

			property.setProperty(type, String.valueOf(num));
			property.store(fos, null);
			fos.close();
			}catch(IOException e){
				System.out.print(e.getMessage());
				e.printStackTrace();
			}
		

	}
}
