package com.iterator;

import java.util.ArrayList;

import com.schedule.loadbalance.OnlineAlgorithm;
/**
 * Picking out all the needed compared algorithm into iterator.
 * @author Minxian
 *
 */
public class OnlineAlgorithmIterator implements Iterator{
	ArrayList<OnlineAlgorithm> aoa; 
	int position = 0;
	
	public OnlineAlgorithmIterator(ArrayList<OnlineAlgorithm> aoa){
		this.aoa = aoa;
	}
	public boolean hasNext() {
		if(position >= aoa.size() || aoa.get(position) == null){
			return false;
		}
		else{
			return true;
		}
		// TODO Auto-generated method stub
	}

	public Object next() {
		// TODO Auto-generated method stub
		OnlineAlgorithm ci = aoa.get(position);
		position += 1;
		return ci;
	}

}


