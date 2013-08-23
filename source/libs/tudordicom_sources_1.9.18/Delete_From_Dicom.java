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

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.header.DicomHeader;

/**
 * ImageJ Plugin that deletes the current open image or stack from the local DICOM STORE
 * ( if the images were loaded from the STORE before)
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Delete_From_Dicom implements PlugInFilter {

	private static final long serialVersionUID = 1L;
	private ImagePlus ip;

	
	public int setup(String arg0, ImagePlus ip) {
		this.ip = ip;
		return DOES_ALL;
	}

	public void run(ImageProcessor arg0) {
		System.out.println("run: " + "Delete_From_Dicom  Path: ");
		DicomFileDialog dicomFileDialog = DicomFileDialog.getDicomFileDialog();
    	try {
    		// get SeriesInstanceUID#InstanceNumber
			DicomHeader dh = new DicomHeader(ip);
			if (ip.getStackSize() > 1) {
				try {
					dicomFileDialog.deleteFileByImageURL(dh.getHeaderStringValue("0020,000E") + "#" + "-1");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					dicomFileDialog.deleteFileByImageURL(dh.getHeaderStringValue("0020,000E") + "#" + dh.getHeaderStringValue("0020,0013"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
	}
}
