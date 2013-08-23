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
import ij.ImageStack;
import ij.io.FileInfo;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.i18n.Translatrix;


/**
 * ImageJ Plugin that allows to open dicom files from different sources
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Open_Dicom_File extends JFrame implements PlugIn {

	private static Logger logger = Logger.getLogger("PACS_Server_Plugin");

	private static final long serialVersionUID = 1L;

	private boolean error;

	private ProgressMonitor progressMonitor;

	private DicomFileDialog dicomFileDialog;

	/**
	 * create a new Instance of the Plugin
	 */
	public Open_Dicom_File() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg0) {
		System.out.println("run: " + "Open_Dicom_File " + arg0);
		File[] files = null;
		// create the dicom file chooser
		this.buildInput();		
		if ("next_image".equals(arg0)) {
			try {
				files = dicomFileDialog.getNextImagefromDicomDir(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("next_series".equals(arg0)){
			try {
				files = dicomFileDialog.getNextImagefromDicomDir(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			files = this.selectFiles();
		}
		
		this.importFiles(files);
	}


	/**
	 * create the Input Dialog
	 */
	private void buildInput() {
		dicomFileDialog = DicomFileDialog.getDicomFileDialog();
	}


	public File[] selectFiles() {
		File[] files = null;
		if (JFileChooser.APPROVE_OPTION == dicomFileDialog
				.showDialog()) {
			// one file only
			if (dicomFileDialog.getSingleFileSelected()) {
				logger.info("Single File selected");
				files = new File[1];
				files[0] = dicomFileDialog.getSelectedFile();
			} else {
				files = dicomFileDialog.getSelectedFiles();
				logger.info(files.length + " Files selected");
			}
		}
		return files;
	}
	
	/**
	 * import the selected Files into ImageJ
	 */
	public void importFiles(final File[] files) {

		progressMonitor = new ProgressMonitor(null, "loading Files", "", 0, 25);

		// use Thread while loading Images
		new Thread() {
			private Object info;
			private DicomHeader dh;

			public void run() {
				if (files == null) {
					return;
				}
				if (files.length == 1) {
					progressMonitor.setNote(dicomFileDialog.getSelectedFile()
							.getAbsolutePath());
					progressMonitor.setMaximum(1);
					ImagePlus imgPlus;
					try {
					    imgPlus = new ImagePlus(files[0].getAbsolutePath());
					    imgPlus.getStack();
					    imgPlus.show();
					} catch (Exception ee) {
					    try {
						imgPlus = DicomOpener.loadImageStack(files[0], null);
						progressMonitor.setProgress(1);
						imgPlus.show();
					    } catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					    }
					}
				} else {
					// multiple files
					logger.info(files.length + " Files selected");
					error = false;
					progressMonitor.setMaximum(files.length*2);
					
					ArrayList al = new ArrayList();
					
					ImageStack ipStack = null;
					ImagePlus imgPlus = null;
					int i = 0;
					for (i = 0; i < files.length; i++) {
						if (progressMonitor.isCanceled()) {
							break;
						}
						progressMonitor.setNote(files[i].getAbsolutePath());
						progressMonitor.setProgress(i);
						try {
						    ImagePlus ipl = DicomOpener.loadImageStack(files[i], null);
							al.add(ipl);
						} catch (Exception e) {
							logger.info("error importing series, try to open a single file only");
						} catch (Error e) {
							logger.info("error importing series, try to open a single file only");
						}

					}
					progressMonitor.setNote("sorting slices");
					Collections.sort(al, new ImageComparator());
					
					for (Iterator iter = al.iterator(); iter.hasNext();) {
						ImagePlus ipl = (ImagePlus) iter.next();
						ImageProcessor ip = ipl.getProcessor();
						try {
							progressMonitor.setNote("adding Slice: " 	+ ipl.getFileInfo().fileName);	
						} catch (Exception e) {
						}
						progressMonitor.setProgress(++i);
						try {
							if (ipStack == null) {
								info = ipl.getProperty("Info");
								dh = new DicomHeader(ipl);
								ipStack = new ImageStack(ip.getWidth(), ip
										.getHeight());
							}
							ipStack.addSlice(ipl.getFileInfo().fileName, ip);
							logger.info("Slice: " 	+ ipl.getFileInfo().fileName + "added");
						} catch (Exception e) {
							error = true;
						}
					}
					imgPlus = new ImagePlus(ipStack.getSliceLabel(1), ipStack);	
		    	    
					try {
						imgPlus.setProperty("Info",info);
						imgPlus.setProperty(DicomHeader.class.getSimpleName(), dh);						
					} catch (Exception e) {
					}
					imgPlus.show();
					if (error) {
						logger
								.warning("Some pictures could not be added to Series.\r\nTry to import a single file only.");
					}
				}
				progressMonitor.close();
			}
		}.start();
	}

	/**
	 * compares the images by slicenumber, used for sorting a series
	 * @author Johannes Hermen johannes.hermen(at)tudor.lu
	 *
	 */
	public class ImageComparator implements Comparator{

		public int compare(Object o1, Object o2) {
			try {
				DicomHeader dh1 = new DicomHeader((ImagePlus) o1);
				int sliceNr1 = Integer.parseInt(dh1.getHeaderStringValue("0020,0013"));
				DicomHeader dh2 = new DicomHeader((ImagePlus) o2);
				int sliceNr2 = Integer.parseInt(dh2.getHeaderStringValue("0020,0013"));
				if ( sliceNr1 < sliceNr2) {
					return -1;
				}else if ( sliceNr1 > sliceNr2) {
					return 1;
				}
			} catch (Exception e) {
				return -1;
			}
			return 0;
		}
		
	}
	
	
}
