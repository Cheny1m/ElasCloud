package com.generaterequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import com.resource.VirtualMachine;
/**
 * This class is used to load requests information from vmRequest.txt file into
 * a VirtualMachine arraylist. This class would be generated in LoadBalanceFactory.
 * @author Minxian
 *
 */
public class RequestGenerator {
	
		public ArrayList<VirtualMachine> generateRequest(){
			ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
			File file = new File("src/com/generaterequest/vmRequest.txt");
			if (!file.exists()) {
				System.out.println("can not find file");
				System.exit(0);
			}
			
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String string;
				String s[];
				/*
				 * Key codes for this class by adding requests into Virtual Machine
				 * list. Parameters are (vmNo, starTime, endTime, vmType)
				 */
				while ((string = in.readLine()) != null) {
					s = string.split(" ");
					VirtualMachine vm=new VirtualMachine(Integer.parseInt(s[0]),
							Integer.parseInt(s[1]),Integer.parseInt(s[2]),Integer.parseInt(s[3]));
					vmQueue.add(vm);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return vmQueue;
		}
		
}
