package lu.tudor.santec.dicom.gui.header;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RootPaneContainer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TagSearchPanel extends JPanel implements ListSelectionListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private DicomHeaderTableModel tagTableModel;
	private JTable tagTable;
	private JScrollPane jsp;
	private JTextField nrFilter;
	private JTextField nameFilter;
	private Vector<TagListener> tagListeners = new Vector<TagListener>();
	private HeaderTag ht;
	private JButton loadTemplateFile;
	private JButton loadAllTags;
	private DicomFileDialog dicomChooser;
	private RootPaneContainer parentDialog;;

	public TagSearchPanel() {
		this(null, null);
	}

	public TagSearchPanel(RootPaneContainer parent, DicomFileDialog dicomChooser) {
		this.parentDialog = parent;
		this.dicomChooser = dicomChooser;

		this.setOpaque(false);
		this.setLayout(new BorderLayout());

		CellConstraints cc = new CellConstraints();
		JPanel filterPanel = new JPanel(new FormLayout("105px, 3dlu, 200px:grow", "pref"));

		nrFilter = new JTextField();
		filterPanel.add(nrFilter, cc.xy(1, 1));
		nameFilter = new JTextField();
		filterPanel.add(nameFilter, cc.xy(3, 1));

		KeyListener filterListener = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				filterTable();
			}
		};

		nrFilter.addKeyListener(filterListener);
		nameFilter.addKeyListener(filterListener);

		this.add(filterPanel, BorderLayout.NORTH);

		this.tagTableModel = new DicomHeaderTableModel(HeaderTag.getAllTags(), false, false);
		this.tagTable = new JTable(this.tagTableModel);
		this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
		this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));
		this.tagTable.getSelectionModel().addListSelectionListener(this);

		jsp = new JScrollPane(this.tagTable);
		jsp.setOpaque(false);
		jsp.getViewport().setOpaque(false);
		this.add(jsp, BorderLayout.CENTER);

		ButtonBarBuilder bb = new ButtonBarBuilder();

		loadTemplateFile = new JButton(Translatrix.getTranslationString("TagSelectorDialog.loadTemplate"), DicomIcons.getIcon16(DicomIcons.OPEN_FILE));
		loadTemplateFile.addActionListener(this);
		bb.addFixed(loadTemplateFile);
		bb.addRelatedGap();

		loadAllTags = new JButton(Translatrix.getTranslationString("TagSelectorDialog.loadAllTags"), DicomIcons.getIcon16(DicomIcons.ACTION_HEADER));
		loadAllTags.addActionListener(this);
		bb.addFixed(loadAllTags);
		bb.addUnrelatedGap();

		if (this.dicomChooser != null) {
			JPanel buttonPanel = bb.getPanel();
			buttonPanel.setOpaque(false);
			this.add(buttonPanel, BorderLayout.SOUTH);
		}

	}

	public void filterTable() {
		tagTableModel.getNrFilter().setFilter(nrFilter.getText());
		tagTableModel.setFilteringEnabled((nrFilter.getText().length() > 0), 1);

		tagTableModel.getNameFilter().setFilter(nameFilter.getText());
		tagTableModel.setFilteringEnabled((nameFilter.getText().length() > 0), 2);

		tagTableModel.fireTableDataChanged();
	}

	private void loadAllTags() {
		this.tagTableModel.setShowValue(false);
		this.tagTableModel.setTags(HeaderTag.getAllTags());
		this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
		this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));

	}

	private void loadTemplateFile() {
		if (this.dicomChooser.showNewDialog(parentDialog) == JFileChooser.APPROVE_OPTION) {
			File f = this.dicomChooser.getSelectedFile();
			DicomHeader dh = new DicomHeader(f);
			this.tagTableModel.setShowValue(true);

			this.tagTableModel.setTags(dh.getHeaderTagsAsFlatList());
			this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
			this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(this.loadTemplateFile)) {
			loadTemplateFile();
		} else if (e.getSource().equals(this.loadAllTags)) {
			loadAllTags();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(this.tagTable.getSelectionModel()) && (!e.getValueIsAdjusting())) {
			HeaderTag ht = this.tagTableModel.getHeaderTag(this.tagTable.getSelectedRow());
			// System.out.println(ht);
			this.tagSelected(ht);
		}
	}

	public void setTag(HeaderTag ht) {
		this.ht = ht;
		if (ht != null) {
			nrFilter.setText(ht.getTagNr());
		} else {
			nrFilter.setText("");
		}
		nameFilter.setText("");
		filterTable();
	}

	public HeaderTag getTag() {
		return ht;
	}

	private void tagSelected(HeaderTag ht) {
		this.ht = ht;
		for (TagListener tagListener : tagListeners) {
			tagListener.tagChanged(ht);
		}
	}

	public void addTagListener(TagListener tagListener) {
		this.tagListeners.add(tagListener);
	}

	public void removeTagListener(TagListener tagListener) {
		this.tagListeners.remove(tagListener);
	}

  public void setEditable(boolean editable) {
		nrFilter.setEnabled(editable);
		nameFilter.setEnabled(editable);
		tagTable.setEnabled(editable);
  }

}
