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
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import lu.tudor.santec.dicom.gui.dicomdir.DICOMDIRFileFilter;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.header.TagChooserDialog;
import lu.tudor.santec.dicom.receiver.DicomDirReader;
import lu.tudor.santec.dicom.utils.DCMEcho;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingEvent;
import lu.tudor.santec.settings.SettingsPlugin;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.net.Status;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 */
public class DICOMSettingsPlugin extends SettingsPlugin {

	private static final long serialVersionUID = 1L;

	public static final String FILE_ENABLED = "FileEnabled";

	public static final String FILE_NAME = "FileName";

	public static final String DICOMDIR_ENABLED = "DicomDirEnabled";

	public static final String DICOMDIR_NAME = "DicomDirName";
	
	public static final String DICOMCD_ENABLED = "DicomCDEnabled";

	public static final String DICOMCD_NAME = "DicomCDName";

	public static final String REC_ENABLED = "RecEnabled";

	public static final String REC_DIR = "RecDir";

	public static final String REC_AET = "RecAET";

	public static final String REC_PORT = "RecPort";

	public static final String QUERY_ENABLED = "QueryEnabled";
	
	public static final String QUERY_STRING = "QueryString";

	public static final String QUERY_TO_ADDRESS = "QueryToAddress";

	public static final String QUERY_TO_PORT = "QueryToPort";

	public static final String QUERY_TO_AET = "QueryToAET";

	public static final String SENDER_ENABLED = "SendEnabled";

	public static final String SENDER_URLS = "SenderUrls";
	
	public static final String DICOM_FIELDS = "DicomFields";
	
	public static final String DICOM_SHOWIMAGEPREVIEW = "DicomShowPreview";
	
	public static final String SELECTOR_ENABLED = "SELECTOR_ENABLED";

	public static final String SELECTOR_DIR = "SELECTOR_DIR";

	private JCheckBox fileEnabled;

	private JTextField fileName;

	private JButton chooseFile;

	private JLabel fileNameLabel;

	private JFileChooser fileChooser;

	private JPanel filePanel, dicomRecPanel, dicomSendPanel;

	protected JFileChooser recDirChooser;

	private JButton chooseRecDir;

	private JTextField recDir;

	private JCheckBox recEnabled;

	private JLabel recDirLabel;

	private JLabel recAETLabel;

	private JTextField recAET;

	private JLabel recPortLabel;

	private JCheckBox sendEnabled;

	private JTextField recPort;

	private JCheckBox dicomdirEnabled;

	private JTextField dicomdirName;

	private JButton chooseDicomDirFile;

	protected JFileChooser dicomdirChooser;

	private JPanel dicomdir;

	private JLabel dicomdirLabel;
	
	private JTextArea dicomHeader;
	
	private JLabel dicomHeaderLabel;

	private JPanel dicomHeaderPanel;

	private JPanel dicomQueryPanel;

	private JLabel queryToAddressLabel;

	private JTextField queryToAddress;

	private JLabel queryToPortLabel;

	private JTextField queryToPort;

	private JLabel queryToAETLabel;

	private JTextField queryToAET;

	private JCheckBox queryEnabled;

	private JCheckBox showPreview;

	private JTable senderUrls;

	private DicomURLTableModel senderTableModel;

	private JButton addSenderButton;

	private JButton deleteSenderButton;

	private JPanel dicomCD;

	private JCheckBox dicomCDEnabled;

	private JButton chooseDicomCDFile;

	private JLabel dicomCDLabel;

	private JTextField dicomCDName;

	private JButton jb;

	private JLabel queryStringLabel;

	private JTextField queryString;

	private JPanel selectorPanel;

	private JCheckBox selectorEnabled;

	private JButton selectorChooseFile;

	private JLabel selectorNameLabel;

	private JTextField selectorFileName;

	private boolean simpleMode = false;

	private JLabel recIPLabel;

	private JLabel recAddressLabel;

	private JLabel reindexLabel;

	private JLabel reindexDesc;

	private JButton reindexButton;

	private boolean singleSender;

	private boolean showDicomDir = true;

	private boolean showImageSelector = true;

	private boolean showStore = true;

	private boolean showQuery = true;

	private boolean showSend = true;

	 private static Logger logger = Logger.getLogger("lu.tudor.santec.dicom.gui.DICOMPlugin");
	 
	 private final static ImageIcon FAILED = DicomIcons.getIcon(DicomIcons.STATUS_FAILED);
	 private final static ImageIcon OK = DicomIcons.getIcon(DicomIcons.STATUS_OK);
	 private final static ImageIcon UNKNOWN = DicomIcons.getIcon(DicomIcons.STATUS_UNKNOWN);

	// ***************************************************************************
	// * Constructor *
	// ***************************************************************************

	 

		/**
		 * Creates a new instance of DICOMPlugin
		 */
		public DICOMSettingsPlugin(String name) {
			super(name);
			init();
		}
	 
		/**
		 * Creates a new instance of DICOMPlugin
		 */
		public DICOMSettingsPlugin(String name, boolean simpleMode) {
			super(name);
			this.simpleMode  = simpleMode;
			init();
		}
		
		/**
		 * Creates a new instance of DICOMPlugin
		 */
		public DICOMSettingsPlugin(String name, boolean showDicomDir, boolean showStore, boolean showQuery, boolean showSend, boolean showImageSelector) {
			super(name);
			this.showDicomDir  = showDicomDir;
			this.showStore = showStore;
			this.showQuery = showQuery;
			this.showSend  = showSend;
			this.showImageSelector = showImageSelector;
			init();
		}
	
	private void init() {
	    	BasicConfigurator.configure();
	    	// set loglevel for dcm4che lib to warn (as not rest is not interresting)
	    	org.apache.log4j.Logger.getLogger("org.dcm4che2").setLevel(Level.WARN);
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
		setIcon(DicomIcons.getIcon(DicomIcons.PACS));
		this.setStationaryValues();
		this.buildPanel();
		
		jb.doClick();
		
		relocalize();
	}

	// ***************************************************************************
	// * Class Primitives *
	// ***************************************************************************

	/**
	 * adds the components to the panel
	 */
	private void buildPanel() {
		initComponents();
		CellConstraints cc = new CellConstraints();

		// build the Layout for fileinput
		filePanel = createSubPanel(Translatrix
				.getTranslationString("dicom.FileInput"));
		FormLayout fileLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu");
		filePanel.setLayout(fileLayout);
		JPanel fileChooserPanel = new JPanel(new BorderLayout());
		fileChooserPanel.add(this.fileName, BorderLayout.CENTER);
		fileChooserPanel.add(this.chooseFile, BorderLayout.EAST);
		// add Fields for file
		filePanel.add(this.fileEnabled, cc.xyw(1, 1, 3));
		filePanel.add(this.fileNameLabel, cc.xy(1, 3));
		filePanel.add(fileChooserPanel, cc.xyw(3, 3, 7));
		addSubPanel(filePanel);

		// add Fields for dicomCD
		dicomCD = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomCDInput"));
		FormLayout dicomCDLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu");
		dicomCD.setLayout(dicomCDLayout);
		JPanel dicomCDChooserPanel = new JPanel(new BorderLayout());
		dicomCDChooserPanel.add(this.dicomCDName, BorderLayout.CENTER);
		dicomCDChooserPanel.add(this.chooseDicomCDFile, BorderLayout.EAST);
		dicomCD.add(this.dicomCDEnabled, cc.xyw(1, 1, 3));
		dicomCD.add(this.dicomCDLabel, cc.xy(1, 3));
		dicomCD.add(dicomCDChooserPanel, cc.xyw(3, 3, 7));
		addSubPanel(dicomCD);
		
		// add Fields for dicomdir
		dicomdir = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomDirInput"));
		FormLayout dicomdirLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu");
		dicomdir.setLayout(dicomdirLayout);
		JPanel dicomdirChooserPanel = new JPanel(new BorderLayout());
		dicomdirChooserPanel.add(this.dicomdirName, BorderLayout.CENTER);
		dicomdirChooserPanel.add(this.chooseDicomDirFile, BorderLayout.EAST);
		dicomdir.add(this.dicomdirEnabled, cc.xyw(1, 1, 3));
		dicomdir.add(this.dicomdirLabel, cc.xy(1, 3));
		dicomdir.add(dicomdirChooserPanel, cc.xyw(3, 3, 7));
		if (! simpleMode && showDicomDir)
			addSubPanel(dicomdir);

		// build the Layout for DICOM rec.
		dicomRecPanel = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomRec"));
		FormLayout dicomRecLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
		dicomRecPanel.setLayout(dicomRecLayout);
		JPanel dicomRecChooserPanel = new JPanel(new BorderLayout());
		dicomRecChooserPanel.add(this.recDir, BorderLayout.CENTER);
		dicomRecChooserPanel.add(this.chooseRecDir, BorderLayout.EAST);
		// add Fields for dicom reciever
		dicomRecPanel.add(this.recEnabled, cc.xyw(1, 1, 3));
		dicomRecPanel.add(this.recAddressLabel, cc.xyw(1, 3, 3));
		dicomRecPanel.add(this.recIPLabel, cc.xyw(3, 3, 7));
		dicomRecPanel.add(this.recDirLabel, cc.xy(1, 5));
		dicomRecPanel.add(dicomRecChooserPanel, cc.xyw(3, 5, 7));
		dicomRecPanel.add(this.recAETLabel, cc.xy(1, 7));
		dicomRecPanel.add(this.recAET, cc.xyw(3, 7, 3));
		dicomRecPanel.add(this.recPortLabel, cc.xy(7, 7));
		dicomRecPanel.add(this.recPort, cc.xy(9, 7));
		
		dicomRecPanel.add(this.reindexLabel, cc.xy(1, 9));
		dicomRecPanel.add(this.reindexDesc, cc.xyw(3, 9, 5));
		dicomRecPanel.add(this.reindexButton, cc.xy(9, 9));
		if (! simpleMode && showStore)
			addSubPanel(dicomRecPanel);
		
//		 build the Layout for DICOM query
		dicomQueryPanel = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomQuery"));
		FormLayout dicomQueryLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 20dlu, 10dlu",
				"pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");
		dicomQueryPanel.setLayout(dicomQueryLayout);
		// add Fields for dicom query
		dicomQueryPanel.add(this.queryEnabled, cc.xyw(1, 1, 3));
		jb = createTestButton();
		dicomQueryPanel.add(jb, cc.xy(10, 1));
		dicomQueryPanel.add(this.queryStringLabel, cc.xy(1, 3));
		dicomQueryPanel.add(this.queryString, cc.xyw(3, 3, 8));
		dicomQueryPanel.add(this.queryToAddressLabel, cc.xy(1, 5));
		dicomQueryPanel.add(this.queryToAddress, cc.xyw(3, 5, 3));
		dicomQueryPanel.add(this.queryToPortLabel, cc.xy(7, 5));
		dicomQueryPanel.add(this.queryToPort, cc.xyw(9, 5, 2));
		dicomQueryPanel.add(this.queryToAETLabel, cc.xy(1, 7));
		dicomQueryPanel.add(this.queryToAET, cc.xyw(3, 7, 8));
		if (! simpleMode && showQuery)
			addSubPanel(dicomQueryPanel);
		
		// build the Layout for fileinput
		selectorPanel = createSubPanel(Translatrix
				.getTranslationString("dicom.Selector"));
		FormLayout selectorLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu");
		selectorPanel.setLayout(selectorLayout);
		JPanel selectorChooserPanel = new JPanel(new BorderLayout());
		selectorChooserPanel.add(this.selectorFileName, BorderLayout.CENTER);
		selectorChooserPanel.add(this.selectorChooseFile, BorderLayout.EAST);
		// add Fields for file
		selectorPanel.add(this.selectorEnabled, cc.xyw(1, 1, 3));
		selectorPanel.add(this.selectorNameLabel, cc.xy(1, 3));
		selectorPanel.add(selectorChooserPanel, cc.xyw(3, 3, 7));
		if (! simpleMode && showImageSelector)
			addSubPanel(selectorPanel);

		// build the Layout for DICOM sender
		dicomSendPanel = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomSend"));
		FormLayout dicomSendLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 10dlu, 2dlu, 70dlu:grow, 2dlu, 70dlu:grow",
				"pref, 2dlu, 50dlu, 2dlu, pref, 2dlu, pref, 2dlu");
		dicomSendPanel.setLayout(dicomSendLayout);
		// add Fields for dicom rsender
		dicomSendPanel.add(this.sendEnabled, cc.xyw(1, 1, 3));
		
		dicomSendPanel.add(new JScrollPane(this.senderUrls), cc.xyw(1, 3, 9));
		dicomSendPanel.add(deleteSenderButton, cc.xyw(7, 5, 1));
		dicomSendPanel.add(addSenderButton, cc.xyw(9, 5, 1));
		
		if (! simpleMode && showSend)
			addSubPanel(dicomSendPanel);
		
//		 build the Layout for DICOM header
		dicomHeaderPanel = createSubPanel(Translatrix
				.getTranslationString("dicom.DicomHeader"));
		FormLayout dicomHeaderLayout = new FormLayout(
				"55dlu, 2dlu, pref:grow, 2dlu, 40dlu, 2dlu, 20dlu, 2dlu, 30dlu",
				"pref, 2dlu, pref, 2dlu, fill:70dlu, 2dlu");
		dicomHeaderPanel.setLayout(dicomHeaderLayout);
		// add Fields for dicom header
		dicomHeaderPanel.add(this.showPreview, cc.xyw(1, 1, 7));
		dicomHeaderPanel.add(this.dicomHeaderLabel, cc.xy(1, 3));
		
		JButton chooseTag = new JButton("choose Tag");
		chooseTag.addActionListener(new ActionListener() {
		    private TagChooserDialog chooseTagDialog;

		    public void actionPerformed(ActionEvent e) {
			if (this.chooseTagDialog == null) {
			    try {
				Container parent = DICOMSettingsPlugin.this.getParent();
				do {
				    parent = parent.getParent();
				} while (parent != null && ! (parent instanceof JDialog));
				this.chooseTagDialog = new TagChooserDialog((JDialog) parent);				
			    } catch (Exception e2) {
				this.chooseTagDialog = new TagChooserDialog();
			    }
			}
			HeaderTag ht = chooseTagDialog.selectTag(null);
			if (ht != null) {
			    dicomHeader.append("\n" + ht.getTagNr());
			}
		    }
		});
		dicomHeaderPanel.add(chooseTag, cc.xy(1, 5));
		dicomHeaderPanel.add(new JScrollPane(this.dicomHeader), cc.xywh(3, 3, 7, 3));
		addSubPanel(dicomHeaderPanel);

	}

	/**
	 * initialises the Components
	 */
	private void initComponents() {

		// elements for file
		this.fileEnabled = new JCheckBox();
		this.chooseFile = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				fileChooser = new JFileChooser(fileName.getText());
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
					fileName.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		});
		this.fileNameLabel = new JLabel();
		this.fileName = new JTextField();
		this.fileName.setEditable(false);

		// elements for dicomCD
		this.dicomCDEnabled = new JCheckBox();
		this.chooseDicomCDFile = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			private JFileChooser dicomCDChooser;

			public void actionPerformed(ActionEvent e) {
				dicomCDChooser = new JFileChooser(dicomCDName.getText());
				dicomCDChooser.setFileFilter(new DICOMDIRFileFilter());
				dicomCDChooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (dicomCDChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
					if (dicomCDChooser.getSelectedFile().isFile()) {
						dicomCDName.setText(dicomCDChooser.getSelectedFile()
								.getAbsolutePath());
					} else {
						dicomCDName.setText(dicomCDChooser.getSelectedFile()
								.getAbsolutePath()
								+ File.separator + "DICOMDIR");
					}
				}
			}
		});
		this.dicomCDLabel = new JLabel();
		this.dicomCDName = new JTextField();
		this.dicomCDName.setEditable(false);
		
		// elements for dicomdir
		this.dicomdirEnabled = new JCheckBox();
		this.chooseDicomDirFile = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				dicomdirChooser = new JFileChooser(dicomdirName.getText());
				dicomdirChooser.setFileFilter(new DICOMDIRFileFilter());
				dicomdirChooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (dicomdirChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
						if (dicomdirChooser.getSelectedFile().isFile()) {
						dicomdirName.setText(dicomdirChooser.getSelectedFile()
								.getAbsolutePath());
					} else {
						dicomdirName.setText(dicomdirChooser.getSelectedFile()
								.getAbsolutePath()
								+ File.separator + "DICOMDIR");
					}
				}
			}
		});
		this.dicomdirLabel = new JLabel();
		this.dicomdirName = new JTextField();
		this.dicomdirName.setEditable(false);

		// elements for DICOM reciever
		this.recEnabled = new JCheckBox();
		this.recAddressLabel = new JLabel();
		this.recIPLabel = new JLabel();
		this.recIPLabel.setText(getIP());
		
		
		this.chooseRecDir = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				recDirChooser = new JFileChooser(recDir.getText());
				recDirChooser.addChoosableFileFilter(new DICOMDIRFileFilter());
				recDirChooser
						.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (recDirChooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
					if (recDirChooser.getSelectedFile().isFile()) {
						recDir.setText(recDirChooser.getSelectedFile()
								.getAbsolutePath());
					} else {
						recDir.setText(recDirChooser.getSelectedFile()
								.getAbsolutePath()
								+ File.separator + "DICOMDIR");
					}
				}
			}
		});
		this.recDirLabel = new JLabel();
		this.recDir = new JTextField();
		this.recDir.setEditable(false);
		this.recAETLabel = new JLabel();
		this.recAET = new JTextField();
		this.recPortLabel = new JLabel();
		this.recPort = new JTextField();
		this.recPort.setDocument(new NumberValidation());
		
		this.reindexLabel = new JLabel(Translatrix.getTranslationString("dicom.reindex"));
		this.reindexDesc = new JLabel(Translatrix.getTranslationString("dicom.reindexDesc"));
		this.reindexButton = new JButton(new AbstractAction("", DicomIcons.getIcon16(DicomIcons.FILTER_EDIT)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
			    reindex(new File(recDir.getText()), DICOMSettingsPlugin.this);
			}
		});
		
		//	 elements for DICOM query
		this.queryEnabled = new JCheckBox();
		this.queryStringLabel = new JLabel();
		this.queryString = new JTextField();
		
		this.queryToAddressLabel = new JLabel();
		this.queryToAddress = new JTextField();

		this.queryToPortLabel = new JLabel();
		this.queryToPort = new JTextField();
		this.queryToPort.setDocument(new NumberValidation());

		this.queryToAETLabel = new JLabel();
		this.queryToAET = new JTextField();
		
		//	elements for selector
		this.selectorEnabled = new JCheckBox();
		this.selectorChooseFile = new JButton(new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			private JFileChooser selectorFileChooser;

			public void actionPerformed(ActionEvent e) {
				selectorFileChooser = new JFileChooser(selectorFileName.getText());
				selectorFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				selectorFileChooser.showOpenDialog(getParent());
				if (selectorFileChooser.getSelectedFile() != null) {
					selectorFileName.setText(selectorFileChooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		});
		this.selectorNameLabel = new JLabel();
		this.selectorFileName = new JTextField();
		this.selectorFileName.setEditable(false);
		
		// elements for DICOM sender
		this.sendEnabled = new JCheckBox();
		
		this.senderTableModel = new DicomURLTableModel(true);
		this.senderUrls = new JTable(senderTableModel);
		
		this.senderUrls.getColumnModel().getColumn(3).setMaxWidth(50);
		this.senderUrls.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
		this.senderUrls.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor());
		this.senderUrls.getColumnModel().getColumn(4).setMaxWidth(30);
		this.addSenderButton = new JButton();
		this.addSenderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (singleSender && senderTableModel.getRowCount() > 0) {
					return;
				}
				// create new dicom url;
				senderTableModel.addLine(new DcmURL("dicom://receiver:sender@host:5104"));
			}
		});
		this.deleteSenderButton = new JButton();
		this.deleteSenderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				senderTableModel.removeLine(senderUrls.getSelectedRow());
				
				if (singleSender && senderTableModel.getRowCount() == 0)
					addSenderButton.setEnabled(true);
			}
		});
		
		//	elements for DICOM header
		this.showPreview = new JCheckBox();
		this.dicomHeader = new JTextArea();
		this.dicomHeaderLabel = new JLabel();
		this.dicomHeaderLabel.setVerticalAlignment(JLabel.TOP);
		
	}

	public static String getIP() {
	    try {
		return InetAddress.getLocalHost().getHostAddress();
	    } catch (UnknownHostException e) {
		e.printStackTrace();
	    }
	    return "";
	}

	// ***************************************************************************
	// * Class Body *
	// ***************************************************************************

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#revertToDefaults()
	 */
	public void revertToDefaults() {

		// file
		this.fileEnabled.setSelected(((Boolean) getDefault(FILE_ENABLED))
				.booleanValue());
		this.fileName.setText((String) getDefault(FILE_NAME));

		// dicomCD
		this.dicomCDEnabled
				.setSelected(((Boolean) getDefault(DICOMCD_ENABLED))
						.booleanValue());
		this.dicomCDName.setText((String) getDefault(DICOMCD_NAME));
		
		// dicomdir
		this.dicomdirEnabled
				.setSelected(((Boolean) getDefault(DICOMDIR_ENABLED))
						.booleanValue());
		this.dicomdirName.setText((String) getDefault(DICOMDIR_NAME));

		// dicom recieve
		this.recEnabled.setSelected(((Boolean) getDefault(REC_ENABLED))
				.booleanValue());
		this.recDir.setText((String) getDefault(REC_DIR));
		this.recAET.setText((String) getDefault(REC_AET));
		this.recPort.setText((String) getDefault(REC_PORT));

		// dicom query
		this.queryEnabled.setSelected(((Boolean) getDefault(QUERY_ENABLED))
				.booleanValue());
		this.queryString.setText((String) getDefault(QUERY_STRING));
		this.queryToAddress.setText((String) getDefault(QUERY_TO_ADDRESS));
		this.queryToPort.setText((String) getDefault(QUERY_TO_PORT));
		this.queryToAET.setText((String) getDefault(QUERY_TO_AET));

		// file
		this.selectorEnabled.setSelected(((Boolean) getDefault(SELECTOR_ENABLED))
				.booleanValue());
		this.selectorFileName.setText((String) getDefault(SELECTOR_DIR));
		
		// dicom send
		this.sendEnabled.setSelected(((Boolean) getDefault(SENDER_ENABLED))
				.booleanValue());
		this.senderTableModel.setUrls((String) getDefault(SENDER_URLS));

		// dicom header
		this.dicomHeader.setText((String) getDefault(DICOM_FIELDS));
		this.showPreview.setSelected(((Boolean) getDefault(DICOM_SHOWIMAGEPREVIEW))
				.booleanValue());
		
		reflectSettings();
		super.revertToDefaults();
	}

	public void setStationaryValues() {
		
		boolean panelEnabled = true;
		
		if (simpleMode)
			panelEnabled = false;
		else 
			
		
		// file
		setStationary(FILE_ENABLED, new Boolean(true));
		setStationary(FILE_NAME, "");

		// dicomCD
		setStationary(DICOMCD_ENABLED, new Boolean(true));
		setStationary(DICOMCD_NAME, "");
		
		// dicomdir
		setStationary(DICOMDIR_ENABLED, new Boolean(panelEnabled));
		setStationary(DICOMDIR_NAME, "");

		// dicom recieve
		setStationary(REC_ENABLED, false);
		setStationary(REC_DIR, "./DICOMSTORE/DICOMDIR");
		setStationary(REC_AET, "SANTEC");
		setStationary(REC_PORT, "5104");

		//	dicom query
		setStationary(QUERY_ENABLED, new Boolean(panelEnabled));
		setStationary(QUERY_STRING, "");
		setStationary(QUERY_TO_ADDRESS, "PACS_ADDRESS");
		setStationary(QUERY_TO_PORT, "5104");
		setStationary(QUERY_TO_AET, "PACS_AET");
		
		// selector
		setStationary(SELECTOR_ENABLED, new Boolean(panelEnabled));
		setStationary(SELECTOR_DIR, "");
		
		// dicom send
		setStationary(SENDER_ENABLED, new Boolean(panelEnabled));

		String url = "dicom://" + getStationary(REC_AET) + "@" + "localhost:" + getStationary(REC_PORT);
		setStationary(SENDER_URLS, url);

		// dicom header
		setStationary(DICOM_SHOWIMAGEPREVIEW, new Boolean(true));
//		setStationary(DICOM_FIELDS,"0008,0060\n0008,1010\n0020,0013\n0018,0050\n0018,0060\n0008,0021\n0008,0031");
		setStationary(DICOM_FIELDS,"0008,0060\n0020,0013\n0010,0010\n0010,0030\n0010,0040\n0018,0015");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#updateSettings()
	 */
	public void updateSettings() {

		// setValue("LogLevel", new
		// Integer(this.defFileInput.getSelectedIndex()));

		// file
		setValue(FILE_ENABLED, new Boolean(this.fileEnabled.isSelected()));
		setValue(FILE_NAME, this.fileName.getText());

		// dicomCD
		setValue(DICOMCD_ENABLED, new Boolean(this.dicomCDEnabled
				.isSelected()));
		setValue(DICOMCD_NAME, this.dicomCDName.getText());
		
		// dicomdir
		setValue(DICOMDIR_ENABLED, new Boolean(this.dicomdirEnabled
				.isSelected()));
		setValue(DICOMDIR_NAME, this.dicomdirName.getText());

		// dicom recieve
		setValue(REC_ENABLED, new Boolean(this.recEnabled.isSelected()));
		setValue(REC_DIR, this.recDir.getText());
		setValue(REC_AET, this.recAET.getText());
		setValue(REC_PORT, this.recPort.getText());

		// dicom query
		setValue(QUERY_ENABLED, new Boolean(this.queryEnabled.isSelected()));
		setValue(QUERY_STRING, this.queryString.getText());
		setValue(QUERY_TO_ADDRESS, this.queryToAddress.getText());
		setValue(QUERY_TO_PORT, this.queryToPort.getText());
		setValue(QUERY_TO_AET, this.queryToAET.getText());
		
		//	selector
		setValue(SELECTOR_ENABLED, new Boolean(this.selectorEnabled.isSelected()));
		setValue(SELECTOR_DIR, this.selectorFileName.getText());
		
		// dicom send
		setValue(SENDER_ENABLED, new Boolean(this.sendEnabled.isSelected()));
		setValue(SENDER_URLS, senderTableModel.getUrlsAsString());

		// dicom header
		setValue(DICOM_SHOWIMAGEPREVIEW, new Boolean(this.showPreview.isSelected()));
		setValue(DICOM_FIELDS,this.dicomHeader.getText());
		
		super.updateSettings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see lu.tudor.santec.settings.SettingsPlugin#reflectSettings()
	 */
	public void reflectSettings() {
		super.reflectSettings();
		try {

			// file
			this.fileEnabled.setSelected(((Boolean) getValue(FILE_ENABLED))
					.booleanValue());
			this.fileName.setText((String) getValue(FILE_NAME));

			// dicomCD
			this.dicomCDEnabled
					.setSelected(((Boolean) getValue(DICOMCD_ENABLED))
							.booleanValue());
			this.dicomCDName.setText((String) getValue(DICOMCD_NAME));
			
			// dicomdir
			this.dicomdirEnabled
					.setSelected(((Boolean) getValue(DICOMDIR_ENABLED))
							.booleanValue());
			this.dicomdirName.setText((String) getValue(DICOMDIR_NAME));

			// dicom recieve
			this.recEnabled.setSelected(((Boolean) getValue(REC_ENABLED))
					.booleanValue());
			this.recDir.setText((String) getValue(REC_DIR));
			this.recAET.setText((String) getValue(REC_AET));
			this.recPort.setText((String) getValue(REC_PORT));

			//	dicom query
			this.queryEnabled.setSelected(((Boolean) getValue(QUERY_ENABLED))
					.booleanValue());
			this.queryString.setText((String) getValue(QUERY_STRING));
			this.queryToAddress.setText((String) getValue(QUERY_TO_ADDRESS));
			this.queryToPort.setText((String) getValue(QUERY_TO_PORT));
			this.queryToAET.setText((String) getValue(QUERY_TO_AET));
			
			// file
			this.selectorEnabled.setSelected(((Boolean) getValue(SELECTOR_ENABLED))
					.booleanValue());
			this.selectorFileName.setText((String) getValue(SELECTOR_DIR));
			
			// dicom send
			this.sendEnabled.setSelected(((Boolean) getValue(SENDER_ENABLED))
					.booleanValue());
			this.senderTableModel.setUrls((String) getValue(SENDER_URLS));
			if (singleSender && this.senderTableModel.getRowCount()>0)
				this.addSenderButton.setEnabled(false);
			
			// dicom header
			this.showPreview.setSelected(((Boolean) getValue(DICOM_SHOWIMAGEPREVIEW))
					.booleanValue());
			this.dicomHeader.setText((String) getValue(DICOM_FIELDS));
			if (this.dicomHeader.getText() == null || this.dicomHeader.getText().equals("")) {
				this.dicomHeader.setText((String) getStationary(DICOM_FIELDS));	
			}

		} catch (Exception e) {
			logger.warning("Could not load Settings: "+e.getLocalizedMessage()); 
		}
	}

	/**
	 * Method is part of the Relocalizable interface. The method does everything
	 * required to reflect changes of active Locale
	 */
	public void relocalize() {
		
		setLabel(Translatrix.getTranslationString("dicom.Dicom"));
		
		setSubPanelTitle(this.filePanel,Translatrix	.getTranslationString("dicom.FileInput"));
		this.fileEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.fileNameLabel.setText(Translatrix.getTranslationString("dicom.DefFilePath"));
		
		setSubPanelTitle(dicomCD,Translatrix.getTranslationString("dicom.DicomCDInput"));
		this.dicomCDEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.dicomCDLabel.setText(Translatrix.getTranslationString("dicom.DefFilePath"));
		
		setSubPanelTitle(dicomdir,Translatrix.getTranslationString("dicom.DicomDirInput"));
		this.dicomdirEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.dicomdirLabel.setText(Translatrix.getTranslationString("dicom.DefFilePath"));
		
		setSubPanelTitle(dicomRecPanel, Translatrix	.getTranslationString("dicom.DicomRec"));
		this.recAddressLabel.setText(Translatrix.getTranslationString("dicom.QueryToAddress"));
		this.recEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.recDirLabel.setText(Translatrix.getTranslationString("dicom.DefStorePath"));
		this.recAETLabel.setText(Translatrix.getTranslationString("dicom.RecAET"));
		this.recPortLabel.setText(Translatrix.getTranslationString("dicom.RecPort"));
		
		setSubPanelTitle(dicomQueryPanel,Translatrix.getTranslationString("dicom.DicomQuery"));
		this.queryEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.queryStringLabel.setText(Translatrix.getTranslationString("dicom.QueryString"));
		this.queryToAddressLabel.setText(Translatrix.getTranslationString("dicom.QueryToAddress"));
		this.queryToPortLabel.setText(Translatrix.getTranslationString("dicom.QueryToPort"));
		this.queryToAETLabel.setText(Translatrix.getTranslationString("dicom.QueryToAET"));
		
		setSubPanelTitle(this.selectorPanel,Translatrix	.getTranslationString("dicom.Selector"));
		this.selectorEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.selectorNameLabel.setText(Translatrix.getTranslationString("dicom.DefFilePath"));
		
		setSubPanelTitle(dicomSendPanel,Translatrix.getTranslationString("dicom.DicomSend"));
		this.sendEnabled.setText(Translatrix.getTranslationString("dicom.Show"));
		this.addSenderButton.setText(Translatrix.getTranslationString("dicom.addSender"));
		this.deleteSenderButton.setText(Translatrix.getTranslationString("dicom.deleteSender"));
		
		setSubPanelTitle(dicomHeaderPanel,Translatrix.getTranslationString("dicom.DicomHeader"));
		this.showPreview.setText(Translatrix.getTranslationString("dicom.showPreview"));
		this.dicomHeaderLabel.setText(Translatrix.getTranslationString("dicom.DicomHeaderFields"));
	}
	
	/**
     * a Document for the IntegerField that only allows integer Numbers to be
     * inserted.
     */
    public class NumberValidation extends PlainDocument
    {
        //~ Static fields/initializers =========================================

        private static final long serialVersionUID = 1L;
        //~ Constructors =======================================================
        /**
         * Constructor for the Validationdocument
         */
        public NumberValidation()
        {
            super();
        }

        //~ Methods ============================================================
        /* (non-Javadoc)
         * @see javax.swing.text.Document#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
         */
        public void insertString(int offset, String str, AttributeSet attr)
            throws BadLocationException
        {
            if ((str != null) && (str != "")) {
                try {
                	int i = Integer.parseInt(str);
                    if (i >= 0 && i <= 65535)
                    	super.insertString(offset, str, attr);
                    
                    //IntegerField.this.setBackground(VALID_BACKGROUND);
                } catch (NumberFormatException e) {
                    java.awt.Toolkit.getDefaultToolkit().beep();

                    //IntegerField.this.setBackground(INVALID_BACKGROUND);
                }
            } else {
                //IntegerField.this.setBackground(VALID_BACKGROUND);
            }
        }
    }
    
    
    public JButton createTestButton() {
    	final JButton jb = new JButton(UNKNOWN);
    	jb.setToolTipText("Test connection");
    	jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					DcmURL url = new DcmURL("dicom://" + 
							queryToAET.getText() + ":" +
							recAET.getText() + "@" +
							queryToAddress.getText() + ":" +
							queryToPort.getText());
					if (DCMEcho.sendEcho(url) == Status.Success) {
						jb.setIcon(OK);
					} else 
						jb.setIcon(FAILED);					
				} catch (Exception ee) {
					jb.setIcon(FAILED);
				}
			}
    	});
    	return jb;
    }
    
    public void reindex(File inFile, JComponent comp) {

	try {

	    	setValue(REC_ENABLED, false);

		m_Owner.fireSettingEvent(new SettingEvent (this, getName(), REC_ENABLED));
		
	    	// get DIR with images
	    	File dir = inFile.getParentFile();
	    
	    	// delete old DICOMDIR file
		inFile.delete();
	    
		final File[] files = getFilesRecursive(dir);

		// create new DICOMDIR file
		final DicomDirReader ddr = new DicomDirReader();
		ddr.loadDicomDirFile(inFile, true, true);

		if (files.length == 0)
		    return;
		
		final ProgressMonitor progressMonitor = new ProgressMonitor(comp, Translatrix.getTranslationString("dicom.reindexing"), "", 0, files.length-1);
		
		new Thread() {
		    public void run() {
			for (int i = 0; i < files.length; i++) {
			    if (! files[i].isDirectory())
				try {
				    progressMonitor.setNote(" ( " + (i + 1) + " of " + (files.length + 1)+ " )");
				    progressMonitor.setProgress(i);
//				    System.out.println("adding " + files[i].getAbsolutePath());
				    ddr.append(files[i]);
				} catch (Exception e) {
				    e.printStackTrace();
				}
			}
			
			progressMonitor.close();
			setValue(REC_ENABLED, true);
			m_Owner.fireSettingEvent(new SettingEvent (DICOMSettingsPlugin.this, DICOMSettingsPlugin.this.getName(), REC_ENABLED));
		    }
		}.start();
		

	} catch (Exception e1) {
		logger.warning("error reindexing dicom view");
		e1.printStackTrace();
	}	
	
    }
    
	
	
	private static File[] getFilesRecursive(File dir) {
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
	
	public void setSingleSender(boolean singleSender) {
		this.singleSender = singleSender;
	}

}
