package lu.tudor.santec.dicom.gui.dicomdir;

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

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomFilter;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.receiver.DicomDirWatcher;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;

import sun.awt.shell.ShellFolder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This Panel allows to Choose a DICOMDIR file and open Images from it
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 * 
 */
public class DICOMDIRPanel extends JPanel implements DICOMDIRVIEW, ActionListener, DicomFilter {

	private final static Logger log = Logger.getLogger("DICOMDIRPanel");
	
	private static final long serialVersionUID = 1L;

	private JTextField fileField;

	private JButton chooseFileButton;
	
	private JButton reloadButton;

	private JLabel fileFieldLabel;

	private CardLayout cardLayout;

	private JPanel contentPanel;

	private JFileChooser dicomdirFileChooser;

	public DICOMDIRSearchPanel searchPanel;

	public DicomFileDialog parent;

	private File file;

	private DicomDirWatcher ddw;

	private int updateInterval;

	private static final String FILE_VIEW = "file";

	private static final String DICOMDIR_VIEW = "dicomdir";

	public static final int NO_UPDATE = -1;

	/**
	 * @param file
	 *            default file
	 * @param parent
	 *            the Parent DicomFileDialog
	 * @param send
	 *            show send option
	 */
	public DICOMDIRPanel(File file, DicomFileDialog parent, int updateInterval, boolean send, boolean delete) {

		this.parent = parent;
		this.file = file;
		this.updateInterval = updateInterval;
		cardLayout = new CardLayout();
		this.setLayout(cardLayout);

		CellConstraints cc = new CellConstraints();
		FormLayout fl = new FormLayout("6dlu, 200dlu:grow, 4dlu",
				"6dlu, pref, 4dlu, fill:100dlu:grow");
		contentPanel = new JPanel(fl);

		JPanel buttonPanel = new JPanel(new FormLayout(
				"4dlu, pref, 4dlu, 200dlu:grow, 4dlu, pref, 4dlu, pref, 4dlu",
				"2dlu, 20dlu, 2dlu"));
		buttonPanel
				.setBorder(new LineBorder(new JTabbedPane().getBackground()));

		fileFieldLabel = new JLabel(Translatrix
				.getTranslationString("dicom.ChooseFile"));
		fileField = new JTextField(file.getAbsolutePath());
		chooseFileButton = new JButton("...");
		chooseFileButton.addActionListener(this);
		chooseFileButton.setToolTipText("Load DICOMDIR");
		reloadButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_RELOAD));
		reloadButton.setToolTipText("Refresh DICOMDIR");
		reloadButton.addActionListener(this);

		buttonPanel.add(fileFieldLabel, cc.xy(2, 2));
		buttonPanel.add(fileField, cc.xy(4, 2));
		buttonPanel.add(chooseFileButton, cc.xy(6, 2));
		buttonPanel.add(reloadButton, cc.xy(8, 2));

		contentPanel.add(buttonPanel, cc.xy(2, 2));

		searchPanel = new DICOMDIRSearchPanel(file, this, true, delete, send, false, parent.dicomFields);
		
		contentPanel.add(searchPanel, cc.xyw(1, 4, 3));

		this.add(contentPanel, DICOMDIR_VIEW);

		dicomdirFileChooser = new JFileChooser(file);
		dicomdirFileChooser.addChoosableFileFilter(new DICOMDIRFileFilter());
		dicomdirFileChooser.addActionListener(this);
		this.add(dicomdirFileChooser, FILE_VIEW);

		// not that good, because of cdrom access all the time
		if (updateInterval > 0)
			ddw = new DicomDirWatcher(file, updateInterval, this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.chooseFileButton)) {
			cardLayout.show(this, FILE_VIEW);
		} else if (e.getSource().equals(this.reloadButton)) {
			// search for cdrom drive
			if (updateInterval < 0 && !file.getName().toUpperCase().endsWith("DICOMDIR") && !file.canRead()) {
				try {
					System.out.println("find cdrom");
					String os = System.getProperty("os.name").toLowerCase();
					File[] drives = null;
					
					// list Filesystem roots
					if (os.startsWith("win")) {
						drives = ShellFolder.listRoots();
					} else if (os.startsWith("mac")) {
					    	drives = new File("/Volumes").listFiles();
					}else {
						drives = new File("/media").listFiles();
					}
					
					// search for dicomdir in system roots
					for (int i = 0; i < drives.length; i++) {
						File dicomDir = new File(drives[i] + File.separator + "DICOMDIR");
						if (dicomDir.canRead()) {
							System.out.println("DICOM CD found at: " + dicomDir);
							file = dicomDir;
							break;
						} 
						
						dicomDir = new File(drives[i] + File.separator + "dicomdir");
						if (dicomDir.canRead()) {
							System.out.println("DICOM CD found at: " + dicomDir);
							file = dicomDir;
							break;
						}
					}				
				} catch (Exception ee) {
					ee.printStackTrace();
				}
		    }
			this.setPath(file);
			// TODO reset blink
		} else if (e.getSource().equals(dicomdirFileChooser)) {
			if (e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
				file = this.dicomdirFileChooser.getSelectedFile();
				this.setPath(file);
				try {
					ddw.stop();
				} catch (Exception ee) {
				}
//				 not that good, because of cdrom access all the time
				if (updateInterval > 0)
					ddw = new DicomDirWatcher(file, updateInterval, this);
			}
			cardLayout.show(this, DICOMDIR_VIEW);
		} 
	}

	/**
	 * sets the path of the Panel
	 * @param f the Path
	 */
	public void setPath(File f) {
		searchPanel.setPath(f);
		fileField.setText(f.getAbsolutePath());
	}


	public DicomFileDialog getParentDialog() {
		return this.parent;
	}

	public void setDicomSenders(DcmURL[] senders) {
		this.searchPanel.setDicomSenders(senders);
	}

	public void dicomdirChanged(DicomEvent d_Event) {
		log.info("DICOMDIR changed: " + d_Event.getDicomDir());
		this.setPath(file);
	}

	public void setDicomFilterTags(Vector headerTags) {
		searchPanel.setDicomFilterTags(headerTags);		
	}
	
}
