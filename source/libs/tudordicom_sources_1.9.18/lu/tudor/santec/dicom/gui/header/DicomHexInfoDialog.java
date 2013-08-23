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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHexInfoDialog extends JDialog implements ActionListener{
	
	private static final long serialVersionUID = 1L;

	private DicomHexInfoPanel dicomHexInfoPanel;

	private Vector<JPanel> panels = new Vector<JPanel>();

	JButton close = new JButton("close");

	public DicomHexInfoDialog(JDialog parent) {
		super(parent);
		this.setModal(true);
		this.buildPanel();
	}
	
	public DicomHexInfoDialog() {
		this.getContentPane().setLayout(new  BorderLayout());
		this.setTitle("DICOM HEX");
		this.buildPanel();
	}
	
	private void buildPanel() {
		this.getContentPane().setLayout(new BorderLayout());
		this.setTitle("DICOM HEX");

		dicomHexInfoPanel = new DicomHexInfoPanel();
		this.panels .add(dicomHexInfoPanel);
		this.add(dicomHexInfoPanel, BorderLayout.CENTER);
		this.close.addActionListener(this);
		this.add(close, BorderLayout.SOUTH);
		this.setSize(720,700);
		this.setResizable(false);
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(this.close))
			this.setVisible(false);
	}
	
	public void setInfo(File f) {
		this.dicomHexInfoPanel.setFile(f);
	}
	
	public static void main(String[] args) {
		DicomHexInfoDialog dhd = new DicomHexInfoDialog();
		dhd.setInfo(new File("/home/hermenj/tux.dcm"));
		dhd.setVisible(true);
		
	}
	
}
