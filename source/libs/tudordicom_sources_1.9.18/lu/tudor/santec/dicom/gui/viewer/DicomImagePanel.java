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

import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.OverlayExtractor;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderInfoDialog;
import lu.tudor.santec.dicom.gui.header.DicomHexInfoDialog;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che2.data.Tag;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImagePanel extends JTextField implements MouseListener,
		MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {
	
	private static final long serialVersionUID = 1L;

	private static final String WARNING_TEXT = Translatrix.getTranslationString("dicom.dicomImagePanel.warning");

	private static final String[] WARNING_TEXT_IMAGEIO = Translatrix.getTranslationString("dicom.dicomImagePanel.imageiowarning").split("\n");

	private static final boolean isImageIOWorking = DicomOpener.isImageIOWorking();

	private String bigAreaTitle = "Optimage Image View";
	
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomImagePanel.class
			.getName());

// fields that contain painted objects ############################
	/**
	 * the shown Image
	 */
	protected ImagePlus image;
	
	/**
	 * imageprocessor for image 
	 */
	protected ImageProcessor imageProcessor;

	/**
	 *  the added ROIs
	 */
	private Vector<Roi> rois;

	/**
	 * some default ROIs 
	 */
	private Vector<Roi> defaultRois;
	
	private Vector<OSDText> osdTexts;
	
	/**
	 * width for windowing
	 */
	protected int windowWidth;

	/**
	 * center for windowing
	 */
	protected int windowCenter;

	/**
	 * width for windowing at startup
	 */
	private int origWindowWith;

	/**
	 * center for windowing at startup
	 */
	private int origWindowCenter;
	
	/**
	 * calibration of image
	 */
	private Calibration calibration;

	/**
	 * coefficient of callibration
	 */
	private double calibrationCoefficient = 0;

//	private DicomHeader dicomHeader;
	
	private double pixelSizeX = 0;
	private double pixelSizeY = 0;

//	 fields that contain helper values for painting ########################
	/**
	 * Offset to draw
	 */
	private static int OFFSET = 5;

	/**
	 * OFFSET twice
	 */
	private static int DOUBLE_OFFSET = OFFSET * 2;
	
	/**
	 * startposition of mousecursor for dragging
	 */
	private int startpointY;

	/**
	 * startposition of mousecursor for dragging
	 */
	private int startpointX;

	/**
	 * position of mousecursor
	 */
	private int mouseY;

	/**
	 * position of mousecursor
	 */
	private int mouseX;
	
	/**
	 * position shift X
	 */
	private int shiftX;

	/**
	 * position shift Y
	 */
	private int shiftY;
	
	/**
	 * 
	 */
	private double oldZoomFactor;

	/**
	 * 
	 */
	private double zoomOffsetX;

	/**
	 * 
	 */
	private double zoomOffsetY;
	
	/**
	 * is Mousecursor in Panel
	 */
	private boolean mouseIn;
	
	private int xPixel;

	private int yPixel;
	
	private double imageAspect;


//	 fields that contain java components #############################
	/**
	 * additional copy of itself for showbig dialog
	 */
	protected DicomImagePanel bigArea;
	
	/**
	 * showbig dialog
	 */
	private JDialog bigDialog;
	
	protected JDialog parentDialog;
	
	/**
	 * popupmenue to show
	 */
	private JPopupMenu popup;
	
	private JToolBar toolBar;
	
	protected DicomHeaderInfoDialog dicomHeaderInfoDialog;
	
	/**
	 *can this imagearea show a bigdialog
	 */
	private boolean extendable;

	/**
	 * zoomfactor
	 */
	private double zoomFactor = 1;

	/**
	 * use pixels instead of HU for Intensity
	 */
	private boolean usePixelValues;

	/**
	 * is image moveable imageProcessor
	 */
	private boolean moveable;

	private boolean showRois = true; 
	private boolean showText = true;
	private boolean showDicomInfos = false;
	
	private String[] dicomInfos = {"0020,0013","0010,0010","0010,0030","0010,0040","0018,0015",};
	
	private NumberFormat nf  = NumberFormat.getInstance();
	private int halfArrowSize = 16;
	private int arrowSize = 2*halfArrowSize;	
	private Image[] arrowArray = {DicomIcons.getImage(DicomIcons.ICON_UP,arrowSize),DicomIcons.getImage(DicomIcons.ICON_DOWN,arrowSize),DicomIcons.getImage(DicomIcons.ICON_LEFT,arrowSize),DicomIcons.getImage(DicomIcons.ICON_RIGHT,arrowSize)};

	private Cursor CURSOR_DEFAULT = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private Cursor CURSOR_ZOOM = getToolkit().createCustomCursor(DicomIcons.getImage(DicomIcons.CURSOR_ZOOM,32),new Point(16,16),"zoom");
	private Cursor CURSOR_WINDOW = getToolkit().createCustomCursor(DicomIcons.getImage(DicomIcons.CURSOR_WINDOW,32),new Point(16,16),"window");
	private Cursor CURSOR_MOVE = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	
	private CursorTask cursorTask = new CursorTask();
	private Timer cursorTimer = new Timer();

	private AbstractAction actionResetAll;

	private AbstractAction actionResetWindowing;

	private AbstractAction actionResetZoom;

	private AbstractAction actionResetShift;

	private AbstractAction actionOpenImageJ;

	private AbstractAction actionSaveScreenShot;

	private AbstractAction actionShowHeader;
	
	private AbstractAction actionShowHex;
	
	private AbstractAction actionHelp;

	private JToggleButton buttonRois;

	private JToggleButton buttonText;

	private JToggleButton buttonWindow = new JToggleButton();

	private JToggleButton buttonZoom;

	private JToggleButton buttonMove;
	
	private JToggleButton buttonCrop;
	
	private JToggleButton buttonDoNothing = new JToggleButton();
		
	private JToggleButton currentButton = buttonWindow;

	private LinkedHashMap<AbstractButton, ImageListener> modeButtons = new  LinkedHashMap<AbstractButton, ImageListener>();
	private LinkedHashMap<AbstractButton, ImageListener> functionButtons = new  LinkedHashMap<AbstractButton, ImageListener>();
	
	private ImageListener imageListener;
	
	private SliceListener sliceListener;

	private Vector<OSDText> defaultOsdTexts;

	private int pixelDepth = 8;

	public DicomImagePanel parentPanel;
	
	private Series series;

	private boolean multislice;

	private JToggleButton buttonStack;

	private String unit;

	private boolean firstDrawing = true;

	private int panelOffsetY;

	private int panelOffsetX;

	private int oldPaintSize;

	private boolean interpolate = false;

	private ImageProcessor origProcessor;

	protected DicomHexInfoDialog dicomHexInfoDialog;

	private boolean dropable = true;

	private ImagePlus overlayImage;

	private boolean one2one;

	private JToggleButton buttonOne2one;

	private DicomHeader dicomHeader;

	private Vector<WindowListener> windowListeners = new Vector<WindowListener>();

	private Dimension bigAreaPrefferedSize = null;

	private boolean useDefaultButtons;

	private boolean zoomable;

	private int panelWidth;

	private int panelHeight;

	private double imageWidth;

	private double imageHeight;

	private int offsetX;

	private int offsetY;

	private int xPaintSize;

	private int yPaintSize;

	private double factorX;

	private double factorY;

	private String lastSeriesUID;

	private boolean imageIOMessage;


	/**
	 * Constructor with special Size
	 * 
	 * @param width
	 *            The width of the image
	 * @param height
	 *            The hieght of the image
	 */
	public DicomImagePanel(int width, int height) {
		super();
		this.createImageArea(width, height, true, false, false, true);
	}

	/**
	 * @param width The width of the image
	 * @param height The hieght of the image
	 * @param extendable
	 * @param zoomable
	 * @param moveable
	 */
	public DicomImagePanel(int width, int height, boolean extendable,
			boolean zoomable, boolean moveable) {
		super();
		this.createImageArea(width, height, extendable, zoomable,
				moveable, true);
	}
	
	public DicomImagePanel(int width, int height, boolean extendable,
			boolean zoomable, boolean moveable, boolean useDefaultButtons) {
		super();
		this.createImageArea(width, height, extendable, zoomable,
				moveable, useDefaultButtons);
		
	}
	
	

	/**
	 * wrapper for the different constructors
	 * 
	 * @param width
	 * @param height
	 * @param extendable
	 * @param zoomable
	 * @param moveable
	 */
	private void createImageArea(int width, int height,
			boolean extendable, boolean zoomable, boolean moveable, boolean useDefaultButtons) {
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLineBorder(Color.BLACK, 1), BorderFactory
				.createLineBorder(Color.LIGHT_GRAY, 2)));
		
		Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();
		if ( height >  scrnsize.height-50) {
			double factor = height / (scrnsize.height-50);
			height = (int) (height * factor);
			width = (int) (width * factor);
		}
		this.setPreferredSize(new Dimension(width + DOUBLE_OFFSET, height
				+ DOUBLE_OFFSET));
		this.setBackground(Color.BLACK);
		this.setCursor(CURSOR_DEFAULT);
		this.setFocusable(true);
		this.addFocusListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.extendable = extendable;
		this.moveable = moveable;
		this.zoomable = zoomable;
		this.useDefaultButtons = useDefaultButtons;
		this.addMouseWheelListener(this);
		
		this.nf.setMaximumFractionDigits(1);
		
		this.actionResetAll = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.resetAll"),DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_UNDO)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				firstDrawing = true;
				zoomFactor = 1;
				shiftX = 0;
				shiftY = 0;
				zoomOffsetX = 0;
				zoomOffsetY = 0;
				windowImage(origWindowCenter - windowCenter,
				origWindowWith - windowWidth, false, true);
			}
		};
		
		this.actionResetWindowing = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.resetWindowing"),DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_WINDOW_UNDO)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				windowImage(origWindowCenter - windowCenter,
						origWindowWith - windowWidth, false, true);
			}
		};

		this.actionResetZoom = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.resetZoom"),DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ZOOM_UNDO)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				firstDrawing = true;
				zoomFactor = 1;
				shiftX = 0;
				shiftY = 0;
				zoomOffsetX = 0;
				zoomOffsetY = 0;
				repaint();
			}
		};
		
		this.actionResetShift = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.resetShift"),DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_MOVE_UNDO)) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				firstDrawing = true;
				shiftX = 0;
				shiftY = 0;
				zoomOffsetX = 0;
				zoomOffsetY = 0;
				repaint();
			}
		};
		
		
		this.actionOpenImageJ = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.openImagej"), DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_IMAGEJ)) {
			private static final long serialVersionUID = 1L;
			private ImageJ m_ImageJ;
			public void actionPerformed(ActionEvent e) {
				      if (m_ImageJ == null) {
				          m_ImageJ = new ImageJ();
				          m_ImageJ.setVisible(false);
				      }
				      if (series != null) {
				    	  Series s = (Series) series;
				    	  WindowManager.addWindow(new ImageWindow(s.getAsImageStack()));
				      } else if (image != null) {
				    	  ImagePlus imp = new ImagePlus("DicomViewer debug",image.getProcessor().duplicate());
				    	  imp.setCalibration(image.getCalibration());
				      		WindowManager.addWindow(new ImageWindow(imp));
				      }
				      m_ImageJ.setVisible(true);
			}
		};
		
		this.actionShowHeader = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.showHeader"), DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_HEADER)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				if (dicomHeaderInfoDialog == null) {
					dicomHeaderInfoDialog = new DicomHeaderInfoDialog();
					dicomHeaderInfoDialog.setLocationRelativeTo(DicomImagePanel.this);
				}
				File f = new File(image.getOriginalFileInfo().directory + image.getOriginalFileInfo().fileName);
				dicomHeaderInfoDialog.setInfo(f);
				dicomHeaderInfoDialog.setVisible(true);
			}
		};
		
		this.actionShowHex = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.showHex"), DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_HEX)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				if (dicomHexInfoDialog == null) {
				    dicomHexInfoDialog = new DicomHexInfoDialog();
				}
				File f = new File(image.getOriginalFileInfo().directory + image.getOriginalFileInfo().fileName);
				dicomHexInfoDialog.setInfo(f);
				dicomHexInfoDialog.setVisible(true);
			}
		};
		
		this.actionSaveScreenShot = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.screenShot"), DicomIcons
				.getScreenDependentIcon(DicomIcons.ACTION_EXPORT)) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				int retval = jfc.showSaveDialog(parentDialog);
				if (retval == JFileChooser.APPROVE_OPTION) {
					File file = jfc.getSelectedFile();
					BufferedImage shot = DicomImagePanel.this.getScreenShot();
					try {
						ImageIO.write(shot, "png", file);
					} catch (IOException ioe) {
					}
				}
			}
		};
		
		this.actionHelp = new AbstractAction(Translatrix.getTranslationString("dicom.dicomImagePanel.help"), DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_HELP)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				JPanel mp = new JPanel(new BorderLayout());
				JPanel jp = new JPanel(new GridLayout(0,1));
				Component[] comps = toolBar.getComponents();
				for (int i = 0; i < comps.length; i++) {
					try {
						AbstractButton element = (AbstractButton) comps[i];
						jp.add(new JLabel(element.getToolTipText(),element.getIcon(), JLabel.LEFT));
					} catch (Exception ee) {

					}
				}
				
				StringBuffer helpText = new StringBuffer(Translatrix.getTranslationString("dicom.dicomImagePanel.helpHeader1"));
				if (multislice) {
					helpText.append(Translatrix.getTranslationString("dicom.dicomImagePanel.helpHeader2"));
				} else {
					helpText.append(Translatrix.getTranslationString("dicom.dicomImagePanel.helpHeader3"));
				}
				helpText.append(Translatrix.getTranslationString("dicom.dicomImagePanel.helpHeader4")) ;
				
				mp.add(new JLabel(helpText.toString(),JLabel.LEFT), BorderLayout.NORTH);
				mp.add(jp, BorderLayout.CENTER);
				JOptionPane.showMessageDialog(
						parentDialog, 
						mp,
						Translatrix.getTranslationString("dicom.dicomImagePanel"),
						JOptionPane.OK_OPTION, DicomIcons.getIcon(DicomIcons.ICON_HELP)
				);
			}
		};
		
	}

	public void clear()  {
		this.image = null;
		this.imageProcessor = null;
		this.repaint();
		if (this.parentPanel != null) {
			this.parentPanel.image = null;
			this.parentPanel.imageProcessor = null;
			this.parentPanel.repaint();
		}
		if (this.bigArea != null) {
			this.bigArea.image = null;
			this.bigArea.imageProcessor = null;
			this.bigArea.repaint();
		}
	}
	
	
	/**
	 * sets the showed image
	 * 
	 * @param ipl
	 */
	public void setImage(ImagePlus image) {

		if (image == null) {
			clear();
			return;
		}
		
		this.image = image;
		try {
			this.imageProcessor = image.getProcessor();
		} catch (Exception e) {
			logger.info("Image is no DicomImage");
		}
		
		if (this.imageProcessor == null) {
			logger.warning("This Object is no Image");
			return;
		}

		firstDrawing = true;
		
		// read DICOM Header
		try {
		    if (image.getProperty(DicomHeader.class.getSimpleName()) != null)
			this.dicomHeader = (DicomHeader) image.getProperty(DicomHeader.class.getSimpleName());
		    else {
			try {
			    File f = new File(image.getOriginalFileInfo().directory + image.getOriginalFileInfo().fileName);
			    this.dicomHeader = new DicomHeader(f);
			} catch (Exception e) {
			}
		    }
		    

        		try {
        		    if (OverlayExtractor.hasOverlayImage(dicomHeader.getDicomObject())) {
        			overlayImage = OverlayExtractor.extractOverlay(dicomHeader.getDicomObject());
        		    } else {
        			overlayImage = null;
        		    }
        		} catch (Exception e) {
        		    overlayImage = null;
        		}
        		
        		
        		if (this.dicomHeaderInfoDialog != null) {
        			dicomHeaderInfoDialog.setInfo(dicomHeader);
        		}
        		
        		imageAspect =  (0.0 + image.getHeight()) / image.getWidth();
        		
        		this.calibration = image.getCalibration();
        		try {
        			this.calibrationCoefficient = this.calibration.getCoefficients()[0];
        			this.usePixelValues = false;
        		} catch (Exception e) {		
        			this.usePixelValues = true;
        		}
        
        		String modality = dicomHeader.getHeaderStringValue(Tag.Modality).toUpperCase();
        		if (! usePixelValues) {
        		    if (modality.equals("CT")) {
        			unit = "HU";
        		    } else if (modality.equals("NM")) {
        			unit = "Counts";
        		    } else if (modality.equals("PT")) {
        			unit = "Counts";
        		    } else {
        			unit = "";
        		    }
        		}
        		
        		try {
        			pixelDepth = image.getBitDepth();
        	
        			// try to read pixelsize from 0028,0030
        			String[] spacing = dicomHeader.getHeaderStringValues(Tag.PixelSpacing);
        			this.pixelSizeX = Double.parseDouble(spacing[0]);	
        			this.pixelSizeY = Double.parseDouble(spacing[1]);
        		} catch (Exception e) {
        			try {
        				// else try to read pixelsize from 0018,1164
        				String[] spacing = dicomHeader.getHeaderStringValues(Tag.ImagerPixelSpacing);
        				this.pixelSizeX = Double.parseDouble(spacing[0]);
        				this.pixelSizeY = Double.parseDouble(spacing[1]);
        					
        			} catch (Exception e1) {
        			}	
        		}

        		String transferSyntax = dicomHeader.getHeaderStringValue(Tag.TransferSyntaxUID);
        		if (transferSyntax != null && transferSyntax.startsWith("1.2.840.10008.1.2.4") && ! isImageIOWorking) {
        			imageIOMessage = true;
        		} else {
        			imageIOMessage = false;
        		}
         		
		} catch (Exception e) {
		    this.dicomHeader = null;
		    e.printStackTrace();
		}
		
		double windowMin = this.imageProcessor.getMin();
		double windowMax = this.imageProcessor.getMax();

		
		
		String seriesUId = dicomHeader.getHeaderStringValue(Tag.SeriesInstanceUID);
		
		if (! seriesUId.equals(this.lastSeriesUID)) {
			this.origWindowWith = (int) (windowMax - windowMin);
			this.origWindowCenter = (int) (windowMin + (windowWidth / 2));
			this.windowWidth = this.origWindowWith;
			this.windowCenter = this.origWindowCenter;			
		} else {
			setWindow(this.windowCenter, this.windowWidth);
		}
		this.lastSeriesUID = seriesUId;
		
		repaint();
		
		if (this.bigArea != null) {
			this.bigArea.setImage(this.image);
			this.bigArea.origWindowCenter = this.origWindowCenter;
			this.bigArea.origWindowWith = this.origWindowWith;
			this.bigArea.setDefaultRois(this.defaultRois);
			this.bigArea.setRois(this.rois);
		}
		
		if (this.modeButtons.get(currentButton) != null ) {
			((ImageListener) this.modeButtons.get(currentButton)).imageChanged(image);
		}
		if (imageListener != null) imageListener.imageChanged(image);
	}

	public BufferedImage getScreenShot() {
	        int w = this.getWidth();
	        int h = this.getHeight();
	        BufferedImage shot = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	        Graphics2D g2 = shot.createGraphics();
	        this.paint(g2);
	        g2.dispose();
		return shot;
	}
	
	/**
	 * sets the preferred size
	 * 
	 * @param width
	 * @param height
	 */
	public void setImageSize(int width, int height) {
		this.setPreferredSize(new Dimension(width + DOUBLE_OFFSET, height
				+ DOUBLE_OFFSET));
		repaint();
	}

	public void setWindowingDisabled(boolean windowingDisabled) {
		if (windowingDisabled) {
			this.currentButton = buttonDoNothing;
		} else {
			this.currentButton = buttonWindow;
		}
	}
	/**
	 * sets the default rois
	 * @param rois
	 */
	public void setDefaultRois(Vector<Roi> rois) {
		this.defaultRois = rois;
		if (this.bigArea != null) {
			this.bigArea.setDefaultRois(rois);
		}
		repaint();
	}

	/**
	 * sets the rois
	 * @param rois
	 */
	public void setRois(Vector<Roi> rois) {
		this.rois = rois;
		if (this.bigArea != null) {
			this.bigArea.setRois(rois);
		}
		repaint();
	}
	
	public Vector<Roi> getRois() {
		return rois;
	}
	
	/**
	 * sets the texts
	 * @param texts
	 */
	public void setOSDTexts(Vector<OSDText> texts) {
		this.osdTexts = texts;
		if (this.bigArea != null) {
			this.bigArea.setOSDTexts(texts);
		}
		repaint();
	}
	
	/**
	 * sets the texts
	 * @param texts
	 */
	public void setDefaultOSDTexts(Vector<OSDText> texts) {
		this.defaultOsdTexts = texts;
		if (this.bigArea != null) {
			this.bigArea.setDefaultOSDTexts(texts);
		}
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		try {
			if (image != null) {

				calcPositions();
					
//				System.out.println("-------------------------------------------------------------------------------------------------");
//				System.out.println("draw: "  + offsetX + "," + offsetY + "        height/width = " + imageAspect);
//				System.out.println("imageWidth: " + imageWidth + " imageHeight: " + imageHeight);
//				System.out.println("xPaintSize: " + xPaintSize + " yPaintSize: " + yPaintSize);
//				System.out.println("factorX: " + factorX + " factorY: " + factorY);
//				System.out.println("zoomOffsetX: " + zoomOffsetX + " zoomOffsetY: " + zoomOffsetY);
				
				
				// new
				if (interpolate ) {
					if (xPaintSize != oldPaintSize) {
						imageProcessor.setInterpolate(true);
						imageProcessor = imageProcessor.resize(xPaintSize, yPaintSize);
						image.setProcessor("bla", imageProcessor);
						image.updateImage();
					}
					oldPaintSize = xPaintSize;
				} else {
				}
				
				g.drawImage(image.getImage(), offsetX, offsetY, xPaintSize, yPaintSize, this);		
//				g.drawImage(image.getImage(), offsetX, offsetY, image.getWidth(), image.getHeight(), this);		
				
				if (this.showText && overlayImage != null)
				    g.drawImage(overlayImage.getImage(), offsetX, offsetY, xPaintSize, yPaintSize, this);	
				
				if (this.getWidth() >= 200) {
					// draw the default rois into G
					drawRois(g, defaultRois, offsetX, offsetY, factorX, factorY);
					drawOSDTexts(g, defaultOsdTexts, offsetX, offsetY, factorX, factorY);
								
					if (this.showRois) {
						// draw the rois into G
						drawRois(g, rois, offsetX, offsetY, factorX, factorY);
						drawOSDTexts(g, osdTexts, offsetX, offsetY, factorX, factorY);
					}
					
					if (panelWidth > 300) {
						g.setFont(g.getFont().deriveFont(Font.BOLD));
						g.setColor(Color.RED);
						g.drawString(WARNING_TEXT, (panelWidth/2)-80,DOUBLE_OFFSET *2);
						
						if (imageIOMessage) {
							Font f = g.getFont();
							g.setFont(g.getFont().deriveFont(16.0f));
							for (int i = 0; i < WARNING_TEXT_IMAGEIO.length; i++) {
								g.drawString(WARNING_TEXT_IMAGEIO[i], (panelWidth/2)-120,DOUBLE_OFFSET *(5+(i*2)));							
							}
							g.setFont(f);
						}
						
					}
								
					// draw some infos into G
					if (this.showText) {
					    
						if (this.showDicomInfos) {
							g.setColor(Color.GREEN.darker());
							for (int i = 0; i < this.dicomInfos.length; i++) {
							    try {
								g.drawString(this.dicomHeader.getHeader(this.dicomInfos[i]), DOUBLE_OFFSET, DOUBLE_OFFSET*((i+1)*2));
							    } catch (Exception e) {}
							}
						}
						
						if (panelWidth > 300) {
							g.setColor(Color.GREEN.darker());
							g.drawString("Center: " + this.windowCenter, DOUBLE_OFFSET, panelHeight);
							g.drawString("Width: " + this.windowWidth, panelWidth-80, panelHeight);
							
							if (one2one) {
								g.drawString( "1:1", (panelWidth/2)-10,panelHeight);
							} else {
								g.drawString( nf.format(factorX) + "x", (panelWidth/2)-10,panelHeight);					    
							}
							
							if (mouseIn) {
								// draw pixel value
								int val = image.getPixel(xPixel,yPixel)[0];
								g.drawString("Px  Value=" + val, panelWidth-140, DOUBLE_OFFSET *4);
								if (! usePixelValues) {
									g.drawString("Cal Value=" + pixelToHounsfield(val) + unit, panelWidth-140, DOUBLE_OFFSET *6);
								}
								String unit = "mm";
								// draw position
								double xpos = xPixel;
								double ypos = yPixel;
								if ( this.pixelSizeX == 0 || this.pixelSizeY == 0 ) {
									unit = "px";
								} else {
									xpos = xpos  * this.pixelSizeX;
									ypos = ypos * this.pixelSizeY;
								}
								g.drawString("X=" + nf.format(xpos), panelWidth-140, DOUBLE_OFFSET *2);
								g.drawString("Y=" + nf.format(ypos)+ unit, panelWidth-80, DOUBLE_OFFSET *2);
								
								
							}
						}
						
						
					}
				}
				
				// show arrows if image is bigger than screen
				if (this.showRois) {
				    // up arrow
				    if (offsetY < 0 ) {
					g.drawImage(arrowArray[0],DOUBLE_OFFSET + (panelWidth/2)-halfArrowSize,OFFSET,arrowSize,arrowSize,this);
				    }
				    // down arrow
				    if ((panelHeight + DOUBLE_OFFSET) < (yPaintSize+offsetY)) {
					g.drawImage(arrowArray[1],OFFSET + (panelWidth/2)-halfArrowSize, panelHeight - arrowSize-5 ,arrowSize,arrowSize,this);
				    }
				    // left arow
				    if (offsetX < 0 ) {
					g.drawImage(arrowArray[2],OFFSET , OFFSET + (panelHeight/2)-halfArrowSize,arrowSize,arrowSize,this);
				    }
				    // right arrow
				    if ((panelWidth + DOUBLE_OFFSET) < (xPaintSize+offsetX)) {
					g.drawImage(arrowArray[3],panelWidth - arrowSize, OFFSET + (panelHeight/2)-halfArrowSize,arrowSize,arrowSize,this);
				    }
				}
			}
		} catch (Throwable e) {
			logger.fine(e.getMessage());
//			e.printStackTrace();
		}
		
		
	}

	private void calcPositions() {
		try {
			panelWidth = this.getWidth() - DOUBLE_OFFSET;
			panelHeight = this.getHeight() - DOUBLE_OFFSET;
			
			imageWidth = image.getWidth();
			imageHeight = image.getHeight();
			
			xPaintSize = (int) (panelWidth * zoomFactor);
			yPaintSize = (int) (panelHeight* zoomFactor);
			
			// set Zoom to 1.0
			if (one2one) {
			    xPaintSize = (int) (imageWidth);
			    yPaintSize = (int) (imageHeight);
			}
			
			offsetX = OFFSET + shiftX+ (int)zoomOffsetX;
			offsetY = OFFSET + shiftY+ (int)zoomOffsetY;
			
			double panelAspect = (0.0 + panelHeight) / panelWidth;
			if (imageAspect > panelAspect) {
				// fit image to height
				xPaintSize = (int) (yPaintSize  /  imageAspect);
			} else {
				// fit image to width
				yPaintSize = (int) (xPaintSize *  imageAspect);
			}
			
			if (firstDrawing) {
				panelOffsetY =  ((panelHeight-yPaintSize) / 2);
				panelOffsetX =  ((panelWidth-xPaintSize) / 2);
				firstDrawing = false;
			}
			
			offsetY += panelOffsetY;
			offsetX += panelOffsetX;
				
			factorX = xPaintSize / imageWidth;
			factorY = yPaintSize / imageHeight;
			

			xPixel = (int)((mouseX- offsetX) / factorX);
			yPixel = (int)((mouseY-offsetY)/factorY);
		} catch (Exception e) {
		}
		
	}

	/**
	 * @param g
	 * @param rois
	 * @param offsetX
	 * @param offsetY
	 * @param factorX
	 * @param factorY
	 */
	private static void drawRois(Graphics g, Vector<Roi> rois, int offsetX,
			int offsetY, double factorX, double factorY) {

		if (rois == null)
			return;

		for (Iterator<Roi> iter = rois.iterator(); iter.hasNext();) {
			Roi roi = (Roi) iter.next();
			if (roi == null) {
				continue;
			}
			try {
				String[] colors = roi.getName().split(",");
				g.setColor(new Color(Integer.parseInt(colors[0].trim()),
						Integer.parseInt(colors[1].trim()), Integer
								.parseInt(colors[2].trim())));
			} catch (Exception e) {
				if (roi.getStrokeColor() != null) 
					g.setColor(roi.getStrokeColor());
				else
					g.setColor(Color.YELLOW);
			}
			if (roi instanceof Line) {
				Line line = (Line) roi;
				g.drawLine((int) (line.x1 * factorX) + offsetX,
						(int) (line.y1 * factorY) + offsetY,
						(int) (line.x2 * factorX) + offsetX,
						(int) (line.y2 * factorY) + offsetY);
				
			} else if (roi instanceof OvalRoi || roi instanceof PolygonRoi
					|| roi instanceof Roi) {
				Polygon p = roi.getPolygon();
				for (int i = 0; i < p.xpoints.length; i++) {
					p.xpoints[i] = (int) (p.xpoints[i] * factorX) + offsetX;
				}
				for (int i = 0; i < p.ypoints.length; i++) {
					p.ypoints[i] = (int) (p.ypoints[i] * factorY) + offsetY;
				}
				g.drawPolygon(p);
				
			} else {
				logger.info("Roi " + roi.getClass().getName()
						+ " not implemented");
			}
		}

	}
	
	/**
	 * @param g
	 * @param rois
	 * @param offsetX
	 * @param offsetY
	 * @param factorX
	 * @param factorY
	 */
	private static void drawOSDTexts(Graphics g, Vector<OSDText> osdTexts, int offsetX,
			int offsetY, double factorX, double factorY) {

		if (osdTexts == null)
			return;

		for (Iterator<OSDText> iter = osdTexts.iterator(); iter.hasNext();) {
			try {
				OSDText osdText = (OSDText) iter.next();
				if (osdText == null) {
					continue;
				}
				g.setColor(osdText.color);
				g.drawString(osdText.text, (int) (osdText.xPos * factorX) + offsetX,	(int) (osdText.yPos * factorY) + offsetY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	// MouseListener #######################################
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2 && extendable) {
			this.showBigArea(true);
		}
	}

	public void mouseReleased(MouseEvent e) {
		//	show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
			// set mousecursor for windowing and moving
		}
		setMouseCursor(CURSOR_DEFAULT);
//		System.out.println("DEFAULT Cursor");
	}

	public void mouseEntered(MouseEvent e) {
		mouseIn = true;
	}

	public void mouseExited(MouseEvent e) {
		mouseIn = false;
		repaint();
	}

	public void mousePressed(MouseEvent e) {
		startpointY = e.getY();
		startpointX = e.getX();

		// show popup
		if (e.isPopupTrigger()) {
			showPopup(e.getComponent(), e.getX(), e.getY());
			// set mousecursor for windowing and moving
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			if ( (e.isControlDown() || currentButton.equals(buttonMove))&& moveable) {
				setMouseCursor(CURSOR_MOVE);
			} else if ( currentButton.equals(buttonZoom)) {
				// zooming
			} else if ( currentButton.equals(buttonWindow)) {
				setMouseCursor(CURSOR_WINDOW);
			} else if (this.modeButtons.containsKey(currentButton)){
				((ImageListener) this.modeButtons.get(currentButton)).pixelClicked(image,xPixel, yPixel);
			}
			if (imageListener != null) imageListener.pixelClicked(image,xPixel, yPixel);
		}
	}

	public void mouseDragged(MouseEvent e) {
		this.mouseX = e.getX();
		this.mouseY = e.getY();
		if (SwingUtilities.isLeftMouseButton(e)) {
			// moving of picture
			if ((e.isControlDown() || currentButton.equals(buttonMove)) && moveable) {
				moveImage(startpointY - e.getY(), e.getX() - startpointX, e.isShiftDown());
			} else if (currentButton.equals(buttonZoom)) {
				zoomImage(1,startpointY - e.getY() , e.isShiftDown());
			} else if (currentButton.equals(buttonStack)) {
				if (sliceListener != null) {
					if ( (startpointY - e.getY()) > 0) {
						sliceListener.previousSlice();
					} else {
						sliceListener.nextSlice();
					}
				}
			}else if (currentButton.equals(buttonWindow)){
				
				int windowCenterDiff = startpointY - e.getY();
				int windowWidthDiff = e.getX() - startpointX;
				
				// for pixelvalues higher incr.
				if (this.usePixelValues) {
					if (this.windowWidth > 2000) {
						windowCenterDiff *= (this.windowWidth / 1000);
						windowWidthDiff *= (this.windowWidth / 1000);
					} else {
						windowCenterDiff *= 2;
						windowWidthDiff *= 2;
					}
				}
				windowImage(windowCenterDiff, windowWidthDiff, e.isShiftDown(), true);
			} else {
				if (imageListener != null) {
					calcPositions();
					imageListener.mouseMoved(xPixel, yPixel, e);
				}
			}
			startpointY = e.getY();
			startpointX = e.getX();
		}

	}

	public void mouseMoved(MouseEvent e) {
		this.mouseX = e.getX();
		this.mouseY = e.getY();
		repaint();
		if (this.modeButtons.containsKey(currentButton)) {
			((ImageListener) this.modeButtons.get(currentButton)).mouseMoved(xPixel, yPixel, e);
		}
		if (imageListener != null) imageListener.mouseMoved(xPixel, yPixel, e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isControlDown() && zoomable) {
			// zooming
			this.zoomImage(e.getScrollAmount(), e.getWheelRotation(), e.isShiftDown());
		} else {
			if (multislice) {
				// sliceEvent
				if (sliceListener != null) {
					if (e.getWheelRotation() < 0) {
						sliceListener.previousSlice();
					} else {
						sliceListener.nextSlice();
					}
				}
			} else {
				// zooming
				this.zoomImage(e.getScrollAmount(), e.getWheelRotation(), e.isShiftDown());			
			}
		}
		
	}

	public void keyTyped(KeyEvent e) {
		System.out.println(e);
	    if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
		    this.buttonRois.doClick();
		    this.repaint();
		} else if (e.getKeyChar() == 't' || e.getKeyChar() == 'T') {
		    this.buttonText.doClick();
		    this.repaint();
		} else if (e.getKeyChar() == 'h' || e.getKeyChar() == 'H') {
		    this.actionShowHeader.actionPerformed(null);
		} else if (e.getKeyChar() == 'w' || e.getKeyChar() == 'W') {
		    this.buttonWindow.doClick();
		} else if (e.getKeyChar() == 'z' || e.getKeyChar() == 'Z') {
		    this.buttonZoom.doClick();
		} else if (e.getKeyChar() == 'm' || e.getKeyChar() == 'M') {
		    this.buttonMove.doClick();
		} else if (e.getKeyChar() == 's' || e.getKeyChar() == 'S') {
		    this.buttonStack.doClick();
		} 
	    
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (sliceListener != null) sliceListener.previousSlice();
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (sliceListener != null) sliceListener.nextSlice();
		}
	}
	
	// end of MouseListener ###################################

	
	/**
	 * Set an alternative Title to the "Image View"
	 * @param title
	 */
	public void setBigAreaTitle(String title) {
		this.bigAreaTitle = title;
	}
	
	/**
	 * Set a preferred size for the pop-up image window
	 * @param dimension
	 */
	public void setBigAreaPreferredSize(Dimension dimension) {
		this.bigAreaPrefferedSize = dimension;
	}
	
	/**
	 * shows a big version of the image 
	 * @param show
	 */
	public void showBigArea(boolean show) {
		if (this.bigArea == null) {
			JDialog jd = findParentDialog();
			if (jd != null) {
				this.bigDialog = new JDialog(jd);
				if (jd.isModal())
					this.bigDialog.setModal(true);
			}
			else
				this.bigDialog = new JDialog();
			
			this.bigDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			this.bigDialog.setTitle(bigAreaTitle);
			this.bigDialog.getContentPane().setLayout(new BorderLayout());
			createBigArea();
			this.bigDialog.getContentPane().add(this.bigArea, BorderLayout.CENTER);
			this.bigDialog.getContentPane().add(this.bigArea.createToolBar(), BorderLayout.WEST);
			this.bigArea.setImage(this.image);
			center(this.bigDialog);
			DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
			double monitorAspect = (0.0 + dm.getHeight()) / dm.getWidth();
//			System.out.println("imageAspect "+imageAspect);
//			System.out.println("monitorAspect "+monitorAspect);
//			System.out.println("MoniHeight " +dm.getHeight());
			if (bigAreaPrefferedSize != null) {
				this.bigArea.setPreferredSize(bigAreaPrefferedSize);
			} else {
				if (imageAspect > monitorAspect) {
					int tmp = dm.getHeight()-100;
					this.bigArea.setImageSize((int) (tmp/imageAspect), (int) tmp);	
//				System.out.println((tmp/imageAspect) + "-" +tmp);
				} else {
					int tmp = dm.getWidth()-100;
					this.bigArea.setImageSize((int) tmp, (int) (tmp*imageAspect));	
//				System.out.println(tmp + "+" +(tmp*imageAspect));
				}
			}
			this.bigDialog.pack();
		}

		if (show) {
			this.bigArea.origWindowCenter = this.origWindowCenter;
			this.bigArea.origWindowWith = this.origWindowWith;
			this.bigArea.setDefaultRois(this.defaultRois);
			this.bigArea.setRois(this.rois);
			this.bigArea.setOSDTexts(this.osdTexts);
		}
		this.bigDialog.setVisible(show);

	}

	private JDialog findParentDialog() {
		Component comp = this;
		while (comp != null) {
			if (comp instanceof JDialog)
				return (JDialog)comp;
			comp = comp.getParent();
		}
		return null;
	}

	public DicomImagePanel createBigArea() {
		
		this.bigArea = new DicomImagePanel(200, 200, false, true, true, useDefaultButtons);
		this.bigArea.parentPanel = this;
		this.bigArea.modeButtons = this.modeButtons;
		this.bigArea.functionButtons = this.functionButtons;
		this.bigArea.setParentDialog(this.bigDialog);

		return this.bigArea;
	}
	
	/**
	 * windows the image
	 * @param windowCenter
	 * @param windowWith
	 */
	public void setWindow(Integer center, Integer width) {
		if (center != null && width != null) {
			windowImage(center.intValue() - windowCenter,
					width.intValue() - windowWidth, false, false);
		} else {
			windowImage(this.origWindowCenter - windowCenter,
					this.origWindowWith - windowWidth, false, false);
		}
		
	}
	
	/**
	 * windows the image
	 * @param windowCenterDiff
	 * @param windowWithDiff
	 */
	private void windowImage(int windowCenterDiff, int windowWithDiff, boolean turbo, boolean notify ) {

		if(this.pixelDepth >= 24 ) {
			return;
		}
		
		
		if (turbo) {
			windowCenterDiff *= 5;
			windowWithDiff *= 5;
		}

//		System.out.println("window change: center: " + windowCenterDiff+ " with:" + windowWithDiff);

		
		this.windowCenter += windowCenterDiff;
		this.windowWidth += windowWithDiff;
		
		int maxVal = 256;
		if (pixelDepth == 16) {
			maxVal = Short.MAX_VALUE*2;
		}

	
			
			if (this.windowWidth <= 0 ) {
				this.windowWidth = 1;
			} else if (this.windowWidth >= maxVal ) {
				this.windowWidth = maxVal;
			}
			

			// set minimum for windowsize and center
			if (this.windowCenter-(this.windowWidth/2) <= 0 ) {
				this.windowCenter = this.windowWidth/2;
			}
			// set maximum for windowsize and center
			if (this.windowCenter+(this.windowWidth/2) >= maxVal ) {
				this.windowCenter = maxVal-(this.windowWidth/2);
			}
//		}

		// old
//		double windowMin = this.hounsfieldToPixel(this.windowCenter
//				- (this.windowWidth / 2));
//		double windowMax = this.hounsfieldToPixel(this.windowCenter
//				+ (this.windowWidth / 2));
		double windowMin = this.windowCenter- (this.windowWidth / 2);
		double windowMax = this.windowCenter+ (this.windowWidth / 2);

//		System.out.println("window to: center: " + windowCenter + " with:" + windowWidth);

		try {
//			this.imageProcessor = image.getProcessor();
			this.imageProcessor.setMinAndMax(windowMin, windowMax);
//			System.out.println("image min: " + this.imageProcessor.getMin() + " max:" + this.imageProcessor.getMax());

			this.image.updateImage();
		}catch(Exception e) {
//			System.out.println("Image is Null");
		}
		
		if (notify) {
        		for (Iterator<WindowListener> iterator = windowListeners .iterator(); iterator
        			.hasNext();) {
        		    WindowListener wl = (WindowListener) iterator.next();
        		    wl.setWindow(this, windowCenter, windowWidth);
        		}
		}
		repaint();
	}
	
	public void addWindowsListener(WindowListener wl) {
	    this.windowListeners.add(wl);
	}
	
	public void removeWindowsListener(WindowListener wl) {
	    this.windowListeners.remove(wl);
	}

	/**
	 * moves the image
	 * @param y
	 * @param x
	 */
	private void moveImage(int y, int x, boolean turbo) {
		if (turbo) {
			x = x * 5;
			y = y * 5;
		}
		this.shiftX += x;
		this.shiftY -= y;
		repaint();
	}
	
	/**
	 * zooms the image
	 * @param scrolls
	 * @param direktion
	 * @param turbo
	 */
	private void zoomImage(double scrolls, double direktion, boolean turbo) {
		
	    if (one2one)
		return;
	    
//		System.out.println("zoom " + scrolls + " "  +direktion + " " + turbo);
		
		setMouseCursor(CURSOR_ZOOM);
		cursorTask.cancel();
		cursorTask = new CursorTask();
		cursorTimer.schedule(cursorTask,500);
		if (turbo) {
			scrolls = scrolls * 5;
		}
	    oldZoomFactor = this.zoomFactor;
		this.zoomFactor += (scrolls * 3 * direktion) / 100;
		if ( direktion < 0 &&  (this.zoomFactor < 1) ) {
			this.zoomFactor = 1;
		} else if (this.zoomFactor > 100) {
			this.zoomFactor = 100;
		}
		
//		zoomOffsetX +=((mouseX * oldZoomFactor) - (mouseX * zoomFactor))/1.75;
//		zoomOffsetY +=((mouseY * oldZoomFactor) - (mouseY * zoomFactor))/1.75;
		
		zoomOffsetX +=((mouseX * oldZoomFactor) - (mouseX * zoomFactor));
		zoomOffsetY +=((mouseY * oldZoomFactor) - (mouseY * zoomFactor));
		
//		System.out.println("X " + mouseX);
//		System.out.println("Y " + mouseY);
//		
//		System.out.println("zoomoffsetX " + zoomOffsetX);
//		System.out.println("zoomoffsetY " + zoomOffsetY);
//		System.out.println("-----------------------------------------------");
		
		
		
		repaint();
//		setMouseCursor(CURSOR_DEFAULT);
	}

	/**
	 * Returns the coresponding pixel value
	 * 
	 * @param calibration
	 *            The image calibration (y-achsenabschnitt)
	 * @param hounsfield
	 *            the HU to be converted
	 * @return the correct pixelvalue
	 */
//	private double hounsfieldToPixel(double hounsfield) {
//		return hounsfield - calibrationCoefficient;
//	}

	/**
	 * Returns the corresponding HU value
	 * 
	 * @param calibration
	 *            the image calibration
	 * @param pixel
	 *            the pixel to be converted
	 * @return the correct HU
	 */
	private double pixelToHounsfield(double pixel) {
		return pixel + calibrationCoefficient;
	}

	/**
	 * creates the popupmenue
	 * @param c
	 * @param x
	 * @param y
	 */
	private void showPopup(Component c, int x, int y) {
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(this.actionResetAll);
			popup.add(this.actionResetWindowing);
			popup.add(this.actionResetZoom);
			popup.add(this.actionResetShift);
			popup.add(this.actionOpenImageJ);
		}
		popup.show(c, x, y);
	}
	
	/**
	 * shows the Dialog centered on the Screen.
	 */
	private static void center(JDialog comp) {
		Dimension f = comp.getSize();
		Point d = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint(); 
		comp.setLocation(d.x - (f.width/ 2), 0);
	}
		
	
	protected void setMouseCursor(Cursor c) {
		this.setCursor(c);
		if (this.parentDialog != null) {
			this.parentDialog.setCursor(c);
		}
	}
	
	protected void setParentDialog(JDialog jd) {
		this.parentDialog = jd;
	}
	
	public JToolBar createToolBar() {
		
		if (toolBar != null) {
			return toolBar;
		}
		toolBar = new JToolBar(JToolBar.VERTICAL); 
		toolBar.setMinimumSize(new Dimension(20,20));
		toolBar.addKeyListener(this);
		
		if (useDefaultButtons) {
			buttonWindow = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_WINDOW)) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					setModeButton((JToggleButton) e.getSource());
				}
			});
			buttonWindow.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.windowImage"));
			toolBar.add(buttonWindow);
			setModeButton(buttonWindow);
		}
		
		if (useDefaultButtons) {
			buttonZoom = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ZOOM)) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					setModeButton((JToggleButton) e.getSource());
				}
			});
			buttonZoom.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.zoomImage"));
			toolBar.add(buttonZoom);
		}

		buttonMove = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_MOVE)) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				setModeButton((JToggleButton) e.getSource());
			}
		});
		buttonMove.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.moveImage"));
		toolBar.add(buttonMove);
		
    	if (useDefaultButtons) {
    		if (multislice) {
    			buttonStack = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_STACK)) {
    				private static final long serialVersionUID = 1L;
    				public void actionPerformed(ActionEvent e) {
    					setModeButton((JToggleButton) e.getSource());
    				}
    			});
    			buttonStack.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.runStack"));
    			toolBar.add(buttonStack);
    		}
    	}
		
		for (Iterator<AbstractButton> iter = modeButtons.keySet().iterator(); iter.hasNext();) {
			JToggleButton element = (JToggleButton) iter.next();
			toolBar.add(element);
		}
		
		toolBar.addSeparator();
		
		for (Iterator<AbstractButton> iter1 = functionButtons.keySet().iterator(); iter1.hasNext();) {
			AbstractButton element = (AbstractButton) iter1.next();
			toolBar.add(element);
		}
		
		if (useDefaultButtons) {
			buttonOne2one = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ONE2ONE)) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					one2one = buttonOne2one.isSelected();
				}
			});
			buttonOne2one.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.one2one"));
			toolBar.add(buttonOne2one);
		}

		if (useDefaultButtons) {
			buttonText = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_TEXT)) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					showText = ! showText;
					buttonText.setSelected(showText);
					repaint();
				}
			});
			this.buttonText.setSelected(showText);
			this.buttonText.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.toggleText"));
			toolBar.add(buttonText);
		}
		
		if (useDefaultButtons) {
    		buttonRois = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ROIS)) {
    			private static final long serialVersionUID = 1L;
    			public void actionPerformed(ActionEvent e) {
    				showRois = ! showRois;
    				buttonRois.setSelected(showRois);
    				repaint();
    			}
    		});
    		this.buttonRois.setSelected(showRois);
    		this.buttonRois.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.toggleRois"));
    		toolBar.add(buttonRois);
		}
		
		if (useDefaultButtons) {
			toolBar.addSeparator();
		}
		
		this.actionResetAll.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.resetAll"));
		toolBar.add(this.actionResetAll);
		if (useDefaultButtons) {
    		this.actionOpenImageJ.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.openImagej"));
    		toolBar.add(this.actionOpenImageJ);
    		this.actionShowHeader.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.showHeader"));
    		toolBar.add(this.actionShowHeader);
    		this.actionShowHex.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.showHex"));
    		toolBar.add(this.actionShowHex);
    		this.actionSaveScreenShot.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.screenShot"));
    		toolBar.add(this.actionSaveScreenShot);
    		
    		toolBar.addSeparator();
    		
    		this.actionHelp.putValue(Action.SHORT_DESCRIPTION, Translatrix.getTranslationString("dicom.dicomImagePanel.help"));
    		toolBar.add(this.actionHelp);
		}
		
		return toolBar;
	}
	
	
	private void setModeButton(JToggleButton jtb) {
		this.currentButton = jtb;
		Component[] comps = toolBar.getComponents();
		for (int i = 0; i < comps.length; i++) {
			try {
				boolean isButton = false;
				JToggleButton element = (JToggleButton) comps[i];
				if (element.equals(jtb)) {
					isButton = true;
				} 			
				try {
					element.setSelected(isButton);
					((ImageListener) this.modeButtons.get(element)).optionSelected(isButton);	
				} catch (Exception e) {
				}
			} catch (Exception e) {
//				e.printStackTrace();
				// seperator detected..... break
				break;
			}

		}
	}
		
	public JToggleButton addModeButton(ImageIcon icon, String text, ImageListener il) {
		JToggleButton b = new JToggleButton(new AbstractAction("",icon) {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e) {
				if (bigArea != null) {
					bigArea.setModeButton((JToggleButton) e.getSource());
				}
			
			}
		});
		b.setToolTipText(text);
		this.modeButtons.put(b,il);
		return b;
	}

	public void addFunctionButton(AbstractButton button, ImageListener il) {
		this.functionButtons.put(button,il);
	}
	
	// #### inner classes ###################################
	class CursorTask extends TimerTask
	{
	  public void run()
	  {
		  setCursor(CURSOR_DEFAULT);
			if (parentDialog != null) {
				parentDialog.setCursor(CURSOR_DEFAULT);
			}
	  }
	}


	public double getPixelSizeX() {
		return pixelSizeX;
	}

	public double getPixelSizeY() {
		return pixelSizeY;
	}

	public void focusGained(FocusEvent e) {
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLineBorder(Color.RED, 1), BorderFactory
				.createLineBorder(Color.LIGHT_GRAY, 2)));
		this.validate();
	}

	public void focusLost(FocusEvent e) {
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createLineBorder(Color.BLACK, 1), BorderFactory
				.createLineBorder(Color.LIGHT_GRAY, 2)));
		this.validate();
	}

	public void setShowDicomInfos(boolean showDicomInfos) {
		this.showDicomInfos = showDicomInfos;
	}
	
	public void setSliceListener(SliceListener sl) {
		this.sliceListener = sl;
		this.multislice = true;
	}
	
	public void setImageListener(ImageListener il) {
		this.imageListener = il;
	}

	/**
	 * @return the interpolate
	 */
	public boolean isInterpolate() {
		return interpolate;
	}

	/**
	 * @param interpolate the interpolate to set
	 */
	public void setInterpolation(boolean interpolate) {
		if (interpolate == this.interpolate)
			return;
		
		if (interpolate) {
			this.origProcessor = this.imageProcessor.duplicate();
		} else {
			this.origProcessor.setMinAndMax(imageProcessor.getMin(), imageProcessor.getMax());
			this.imageProcessor = origProcessor;
			this.origProcessor = null;
		}
		this.image.setProcessor("bla", this.imageProcessor);
			
		try {
			bigArea.setInterpolation(interpolate);
		} catch (Exception e) {
		}
		this.interpolate = interpolate;
		
		this.oldPaintSize = 0;
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see javax.swing.text.JTextComponent#replaceSelection(java.lang.String)
	 */
	public void replaceSelection(String content) {
	    	if (dropable ) {
	    	    try {
	    		content = content.replaceAll("file:/", "");
	    		ImagePlus ip = new ImagePlus(content);
	    		if (parentPanel != null)
	    		    parentPanel.setImage(ip);
	    		else {
	    		    setImage(ip);
	    		}
	    	    } catch (Exception e) {
	    		e.printStackTrace();
	    	    }
	    	}
	}

	/**
	 * returns if files can be dropped on this panel
	 * @return 
	 */
	public boolean isFilesDropable() {
	    return dropable;
	}

	/**
	 * sets if files can be dropped on this panel
	 * @param dropable the dropable to set
	 */
	public void setFilesDropable(boolean dropable) {
	    this.dropable = dropable;
	}

	public ImagePlus getImage() {
		return image;
	}

	public JDialog getBigDialog() {
		return bigDialog;
	}

	public void setSeries(Series activeSeries) {
		this.series = activeSeries;
	}

	public Series getSeries() {
		return this.series;
	}

	/**
	 * @return the dicomInfos
	 */
	public String[] getDicomInfos() {
	    return dicomInfos;
	}

	/**
	 * @param dicomInfos the dicomInfos to set
	 */
	public void setDicomInfos(String[] dicomInfos) {
	    this.dicomInfos = dicomInfos;
	}

	/**
	 * @return the dicomHeader
	 */
	public DicomHeader getDicomHeader() {
		return dicomHeader;
	}
	
	
	

}
