package lu.tudor.santec.dicom.gui.filechooser;

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

import java.io.File;
import java.util.Vector;

import javax.swing.JTabbedPane;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomFilter;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class FILEPanelDicom extends JTabbedPane implements DicomFilter{

	private static final long serialVersionUID = 1L;
	private FILEChooserDicom dicomFileChooser;
	private FILEChooserDicom dicomFileManager;
	private DicomFileDialog parent;

	public FILEPanelDicom(File file, DicomFileDialog parent, boolean send) {
		this.parent = parent;
		dicomFileChooser = new FILEChooserDicom(file, this, false, parent.dicomFields);	
		this.addTab(Translatrix.getTranslationString("dicom.Open"), dicomFileChooser);
		
		dicomFileManager = new FILEChooserDicom(file, this, true, parent.dicomFields);
		if (send) {	
			this.addTab(Translatrix.getTranslationString("dicom.Manage"), dicomFileManager);
		}
	}
	
	public DicomFileDialog getDicomFileDialog() {
		return this.parent;
	}
	
	public void setFile(File f) {
		this.dicomFileChooser.setFile(f);
		this.dicomFileManager.setFile(f);
	}

	public void setDicomSenders(DcmURL[] senders) {
		this.dicomFileManager.setDicomSenders(senders);
	}

	public void setDicomFilterTags(Vector headerTags) {
		this.dicomFileChooser.setDicomFilterTags(headerTags);
		this.dicomFileManager.setDicomFilterTags(headerTags);
	}

	
}
