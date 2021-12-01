package com.datacenter;

import com.generaterequest.CreateVM;
import com.generaterequest.PMBootor;
import com.schedule.loadbalance.OfflineAlgorithm;
import com.schedule.loadbalance.OnlineAlgorithm;

/**
 * This class is the basic class factory to produce factories for LoadBalance
 * Factory and EnergySaving Factory. In this class, the general data center 
 * processing flows are included.
 * 1. Requests are produced by CreateVM in particular creating algorithm;
 * 2. Initialize Physical Machines can provide services and resources;
 * 3. Load the VM requests from request file ;
 * 4. Use different algorithm to schedule the requests;
 * 5. Calculate the value of needed compared indices;
 * 6. Output the needed results.
 * In FlexCloud, some design patterns are used: 
 * Decorator, Abstract Factory, Strategy, Iterator design patterns. 
 * 
 * @author Minxian
 *
 */
public interface DataCenterImp {
	/**
	 * Creating the VM requests based on configurations
	 * @param cv
	 */
	public void createVM(CreateVM cv);

	/**
	 * Boot up PMs, since it is simulation rather than in real environment,
	 * booting means instances of Physical Machine have been produced.
	 * @param pmb
	 */
	//引导产生虚拟环境的pm
	public void bootPM(PMBootor pmb);
	
	/**
	 * Allocate VM requests to corresponding PM
	 * 将VM请求分配给相应的PM
	 * @param aa
	 */	
	public void allocate(OnlineAlgorithm onla);
	
	public void allocate(OfflineAlgorithm ofla);
	/**
	 * Generating the VM requests from the creating VM source file
	 */
	public void generateReuquest();
	
	/**
	 * Show the calculated indices results
	 */
	public void showIndex();
	/**
	 * Scheduling description
	 * @return
	 */
	public String getDescription();
	
}
