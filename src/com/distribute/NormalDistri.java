package com.distribute;
/**
 * This class is used to create requests in normal distribute
 * @see http://www.360doc.com/content/06/0427/18/7445_106174.shtml
 * @author LukeXu
 *
 */
public class NormalDistri {
	int miu;//lamda, the average value of a sequence numbers
	int sigma2;
	
	public int nextInt(int number){
		miu = number / 2;
		sigma2 = number;
		double N = 12;
		  double x=0,temp=N;
		  do{
		   x=0;
		   for(int i=0;i<N;i++)
		    x=x+(Math.random());
		   x=(x-temp/2)/(Math.sqrt(temp/12));
		   x=miu+x*Math.sqrt(sigma2);
		   }while(x<=0);          
		   return (int)x;
	}

     public static void main(String[] args){
    	 NormalDistri nd = new NormalDistri();
    	 for(int i = 0; i < 100; i++){
    	 nd.nextInt(50);
    	 }
     }
}
