package com.resource;
/**
 * This is the base class for Physical Machine and Virtual Machine and defining 
 * common share attributes and methods like CPU, memory and bandwidth（带宽）
 * @author yuanliang, Minxian
 *
 */
public class Server {
	protected float cpu;
	protected float mem;
	protected float storage;
	protected float cpuTotal;
	protected float memTotal;
	protected float storageTotal;
	public float getCpu() {
		return cpu;
	}
	public void setCpu(float cpu) {
		this.cpu = cpu;
	}
	public float getMem() {
		return mem;
	}
	public void setMem(float mem) {
		this.mem = mem;
	}
	public float getStorage() {
		return storage;
	}
	public void setStorage(float storage) {
		this.storage = storage;
	}
	/**
	 * Left available resource
	 * @return
	 */
	public float getCpuTotal() {
		return cpuTotal;
	}
	public void setCpuTotal(float cpuTotal) {
		this.cpuTotal = cpuTotal;
	}
	public float getMemTotal() {
		return memTotal;
	}
	public void setMemTotal(float memTotal) {
		this.memTotal = memTotal;
	}
	public float getStorageTotal() {
		return storageTotal;
	}
	public void setStorageTotal(float storageTotal) {
		this.storageTotal = storageTotal;
	}
	
}
