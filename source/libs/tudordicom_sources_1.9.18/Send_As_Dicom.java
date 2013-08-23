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
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.File;

import lu.tudor.santec.dicom.exporter.DicomExporter;
import lu.tudor.santec.dicom.gui.DICOMSettingsPlugin;
import lu.tudor.santec.dicom.gui.DicomSettings;
import lu.tudor.santec.dicom.sender.DicomSender;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.DicomObject;

/**
 * ImageJ Plugin that sends dicom Images to the specified dicom node
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Send_As_Dicom implements PlugInFilter {

	private static final long serialVersionUID = 1L;
	private ImagePlus ip;
	private String dicomUrl;

	public int setup(String dicomUrl, ImagePlus ip) {
	    this.ip = ip;
	    this.dicomUrl = dicomUrl;
	    return DOES_8G+DOES_16+DOES_RGB;
	}

	public void run(ImageProcessor arg0) {
		System.out.println("run: " + "Send_As_Dicom Url: " + dicomUrl);
		try {
		DicomSender ds = null;
		if (dicomUrl != null && dicomUrl != "") {
			 ds = new DicomSender(new DcmURL(dicomUrl));
		} else {
			DICOMSettingsPlugin settings = DicomSettings.getDicomSettingsPlugin();
			String url = "dicom://" +
			settings.getValue("SendToAET");
			if (settings.getValue( "SenderAET") != null && settings.getValue("SenderAET") != "") {
				url = url + ":" + settings.getValue("SenderAET");
			}
			url = url  + "@" +settings.getValue("SendToAddress") + ":"+
			settings.getValue("SendToPort"); 
			
		    GenericDialog gd = new GenericDialog("dicomurl", IJ.getInstance());
		    gd.addStringField("dicom://sendtoAET:senderAET@sendtoAddress:sendtoPort", url, 60);
		    gd.showDialog();
		    if (gd.wasCanceled()){
		            return;
		    }
		    url = gd.getNextString();
			try {
				ds = new DicomSender(new DcmURL(url));
			} catch (Exception e) {
				IJ.error("DICOMURL: " + url + " is not valid!");
			}
			
		}
		DicomExporter de = new DicomExporter();
			File temp = File.createTempFile("tudor_dicom",".dcm");
		    	DicomObject dObj = de.createHeader(ip, true, true, true);
		    	de.write(dObj, ip, temp, true);
			ds.send(temp);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}



}
