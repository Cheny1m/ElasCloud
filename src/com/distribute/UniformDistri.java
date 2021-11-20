package com.distribute;

import java.util.Random;
/**
 * Create number in uniform distribution with nextInt() of class Random
 * @author LukeXu
 *
 */
public class UniformDistri {
	Random random = new Random();
	public int nextInt(int number){
		return random.nextInt(number);
	}
	
	public static void main(String[] args){
		UniformDistri uniformDis = new UniformDistri();
		for(int i = 0; i < 100; i++){
			System.out.println(uniformDis.nextInt(100));
		}
	}
}
