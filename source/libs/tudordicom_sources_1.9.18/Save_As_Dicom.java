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
import ij.Macro;
import ij.io.SaveDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.anonymizer.DicomAnonymizer;
import lu.tudor.santec.dicom.anonymizer.SimpleAnonPanel;
import lu.tudor.santec.dicom.exporter.DicomExporter;

import org.dcm4che2.data.DicomObject;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * ImageJ Plugin that saves dicom images to the filesystem
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Save_As_Dicom implements PlugInFilter, ActionListener {

	private static final long serialVersionUID = 1L;
	private ImagePlus ip;
	private String path;
	private DicomExporter de;
	private SimpleAnonPanel anonPanel = new SimpleAnonPanel();
	private JDialog anonDialog;
	private JButton cancelButton;
	private JButton okButton;
	private File f;
	private boolean silent;
	private JCheckBox multiSliceExport;

	public int setup(String path, ImagePlus ip) {
	    this.ip = ip;
	    this.path = path;
	    
	    anonDialog = new JDialog();
	    anonDialog.setModal(true);
	    anonDialog.setLayout(new BorderLayout());
	    
	    multiSliceExport = new JCheckBox("Export as Multislice");
	    anonDialog.add(multiSliceExport, BorderLayout.NORTH);
	    
	    anonDialog.add(anonPanel, BorderLayout.CENTER);
	    
	    f = null;
	    if (path != null && path != "") {
			f = new File(path);
			silent = true;
	    }
	    
	    String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			path = Macro.getValue(macroOptions, "filename", null);
			if (path != null) {
				f = new File(path);
				silent = true;				
			} 
		}
	    
	    anonPanel.setRemovePatient(!silent);
	    anonPanel.setRemoveInstitution(!silent);
	    anonPanel.setRemoveManufacturer(!silent);
	    
	    ButtonBarBuilder bb = new ButtonBarBuilder();
		
		bb.addGlue();
		
		cancelButton = new JButton("cancel");
		cancelButton.addActionListener(this);
		bb.addGridded(cancelButton);
		
		bb.addRelatedGap();
		
		okButton = new JButton("run");
		okButton.addActionListener(this);
		bb.addGridded(okButton);
		
		JPanel buttonPanel = bb.getPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		buttonPanel.setOpaque(false);
		anonDialog.add(buttonPanel, BorderLayout.SOUTH);
		anonDialog.pack();
	    
	    return DOES_8G+DOES_16+DOES_RGB;
	}

	public void run(ImageProcessor arg0) {
		try {

			de = new DicomExporter();

			if (silent) {
				exportFile(ip, f);
			} else {
				anonDialog.setVisible(true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void exportFile(ImagePlus ip, File f) throws Exception{
	    	DicomObject dObj = de.createHeader(ip, true, true, true);

	    	 if (anonPanel.isRemovePatient())
	    		 DicomAnonymizer.removePatientInfo(dObj, anonPanel.getNewPatientID(), anonPanel.getNewPatientName());
		    
		    if (anonPanel.isRemoveInstitution()) {
		    	DicomAnonymizer.removeInstitutionInfo(dObj, anonPanel.getNewInstitutionName());
		    	DicomAnonymizer.removePhysicianInfo(dObj);
		    }
		    
		    if (anonPanel.isRemoveManufacturer())
		    	DicomAnonymizer.removeManufacturerInfo(dObj);
		    de.write(dObj, ip, f, multiSliceExport.isSelected());
	}

	public void actionPerformed(ActionEvent e) {
	    if (e.getSource().equals(this.okButton)) {
		
		System.out.println("run: " + "Save_As_Dicom Path: " + path);
		f = null;
		if (path != null && path != "") {
			f = new File(path);
		} else {
			SaveDialog sd = new SaveDialog("filename","exported" + ip.getBitDepth() + ".dcm","");
			try {
				f = new File(sd.getDirectory() + File.separatorChar + sd.getFileName());
			} catch (Exception e3) {
				e3.printStackTrace();
				return;
			}
		}
		
		if (f == null ) {
		    return;
		}
		
		try {
			exportFile(ip, f);
			System.out.println("Saving file: " + f.getAbsolutePath());
		} catch (Exception e2) {
		    e2.printStackTrace();
		}

	    } else if (e.getSource().equals(this.cancelButton)) {
	    }
	    anonDialog.setVisible(false);
	}
}
