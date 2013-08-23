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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ProgressMonitorInputStream;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.anonymizer.DicomAnonymizerGui;
import lu.tudor.santec.dicom.gui.DICOMSettingsPlugin;
import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.DicomImageDiffDialog;
import lu.tudor.santec.dicom.gui.DiskspaceMonitorButton;
import lu.tudor.santec.dicom.gui.MemoryMonitorButton;
import lu.tudor.santec.dicom.gui.header.DicomHeaderDiffDialog;
import lu.tudor.santec.dicom.gui.header.selector.HeaderdataEvaluator;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.I18nPlugin;
import lu.tudor.santec.settings.LoggingPlugin;
import lu.tudor.santec.settings.SettingsPanel;

import com.l2fprod.common.swing.JButtonBar;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Viewer extends JFrame implements	ActionListener, FocusListener, SliceListener{

	public static final String VERSION = "1.9.18";
	
	private Logger logger = Logger.getLogger(Viewer.class.getName());
	
	private static final long serialVersionUID = 1L;

	private JButton showSettings;

	private JButton infoButton;

	private SettingsPanel settings;

	private LoggingPlugin loggingPlugin;

	private DICOMSettingsPlugin dicomPlugin;
	
	private I18nPlugin i18nPlugin;

	private DicomFileDialog dicomFileDialog;

	private JButton inputButton;

	private JToolBar toolBar;

	private JButton helpButton;
	
	private DicomImagePanel[] imagePanels;
	
	private JToolBar imageToolBar;
	private JPanel imageToolBarPanel = new JPanel(new GridLayout(1,1));

//	private ThumbnailBar thumbnailBar;
	private SeriesBar seriesBar;

//	private StartScriptPlugin startupPlugin;

	private JToolBar settingBar;

	private JToggleButton screen1;

	private JToggleButton screen2v;

	private JToggleButton screen2h;

	private JToggleButton screen4;
	
	private JPanel screenPanel;

	private DicomImagePanel activeScreen;

	private Properties screenSettings = new Properties();

	protected String splitscreen;

	private ActionListener layoutSwitcher;

	private JToggleButton three_monitor;

	private final static String infoMessage = "<html><h1>SANTEC/TUDOR Dicom Viewer " +VERSION+"</h1>"
		+ "<h3>Centre de Recherche Public Henri Tudor<br>"
		+ "Department: Santec www.santec.tudor.lu<br>" +
				"29, Avenue John F. Kennedy, L-1855 Luxembourg - Kirchberg<h3>"
			+ "written by Johannes Hermen johannes.hermen@tudor.lu<br>"
			+ "Distributable under LGPL license - (c) 2012 Tudor/Santec<br>" 
			+ "Visit http://www.santec.tudor.lu/project/dicom for Infos and Updates<br><br>"
			+ "the following libraries were used to build and run this project:<br>"
			+ "<ul>"
			+ "<li><p>dcm4che2: A OpenSource DICOM Toolkit<br>http://sourceforge.net/projects/dcm4che/<br>&nbsp;</p></li>"
			+ "<li><p>ImageJ: Image Processing and Analysis in Java<br>http://rsb.info.nih.gov/ij/<br>&nbsp;</p></li>"
			+ "<li><p>FormLayout: Build better screens faster<br>http://www.jgoodies.com/freeware/forms/<br>&nbsp;</p></li>"
			+ "<li><p>ImageJ 3D Viewer: An OpenSource 3D Viewer for ImageJ<br>http://3dviewer.neurofly.de/<br>&nbsp;</p></li>"
			+ "</ul>"
			+ "Thanks to the following persons for providing patches, bugreports and translations:<br>"
			+ "<ul>"
			+ "<li><p>David Pinelle, University of Saskatchewan [patches and new features for export and headerhandling]</p></li>"
			+ "<li><p>Dante Brunini [italian translation]</p></li>"
			+ "<li><p>Juan Miguel Boyero Corral and dgilperez [spanish translation]</p></li>"
			+ "</ul>";
			
	private static final File SCREENPROPERTY_FILE = new File("screensettings.properties");

	private static final File PLUGIN_FILE = new File("DicomViewer.plugins");

	private static final String MAIN_LOCATION_X = "MAIN_LOCATION_X";

	private static final String MAIN_LOCATION_Y = "MAIN_LOCATION_Y";

	private static final String MAIN_WIDTH = "MAIN_WIDTH";

	private static final String MAIN_HEIGHT = "MAIN_HEIGHT";

	private static final String SPLITSCREEN = "SPLITSCREEN";
	
	JDialog[] dialogs = {
			new JDialog(),
			new JDialog()
			};

	private JButton headerDiffButton;

	private JButton anonButton;

	private JButton headerdataButton;

	private JButton imageDiffButton;

	private Vector<String> enabledPlugins = new Vector<String>();

	private Vector<String> disabledPlugins = new Vector<String>();
	
	/**
	 * create a new Instance of the Plugin
	 */
	public Viewer(boolean exitOnCLose) {
		super("SANTEC/TUDOR DICOM Viewer " + VERSION);
		if (exitOnCLose)
		    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		else
		    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				saveSettings();
				if (dicomFileDialog != null && dicomFileDialog.pacsPanel != null) {
					dicomFileDialog.pacsPanel.stopDicomServer();
				}
				dicomFileDialog = null;
				settings = null;
			}
		});
		this.buildPanel();
	}
	
	/**
	 * create a new Instance of the Plugin
	 */
	public Viewer(boolean exitOnCLose, String[] fileNames) {
		super("SANTEC/TUDOR DICOM Viewer " + VERSION);
		if (exitOnCLose) {
		    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				saveSettings();
				if (dicomFileDialog != null && dicomFileDialog.pacsPanel != null) {
					dicomFileDialog.pacsPanel.stopDicomServer();
				}
				dicomFileDialog = null;
				settings = null;
			}
		    });
		}
		else
		    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowListener() {
		    
		    public void windowOpened(WindowEvent e) {}
		    public void windowIconified(WindowEvent e) {}
		    public void windowDeiconified(WindowEvent e) {}
		    public void windowDeactivated(WindowEvent e) {}
		    public void windowActivated(WindowEvent e) {}
		    public void windowClosed(WindowEvent e) {}
		    
		    public void windowClosing(WindowEvent e) {
			System.out.println("closing");
			saveSettings();
			if (dicomFileDialog != null && dicomFileDialog.pacsPanel != null) {
			    dicomFileDialog.pacsPanel.stopDicomServer();
			}
			dicomFileDialog = null;
			settings = null;
		    }

		});
		
		this.addComponentListener(new ComponentListener() {
			public void componentShown(ComponentEvent e) {}
			public void componentResized(ComponentEvent e) {}
			public void componentMoved(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {
				System.out.println("hidden");
				clear();
			}
		});
		
		
		this.buildPanel();

		Collection<File> images = new ArrayList<File>();
		
		    for (int i = 0; i < fileNames.length; i++) {
			String fileName = fileNames[i];
			System.out.println("loading image(s): " + fileName);
			if (fileName.endsWith(".zip")) {
			    try {			
				Collection<File> zipImages = new ArrayList<File>();

				InputStream is = null;
				try {
				    URL url = new URL(fileName);
				    is = url.openStream();
				} catch (Exception e) {
				    System.out.println(e.getMessage());
				    is = new FileInputStream(fileName);
				}
		
				ZipInputStream zis = new ZipInputStream(new ProgressMonitorInputStream(this, "loading images" , is));
				ZipEntry entry;
				while ((entry = zis.getNextEntry()) != null) {
				    File f = createTempFile(zis);
				    if (f.length() >=10)
					zipImages.add(f);
				}
				zis.close();
				System.out.println("found " + zipImages.size() + " images");
				seriesBar.addSeries(zipImages.toArray(new File[0]));
				
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			} else {
			    logger.info("loading file: " + fileName);
			    InputStream is = null;
			    try {
				    URL url = new URL(fileName);
				    is = url.openStream();
				    File f = createTempFile(is);
				    images.add(f);
				    seriesBar.addSeries(images.toArray(new File[0]));
				    images.clear();
			    } catch (Exception e) {
				    System.out.println(e.getMessage());
				    images.add(new File(fileName));			  
				    seriesBar.addSeries(images.toArray(new File[0]));
				    images.clear();
			    }
			}
			
		    }		
	}

	private File createTempFile(InputStream zis) {
	    int count = 0;
	    try {
		byte data[] = new byte[2048];
		File f = File.createTempFile("dicomViewer", ".dcm");
		f.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(f);
		
		BufferedOutputStream dest = new BufferedOutputStream(fos, 2048);
		while ((count = zis.read(data, 0, 2048)) != -1) {
		   dest.write(data, 0, count);
		}
		dest.flush();
		dest.close();
		return f;
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    return null;
	}

	public void setFiles(File[] files) {
		seriesBar.addSeries(files);
//		thumbnailBar.setThumbs(files);
	}
	
	public void clear() {
		
		seriesBar.removeallSeries();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Viewer(true, args);
		
		System.out.println("done..........");
		
	}
	
	private void center(JFrame parent, JDialog dialog) {
		Dimension f = parent.getSize();
		int posX = parent.getX();
		int posY = parent.getY();
		dialog.setLocation(posX + (f.width/ 2) - dialog.getSize().width/2 , posY + (f.height / 2) - dialog.getSize().height/2);
	}

	private void buildPanel() {
	
	    	try {
		    loadAvailableModules(PLUGIN_FILE.toURL());
		} catch (MalformedURLException e1) {
		    e1.printStackTrace();
		}
	    
		for (int i = 0; i < dialogs.length; i++) {
			dialogs[i].setTitle("Screen " + (i+1));
			dialogs[i].setLayout(new BorderLayout());
			dialogs[i].setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		}
		
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
        		
		this.setIconImage(DicomIcons.getImage(DicomIcons.DICOM_VIEWER,22));
		this.getContentPane().setLayout(new BorderLayout());


		toolBar = new JToolBar();
		
		settingBar = new JToolBar();
		
		// create the settings
		this.buildSettings();
		this.showSettings = new JButton(DicomIcons.getIcon(DicomIcons.ICON_SETTINGS));
		this.showSettings.setToolTipText("Show Settings Dialog");
		this.showSettings.addActionListener(this);
		
		// create the dicom file chooser
		this.buildInput();
		this.inputButton = this.dicomFileDialog.getToolBarButton();
		this.inputButton.addActionListener(this);
		// add input
		toolBar.add(inputButton);
					
		// create the info
		this.infoButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_INFO));
		this.infoButton.setToolTipText("Show Info Dialog");
		this.infoButton.addActionListener(this);
		
		// create the helpButton
		this.helpButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_HELP));
		this.helpButton.setToolTipText("Show Help Dialog");
		this.helpButton.addActionListener(this);
		
		layoutSwitcher = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				screen1.setSelected(false);
				screen2h.setSelected(false);
				screen2v.setSelected(false);
				screen4.setSelected(false);
				three_monitor.setSelected(false);
				
				screenPanel.removeAll();
				
				splitscreen = e.getActionCommand();
				
				if (e.getActionCommand().equals("screen1")) {
					screen1.setSelected(true);
					screenPanel.setLayout(new GridLayout(1,1));
					createImagePanels(1,800,800, false);
				}else if (e.getActionCommand().equals("screen2h")) {
					screen2h.setSelected(true);
					screenPanel.setLayout(new GridLayout(2,1));
					createImagePanels(2,800,400, false);
				}else if (e.getActionCommand().equals("screen2v")) {
					screen2v.setSelected(true);
					screenPanel.setLayout(new GridLayout(1,2));
					createImagePanels(2,400,800, false);
				}else if (e.getActionCommand().equals("screen4")) {
					screen4.setSelected(true);
					screenPanel.setLayout(new GridLayout(2,2));
					createImagePanels(4,400,400, false);
				} else if (e.getActionCommand().equals("three_monitor")) {
					three_monitor.setSelected(true);
					screenPanel.setLayout(new GridLayout(1,1));
					createImagePanels(2,800,800, true);
				}
				
				screenPanel.validate();
			}
		};
		
		toolBar.addSeparator();
		this.screen1 = new JToggleButton(DicomIcons.getIcon(DicomIcons.SCREEN_1));
		this.screen1.setToolTipText("1 Screen");
		this.screen1.setSelected(true);
		this.screen1.setActionCommand("screen1");
		this.screen1.addActionListener(layoutSwitcher);
		toolBar.add(this.screen1);
		this.screen2v = new JToggleButton(DicomIcons.getIcon(DicomIcons.SCREEN_2V));
		this.screen2v.setToolTipText("2 Screens Vertical");
		this.screen2v.setActionCommand("screen2v");
		this.screen2v.addActionListener(layoutSwitcher);
		toolBar.add(this.screen2v);
		this.screen2h = new JToggleButton(DicomIcons.getIcon(DicomIcons.SCREEN_2H));
		this.screen2h.setToolTipText("2 Screens Horizontal");
		this.screen2h.setActionCommand("screen2h");
		this.screen2h.addActionListener(layoutSwitcher);
		toolBar.add(this.screen2h);
		this.screen4 = new JToggleButton(DicomIcons.getIcon(DicomIcons.SCREEN_4));
		this.screen4.setToolTipText("4 Screens");
		this.screen4.setActionCommand("screen4");
		this.screen4.addActionListener(layoutSwitcher);
		toolBar.add(this.screen4);
		this.three_monitor = new JToggleButton(DicomIcons.getIcon(DicomIcons.THREE_MONITOR));
		this.three_monitor.setToolTipText("3 Monitors");
		this.three_monitor.setActionCommand("three_monitor");
		this.three_monitor.addActionListener(layoutSwitcher);
		toolBar.add(this.three_monitor);
		
		toolBar.addSeparator();
		
		// create the anonymizer button
		this.anonButton = new JButton(DicomIcons.getIcon(DicomIcons.ICON_ANON));
		this.anonButton.setToolTipText("Anonymize DICOM Files");
		this.anonButton.addActionListener(this);
		toolBar.add(anonButton);
		
		// create the Header Diff button
		this.headerDiffButton = new  JButton(DicomIcons.getIcon(DicomIcons.ICON_DIFF));
		this.headerDiffButton.setToolTipText("Compare DICOM Headers");
		this.headerDiffButton.addActionListener(this);
		toolBar.add(headerDiffButton);
		
		// create the Image Diff button
		this.imageDiffButton = new  JButton(DicomIcons.getIcon(DicomIcons.ICON_DIFF_IMAGES));
		this.imageDiffButton.setToolTipText("Compare DICOM Images");
		this.imageDiffButton.addActionListener(this);
		toolBar.add(imageDiffButton);
		
		// create the headerdata button
		this.headerdataButton = new JButton(DicomIcons.getIcon(DicomIcons.HEADERDATA_EVAL));
		this.headerdataButton.setToolTipText(Translatrix.getTranslationString("TagSelectorDialog.title"));
		this.headerdataButton.addActionListener(this);
		toolBar.add(headerdataButton);
//		this.headerdataButton.setVisible(false);


		// add settings
		settingBar.add(showSettings);

		// add info
		settingBar.add(infoButton);
		
		// add help
		settingBar.add(helpButton);

		settingBar.addSeparator();

		// add disc monitor
		settingBar.add(new DiskspaceMonitorButton(new File((String)dicomPlugin.getValue(DICOMSettingsPlugin.REC_DIR)), true));
		
		// add memory monitor
		settingBar.add(new MemoryMonitorButton(true, true));

		JPanel toolBarPanel = new JPanel(new BorderLayout());
		
		toolBarPanel.add(toolBar, BorderLayout.CENTER);
		toolBarPanel.add(settingBar, BorderLayout.EAST);
		this.getContentPane().add(toolBarPanel, BorderLayout.NORTH);

		
		this.screenPanel=new JPanel(new GridLayout(1,1));
		this.layoutSwitcher.actionPerformed(new ActionEvent(
				this, 0, "screen1"));

//		thumbnailBar = new ThumbnailBar(JButtonBar.VERTICAL,64, this.activeScreen.parentPanel);
		seriesBar = new SeriesBar(JButtonBar.VERTICAL,96, this.activeScreen.parentPanel, false);
		
		this.getContentPane().add(this.screenPanel,BorderLayout.CENTER);
		this.getContentPane().add(imageToolBarPanel,BorderLayout.WEST);
//		this.getContentPane().add(thumbnailBar,BorderLayout.EAST);
		this.getContentPane().add(seriesBar,BorderLayout.EAST);
		this.pack();
		
		
//		// testing hermenj
//		if (System.getProperty("user.name").equals("hermenj")) {
//			this.setLocation(1280, 0);
//			this.setSize(1024, 768);
//		}
		

		loadSettings();
		
		// init the locale:
        Locale locale = (Locale)i18nPlugin.getValue("Locale");
        if (locale == null) {
            Translatrix.setLocale("en_US");
        } else {
            Translatrix.setLocale(locale);
        }
        Locale.setDefault(Translatrix.getLocale());
        settings.relocalize();
        
        // Making Swing Components Multilingual:
        SwingLocalizer.localizeJOptionPane();
        SwingLocalizer.localizeJFileChooser();
		
		if (! splitscreen.equals("three_monitor"))
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		this.setVisible(true);
	}

	private void createImagePanels(int anz, int width, int height, boolean external) {
		imagePanels = new DicomImagePanel[anz];
		String[] dicomFields = ((String)this.dicomPlugin.getValue(DICOMSettingsPlugin.DICOM_FIELDS)).split("\n");
		for (int i = 0; i < anz; i++) {
			DicomImagePanel imageArea = new DicomImagePanel(10, 10, true, false,false);
			imagePanels[i] = imageArea.createBigArea();
			imagePanels[i].setImageSize(width,height);
			imagePanels[i].addFocusListener(this);
			imagePanels[i].setShowDicomInfos(true);
			imagePanels[i].setDicomInfos(dicomFields);
			
			if (enabledPlugins.size() > 0) {
			    for (String plugin : enabledPlugins) {
				try {
				    Class clazz = Class.forName(plugin);
				    Constructor constructor = clazz.getConstructor(DicomImagePanel.class);
				    constructor.newInstance(imageArea);
				} catch (Exception e) {
				    logger.log(Level.WARNING, "Error loading plugin: " + plugin, e);
				}
			    }
			} else {
				try {
					new DicomImage3DViewer(imageArea);					
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			    new DicomImageMeasurement(imageArea);
			    new DicomImageAngleMeasurement(imageArea);
			    new DicomImageROI(imageArea);
			    new DicomImageHistogramViewerPlugin(imageArea);
			    new DicomImageInverter(imageArea);
			    new DicomImageInterpolator(imageArea);
			    
//			new DicomImageCornerSelector(imageArea,100);
//			new DicomImageRotator(imageArea);		    
			}
			
			
			
			
			imagePanels[i].setSliceListener(this);
			
			if (! external) {
				this.screenPanel.add(imagePanels[i]);
			} else {
				try {
					dialogs[i].getContentPane().remove(0);					
				} catch (Exception e) {
				}
				dialogs[i].getContentPane().add(imagePanels[i]);
				dialogs[i].setSize(800,800);
				dialogs[i].setVisible(true);
			}
		}
		
		if (! external) {
			for (int i = 0; i < dialogs.length; i++) {
				dialogs[i].setVisible(false);
			}
		} else {
			this.setSize(267, this.getWidth());
			this.setExtendedState(JFrame.MAXIMIZED_VERT);
		}
		
		this.activeScreen = imagePanels[0];
		try {
			imageToolBarPanel.removeAll();	
		} catch (Exception e) {
		}
		this.imageToolBar = activeScreen.createToolBar();
		imageToolBarPanel.add(this.imageToolBar);
		imageToolBarPanel.validate();

		if (this.seriesBar != null) {
			this.seriesBar.setImagePanel(this.activeScreen.parentPanel);
		}
	}
	
	
	
	/**
	 * create the settings Dialog
	 */
	private void buildSettings() {
		
		Translatrix.loadSupportedLocales("lu.tudor.santec.dicom.gui.resources.supportedLocales");
		
		this.dicomPlugin = new DICOMSettingsPlugin("dicom");
		this.loggingPlugin = new LoggingPlugin("logging");
		this.i18nPlugin = new I18nPlugin("i18n");
//		this.startupPlugin = new StartScriptPlugin("startscript","dicomviewer","Dicom_Viewer.app/Contents/Resources/Java/lu.tudor.santec.dicom.jar");
		this.settings = new SettingsPanel(this);
		this.settings.addPlugin(i18nPlugin);
		this.settings.addPlugin(dicomPlugin);
		this.settings.addPlugin(loggingPlugin);
//		this.settings.addPlugin(startupPlugin);
		
		File f = new File("settings.xml");
		
		if (System.getProperty("deployment.javaws.home.jnlp.url") != null) {
		    f = new File(System.getProperty("user.home"), "settings.url");
		    this.settings.setValue("dicom", DICOMSettingsPlugin.REC_ENABLED, false);
		}
		
		this.settings.setSettingsFile(f);
		this.settings.loadSettings();
	}

	/**
	 * create the Input Dialog
	 */
	private void buildInput() {
		dicomFileDialog = new DicomFileDialog(this, settings, dicomPlugin);
	}

	/**
	 * create the Info Dialog
	 */
	private void buildInfo() {
		
		JOptionPane.showMessageDialog(this, infoMessage + "<hr>" + DicomOpener.getJavaImageInfos().replaceAll("\n", "<br>"),
				"Info: Tudor DICOM Viewer", JOptionPane.INFORMATION_MESSAGE,
				DicomIcons.getIcon(DicomIcons.ICON_INFO));

	}
	
	/**
	 * create the help Dialog
	 */
	private void buildHelp() {
		JDialog dialog = new JDialog(this, "Help: Simple DICOM Viewer");
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		try {
			editorPane.setPage(DicomIcons.class.getResource("resources/dicomviewer.html"));
			JScrollPane editorScrollPane = new JScrollPane(editorPane);
			dialog.getContentPane().add(editorScrollPane);
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
			this.settings.setSize(650,650);
			center(this, this.settings);
			this.settings.setVisible(true);
		} else if (e.getSource().equals(this.inputButton)) {
			this.importFiles();
		} else if (e.getSource().equals(this.infoButton)) {
			this.buildInfo();
		}else if (e.getSource().equals(this.helpButton)) {
			this.buildHelp();
		} else if (e.getSource().equals(this.headerDiffButton)) {
			new DicomHeaderDiffDialog(this.dicomFileDialog);
		} else if (e.getSource().equals(this.imageDiffButton)) {
		    new DicomImageDiffDialog(this.dicomFileDialog);
		} else if (e.getSource().equals(this.anonButton))  {
		    new DicomAnonymizerGui(false);
		} else if (e.getSource().equals(this.headerdataButton)) {
			HeaderdataEvaluator evaluator = new HeaderdataEvaluator(this ,dicomFileDialog);
			evaluator.setViewer(this);
			evaluator.showDialog(null);
		}
	}

	/**
	 * import the selected Files into ImageJ
	 */
	public void importFiles() {
		
		// use Thread while loading Images
		new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {

				Vector filterTags = null;
				
				if (JFileChooser.APPROVE_OPTION == dicomFileDialog
						.showNewDialog(Viewer.this, filterTags)) {
					
					if (dicomFileDialog.singleFileSelected) {
						File[] fArr = {dicomFileDialog
								.getSelectedFile()};
//						thumbnailBar.setThumbs(fArr);
						seriesBar.addSeries(fArr);
					} else {
//						thumbnailBar.setThumbs(dicomFileDialog
//								.getSelectedFiles());
						seriesBar.addSeries(dicomFileDialog
								.getSelectedFiles());
					}
				}
			}

		}.start();
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

	public void focusGained(FocusEvent e) {
		try {
			imageToolBarPanel.removeAll();	
		} catch (Exception e1) {
		}
		this.activeScreen = (DicomImagePanel) e.getSource();
		this.imageToolBar = activeScreen.createToolBar();
		imageToolBarPanel.add(this.imageToolBar);
		imageToolBarPanel.validate();
		
		try {
			this.seriesBar.setCurrentSeries((Series)this.activeScreen.parentPanel.getSeries());
		} catch (Exception e2) {
//			System.out.println("series is null!!!");
		}

		this.seriesBar.setImagePanel(this.activeScreen.parentPanel);
		
	}

	public void focusLost(FocusEvent e) {
	}

	public void nextSlice() {
		this.seriesBar.showNextImage();
	}

	public void previousSlice() {
		this.seriesBar.showPreviousImage();
	}
	
	private void loadSettings() {
		try {
			screenSettings.load(new FileInputStream(SCREENPROPERTY_FILE));
			
			this.setLocation(
					Integer.parseInt(screenSettings.getProperty(MAIN_LOCATION_X)),
					Integer.parseInt(screenSettings.getProperty(MAIN_LOCATION_Y)));
			
			this.setSize(
					Integer.parseInt(screenSettings.getProperty(MAIN_WIDTH)),
					Integer.parseInt(screenSettings.getProperty(MAIN_HEIGHT)));
			
			this.layoutSwitcher.actionPerformed(new ActionEvent(
					this, 
					0,
					screenSettings.getProperty(SPLITSCREEN)));
			
			for (int i = 0; i < dialogs.length; i++) {
				dialogs[i].setLocation(
						Integer.parseInt(screenSettings.getProperty("DIALOG_" + i  + "_LOCATION_X")),
						Integer.parseInt(screenSettings.getProperty("DIALOG_" + i  + "_LOCATION_Y")));
				dialogs[i].setSize(
						Integer.parseInt(screenSettings.getProperty("DIALOG_" + i  + "_WIDTH")),
						Integer.parseInt(screenSettings.getProperty("DIALOG_" + i  + "_HEIGHT")));
			}
			
		} catch (FileNotFoundException e) {
		    System.err.println("Screensettings not found at: " +SCREENPROPERTY_FILE+ " ...creating next time...");
		} catch (Exception e) {
		    System.err.println(e.getLocalizedMessage());
		}
	}
	
	private void saveSettings() {
		try {
			screenSettings.put(MAIN_LOCATION_X, this.getLocation().x + "");
			screenSettings.put(MAIN_LOCATION_Y, this.getLocation().y + "");
			screenSettings.put(MAIN_WIDTH, this.getWidth() + "");
			screenSettings.put(MAIN_HEIGHT, this.getHeight() + "");
			screenSettings.put(SPLITSCREEN, splitscreen);
			
			for (int i = 0; i < dialogs.length; i++) {
				screenSettings.put("DIALOG_" + i  + "_LOCATION_X", dialogs[i].getLocation().x + "");
				screenSettings.put("DIALOG_" + i  + "_LOCATION_Y", dialogs[i].getLocation().y + "");
				screenSettings.put("DIALOG_" + i  + "_WIDTH", dialogs[i].getWidth() + "");
				screenSettings.put("DIALOG_" + i  + "_HEIGHT", dialogs[i].getHeight() + "");
			}
			
			screenSettings.store(new FileOutputStream(SCREENPROPERTY_FILE), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 private void loadAvailableModules(URL url) {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(url.openStream()));
			String line = new String();
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#") && line.length() > 3) {
					try {
						String[] fields = line.split("=");

						if (fields[1].toLowerCase() .equals("on")) {
							this.enabledPlugins.add(fields[0]);
						} else {
							this.disabledPlugins.add(fields[0]);
						}
					} catch (Exception e) {
						logger.log(Level.WARNING, "line is no valid settingline: " + line);
					}
				}
			}
		}catch (FileNotFoundException e) {
			logger.log(Level.INFO, "No plugins file found at " + url);
		} catch (Exception e) {
			logger.log(Level.WARNING, "error parsing file ", e);
		}
	}
	
}
