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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.RootPaneContainer;

import lu.tudor.santec.dicom.gui.dicomdir.DICOMDIRPanel;
import lu.tudor.santec.dicom.gui.dicomdir.PACSPanel;
import lu.tudor.santec.dicom.gui.filechooser.FILEPanelDicom;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.query.QueryPanel;
import lu.tudor.santec.dicom.gui.selector.DicomFile;
import lu.tudor.santec.dicom.gui.selector.DicomMatcher;
import lu.tudor.santec.dicom.gui.selector.SelectorPanel;
import lu.tudor.santec.dicom.receiver.DicomDirReader;
import lu.tudor.santec.dicom.sender.DicomSender;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingEvent;
import lu.tudor.santec.settings.SettingListener;
import lu.tudor.santec.settings.SettingsPanel;

import org.dcm4che.util.DcmURL;

import com.l2fprod.common.swing.JButtonBar;

/**
 * a Dialog to open DICOM files from the local filesystem, a DICOMDIR file or an integrated DICOM RECIEVER Node. 
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomFileDialog extends JDialog implements SettingListener {
	
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomFileDialog.class
			.getName());
	
	private JPanel dicomFilePanel;
	
	private static DicomFileDialog staticDialog;

	private static final long serialVersionUID = 1L;

	private JButtonBar bb;

	private JToggleButton fileButton;

	private JToggleButton pacsButton;

	private JToggleButton dicomcdButton;

	private JPanel contentPanel;

	private CardLayout cardLayout = new CardLayout();

	private DICOMDIRPanel dicomCDPanel;

	private FILEPanelDicom dicomFileChooser;

	public PACSPanel pacsPanel;

	private File[] selectedFiles;
	
	private File selectedFile;

	private int retValue = JFileChooser.CANCEL_OPTION;
	
	public boolean singleFileSelected = false;

	protected DICOMSettingsPlugin dicomSettingsPlugin;

	private ButtonGroup group;

	private String error;
	
	private static final String FILE_VIEW = "file";

	private static final String PACS_VIEW = "pacs";

	private static final String DICOMCD_VIEW = "dicomcd";
	
	private static final String DICOMDIR_VIEW = "dicomdir";
	
	private static final String QUERY_VIEW = "query";

	private static final String SELECTOR_VIEW = "selector";

	private static final String FILTER_TAGS = "filtertags.properties";
	
	public JButton toolBarButton = new JButton(DicomIcons.getIcon32(DicomIcons.PACS));
	
	public  String[] dicomFields = {""};

	private JToggleButton queryButton;

	private QueryPanel dicomQueryPanel;

	private DicomDirReader dicomDirReader;

	public JDialog dialog;

	private RootPaneContainer owner;	
	
	private DcmURL[] senders;

	private JToggleButton dicomdirButton;

	private DICOMDIRPanel dicomdirPanel;
	
	private Vector dicomdirs = new Vector();

	private JToggleButton selectorButton;

	private SelectorPanel dicomSelectorPanel;

	private String[] defaultFields = {
			    "0008,0060",
			    "0008,1010",
			    "0020,0013",
			    "0018,0050",
			    "0018,0060",
			    "0008,0021",
			    "0008,0031"
		    };
	
	
	
	/**
	 * @param owner the Owner Window
	 * @param settingsPanel a reference to the SettingsPanel
	 * @param dsp a reference to a DICOMSettingsPlugin
	 */
	public DicomFileDialog(JFrame owner, SettingsPanel settingsPanel,
			DICOMSettingsPlugin dsp) {
		super(owner);
		this.owner = owner;
		dialog = this;
		createDicomFilePanel(settingsPanel, dsp);
		this.getContentPane().add(this.dicomFilePanel, BorderLayout.CENTER);
	}
	
	/**
	 * @param owner the Owner Window
	 * @param settingsPanel a reference to the SettingsPanel
	 * @param dsp a reference to a DICOMSettingsPlugin
	 */
	public DicomFileDialog(JDialog owner, SettingsPanel settingsPanel,
			DICOMSettingsPlugin dsp) {
		super(owner);
		this.owner = owner;
		dialog = this;
		createDicomFilePanel(settingsPanel, dsp);
		this.getContentPane().add(this.dicomFilePanel, BorderLayout.CENTER);
	}
	
	/**
	 * @param owner the Owner Window
	 * @param settingsPanel a reference to the SettingsPanel
	 * @param dsp a reference to a DICOMSettingsPlugin
	 */
	public DicomFileDialog(SettingsPanel settingsPanel,
			DICOMSettingsPlugin dsp) {
		dialog = this;
		createDicomFilePanel(settingsPanel, dsp);
		this.getContentPane().add(this.dicomFilePanel, BorderLayout.CENTER);
	}
	
	
	private void createDicomFilePanel(SettingsPanel sp,
			DICOMSettingsPlugin dsp) {
	    	initTranslatrix();
		sp.addSettingListener(this);
		
		org.apache.log4j.Logger.getLogger("loci").setLevel(org.apache.log4j.Level.WARN);
				
		this.dicomSettingsPlugin = dsp;
		this.dicomFields = ((String)this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOM_FIELDS)).split("\n");
		if (dicomFields.length == 0) {
		    dicomFields = defaultFields;
		} else {
			for (int i = 0; i < dicomFields.length; i++) {
			    dicomFields[i] = dicomFields[i].trim();
			}
		}
		
		loadDicomSenders();
		
		dicomFilePanel = new JPanel();
		dicomFilePanel.setLayout(new BorderLayout());
		bb = new JButtonBar(JButtonBar.VERTICAL);
		bb.setPreferredSize(new Dimension(100,40));
		group = new ButtonGroup();
		
		ImagePreviewDicom.showPreview = 
			((Boolean) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOM_SHOWIMAGEPREVIEW)).booleanValue();
		
		contentPanel = new JPanel();
		contentPanel.setLayout(cardLayout);
		dicomFilePanel.add(contentPanel, BorderLayout.CENTER);

		dicomdirs.removeAllElements();
		
		// add file Button
		this.showFilePanel();

//		// add pacs button
		this.showPacsPanel();
		
//		//	add query button
		this.showQueryPanel();
		
//		// add dicomcd button
		this.showDicomCDPanel();
		
		// add dicomdir button
		this.showDICOMDIRPanel();
		
//		// add dicomdir button
		this.showSelectorPanel();

		dicomFilePanel.add(bb, BorderLayout.WEST);

		try {
			((JToggleButton)group.getElements().nextElement()).doClick();			
		} catch (Exception e) {
		}
	}
	
	
	public void loadDicomSenders() {
		try {
			String[] dicomSenders = ((String)this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.SENDER_URLS)).split("\n");
			this.senders = new DcmURL[dicomSenders.length];
			for (int i = 0; i < dicomSenders.length; i++) {
				senders[i] = new DcmURL(dicomSenders[i]);
			}			
		} catch (Exception e) {
			logger.info("error loading dicom senders, check settings");
		}
	}

	/**
	 * creates an Action to show the tabs of the MainPanel
	 * 
	 * @param text
	 *            Text to be shown on the Button
	 * @param icon
	 *            Icon to be shown on the Button
	 * @param action
	 *            the name of the Tab to be shown
	 * @return the Action
	 */
	public AbstractAction createTabAction(String text, Icon icon, String action) {
		class TabAction extends AbstractAction {
			private static final long serialVersionUID = 1L;

			public TabAction(String text, Icon icon, String action) {
				super(text, icon);
				putValue(ACTION_COMMAND_KEY, action);
			}
			public void actionPerformed(ActionEvent e) {
				cardLayout.show(contentPanel, e.getActionCommand());
				setWaitCursor(false);
			}
		}
		return new TabAction(text, icon, action);
	}

	/**
	 * returns the selected Files
	 * @return
	 */
	public File[] getSelectedFiles() {
		return selectedFiles;
	}

	/**
	 * returns the selected File
	 * @return
	 */
	public File getSelectedFile() {
		return this.selectedFile;
	}
	

	/**
	 * @return
	 */
	public boolean getSingleFileSelected() {
		return this.singleFileSelected;
	}
	
	/**
	 * @param singleFileSelected
	 */
	public void setSingleFileSelected(boolean singleFileSelected) {
		this.singleFileSelected = singleFileSelected;
	}

	/**
	 * @param selectedFiles
	 */
	public void setSelectedFiles(File[] selectedFiles) {
		this.selectedFiles = selectedFiles;
	}
	
	/**
	 * @param selectedFiles
	 */
	public void setSelectedFile(File selectedFile) {
		this.selectedFile = selectedFile;
		if (this.dicomFileChooser != null)
			this.dicomFileChooser.setFile(selectedFile);
	}

	/**
	 * @param parent
	 * @param dialog
	 */
	private void center(Component parent, JDialog dialog) {
		dialog.setLocationRelativeTo(parent);
	}
	
	/**
	 * returns the DicomFileDialog as a JPanel to integrate in the application
	 * @return the DicomFileDialog as a JPanel to integrate
	 */
	public JPanel getAsPanel() {
		return this.dicomFilePanel;
	}

	public int showDialog() {
		return showNewDialog(null, null);
	}

	public int showDialog(Vector headerTags) {
		return showNewDialog(null, headerTags);
	}
	
	public int showNewDialog(RootPaneContainer parent) {
		return showNewDialog(parent, null);
	}
	
	/**
	 * @return
	 */
	public int showNewDialog(RootPaneContainer parent, Vector headerTags) {
		
		if (dicomFileChooser != null) {
			dicomFileChooser.setDicomFilterTags(headerTags);
		}
		if (pacsPanel != null) {
			pacsPanel.setDicomFilterTags(headerTags);
		} 
		if (dicomCDPanel != null) {
			dicomCDPanel.setDicomFilterTags(headerTags);
		}
		if (dicomdirPanel != null) {
			dicomdirPanel.setDicomFilterTags(headerTags);
		}
		
		JDialog jd = null;
		if (parent instanceof JDialog)
			 jd = new JDialog((JDialog)parent);
		else if (parent instanceof JFrame)
			jd = new JDialog((JFrame)parent);
		else if(owner instanceof JDialog) 
			jd = new JDialog((JDialog)owner);
		else if(owner instanceof JFrame) 
			jd = new JDialog((JFrame)owner);
		else 
			jd = new JDialog();
		jd.setModal(true);
		dialog = jd;
		jd.getContentPane().setLayout(new BorderLayout());
		jd.getContentPane().add(this.dicomFilePanel, BorderLayout.CENTER);
		this.setWaitCursor(false);
		if (this.pacsPanel != null) {
			this.pacsPanel.setServerStatus(PACSPanel.SERVER_RESET);
		}
		jd.validate();
		jd.setSize(850, 600);
		Component jc = (Component) owner;
		if (jc == null)
			try {
				if (parent instanceof JDialog)
					jc = (Component) parent;	
				else if (parent instanceof JFrame)
					jc = (Component) parent;	
			} catch (Exception e) {}
		this.center(jc, jd);
		jd.setVisible(true);
		return retValue;
	}	
	
//	/**
//	 * @return
//	 */
//	public int showNewDialog(JFrame parent) {
//		JDialog jd = new JDialog(parent);
//		jd.setModal(true);
//		dialog = jd;
//		jd.getContentPane().setLayout(new BorderLayout());
//		jd.getContentPane().add(this.dicomFilePanel, BorderLayout.CENTER);
//		this.setWaitCursor(false);
//		if (this.pacsPanel != null) {
//			this.pacsPanel.setServerStatus(PACSPanel.SERVER_RESET);
//		}
//		jd.validate();
//		jd.setSize(800, 600);
//		JComponent jc = null;
//		try {
//			jc = (JComponent) parent.getComponent(0);		
//		} catch (Exception e) {
//		}
//		this.center(jc, jd);
//		jd.setVisible(true);
//		return retValue;
//	}

	/**
	 * @param retValue
	 */
	public void setRetValue(int retValue) {
		this.retValue = retValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingListener#settingChanged(lu.tudor.santec.settings.SettingEvent)
	 */
	public void settingChanged(SettingEvent p_Event) {
		if (p_Event.getSource().equals(this.dicomSettingsPlugin)) {
			try {
				this.dicomFields = ((String)this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOM_FIELDS)).split("\n");
			} catch (Exception e) {
				logger.info("unable to set dicomFields to: "+this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOM_FIELDS));
			}
			if (dicomFields.length == 0) {
			    dicomFields = defaultFields;
			} else {
				for (int i = 0; i < dicomFields.length; i++) {
				    dicomFields[i] = dicomFields[i].trim();
				}
			}
			
			loadDicomSenders();
			
			dicomdirs.removeAllElements();
			
			// add file Button
			this.showFilePanel();
			// add pacs button
			this.showPacsPanel();
			// add query button
			this.showQueryPanel();
			// add dicomcd button
			this.showDicomCDPanel();
			// add dicomdir button
			this.showDICOMDIRPanel();
			// add dicomdir button
			this.showSelectorPanel();
			
			try {
				((JButton)group.getElements().nextElement()).doClick();			
			} catch (Exception e) {
			}
		}
	}

	public JButton getToolBarButton() {
		return this.toolBarButton;
	}
	
	
	@SuppressWarnings("serial")
	public Action getToolBarAction() {
		return new AbstractAction("",DicomIcons.getIcon32(DicomIcons.PACS)) {
		    public void actionPerformed(ActionEvent e) {
			showDialog();
		    }
		};
	}

	private void showFilePanel() {
		boolean enabled = false;
		try {
			enabled = ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.FILE_ENABLED))
				.booleanValue();
		} catch (Exception e) {}
		try {
			bb.remove(fileButton);
			group.remove(fileButton);
			contentPanel.remove(dicomFileChooser);
			dicomFileChooser = null;
		} catch (Exception e) {
		}
		// add file Button
		if (enabled) {
			fileButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.File"), DicomIcons.getIcon(DicomIcons.OPEN_FILE),
					FILE_VIEW));
			bb.add(fileButton);
			group.add(fileButton);
			dicomFileChooser = new FILEPanelDicom(new File(
					(String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.FILE_NAME)),
					this, ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.SENDER_ENABLED))
					.booleanValue());
			dicomFileChooser.setDicomSenders(senders);
			contentPanel.add(dicomFileChooser, FILE_VIEW);
		}
	}
	
	private void showQueryPanel() {
		boolean enabled = false;
		try {
			enabled = ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.QUERY_ENABLED))
				.booleanValue();
		} catch (Exception e) {}
		try {
			bb.remove(queryButton);
			group.remove(queryButton);
			contentPanel.remove(dicomQueryPanel);
			dicomFileChooser = null;
		} catch (Exception e) {
		}
		// add file Button
		if (enabled) {
			queryButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.Query"), DicomIcons.getIcon(DicomIcons.OPEN_QUERY),
					QUERY_VIEW));
			bb.add(queryButton);
			group.add(queryButton);
			
			String url = "dicom://" + (String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.QUERY_TO_AET) + ":"
				+ (String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_AET) + "@" 
				+	(String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.QUERY_TO_ADDRESS) + ":" 
				+	(String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.QUERY_TO_PORT);
	    	
			DcmURL dcmUrl = new DcmURL(url);
			
			dicomQueryPanel = new QueryPanel(dcmUrl, this);
			String localDest = "dicom://" + this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_AET);
			localDest = localDest  + "@" +"localhost" + ":"+ this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_PORT); 
			DcmURL localDestUrl = new DcmURL(localDest);
			dicomQueryPanel.setLocalDest(localDestUrl);
			dicomQueryPanel.setDicomSenders(senders);
			dicomQueryPanel.setSearchString((String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.QUERY_STRING));
			contentPanel.add(dicomQueryPanel, QUERY_VIEW);
		}
	}
	
	private void showPacsPanel() {
		boolean enabled = false;
		try {
			enabled = ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_ENABLED))
				.booleanValue();
		} catch (Exception e) {}
			
		// add file Button
		try {
			bb.remove(pacsButton);
			group.remove(pacsButton);
			contentPanel.remove(pacsPanel);
			pacsPanel.stopDicomServer();
			pacsPanel = null;
		} catch (Exception e) {
		}
		if (enabled) {
			
			dicomdirs.add((String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_DIR));

			pacsPanel = new PACSPanel(
					new File((String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_DIR)), 
					this, 
					2000
			);
//			pacsPanel.setPath(new File((String) this.dicomSettingsPlugin
//					.getValue(DICOMSettingsPlugin.REC_DIR)));
			pacsButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.Pacs"), DicomIcons.getIcon(DicomIcons.OPEN_DICOM_STORE),
					PACS_VIEW));
			bb.add(pacsButton);
			group.add(pacsButton);
			contentPanel.add(pacsPanel, PACS_VIEW);
			
			
			pacsPanel.setDicomSenders(senders);
			try {
				pacsPanel.startDicomServer((String) this.dicomSettingsPlugin
						.getValue(DICOMSettingsPlugin.REC_AET), new Integer((String)this.dicomSettingsPlugin
						.getValue(DICOMSettingsPlugin.REC_PORT)));	
			} catch (Exception e) {
				logger.warning(e.getLocalizedMessage());
			}
		}
	}
	
	private void showDicomCDPanel() {
		boolean enabled = false;
		try {
			enabled =  ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOMCD_ENABLED))
				.booleanValue();
		} catch (Exception e) {}
		
		try {
			bb.remove(dicomcdButton);
			group.remove(dicomcdButton);
			contentPanel.remove(dicomCDPanel);
			dicomCDPanel = null;
		} catch (Exception e) {
		}
		// add dicomdir Button
		if (enabled) {
			dicomcdButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.DicomCD"),
					DicomIcons.getIcon(DicomIcons.OPEN_DICOMCD),
					DICOMCD_VIEW));
			bb.add(dicomcdButton);
			group.add(dicomcdButton);
			dicomCDPanel = new DICOMDIRPanel(
					new File((String) this.dicomSettingsPlugin
							.getValue(DICOMSettingsPlugin.DICOMCD_NAME)), 
							this, 
							DICOMDIRPanel.NO_UPDATE, 
							((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_ENABLED)).booleanValue(),
							false);
			dicomCDPanel.setDicomSenders(senders);
			contentPanel.add(dicomCDPanel, DICOMCD_VIEW);
		}
	}
	
	private void showDICOMDIRPanel() {
		boolean enabled = false;
		try {
			enabled = ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOMDIR_ENABLED))
				.booleanValue();
		} catch (Exception e) {}
		
		try {
			bb.remove(dicomdirButton);
			group.remove(dicomdirButton);
			contentPanel.remove(dicomdirPanel);
			dicomdirPanel = null;
		} catch (Exception e) {
		}
		// add dicomdir Button
		if (enabled) {
			
			dicomdirs.add((String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOMDIR_NAME));

			dicomdirButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.Dicomdir"),
					DicomIcons.getIcon(DicomIcons.OPEN_REMOTE_DICOMDIR),
					DICOMDIR_VIEW));
			bb.add(dicomdirButton);
			group.add(dicomdirButton);
			dicomdirPanel = new DICOMDIRPanel(
					new File((String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.DICOMDIR_NAME)), 
					this, 
					2000, 
					false,
					true);
			dicomdirPanel.setDicomSenders(senders);
			contentPanel.add(dicomdirPanel, DICOMDIR_VIEW);
		}
	}

	private void showSelectorPanel() {
		boolean enabled = false;
		try {
			enabled = ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.SELECTOR_ENABLED))
		.booleanValue();
		} catch (Exception e) {}
		
		try {
			bb.remove(selectorButton);
			group.remove(selectorButton);
			contentPanel.remove(dicomSelectorPanel);
			dicomSelectorPanel = null;
		} catch (Exception e) {
		}
		// add file Button
		if (enabled) {
			selectorButton = new JToggleButton(createTabAction(Translatrix
					.getTranslationString("dicom.Selector"), DicomIcons.getIcon(DicomIcons.OPEN_SELECTOR),
					SELECTOR_VIEW));
			bb.add(selectorButton);
			group.add(selectorButton);
			dicomSelectorPanel = new SelectorPanel(new File(
					(String) this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.SELECTOR_DIR)),
					this, ((Boolean) dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_ENABLED)).booleanValue());
			dicomSelectorPanel.setDicomSenders(senders);
			String localDest = "dicom://" + this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_AET);
			localDest = localDest  + "@" +"localhost" + ":"+ this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_PORT); 
			DcmURL localDestUrl = new DcmURL(localDest);
			dicomSelectorPanel.setLocalDest(localDestUrl);
			
			Properties tags = new Properties();
			try {
				tags.load(new FileInputStream(FILTER_TAGS));
			} catch (FileNotFoundException e) {
			    System.err.println("Filtertag settings not found at: " +FILTER_TAGS+ " ...creating next time...");
			} catch (Exception e) {
			    System.out.println(e.getLocalizedMessage());
			} 
			Vector headerTags = new Vector();
			SortedSet set = new TreeSet(tags.keySet());
			for (Iterator iter = set.iterator(); iter.hasNext();) {
				String tagNr = (String) iter.next();
				headerTags.add(new HeaderTag(tagNr, null, (String)tags.get(tagNr)));
			}
			dicomSelectorPanel.setDicomFilterTags(HeaderTag.loadTags(FILTER_TAGS));
			
			contentPanel.add(dicomSelectorPanel, SELECTOR_VIEW);
			
			Runtime.getRuntime().addShutdownHook( new Thread() { 
				  public void run() { 
					  HeaderTag.saveTags(FILTER_TAGS, dicomSelectorPanel.getDicomFilterTags());
				  } 
				} ); 
		}
		
	}
	
	protected DicomSender createLocalDicomSender() {
		String url = "dicom://" + this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_AET);
		url = url  + "@" +"localhost" + ":"+
		this.dicomSettingsPlugin.getValue(DICOMSettingsPlugin.REC_PORT); 
		logger.info("created new Local Dicomsender: "+url);
		DcmURL dcmUrl = new DcmURL(url);
		return new DicomSender(dcmUrl);
	}
	
    /**
     * sets the Mousecursor of the MainFrame to a WaitCursor and Back
     *
     * @param on true=waitcursor false=normalcursor
     */
    public void setWaitCursor(boolean on)
    {
        if (on) {
            dialog.getGlassPane().setVisible(true); 
            dialog.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
        } else {
        	dialog.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        	dialog.getGlassPane().setVisible(false); 
        }
    }

	public String getError() {
		return error;
	}
	
	
	/**
	 * returns the path to the image by a string of SeriesInstanceUID#InstanceNumber of the image
	 * @param imageUrl ( SeriesInstanceUID#InstanceNumber)
	 * @return the image as File
	 */
	public File loadFileByImageURL(String imageUrl) throws Exception {
		
		for (Iterator iter = dicomdirs.iterator(); iter.hasNext();) {
			String dicomdir = (String) iter.next();
			
			logger.info("trying to load image from: " + dicomdir);
			
			try {
				
				dicomDirReader = new DicomDirReader();
				dicomDirReader.loadDicomDirFile(new File(dicomdir),false, false); 
	
				if (imageUrl.indexOf("#") == -1) {
					File image = dicomDirReader.getImageFromSOPInstanceUID(imageUrl);
					if (image != null) {
						logger.info("image loaded from: " + dicomdir);
						return image;
					}
				} 
			
				try {
					String[] parts = imageUrl.split("#");
					DicomMatcher dm = new DicomMatcher(null);
					
					HeaderTag[] htArr = null;
					if (parts.length==1){
						htArr = new HeaderTag[]{
								new HeaderTag("0008,0018", null, parts[0])
						};
					} else {
						htArr = new HeaderTag[]{
								new HeaderTag("0020,000E", null, parts[0]),
								new HeaderTag("0020,0013", null, parts[1])
						};
					}
					
					dm.setHeaderTags(htArr);
					Vector v = dm.findMachingFiles(new File(dicomdir).getParentFile());
					DicomFile file = (DicomFile) v.get(0);
					return file.getFile();
					
//					return dicomDirReader.getImageFromSeriesUID(parts[0],Integer.parseInt(parts[1]));			
				} catch (Exception e) {
					e.printStackTrace();
				}	
			} catch (Exception e) {
				logger.warning("dicomdir: " + dicomdir + " does not exist");
			}
		}
		logger.warning("image not found");
		return null;
	}
	
	/**
	 * Saves the Image to the local PACS
	 * @param file
	 * @return a string of SOPInstanceUID of the image
	 * @throws Exception
	 */
	public String saveFiletoLocalPacs(File file) throws Exception {
		try {
			this.createLocalDicomSender().send(file);			
		} catch (Exception e) {
			logger.warning("unable to save image to: " + this.createLocalDicomSender().getUrl());
		}
		DicomHeader dh =  new DicomHeader(file);
		String SOPInstanceUID = dh.getHeaderStringValue("0002,0003");
		if (SOPInstanceUID != "") 
			return SOPInstanceUID;
		else
			return dh.getHeaderStringValue("0020,000E") + "#" + dh.getHeaderStringValue("0020,0013");
	}
	
	public static DicomFileDialog getDicomFileDialog() {
		if (staticDialog == null ) {
		    	initTranslatrix();
			staticDialog = new DicomFileDialog(DicomSettings.getSettingPanel(),DicomSettings.getDicomSettingsPlugin());
		}
		
		return staticDialog;
	}
	
	public static DicomFileDialog getDicomFileDialog(JDialog parent) {
		if (staticDialog == null ) {
		    	initTranslatrix();
			staticDialog = new DicomFileDialog(parent, DicomSettings.getSettingPanel(),DicomSettings.getDicomSettingsPlugin());
		}
		return staticDialog;
	}
	
	public static DicomFileDialog getDicomFileDialog(JFrame parent) {
		if (staticDialog == null ) {
			initTranslatrix();
			staticDialog = new DicomFileDialog(parent, DicomSettings.getSettingPanel(),DicomSettings.getDicomSettingsPlugin());
		}
		return staticDialog;
	}
	
	/**
	 * opens the first found file in the dicomdir
	 */
	public File[] getNextImagefromDicomDir(boolean series) throws Exception {
		dicomDirReader = new DicomDirReader();
		dicomDirReader.loadDicomDirFile(new File((String) this.dicomSettingsPlugin.getValue("RecDir")),false, false); 
		return dicomDirReader.getNextImage(series);
	}
	
	
	/**
	 * returns the path to the image by a string of SeriesInstanceUID#InstanceNumber of the image
	 * @param imageUrl ( SeriesInstanceUID#InstanceNumber)
	 * @return the image as File
	 */
	public void deleteFileByImageURL(String imageUrl) throws Exception {
		dicomDirReader = new DicomDirReader();
		dicomDirReader.loadDicomDirFile(new File((String) this.dicomSettingsPlugin.getValue("RecDir")),false, false); 
		String[] parts = imageUrl.split("#");
		System.out.println("deleting Image: "+ imageUrl);
		dicomDirReader.deleteImageFromSeriesUID(parts[0],Integer.parseInt(parts[1]));
	}
	
    public void setVisible(boolean b) {
    	if (b)
    		dialog.setVisible(b);
    	else {
    		dialog.setVisible(false);
    		dialog.dispose();
    	}
    }
    
    public JDialog getCurrentDialog() {
    	return this.dialog;
    }
    
    
    public static void main(String[] args) {
    	JFrame parent = new JFrame();

    	DicomFileDialog dfd = new DicomFileDialog(parent, DicomSettings.getSettingPanel(), DicomSettings.getDicomSettingsPlugin());
    	
    	dfd.showDialog();
    }
		
    private static void initTranslatrix() {
	try {
	    Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
	    Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
	    Translatrix.addBundle(SwingLocalizer.getBundle());
	    Translatrix.setDefaultWhenMissing(true);
	    SwingLocalizer.localizeJFileChooser();
	    SwingLocalizer.localizeJOptionPane();		    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
}
