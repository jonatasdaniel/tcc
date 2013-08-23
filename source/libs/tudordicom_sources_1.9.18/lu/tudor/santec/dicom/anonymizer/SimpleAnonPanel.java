package lu.tudor.santec.dicom.anonymizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lu.tudor.santec.dicom.gui.header.DicomHeader;

import org.dcm4che2.util.StringUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SimpleAnonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JCheckBox removePatientCB;
    private JTextField newPatientID;
    private JCheckBox removeInstitutionCB;
    private JTextField newInstitutionName;
    private JCheckBox removeManufacturerCB;
    private JTextField newPatientName;
    private JCheckBox removePatientAdditionalCB;
    private JCheckBox removeImageDataCB;
	private JCheckBox renameCB;
	private JCheckBox removePhysicianCB;
	private JCheckBox removePrivateCB;
	private JCheckBox removeThisCB;
	private JTextArea removeThisField;

    public SimpleAnonPanel() {
    
    CellConstraints cc = new CellConstraints();
	this.setLayout(new FormLayout(
		"3dlu, pref, 3dlu, pref:grow, 3dlu",
		"3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref,3dlu, pref, 3dlu, fill:70dlu, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, fill:pref:grow, 3dlu"
	));
	this.setOpaque(false);
	
	removePatientCB = new JCheckBox("remove Patient Information", true);
	removePatientCB.setToolTipText(createInfo(DicomAnonymizer.PATIENT_TAGS));
	removePatientCB.setOpaque(false);
	this.add(removePatientCB, cc.xyw(2,2,3));
	removePatientCB.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		    newPatientID.setEnabled(removePatientCB.isSelected());
		    newPatientName.setEnabled(removePatientCB.isSelected());
	    }
	});
	this.add(new JLabel("NEW Patient ID"), cc.xyw(2,4,1));
	newPatientID = new JTextField(System.currentTimeMillis()+"");
	this.add(newPatientID, cc.xyw(4,4,1));
	this.add(new JLabel("NEW Patient Name"), cc.xyw(2,6,1));
	newPatientName = new JTextField("ANONYMOUS");
	this.add(newPatientName, cc.xyw(4,6,1));
	this.add(new JSeparator(), cc.xyw(2,8,3));
	
	removePatientAdditionalCB = new JCheckBox("remove Patient Additional Information", true);
	removePatientAdditionalCB.setToolTipText(createInfo(DicomAnonymizer.PATIENT_ADDITIONAL_TAGS));
	removePatientAdditionalCB.setOpaque(false);
	this.add(removePatientAdditionalCB, cc.xyw(2,10,3));
		this.add(new JSeparator(), cc.xyw(2,12,3));
	
	removeInstitutionCB = new JCheckBox("remove Institution Information", true);
	removeInstitutionCB.setToolTipText(createInfo(DicomAnonymizer.ORGANISATION_TAGS));
	removeInstitutionCB.setOpaque(false);
	this.add(removeInstitutionCB, cc.xyw(2,14,3));
	removeInstitutionCB.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		    newInstitutionName.setEnabled(removeInstitutionCB.isSelected());
	    }
	});
	this.add(new JLabel("NEW Institution Name"), cc.xyw(2,16,1));
	newInstitutionName = new JTextField();
	this.add(newInstitutionName, cc.xyw(4,16,1));
	this.add(new JSeparator(), cc.xyw(2,18,3));
	
	removePhysicianCB = new JCheckBox("remove Physician Information", true);
	removePhysicianCB.setToolTipText(createInfo(DicomAnonymizer.PHYSICIAN_TAGS));
	removePhysicianCB.setOpaque(false);
	this.add(removePhysicianCB, cc.xyw(2,20,3));
	
	this.add(new JSeparator(), cc.xyw(2,22,3));
	
	removeManufacturerCB = new JCheckBox("remove Manufacturer Information", true);
	removeManufacturerCB.setToolTipText(createInfo(DicomAnonymizer.MANUFACTURER_TAGS));
	removeManufacturerCB.setOpaque(false);
	this.add(removeManufacturerCB, cc.xyw(2,24,3));
	
	this.add(new JSeparator(), cc.xyw(2,26,3));
	
	removePrivateCB = new JCheckBox("remove Private Tags", false);
	removePrivateCB.setOpaque(false);
	removePrivateCB.setToolTipText("<html><b>This will remove ALL PRIVATE Tags</b><br>");
	this.add(removePrivateCB, cc.xyw(2,28,3));
	
	this.add(new JSeparator(), cc.xyw(2,30,3));
	
	removeThisCB = new JCheckBox("remove THIS Tags", false);
	removeThisCB.setOpaque(false);
	this.add(removeThisCB, cc.xyw(2,32,1));
	this.removeThisCB.setToolTipText("<html><b>This will remove ALL Tags Specified below.</b><br>Tags have to be one per line in the format 0010,0010");
	removeThisField = new JTextArea();
	removeThisField.setToolTipText("<html><b>This will remove ALL Tags Specified below.</b><br>Tags have to be one per line in the format 0010,0010");
	JScrollPane scroller = new JScrollPane(removeThisField);
	this.add(scroller, cc.xywh(4,32,1,3));
	
	this.add(new JSeparator(), cc.xyw(2,36,3));
	
	removeImageDataCB = new JCheckBox("remove Image (This will leave only the DICOM-Header!)", false);
	removeImageDataCB.setToolTipText(createInfo(DicomAnonymizer.IMAGE_TAGS));
	removeImageDataCB.setOpaque(false);
	this.add(removeImageDataCB, cc.xyw(2,38,3));

	this.add(new JSeparator(), cc.xyw(2,40,3));
	
	renameCB = new JCheckBox("rename Files to "+DicomAnonymizer.SUFFIX_ANON+"/"+DicomAnonymizer.SUFFIX_HEADER, true);
	renameCB.setOpaque(false);
	this.add(renameCB, cc.xyw(2,42,3));

	this.add(new JSeparator(), cc.xyw(2,44,3));
	
    }
    
    private String createInfo(int[] tags) {
	StringBuffer sb = new StringBuffer("<html><b>This will remove the following Tags</b><br>");
	for (int tag : tags) {
	    sb.append(DicomHeader.getHeaderName(
		    StringUtils.shortToHex(tag >> 16) + 	',' + 
		    StringUtils.shortToHex(tag)));
	    sb.append("<br>");
	}
	return sb.toString();
    }

    public boolean isRemovePatient() {
	return removePatientCB.isSelected();
    }
    
    public boolean isRemovePatientAdditional() {
	return removePatientAdditionalCB.isSelected();
    }

    public boolean isRemoveInstitution() {
	return removeInstitutionCB.isSelected();
    }

    public boolean isRemoveManufacturer() {
	return removeManufacturerCB.isSelected();
    }
    
    public boolean isRemovePrivate() {
	return removePrivateCB.isSelected();
    }
    
    public boolean isRemoveThis() {
	return removeThisCB.isSelected();
    }
    
    public String[] removeThisTags() {
    	String fields = removeThisField.getText();
    	return fields.split("\n"); 
    }
    
    public boolean isRemovePhysician() {
    	return removePhysicianCB.isSelected();
    }

    public String getNewPatientID() {
	return newPatientID.getText();
    }

    public String getNewPatientName() {
	return newPatientName.getText();
    }

    public String getNewInstitutionName() {
	return newInstitutionName.getText();
    }

    public void setRemovePatient(boolean b) {
	this.removePatientCB.setSelected(b);
    }
    
    public void setRemoveInstitution(boolean b) {
	this.removeInstitutionCB.setSelected(b);
    }

    public void setRemoveManufacturer(boolean b) {
	this.removeManufacturerCB.setSelected(b);
    }

	public boolean isRemoveImageData() {
		return this.removeImageDataCB.isSelected();
	}
	
    public void setRemoveImageData(boolean b) {
    	this.removeImageDataCB.setSelected(b);
    }
    
	public boolean isRename() {
		return this.renameCB.isSelected();
	}
	
    public void setRename(boolean b) {
    	this.renameCB.setSelected(b);
    }
    
}
