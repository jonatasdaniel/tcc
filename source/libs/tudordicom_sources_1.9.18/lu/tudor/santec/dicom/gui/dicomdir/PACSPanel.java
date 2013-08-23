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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.LineBorder;

import lu.tudor.santec.dicom.gui.DICOMSettingsPlugin;
import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomFilter;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.receiver.DicomDirWatcher;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import lu.tudor.santec.dicom.receiver.DicomStorageServer;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class PACSPanel extends JPanel implements DICOMDIRVIEW, DicomFilter{

	private static final long serialVersionUID = 1L;
	
	private final static Logger log = Logger.getLogger("PACSPanel");

	private JLabel pacsStatus;

	private JLabel pacsStatusIcon;

	private JLabel pacsStatusLabel;

	private DICOMDIRSearchPanel pacsPanel;

	private DicomStorageServer dicomStorageServer;

	private File file;

	private DicomFileDialog parent;

	public static final int SERVER_ON = 0;

	public static final int SERVER_OFF = 1;

	public static final int SERVER_UPDATE = 2;

	public static final int SERVER_RESET = 3;
	
	private JButton reloadButton;

	private Integer port;

	private String AET;

	public PACSPanel(File file, DicomFileDialog parent, int updateInterval) {
		this.file = file;
		this.parent = parent;
//		dicomStorageServer = new DicomStorageServer("", new Integer(5104), file
//				.getAbsolutePath(), false, "password");
		CellConstraints cc = new CellConstraints();
		FormLayout fl = new FormLayout("6dlu, fill:200dlu:grow, 4dlu",
				"6dlu, fill:pref, 4dlu, fill:100dlu:grow");
		this.setLayout(fl);
		pacsStatusLabel = new JLabel(Translatrix
				.getTranslationString("dicom.PacsServerStatus"));
		pacsStatus = new JLabel();
		pacsStatusIcon = new JLabel();
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new FormLayout(
				"4dlu, pref, 4dlu, pref:grow, 4dlu, pref, 4dlu, pref, 4dlu",
				"2dlu, fill:20dlu, 2dlu"));
		statusPanel
		.setBorder(new LineBorder(new JTabbedPane().getBackground()));
		reloadButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_RELOAD));
		reloadButton.setToolTipText(Translatrix.getTranslationString("refresh.DICOMDIR"));
		reloadButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
					reload();
			}
		});
		statusPanel.add(pacsStatusLabel, cc.xy(2, 2));
		statusPanel.add(pacsStatus, cc.xy(4, 2));
		statusPanel.add(reloadButton, cc.xy(6, 2));
		statusPanel.add(pacsStatusIcon, cc.xy(8, 2));
		this.add(statusPanel, cc.xyw(2, 2, 1));
		pacsPanel = new DICOMDIRSearchPanel(file, this, true, true, true, true, parent.dicomFields);
		this.add(pacsPanel, cc.xyw(1, 4, 3));
		this.parent.toolBarButton.setToolTipText(Translatrix
				.getTranslationString("dicom.ShowOpenDialog"));
		this.setServerStatus(SERVER_OFF);
		
		new DicomDirWatcher(file, updateInterval, this);
	}

	public void setPath(File f) {
		file = f;
		pacsPanel.setPath(file);
	}
	
	public void reload() {
		setPath(file);
		this.setServerStatus(PACSPanel.SERVER_ON);
	}

	public void setServerStatus(int status) {
		this.parent.toolBarButton.setIcon(DicomIcons.getIcon(DicomIcons.PACS));
		switch (status) {
		case SERVER_RESET:
			
			if (this.dicomStorageServer == null || !this.dicomStorageServer.isRunning()) {
				this.setServerStatus(SERVER_OFF);
			} else {
				this.setServerStatus(SERVER_ON);
			}
			break;
		case SERVER_ON:
			this.pacsStatus.setText(Translatrix
					.getTranslationString("dicom.PacsServerIsRunning") + "  " +AET + "@" + DICOMSettingsPlugin.getIP() + ":" + port );
			this.pacsStatusIcon.setIcon(DicomIcons.getIcon(DicomIcons.PACS_ON));
//			this.parent.toolBarButton.setIcon(Icons.getIcon(Icons.PACS_ON));
			break;
		case SERVER_UPDATE:
			this.pacsStatus.setText(Translatrix
					.getTranslationString("dicom.PacsServerNewFiles") + "  " +AET + "@" + DICOMSettingsPlugin.getIP() + ":" + port );
			this.pacsStatusIcon.setIcon(DicomIcons.getIcon(DicomIcons.PACS_UPDATE));
//			this.parent.toolBarButton.setIcon(Icons.getIcon(Icons.PACS_UPDATE));
			break;
		default:
			this.pacsStatus.setText(Translatrix
					.getTranslationString("dicom.PacsServerIsOffline"));
			this.pacsStatusIcon.setIcon(DicomIcons.getIcon(DicomIcons.PACS_OFF));
//			this.parent.toolBarButton.setIcon(Icons.getIcon(Icons.PACS_OFF));
			break;
		}
	}

	public void startDicomServer(String AET, Integer port) throws Exception {
	    if (dicomStorageServer != null) {
		try {
		    dicomStorageServer.stop();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    this.AET = AET;
	    this.port = port;
		dicomStorageServer = new DicomStorageServer(AET, port, file
				.getAbsolutePath(),false, "password", false);
		dicomStorageServer.start();
		setServerStatus(SERVER_ON);
	}

	public void stopDicomServer() {
		try {
		    dicomStorageServer.stop();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		setServerStatus(SERVER_OFF);
	}
	
	public DicomFileDialog getParentDialog() {
		return parent;
	}

	public void dicomdirChanged(DicomEvent d_Event) {
			log.info("----DICOM STORE changed: " + d_Event.getDicomDir());
				this.setPath(file);
				this.setServerStatus(PACSPanel.SERVER_UPDATE);
	}

	public void setDicomSenders(DcmURL[] senders) {
		if (senders != null) 
			this.pacsPanel.setDicomSenders(senders);
	}

	public void setDicomFilterTags(Vector headerTags) {
		pacsPanel.setDicomFilterTags(headerTags);
	}

}
