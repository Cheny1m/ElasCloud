package cloudscheinterface;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.generaterequest.PMBootor;
import com.specification.PmInfo;
import com.specification.VmInfo;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * This class is used to fill the tables of VM and PM specification
 * @author Minxian
 *
 */
public class TableContent {
	PmInfo pmInfo;
	VmInfo vmInfo;
	PMBootor pmb = new PMBootor();
	TableModel tableModel;
	int columnIndex;
	ArrayList<Integer> pmNum;
        
        Pattern pattern;
        Matcher match;
	public TableContent(){
		pmNum = pmb.bootPMFromOrig();
	}
	
	public void setPMSpecification(JTable jTable){
                pmInfo = new PmInfo();
		tableModel = jTable.getModel();
		for(int row = 0; row < 3; row++){
		columnIndex = 0;
		tableModel.setValueAt(row + 1, row, columnIndex++);
		tableModel.setValueAt(pmInfo.getCpu(row+1),row, columnIndex++);
		tableModel.setValueAt(pmInfo.getMem(row+1), row, columnIndex++);
		tableModel.setValueAt(pmInfo.getStorage(row + 1), row, columnIndex++);
		
		}
	}
	
	public void setVMSpecification(JTable jTable){
                vmInfo = new VmInfo();
		tableModel = jTable.getModel();
		for(int row = 0; row < 8; row++){
		columnIndex = 0;
		tableModel.setValueAt(row + 1, row, columnIndex++);
		tableModel.setValueAt(vmInfo.getCpu(row+1),row, columnIndex++);
		tableModel.setValueAt(vmInfo.getMem(row+1), row, columnIndex++);
		tableModel.setValueAt(vmInfo.getStorage(row + 1), row, columnIndex++);
		
		}
	}
        /**
         * Verify the inputs.
         * Inputs should be positive integers.
         */
	public boolean isNumIllegal(JTable jTable){
            tableModel = jTable.getModel();
            int row = 0;
            if(isNumber(String.valueOf(tableModel.getValueAt(row++, 3))) && 
               isNumber(String.valueOf(tableModel.getValueAt(row++, 3))) &&
               isNumber(String.valueOf(tableModel.getValueAt(row++, 3)))){
                return true;
            }
            else{
                return false;
            }
        }
        
	public void setPMNumOnPropertyFile(JTable jTable){
		tableModel = jTable.getModel();
		int row = 0;
		pmb.setProperties("type1", tableModel.getValueAt(row++, 3));
		pmb.setProperties("type2", tableModel.getValueAt(row++, 3));
		pmb.setProperties("type3", tableModel.getValueAt(row++, 3));
	}
        
        public boolean  isNumber(String str){
            pattern = Pattern.compile("[0-9]*");
            match = pattern.matcher(str);
            if(match.matches() == false){
            return false;
             }
             else{
            return true;
            }
    }
}
