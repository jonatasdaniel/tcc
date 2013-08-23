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

import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lu.tudor.santec.dicom.gui.DicomFilter;
import lu.tudor.santec.dicom.gui.DicomURLChooser;
import lu.tudor.santec.dicom.gui.ErrorDialog;
import lu.tudor.santec.dicom.gui.ImagePreviewDicom;
import lu.tudor.santec.dicom.gui.TableSorter;
import lu.tudor.santec.dicom.gui.filechooser.FILEChooserDicom;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.utils.DateRenderer;
import lu.tudor.santec.dicom.gui.utils.TimeRenderer;
import lu.tudor.santec.dicom.receiver.DicomDirReader;
import lu.tudor.santec.dicom.sender.DicomSender;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * a JPanel to search for studies / series / Images in a DicomDir
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DICOMDIRSearchPanel extends JPanel implements ActionListener,
		ListSelectionListener, MouseListener, DicomFilter {

	private static final long serialVersionUID = 1L;

	private static final int IMAGE = 0;

	private static final int SERIES = 2;

	private static final int ALL = 3;

	public DicomDirReader ddr;

	private DICOMDIRVIEW parent;

	private JTable patientTable;
	
	private JTable studyTable;

	private JTable seriesTable;

	private JTable pictureTable;
	
	private PatientTableModel patientTableModel;

	private StudyTableModel studyTableModel;

	private SeriesTableModel seriesTableModel;

	private PictureTableModel pictureTableModel;

	private ImagePreviewDicom dicomImagePreview;

	private JButton openImageButton = new JButton();

	private JButton cancelButton = new JButton();

	private JButton deleteButton = new JButton();

	private JButton sendButton = new JButton();
	
	private JButton openSeriesButton = new JButton();
	
	private JButton openAllButton = new JButton();

	private File inFile;

	private ProgressMonitor progressMonitor;

	private boolean create;
	
	private boolean canSend = false;

	private ButtonBarBuilder bbuilder;

//	private Object[] picArr;

	private TableSorter patientTableSorter;

	private TableSorter studyTableSorter;

	private TableSorter seriesTableSorter;

	private TableSorter pictureTableSorter;

	private DcmURL[] senders;

	private DicomSender dicomSender;

	private Vector filterTags;

	private JLabel filterLabel;

	private ArrayList<File> sendFiles;

	private MouseEvent event;

	private JPopupMenu popup;

	private boolean delete;

	private boolean canDelete;

	private TitledBorder patientTableBorder;

	private TitledBorder studyTableBorder;

	private TitledBorder seriesTableBorder;

	private TitledBorder pictureTableBorder;

//	private JButton reindexButton;

	private static Logger logger = Logger
			.getLogger("lu.tudor.santec.dicom.gui.DicomDirSearchPanel");

	/**
	 * @param inFile the DICOMDIR file
	 * @param parent the parent DicomFileDialog
	 * @param open show open option
	 * @param delete show delete option
	 * @param send show send option
	 * @param create create DICOMDIR on startup
	 */
	public DICOMDIRSearchPanel(File inFile, DICOMDIRVIEW parent, boolean open,
			boolean delete, boolean send, boolean create, String[] dicomFields) {
		this.parent = parent;
		this.create = create;
		this.canSend = send;
		this.delete = delete;
		
			
		CellConstraints cc = new CellConstraints();
		FormLayout fl = new FormLayout(
				"6dlu, 100dlu:grow, 4dlu, 100dlu:grow, 4dlu, pref, 6dlu",
				"6dlu, pref, 3dlu, fill:80dlu:grow, 4dlu, 80dlu, 4dlu, pref, 6dlu");
		this.setLayout(fl);

		ddr = new DicomDirReader();

		Color bgColor = new JTextField().getBackground();
		
		filterLabel = new JLabel();
		filterLabel.setOpaque(true);
		this.add(filterLabel, cc.xyw(2, 2, 5));

		patientTableModel = new PatientTableModel();
		this.patientTableSorter = new TableSorter(patientTableModel);
		patientTable = new JTable(patientTableSorter) {
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
//				System.out.println(">row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
//        			System.out.println("<row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
				super.changeSelection (row, column, toggle, extend);
		    }
		};
		this.patientTableSorter.setTableHeader(this.patientTable.getTableHeader());
		patientTable.setRowSelectionAllowed(true);
		patientTable.setColumnSelectionAllowed(false);
		patientTable.addMouseListener(this);
		
		this.setTableFocusKey(patientTable);
		patientTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp0 = new JScrollPane(patientTable);
		jsp0.setFocusCycleRoot(false);
		this.patientTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Patients")+":");
		jsp0.setBorder(this.patientTableBorder);
		jsp0.getViewport().setBackground(bgColor);
		this.add(jsp0, cc.xyw(2, 4, 1));
		patientTable.requestFocus();
		
		studyTableModel = new StudyTableModel();
		this.studyTableSorter = new TableSorter(studyTableModel);
		studyTable = new JTable(studyTableSorter){
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
//				System.out.println(">row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
//        			System.out.println("<row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
				super.changeSelection (row, column, toggle, extend);
		    }
		};
		this.studyTableSorter.setTableHeader(this.studyTable.getTableHeader());
		studyTable.setRowSelectionAllowed(true);
		studyTable.setColumnSelectionAllowed(false);
		studyTable.addMouseListener(this);
		studyTable.getColumnModel().getColumn(1).setCellRenderer(new DateRenderer());
		studyTable.getColumnModel().getColumn(2).setCellRenderer(new TimeRenderer());
		this.setTableFocusKey(studyTable);
		studyTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp1 = new JScrollPane(studyTable);
		jsp1.setFocusCycleRoot(false);
		this.studyTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Studies")+":");
		jsp1.setBorder(this.studyTableBorder);
		jsp1.getViewport().setBackground(bgColor);
		this.add(jsp1, cc.xyw(4, 4, 1));

		seriesTableModel = new SeriesTableModel();
		this.seriesTableSorter = new TableSorter(seriesTableModel);
		seriesTable = new JTable(seriesTableSorter){
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
//				System.out.println(">row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
//        			System.out.println("<row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
				super.changeSelection (row, column, toggle, extend);
		    }
		};
		this.seriesTableSorter.setTableHeader(this.seriesTable.getTableHeader());
		seriesTable.getColumnModel().getColumn(0).setMaxWidth(60);
		seriesTable.getColumnModel().getColumn(2).setMaxWidth(50);
		seriesTable.setRowSelectionAllowed(true);
		seriesTable.setColumnSelectionAllowed(false);
		seriesTable.addMouseListener(this);
		this.setTableFocusKey(seriesTable);
		seriesTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp2 = new JScrollPane(seriesTable);
		this.seriesTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Series")+":");
		jsp2.setBorder(this.seriesTableBorder);
		jsp2.getViewport().setBackground(bgColor);
		this.add(jsp2, cc.xy(2, 6));

		pictureTableModel = new PictureTableModel();
		this.pictureTableSorter = new TableSorter(pictureTableModel);
		pictureTable = new JTable(pictureTableSorter){
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
//				System.out.println(">row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
//        			System.out.println("<row " + row  + " toggle " + toggle + " extend " + extend + " selected " + getSelectedRow());
				super.changeSelection (row, column, toggle, extend);
		    }
		};
		this.pictureTableSorter.setTableHeader(this.pictureTable.getTableHeader());
		pictureTable.setRowSelectionAllowed(true);
		pictureTable.setColumnSelectionAllowed(false);
		pictureTable.addMouseListener(this);
		pictureTable.getColumnModel().getColumn(0).setMaxWidth(55);
		
		this.setTableFocusKey(pictureTable);
		pictureTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp3 = new JScrollPane(pictureTable);
		this.pictureTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Pictures")+":");
		jsp3.setBorder(this.pictureTableBorder);
		jsp3.getViewport().setBackground(bgColor);
		this.add(jsp3, cc.xy(4, 6));

		dicomImagePreview = new ImagePreviewDicom(this.pictureTable, parent.getParentDialog().getCurrentDialog(), dicomFields);
		this.add(dicomImagePreview, cc.xywh(6, 4, 1, 3));

		bbuilder = new ButtonBarBuilder();

//		reindexButton = new JButton("reindex");
//		reindexButton.addActionListener(this);
//		bbuilder.addFixed(reindexButton);
		
		if (this.delete) {
		    deleteButton = new JButton(Translatrix
			    .getTranslationString("dicom.Delete"));
		    deleteButton.setEnabled(false);
		    deleteButton.addActionListener(this);
		    bbuilder.addFixed(deleteButton);
		    bbuilder.addRelatedGap();
		}
		if (send) {
			sendButton = new JButton(Translatrix.getTranslationString("dicom.Send"));
			sendButton.setEnabled(false);
			sendButton.addActionListener(this);
			bbuilder.addFixed(sendButton);
		}

		bbuilder.addGlue();
		
	
		// add Buttons
		if (open) {
			bbuilder.addRelatedGap();
			openImageButton = new JButton(Translatrix.getTranslationString("dicom.OpenImage"));
			openImageButton.setEnabled(false);
			openImageButton.addActionListener(this);
			bbuilder.addGridded(openImageButton);
		}
		
		if (open) {
			bbuilder.addRelatedGap();
			openSeriesButton = new JButton(Translatrix.getTranslationString("dicom.OpenSeries"));
			openSeriesButton.setEnabled(false);
			openSeriesButton.addActionListener(this);
			bbuilder.addGridded(openSeriesButton);
		}
		
		if (open && ! create) {
			bbuilder.addRelatedGap();
			openAllButton = new JButton(Translatrix.getTranslationString("dicom.OpenAll"));
//			openAllButton.setEnabled(false);
			openAllButton.addActionListener(this);
			bbuilder.addGridded(openAllButton);
		}
		
		bbuilder.addRelatedGap();
		cancelButton = new JButton(Translatrix
				.getTranslationString("dicom.Cancel"));
		cancelButton.addActionListener(this);
		bbuilder.addFixed(cancelButton);
		this.add(bbuilder.getPanel(), cc.xyw(2, 8, 5));

		this.setPath(inFile);

	}

	/**
	 * reload the DICOMDIR file
	 * @param inFile
	 */
	public void reload(File inFile) {
		this.parent.getParentDialog().setWaitCursor(true);
		try {
			this.inFile = inFile;
			canDelete = this.delete;
			if (! inFile.canWrite()) {
				canDelete = false;
			}
			
			if (ddr.loadDicomDirFile(inFile, create, canDelete)) {
				this.patientTableModel.setPatients(ddr.getPatients());	
				this.patientTableBorder.setTitle(Translatrix.getTranslationString("dicom.Patients") + ": [" + this.patientTableModel.getRowCount() +"]");
			    
			} else {
				this.patientTableModel.setPatients(new Vector());
				this.patientTableBorder.setTitle(Translatrix.getTranslationString("dicom.Patients"));
			    
			}
			this.studyTableModel.setStudies(new Vector());
			this.seriesTableModel.setSeries(new Vector());
			this.pictureTableModel.setPictures(new Vector());
			this.dicomImagePreview.loadImage(null);
		} catch (Throwable e1) {
			logger.warning("error updating dicom view");
			e1.printStackTrace();
			this.parent.getParentDialog().setWaitCursor(false);
		}
		if (canSend) sendButton.setEnabled(false);
		deleteButton.setEnabled(false);
		openImageButton.setEnabled(false);
		openSeriesButton.setEnabled(false);
//		openAllButton.setEnabled(false);
		this.parent.getParentDialog().setWaitCursor(false);
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.parent.getParentDialog().pacsPanel != null) {
			this.parent.getParentDialog().pacsPanel.setServerStatus(PACSPanel.SERVER_RESET);
		}
		if (e.getSource().equals(this.cancelButton)) {
			this.cancel();
		} else if (e.getSource().equals(this.openImageButton)) {
			this.open(IMAGE);
		} else if (e.getSource().equals(this.openSeriesButton)) {
			this.open(SERIES);
		} else if (e.getSource().equals(this.openAllButton)) {
			this.open(ALL);
		} else if (e.getSource().equals(this.deleteButton)) {
			this.delete(null);
		} else if (e.getSource().equals(this.sendButton)) {
			this.send();
//		} else if (e.getSource().equals(this.reindexButton)) {
//			this.reindex(this.inFile);
		}
	}


	/**
	 * set the path of the DICOMDIR file
	 * @param f
	 */
	public void setPath(File f) {
		this.reload(f);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
	    
		if (e.getValueIsAdjusting() == true)
			return;
		
		if (this.parent.getParentDialog().pacsPanel != null) { 
			this.parent.getParentDialog().pacsPanel.setServerStatus(PACSPanel.SERVER_RESET);
		}
		this.parent.getParentDialog().setWaitCursor(true);
		if (e.getSource().equals(this.patientTable.getSelectionModel())) {
			// patient selected
			this.selectPatient();
		} else if (e.getSource().equals(this.studyTable.getSelectionModel())) {
			// study selected
			this.selectStudy();
		} else if (e.getSource().equals(this.seriesTable.getSelectionModel())) {
			// series selected
			this.selectSeries();
		} else if (e.getSource().equals(this.pictureTable.getSelectionModel())) {
			// picture selected
			this.selectImages();
		}
		this.parent.getParentDialog().setWaitCursor(false);
	}

	/**
	 * allows to browse the tables using up/down/tab keys
	 * @param table
	 */
	@SuppressWarnings("unchecked")
	private void setTableFocusKey(JTable table) {
		Set keys = table
				.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
		// table.putClientProperty(ORIGINAL_FORWARD_FOCUS_KEYS, keys);
		keys = new HashSet(keys);
		keys.add(KeyStroke.getKeyStroke("TAB"));
		table.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

		keys = table
				.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
		// table.putClientProperty(ORIGINAL_BACKWARD_FOCUS_KEYS, keys);
		keys = new HashSet(keys);
		keys.add(KeyStroke.getKeyStroke("shift TAB"));
		table.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);
	}

	/**
	 * opens the selected Files from the DICOMDIR
	 */
	public void open(int openType) {
		this.parent.getParentDialog().setWaitCursor(true);
		try {
		    if (openType==IMAGE && pictureTable.getSelectedRowCount() > 0) {
		        if (pictureTable.getSelectedRowCount() == 1) {
		            // for one single DICOM Image
		            parent.getParentDialog().setSingleFileSelected(true);
					parent.getParentDialog().setSelectedFile(ddr.getImagePathFromImage(this.pictureTableModel.getRecord(this.pictureTable.getSelectedRow())));
					parent.getParentDialog().setSelectedFiles(filesForRowsFromModel(seriesTable.getSelectedRows(), this.seriesTableModel));
				} else {
				    // for multiple Dicom images
				    parent.getParentDialog().setSingleFileSelected(false);
				    parent.getParentDialog().setSelectedFile(null);
				    parent.getParentDialog().setSelectedFiles(filesForRowsFromModel(pictureTable.getSelectedRows(), this.pictureTableModel));
				}
		    } else if (openType==SERIES && seriesTable.getSelectedRowCount() > 0) {
//		        // for one or more series
		        parent.getParentDialog().setSingleFileSelected(false);
		        parent.getParentDialog().setSelectedFile(null);
		        parent.getParentDialog().setSelectedFiles(filesForRowsFromModel(seriesTable.getSelectedRows(), this.seriesTableModel));
		    } else if (openType==ALL) {
//		        // for one or more series
		        parent.getParentDialog().setSingleFileSelected(false);
		        parent.getParentDialog().setSelectedFile(null);
		        File dir = this.inFile;
		        if (! dir.isDirectory()) dir = dir.getParentFile();
		        File[] files = FILEChooserDicom.getFilesRecursive(dir);
				Arrays.sort(files);
		        parent.getParentDialog().setSelectedFiles(files);
		    } else {
		        throw new RuntimeException(); // cancel
		    }
			parent.getParentDialog().setRetValue(JFileChooser.APPROVE_OPTION);
			parent.getParentDialog().setVisible(false);
		} catch (Exception ee) {
			parent.getParentDialog().setSelectedFiles(null);
			parent.getParentDialog().setSelectedFile(null);
			parent.getParentDialog().setRetValue(JFileChooser.CANCEL_OPTION);
			parent.getParentDialog().setVisible(false);
		}
		this.parent.getParentDialog().setWaitCursor(false);
	}

    private File[] filesForRowsFromModel(int[] rows, PictureTableModel pictureTableModel) throws Exception {
        File[] files = new File[rows.length];
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            File file = ddr.getImagePathFromImage(pictureTableModel.getRecord(row));
            files[i] = file;
        }
        return files;
    }

    private File[] filesForRowsFromModel(int[] rows, SeriesTableModel seriesTableModel) throws Exception {
        List<File> result = new ArrayList<File>();
        for (int i = 0; i < rows.length; i++) {
            int row = rows[i];
            File[] files = ddr.getImagePathsFromSeries(seriesTableModel.getRecord(row));
            result.addAll(Arrays.asList(files));
        }
        return result.toArray(new File[result.size()]);
    }

	/**
	 * sends the selected Files from the DICOMDIR
	 */
	@SuppressWarnings("unchecked")
	public void send() {
		
		DcmURL url = DicomURLChooser.showDialog(parent.getParentDialog().getCurrentDialog(), senders);
		if (url == null )
			return;
		dicomSender = new DicomSender(url);
		
		sendFiles = new ArrayList<File>();
		
		// for pic
		if (this.pictureTable.getSelectedRow() != -1) {
			File f;
			try {
			    int[] rows = this.pictureTable.getSelectedRows();
			    for (int i = 0; i < rows.length; i++) {
				f = ddr.getImagePathFromImage(this.pictureTableModel
					.getRecord(pictureTableSorter.modelIndex(rows[i])));
				logger.warning("sending image: " + f);
				sendFiles.add(f);				
			    }
			} catch (Exception e) {
				logger.warning("no image found");
				ErrorDialog.showErrorDialog(parent.getParentDialog().getCurrentDialog() , "Send Error", "no image found", e);
				this.parent.getParentDialog().setWaitCursor(false);
			}
		// for Series
		} else if (this.seriesTable.getSelectedRow() != -1) {
		    	int[] rows = this.seriesTable.getSelectedRows();
		    	for (int i = 0; i < rows.length; i++) {
		    	    File[] fArr = ddr.getImagePathsFromSeries(this.seriesTableModel
		    		    .getRecord(seriesTableSorter.modelIndex(rows[i])));
		    	    logger.warning("sending series with " + fArr.length + " images");
		    	    sendFiles.addAll(Arrays.asList(fArr));
			}
		    
		// for study
		} else if (this.studyTable.getSelectedRow() != -1) {
		    	int[] rows = this.studyTable.getSelectedRows();
		    	for (int i = 0; i < rows.length; i++) {
		    	    Vector series = ddr.getSeriesFromStudy(this.studyTableModel
		    		    .getRecord(studyTableSorter.modelIndex(rows[i])));
		    	    for (Iterator iter = series.iterator(); iter.hasNext();) {
		    		DicomObject serie = (DicomObject) iter.next();
		    		sendFiles.addAll(Arrays.asList(ddr.getImagePathsFromSeries(serie)));
		    	    }
		    	    logger.warning("sending studies with " + sendFiles.size() + " images");			    
			}
		
		// for patient
		} else if (this.patientTable.getSelectedRow() != -1) {
        	    	int[] rows = this.patientTable.getSelectedRows();
        	    	for (int i = 0; i < rows.length; i++) {
        	    	Vector studies = ddr.getStudiesFromPatients(this.patientTableModel
    	    		    .getRecord(patientTableSorter.modelIndex(rows[i])));
        	    	    for (Iterator iter = studies.iterator(); iter.hasNext();) {
        	    		DicomObject study = (DicomObject) iter.next();
				Vector series = ddr.getSeriesFromStudy(study);
				for (Iterator iter2 = series.iterator(); iter2.hasNext();) {
				    DicomObject serie = (DicomObject) iter2.next();
				    sendFiles.addAll(Arrays.asList(ddr.getImagePathsFromSeries(serie)));
				}
			    }
        		}
        	    	logger.warning("sending patient with " + sendFiles.size() + " images");			    				
		}
		
		
		
		if (sendFiles.size() == 0) {
			return;
		}
		progressMonitor = new ProgressMonitor(this, Translatrix.getTranslationString("dicom.SendingFilesTo") +" \r\n"
				+ dicomSender.getUrl(), "", 0, sendFiles.size());
		new Thread() {
			public void run() {
				parent.getParentDialog().setWaitCursor(true);
				try {
					for (int i = 0; i < sendFiles.size(); i++) {
						File element = (File) sendFiles.get(i);
						if (progressMonitor.isCanceled()) {
							break;
						}
						progressMonitor.setNote("img " + element.getName()
								+ " ( " + (i + 1) + " of " + (sendFiles.size() + 1)
								+ " )");
						progressMonitor.setProgress(i);
						dicomSender.send(element);

					}
				} catch (UnknownHostException e1) {
					ErrorDialog.showErrorDialog(parent.getParentDialog().getCurrentDialog() , "Send Error", "Dicom Reciever at Host: " + e1
						.getLocalizedMessage() + " does not exist!", e1);
					parent.getParentDialog().setWaitCursor(false);
				} catch (Exception e1) {
					ErrorDialog.showErrorDialog(parent.getParentDialog().getCurrentDialog() , "Send Error", e1.getClass().getName()+" "+ e1
							.getLocalizedMessage(), e1);
					parent.getParentDialog().setWaitCursor(false);
				}
				progressMonitor.close();
				parent.getParentDialog().setWaitCursor(false);
			}
		}.start();
	}

	/**
	 * deletes the selected Files from the DICOMDIR
	 */
	public void delete(JTable table) {
		int patient = -2;
		int picture = -2;
		int series = -2;
		int study = -2;
		
		if (table == null ) {
		    if (this.pictureTable.getSelectedRow() != -1) {
			picture = this.pictureTable.getSelectedRow()-1;
			series = this.seriesTable.getSelectedRow();
			study = this.studyTable.getSelectedRow();
			    int[] rows = pictureTable.getSelectedRows();
			    for (int i = 0; i < rows.length; i++) {
				ddr.deleteRecord(pictureTableModel.getRecord(rows[i]));			
			    }
		    } else if (this.seriesTable.getSelectedRow() != -1) {
			series = this.seriesTable.getSelectedRow()-1;
			study = this.studyTable.getSelectedRow();
			    int[] rows = seriesTable.getSelectedRows();
			    for (int i = 0; i < rows.length; i++) {
				ddr.deleteRecord(seriesTableModel.getRecord(rows[i]));			
			    }
		    } else if (this.studyTable.getSelectedRow() != -1) {
			study = this.studyTable.getSelectedRow()-1;
			    int[] rows = studyTable.getSelectedRows();
			    for (int i = 0; i < rows.length; i++) {
				ddr.deleteRecord(studyTableModel.getRecord(rows[i]));			
			    }
		    }else if (this.patientTable.getSelectedRow() != -1) {
			study = this.patientTable.getSelectedRow()-1;
			int[] rows = patientTable.getSelectedRows();
			for (int i = 0; i < rows.length; i++) {
			    ddr.deleteRecord(patientTableModel.getRecord(rows[i]));			
		        }
		    }		    
		} else {
		    DicomTableModel model = (DicomTableModel) ((TableSorter)table.getModel()).getTableModel();
		    int[] rows = table.getSelectedRows();
		    for (int i = 0; i < rows.length; i++) {
			ddr.deleteRecord(model.getRecord(rows[i]));			
		    }
		}
		
		this.parent.setPath(this.inFile);
		if (patient==-1 ) patient = 0;
		if (series==-1 ) series = 0;
		if (study==-1 ) study = 0;
		if (picture==-1 ) picture = 0;
		this.selectEntry(patient, study,series,picture);
		this.parent.getParentDialog().setWaitCursor(false);
	}

	public void selectEntry(int patient, int study, int series, int picture){
		try {
			this.patientTable.setRowSelectionInterval(patient,patient);
			this.studyTable.setRowSelectionInterval(study,study);
			this.seriesTable.setRowSelectionInterval(series,series);
			this.pictureTable.setRowSelectionInterval(picture,picture);
		} catch (Exception e) {
			// does not matter
		}
	}
	
	/**
	 * closes the dialog
	 */
	public void cancel() {
		parent.getParentDialog().setRetValue(JFileChooser.CANCEL_OPTION);
		parent.getParentDialog().setVisible(false);
		this.parent.getParentDialog().setWaitCursor(false);
	}
	
	public void enableSend(boolean enable) {
		this.sendButton.setEnabled(enable);
		this.canSend = enable;
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent arg0) {	}
	public void mouseExited(MouseEvent arg0) {	}
	public void mousePressed(MouseEvent e) {	
	    if (e.isPopupTrigger() && this.canDelete)
		showMenu(e);
	}

	public void mouseReleased(MouseEvent e) { 
	    if (e.isPopupTrigger() && this.canDelete)
		showMenu(e);
	}
	
	private void showMenu(MouseEvent e) {
	    this.event = e;
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(new AbstractAction(
						Translatrix.getTranslationString("dicom.Delete")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent ae) {
					//	get the current row
				    	JTable table = ((JTable)event.getSource());
					int row = table.rowAtPoint(event.getPoint());
					if (! table.isRowSelected(row))
					    table.setRowSelectionInterval(row, row);
					
					try {
					    delete(table);
					} catch (Exception e) {
					    e.printStackTrace();
					}
				}
			});

		}
		JTable table = ((JTable)event.getSource());
		int row = table.rowAtPoint(event.getPoint());
		if (row < 0) 
			return;
		if (! table.isRowSelected(row))
		    table.setRowSelectionInterval(row, row);
		popup.show(e.getComponent(), e.getX(), e.getY());
	}
	
	private void selectPatient() {
		try {
			if (canSend) sendButton.setEnabled(true);
			deleteButton.setEnabled(canDelete);
			openImageButton.setEnabled(false);	
			openSeriesButton.setEnabled(false);
			int row = this.patientTable.getSelectedRow();
			if (row >= 0) {
        			this.studyTableModel.setStudies(ddr
        					.getStudiesFromPatients(this.patientTableModel
        							.getRecord(this.patientTableSorter.modelIndex(row))));
        			this.studyTableBorder.setTitle(Translatrix.getTranslationString("dicom.Studies") + ": [" + this.studyTableModel.getRowCount() +"]");
        			try {
        				this.studyTable.setRowSelectionInterval(0,0);
        			} catch (Exception e) {}
			} else {
			    this.studyTableModel.setStudies(new Vector());
			    this.studyTableBorder.setTitle(Translatrix.getTranslationString("dicom.Studies"));
			    this.studyTable.clearSelection();
			    if (canSend) sendButton.setEnabled(false);
			    deleteButton.setEnabled(false);
			    openImageButton.setEnabled(false);
			    openSeriesButton.setEnabled(false);
			}
		} catch (Exception e1) {
			logger.fine("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void selectStudy() {
		try {
			if (canSend) sendButton.setEnabled(true);
			deleteButton.setEnabled(canDelete);
			openImageButton.setEnabled(false);	
			openSeriesButton.setEnabled(false);	
			
			int row = this.studyTable.getSelectedRow();
			if (row >= 0) {
			    Vector series = ddr
			    .getSeriesFromStudy(this.studyTableModel
				    .getRecord(this.studyTableSorter.modelIndex(row)));
			    
			    if (filterTags != null) {
				for (Iterator iter = filterTags.iterator(); iter.hasNext();) {
				    HeaderTag ht = (HeaderTag) iter.next();
				    if ("0008,0060".equals(ht.tagNr)) {
					Vector removedSeries = new Vector();
					for (Iterator iterator = series.iterator(); iterator
					.hasNext();) {
					    DicomObject dr = (DicomObject) iterator.next();
					    if (! dr.getString(Tag.Modality).equals(ht.tagValue)) {
						removedSeries.add(dr);
					    }
					}
					series.removeAll(removedSeries);
				    }
				}
			    }
			    this.seriesTableModel.setSeries(series);
			    this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series") + ": [" + this.seriesTableModel.getRowCount() +"]");
			    try {
				this.seriesTable.setRowSelectionInterval(0,0);
			    } catch (Exception e) {}
			} else {
			    this.seriesTableModel.setSeries(new Vector());
			    this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series"));
			    this.seriesTable.clearSelection();
			}
		} catch (Exception e1) {
			logger.fine("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	private void selectSeries() {
		try {
		    	int row = this.seriesTable.getSelectedRow();
		    	// only enable buttons if a series is selected
	             	boolean hasSelection = row >= 0;
//	             	if (canSend) sendButton.setEnabled(hasSelection);
//	             	deleteButton.setEnabled(hasSelection);
	              	openImageButton.setEnabled(hasSelection);
	              	openSeriesButton.setEnabled(hasSelection);
	              	
			if (row >= 0) {
			    this.pictureTableModel.setPictures(ddr
				    .getImagesFromSeries(this.seriesTableModel
					    .getRecord(this.seriesTableSorter.modelIndex(row))));
			    this.dicomImagePreview.loadImage(null);
			    this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures") + ": [" + this.pictureTableModel.getRowCount() +"]");
			    try {
				this.pictureTable.setRowSelectionInterval(0,0);	
			    } catch (Exception e) {}			    
			} else {
			    this.pictureTableModel.setPictures(new Vector());
			    this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures"));
			    this.pictureTable.clearSelection();
			}
			
		} catch (Exception e1) {
			logger.fine("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	private void selectImages() {
		try {
		    	int row = this.pictureTable.getSelectedRow();
	             	// only enable buttons if an image is selected'
		    	boolean hasSelection = row >= 0 ||  this.seriesTable.getSelectedRow() >= 0;
//	             	if (canSend) sendButton.setEnabled(hasSelection);
//	             	deleteButton.setEnabled(hasSelection);
	             	openImageButton.setEnabled(hasSelection);
			if (row >= 0) {
			    File img = ddr.getImagePathFromImage(this.pictureTableModel
				    .getRecord(this.pictureTableSorter.modelIndex(row)));
			    this.dicomImagePreview.loadImage(img);
			} else {
			    this.dicomImagePreview.loadImage(null);
			}
		} catch (Exception e1) {
			logger.fine("no image found");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}

	public void setDicomSenders(DcmURL[] senders) {
		this.senders = senders;
	}

	public void setDicomFilterTags(Vector headerTags) {
		this.filterTags = headerTags;
		if (filterTags != null && filterTags.size() > 0) {
			String text = "Filter: ";
			for (Iterator iter = headerTags.iterator(); iter.hasNext();) {
				HeaderTag ht = (HeaderTag) iter.next();
				text += ht.tagName + "=" + ht.tagValue  + " ";
			}
			filterLabel.setText(text);
			filterLabel.setBackground(Color.YELLOW);
		} else {
			filterLabel.setText("");
			filterLabel.setBackground(new JLabel().getBackground());
		}
	}

}
