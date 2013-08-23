/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2006 by SANTEC/TUDOR www.santec.tudor.lu                   
 *                                                                           
 *                                                                           
 *  This library is free software; you can redistribute it and/or modify it  
 *  under the terms of the GNU Lesser General Public License as published    
 *  by the Free Software Foundation; either version 2 of the License, or     
 *  (at your option) any later version.                                      
 *                                                                           
 *  This software is distributed in the hope that it will be useful, but     
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 *  Lesser General Public License for more details.                          
 *                                                                           
 *  You should have received a copy of the GNU Lesser General Public         
 *  License along with this library; if not, write to the Free Software      
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
 *                                                                           
 *****************************************************************************/

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import lu.tudor.santec.dicom.gui.header.DicomHeader;

/**
 * ImageJ Plugin that reads the selected DICOM Header Value from the images an puts it to the 
 * Result Table
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Get_Dicom_Value implements PlugInFilter {

	private ImagePlus ip;
	private String headerField;
    private ResultsTable    rt;
    
	public int setup(String headerField, ImagePlus ip) {
	    if (ip == null ) {
		      return DONE;
		}
		this.ip = ip;
		this.headerField = headerField;
		return DOES_ALL;
	}

	public void run(ImageProcessor arg0) {
		System.out.println("run: " + "Get_Dicom_Value");
		if (headerField == null || headerField == "") {
			if (!readField()) {
				return;
			}
		} else if (headerField.length() != 9) {
			return;
		}
		
		DicomHeader dh;
		try {
		    dh = new DicomHeader(ip);
		    String value = dh.getHeaderStringValue(headerField);
		    System.out.println(headerField + " " + value);
		    
//		    int measurements = Analyzer.getMeasurements();
//		    Analyzer.setMeasurements(measurements);
//		    Analyzer analyzer = new Analyzer();
//		    ImageStatistics stat = ip.getStatistics();
//		    Roi roi = ip.getRoi();
//		    analyzer.saveResults(stat,roi);
//		    
//		    
//		    rt = Analyzer.getResultsTable();
//		    rt.addLabel("Attribute",headerField + ":" + value);
//		    		    
//		    int counter = rt.getCounter();
//		    IJ.write(rt.getRowAsString(counter-1));
		    
		    ResultsTable rt = ResultsTable.getResultsTable();
			rt.incrementCounter();
			rt.addLabel("Tag",headerField + ":" + value);
			rt.show("Results");
		    
		} catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
	private boolean readField(){
        GenericDialog gd = new GenericDialog("Query DICOM Header", IJ.getInstance());
        gd.addStringField("9 characs [group,element] in format: xxxx,xxxx", "0008,0060", 9);
        gd.showDialog();
        if (gd.wasCanceled()){
            return false;
        }
        headerField = gd.getNextString();
        if(headerField.length()!=9){
            IJ.error("Input requirement:\n9 characs [group,element] in format: xxxx,xxxx");
            return false;
        }
        return true;
    }
	
	public static String getHeaderValue(String tag) {
		try {
			ImagePlus ip = WindowManager.getCurrentImage();
			DicomHeader dh = new DicomHeader(ip);
			String value = dh.getHeaderStringValue(tag);
			return value;			
		} catch (Exception e) {
			return null;
		}
	}

}
