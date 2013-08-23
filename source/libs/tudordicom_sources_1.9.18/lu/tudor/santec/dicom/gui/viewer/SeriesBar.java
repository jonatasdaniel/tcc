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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.i18n.Translatrix;
import net.iharder.dnd.FileDrop;

import org.dcm4che2.data.Tag;

import com.l2fprod.common.swing.JButtonBar;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class SeriesBar extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L;
	private ButtonGroup group= new ButtonGroup();
	private DicomImagePanel dicomImagePanel;
	private JButtonBar jbuttonbar;
	private JScrollPane jsp;
	private JCheckBox imageInMemory;
	private LinkedHashMap<String, Series> series = new LinkedHashMap<String, Series>();
	private Series activeSeries;

	// the logger for this class
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private JButton nextButton;
	private JButton prevButton;
	private JPopupMenu popup;
	private boolean memoryButton;
	private JToggleButton currButton;
	private JButton clearButton;

	public SeriesBar(int direction, int thumbSize, DicomImagePanel dip) {
		init(direction, thumbSize, dip, true);
	}
	
	public SeriesBar(int direction, int thumbSize, DicomImagePanel dip, boolean memoryButton) {
		init(direction, thumbSize, dip, memoryButton);
	}
	
	/**
	 * @param thumbSize  
	 */
	private void init(int direction, int thumbSize, DicomImagePanel dip, boolean memoryButton) {
		this.memoryButton = memoryButton;
		this.setLayout(new BorderLayout());
		this.jbuttonbar = new JButtonBar(direction);
		this.dicomImagePanel = dip;
		this.dicomImagePanel.setFilesDropable(false);
		this.jsp = new JScrollPane(this.jbuttonbar);
		this.jsp.getVerticalScrollBar().setUnitIncrement(10);
		this.jsp.getHorizontalScrollBar().setUnitIncrement(10);
		this.jsp.setAutoscrolls(true);
		this.add(jsp,BorderLayout.CENTER);
		this.add(getSeriesPanel(), BorderLayout.NORTH);
		this.setMinimumSize(new Dimension(150,150));
		this.setPreferredSize(new Dimension(200, 200));
		imageInMemory = new JCheckBox("Images in Memory",true);
		if (memoryButton)
			this.add(imageInMemory, BorderLayout.SOUTH);
		
		new  FileDrop( this, new FileDrop.Listener()
		      {   public void  filesDropped( java.io.File[] files )
		          {   
			  // handle file drop
			    // add dirs as series
			    for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
				    File[] innerFiles = files[i].listFiles();
				    addSeries(innerFiles);    
				}
			    }
			    // add single files as one series
			    addSeries(files);
		          }   // end filesDropped
		      }
		); // end FileDrop.Listener

		
	}
	
	private JPanel getSeriesPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(1,2));
		nextButton = new JButton(Translatrix.getTranslationString("series.next"));
		nextButton.addActionListener(this);
		prevButton = new JButton(Translatrix.getTranslationString("series.prev"));
		prevButton.addActionListener(this);
		panel.add(prevButton);
		panel.add(nextButton);
		mainPanel.add(panel, BorderLayout.CENTER);
		clearButton = new JButton(Translatrix.getTranslationString("series.closeAllSeries"));
		clearButton.addActionListener(this);
		mainPanel.add(clearButton, BorderLayout.SOUTH);
		return mainPanel;
	}
	
	public void addSeries(final File[] files) {
	    if (files == null ) return;
		
		new Thread() {
			public void run() {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				Collection<File[]> seriesFiles = seperateSeries(files);
				for (File[]  files : seriesFiles) {
				    Series s = new Series(files, imageInMemory.isSelected());
				    String actionCommand = s.ID;
				    
				    if ( files.length == 1) {
				    	try {
				    		actionCommand+=new DicomHeader(s.imagePlus).getHeaderStringValue(Tag.InstanceNumber);								
						} catch (Exception e) {
							e.printStackTrace();
						}
				    } 
				    	
					if (series.containsKey(actionCommand)) 
						continue;				    	
				    
				    if (activeSeries == null) {
						activeSeries = s;
						dicomImagePanel.setImage(s.imagePlus);
						dicomImagePanel.setSeries(activeSeries);
				    }
				    series.put(actionCommand,s);
				    if (s.imagePlus != null) {
					JToggleButton fileButton = s.button;
					fileButton.setBorder(new LineBorder(Color.BLACK));
					fileButton.setActionCommand(actionCommand);
					fileButton.addActionListener(SeriesBar.this);
					fileButton.addMouseListener(SeriesBar.this);
					jbuttonbar.add(fileButton);
					group.add(fileButton);
				    }
				    jbuttonbar.validate();
				    System.gc();
				}
				
			    jbuttonbar.validate();
			    System.gc();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				
				
			}
		}.start();
		
	}
	
	public void addSeries(final Collection<byte[]> images) {
		addSeries(images, false);
	}
	
	/**
	 * @param selectSeries  
	 */
	public void addSeries(final Collection<byte[]> images, boolean selectSeries) {
		
		new Thread() {
			public void run() {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				
				Series s = new Series(images); 
				String actionCommand = s.ID;
			    
			    if ( images.size() == 1) {
			    	try {
			    		actionCommand+=new DicomHeader(s.imagePlus).getHeaderStringValue(Tag.InstanceNumber);								
					} catch (Exception e) {
						e.printStackTrace();
					}
			    }
				
			    if (series.containsKey(actionCommand)) {
					jbuttonbar.validate();
					System.gc();
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					return;
			    }
				
				if (activeSeries == null) {
					activeSeries = s;
				}
				series.put(actionCommand,s);
				if (s.imagePlus != null) {
					JToggleButton fileButton = s.button;
					fileButton.setBorder(new LineBorder(Color.BLACK));
					fileButton.setActionCommand(actionCommand);
					fileButton.addActionListener(SeriesBar.this);
					fileButton.addMouseListener(SeriesBar.this);
					jbuttonbar.add(fileButton);
					group.add(fileButton);
					fileButton.doClick();
				}
				
				jbuttonbar.validate();
				System.gc();
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}.start();
		
	}
	
	public void removeallSeries() {
		Component[] c = this.jbuttonbar.getComponents();
		series.clear();
		for (int i = 0; i < c.length; i++) {
			if (c[i] instanceof JToggleButton) {
				jbuttonbar.remove(c[i]);
			}
		}
		this.jbuttonbar.removeAll();
		this.jbuttonbar.updateUI();
		this.jbuttonbar.validate();
		
		dicomImagePanel.setImage(null);
		dicomImagePanel.setSeries(null);
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getSource().equals(nextButton)) {
			this.showNextImage();
		} else if (e.getSource().equals(prevButton)) {
			this.showPreviousImage();
		} else if (e.getSource().equals(clearButton)) {
			this.removeallSeries();
		}else {
		
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			try {
				Series s = (Series) series.get(e.getActionCommand());
				if (s != null) {
					activeSeries = s;
					logger.info("Loading Series: " +  e.getActionCommand());
					dicomImagePanel.setImage(s.imagePlus);
					dicomImagePanel.setSeries(activeSeries);
				} else {
					logger.info("Error loading Series: " + e.getActionCommand());
				}

			} catch (Error err) {
				err.printStackTrace();
				
			}
			System.gc();
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
		
	public void setImagePanel(DicomImagePanel panel) {
	    	this.dicomImagePanel.setFilesDropable(false);
		this.dicomImagePanel = panel;
	}
	
	public void showNextImage() {
		if (activeSeries == null)
			return;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
				activeSeries.next();
				dicomImagePanel.setImage(activeSeries.imagePlus);
				dicomImagePanel.setSeries(activeSeries);
		} catch (Error err) {
			err.printStackTrace();
			
		}
		System.gc();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void showPreviousImage() {
		if (activeSeries == null)
			return;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
				activeSeries.previous();
				dicomImagePanel.setImage(activeSeries.imagePlus);
				dicomImagePanel.setSeries(activeSeries);
		} catch (Error err) {
			err.printStackTrace();
			
		}
		System.gc();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		dicomImagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void setCurrentSeries(Series s) {
		this.activeSeries = s;
		this.activeSeries.button.setSelected(true);
	}
	
	public Series getCurrentSeries() {
		return this.activeSeries;
	}


	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		//		show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
			// set mousecursor for windowing and moving
		}
	}

	public void mouseReleased(MouseEvent e) {
		//			show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
			// set mousecursor for windowing and moving
		}
	}
	
	/**
	 * creates the popupmenue
	 * @param c
	 * @param x
	 * @param y
	 */
	private void showPopup(Component c, int x, int y) {
		this.currButton = (JToggleButton) c ;
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(new AbstractAction(Translatrix.getTranslationString("dicom.SeriesBar.closeSeries")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					try {
						Series s = (Series) series.get(currButton.getActionCommand());
						if (s != null) {
							jbuttonbar.remove(currButton); 
							series.remove(currButton.getActionCommand());
							jbuttonbar.updateUI();
							jbuttonbar.validate();
							logger.info("Series: " + s.ID + " deleted");
							s = null;
							activeSeries = null;
							dicomImagePanel.setImage(null);
							dicomImagePanel.setSeries(null);
						} else {
							logger.info("Error removing Series: ");
						}
						
					} catch (Error err) {
						err.printStackTrace();
						
					}
				}
			});

		}
		popup.show(c, x, y);
	}
	
	
	/**
	 * seperate the files into seperate arrays for each seriesUID
	 * and sorts each series by instanceNo.
	 * @param files2sort
	 * @return
	 */
	public static Vector<File[]> seperateSeries(File[] files2sort) {
	    HashMap<String, SortedMap<Integer, File>> series = new HashMap<String, SortedMap<Integer, File>>();
	    
	    try {
	    	for (File file : files2sort) {

	    		DicomHeader dh = new DicomHeader(file);
	    		if (! dh.isEmpty()) {
	    			String seriesUID = dh.getHeaderStringValue(Tag.SeriesInstanceUID);
	    			int instanceNr = dh.getHeaderIntegerValue(Tag.InstanceNumber);
	    			if (!series.containsKey(seriesUID)) {
	    				SortedMap<Integer, File> al = new TreeMap<Integer, File>();
	    				series.put(seriesUID, al);
	    			} 
	    			(series.get(seriesUID)).put(instanceNr, file);
	    		}	    			
	    	}
		} catch (Error e) {
			System.out.println(e);
		}
	    
	    
	    Vector<File[]> retVal = new Vector<File[]>();
	    for (SortedMap<Integer, File> alFiles : series.values()) {
		retVal.add(alFiles.values().toArray(new File[0]));		
	    }
	    return retVal;
	}
	
}
