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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.OverlayExtractor;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderInfoDialog;
import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class ImagePreviewDicom extends JPanel implements PropertyChangeListener, ActionListener {

	private static final long serialVersionUID = 1L;

	ImageIcon thumbnail = null;

	File file = null;

	private JLabel iconLabel = new JLabel();

	private JLabel textLabel = new JLabel();
	
	private JButton showDicomInfo = new JButton();
	
	private DicomHeaderInfoDialog infoDialog;
	
	private ImagePlus image;

	private String[] dicomFields;

	private DicomHeader dh;

	private JScrollPane textScrollPanel;

	private JPanel content;

	private JCheckBox showPreviewCheckBox;

	public static boolean showPreview = true;
	
	private static Vector listeners = new Vector();

	public ImagePreviewDicom(JComponent fc, JDialog parent, String[] dicomFields) {
		this.infoDialog = new DicomHeaderInfoDialog(parent);
		this.dicomFields = dicomFields;
		fc.addPropertyChangeListener(this);
		this.setLayout(new BorderLayout());
		
		this.textLabel.setVerticalAlignment(JLabel.TOP);
		this.iconLabel.setBorder(new TitledBorder(Translatrix
				.getTranslationString("dicom.Preview")));
		this.textScrollPanel = new JScrollPane(this.textLabel);
		this.textScrollPanel.setPreferredSize(new Dimension(220, 230));
		this.textScrollPanel.setBorder(new TitledBorder(Translatrix
				.getTranslationString("dicom.Informations")));
		this.iconLabel.setPreferredSize(new Dimension(220, 230));
		this.iconLabel.setMaximumSize(new Dimension(220, 230));
		this.iconLabel.setMinimumSize(new Dimension(220, 230));
		this.iconLabel.setVerticalAlignment(JLabel.CENTER);
		this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
		this.showDicomInfo.setText(Translatrix
				.getTranslationString("dicom.ShowInfo"));
		this.showDicomInfo.addActionListener(this);
		this.showDicomInfo.setEnabled(false);
		
		showPreviewCheckBox = new JCheckBox(Translatrix.getTranslationString("dicom.showPreview"), showPreview);
		showPreviewCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showPreview = showPreviewCheckBox.isSelected();
				for (Iterator iter = listeners.iterator(); iter.hasNext();) {
					ImagePreviewDicom element = (ImagePreviewDicom) iter.next();
					element.firePropertyChange("showPreview", null, null);					
				}
			}
		});
		
		
		this.add(showPreviewCheckBox, BorderLayout.NORTH);

		buildContentPanel(showPreview);
		
		this.add(showDicomInfo, BorderLayout.SOUTH);
		
		
		this.addPropertyChangeListener(this);
		listeners.add(this);
		
	}
	
	private void buildContentPanel(boolean showPreview) {
		try {
			this.remove(content);			
		} catch (Exception e) {
		}
		
		content = new JPanel(new BorderLayout());
		
		if (showPreview) {
			content.add(iconLabel, BorderLayout.NORTH);
		}
		content.add(textScrollPanel, BorderLayout.CENTER);
		this.add(content, BorderLayout.CENTER);
		
		this.updateUI();
		this.validate();
		
	}

	public void loadImage(File file) {
		
		System.gc();
		
		if (file == null || file.isDirectory()) {
			thumbnail = null;
			this.iconLabel.setIcon(null);
			this.iconLabel.setText("no Preview available");
			this.textLabel.setText(Translatrix.getTranslationString("dicom.noDICOMImage"));
			this.showDicomInfo.setEnabled(false);
			return;
		}

		try {
			
		    dh = new DicomHeader(file);
		    
			String info = "";
			if (dh != null) {				
				for (int i = 0; i < this.dicomFields.length; i++) {
					info += getDicomHeader(this.dicomFields[i]);
				}
				info +=  "File Size" + ": <font color='#787878'>"
				+ file.length()/1024 + "</font><br>";
				this.showDicomInfo.setEnabled(true);
			} else {
				info += "no DICOM Image";
				this.showDicomInfo.setEnabled(false);
			}
			this.textLabel.setText("<html><body>" + info);

		    if (showPreview) {

				image = DicomOpener.loadImage(file, dh, null);

				if (OverlayExtractor.hasOverlayImage(dh.getDicomObject())) {
				    image = OverlayExtractor.createOverlayImage(image, dh.getDicomObject());
				}
				
				
				if (image != null) {
					
					Image awtImage = image.getImage();
					int thumbWidth = 200;
					int thumbHeight = 200;
					double thumbRatio = (double)thumbWidth / (double)thumbHeight;
					int imageWidth = awtImage.getWidth(null);
					int imageHeight = awtImage.getHeight(null);
					double imageRatio = (double)imageWidth / (double)imageHeight;
					if (thumbRatio < imageRatio) {
						thumbHeight = (int)(thumbWidth / imageRatio);
					} else {
						thumbWidth = (int)(thumbHeight * imageRatio);
					}
					BufferedImage thumbImage = new BufferedImage(thumbWidth, 
							thumbHeight, BufferedImage.TYPE_INT_RGB);
					Graphics2D graphics2D = thumbImage.createGraphics();
					graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
							RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					graphics2D.drawImage(awtImage, 0, 0, thumbWidth, thumbHeight, null);
					
					this.thumbnail = new ImageIcon(thumbImage);
					this.iconLabel.setIcon(this.thumbnail);
					this.iconLabel.setText("");
					awtImage = null;
					image = null;
					
					
				} else {
					this.iconLabel.setIcon(null);
					this.iconLabel.setText("no Preview available");
				}
			} else {
				image = null;
				this.iconLabel.setIcon(null);
				this.iconLabel.setText("Preview disabled");
			}
		} catch (Exception e) {
			// no Dicom File
			this.textLabel.setText(Translatrix.getTranslationString("dicom.noDICOMImage"));
			this.iconLabel.setIcon(null);
			this.iconLabel.setText("");
			this.showDicomInfo.setEnabled(false);
		} catch (Error e1) {
			// no Dicom File
			this.textLabel.setText(Translatrix.getTranslationString("dicom.noDICOMImage"));
			this.iconLabel.setIcon(null);
			this.iconLabel.setText("");
			this.showDicomInfo.setEnabled(false);
		}
		
		System.gc();
		
	}

	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();
		
		if (prop.equals("showPreview")) {
			showPreviewCheckBox.setSelected(showPreview);
			buildContentPanel(showPreview);
		}
		
		if (showPreview) {
			update = true;
		}
 
		// If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			file = null;
			update = true;

			// If a file became selected, find out which one.
		} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			file = (File) e.getNewValue();
			update = true;
		}

		// Update the preview accordingly.
		if (update) {
			thumbnail = null;
			if (isShowing()) {
				loadImage(file);
			}
		}
	}

	/**
	 * Get the DICOM entry
	 * 
	 * @param header
	 *            DICOM Header
	 * @param userInput
	 *            DICOM entry index
	 * @return DICOM entry
	 */
	private String getDicomHeader(String dicomTag) {

		String attrvalue = "";

		if (dh != null) {
				try {
					String name = dh.getHeaderName(dicomTag);
					String value = dh.getHeaderStringValue(dicomTag);
				
					if (dicomTag.equals("0008,0021")) { // Series Date
						value = value.substring(6,8) + "."+ value.substring(4,6) + "." + value.substring(0,4);
					} else if (dicomTag.equals("0008,0031")) {  // Series Time
						value = value.substring(0,2) + ":" + value.substring(2,4) +":" +value.substring(4,6);
					}
					
					if (value == null || value.equals(""))
						value = Translatrix.getTranslationString("dicom.notavailable");
					
					int size = (name + value).length();
					if (size > 30 ) {
//						value = value.substring(0,26-name.length());
						value = "<br>&nbsp;" + value;
					}
					
					if (name == null || name.equals("")) {
						name=dicomTag;
					}
						attrvalue = "" + name + ": <font color='#787878'>"
							+ value + "</font><br>";
				} catch (Throwable e) { // Anything else
//					e.printStackTrace();
				}
			}			
		return attrvalue;
	}
	
	public static String formatDicomInfo(String header) {
		return header.replaceAll("\n","\r\n").replaceAll(((char)0)+""," ");
	}

	public void actionPerformed(ActionEvent arg0) {
			this.infoDialog.setInfo(dh);
			this.infoDialog.setVisible(true);		
	}

}
