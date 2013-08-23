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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHeaderInfoDialog extends JDialog implements ActionListener{
	
	private static final long serialVersionUID = 1L;

	private DicomHeaderInfoPanel dicomHeaderInfoPanel;

	private Vector<JPanel> panels = new Vector<JPanel>();

	private JLabel sliceLabel;

	private JButton prevButton;

	private JButton nextButton;

	private Vector<DicomHeader> headers;
	
	int slice = 0;

	private JButton copy;

	private JButton close;

	public DicomHeaderInfoDialog(JDialog parent) {
		super(parent);
		this.setModal(true);
		this.buildPanel(false);
	}
	
	public DicomHeaderInfoDialog() {
		this.setTitle("DICOM Header");
		this.buildPanel(false);
	}
	
	public DicomHeaderInfoDialog(JDialog parent, boolean multiSlice) {
		super(parent);
		this.setModal(true);
		this.buildPanel(multiSlice);
	}
	
	public DicomHeaderInfoDialog(boolean multiSlice) {
		this.setTitle("DICOM Header");
		this.buildPanel(multiSlice);
	}
	
	private void buildPanel(boolean multiSlice) {
		this.getContentPane().setLayout(new BorderLayout());
		this.setTitle("DICOM Header");
		
		JPanel slicePanel = new JPanel(new BorderLayout());
		this.prevButton = new JButton(DicomIcons.getIcon22(DicomIcons.PREV));
		this.prevButton.addActionListener(this);
		slicePanel.add(prevButton, BorderLayout.WEST);
		this.sliceLabel = new JLabel();
		this.sliceLabel.setFont(sliceLabel.getFont().deriveFont(16));
		this.sliceLabel.setHorizontalAlignment(JLabel.CENTER);
		this.sliceLabel.addMouseWheelListener(new MouseWheelListener() {
		    public void mouseWheelMoved(MouseWheelEvent e) {
			setSlice(slice +e.getWheelRotation());
		    }
		});
		slicePanel.add(sliceLabel, BorderLayout.CENTER);
		this.nextButton = new JButton(DicomIcons.getIcon22(DicomIcons.NEXT));
		this.nextButton.addActionListener(this);
		slicePanel.add(nextButton, BorderLayout.EAST);
		
		if (multiSlice)
		    this.add(slicePanel, BorderLayout.NORTH);

		dicomHeaderInfoPanel = new DicomHeaderInfoPanel(this);
		this.panels .add(dicomHeaderInfoPanel);
		this.add(dicomHeaderInfoPanel, BorderLayout.CENTER);
		
		ButtonBarBuilder bb = new ButtonBarBuilder();
		
		this.copy = new JButton(Translatrix.getTranslationString("dicom.copy"));
		this.copy.addActionListener(this);
		bb.addGridded(this.copy);
		bb.addRelatedGap();
		bb.addGlue();
		
		this.close = new JButton(Translatrix.getTranslationString("dicom.close"));
		this.close.addActionListener(this);
		bb.addGridded(this.close);
		
		this.add(bb.getPanel(), BorderLayout.SOUTH);
		this.setSize(600,700);
		
	}

	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource().equals(this.close))
			this.setVisible(false);
		else if (arg0.getSource().equals(this.copy))
			copy2Clipboard(dicomHeaderInfoPanel.getText());
		else if (arg0.getSource().equals(this.prevButton))
		    setSlice(slice-1);
		else if (arg0.getSource().equals(this.nextButton))
		    setSlice(slice+1);
	}

//	/**
//	 * @deprecated
//	 * @param info
//	 */
//	public void setInfo(String info) {
//		DicomHeader dh = new DicomHeader(info);
//		this.dicomHeaderInfoPanel.setInfo(dh);
//	}
	
	public void setInfo(DicomHeader dh) {
		this.slice = 0;
		this.dicomHeaderInfoPanel.setInfo(dh, true);
	}
	
//	/**
//	 * @deprecated
//	 * @param ip
//	 */
//	public void setInfo(ImagePlus ip) {
//		DicomHeader dh = new DicomHeader(ip);
//		this.dicomHeaderInfoPanel.setInfo(dh);
//	}
	
	public void setInfo(File f) {
		DicomHeader dh = new DicomHeader(f);
		this.slice = 0;
		this.dicomHeaderInfoPanel.setInfo(dh, true);
	}
	
	
	public void setInfos(Collection<DicomHeader> dhs) {
	    	this.headers = new Vector<DicomHeader>(dhs);
	    	slice = 0;
	    	setSlice(slice);	    	
	}

	public void setInfos(File[] files) {
	    	this.headers = new Vector<DicomHeader>();
	    	for (int i = 0; i < files.length; i++) {
		    this.headers.add(new DicomHeader(files[i]));
		}
	    	slice = 0;
	    	setSlice(slice);	   
	}
	
	private void setSlice(int slice) {
	    	if (slice >= this.headers.size()) slice = headers.size()-1;
	    	if (slice < 0) slice = 0;
	    	this.slice = slice;
	    	this.sliceLabel.setText("Slice " + (slice+1) + " of " + this.headers.size());
	    	this.dicomHeaderInfoPanel.setInfo(this.headers.get(slice), true);
	}
	
	public static void copy2Clipboard(String text) {
		try {
			StringSelection stringSelection = new StringSelection (text);
			Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
			clpbrd.setContents (stringSelection, null);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		DicomHeaderInfoDialog dhd = new DicomHeaderInfoDialog(true);
		dhd.setInfo(new File("/home/hermenj/tux.dcm"));
		dhd.setVisible(true);
		
	}
	
}
