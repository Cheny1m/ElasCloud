package com.iterator;

import java.util.ArrayList;

import com.schedule.energysaving.*;
import com.schedule.loadbalance.*;

/**
 * Comparison algorithms aggregation, which would add all comparison algorithms
 * into Iterator according to selection setting from interface.
 * 
 * @author Minxian
 * 
 */
public class AlgorithmItem {

	private boolean randomAlgortihm = false;
	private boolean roundRobinAlgorithm = false;
	private boolean listScheduling = false;
	private boolean olrsa = false;
	private boolean drs = false;
	private boolean sae = false;
	private boolean iw = false;
	// Two kinds of offline algorithms
	private boolean lpt = false;
	private boolean edf = false;
	private boolean cmp = false;
	private boolean mig = false;
	private boolean rr =false;
	private boolean ley=false;
	private boolean ffd = false;
	private boolean leynom = false;
	private boolean newley = false;
	private boolean mbfd = false;
	private boolean save = false;

	ArrayList<OnlineAlgorithm> aoa = new ArrayList<OnlineAlgorithm>();
	ArrayList<OfflineAlgorithm> aofa = new ArrayList<OfflineAlgorithm>();

	public AlgorithmItem() {
	}

	public boolean isRandomAlgortihm() {
		return randomAlgortihm;
	}

	public void setRandomAlgortihm(boolean randomAlgortihm) {
		this.randomAlgortihm = randomAlgortihm;
	}

	public boolean isRoundRobinAlgorithm() {
		return roundRobinAlgorithm;
	}

	public void setRoundRobinAlgorithm(boolean roundRobinAlgorithm) {
		this.roundRobinAlgorithm = roundRobinAlgorithm;
	}

	public boolean isListScheduling() {
		return listScheduling;
	}

	public void setListScheduling(boolean listScheduling) {
		this.listScheduling = listScheduling;
	}

	public boolean isOlrsa() {
		return olrsa;
	}

	public void setOlrsa(boolean olrsa) {
		this.olrsa = olrsa;
	}

	public boolean isEdf() {  return edf; }

	public void setEdf(boolean edf) {
		this.edf = edf;
	}

	// private boolean isEdf() {  return edf; }

	//private boolean isLPT() { return lpt; }

	public boolean isLPT() { return lpt; }

	public void setLPT(boolean lpt) {
		this.lpt = lpt;
	}

	public boolean isCMP() {
		return cmp;
	}

	public void setCMP(boolean cmp) {
		this.cmp = cmp;
	}

	public boolean isRR() {
		return rr;
	}

	public void setRR(boolean rr) {
		this.rr = rr;
	}

	public boolean isLey() {
		return ley;
	}

	public void setLey(boolean ley) {
		this.ley = ley;
	}

	public boolean isDRS() {
		return drs;
	}

	public void setDRS(boolean drs) {
		this.drs = drs;
	}

	public boolean isSAE() {
		return sae;
	}

	public void setSAE(boolean sae) {
		this.sae = sae;
	}

	public boolean isMIG() {
		return mig;
	}

	public void setMIG(boolean mig) {
		this.mig = mig;
	}

	public boolean isIW() { return iw; }

	public void setIW(boolean iw) { this.iw = iw; }

	public boolean isFFD() {
		return ffd;
	}

	public void setFFD(boolean ffd) {
		this.ffd = ffd;
	}

	public boolean isLeynom() {
		return leynom;
	}

	public void setLeynom(boolean leynom) {this.leynom = leynom;}

	public boolean isNewley() {
		return newley;
	}

	public void setNewley(boolean newley) {this.newley = newley;}

	public boolean isMbfd() {
		return mbfd;
	}

	public void setMbfd(boolean mbfd) {this.mbfd = mbfd;}

	public boolean isSave() {return save;}

	public void setSave(boolean save) {this.save = save;}

	public Iterator createIterator() {
		//移除在线算法列表中的所有元素
		aoa.removeAll(aoa);
		if (isRandomAlgortihm()) {
			aoa.add(new RandomAlgorithm());
		}
		if (isRoundRobinAlgorithm()) {
			aoa.add(new RoundRobinAlgorithm());
		}
		if (isListScheduling()) {
			aoa.add(new LSAlgortihm());
		}
		if (isOlrsa()) {
			aoa.add(new OLRSAAlgorithm());
		}
		if (isDRS()) {
			aoa.add(new DRSAlgorithm());
		}
		if (isSAE()) {
			aoa.add(new SAEAlgorithm());
		}
		if (isIW()) {
			aoa.add(new IWAlgorithm());
		}
		if (isSave()) {
			aoa.add(new Save());
		}
		return new OnlineAlgorithmIterator(aoa);
	}

	/*
	 * I have met obstacles here that algorithms has been added duplicated. The
	 * reason lies in the return value is an arrayList would accept more kinds
	 * of values. So before more algorithms would be added, the arrayList should
	 * be keep empty.
	 * 
	 * @return
	 */

	public Iterator createOfflineIterator() {
		aofa.removeAll(aofa);
		if (isLPT()) {
			aofa.add(new EnergySavingLPT());
		}
		if (isEdf()) {
			aofa.add(new EnergySavingEDF());
		}
		if (isCMP()) {
			aofa.add(new EnergySavingCMP());
		}
		if (isMIG()) {
			aofa.add(new EnergySavingMIG());
		}
		if (isRR()){
			aofa.add(new RoundRobin());
		}
		if (isLey()){
			aofa.add(new LoadEnergy());
		}
		if(isFFD()){
			aofa.add(new FirstFitDecreasing());
		}
		if (isLeynom()){
			aofa.add(new LoadEnergyNoMigration());
		}
		if (isNewley()){
			aofa.add(new NewLoadEnergy());
		}
		if(isMbfd()){
			aofa.add(new MBFD());
		}
		return new OfflineAlgorithmIterator(aofa);
	}
}
