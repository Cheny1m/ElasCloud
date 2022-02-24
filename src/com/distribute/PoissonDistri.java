package com.distribute;


/**
 * Poisson Distribution with P(X=k) = (exp(-lamda)*(lamda)^k)/k!
 * See link: http://zhidao.baidu.com/question/92917092.html&__bd_tkn__=5cab4b637961de6d441bbb6fabfe29a0cb0081e08078338d51fed8133ea5c69d362ad36bb4bcda3b39bb3949f6bbe47087ac3af56e60b1f4e7eb6015715ef4339a67a0fa40491fc7006f377bd441bc7b3d7f9973702cc8fbd145447a0229465db8610a3738c0dfac947beabbc9da8802cf3121f248
 * @author Minxian
  */

public class PoissonDistri {

	int lamda;//lamda, the average value of a sequence numbers；
	double poissonProbability;
	int k;
	public int nextInt(int number){
			lamda = number / 2;
			poissonProbability = Math.exp(-lamda); 
			k = 0;
			while(true)
			{
				double randValue = Math.random();
				if( poissonProbability > randValue)
					break;
				else
				{	//Iterative process
					poissonProbability = poissonProbability*(1.0*lamda / (k+1));
					k++;
				}
				if( k >= 5 * lamda ) //Constraint the numbers in a range
				{
					k = 0;
					poissonProbability = Math.exp(-lamda); 
				}
			}
		return k;
	}

	public static void main(String[] args){
		ExponentialServiceTime nd = new ExponentialServiceTime();
		//输入值
		int number = 5;
		double y=0;
		int count = 0;
		int Totalcount = 100000;
		double[] sum = new double[Totalcount+5];

		double MeanValue;
		double StandardDeviation = 0;
		//验证均值
		for(int i = 0; i < Totalcount; i++){
			System.out.println(nd.nextInt(number));
			sum[i] = nd.nextInt(number);
			y=y+sum[i];
		}
		MeanValue = y / Totalcount;
		//验证标准差
		for(int i = 0 ; i < Totalcount ; i++){
			StandardDeviation += (sum[i] - MeanValue)*(sum[i] - MeanValue);
		}
		StandardDeviation = Math.sqrt(StandardDeviation/Totalcount);
		System.out.println("平均值为:" + MeanValue);
		System.out.println("标准差为:"+ StandardDeviation);
	}
}
