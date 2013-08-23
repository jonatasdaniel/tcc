package lu.tudor.santec.dicom.gui.header;

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
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.gui.DicomFileDialog;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHeaderDiffDialog extends JFrame implements PropertyChangeListener{
	
	private static final long serialVersionUID = 1L;

	private DicomHeaderInfoPanel dicomHeaderInfoPanel;

	private Vector<JPanel> panels = new Vector<JPanel>();

	private MyAdjustmentListener adjustmentListener;

	private DicomFileDialog dicomFileDialog;

	private JPanel mainPanel;

	private JPanel filePanel;

	public DicomHeaderDiffDialog(DicomFileDialog dialog) {
	    	this.dicomFileDialog = dialog;
		this.setTitle("DICOM Header Diff");
		this.buildPanel(2);
	}
	
	private void buildPanel(int files) {
	    
	    	this.setLayout(new BorderLayout());
	    
	    	this.filePanel = new JPanel(new GridLayout(1,0));
	    
	    	this.mainPanel = new JPanel();
		this.mainPanel.setLayout(new GridLayout(1,0));
		this.setTitle("DICOM Diff");
		
		this.adjustmentListener = new MyAdjustmentListener();
		
		for (int i = 0; i <files; i++) {
			dicomHeaderInfoPanel = new DicomHeaderInfoPanel(null);
			dicomHeaderInfoPanel.addAdjustmentListener(adjustmentListener);
			dicomHeaderInfoPanel.addPropertyChangeListener(this);
			panels.add(dicomHeaderInfoPanel);
			mainPanel.add(dicomHeaderInfoPanel);
			
			JButton jb = new JButton("select file");
			jb.setActionCommand(i+"");
			jb.addActionListener(new ActionListener() {					    
			    public void actionPerformed(ActionEvent e) {
				if (JFileChooser.APPROVE_OPTION ==dicomFileDialog.showNewDialog(DicomHeaderDiffDialog.this)) {
				    DicomHeader dh = new DicomHeader(dicomFileDialog.getSelectedFile());
				    ((DicomHeaderInfoPanel)panels.get(Integer.parseInt(e.getActionCommand()))).setInfo(dh);
				}
			    }
			});
			
			filePanel.add(jb);
			
		}
		
		this.add(filePanel, BorderLayout.NORTH);
		this.add(mainPanel, BorderLayout.CENTER);
		
		this.setSize(800,800);
		this.setVisible(true);
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}

	public void setInfo(File f) {
		DicomHeader dh = new DicomHeader(f);
		this.dicomHeaderInfoPanel.setInfo(dh);
	}
	
//	public static void main(String[] args) {
//		DicomDiffDialog dhd = new DicomDiffDialog(new DicomFileDialog());
//		dhd.setInfo(new File("/home/hermenj/tux.dcm"));
//		
//	}
	
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
            
            for (Iterator<JPanel> iter = panels.iterator(); iter.hasNext();) {
				DicomHeaderInfoPanel element = (DicomHeaderInfoPanel) iter.next();
				element.setAdjustment(value);
			}
            
        }
    }

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(DicomHeaderInfoPanel.FILE_CHANGED)) {	
			
			DicomHeaderInfoPanel p1 = (DicomHeaderInfoPanel) panels.get(0);
			DicomHeaderInfoPanel p2 = (DicomHeaderInfoPanel) panels.get(1);
			
			if (p1.getText() == null || p1.getText().length()<2 || 
					p2.getText() == null || p2.getText().length()<2)
				return;
			
			Diff diff= new Diff();
			
			p1.showDiff(diff.diff(p1.getText(), p2.getText()));
			
			p2.showDiff(diff.diff(p2.getText(), p1.getText()));
			
			try {
				this.setTitle("Dicom Diff: " + p1.getFile().getName() + " vs " + p2.getFile().getName());
			} catch (Exception e) {
			}
		}
	}


}
