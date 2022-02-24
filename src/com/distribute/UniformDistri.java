package com.distribute;

import java.util.Random;
/**
 * Create number in uniform distribution with nextInt() of class Random
 * @author LukeXu
 *
 */
public class UniformDistri {
	static Random random = new Random();
	public static int nextInt(int number){
		return random.nextInt(number);
	}
	
	public static void main(String[] args){
		UniformDistri uniformDis = new UniformDistri();
		double y = 0;
		for(int i = 0; i < 100; i++){
			System.out.println(uniformDis.nextInt(101)/100f);
			y += uniformDis.nextInt(100);
		}
		System.out.println("平均值：" + y/100);
	}
}
