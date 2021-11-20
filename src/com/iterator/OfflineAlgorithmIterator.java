package com.iterator;

import com.schedule.loadbalance.OfflineAlgorithm;
import java.util.ArrayList;

/**
 * Picking out all the needed compared algorithm into iterator.
 * @author Minxian
 *
 */
public class OfflineAlgorithmIterator implements Iterator{
	ArrayList<OfflineAlgorithm> aofa; 
	int position = 0;
	
	public OfflineAlgorithmIterator(ArrayList<OfflineAlgorithm> aofa){
		this.aofa = aofa;
	}
	public boolean hasNext() {
		if(position >= aofa.size() || aofa.get(position) == null){
			return false;
		}
		else{
			return true;
		}
		// TODO Auto-generated method stub
	}

	public Object next() {
		// TODO Auto-generated method stub
		OfflineAlgorithm ci = aofa.get(position);
		position += 1;
		return ci;
	}

}


