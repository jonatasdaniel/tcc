package lu.tudor.santec.dicom.gui.query;

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

import java.awt.AWTKeyStroke;
import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lu.tudor.santec.dicom.gui.DicomURLChooser;
import lu.tudor.santec.dicom.gui.ErrorDialog;
import lu.tudor.santec.dicom.gui.TableSorter;
import lu.tudor.santec.dicom.gui.dicomdir.DICOMDIRVIEW;
import lu.tudor.santec.dicom.gui.dicomdir.PACSPanel;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.utils.DateRenderer;
import lu.tudor.santec.dicom.gui.utils.TimeRenderer;
import lu.tudor.santec.dicom.query.DcmQR;
import lu.tudor.santec.dicom.query.DicomQuery;
import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.DicomObject;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * a JPanel to search for studies / series / Images in a DicomDir
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class QuerySearchPanel extends JPanel implements ActionListener,
		ListSelectionListener, MouseListener {

	private static final long serialVersionUID = 1L;

	public DicomQuery dq;

	private DICOMDIRVIEW parent;

	private JTable patientTable;
	
	private JTable studyTable;

	private JTable seriesTable;

	private JTable pictureTable;

	private QueryPatientTableModel patientTableModel;
	
	private QueryStudyTableModel studyTableModel;

	private QuerySeriesTableModel seriesTableModel;

	private QueryPictureTableModel pictureTableModel;

	private JButton cancelButton = new JButton();

	private JButton sendButton = new JButton();

	private ButtonBarBuilder bbuilder;

	private DcmURL url;

	private DcmURL[] senders;

	private TableSorter patientTableSorter;

	private TableSorter studyTableSorter;

	private TableSorter seriesTableSorter;

	private TableSorter pictureTableSorter;

	private String moveAET;

	private JButton movelocalButton;

	private DcmURL localDest;

	private String wantedModality;

	private DcmURL moveUrl;

	private TitledBorder patientTableBorder;

	private TitledBorder studyTableBorder;

	private TitledBorder seriesTableBorder;

	private TitledBorder pictureTableBorder;
	
	public static  final String[] MODALITIES = {
			"",
			"CR",
			"DR",
			"MG",
			"CT",
			"MR",
			"NM",
			"PT",
			"RF",
			"SR",
			"EPS",
			"US",
			"DX",
			"ECG",
			"ES",
			"XC",
			"GM",
			"HD",
			"IO",
			"IVUS",
			"PX",
			"RG",
			"RTIMAGE",
			"SM",
			"XA"
	};

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
	public QuerySearchPanel(DICOMDIRVIEW parent, DcmURL url) {
		this.parent = parent;
		this.url = url;
		
		
		
		CellConstraints cc = new CellConstraints();
		FormLayout fl = new FormLayout(
				"6dlu, 100dlu:grow, 4dlu,	60dlu:grow, 4dlu, 60dlu, 4dlu, pref, 6dlu",
				"6dlu, fill:80dlu:grow, 4dlu, 80dlu, 4dlu, pref, 6dlu");
		this.setLayout(fl);

		
		dq = new DicomQuery(url);

		Color bgColor = new JTextField().getBackground();

		patientTableModel = new QueryPatientTableModel();
		this.patientTableSorter = new TableSorter(patientTableModel);
		patientTable = new JTable(patientTableSorter)  {
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
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
		this.add(jsp0, cc.xyw(2, 2, 1));
		patientTable.requestFocus();
		
		studyTableModel = new QueryStudyTableModel();
		this.studyTableSorter = new TableSorter(studyTableModel);
		studyTable = new JTable(studyTableSorter) {
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
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
		this.add(jsp1, cc.xyw(4, 2, 3));


		seriesTableModel = new QuerySeriesTableModel();
		this.seriesTableSorter = new TableSorter(seriesTableModel);
		seriesTable = new JTable(seriesTableSorter) {
		    private static final long serialVersionUID = 1L;
		    public void changeSelection (int row, int column, boolean toggle, boolean extend) {
        			if (row == -1 || column == -1) {
        			    return;
        			}
        			if (getSelectedRow() == row) {
        			    toggle = true;
        			}
				super.changeSelection (row, column, toggle, extend);
		    }
		};
		this.seriesTableSorter.setTableHeader(this.seriesTable.getTableHeader());
		seriesTable.getColumnModel().getColumn(0).setMaxWidth(60);
		seriesTable.getColumnModel().getColumn(2).setMaxWidth(30);
		seriesTable.setRowSelectionAllowed(true);
		seriesTable.setColumnSelectionAllowed(false);
		seriesTable.addMouseListener(this);
		this.setTableFocusKey(seriesTable);
		seriesTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp2 = new JScrollPane(seriesTable);
		this.seriesTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Series")+":");
		jsp2.setBorder(this.seriesTableBorder);
		jsp2.getViewport().setBackground(bgColor);
		this.add(jsp2, cc.xyw(2, 4, 3));

		pictureTableModel = new QueryPictureTableModel();
		this.pictureTableSorter = new TableSorter(pictureTableModel);
		pictureTable = new JTable(pictureTableSorter);
		this.pictureTableSorter.setTableHeader(this.pictureTable.getTableHeader());
		pictureTable.setRowSelectionAllowed(true);
		pictureTable.setColumnSelectionAllowed(false);
		pictureTable.addMouseListener(this);
		pictureTable.getColumnModel().getColumn(0).setMaxWidth(50);
		
		this.setTableFocusKey(pictureTable);
		pictureTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane jsp3 = new JScrollPane(pictureTable);
		this.pictureTableBorder = new TitledBorder(Translatrix.getTranslationString("dicom.Pictures"));
		jsp3.setBorder(this.pictureTableBorder);
		jsp3.getViewport().setBackground(bgColor);
		this.add(jsp3, cc.xy(6, 4));

		bbuilder = new ButtonBarBuilder();
		
		sendButton = new JButton(Translatrix.getTranslationString("dicom.Send"));
		sendButton.setEnabled(false);
		sendButton.addActionListener(this);
		bbuilder.addGridded(sendButton);
		
		bbuilder.addGlue();

		bbuilder.addRelatedGap();
		movelocalButton = new JButton(Translatrix.getTranslationString("dicom.MoveLocal"));
		movelocalButton.setEnabled(false);
		movelocalButton.addActionListener(this);
		bbuilder.addGridded(movelocalButton);
		
		bbuilder.addRelatedGap();
		cancelButton = new JButton(Translatrix
				.getTranslationString("dicom.Cancel"));
		cancelButton.addActionListener(this);
		bbuilder.addGridded(cancelButton);
		this.add(bbuilder.getPanel(), cc.xyw(2, 6, 5));
		
//		this.reload("*");

	}
	
	/**
	 * reload the DICOMDIR file
	 * @param wantedModality 
	 * @param inFile
	 */
	public void query(Vector<HeaderTag> filters, DcmQR.QueryRetrieveLevel level) {
		this.parent.getParentDialog().setWaitCursor(true);
		try {
			dq = new DicomQuery(this.url);
			
			if (level.equals(DcmQR.QueryRetrieveLevel.PATIENT)) {
				this.patientTableModel.setPatients(dq.query(filters, level));
				this.studyTableModel.setStudies(new Vector<DicomObject>());				
			} else if (level.equals(DcmQR.QueryRetrieveLevel.STUDY)) {
				this.patientTableModel.setPatients(new Vector<DicomObject>());
				this.studyTableModel.setStudies(dq.query(filters, level));			
			}
			
			this.seriesTableModel.setSeries(new Vector<DicomObject>());
			this.pictureTableModel.setPictures(new Vector<DicomObject>());
			
			this.patientTableBorder.setTitle(Translatrix.getTranslationString("dicom.Patients")+": [" + this.patientTableModel.getRowCount()+ "]");
			this.studyTableBorder.setTitle(Translatrix.getTranslationString("dicom.Studies")+": [" + this.studyTableModel.getRowCount()+ "]");
			this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series")+": [" + this.seriesTableModel.getRowCount()+ "]");
			this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures")+": [" + this.pictureTableModel.getRowCount()+ "]");
			
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error running DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
		sendButton.setEnabled(false);
		movelocalButton.setEnabled(false);
		this.parent.getParentDialog().setWaitCursor(false);
	}

	public List<String> getQueryLevels() {
		List<String> keys = new ArrayList<String>();
		keys.add(DcmQR.QueryRetrieveLevel.PATIENT.name());
		keys.add(DcmQR.QueryRetrieveLevel.STUDY.name());
		return keys;
	}
	
	public List<String> getQueryFields(DcmQR.QueryRetrieveLevel level) {
		List<String> keys = new ArrayList<String>();
		if (level.equals(DcmQR.QueryRetrieveLevel.PATIENT)) {
			for (int matchKey : DcmQR.PATIENT_MATCHING_KEYS) {
				keys.add(DicomHeader.getHeaderName(matchKey));
			}
		} else if (level.equals(DcmQR.QueryRetrieveLevel.STUDY)) {
			for (int matchKey : DcmQR.PATIENT_STUDY_MATCHING_KEYS) {
				keys.add(DicomHeader.getHeaderName(matchKey));
			}
		}
		return keys;
	}

	/**
	 * reload the DICOMDIR file
	 * @param wantedModality 
	 * @param inFile
	 */
	public void query(int searchTag, String searchString, String wantedModality) {
		this.wantedModality = wantedModality;
		this.parent.getParentDialog().setWaitCursor(true);
		try {
			dq = new DicomQuery(this.url);
			this.patientTableModel.setPatients(dq.queryPatients(searchTag, searchString));
			this.studyTableModel.setStudies(new Vector<DicomObject>());
			this.seriesTableModel.setSeries(new Vector<DicomObject>());
			this.pictureTableModel.setPictures(new Vector<DicomObject>());
			
			this.patientTableBorder.setTitle(Translatrix.getTranslationString("dicom.Patients")+": [" + this.patientTableModel.getRowCount()+ "]");
			this.studyTableBorder.setTitle(Translatrix.getTranslationString("dicom.Studies")+": [" + this.studyTableModel.getRowCount()+ "]");
			this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series")+": [" + this.seriesTableModel.getRowCount()+ "]");
			this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures")+": [" + this.pictureTableModel.getRowCount()+ "]");
			
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error running DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
		sendButton.setEnabled(false);
		movelocalButton.setEnabled(false);
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
		} else if (e.getSource().equals(this.sendButton)) {
			this.moveTo(null);
		} else if (e.getSource().equals(this.movelocalButton)) {
			this.moveTo(localDest);
		}
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
			// study selected
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	 * sends the selected Files from the DICOMDIR
	 */
	public void moveTo(DcmURL p_url) {
		moveUrl = p_url;
		if (moveUrl == null)
			this.moveUrl = DicomURLChooser.showDialog(parent.getParentDialog(), senders);
		if (moveUrl == null )
			return;

		new Thread() {

			public void run() {

				moveAET = moveUrl.getCalledAET();
				
				parent.getParentDialog().setWaitCursor(true);
				
				// move images
				if (pictureTable.getSelectedRow() != -1) {
					try {
						dq = new DicomQuery(url);
						int[] rows = pictureTable.getSelectedRows();
						for (int i = 0; i < rows.length; i++) {
						    DicomObject image = pictureTableModel.getDimse(
									pictureTableSorter.modelIndex(rows[i]));
							dq.moveImage(parent.getParentDialog(), moveAET, image);				
						}
					} catch (Exception e) {
						ErrorDialog.showErrorDialog(parent.getParentDialog(), "Error on DICOM MOVE" , 
								e.getLocalizedMessage(), e);
						e.printStackTrace();
					}
				} else	
				// move series
				if (seriesTable.getSelectedRow() != -1) {
					try {
						dq = new DicomQuery(url);
						int[] rows = seriesTable.getSelectedRows();
						for (int i = 0; i < rows.length; i++) {
						    DicomObject series = seriesTableModel.getDimse(seriesTableSorter.modelIndex(seriesTable.getSelectedRow()));
							dq.moveSeries(parent.getParentDialog(), moveAET, series);
						}
					} catch (Exception e) {
						ErrorDialog.showErrorDialog(parent.getParentDialog(), "Error on DICOM MOVE" , 
								e.getLocalizedMessage(), e);
						e.printStackTrace();
					}
				} else
				// move studies
				if (studyTable.getSelectedRow() != -1) {
					try {
//						dq = new DicomQuery(url);
//						int[] rows = studyTable.getSelectedRows();
//						for (int i = 0; i < rows.length; i++) {
//						    DicomObject study = studyTableModel.getDimse(studyTableSorter.modelIndex(rows[i]));
//							Vector<DicomObject> serVect = dq.querySeriesByStudy(study, wantedModality);
//							for (Iterator<DicomObject> iter = serVect.iterator(); iter.hasNext();) {
//							    DicomObject series = (DicomObject) iter.next();
//								dq.moveSeries(parent.getParentDialog(), moveAET, series);							
//							}
//						}
					    
					    dq = new DicomQuery(url);
						int[] rows = studyTable.getSelectedRows();
						for (int i = 0; i < rows.length; i++) {
						    DicomObject series = studyTableModel.getDimse(studyTableSorter.modelIndex(studyTable.getSelectedRow()));
							dq.moveStudies(parent.getParentDialog(), moveAET, series);
						}
					    
					} catch (Exception e) {
						ErrorDialog.showErrorDialog(parent.getParentDialog(), "Error on DICOM MOVE" , 
								e.getLocalizedMessage(), e);
						e.printStackTrace();
					}
				} 

				parent.getParentDialog().setWaitCursor(false);
				
				
			}
		}.start();
		
	}
	
	/**
	 * closes the dialog
	 */
	public void cancel() {
		try {
			dq.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		parent.getParentDialog().setRetValue(JFileChooser.CANCEL_OPTION);
		parent.getParentDialog().setVisible(false);
		parent.getParentDialog().setWaitCursor(false);
	}
	

	public void mouseClicked(MouseEvent arg0) {
		if (this.parent.getParentDialog().pacsPanel != null) {
			this.parent.getParentDialog().pacsPanel.setServerStatus(PACSPanel.SERVER_RESET);
		}
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
	private void selectPatient() {
		try {
			dq = new DicomQuery(this.url);
			if (this.patientTable.getSelectedRow() == -1)
				return;
			
			sendButton.setEnabled(false);
			movelocalButton.setEnabled(false);
			this.studyTableModel.setStudies(dq.queryStudiesByPatient(this.patientTableModel.getDimse(this.patientTableSorter.modelIndex(this.patientTable.getSelectedRow()))));
			this.seriesTableModel.setSeries(new Vector<DicomObject>());
			this.pictureTableModel.setPictures(new Vector<DicomObject>());
			
			this.studyTableBorder.setTitle(Translatrix.getTranslationString("dicom.Studies")+": [" + this.studyTableModel.getRowCount()+ "]");
			this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series")+": [" + this.seriesTableModel.getRowCount()+ "]");
			this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures")+": [" + this.pictureTableModel.getRowCount()+ "]");
			
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error on DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	private void selectStudy() {
		try {
			dq = new DicomQuery(this.url);
			if (this.studyTable.getSelectedRow() == -1)
				return;
			
			sendButton.setEnabled(true);
			movelocalButton.setEnabled(true);
			this.seriesTableModel.setSeries(dq.querySeriesByStudy(this.studyTableModel.getDimse(this.studyTableSorter.modelIndex(this.studyTable.getSelectedRow())), wantedModality));
			this.pictureTableModel.setPictures(new Vector<DicomObject>());
//			this.seriesTable.setRowSelectionInterval(-1,-1);
//			this.pictureTable.setRowSelectionInterval(-1,-1);
			this.seriesTableBorder.setTitle(Translatrix.getTranslationString("dicom.Series")+": [" + this.seriesTableModel.getRowCount()+ "]");
			this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures")+": [" + this.pictureTableModel.getRowCount()+ "]");
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error on DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	private void selectSeries() {
		try {
			dq = new DicomQuery(this.url);
			if (this.seriesTable.getSelectedRow() == -1)
				return;
			
			sendButton.setEnabled(true);
			movelocalButton.setEnabled(true);
			Vector<DicomObject> pictures = dq.queryPicturesBySeries(this.seriesTableModel.getDimse(this.seriesTableSorter.modelIndex(this.seriesTable.getSelectedRow())));
			this.pictureTableModel.setPictures(pictures);
			this.pictureTableBorder.setTitle(Translatrix.getTranslationString("dicom.Pictures")+": [" + this.pictureTableModel.getRowCount()+ "]");
			
//			this.pictureTable.setRowSelectionInterval(-1,-1);
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error on DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	private void selectImages() {
		
		if (this.pictureTable.getSelectedRow() == -1)
			return;
		
		try {
			sendButton.setEnabled(true);
			movelocalButton.setEnabled(true);
		} catch (Exception e1) {
			ErrorDialog.showErrorDialog(this.parent.getParentDialog(), "Error on DICOM QUERY" , 
					e1.getLocalizedMessage() + "", e1);
			logger.warning("error updating dicom view");
			this.parent.getParentDialog().setWaitCursor(false);
		}
	}
	
	
	public static void main(String[] args) {
		
		String url = "dicom://" + "dicomserver";
    	url = url  + "@" +"localhost" + ":"+ "5104"; 
		DcmURL dcmUrl = new DcmURL(url);
		
		JFrame frame = new JFrame();
		QuerySearchPanel qsPanel = new QuerySearchPanel(null, dcmUrl);
		frame.getContentPane().add(qsPanel);
		frame.setSize(600,500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
	}

	public void setDicomSenders(DcmURL[] senders) {
		this.senders = senders;
	}
	
	public void setLocalDest(DcmURL localDest) {
		this.localDest = localDest;
	}
	
}
