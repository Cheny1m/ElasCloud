/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cloudscheinterface;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *This class is used to filter the files end with .fc
 * fc is short for flex cloud
 * @author LukeXu
 */
public class MyCustomerFilter extends FileFilter{

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || f.getAbsolutePath().endsWith(".fc");
    }

    @Override
    public String getDescription() {
        return "Text documents (*.fc)";
    }
    
}
