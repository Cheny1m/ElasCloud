package com.generaterequest;

import java.io.*;
import java.util.Properties;

import com.datacenter.LoadBalanceFactory;

/**
 * 
 * @author Minxian
 * 2012.11.26 eve, quite a sunny day~~
 * Convert the LLNL to our lab specification
 * Why use BufferedReader instead of others? Just because I am currently reading the sub-chapter about 
 * Java IO System of Thinking in Java, and it introduced a practical tool as BufferedReader class to
 * show a better performance on reading and writing. 
 */
public class CreateLLNLRequests implements RWCreateVM{
	/**
	 * 
	 */
	int vmSize;
	int startTime;
	int minSpan;
	Properties property;
	InputStream is;
	
	public CreateLLNLRequests(){
		initialzeSetting();
	}
	/*
	 * Refactor method to be corresponding with other methods
	 */
	public StringBuilder generaterequest(){
		return 	read("src/com/generaterequest/LLNL-Thunder-2007-1.1-cln.swf"); //LLNL original log file
	}
	/*
	 * Refactor method to be corresponding with other methods
	 */
	public void writeToTxt(StringBuilder sb){
		write("src/com/generaterequest/vmRequest.txt",sb); //output file
	}
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
			LoadBalanceFactory.MAXTIME = (startTime + minSpan) * 100; 
		}
	
	public int getProperties(String propertyName){
		String s = property.getProperty(propertyName);
		return Integer.parseInt(s);
	}
	
	public StringBuilder read(String fileName) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sb1 = new StringBuilder();
		int lines = 0;            // Effective Lines
		final long requestNumber = vmSize; //Can be configured, you can get the number of requests 
										//as you want, but should be less than 
										//128662 according to the LLNL original log file
		
		final long timeSlots = 60;   //In the LLNL original log file, the start time is in
									 //seconds, the time should be divided by this value to 
									// get the time slots as we defined, here assumed 300sec(5 mins) a slot  
		
//		final long maxProcs = 4008;  //In the LLNL original log file, they assumed, max processors 
									//as 4008, the lineContent[8] is the number of processors a request
									//needs, lineContent[8] should be divided by this value to get 
									//percentage of resource it needs
		long requestID = 0;
		long startTime, endTime;
		float resourcePercent;
		int pmType;
		long benchmark = 201800/timeSlots + 7000;;
		
		try{
			BufferedReader in = new BufferedReader(new FileReader(
					new File(fileName).getAbsoluteFile()));
			try{
				
				String s;
				//Eliminate the influence of ineffective first 20 lines
				for(int i = 0; i <2522 ; i++){
					in.readLine();
				}
				
				while((s = in.readLine()) != null && lines < requestNumber){
					//lineContent[1] is ID, positive number start from 1,
					//lineContent[2] is start time in seconds
					//lineContent[4] is duration time in seconds
					//lineContent[9] is number of processors needed about 0~
					String lineContent[] =  s.split("\\s++");
					//sb.append(line[0]+" "+ line[1]+" "+line[2]+" "+line[4]);
					startTime = Long.parseLong(lineContent[2])/timeSlots ; //Convert type and 
																		//divide time slots as defined
					endTime = startTime + Long.parseLong(lineContent[4])/timeSlots+1; //In the original file, this parameter
																				 //represents duration
																					//Since duration maybe less than 300s,
					if(endTime - startTime >= 30 && Long.parseLong(lineContent[2]) != -1) {														//end time should +1 at end
						lines++;
						resourcePercent = Float.parseFloat(lineContent[8]);
					
					if(resourcePercent <= 0)  resourcePercent = 0; //Avoid some bad requests in log file like -1 resourcePercetage
					if(resourcePercent >=0 && resourcePercent < 8) pmType = 1; //pmType should be defined earlier 
					else if (resourcePercent >= 8 && resourcePercent < 16) pmType =2; 
					else if (resourcePercent >= 16 && resourcePercent < 24) pmType =3; 
					else if (resourcePercent >= 24 && resourcePercent < 100) pmType =1; 
					else if (resourcePercent >= 100 && resourcePercent < 200) pmType =2;  
					else if (resourcePercent >= 200 && resourcePercent < 300) pmType =3; 
					else if  (resourcePercent >= 300 && resourcePercent < 400) pmType = 1;
					 else pmType = 2;
					 sb.append(requestID++ +" "+ (startTime-benchmark)+" "+(endTime-benchmark)+" "+pmType+"\n");
					 
					 sb1.append(pmType+" "+(startTime-benchmark)+" "+(endTime-benchmark)+"\n");
					 
					}
				}
			}finally{
				 System.out.println(sb);
				 System.out.println("------------------");
				 System.out.println(sb1);
					in.close();
				}
			}catch(IOException e){
					throw new RuntimeException(e);
				}
			//return sb.toString();
			return sb;
		}
	
	public void write(String fileName, StringBuilder text){
		try {
			PrintWriter out = new PrintWriter(
					new File(fileName).getAbsoluteFile());
			try{
				out.print(text.toString());
			}finally {
				out.close();
			}
		}catch(IOException e){
				throw new RuntimeException(e);
			}
		}
	
	
	public static void main(String[] args) {
		CreateLLNLRequests clr = new CreateLLNLRequests();
		clr.writeToTxt(clr.generaterequest());
	}
}
