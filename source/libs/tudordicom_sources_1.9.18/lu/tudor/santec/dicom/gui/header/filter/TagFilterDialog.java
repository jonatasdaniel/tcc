package lu.tudor.santec.dicom.gui.header.filter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import lu.tudor.santec.dicom.gui.DicomFileDialog;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.header.TagListener;
import lu.tudor.santec.dicom.gui.header.TagSearchPanel;
import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TagFilterDialog extends JDialog implements ActionListener, TagListener {

    private static final long serialVersionUID = 1L;
    private JPanel mainPanel;
    private TagFilterPanel filterTagPanel;
    private JButton closeButton;
    private JButton saveButton;
    private Component parent;
	private JButton saveAndAddButton;
	private boolean addNext;
	private TagSearchPanel tagSearchPanel;
	private HeaderTag ht;
	private DicomFileDialog dicomChooser;

    public TagFilterDialog(JFrame parent) {
	super(parent, true);
	this.parent = parent;
	initDialog();
	setLocationRelativeTo(parent);
    }
    
    public TagFilterDialog(JDialog parent, DicomFileDialog dicomChooser) {
	super(parent, true);
	this.parent = parent;
	this.dicomChooser = dicomChooser;
	this.
	initDialog();
	setLocationRelativeTo(parent);
    }
    
    public TagFilterDialog() {
	initDialog();
    }
    
    private void initDialog() {
	this.setTitle(Translatrix.getTranslationString("FilterTagDialog.title"));
	
	this.setLayout(new BorderLayout());
	
	this.mainPanel = new JPanel(new FormLayout(
		"fill:200dlu, 5dlu, pref:grow, 5dlu",
		"pref, 3dlu, top:200dlu:grow"));
	CellConstraints cc = new CellConstraints();
	
	
	tagSearchPanel = new TagSearchPanel(this, this.dicomChooser);
	tagSearchPanel.addTagListener(this);
	this.mainPanel.add(tagSearchPanel, cc.xy(1,3));

	this.filterTagPanel = new TagFilterPanel();
	this.mainPanel.add(filterTagPanel, cc.xy(3,3));
	
	this.add(this.mainPanel, BorderLayout.CENTER);
	
	ButtonBarBuilder bb = new ButtonBarBuilder();
	bb.addGlue();
	
	this.closeButton = new JButton(Translatrix.getTranslationString("dicom.Cancel"));
	this.closeButton.addActionListener(this);
	bb.addGridded(this.closeButton);
	bb.addRelatedGap();
	
	this.saveButton = new JButton(Translatrix.getTranslationString("dicom.Save"));
	this.saveButton.addActionListener(this);
	bb.addGridded(this.saveButton);
	
	this.saveAndAddButton = new JButton(Translatrix.getTranslationString("dicom.SaveAndAdd"));
	this.saveAndAddButton.addActionListener(this);
	bb.addFixed(this.saveAndAddButton);
	
	this.add(bb.getPanel(), BorderLayout.SOUTH);
	
	this.pack();
    }


    public HeaderTag selectTag(HeaderTag ht) {
		tagSearchPanel.setTag(ht);
		this.filterTagPanel.setHeaderTag(ht);
		
		if (parent != null)
		    setLocationRelativeTo(parent);
		addNext = false;
		this.setVisible(true);
	return this.ht;
    }
    
    public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.saveButton)) {
	    ht = this.filterTagPanel.getTag();
	} else if (e.getSource().equals(this.saveAndAddButton)) {
	    ht = this.filterTagPanel.getTag();
	    addNext = true;
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

	new TagFilterDialog().selectTag(null);
    }

	public boolean isAddNext() {
		return addNext;
	}

	@Override
	public void tagChanged(HeaderTag tag) {
		this.ht=tag;
		System.out.println("tagchanged: " + tag);
		this.filterTagPanel.setHeaderTag(tag);
	}
}
