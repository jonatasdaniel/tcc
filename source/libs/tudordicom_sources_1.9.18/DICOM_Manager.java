/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2008 by SANTEC/TUDOR www.santec.tudor.lu                   
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
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ProgressMonitor;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.anonymizer.DicomAnonymizerGui;
import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomImageDiffDialog;
import lu.tudor.santec.dicom.gui.DicomSettings;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.MemoryMonitorButton;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderDiffDialog;
import lu.tudor.santec.dicom.gui.header.selector.HeaderdataEvaluator;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingsPanel;


/**
 * ImageJ Plugin that shows a management dialog for the DICOM STORE
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DICOM_Manager extends JFrame implements PlugIn,
		ActionListener, WindowListener{

	private final static String NAME = "DICOM Manager";
	
	private static Logger logger = Logger.getLogger(NAME);

	private static final long serialVersionUID = 1L;

	private JButton showSettings;

	private JButton infoButton;

	private SettingsPanel settings;

	private DicomFileDialog dicomFileDialog;

	private JButton inputButton;

	private JToolBar toolBar;

	private boolean error;

	private ProgressMonitor progressMonitor;

	private JButton helpButton;

	private JButton anonButton;

	private JButton headerDiffButton;

	private JButton headerdataButton;

	private JButton imageDiffButton;

	private final static String infoMessage = "<html><h1>Tudor DICOM Plugin</h1>"
		+ "<h2>Centre de Recherche Public Henri Tudor</h2>"
		+ "<h3>Centre de Ressources Santec www.santec.tudor.lu<br>" +
				"29, Avenue John F. Kennedy<br>" +
				"L-1855 Luxembourg - Kirchberg<h3>"
			+ "written by Johannes Hermen johannes.hermen@tudor.lu<br>"
			+ "Distributable under LGPL license - (c) 2009 Tudor/Santec<br>" 
			+ "Visit www.santec.tudor.lu/project/dicom for Infos and Updates<br><br>"
			+ "the following libraries were used to build and run this project:<br>"
			+ "<ul>"
			+ "<li><p>dcm4che2: A OpenSource DICOM Toolkit<br>Distributable under LGPL license<br>Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG<br>http://sourceforge.net/projects/dcm4che/<br>&nbsp;</p></li>"
//			+ "<li><p>imageJ: Image Processing and Analysis in Java<br>Distributable under public-domain<br>Rasband, W.S., ImageJ, U. S. National Institutes of Health<br>http://rsb.info.nih.gov/ij/<br>&nbsp;</p></li>"
			+ "<li><p>FormLayout: Build better screens faster<br>Distributable under BSD Licence<br>Copyright (c) 2003 JGoodies<br>http://www.jgoodies.com/freeware/forms/<br>&nbsp;</p></li>"
			+ "<li><p>L2FProd.com: Common Components (ButtonBar)<br>Distributable under SkinLF License<br>Copyright 2004, l2fprod.com<br>http://www.l2fprod.com/<br>&nbsp;</p></li>"
			+ "</ul>";
	
	/**
	 * create a new Instance of the Plugin
	 */
	public DICOM_Manager() {
		super(NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg0) {
		System.out.println("run: " + "DICOM_Manager");
		setWaitCursor(true);
        this.addWindowListener(this);
	
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
		
		this.setIconImage(DicomIcons.getImage(DicomIcons.PACS_ON,22));
		this.getContentPane().setLayout(new BorderLayout());
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// create the settings
		this.buildSettings();
		toolBar = new JToolBar();
		// ButtonBarBuilder bb = new ButtonBarBuilder();
		this.showSettings = new JButton(DicomIcons.getIcon(DicomIcons.ICON_SETTINGS));
		this.showSettings.setToolTipText("Show Settings Dialog");
		this.showSettings.addActionListener(this);

		// create the dicom file chooser
		this.buildInput();
		this.inputButton = this.dicomFileDialog.getToolBarButton();
		this.inputButton.addActionListener(this);

		// create the info
		this.infoButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_INFO));
		this.infoButton.setToolTipText("Show Info Dialog");
		this.infoButton.addActionListener(this);
		
		// create the helpButton
		this.helpButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_HELP));
		this.helpButton.setToolTipText("Show Help Dialog");
		this.helpButton.addActionListener(this);
		
		// create the anonymizer button
		this.anonButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_ANON));
		this.anonButton.setToolTipText("Anonymize DICOM Files");
		this.anonButton.addActionListener(this);
		
		// create the header comparison button
		this.headerDiffButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_DIFF));
		this.headerDiffButton.setToolTipText("Compare DICOM Headers");
		this.headerDiffButton.addActionListener(this);
		
		// create the Image Diff button
		this.imageDiffButton = new  JButton(DicomIcons.getIcon(DicomIcons.ICON_DIFF_IMAGES));
		this.imageDiffButton.setToolTipText("Compare DICOM Images");
		this.imageDiffButton.addActionListener(this);
		
		// create the headerdata button
		this.headerdataButton = new JButton(DicomIcons.getIcon(DicomIcons.HEADERDATA_EVAL));
		this.headerdataButton.setToolTipText(Translatrix.getTranslationString("TagSelectorDialog.title"));
		this.headerdataButton.addActionListener(this);

		// add input
		toolBar.add(inputButton);

		// add anon
		toolBar.add(anonButton);
		
		// add diff
		toolBar.add(headerDiffButton);
		toolBar.add(imageDiffButton);
		
		// add eval header
		toolBar.add(headerdataButton);
		
		// add settings
		toolBar.add(showSettings);

		// add info
		toolBar.add(infoButton);
		
		// add help
		toolBar.add(helpButton);

		toolBar.addSeparator();

		// add memory monitor
		toolBar.add(new MemoryMonitorButton(false, true));

		this.getContentPane().add(new JLabel(NAME),
				BorderLayout.NORTH);
		this.getContentPane().add(toolBar, BorderLayout.CENTER);

		this.pack();
		setWaitCursor(false);
		this.setVisible(true);

	}

	/**
	 * create the settings Dialog
	 */
	private void buildSettings() {
		this.settings = DicomSettings.getSettingPanel();
	}

	/**
	 * create the Input Dialog
	 */
	private void buildInput() {
		dicomFileDialog = DicomFileDialog.getDicomFileDialog();
	}

	/**
	 * create the Info Dialog
	 */
	private void buildInfo() {
		JOptionPane.showMessageDialog(this, infoMessage,
				"Tudor DICOM Plugin - About", JOptionPane.INFORMATION_MESSAGE,
				DicomIcons.getIcon(DicomIcons.ICON_INFO));

	}
	
	/**
	 * create the help Dialog
	 */
	private void buildHelp() {
		JDialog dialog = new JDialog(this, "Tudor DICOM Plugin - Help");
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		try {
			editorPane.setPage(DicomIcons.class.getResource("resources/dicom.html"));
			JScrollPane editorScrollPane = new JScrollPane(editorPane);
			dialog.add(editorScrollPane);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setSize(600, 600);
			dialog.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.showSettings)) {
			this.settings.setVisible(true);
		} else if (e.getSource().equals(this.inputButton)) {
			this.importFiles();
		} else if (e.getSource().equals(this.infoButton)) {
			this.buildInfo();
		}else if (e.getSource().equals(this.helpButton)) {
			this.buildHelp();
		}else if (e.getSource().equals(this.anonButton))  {
		    new DicomAnonymizerGui(false);
		}else if (e.getSource().equals(this.headerDiffButton))  {
		    new DicomHeaderDiffDialog(dicomFileDialog);
		}else if (e.getSource().equals(this.imageDiffButton)) {
		    new DicomImageDiffDialog(this.dicomFileDialog);
		}else if (e.getSource().equals(this.headerdataButton)) {
			new HeaderdataEvaluator(this ,dicomFileDialog).showDialog(null);
		}
		
	}

	/**
	 * import the selected Files into ImageJ
	 */
	public void importFiles() {

		progressMonitor = new ProgressMonitor(this, "loading Files", "", 0, 25);
		
		// use Thread while loading Images
		new Thread() {
			public void run() {
				Object info = null;
				DicomHeader dh = null;
				
				if (JFileChooser.APPROVE_OPTION == dicomFileDialog
						.showDialog()) {
					// one file only
					if (dicomFileDialog.getSingleFileSelected()) {
						logger.info("Single File selected");
						progressMonitor.setNote(dicomFileDialog
								.getSelectedFile().getAbsolutePath());
						progressMonitor.setMaximum(1);
						ImagePlus imgPlus;
						try {
						    imgPlus = new ImagePlus(dicomFileDialog.getSelectedFile().getAbsolutePath());
						    imgPlus.getStack();
						    imgPlus.show();
						} catch (Exception ee) {
						    try {
							imgPlus = DicomOpener.loadImageStack(dicomFileDialog.getSelectedFile(), null);
							progressMonitor.setProgress(1);
							imgPlus.show();
						    } catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						    }
						}
					} else {
						// multiple files
						File[] files = dicomFileDialog.getSelectedFiles();
						logger.info(files.length + " Files selected");
						error = false;
						try {
							progressMonitor.setMaximum(files.length);
							ImageStack ipStack = null;
							ImagePlus imgPlus = null;
							for (int i = 0; i < files.length; i++) {
								if (progressMonitor.isCanceled()) {
									break;
								}
								progressMonitor.setNote(files[i]
										.getAbsolutePath());
								progressMonitor.setProgress(i);

								imgPlus = DicomOpener.loadImageStack(files[i], null);
								ImageProcessor ip = imgPlus.getProcessor();

								if (ipStack == null) {
									ipStack = new ImageStack(ip.getWidth(), ip
											.getHeight());
									info = imgPlus.getProperty("Info");
									dh = new DicomHeader(imgPlus);
								}
								try {
									ipStack.addSlice(files[i].getName(), ip);
								} catch (Exception e) {
									error = true;
								}

								logger.info("Slice: " + i + " : "
										+ files[i].getAbsolutePath() + "added");
							}
							imgPlus = new ImagePlus(ipStack.getSliceLabel(1),
									ipStack);
							try {
								imgPlus.setProperty("Info",info);
								imgPlus.setProperty(DicomHeader.class.getSimpleName(), dh);						
							} catch (Exception e) {
							}
							imgPlus.show();
						} catch (Exception e) {
							logger
									.info("error importing series, try to open a single file only");
							JOptionPane
									.showMessageDialog(
											DICOM_Manager.this,
											"Import of this Series Failes.\r\nTry to import a single file only.",
											"Warning",
											JOptionPane.WARNING_MESSAGE);
						} catch (Error e) {
							logger
									.info("error importing series, try to open a single file only");
							JOptionPane
									.showMessageDialog(
											DICOM_Manager.this,
											"Import of this Series Failes.\r\nTry to import a single file only.",
											"Warning",
											JOptionPane.WARNING_MESSAGE);
						}

						if (error) {
							JOptionPane
									.showMessageDialog(
											DICOM_Manager.this,
											"Some pictures could not be added to Series.\r\nTry to import a single file only.",
											"Warning",
											JOptionPane.WARNING_MESSAGE);
						}
					}
				}
				progressMonitor.close();
			}
		}.start();
	}

	public void windowActivated(WindowEvent arg0) {
	}
	public void windowClosed(WindowEvent arg0) {
	}
	public void windowClosing(WindowEvent arg0) {
		this.dicomFileDialog.pacsPanel.stopDicomServer();
		this.dicomFileDialog = null;
		this.settings = null;
		this.dispose();
	}
	public void windowDeactivated(WindowEvent arg0) {
	}
	public void windowDeiconified(WindowEvent arg0) {
	}
	public void windowIconified(WindowEvent arg0) {
	}
	public void windowOpened(WindowEvent arg0) {
	}

    /**
     * sets the Mousecursor of the MainFrame to a WaitCursor and Back
     *
     * @param on true=waitcursor false=normalcursor
     */
    public void setWaitCursor(boolean on)
    {
        if (on) {
            getGlassPane().setVisible(true); 
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
        } else {
            getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            getGlassPane().setVisible(false); 
        }
    }
	
}
