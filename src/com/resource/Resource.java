package com.resource;
/**
 * This class is used to store the available resource in each slot. 
 * ArrayList<Resource> is an attribute of class PhysicalMachine
 *	此类用于存储每个插槽中的可用资源
 * @author Minxian
 */
public class Resource {
	float cpuUtility;
	float memUtility;
	float stoUtility;

	Resource(float p_cpuUtility, float p_memUtility, float p_stoUtility){
		this.cpuUtility = p_cpuUtility;
		this.memUtility = p_memUtility;
		this.stoUtility = p_stoUtility;
	}
	
	public void setCpuUtility(float p_cpuUtility){ this.cpuUtility = p_cpuUtility; }
	public float getCpuUtility(){ return cpuUtility; }
	
	public void setMemUtility(float p_memUtility){
		this.memUtility = p_memUtility;
	}
	public float getMemUtility(){ return memUtility; }
	
	public void setStoUtility(float p_stoUtility){
		this.stoUtility = p_stoUtility;
	}
	public float getStoUtility(){ return stoUtility; }
}
