package com.schedule.loadbalance;

import com.datacenter.LoadBalanceFactory;
import com.generaterequest.PMBootor;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Random scheduling algorithm: round-robin generating a index and try to
 * allocate VM to the generated index. If failed, try another index.
 * 
 * @author Minxian
 * 
 */
public class RoundRobinAlgorithm extends OnlineAlgorithm {

	int index; // Allocated PM ID
	int currentTime = 0;
	int vmId = 0; // vmId is the the id in sorted in vmQueue
	int pmTotalNum;
	int increase = 1;
	int decrease = -1;
	int triedAllocationTimes = 0;
	VirtualMachine vm;
	String indexInfo = "RoundRobinIndex: ";

	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	ArrayList<PhysicalMachine> pmQueueOne = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueTwo = new ArrayList<PhysicalMachine>();
	ArrayList<PhysicalMachine> pmQueueThree = new ArrayList<PhysicalMachine>();

	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();

	public RoundRobinAlgorithm() {
		// System.out.println(getDescription());
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description + "-RoundRobin Algorithm-----";
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

		// System.out.println(pmQueueOne.get(0).resource.get(0).getCpuUtility());
		// System.out.println(pmQueueOne.get(0).resource.get(0).getMemUtility());
		// System.out.println(pmQueueOne.get(0).resource.get(0).getStoUtility());

		pmTotalNum = pmQueueOne.size() + pmQueueTwo.size()
				+ pmQueueThree.size();
		while (!vmQueue.isEmpty()) {
			// Pick out the vm with startTime less than currentTime to allocate
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				vm = vmQueue.get(vmId);
			} else {
				vmId++;
				triedAllocationTimes = 0;
				// Refactored method, see details in definition.
				checkVmIdAvailable();
				continue;
			}

			// Randomly find a PM
			index = index % pmTotalNum;
			System.out.println(indexInfo + index);
			// Three queues should be decided which queue would be added.
			if (index >= 0 && index < pmQueueOne.size()) {
				allocateVm(vm, pmQueueOne.get(index));
			} else if (index >= pmQueueOne.size()
					&& index < pmQueueTwo.size() + pmQueueOne.size()) {
				allocateVm(vm, pmQueueTwo.get(index - pmQueueOne.size()));
			} else {
				allocateVm(
						vm,
						pmQueueThree.get(index - pmQueueOne.size()
								- pmQueueTwo.size()));
			}
		}
		LoadBalanceFactory.print.println(LoadBalanceFactory.FINISHEDINFO);
	}

	/**
	 * Key scheduling procedure for algorithm. Main procedures are as below: 1.
	 * Check whether resource of a PM is available. 2. If resource available,
	 * output success information. Put the VM to deleteQueue, and remove that VM
	 * from vmQueue. 3. Update available resource of PM.
	 * 
	 * @param vm2
	 * @param pm2
	 */
	private void allocateVm(VirtualMachine vm2, PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		if (checkResourceAvailble(vm2, pm2)) {
			LoadBalanceFactory.print.println("Allocate:VM" + vm2.getVmNo()
					+ " to PM" + pm2.getNo());

			deleteQueue.add(vm2);
			vmQueue.remove(vm2);
			pm2.vms.add(vm2);
			vm2.setPmNo(pm2.getNo());
			updateResource(vm2, pm2, decrease);

			//vmId++;
			vmId = 0;
			triedAllocationTimes = 0;
			checkVmIdAvailable();
		} else {
			if (triedAllocationTimes == pmTotalNum) {
				System.out
						.println("VM number is too large, PM number is not enough");
				JOptionPane.showMessageDialog(null,
						"VM number is too large, PM number is not enough",
						"Error", JOptionPane.OK_OPTION);
				throw new IllegalArgumentException("PM too less");
			} else {
				triedAllocationTimes++;
				LoadBalanceFactory.print.println(LoadBalanceFactory.FAILEDINFO);
			}
		}
		index++;
	}

	/**
	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
	 */
	private void checkVmIdAvailable() {
		if (vmId >= vmQueue.size()) {
			currentTime++;
			vmId = 0;
			triedAllocationTimes = 0;
			LoadBalanceFactory.print.println("===currentTime:" + currentTime
					+ "===");
			processDeleteQueue(currentTime, deleteQueue);

		}
	}

	/**
	 * Check whether the left resource are available
	 * 
	 * @param vm3
	 * @param pm3
	 * @return
	 */
	private boolean checkResourceAvailble(VirtualMachine vm3,
			PhysicalMachine pm3) {
		boolean allocateSuccess = true;
		boolean oneSlotAllocation;
		for (int t = vm3.getStartTime(); t < vm3.getEndTime(); t++) {
			oneSlotAllocation = (pm3.resource.get(t).getCpuUtility() > vm3
					.getCpuTotal())
					&& (pm3.resource.get(t).getMemUtility() > vm3.getMemTotal())
					&& (pm3.resource.get(t).getStoUtility() > vm3
							.getStorageTotal());
			allocateSuccess = allocateSuccess && oneSlotAllocation;
			// System.out.println(pm3.resource.get(t).getCpuUtility() +" "+
			// vm3.getCpuTotal());
			// System.out.println(pm3.resource.get(t).getMemUtility() +" "+
			// vm3.getMemTotal());
			// System.out.println(pm3.resource.get(t).getStoUtility() +" "+
			// vm3.getStorageTotal());

			if (false == allocateSuccess) {
				// If allocated failed, return exactly.
				return allocateSuccess;
			}
		}
		return allocateSuccess;
	}

	/**
	 * Update the available resource. When parameter 3 equals to increase,
	 * available resource would increased, else resource would be decreased.
	 * 
	 * @param vm4s
	 * @param pm4
	 * @param incOrDec
	 */
	private void updateResource(VirtualMachine vm4, PhysicalMachine pm4,
			int incOrDec) {
		if (incOrDec == decrease) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				pm4.resource.get(t)
						.setCpuUtility(
								pm4.resource.get(t).getCpuUtility()
										- vm4.getCpuTotal());
				pm4.resource.get(t)
						.setMemUtility(
								pm4.resource.get(t).getMemUtility()
										- vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(
						pm4.resource.get(t).getStoUtility()
								- vm4.getStorageTotal());
			}
			System.out.println("Resource is updated(dec)");
		}
		if (incOrDec == increase) {
			for (int t = vm4.getStartTime(); t < vm4.getEndTime(); t++) {
				pm4.resource.get(t)
						.setCpuUtility(
								pm4.resource.get(t).getCpuUtility()
										+ vm4.getCpuTotal());
				pm4.resource.get(t)
						.setMemUtility(
								pm4.resource.get(t).getMemUtility()
										+ vm4.getMemTotal());
				pm4.resource.get(t).setStoUtility(
						pm4.resource.get(t).getStoUtility()
								+ vm4.getStorageTotal());
			}
			LoadBalanceFactory.print.println("Remove:VM" + vm4.getVmNo()
					+ " from PM" + pm4.getNo());
			LoadBalanceFactory.print.println("Resource is updated(inc)");

		}
	}

	/**
	 * After the VM has been added to deleteQueue, if end time comes, that VM
	 * should be removed from deleteQueue. Available resource should also be
	 * updated.
	 * 
	 * @param p_currentTime
	 * @param p_deleteQueue
	 */
	private void processDeleteQueue(int p_currentTime,
			ArrayList<VirtualMachine> p_deleteQueue) {
		// TODO Auto-generated method stub
		VirtualMachine vm5;
		PMBootor pmb1 = new PMBootor();
		ArrayList<Integer> pmNum = pmb1.bootPM();
		int pmNo;

		for (int i = 0; i < p_deleteQueue.size(); i++) {
			vm5 = p_deleteQueue.get(i);
			pmNo = vm5.getPmNo();
			if (p_currentTime >= vm5.getEndTime()) {
				if (pmNo >= 0 && pmNo < pmNum.get(0)) {
					updateResource(vm5, pmQueueOne.get(pmNo), increase);
				} else if (pmNo >= pmNum.get(0)
						&& pmNo < pmNum.get(0) + pmNum.get(1)) {
					updateResource(vm5, pmQueueTwo.get(pmNo - pmNum.get(0)),
							increase);
				} else {
					updateResource(vm5, pmQueueThree.get(pmNo - pmNum.get(0)
							- pmNum.get(1)), increase);
				}
				p_deleteQueue.remove(vm5);
			}
		}
	}

}
