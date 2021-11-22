package com.schedule.loadbalance;

import com.comparedindex.CalImbalanceDegree;
import com.datacenter.DataCenter;
import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import com.resource.PhysicalMachine;
import com.resource.VirtualMachine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JOptionPane;

/**
 * Dynamic Resource Scheduling algorithm is based on VMware algorithm. The
 * basic process of DRS algorithm is composed of two parts: pre-allocation and
 * post migration.
 * 
 * @author Minxian
 * 
 */
public class DRSAlgorithm extends OnlineAlgorithm {

	int dataCenterIndex; // Selected data center ID
	int rackIndex; // Selected rack ID
	int index; // Allocated PM ID
	int currentTime = 0;
	int vmId = 0; // vmId is the the id in sorted in vmQueue
	int pmTotalNum;
	int increase = 1;
	int decrease = -1;
	int triedAllocationTimes = 0;
	Random random = new Random();
	VirtualMachine vm;
	ArrayList<VirtualMachine> vmQueue = new ArrayList<VirtualMachine>();
	ArrayList<DataCenter> arr_dc = new ArrayList<DataCenter>();
	ArrayList<VirtualMachine> deleteQueue = new ArrayList<VirtualMachine>();
	int pmQueueOneSize;
	int pmQueueTwoSize;
	int pmQueueThreeSize;

	int migrationTimes = 0;
	int successMigrationTimes = 0;

	int lowestLoadDataCenerIndex = 0;
	int lowestLoadRackIndex = 0;
	int lowestLoadPMIndex = 0;

	int highestLoadDataCenterIndex = 0;
	int highestLoadRackIndex = 0;
	int highestLoadPMIndex = 0;

	DataCenter tempDC;
	LoadBalanceFactory tempLbf;
	PhysicalMachine tempPM;
	VirtualMachine vm1;
	float improvedImbalanceDegree = 0.0f;
	CalImbalanceDegree aid;

	final float THRESHOLDIMBALANCEDEGREE = 0.007f;
	final int MAXMIGRATIONTIMES = 50;

	public DRSAlgorithm() {
		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return description + "-DRS Algorithm-----";
	}

	/**
	 * Generate the random index and try to allocate VM to the PM with generated
	 * index.
	 */
	@Override
	public void allocate(ArrayList<VirtualMachine> vmQueue,
			ArrayList<DataCenter> p_arr_dc) {
		DataCenterFactory.print.println(getDescription());
		this.vmQueue = vmQueue;
		this.arr_dc = p_arr_dc;
		pmQueueOneSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueOne().size();
		pmQueueTwoSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueTwo().size();
		pmQueueThreeSize = arr_dc.get(dataCenterIndex).getArr_lbf()
				.get(rackIndex).getPmQueueThree().size();
		// Dangerous codes
		pmTotalNum = pmQueueOneSize + pmQueueTwoSize + pmQueueThreeSize;
		int allocatedDataCenterID;
		int allocatedRackID;

		while (!vmQueue.isEmpty()) {
			if (currentTime >= vmQueue.get(vmId).getStartTime()) {
				vm = vmQueue.get(vmId);
			} else {
				vmId++;
				triedAllocationTimes = 0;
				checkVmIdAvailable();
				continue;
			}

			Collections.sort(arr_dc, new SortByDataCenterUtility());
			dataCenterIndex = 0;
			allocatedDataCenterID = arr_dc.get(dataCenterIndex).getD_id();

			Collections.sort(arr_dc.get(dataCenterIndex).getArr_lbf(),
					new SortByRackUtility());
			rackIndex = 0;
			allocatedRackID = arr_dc.get(dataCenterIndex).getArr_lbf()
					.get(rackIndex).getLbf_ID();

			if (vm.getVmType() > 0 && vm.getVmType() < 4) {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueOne(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueOne().get(index));
			} else if (vm.getVmType() >= 4 && vm.getVmType() < 7) {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueTwo(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueTwo().get(index));
			} else {
				Collections.sort(
						arr_dc.get(dataCenterIndex).getArr_lbf().get(rackIndex)
								.getPmQueueThree(), new SortByPMUtility());
				allocateVm(allocatedDataCenterID, allocatedRackID, vm, arr_dc
						.get(dataCenterIndex).getArr_lbf().get(rackIndex)
						.getPmQueueThree().get(index));
			}
		}
		migrateVMs(arr_dc);
		DataCenterFactory.print.println(DataCenterFactory.FINISHEDINFO);
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
	private void allocateVm(int dataCenterNo, int rackNo, VirtualMachine vm2,
			PhysicalMachine pm2) {
		// TODO Auto-generated method stub
		if (checkResourceAvailble(vm2, pm2)) {
			DataCenterFactory.print.println("Allocate:VM" + vm2.getVmNo() + " "
					+ "to DataCenter" + dataCenterNo + " Rack" + rackNo + " PM"
					+ pm2.getNo());
			deleteQueue.add(vm2);
			vmQueue.remove(vm2);
			pm2.vms.add(vm2);
			vm2.setPmNo(pm2.getNo());
			vm2.setRackNo(rackNo);
			vm2.setDataCenterNo(dataCenterNo);

			updateResource(vm2, pm2, decrease);

			vmId++;
			triedAllocationTimes = 0;
			checkVmIdAvailable();
			index = 0;
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
				DataCenterFactory.print.println(DataCenterFactory.FAILEDINFO);
				index++; // Try another PM
			}
		}
	}

	/**
	 * Check whether the vmId has surpassed bound, if yes, reset vmId as 0.
	 */
	private void checkVmIdAvailable() {
		if (vmId >= vmQueue.size()) {
			currentTime++;
			vmId = 0;
			triedAllocationTimes = 0;
			DataCenterFactory.print.println("===currentTime:" + currentTime
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
			DataCenterFactory.print.println("Resource is updated(dec)");
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
			DataCenterFactory.print.println("Remove:VM" + vm4.getVmNo()
					+ " from DataCenter" + vm4.getDataCenterNo() + " Rack"
					+ vm4.getRackNo() + " PM" + pm4.getNo());
			DataCenterFactory.print.println("Resource is updated(inc)");

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
		int pmNo;
		int dataCenterNo;
		int rackNo;

		for (int i = 0; i < p_deleteQueue.size(); i++) {
			vm5 = p_deleteQueue.get(i);
			dataCenterNo = vm5.getDataCenterNo();
			rackNo = vm5.getRackNo();
			pmNo = vm5.getPmNo();

			if (p_currentTime >= vm5.getEndTime()) {
				if (pmNo >= 0 && pmNo < pmQueueOneSize) {
					updateResource(vm5, arr_dc.get(dataCenterNo).getArr_lbf()
							.get(rackNo).getPmQueueOne().get(pmNo), increase);
				} else if (pmNo >= pmQueueOneSize
						&& pmNo < pmQueueOneSize + pmQueueTwoSize) {
					updateResource(
							vm5,
							arr_dc.get(dataCenterNo).getArr_lbf().get(rackNo)
									.getPmQueueTwo().get(pmNo - pmQueueOneSize),
							increase);
				} else {
					updateResource(
							vm5,
							arr_dc.get(dataCenterNo)
									.getArr_lbf()
									.get(rackNo)
									.getPmQueueThree()
									.get(pmNo - pmQueueOneSize
											- pmQueueThreeSize), increase);
				}
				p_deleteQueue.remove(vm5);
			}
		}
	}

	/**
	 * Migration Process based on VMware DRS algorithm, VMs on the PM with
	 * highest load would be migrated to PM with lowest load. Loop is stopped
	 * when the imbalance degree is below the threshold or actual migration
	 * times surpasses the predefined migration times.
	 * 
	 * @param p_arr_dc
	 */
	private void migrateVMs(ArrayList<DataCenter> p_arr_dc) {
		DataCenterFactory.print
				.println("DRS algorithm migration process begins......");

		aid = new CalImbalanceDegree(p_arr_dc);
		DataCenterFactory.print.println("DRS Imblance Degree Before Migration:"
				+ aid.getIndexValue());
		while (aid.getIndexValue() > THRESHOLDIMBALANCEDEGREE
				|| migrationTimes < MAXMIGRATIONTIMES) {
			aid = new CalImbalanceDegree(p_arr_dc);
			improvedImbalanceDegree = aid.getIndexValue();

			Collections.sort(p_arr_dc, new SortByDataCenterUtility());
			highestLoadDataCenterIndex = p_arr_dc.size() - 1;

			Collections.sort(p_arr_dc.get(highestLoadDataCenterIndex)
					.getArr_lbf(), new SortByRackUtility());
			highestLoadRackIndex = p_arr_dc.get(highestLoadDataCenterIndex)
					.getArr_lbf().size() - 1;

			if (p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
					.get(highestLoadRackIndex).getPmQueueOne().size() > 0) {
				migratePMQueue(p_arr_dc,
						p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
								.get(highestLoadRackIndex).getPmQueueOne(),
						p_arr_dc.get(lowestLoadDataCenerIndex).getArr_lbf()
								.get(lowestLoadRackIndex).getPmQueueOne());
			}

			if (p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
					.get(highestLoadRackIndex).getPmQueueTwo().size() > 0) {
				migratePMQueue(p_arr_dc,
						p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
								.get(highestLoadRackIndex).getPmQueueTwo(),
						p_arr_dc.get(lowestLoadDataCenerIndex).getArr_lbf()
								.get(lowestLoadRackIndex).getPmQueueTwo());
			}

			if (p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
					.get(highestLoadRackIndex).getPmQueueThree().size() > 0) {
				migratePMQueue(p_arr_dc,
						p_arr_dc.get(highestLoadDataCenterIndex).getArr_lbf()
								.get(highestLoadRackIndex).getPmQueueThree(),
						p_arr_dc.get(lowestLoadDataCenerIndex).getArr_lbf()
								.get(lowestLoadRackIndex).getPmQueueThree());
			}
		}
		DataCenterFactory.print.println("Total successful migration times is: "
				+ successMigrationTimes);

		sortAllPMsInOrder(p_arr_dc);
	}

	/**
	 * Refactored method for the migration process to be suitable for three kinds
	 * of PMs
	 * 
	 * @param p_arr_dc
	 * @param highestPMQueue
	 * @param lowestPMQueue
	 */
	public void migratePMQueue(ArrayList<DataCenter> p_arr_dc,
			ArrayList<PhysicalMachine> highestPMQueue,
			ArrayList<PhysicalMachine> lowestPMQueue) {
		Collections.sort(highestPMQueue, new SortByPMUtility());
		highestLoadPMIndex = highestPMQueue.size() - 1;

		tempDC = p_arr_dc.get(highestLoadDataCenterIndex);
		tempLbf = p_arr_dc.get(highestLoadDataCenterIndex)
				.getArr_lbf().get(highestLoadRackIndex);
		tempPM = highestPMQueue.get(highestLoadPMIndex);

		vm1 = highestPMQueue.get(highestLoadPMIndex).vms.get(0);

		highestPMQueue.get(highestLoadPMIndex).vms.remove(vm1);
		lowestPMQueue.get(lowestLoadPMIndex).vms.add(vm1);

		DataCenterFactory.print.print("Try Migration..." + "From DataCenter"
				+ tempDC.getD_id() + " Rack" + tempLbf.getLbf_ID() + " PM"
				+ tempPM.getNo());
		tempDC = p_arr_dc.get(lowestLoadDataCenerIndex);
		tempLbf = p_arr_dc.get(lowestLoadDataCenerIndex).getArr_lbf()
				.get(lowestLoadRackIndex);
		tempPM = lowestPMQueue.get(lowestLoadPMIndex);
		DataCenterFactory.print.println(" To DataCenter" + tempDC.getD_id()
				+ " Rack" + tempLbf.getLbf_ID() + " PM" + tempPM.getNo());

		migrationTimes++;

		aid = new CalImbalanceDegree(p_arr_dc);

		if (aid.getIndexValue() < improvedImbalanceDegree) {
			DataCenterFactory.print.println("Successful Migration ......");
			DataCenterFactory.print.println("PreviousImbalanceDegree:"
					+ improvedImbalanceDegree);
			DataCenterFactory.print.println("LaterImbalanceDegree:"
					+ aid.getIndexValue());
			successMigrationTimes++;

			// Reset indices to lowest ones.
			lowestLoadDataCenerIndex = 0;
			lowestLoadRackIndex = 0;
			lowestLoadPMIndex = 0;
		} else {
			DataCenterFactory.print.println("Failed Migration......");
			highestPMQueue.get(highestLoadPMIndex).vms.add(vm1);
			lowestPMQueue.get(lowestLoadPMIndex).vms.remove(vm1);
			lowestLoadPMIndex++;

			if (lowestLoadPMIndex == lowestPMQueue.size()) {
				lowestLoadPMIndex = 0;
				lowestLoadRackIndex++;
				if (lowestLoadRackIndex == p_arr_dc
						.get(lowestLoadDataCenerIndex).getArr_lbf().size()) {
					lowestLoadRackIndex = 0;
					lowestLoadDataCenerIndex++;
					if (lowestLoadDataCenerIndex == p_arr_dc.size()) {
						lowestLoadDataCenerIndex = 0;
					}
				}
			}
		}
	}

	/**
	 * Sorting the PM sequences to be the initial sequence.
	 * 
	 * @param p_arr_dc
	 */
	public void sortAllPMsInOrder(ArrayList<DataCenter> p_arr_dc) {
		Collections.sort(p_arr_dc, new SortByDataCenterID());
		for (DataCenter dc : p_arr_dc) {
			Collections.sort(dc.getArr_lbf(), new SortByRackID());
			for (LoadBalanceFactory lbf : dc.getArr_lbf()) {
				Collections.sort(lbf.getPmQueueOne(), new SortByPMID());
				Collections.sort(lbf.getPmQueueTwo(), new SortByPMID());
				Collections.sort(lbf.getPmQueueThree(), new SortByPMID());
			}
		}
	}
}

/**
 * The following classes are used to sort DataCenter list based on different
 * elements.
 * 
 * @author Minxian
 * 
 */
class SortByDataCenterUtility implements Comparator<DataCenter> {

	@Override
	public int compare(DataCenter o1, DataCenter o2) {
		DataCenter dc1 = (DataCenter) o1;
		DataCenter dc2 = (DataCenter) o2;
		if (dc1.getDataCenterLoad() > dc2.getDataCenterLoad()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class SortByDataCenterID implements Comparator<DataCenter> {

	@Override
	public int compare(DataCenter o1, DataCenter o2) {
		DataCenter dc1 = (DataCenter) o1;
		DataCenter dc2 = (DataCenter) o2;
		if (dc1.getD_id() > dc2.getD_id()) {
			return 1;
		} else {
			return 0;
		}
	}
}

class SortByRackUtility implements Comparator<LoadBalanceFactory> {

	@Override
	public int compare(LoadBalanceFactory p_lbf1, LoadBalanceFactory p_lbf2) {
		LoadBalanceFactory lbf1 = p_lbf1;
		LoadBalanceFactory lbf2 = p_lbf2;
		if (lbf1.getRackLoad() > lbf2.getRackLoad()) {
			return 1;
		}
		return 0;
	}
}

class SortByRackID implements Comparator<LoadBalanceFactory> {

	@Override
	public int compare(LoadBalanceFactory p_lbf1, LoadBalanceFactory p_lbf2) {
		LoadBalanceFactory lbf1 = p_lbf1;
		LoadBalanceFactory lbf2 = p_lbf2;
		if (lbf1.getLbf_ID() > lbf2.getLbf_ID()) {
			return 1;
		}
		return 0;
	}
}

class SortByPMUtility implements Comparator<PhysicalMachine> {

	@Override
	public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
		PhysicalMachine pm1 = p_pm1;
		PhysicalMachine pm2 = p_pm2;
		if (pm1.getAvgUtility() > pm2.getAvgUtility()) {
			return 1;
		}
		return 0;
	}
}

class SortByPMID implements Comparator<PhysicalMachine> {

	@Override
	public int compare(PhysicalMachine p_pm1, PhysicalMachine p_pm2) {
		PhysicalMachine pm1 = p_pm1;
		PhysicalMachine pm2 = p_pm2;
		if (pm1.getNo() > pm2.getNo()) {
			return 1;
		}
		return 0;
	}
}
