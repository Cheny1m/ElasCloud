package com.generaterequest;

import com.datacenter.LoadBalanceFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
/**
 * A decorator class for CreateVM.java, requests are produced by end time in
 * decreasing order.
 * @author Minxian
 *
 */
public class CreateVMByEndTime {
	RWCreateVM cv;

	public CreateVMByEndTime(RWCreateVM cv){
		this.cv = cv;
	}
	/*
	 * Overwrite the father methods, requests would be produced in
	 * another sequence
	 */
	public StringBuilder generaterequest(){
		cv.writeToTxt(cv.generaterequest());
		File file = new File("src/com/generaterequest/vmRequest.txt");
		if(!file.exists()){
                        LoadBalanceFactory.print.println("can not find file");
			System.exit(0);
		}
		
		ArrayList<VmRequest> vmRequest = new ArrayList<VmRequest>();
		try{
			BufferedReader in = new BufferedReader(new  FileReader(file));
			String string;
			String s[];
			in.readLine();
			while((string = in.readLine()) != null){
				s = string.split(" ");
				VmRequest vr = new VmRequest(
						Integer.parseInt(s[0]), Integer.parseInt(s[1]),
						Integer.parseInt(s[2]), Integer.parseInt(s[3]));
				vmRequest.add(vr);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		//Using the sorting rule decreasing order by end time
		Collections.sort(vmRequest, new SortByEndTime());
		
		StringBuilder sb1 = new StringBuilder();
		for(VmRequest vr: vmRequest){
			sb1.append(vr.getNo() + " " + vr.getStartTime() 
					+" " + vr.getEndTime() + " " + vr.getVmType() + "\n");
		}
		//System.out.println(sb1);
		return sb1;
	}
	public void writeToTxt(StringBuilder sb){
		cv.writeToTxt(sb);
	}
	
	public static void main(String[] args){
		CreateVMByEndTime cvbp = new CreateVMByEndTime(new CreateVM());
		StringBuilder sb1 = cvbp.generaterequest();
		cvbp.writeToTxt(sb1);
	}
}

/**
 * Overwrite the compare method to compare the elements in VmRequest,
 * and this class is the sorting rule by end time
 * @author Minxian
 *
 */
class SortByEndTime implements Comparator<VmRequest>{
       
	public int compare(VmRequest o1, VmRequest o2){
		VmRequest vr1 = o1;
		VmRequest vr2 = o2;
		if(vr1.getEndTime() < vr2.getEndTime())
			return 1;
		return 0;
	}
}


