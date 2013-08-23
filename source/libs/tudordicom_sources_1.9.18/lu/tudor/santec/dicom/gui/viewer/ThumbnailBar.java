package lu.tudor.santec.dicom.gui.viewer;
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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import lu.tudor.santec.dicom.gui.DicomIcons;

import com.l2fprod.common.swing.JButtonBar;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class ThumbnailBar extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private int thumbSize;
	private ButtonGroup group;
	private File[] fileArr;
	private DicomImagePanel dicomImagePanel;
	private JButtonBar jbuttonbar;
	private JScrollPane jsp;
	private JToggleButton videoButton;
	private VideoThread vt;
	public boolean videoRunning;
	private JTextField videoSpeed;
	private JCheckBox imageInMemory;
	private HashMap<String, ImagePlus> images = new HashMap<String, ImagePlus>();
	// the logger for this class
	private Logger logger = Logger
			.getLogger("lu.tudor.santec.dicom.gui.viewer.ThumbnailBar");

	public ThumbnailBar(int direction, int thumbSize, DicomImagePanel dip) {
		this.setLayout(new BorderLayout());
		this.jbuttonbar = new JButtonBar(direction);
		this.thumbSize = thumbSize;
		this.dicomImagePanel = dip;
		this.jsp = new JScrollPane(this.jbuttonbar);
		this.jsp.getVerticalScrollBar().setUnitIncrement(10);
		this.jsp.getHorizontalScrollBar().setUnitIncrement(10);
		this.add(jsp,BorderLayout.CENTER);
		this.add(getVideoPanel(), BorderLayout.NORTH);
		this.setMinimumSize(new Dimension(thumbSize + 30, thumbSize + 30));
//		this.setPreferredSize(new Dimension(thumbSize + 20, thumbSize + 20));
		imageInMemory = new JCheckBox("Images in Memory");
		this.add(imageInMemory, BorderLayout.SOUTH);
	}
	
	public void setThumbs(File[] files) {
		this.fileArr = files;
		group = new ButtonGroup();
		Component[] c = this.jbuttonbar.getComponents();
		for (int i = 0; i < c.length; i++) {
			if (c[i] instanceof JToggleButton) {
				jbuttonbar.remove(c[i]);
			}
		}
//		this.jbuttonbar.removeAll();
		new Thread() {
			public void run() {
				boolean imageSet = false;
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				for (int i = 0; i < fileArr.length; i++) {
					try {

						ImagePlus ip = new ImagePlus(fileArr[i].getAbsolutePath());
						if (imageInMemory.isSelected()) {
							images.put(fileArr[i].getAbsolutePath(), ip);
						}
						Image image = ip.getImage();
						if (imageSet == false)  {
							try {
								dicomImagePanel.setImage(ip);
								imageSet = true;
							} catch (Error err) {
								err.printStackTrace();
							}
						}
						
						// determine thumbnail size from WIDTH and HEIGHT
					    int thumbWidth = thumbSize;
					    int thumbHeight = thumbSize;
					    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
					    int imageWidth = image.getWidth(null);
					    int imageHeight = image.getHeight(null);
					    double imageRatio = (double)imageWidth / (double)imageHeight;
					    if (thumbRatio < imageRatio) {
					      thumbHeight = (int)(thumbWidth / imageRatio);
					    } else {
					      thumbWidth = (int)(thumbHeight * imageRatio);
					    }
					    // draw original image to thumbnail image object and
					    // scale it to the new size on-the-fly
					    BufferedImage thumbImage = new BufferedImage(thumbWidth, 
					      thumbHeight, BufferedImage.TYPE_INT_RGB);
					    Graphics2D graphics2D = thumbImage.createGraphics();
					    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
					    
					    ImageIcon ii = new ImageIcon(thumbImage);

						if (ii != null) {
							JToggleButton fileButton = new JToggleButton(
									fileArr[i].getName(), ii);
							fileButton.setActionCommand(fileArr[i]
									.getAbsolutePath());
							fileButton.addActionListener(ThumbnailBar.this);
							jbuttonbar.add(fileButton);
							group.add(fileButton);
						}
					} catch (Exception e) {
						System.out.println("not an image");
					}
				}
				System.gc();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}.start();
		
	}

	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource().equals(videoButton)) {
			if (videoButton.isSelected()) {
				videoButton.setIcon(DicomIcons.getIcon16(DicomIcons.VIDEO_PAUSE));
				vt = new VideoThread();
				vt.start();
			} else {
				videoButton.setIcon(DicomIcons.getIcon16(DicomIcons.VIDEO_PLAY));
				videoRunning = false;
				vt.interrupt();
			}
		} else {
//			System.out.println("File " + e.getActionCommand() + " selected");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				ImagePlus ip = (ImagePlus) images.get(e.getActionCommand());
				if (ip == null) {
					logger.info("Loading image from File System");
					ip = new ImagePlus(e.getActionCommand());
				} else {
					logger.info("Loading image from Memory");
				}
				dicomImagePanel.setImage(ip);
			} catch (Error err) {
				err.printStackTrace();
				
			}
			System.gc();
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	public JPanel getVideoPanel() {
		JPanel videoPanel = new JPanel(new GridLayout(1,2));
		videoButton = new JToggleButton(DicomIcons.getIcon16(DicomIcons.VIDEO_PLAY));
		videoButton.addActionListener(this);
		videoSpeed = new JTextField();
		videoSpeed.setText(200+"");
		videoPanel.add(videoButton);
		videoPanel.add(videoSpeed);
		return videoPanel;
	}
	
	public void setImagePanel(DicomImagePanel panel) {
		this.dicomImagePanel = panel;
	}
	
	public class VideoThread extends Thread {

		public void run() {
			videoRunning = true;
			Component[] c = jbuttonbar.getComponents();
			while (true) {
				for (int i = 0; i < c.length; i++) {
					if (! videoRunning ) {
						return;
					}
					if (c[i] instanceof JToggleButton) {
						((JToggleButton) c[i]).doClick();
						try {
							Thread.sleep(Integer.parseInt(videoSpeed.getText()));
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}
	
}
