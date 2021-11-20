package com.schedule.energysaving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.datacenter.LoadBalanceFactory;
import com.generaterequest.CreateVM;
import com.generaterequest.CreateVMByEndTime;
import com.generaterequest.PMBootor;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import com.schedule.loadbalance.OfflineAlgorithm;


/**
 * The energy saving series algorithm,based on LPT(Long Processing Time First). Requests would 
 * be sequenced with decreasing order by processing time  and be allocated to PM with lowest utility.
 * Different from the online series algorithm, indices are quite different. Some requests may be rejected
 * int this scheduling process. More methods would be added into class PhysicalMachine. 
 * @author Minxian
 *
 */
public class EnergySavingEDF extends OfflineAlgorithm{
	
	int index; 	//Allocated PM ID
	int currentTime = 0;
	int vmId = 0;  	//vmId is the the id in sorted in vmQueue
	int pmTotalNum;
	int increase = 1;
	int decrease = -1;
        int rejectedVM ;
	Random random = new Random();
	VirtualMachine vm;
	
	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	ArrayList<PhysicalMachine> pmQueueOne = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueTwo = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueThree = new ArrayList<PhysicalMachine>();
	
	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();
	
	public EnergySavingEDF(){
	//	System.out.println(getDescription());
	}
	
    @Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description + "-EDF Algorithm---";
	}
    
	@Override
    public void createVM(LoadBalanceFactory lbf) {
		lbf.createVM(new CreateVMByEndTime(new CreateVM()));
    }
	/**
	 * Generate the random index and try to allocate VM to the PM with generated
	 * index.
	 */
    @Override
	public void allocate(ArrayList<VirtualMachine> p_vmQueue,
			ArrayList<PhysicalMachine> p_pmQueueOne,
			ArrayList<PhysicalMachine> p_pmQueueTwo,
			ArrayList<PhysicalMachine> p_pmQueueThree) {
		// TODO Auto-generated method stub
		this.vmQueue = p_vmQueue;
		this.pmQueueOne = p_pmQueueOne;
		this.pmQueueTwo = p_pmQueueTwo;
		this.pmQueueThree = p_pmQueueThree;
		
	//	System.out.println(pmQueueOne.get(0).resource.get(0).getCpuUtility());
	//	System.out.println(pmQueueOne.get(0).resource.get(0).getMemUtility());
	//	System.out.println(pmQueueOne.get(0).resource.get(0).getStoUtility());
		
		pmTotalNum = pmQueueOne.size() + pmQueueTwo.size() + pmQueueThree.size();
		while(!vmQueue.isEmpty()){
			//Pick out the vm with startTime less than currentTime to allocate
			if(currentTime >= vmQueue.get(vmId).getStartTime()){
				vm = vmQueue.get(vmId);
			}
			else{
				vmId++;
			 //Refactored method, see details in definition.
				checkVmIdAvailable();
				continue;
			}
				
			//Randomly find a PM
			index = 0;
			LoadBalanceFactory.print.println("LPT-Index:"+index);
			//Three queues should be decided which queue would be added.
			if( vm.getVmType() > 0 && vm.getVmType() <= 3){
				allocateVm(vm, pmQueueOne.get(index));
			}
			else if( vm.getVmType() > 3 && vm.getVmType() <= 6 ){
				allocateVm(vm, pmQueueTwo.get(index));
			}
			else{
				allocateVm(vm, pmQueueThree.get(index));
			}
		}
		LoadBalanceFactory.print.println(LoadBalanceFactory.FINISHEDINFO);
	}
	
	/**
	 * Key scheduling procedure for algorithm. Main procedures are as below:
	 * 1. Check whether resource of a PM is available.
	 * 2. If resource available, output success information. Put the VM to
	 * deleteQueue, and remove that VM from vmQueue.
	 * 3. Update available resource of PM. 
	 * 
	 * @param vm2
	 * @param pm2
	 */
	private void allocateVm(VirtualMachine vm2, PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		if(checkResourceAvailble(vm2, pm2)){
		LoadBalanceFactory.print.println("Allocate:VM" + vm2.getVmNo()+ " to PM" + pm2.getNo());
		deleteQueue.add(vm2);
		vmQueue.remove(vm2);	
		pm2.vms.add(vm2);
		vm2.setPmNo(pm2.getNo());
		updateResource(vm2, pm2, decrease);
                sortPM(vm2);
                
		vmId++; 
		checkVmIdAvailable();
		}
		else{
                        vmQueue.remove(vm2); //Though the request can not be allocated, that VM should be removed.
                        LoadBalanceFactory.print.println("VM is rejected");
                        rejectedVM = pm2.getRejectedNum();
                        pm2.setRejectedNum(rejectedVM + 1);
                        
                        vmId++;
                        checkVmIdAvailable();
		}
	}
	
	
	/**
	 * Check whether the vmId has surpassed bound, if yes, reset vmId
	 * as 0. 
	 */
	private void checkVmIdAvailable() {
		if(vmId >= vmQueue.size()){
			currentTime++;
			vmId = 0;
			LoadBalanceFactory.print.println("===currentTime:"+currentTime+"===");
			processDeleteQueue(currentTime, deleteQueue);
		}
	}

	/**
	 * Check whether the left resource are available
	 * @param vm3
	 * @param pm3
	 * @return
	 */
	private boolean checkResourceAvailble(VirtualMachine vm3, PhysicalMachine pm3) {
		boolean allocateSuccess = true;
		boolean oneSlotAllocation;
		for(int t = vm3.getStartTime(); t < vm3.getEndTime(); t++){
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() > vm3.getCpuTotal()) &&
								(pm3.resource.get(t).getMemUtility() > vm3.getMemTotal()) &&
								(pm3.resource.get(t).getStoUtility() > vm3.getStorageTotal());
			allocateSuccess = allocateSuccess && oneSlotAllocation;
		//	System.out.println(pm3.resource.get(t).getCpuUtility() +" "+ vm3.getCpuTotal());
		//	System.out.println(pm3.resource.get(t).getMemUtility() +" "+ vm3.getMemTotal());
		//	System.out.println(pm3.resource.get(t).getStoUtility() +" "+ vm3.getStorageTotal());

			if(false == allocateSuccess){
				//If allocated failed, return exactly.
				return allocateSuccess;
			}
		}
		return allocateSuccess;
	}
	
	/**
	 * Update the available resource. When parameter 3 equals to increase, available resource
	 * would increased, else resource would be decreased.
	 * @param vm4s
	 * @param pm4
	 * @param incOrDec
	 */
	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4, int incOrDec){
		if(incOrDec == decrease){
		for(int t = vm4.getStartTime(); t < vm4.getEndTime(); t++){
			pm4.resource.get(t).setCpuUtility(
					pm4.resource.get(t).getCpuUtility() - vm4.getCpuTotal());
			pm4.resource.get(t).setMemUtility(
					pm4.resource.get(t).getMemUtility() - vm4.getMemTotal());
			pm4.resource.get(t).setStoUtility(
					pm4.resource.get(t).getStoUtility() - vm4.getStorageTotal());
			}
			LoadBalanceFactory.print.println("Resource is updated(dec)");
		}
		if(incOrDec == increase){
			for(int t = vm4.getStartTime(); t < vm4.getEndTime(); t++){
				pm4.resource.get(t).setCpuUtility(
						pm4.resource.get(t).getCpuUtility() + vm4.getCpuTotal());
				pm4.resource.get(t).setMemUtility(
						pm4.resource.get(t).getMemUtility() + vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(
						pm4.resource.get(t).getStoUtility() + vm4.getStorageTotal());
			}
			LoadBalanceFactory.print.println("Remove:VM" + vm4.getVmNo() + " from PM" + pm4.getNo());
			LoadBalanceFactory.print.println("Resource is updated(inc)");

		}
	}
	
	/**
	 * After the VM has been added to deleteQueue, if end time comes, that VM should
	 * be removed from deleteQueue. Available resource should also be updated.
	 * @param p_currentTime
	 * @param p_deleteQueue
	 */
	private void processDeleteQueue(int p_currentTime, ArrayList<VirtualMachine> p_deleteQueue) {
		// TODO Auto-generated method stub
		VirtualMachine vm5;
		PMBootor pmb1 = new PMBootor();
		ArrayList<Integer> pmNum= pmb1.bootPM();
		int pmNo;
		
		for(int i = 0; i < p_deleteQueue.size(); i++){
			vm5 = p_deleteQueue.get(i);
			pmNo = vm5.getPmNo();
			if(p_currentTime >= vm5.getEndTime()){
				if(pmNo >= 0 && pmNo < pmNum.get(0)){
                                    for(int j = 0; j < pmNum.get(0); j++){
					if(pmNo == pmQueueOne.get(j).getNo()){
                                        updateResource(vm5, pmQueueOne.get(j), increase);
                                        break;
                                        }
                                    }
				}
				else if(pmNo >= pmNum.get(0) && pmNo < pmNum.get(0) + pmNum.get(1)){
                                        for(int j = 0; j < pmNum.get(1); j++){
                                            if(pmNo == pmQueueTwo.get(j).getNo()){
                                            updateResource(vm5, pmQueueTwo.get(j), increase);
                                            break;
                                            }
                                        }
				}
				else{
                                        for(int j = 0; j < pmNum.get(2); j++){
                                            if(pmNo == pmQueueThree.get(j).getNo()){
                                            updateResource(vm5, pmQueueThree.get(j), increase);
                                            break;
                                            }
                                        }
				}
				p_deleteQueue.remove(vm5);
			}
		}
	}

    private void sortPM(VirtualMachine vm1) {
        if(vm1.getVmType() > 0 && vm1.getVmType() <= 3) {
            Collections.sort(pmQueueOne, new SortByCurrentUtility(currentTime));
        }
        else if(vm1.getVmType() > 3 && vm1.getVmType() <= 6){
            Collections.sort(pmQueueTwo, new SortByCurrentUtility(currentTime));
        }
        else{
            Collections.sort(pmQueueThree, new SortByCurrentUtility(currentTime));
        }
    }
	
}