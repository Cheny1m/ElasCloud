package com.specification;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Elements;
/**2013.03.10
 * This class is used to extract the PM information from the XML file,
 * including the VM type, CPU, memory, storage and proportion of each
 * kind of PM.
 * @author yuanliang, Minxian
 *
 */
public class PmInfo {
	Document doc ;
	Elements elements;
	public PmInfo() {
		try {
			doc = new Builder().build("src/com/specification/pminfo.xml");
			elements = doc.getRootElement().getChildElements();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
/**
 * 
 * @param pmType
 * @param elementsName
 * @return
 */
	public String getValue(int pmType, String elementsName) {
		String s="";
		try {
			s = elements.get(pmType-1).getFirstChildElement(elementsName).getValue();
                        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}
        
	public float getCpu(int pmType)
	{
		String cpu="";
		cpu=this.getValue(pmType,"cpu");
		return Float.parseFloat(cpu);
	}
	public float getMem(int pmType)
	{
		String mem="";
		mem=this.getValue(pmType, "mem");
		return Float.parseFloat(mem);
	}
	public float getStorage(int pmType)
	{
		String storage="";
		storage=this.getValue(pmType, "storage");
		return Float.parseFloat(storage);
	}
	public float getMinPower(int pmType)
	{
		String minPower="";
		minPower=this.getValue(pmType, "minPower");
		return Float.parseFloat(minPower);
	}
	public float getMaxPower(int pmType)
	{
		String maxPower="";
		maxPower=this.getValue(pmType, "maxPower");
		return Float.parseFloat(maxPower);
	}
	public static void main(String[] args) throws Exception {
		PmInfo p = new PmInfo();
		System.out.println(p.getCpu(1));
		System.out.println(p.getMem(2));
		System.out.println(p.getStorage(3));
	}
} 