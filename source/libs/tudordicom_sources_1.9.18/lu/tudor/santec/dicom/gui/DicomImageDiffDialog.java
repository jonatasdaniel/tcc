package lu.tudor.santec.dicom.gui;

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
import ij.io.FileInfo;
import ij.plugin.ImageCalculator;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderInfoPanel;
import lu.tudor.santec.dicom.gui.header.Diff;
import lu.tudor.santec.dicom.gui.viewer.DicomImagePanel;
import lu.tudor.santec.dicom.gui.viewer.WindowListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImageDiffDialog extends JFrame implements PropertyChangeListener{
	
	private static final long serialVersionUID = 1L;

	private MyAdjustmentListener adjustmentListener;

	private DicomFileDialog dicomFileDialog;

	private JPanel headerPanel;

	private JPanel filePanel;

	private JPanel imagePanel;

	private DicomImagePanel dipLeft;

	private DicomImagePanel dipDiff;

	private DicomImagePanel dipRight;

	private DicomHeaderInfoPanel headerPanelLeft;

	private DicomHeaderInfoPanel headerPanelRight;

	protected ImagePlus imageLeft;

	protected ImagePlus imageRight;

	private ImagePlus imageDiff;

	public DicomImageDiffDialog(DicomFileDialog dialog) {
	    	this.dicomFileDialog = dialog;
		this.setTitle("DICOM Image Diff");
		this.buildPanel();
	}
	
	private void buildPanel() {
	    
	    	this.setLayout(new BorderLayout());
	    
	    	this.filePanel = new JPanel(new GridLayout(1,2));
	    
	    	this.headerPanel = new JPanel();
		this.headerPanel.setLayout(new GridLayout(1,2));
		this.setTitle("DICOM Diff");
		
		this.adjustmentListener = new MyAdjustmentListener();
		
		headerPanelLeft = new DicomHeaderInfoPanel(null);
			headerPanelLeft.addAdjustmentListener(adjustmentListener);
			headerPanelLeft.addPropertyChangeListener(this);
			headerPanel.add(headerPanelLeft);
			
			JButton jbLeft = new JButton("select left image");
			jbLeft.addActionListener(new ActionListener() {					    
			    public void actionPerformed(ActionEvent e) {
				if (JFileChooser.APPROVE_OPTION ==dicomFileDialog.showNewDialog(DicomImageDiffDialog.this)) {
				    File f = dicomFileDialog.getSelectedFile();
				    DicomHeader dh = new DicomHeader(f);
				    headerPanelLeft.setInfo(dh);
				    try {
					imageLeft = DicomOpener.loadImage(f);
				    } catch (FileNotFoundException e1) {
					e1.printStackTrace();
					imageLeft = null;
				    }
				    dipLeft.setImage(imageLeft);
				    createDiffImage();
				}
			    }
			});
			filePanel.add(jbLeft);
		
			headerPanelRight = new DicomHeaderInfoPanel(null);
			headerPanelRight.addAdjustmentListener(adjustmentListener);
			headerPanelRight.addPropertyChangeListener(this);
			headerPanel.add(headerPanelRight);
			
			JButton jbRight = new JButton("select right image");
			jbRight.addActionListener(new ActionListener() {					    

			    public void actionPerformed(ActionEvent e) {
				if (JFileChooser.APPROVE_OPTION ==dicomFileDialog.showNewDialog(DicomImageDiffDialog.this)) {
				    File f = dicomFileDialog.getSelectedFile();
				    DicomHeader dh = new DicomHeader(f);
				    headerPanelRight.setInfo(dh);
				    try {
					imageRight = DicomOpener.loadImage(f);
				    } catch (FileNotFoundException e1) {
					e1.printStackTrace();
					imageRight = null;
				    }
				    dipRight.setImage(imageRight);
				    createDiffImage();
				}
			    }
			});
			filePanel.add(jbRight);
			
		this.add(filePanel, BorderLayout.NORTH);
		
		
		this.imagePanel = new JPanel();
		this.imagePanel.setLayout(new FormLayout("fill:pref:grow, fill:pref:grow, fill:pref:grow","fill:270dlu, 3dlu, fill:pref:grow"));
		CellConstraints cc = new CellConstraints();
		
		this.dipLeft = new DicomImagePanel(300,300);
		this.dipDiff = new DicomImagePanel(300,300);
		this.dipRight = new DicomImagePanel(300,300);
		
		WindowListener wl = new WindowListener() {
		    public void setWindow(DicomImagePanel source, int windowCenter,
			    int windowWidth) {
			
			if (! source.equals(dipLeft)) {
			    dipLeft.setWindow(windowCenter, windowWidth);
			    dipLeft.repaint();
			}
//			if (! source.equals(dipDiff)) {
//			    dipDiff.setWindow(windowCenter, windowWidth);
//			    dipDiff.repaint();
//			}
			if (! source.equals(dipRight)) {
			    dipRight.setWindow(windowCenter, windowWidth);
			    dipRight.repaint();
			}
			
		    }
		};
		
		this.dipLeft.addWindowsListener(wl);
//		this.dipDiff.addWindowsListener(wl);
		this.dipRight.addWindowsListener(wl);
		
		this.imagePanel.add(dipLeft, cc.xy(1,1));
		this.imagePanel.add(dipDiff, cc.xy(2,1));
		this.imagePanel.add(dipRight, cc.xy(3,1));

		
		//		this.add(headerPanel, BorderLayout.SOUTH);
		this.imagePanel.add(headerPanel, cc.xyw(1,3,3));
		
		this.add(this.imagePanel, BorderLayout.CENTER);
		
		
		
		this.setSize(800,800);
		this.setVisible(true);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	protected void createDiffImage() {
	    if (imageLeft == null || imageRight == null) {
		imageDiff = null;
	    } else {
		ImageCalculator ic = new ImageCalculator();
		imageDiff = new ImagePlus("diff", imageLeft.getProcessor().duplicate());
		FileInfo fi = new FileInfo();
	    	    fi.directory = imageLeft.getOriginalFileInfo().directory;
	    	    fi.fileName = imageLeft.getOriginalFileInfo().fileName;
	    	imageDiff.setFileInfo(fi);			
//		imageDiff.show();
		ic.calculate("diff", imageDiff, imageRight);
	    }
	    dipDiff.setImage(imageDiff);
	}

	public void setInfo(File f) {
		DicomHeader dh = new DicomHeader(f);
		this.headerPanelLeft.setInfo(dh);
	}

	
	class MyAdjustmentListener implements AdjustmentListener {
        // This method is called whenever the value of a scrollbar is changed,
        // either by the user or programmatically.
        public void adjustmentValueChanged(AdjustmentEvent evt) {
            Adjustable source = evt.getAdjustable();
    
            // getValueIsAdjusting() returns true if the user is currently
            // dragging the scrollbar's knob and has not picked a final value
            if (evt.getValueIsAdjusting()) {
                // The user is dragging the knob
                return;
            }
    
            // Determine which scrollbar fired the event
            int orient = source.getOrientation();
            if (orient == Adjustable.HORIZONTAL) {
                // Event from horizontal scrollbar
            } else {
                // Event from vertical scrollbar
            }
    
            // Determine the type of event
            int type = evt.getAdjustmentType();
            switch (type) {
              case AdjustmentEvent.UNIT_INCREMENT:
                  // Scrollbar was increased by one unit
                  break;
              case AdjustmentEvent.UNIT_DECREMENT:
                  // Scrollbar was decreased by one unit
                  break;
              case AdjustmentEvent.BLOCK_INCREMENT:
                  // Scrollbar was increased by one block
                  break;
              case AdjustmentEvent.BLOCK_DECREMENT:
                  // Scrollbar was decreased by one block
                  break;
              case AdjustmentEvent.TRACK:
                  // The knob on the scrollbar was dragged
                  break;
            }
    
            // Get current value
            int value = evt.getValue();
            
            headerPanelLeft.setAdjustment(value);
            headerPanelRight.setAdjustment(value);
            
        }
    }

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DicomHeaderInfoPanel.FILE_CHANGED)) {	
			
			if (headerPanelLeft.getText() == null || headerPanelLeft.getText().length()<2 || 
					headerPanelRight.getText() == null || headerPanelRight.getText().length()<2)
				return;
			
			Diff diff= new Diff();
			
			headerPanelLeft.showDiff(diff.diff(headerPanelLeft.getText(), headerPanelRight.getText()));
			
			headerPanelRight.showDiff(diff.diff(headerPanelRight.getText(), headerPanelLeft.getText()));
			
			try {
				this.setTitle("Dicom Diff: " + headerPanelLeft.getFile().getName() + " vs " + headerPanelRight.getFile().getName());
			} catch (Exception e) {
			}
		}
	}
	

}
