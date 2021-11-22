package com.specification;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Elements;

/**2013.03.10
 * This class is used to extract the VM information in the XML file,
 * including the VM type, CPU, memory, storage and proportion of each
 * kind of VM requests.
 * @author yuanliang, Minxian
 *
 */
public class VmInfo {
	Document doc ;
	Elements elements;
	float[] arrayProba;
	public VmInfo() {
		try {
			doc = new Builder().build("src/com/specification/vminfo.xml");
			elements = doc.getRootElement().getChildElements();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
/**
 * 
 * @param vmType
 * @param elementsName
 * @return
 */
	public String getValue(int vmType,String elementsName) {
		String s="";
		try {
			int i=vmType-1; 
			s = elements.get(i).getFirstChildElement(elementsName).getValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
	public float getCpu(int vmType){
		String cpu="";
		cpu=this.getValue(vmType,"cpu");
		
		return Float.parseFloat(cpu);
	}
	public float getMem(int vmType){
		String mem="";
		mem=this.getValue(vmType, "mem");
		return Float.parseFloat(mem);
	}
	public float getStorage(int vmType){
		String storage="";
		storage=this.getValue(vmType, "storage");
		return Float.parseFloat(storage);
	}
	public int getVmTypeNo(String VmType){
		int i=-1;
		if ("1-1".equals(VmType)) i=0;
		else if("1-2".equals(VmType)) i=1;
		else if("1-3".equals(VmType)) i=2;
		else if("2-1".equals(VmType)) i=3;
		else if("2-2".equals(VmType)) i=4;
		else if("2-3".equals(VmType)) i=5;
		else if("3-1".equals(VmType)) i=6;
		else if("3-2".equals(VmType)) i=7;
		return i;
	}
	public float getVmProportion(int vmType){
		String proportion = "";
		proportion = this.getValue(vmType, "proportion");
		return Float.parseFloat(proportion);
	}
	/**
	 * Calculate the probability span of a VM type
	 * @param vmType
	 * @return
	 */
	public float[] getVmTypeProbabilitySpan(){
		float vmTypeProba = 0.0f;
		VmInfo p1 = new VmInfo();
		arrayProba = new float[8];
		for(int i = 0; i < 8; i++){
			vmTypeProba += p1.getVmProportion(i+1);
			arrayProba[i] = vmTypeProba;
		}
		return arrayProba;
	}
	
	public static void main(String[] args) throws Exception {
		VmInfo p = new VmInfo();
		System.out.println(p.getCpu(2));
		System.out.println(p.getMem(4));
		System.out.println(p.getStorage(8));
		//System.out.println(p.getVmProportion(1));
		p.getVmTypeProbabilitySpan();
		for(int i = 0; i < 8; i++)
		System.out.println(p.arrayProba[i]);
	}
} 
