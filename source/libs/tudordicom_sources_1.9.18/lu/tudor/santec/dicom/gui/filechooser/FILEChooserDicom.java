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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;

import org.dcm4che.util.DcmURL;

import lu.tudor.santec.dicom.gui.DicomFilter;
import lu.tudor.santec.dicom.gui.DicomURLChooser;
import lu.tudor.santec.dicom.gui.ErrorDialog;
import lu.tudor.santec.dicom.gui.ImagePreviewDicom;
import lu.tudor.santec.dicom.sender.DicomSender;
import lu.tudor.santec.i18n.Translatrix;

/**
 * A FileChooser to open DICOM Files from the filesystem.
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 * 
 */
public class FILEChooserDicom extends JPanel implements ActionListener,
		PropertyChangeListener, DicomFilter {

	private static final long serialVersionUID = 1L;

	private FILEPanelDicom filePanel;

	private JFileChooser fileChooser;

	private boolean send;

	private ProgressMonitor progressMonitor;

	private File[] arr;

	private DcmURL[] senders;

	private DicomSender dicomSender;

	private boolean disableListener;

	/**
	 * creates a new DicomFuileChooser
	 * 
	 * @param dir
	 *            the startdir
	 * @param parent
	 *            the parent DicomFileDialog
	 */
	public FILEChooserDicom(File dir, FILEPanelDicom filePanel, boolean send, String[] dicomFields) {
		this.send = send;
		this.filePanel = filePanel;
		this.setLayout(new BorderLayout());
		this.fileChooser = new JFileChooser(dir);
		this.fileChooser.addActionListener(this);
		this.fileChooser.addPropertyChangeListener(this);
		this.fileChooser.setAccessory(new ImagePreviewDicom(fileChooser, filePanel.getDicomFileDialog(), dicomFields));
		 this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		 this.fileChooser.setLocale(Translatrix.getLocale());
		this.add(this.fileChooser, BorderLayout.CENTER);

		if (send) {
			this.fileChooser.setApproveButtonText(Translatrix.getTranslationString("dicom.Send"));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
			filePanel.getDicomFileDialog().setRetValue(
					JFileChooser.CANCEL_OPTION);
			filePanel.getDicomFileDialog().setVisible(false);
		} else if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)
				&& this.fileChooser.getSelectedFile() != null) {

			selectFiles(this.fileChooser.getSelectedFile());
			
			// send images
			if (send) {
				this.send();
				// or close dialog
			} else {
				filePanel.getDicomFileDialog().setRetValue(
						JFileChooser.APPROVE_OPTION);
				filePanel.getDicomFileDialog().setVisible(false);
			}
		}
	}
	
	private void selectFiles(File f) {
		// for Files only
		disableListener = true;
		if (f.isFile()) {
			// set Dialog to single file
			filePanel.getDicomFileDialog().setSingleFileSelected(true);
			filePanel.getDicomFileDialog().setSelectedFile(f);
		} else {
			filePanel.getDicomFileDialog().setSingleFileSelected(false);
		}
		// for directories and files
		if (f.isFile()) {
			File[] files = f.getParentFile().listFiles();
			Arrays.sort(files);
			filePanel.getDicomFileDialog().setSelectedFiles(files);
		} else {
			File[] files = getFilesRecursive(f);
			Arrays.sort(files);
			filePanel.getDicomFileDialog().setSelectedFiles(files);
		}
		
		if (filePanel.getDicomFileDialog().singleFileSelected == false)
			filePanel.getDicomFileDialog().setSelectedFile(null);
		disableListener = false;
	}	
	

	/**
	 * @param f
	 */
	public void setFile(File f) {
	    if (! disableListener) {
			this.fileChooser.setSelectedFile(f);
			this.validate();
//			if (f != null)
//				selectFiles(f);
		
	    }
	}

	private void send() {
		//		 if send only one file....
		if (filePanel.getDicomFileDialog().getSingleFileSelected()) {
			arr = new File[1];
			arr[0] = this.filePanel.getDicomFileDialog().getSelectedFile();
		} else {
			arr = this.filePanel.getDicomFileDialog().getSelectedFiles();
		}
		
		DcmURL url = DicomURLChooser.showDialog(filePanel.getDicomFileDialog().getCurrentDialog(), senders);
		if (url == null )
			return;
		dicomSender = new DicomSender(url);
		
		progressMonitor = new ProgressMonitor(filePanel.getDicomFileDialog().getCurrentDialog(),
				"Sending Files to: \r\n" + dicomSender.getUrl(), "preparing",
				0, arr.length);
		new Thread() {
			public void run() {
			    boolean firstError = true;
				filePanel.getDicomFileDialog().setWaitCursor(true);
					
					for (int i = 0; i < arr.length; i++) {
					    try {
						File element = (File) arr[i];
						if (progressMonitor.isCanceled()) {
							this.destroy();
						}
						progressMonitor.setNote("img " + element.getName()
								+ " ( " + (i + 1) + " of " + (arr.length + 1)
								+ " )");
						progressMonitor.setProgress(i);
						dicomSender.send(element);
					    } catch (Exception e1) {
						if (firstError) {
						    ErrorDialog.showErrorDialog(filePanel
							    .getDicomFileDialog(),"Error while sending File" , e1.getLocalizedMessage(), e1);
						    firstError = false;
						}
					    }
					}

				progressMonitor.close();
				filePanel.getDicomFileDialog().setWaitCursor(false);
			}
		}.start();
	}

	public void propertyChange(PropertyChangeEvent arg0) {
	    	disableListener = true;
		try {
			if (arg0.getPropertyName() != null && arg0.getNewValue() != null && JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(arg0
					.getPropertyName())) {
				this.filePanel.setFile(new File(arg0.getNewValue().toString()));
			}
		} catch (Exception e) {
			System.err.println("updating filechooser failed...." + arg0);
//			e.printStackTrace();
		}
		disableListener = false;
	}
	
	
	public static File[] getFilesRecursive(File dir) {
		ArrayList<File> al = new ArrayList<File>();
		visitAllFiles(dir, al);
		File[] files = new File[al.size()];
		int i = 0;
		for (Iterator<File> iter = al.iterator(); iter.hasNext();) {
			File element = (File) iter.next();
			files[i] = element;
			i++;
		}
		return files;
	}
	
//	 Process only files under dir
	private static void visitAllFiles(File dir, ArrayList<File> al) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                visitAllFiles(new File(dir, children[i]), al);
            }
        } else {
            al.add(dir);
        }
    }

	public void setDicomSenders(DcmURL[] senders) {
		this.senders = senders;
	}
	
	
	@SuppressWarnings("unchecked")
	public void setDicomFilterTags(Vector headerTags) {
		this.fileChooser.resetChoosableFileFilters();
		if (headerTags != null && headerTags.size() > 0) {
			this.fileChooser.setFileFilter(new DicomFileFilter(headerTags));
		} 
	}
	

}
