
package com.output;

import com.datacenter.DataCenterFactory;
import com.datacenter.LoadBalanceFactory;
import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 *This class is used to output some important data to excel file to 
 * enable a more apprarent comparison 
 * @author LukeXu
 */
public class WriteToExcel {
        
    WritableWorkbook book;
    WritableSheet sheet;
    int column;
    int row;
    int indexRowNo = 3;
    int OriRowNo = 3;
    public WriteToExcel() {

    }
    
    public void openExcel(String filePath, String sheetName, int sheetNo){
        try {
            //Open excel file
            book = Workbook.createWorkbook(new File(filePath));
            // The code fragment below creates a sheet called "First Sheet" at the first position. 
            sheet = book.createSheet(sheetName, sheetNo);
    
        } catch (Exception e) {
            LoadBalanceFactory.print.println(e.toString());
        }
    }
    
    public void writeLabel(int col, int row, String content){
        try {
            Label label = new Label(col, row, content);
            sheet.addCell(label);
        } catch (Exception e) {
            DataCenterFactory.print.println(e.toString());
        }
    }
    public void writeData(int col, int row, double dataD){
        try {
             /**
             * The other point to note is that the cell's location is specified 
             * as (column, row). Both are zero indexed integer values 
             * - A1 being represented by (0,0), B1 by (1,0), A2 by (0,1) and so on. 
             */
            jxl.write.Number number = new jxl.write.Number(col, row, dataD);
            sheet.addCell(number); 
        } catch (Exception e) {
            DataCenterFactory.print.println(e.toString());
        }

    }
    
    public void closeExcel(){
        try {
            book.write();
            book.close();
        } catch (Exception e) {
            LoadBalanceFactory.print.println(e.toString());
        }
    
        // Close file
    }
    
    public int getRowNumber(){
        return row;
    }
    
    public int getCompareRowNumber(){
        return indexRowNo;
    }
   
    public int getOriRowNumber(){
        return OriRowNo;
    }
    public void setRowNumber(int row){
        this.row = row;
    }
    
    public void setIndexRowNumber(int indexRowNo){
        this.indexRowNo = indexRowNo;
    }
    
    public static void main(String args[]) {
        int col;
        int row;
        try {
               WriteToExcel writeToExcel = new WriteToExcel();
               writeToExcel.openExcel("src/com/output/PMdata.xls", "Sheet One", 0);
               row = writeToExcel.getRowNumber();
               col = 0;
               //Write lable
               writeToExcel.writeLabel(col, row, "PM number");
               writeToExcel.writeLabel(col + 1, row, "CPU utilization");
               writeToExcel.writeLabel(col + 2, row, "Memory utilization");
               writeToExcel.writeLabel(col + 3, row, "Storage utilization");
               writeToExcel.writeLabel(col + 4, row, "Average utilization");
               for(row = 1; row < 3; row++){
               writeToExcel.writeData(col, row, row);
               writeToExcel.writeData(col + 1, row, 0.2);
               writeToExcel.writeData(col + 2, row, 0.1);
               writeToExcel.writeData(col + 3, row, 0.3);
               writeToExcel.writeData(col + 4, row, 0.2);
               }
               writeToExcel.setRowNumber(row);
               System.out.println(writeToExcel.getRowNumber());
               writeToExcel.closeExcel();

               
        } catch (Exception e) {
            System.out.println(e);
        }
       
    }
}

