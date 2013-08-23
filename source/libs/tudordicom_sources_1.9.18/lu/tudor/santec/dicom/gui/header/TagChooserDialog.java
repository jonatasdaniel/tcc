package lu.tudor.santec.dicom.gui.header;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import lu.tudor.santec.dicom.gui.header.DicomHeaderTableModel;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TagChooserDialog extends JDialog implements ListSelectionListener, ActionListener {

    private static final long serialVersionUID = 1L;
    private DicomHeaderTableModel tagTableModel;
    private JTable tagTable;
    private JPanel mainPanel;
    private JTextField nrFilter;
    private JTextField nameFilter;
    private JScrollPane jsp;
    private JButton closeButton;
    private JButton okButton;
    private HeaderTag ht;
    private Component parent;

    public TagChooserDialog(JFrame parent) {
	super(parent, true);
	this.parent = parent;
	initDialog();
	setLocationRelativeTo(parent);
    }
    
    public TagChooserDialog(JDialog parent) {
	super(parent, true);
	this.parent = parent;
	initDialog();
	setLocationRelativeTo(parent);
    }
    
    public TagChooserDialog() {
	initDialog();
    }
    
    private void initDialog() {
	this.setTitle(Translatrix.getTranslationString("FilterTagDialog.title"));
	
	this.setLayout(new BorderLayout());
	
	this.mainPanel = new JPanel(new FormLayout(
		"fill:200dlu, 5dlu,",
		"pref, 3dlu, top:200dlu:grow"));
	CellConstraints cc = new CellConstraints();
	
	
	this.tagTableModel = new DicomHeaderTableModel(HeaderTag.getAllTags(), false, false); 
	this.tagTable = new JTable(this.tagTableModel);
	this.tagTable.getColumnModel().getColumn(0).setMaxWidth(this.tagTableModel.getColumnSize(0));
	this.tagTable.getColumnModel().getColumn(1).setMaxWidth(this.tagTableModel.getColumnSize(1));
	this.tagTable.getSelectionModel().addListSelectionListener(this);
	
	this.tagTable.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2) {
		    ht = tagTableModel.getHeaderTag(tagTable.getSelectedRow());
		    setVisible(false);
		}
	    }
	});
	
	
	JPanel filterPanel = new JPanel(new FormLayout(
		"105px, 3dlu, 200px:grow",
		"pref"
		));
	
	nrFilter= new JTextField();
	filterPanel.add(nrFilter, cc.xy(1,1));
	nameFilter = new JTextField();
	filterPanel.add(nameFilter, cc.xy(3,1));
	
	KeyListener filterListener = new KeyAdapter() {
	    public void keyReleased(KeyEvent e) {
		filterTable();
	    }
	};
	
	nrFilter.addKeyListener(filterListener);
	nameFilter.addKeyListener(filterListener);
	
	this.mainPanel.add(filterPanel, cc.xy(1,1));
	
	jsp = new JScrollPane(this.tagTable);
	jsp.setOpaque(false);
	jsp.getViewport().setOpaque(false);
	this.mainPanel.add(jsp, cc.xy(1,3));
	
	this.add(this.mainPanel, BorderLayout.CENTER);
	
	ButtonBarBuilder bb = new ButtonBarBuilder();
	bb.addGlue();
	
	this.closeButton = new JButton(Translatrix.getTranslationString("dicom.Cancel"));
	this.closeButton.addActionListener(this);
	bb.addGridded(this.closeButton);
	bb.addRelatedGap();
	
	
	this.okButton = new JButton(Translatrix.getTranslationString("dicom.OK"));
	this.okButton.addActionListener(this);
	bb.addGridded(this.okButton);
	
	this.add(bb.getPanel(), BorderLayout.SOUTH);
	
	this.pack();
    }
    
    
    protected void filterTable() {
	tagTableModel.getNrFilter().setFilter(nrFilter.getText());
	tagTableModel.setFilteringEnabled((nrFilter.getText().length() > 0), 1);
	
	tagTableModel.getNameFilter().setFilter(nameFilter.getText());
	tagTableModel.setFilteringEnabled((nameFilter.getText().length() > 0), 2);
	
	tagTableModel.fireTableDataChanged();
    }


    public HeaderTag selectTag(HeaderTag ht) {
	this.ht = ht;
	if (ht != null) {
	    nrFilter.setText(ht.getTagNr());
	    nameFilter.setText("");
	    filterTable();
	}
	
	if (parent != null)
	    setLocationRelativeTo(parent);
	
	this.setVisible(true);
	return this.ht;
    }
    

    public void valueChanged(ListSelectionEvent e) {
	if (e.getSource().equals(this.tagTable.getSelectionModel()) && (! e.getValueIsAdjusting())) {
	    ht = this.tagTableModel.getHeaderTag(this.tagTable.getSelectedRow());
	}
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.okButton)) {
	    ht = this.tagTableModel.getHeaderTag(this.tagTable.getSelectedRow());
	} else if (e.getSource().equals(this.closeButton)) {
	    ht = null;
	} 
	setVisible(false);
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
	new TagChooserDialog().selectTag(null);
    }
}
