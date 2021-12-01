package com.distribute;

import java.util.Random;
/**
 * This class is used to create requests in normal distribute
 * see http://www.360doc.com/content/06/0427/18/7445_106174.shtml
 * @author LukeXu
 *
 */

/*
//源代码描述的正态分布好像有问题？
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
    	 double y = 0;
    	 for(int i = 0; i < 10000; i++){
    	 	System.out.println(nd.nextInt(100));
			 y=y+nd.nextInt(100);
    	 }
		 System.out.println("平均值为");
		 System.out.println(y/10000);
     }
}
 */


/*
* @author Yueming Chen
 */
//这里采用高斯分布来进行随机数的产生。
public class NormalDistri {
	public int nextInt(int number) {
		number /= 2.0;
		Random r = new Random();
		double x = number/3.0*r.nextGaussian()+number;
		return (int)x;
	}

	public static void main(String[] args){
		NormalDistri nd = new NormalDistri();
		double y=0;
		for(int i = 0; i < 10000; i++){
			System.out.println(nd.nextInt(100));
			y=y+nd.nextInt(100);
		}
		System.out.println("平均值为:" + y/10000);
	}
}