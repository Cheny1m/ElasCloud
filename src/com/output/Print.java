package com.output;

import cloudscheinterface.NewJFrame;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
/**
 * Standardize the ouputs. One line is console, and another line writting to
 * "src/com/output/output.txt".
 * @author Minxian
 *
 */
public class Print {
	PrintStream p;
	File output;

	public Print() throws IOException{
		output = new File("src/com/output/output.txt");
		if(!output.exists()){
			output.createNewFile();
		}
		
		try{
		p = new PrintStream(
				new BufferedOutputStream(new FileOutputStream(output, false)));
		
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public void println(String string){
		System.out.println(string);
                if(NewJFrame.jTextArea3 != null){
                NewJFrame.jTextArea3.append(string + "\n");
                }
		p.append(string + "\n");
	}
	
	public void print(String string){
		System.out.print(string);
                if(NewJFrame.jTextArea3 != null){
                NewJFrame.jTextArea3.append(string);
                }
		p.append(string);
	}
	public void closeFile(){
		p.close();
	}
	
}
