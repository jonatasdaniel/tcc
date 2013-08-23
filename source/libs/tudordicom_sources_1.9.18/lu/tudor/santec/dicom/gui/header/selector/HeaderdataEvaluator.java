package lu.tudor.santec.dicom.gui.header.selector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.MemoryMonitorButton;
import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.DicomHeaderTableModel;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.header.filter.TagFilterDialog;
import lu.tudor.santec.dicom.gui.header.selector.dnd.DicomHeaderTableDropTargetListener;
import lu.tudor.santec.dicom.gui.header.selector.dnd.HeaderTagTransferHandler;
import lu.tudor.santec.dicom.gui.viewer.Viewer;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class HeaderdataEvaluator extends JDialog implements ListSelectionListener, MouseListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private DicomHeaderTableModel tagTableModel;
	private JTable tagTable;
	private DicomHeaderTableModel selectedTableModel;
	private JTable selectedTable;
	private JTextField nrFilter;
	private JTextField nameFilter;
	private JScrollPane jsp;
	private JButton closeButton;
	private JButton runButton;
	private JButton loadFilesButton;
	private JTextField openField;
	private HeaderDataExtractor headerDataExtractor;
	private DicomFileDialog dicomChooser;
	private DCMResultDialog dcmResultDialog;
	private DicomHeaderTableModel filterTableModel;
	private JTable filterTable;
	private JButton addFilterButton;
	private JButton deleteFilterButton;
	private TagFilterDialog tagFilterDialog;
	private JButton editFilterButton;
	private int rowFrom;
	private int rowTo;
	private JButton loadTemplateFile;
	private JButton loadAllTags;
	private Vector<HeaderTag> usedTags;
	private Vector<HeaderTag> filterTags;
	private JButton saveConfig;
	private JButton loadConfig;
	private JFileChooser fileChooser;
	private File[] files;
	private Properties properties = new Properties();
	public Viewer dicomViewer;
	private JPopupMenu popup;
	private MouseEvent event;
	private JToggleButton saveToFile;

	public static final String FILENAME = "HeaderdataEvaluator.zip";
	private static final Object IMAGE_PATH = "IMAGE_PATH";

	public HeaderdataEvaluator(JFrame parent, DicomFileDialog dicomChooser) {
		super(parent);
		initDialog(dicomChooser);
		this.setLocationRelativeTo(parent);
	}

	public HeaderdataEvaluator(JDialog parent, DicomFileDialog dicomChooser) {
		super(parent);
		initDialog(dicomChooser);
		this.setLocationRelativeTo(parent);
	}

	private void initDialog(DicomFileDialog dicomChooser) {
		this.dicomChooser = dicomChooser;
		if (this.dicomChooser == null) {
			this.dicomChooser = DicomFileDialog.getDicomFileDialog();
		}

		this.fileChooser = new JFileChooser();

		this.setTitle(Translatrix.getTranslationString("TagSelectorDialog.title"));
		// this.setModal(true);

		this.setLayout(new FormLayout("fill:200dlu, 5dlu, fill:250dlu:grow", "pref, 3dlu, pref, 3dlu, fill:150dlu:grow, 3dlu, pref, 3dlu, fill:150dlu:grow, 3dlu, pref"));
		CellConstraints cc = new CellConstraints();

		this.tagFilterDialog = new TagFilterDialog(this, this.dicomChooser);

		JPanel selectFilesPanel = new JPanel(new FormLayout("pref,pref:grow,pref,pref","2dlu, fill:pref, 2dlu"));
		selectFilesPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		selectFilesPanel.setBackground(new Color(200, 255, 200));
		selectFilesPanel.add(new JLabel(Translatrix.getTranslationString("TagSelectorDialog.selectFiles")), cc.xy(1,2));

		openField = new JTextField(40);
		openField.setEditable(false);
		openField.setOpaque(false);
		selectFilesPanel.add(openField, cc.xy(2,2));

		loadFilesButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.open"), DicomIcons.getIcon22(DicomIcons.PACS));
		loadFilesButton.addActionListener(this);
		selectFilesPanel.add(loadFilesButton, cc.xy(3,2));
		
		selectFilesPanel.add(new MemoryMonitorButton(true, true), cc.xy(4,2));

		this.add(selectFilesPanel, cc.xyw(1, 1, 3));

		this.tagTableModel = new DicomHeaderTableModel(HeaderTag.getAllTags(), false, false);
		this.tagTable = new JTable(this.tagTableModel);
		this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
		this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));
		this.tagTable.getSelectionModel().addListSelectionListener(this);
		this.tagTable.addMouseListener(this);
		this.tagTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// enable dragging from tags table
		this.tagTable.setDragEnabled(true);
		this.tagTable.setTransferHandler(new HeaderTagTransferHandler(tagTableModel));

		JPanel searchPanel = new JPanel(new FormLayout("105px, 3dlu, 200px:grow", "pref"));

		nrFilter = new JTextField();
		searchPanel.add(nrFilter, cc.xy(1, 1));
		nameFilter = new JTextField();
		searchPanel.add(nameFilter, cc.xy(3, 1));

		KeyListener filterListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				filterTable();
			}
		};

		nrFilter.addKeyListener(filterListener);
		nameFilter.addKeyListener(filterListener);

		this.add(searchPanel, cc.xy(1, 3));

		jsp = new JScrollPane(this.tagTable);
		jsp.setOpaque(false);
		jsp.getViewport().setOpaque(false);
		this.add(jsp, cc.xywh(1, 5, 1, 5));

		this.add(new JLabel(Translatrix.getTranslationString("TagSelectorDialog.selectedTags"), DicomIcons.getIcon22(DicomIcons.SEARCH), JLabel.LEFT), cc.xy(3, 3));

		
		
		
		
		this.selectedTableModel = new DicomHeaderTableModel(null, false, true);
		this.selectedTable = new JTable(this.selectedTableModel);
		this.selectedTable.getColumnModel().getColumn(0).setMaxWidth(this.selectedTableModel.getColumnSize(0));
		// this.selectedTable.getColumnModel().getColumn(1).setMaxWidth(this.selectedTableModel.getColumnSize(1));
		this.selectedTable.getSelectionModel().addListSelectionListener(this);
		this.selectedTable.addMouseListener(this);
		this.selectedTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		JScrollPane selectedScrollPane = new JScrollPane(this.selectedTable);
		selectedScrollPane.setOpaque(false);
		selectedScrollPane.getViewport().setOpaque(false);
		this.add(selectedScrollPane, cc.xy(3, 5));

		// enable dropping onto selected tags table
		DicomHeaderTableDropTargetListener selectedDropListener = new DicomHeaderTableDropTargetListener(this.selectedTable, this.selectedTableModel);
		new DropTarget(this.selectedTable, selectedDropListener);
		selectedScrollPane.setDropTarget(new DropTarget(selectedScrollPane, selectedDropListener));

		// enable shifting of lines in the table
		this.selectedTable.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent evt) {
				TableMouseDragged(evt);
			}
		});
		this.selectedTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent evt) {
				TableMousePressed(evt);
			}
		});

		this.selectedTable.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					int[] rows = selectedTable.getSelectedRows();
					HeaderTag[] hts = new HeaderTag[rows.length];
					for (int i = 0; i < rows.length; i++) {
						hts[i] = selectedTableModel.getHeaderTag(rows[i]);
					}
					for (int i = 0; i < hts.length; i++) {
						selectedTableModel.removeTag(hts[i]);
					}
				} 
			}
		});

		
		this.selectedTable.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			
			
			
		});
		
		JPanel filterPanel = new JPanel(new BorderLayout(3, 3));
		filterPanel.setBackground(new Color(200, 200, 255));
		filterPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		filterPanel.add(new JLabel(Translatrix.getTranslationString("TagSelectorDialog.filterTags"), DicomIcons.getIcon22(DicomIcons.OPEN_SELECTOR), JLabel.LEFT), BorderLayout.NORTH);

		this.filterTableModel = new DicomHeaderTableModel(null, true, false);
		this.filterTable = new JTable(this.filterTableModel);
		this.filterTable.getColumnModel().getColumn(0).setMaxWidth(this.filterTableModel.getColumnSize(0));
		// this.filterTable.getColumnModel().getColumn(1).setMaxWidth(this.filterTableModel.getColumnSize(1));
		this.filterTable.getSelectionModel().addListSelectionListener(this);
		this.filterTable.addMouseListener(this);
		JScrollPane filterScrollPane = new JScrollPane(this.filterTable);
		filterScrollPane.setOpaque(false);
		filterScrollPane.getViewport().setOpaque(false);
		filterPanel.add(filterScrollPane, BorderLayout.CENTER);

		this.filterTable.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteFilter();
				}
			}
		});

		// enable dropping onto filter tags table
		DicomHeaderTableDropTargetListener filterDropListener = new DicomHeaderTableDropTargetListener(this.filterTable, this.filterTableModel);
		new DropTarget(this.filterTable, filterDropListener);
		filterScrollPane.setDropTarget(new DropTarget(filterScrollPane, filterDropListener));

		ButtonBarBuilder filterButtons = new ButtonBarBuilder();

		deleteFilterButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.deleteFilterButton"), DicomIcons.getIcon16(DicomIcons.FILTER_REMOVE));
		deleteFilterButton.addActionListener(this);
		deleteFilterButton.setEnabled(false);
		filterButtons.addGridded(deleteFilterButton);
		filterButtons.addRelatedGap();
		filterButtons.addGlue();

		editFilterButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.editFilterButton"), DicomIcons.getIcon16(DicomIcons.FILTER_EDIT));
		editFilterButton.addActionListener(this);
		editFilterButton.setEnabled(false);
		filterButtons.addGridded(editFilterButton);
		filterButtons.addRelatedGap();

		addFilterButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.addFilterButton"), DicomIcons.getIcon16(DicomIcons.FILTER_ADD));
		addFilterButton.addActionListener(this);
		filterButtons.addGridded(addFilterButton);

		JPanel filterButtonPanel = filterButtons.getPanel();
		filterButtonPanel.setOpaque(false);
		filterPanel.add(filterButtonPanel, BorderLayout.SOUTH);

		this.add(filterPanel, cc.xy(3, 9));

		ButtonBarBuilder bb = new ButtonBarBuilder();

		loadTemplateFile = new JButton(Translatrix.getTranslationString("TagSelectorDialog.loadTemplate"), DicomIcons.getIcon32(DicomIcons.OPEN_FILE));
		loadTemplateFile.addActionListener(this);
		bb.addFixed(loadTemplateFile);
		bb.addRelatedGap();

		loadAllTags = new JButton(Translatrix.getTranslationString("TagSelectorDialog.loadAllTags"), DicomIcons.getIcon32(DicomIcons.ACTION_HEADER));
		loadAllTags.addActionListener(this);
		bb.addFixed(loadAllTags);
		bb.addUnrelatedGap();
		
		saveToFile = new JToggleButton(Translatrix.getTranslationString("TagSelectorDialog.saveToFile"), DicomIcons.getIcon32(DicomIcons.ICON_DOWN));
		saveToFile.addActionListener(this);
		bb.addFixed(saveToFile);
		bb.addUnrelatedGap();

		bb.addGlue();

		// JPanel configPanel = new JPanel(new GridLayout(0,1));

		loadConfig = new JButton(DicomIcons.getIcon32(DicomIcons.CONFIG_LOAD));
		loadConfig.setToolTipText(Translatrix.getTranslationString("TagSelectorDialog.loadConfig"));
		loadConfig.addActionListener(this);
		// loadConfig.setMargin(new Insets(0,0,0,0));
		// loadConfig.setBorderPainted(false);
		// loadConfig.setContentAreaFilled( false );

		bb.addFixed(loadConfig);

		saveConfig = new JButton(DicomIcons.getIcon32(DicomIcons.CONFIG_SAVE));
		saveConfig.setToolTipText(Translatrix.getTranslationString("TagSelectorDialog.saveConfig"));
		saveConfig.addActionListener(this);
		// saveConfig.setMargin(new Insets(0,0,0,0));
		// saveConfig.setBorderPainted(false);
		// saveConfig.setContentAreaFilled( false );
		bb.addFixed(saveConfig);

		// bb.addFixed(configPanel);

		bb.addRelatedGap();

		runButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.run"), DicomIcons.getIcon32(DicomIcons.VIDEO_PLAY));
		runButton.addActionListener(this);
		runButton.setEnabled(false);

		bb.addRelatedGap();

		closeButton = new JButton(Translatrix.getTranslationString("TagSelectorDialog.close"), DicomIcons.getIcon32(DicomIcons.CLOSE));
		closeButton.addActionListener(this);
		bb.addFixed(closeButton);

		bb.addFixed(runButton);

		JPanel buttonPanel = bb.getPanel();
		this.add(buttonPanel, cc.xyw(1, 11, 3));

		this.headerDataExtractor = new HeaderDataExtractor(this);

		this.dcmResultDialog = new DCMResultDialog(this);

		this.pack();

	}

	protected void filterTable() {
		tagTableModel.getNrFilter().setFilter(nrFilter.getText());
		tagTableModel.setFilteringEnabled((nrFilter.getText().length() > 0), 1);

		tagTableModel.getNameFilter().setFilter(nameFilter.getText());
		tagTableModel.setFilteringEnabled((nameFilter.getText().length() > 0), 2);

		tagTableModel.fireTableDataChanged();
	}

	public Vector<HeaderTag> showDialog(Vector<HeaderTag> usedTagss) {

		loadTags(new File(FILENAME));

		if (usedTagss != null) {
			this.usedTags = usedTagss;
		}

		this.selectedTableModel.setTags(this.usedTags);
		this.filterTableModel.setTags(this.filterTags);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				filterTags = filterTableModel.getHeaderTags();
				usedTags = selectedTableModel.getHeaderTags();
				saveTags(new File(FILENAME));
			}
		});

		this.setVisible(true);

		return selectedTableModel.getHeaderTags();

	}

	private void saveTags(File file) {
		try {
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
			zout.putNextEntry(new ZipEntry("TagSelectorDialog.tags"));
			HeaderTag.saveTags(zout, this.usedTags);
			zout.putNextEntry(new ZipEntry("TagSelectorDialogFilter.tags"));
			HeaderTag.saveTags(zout, this.filterTags);

			zout.putNextEntry(new ZipEntry("Settings.properties"));
			properties.put(IMAGE_PATH, openField.getText());
			properties.store(zout, "");

			zout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadTags(File file) {
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals("TagSelectorDialog.tags")) {
					this.usedTags = HeaderTag.loadTags(zis);
				} else if (entry.getName().equals("TagSelectorDialogFilter.tags")) {
					this.filterTags = HeaderTag.loadTags(zis);
				} else if (entry.getName().equals("Settings.properties")) {
					properties.load(zis);
					try {
						File f = new File(properties.getProperty((String) IMAGE_PATH));
						if (f.canRead()) {
							this.openField.setText(f.getAbsolutePath());
							this.dicomChooser.setSelectedFile(f);
							this.files = this.dicomChooser.getSelectedFiles();
							runButton.setEnabled(true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			zis.close();
		} catch (FileNotFoundException e) {
			System.err.println("Headerdata Evaluator settings not found at: " + file + " ...creating next time...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(this.tagTable.getSelectionModel()) && (!e.getValueIsAdjusting())) {
			// HeaderTag ht =
			// this.tagTableModel.getHeaderTag(this.tagTable.getSelectedRow());
		} else if (e.getSource().equals(this.filterTable.getSelectionModel()) && (!e.getValueIsAdjusting())) {
			if (this.filterTable.getSelectedRow() == -1) {
				this.deleteFilterButton.setEnabled(false);
				this.editFilterButton.setEnabled(false);
			} else {
				this.deleteFilterButton.setEnabled(true);
				this.editFilterButton.setEnabled(true);
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource().equals(this.tagTable)) {
			// add tag
			if (e.getClickCount() >= 2) {
				HeaderTag ht = this.tagTableModel.getHeaderTag(this.tagTable.getSelectedRow());
				this.selectedTableModel.addTag(ht);
			}

		} else if (e.getSource().equals(this.selectedTable)) {
//			// remove tag
//			if (e.getClickCount() >= 2) {
//				HeaderTag ht = this.selectedTableModel.getHeaderTag(this.selectedTable.getSelectedRow());
//				this.selectedTableModel.removeTag(ht);
//			}

		} else if (e.getSource().equals(this.filterTable)) {
			if (e.getClickCount() >= 2)
				editFilter();
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.closeButton)) {
			this.setVisible(false);
		} else if (e.getSource().equals(this.loadFilesButton)) {
			loadFiles();
		} else if (e.getSource().equals(this.addFilterButton)) {
			addFilter();
		} else if (e.getSource().equals(this.deleteFilterButton)) {
			deleteFilter();
		} else if (e.getSource().equals(this.editFilterButton)) {
			editFilter();
		} else if (e.getSource().equals(this.runButton)) {
			runSearch();
		} else if (e.getSource().equals(this.loadTemplateFile)) {
			loadTemplateFile();
		} else if (e.getSource().equals(this.loadAllTags)) {
			loadAllTags();
		} else if (e.getSource().equals(this.loadConfig)) {
			if (this.fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				loadTags(this.fileChooser.getSelectedFile());
				this.selectedTableModel.setTags(this.usedTags);
				this.filterTableModel.setTags(this.filterTags);
			}
		} else if (e.getSource().equals(this.saveConfig)) {
			this.fileChooser.setSelectedFile(new File(FILENAME));
			if (this.fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				this.filterTags = filterTableModel.getHeaderTags();
				this.usedTags = selectedTableModel.getHeaderTags();
				saveTags(this.fileChooser.getSelectedFile());
			}
		}

	}

	private void loadAllTags() {
		this.tagTableModel.setShowValue(false);
		this.tagTableModel.setTags(HeaderTag.getAllTags());
		this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
		this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));

	}

	private void loadTemplateFile() {
		if (this.dicomChooser.showNewDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = this.dicomChooser.getSelectedFile();
			DicomHeader dh = new DicomHeader(f);
			this.tagTableModel.setShowValue(true);

			this.tagTableModel.setTags(dh.getHeaderTagsAsFlatList());
			this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
			this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));
		}
	}

	private void addFilter() {
		do {
			HeaderTag ht = this.tagFilterDialog.selectTag(null);
			if (ht != null)
				this.filterTableModel.addTag(ht);
		} while(this.tagFilterDialog.isAddNext());
	}

	private void deleteFilter() {
		int[] rows = this.filterTable.getSelectedRows();
		HeaderTag[] hts = new HeaderTag[rows.length];
		for (int i = 0; i < rows.length; i++) {
			hts[i] = this.filterTableModel.getHeaderTag(rows[i]);
		}
		for (int i = 0; i < hts.length; i++) {
			this.filterTableModel.removeTag(hts[i]);
		}
	}

	private void editFilter() {
		int row = this.filterTable.getSelectedRow();
		HeaderTag ht = this.filterTableModel.getHeaderTag(row);
		if (ht != null)
			ht = this.tagFilterDialog.selectTag(ht);
		this.filterTableModel.fireTableRowsUpdated(row, row);
	}

	private void runSearch() {
		new Thread() {
			public void run() {
				runButton.setEnabled(false);
				if (saveToFile.isSelected()) {
					File f = DCMResultDialog.showExportDialog(HeaderdataEvaluator.this, "export.csv");
					if (f != null) {
						inspectFiles(f);
					}
				} else {					
					inspectFiles(null);
				}
				
				runButton.setEnabled(true);
			}
		}.start();
	}

	private void loadFiles() {
		if (this.dicomChooser.showNewDialog(this) == JFileChooser.APPROVE_OPTION) {
			this.openField.setText(this.dicomChooser.getSelectedFiles()[0].getParent());
			this.files = this.dicomChooser.getSelectedFiles();
			runButton.setEnabled(true);
		}
	}

	private void inspectFiles(File f) {
		try {
			this.headerDataExtractor.setHeaderTags(this.selectedTableModel.getHeaderTags());
			this.headerDataExtractor.setFilterTags(this.filterTableModel.getHeaderTags());

			ArrayList<File> al = new ArrayList<File>();

			for (int i = 0; i < this.files.length; i++) {
				al.add(this.files[i]);
			}
			Vector<String[]> results = this.headerDataExtractor.workFiles(al, f, true);
			if (f == null) {
				dcmResultDialog.showResult(this.headerDataExtractor.createColumns(true), this.headerDataExtractor.createColumnTooltips(true), results);				
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	private void TableMousePressed(MouseEvent evt) {
		rowFrom = this.selectedTable.rowAtPoint(evt.getPoint());
	}

	private void TableMouseDragged(MouseEvent evt) {
		rowTo = this.selectedTable.rowAtPoint(evt.getPoint());
		if (rowTo != rowFrom && rowTo > -1 && rowTo < this.selectedTable.getRowCount()) {
			this.selectedTableModel.moveRow(rowFrom, rowTo);
			rowFrom = rowTo;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

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
		new HeaderdataEvaluator(new JFrame(), null).showDialog(null);
//		System.exit(0);
	}

	public void setViewer(Viewer viewer) {
		this.dicomViewer = viewer;
	}
	
	
	/**
	 * show a menu on right click
	* @param c
	 * @param x
	 * @param y
	 */
	private void showMenu(final MouseEvent e) {
		this.event = e;
		if (popup == null) {
			popup = new JPopupMenu();
			popup.add(new AbstractAction(Translatrix.getTranslationString("TagSelectorDialog.deleteTags")) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent ae) {
					JTable table = (JTable) event.getSource();
					//	get the current rows
					int[] selectedRows = table.getSelectedRows();
					DicomHeaderTableModel model = (DicomHeaderTableModel) table.getModel();
					Vector<HeaderTag> tags = new Vector<HeaderTag>();
					for (int i : selectedRows) {
						tags.add(model.getHeaderTag(i));
					}
					for (HeaderTag headerTag : tags) {						
						model.removeTag(headerTag);
					}
					
				}
			});
			popup.add(new AbstractAction(Translatrix.getTranslationString("TagSelectorDialog.addTag")) {
                private static final long serialVersionUID = 1L;
                public void actionPerformed(ActionEvent ae) {
                	JTable table = (JTable) event.getSource();
                	DicomHeaderTableModel model = (DicomHeaderTableModel) table.getModel();
					model.addTag(new HeaderTag("","",""));
					table.scrollRectToVisible(table.getCellRect(table.getRowCount()-1, 1, false));
					table.editCellAt(table.getRowCount()-1, 1);
                }
			});
		}
		JTable table = (JTable) event.getSource();
		int row = table.rowAtPoint(event.getPoint());
		if (row < 0) 
			return;
		
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows == null || selectedRows.length == 0) {
		    table.setRowSelectionInterval(row, row);					    
		}
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

}
