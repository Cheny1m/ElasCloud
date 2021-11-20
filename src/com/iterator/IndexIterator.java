package com.iterator;

import java.util.ArrayList;
/**
 * Index iterator is used for collect needed comparison indices.
 * @author Minxian
 *
 */
public class IndexIterator implements Iterator{
	ArrayList<ComparisonIndex> aci; 
	int position = 0;
	
	public IndexIterator(ArrayList<ComparisonIndex> aci){
		this.aci = aci;
	}
	public boolean hasNext() {
		if(position >= aci.size() || aci.get(position) == null){
			return false;
		}
		else{
			return true;
		}
		// TODO Auto-generated method stub
	}

	public Object next() {
		// TODO Auto-generated method stub
		ComparisonIndex ci = aci.get(position);
		position += 1;
		return ci;
	}

}
