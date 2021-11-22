
package cloudscheinterface;

import java.awt.Font;
import java.awt.RenderingHints;
import java.io.FileOutputStream;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;

/**
 * This class is used to show simple bar chart
 * @author Winter Lau
 */
public class DrawResults {
    
    CategoryDataset cdataset, cdataset1;
    public DrawResults(CategoryDataset cdataset) {
        this.cdataset = cdataset;
        JFreeChart chart = ChartFactory.createBarChart3D(
                                                        "Algorithm Comparison Diagram", 
                                                        "Compared Indices",
                                                        "Value", 
                                                        cdataset, 
                                                        PlotOrientation.VERTICAL, 
                                                        true,   
                                                        false,  
                                                        false   
                                                        );
               //Process Chinese problem
                processChart(chart);
                
                //Output chart
                writeChart2Image(chart);
                
                //Output chart as swing component
                ChartFrame pieFrame = new ChartFrame("Algorithm Comparison Diagram", chart);
               pieFrame.pack();
               pieFrame.setVisible(true);   
    }
        
    public DrawResults(CategoryDataset cdataset, String largeValue) {
        this.cdataset1 = cdataset;
        JFreeChart chart = ChartFactory.createBarChart3D(
                                                        "Algorithm Comparison Diagram", 
                                                        "Compared Indices", 
                                                        "Value", 
                                                        cdataset1,
                                                        PlotOrientation.VERTICAL, 
                                                        true,   
                                                        false,  
                                                        false   
                                                        );
               
                processChart(chart);
                
               
                writeChart2Image(chart, largeValue);
                
               
                ChartFrame pieFrame = new ChartFrame("Algorithm Comparison Diagram (large value)", chart);
               pieFrame.pack();
               pieFrame.setVisible(true);   
    }
          
        /**
         * Solve the Chinese fonts problem
         * @param chart
         */
        private static void processChart(JFreeChart chart ){
          CategoryPlot plot = chart.getCategoryPlot();
             CategoryAxis domainAxis = plot.getDomainAxis();
             ValueAxis rAxis = plot.getRangeAxis();
             chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
             TextTitle textTitle = chart.getTitle();
             textTitle.setFont(new Font("Times New Roman", Font.PLAIN, 18));
             domainAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 8));
             domainAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 10));
             rAxis.setTickLabelFont(new Font("sans-serif", Font.PLAIN, 14));
             rAxis.setLabelFont(new Font("Times New Roman", Font.PLAIN, 10));      
             chart.getLegend().setItemFont(new Font("Times New Roman",Font.PLAIN,14));  
//           renderer.setItemLabelGenerator(new LabelGenerator(0.0));
//           renderer.setItemLabelFont(new Font("Times New Roman", Font.PLAIN, 12));
//           renderer.setItemLabelsVisible(true);
        }
        /**
         * Output charts
         * @param chart
         */
       private static void writeChart2Image(JFreeChart chart){
        FileOutputStream fos_jpg = null;
           try {
                   fos_jpg = new FileOutputStream("src/com/output/comparison.jpg");
                   ChartUtilities.writeChartAsJPEG(fos_jpg,1,chart,400,300,null);
           }catch(Exception e){ 
            e.printStackTrace();
           } finally {
                   try {
                           fos_jpg.close();
                   } catch (Exception e) {}
           }
       }

      private static void writeChart2Image(JFreeChart chart, String largeValue){
        FileOutputStream fos_jpg = null;
           try {
                   fos_jpg = new FileOutputStream("src/com/output/comparisonLarge.jpg");
                   ChartUtilities.writeChartAsJPEG(fos_jpg,1,chart,400,300,null);
           }catch(Exception e){ 
            e.printStackTrace();
           } finally {
                   try {
                           fos_jpg.close();
                   } catch (Exception e) {}
           }
       }
       
       
}