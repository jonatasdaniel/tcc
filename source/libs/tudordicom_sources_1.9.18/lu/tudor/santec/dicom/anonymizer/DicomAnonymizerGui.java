package lu.tudor.santec.dicom.anonymizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DicomAnonymizerGui extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private JButton cancelButton;
    private JButton runButton;
    private JButton closeButton;
    private boolean standalone;
    private JTextField dirField;
    private JButton dirButton;
    private JFileChooser fileChooser = new JFileChooser();
    private static boolean stopRunning;
    private JTextArea logField;
    private SimpleAnonPanel anonPanel;
	private JButton clearButton;

    public DicomAnonymizerGui(boolean standalone) {
	setTitle("DicomAnonymizerGui");
	
	this.standalone = standalone;
	
	this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	
	this.setLayout(new BorderLayout());
	this.getContentPane().setBackground(Color.WHITE);
	
	JPanel dirPanel = new JPanel(new BorderLayout(3,3));
	dirPanel.setOpaque(false);
	dirPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
	
	dirField = new JTextField();
	dirPanel.add(dirField, BorderLayout.CENTER);
	
	dirButton = new JButton("choose");
	dirButton.addActionListener(this);
	dirPanel.add(dirButton, BorderLayout.EAST);
	
	this.add(dirPanel, BorderLayout.NORTH);

	this.add(createSimplePanel(), BorderLayout.CENTER);
	
	
	
	ButtonBarBuilder bb = new ButtonBarBuilder();
	
	closeButton = new JButton("close");
	closeButton.addActionListener(this);
	bb.addGridded(closeButton);

	bb.addRelatedGap();
	
	clearButton = new JButton("clear");
	clearButton.addActionListener(this);
	bb.addGridded(clearButton);
	
	bb.addGlue();
	
	cancelButton = new JButton("cancel");
	cancelButton.addActionListener(this);
	bb.addGridded(cancelButton);
	
	bb.addRelatedGap();
	
	runButton = new JButton("run");
	runButton.addActionListener(this);
	bb.addGridded(runButton);
	
	JPanel buttonPanel = bb.getPanel();
	buttonPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
	buttonPanel.setOpaque(false);
	this.add(buttonPanel, BorderLayout.SOUTH);
	
	
	this.setSize(600, 750);
	
	this.setVisible(true);
	
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource().equals(this.closeButton)) {
	    if (standalone) {
		System.exit(0);
	    }
	    this.setVisible(false);
	    this.dispose();
	} else if (e.getSource().equals(this.cancelButton)) {
	    stopRunning = true;
	} else if (e.getSource().equals(this.runButton)) {
		
		int retval = JOptionPane.showConfirmDialog(
				this, 
				"You are about to anonymize/strip all files in the following folder:\n"
				+ new File(dirField.getText()).getAbsolutePath() + "\n\n"
			    + "All ORIGINAL files in this folder will be deleted.\n"
			    + "Do you understand and want to continue?",
			    "Anonymize/Strip images",
			    JOptionPane.WARNING_MESSAGE,
			    JOptionPane.YES_NO_OPTION);
		
		
	    if (retval == JOptionPane.CANCEL_OPTION) {
			return;
		}

	    stopRunning = false;
	    processFiles();
	} else if (e.getSource().equals(this.dirButton)) {
	    chooseDir();
	} else if (e.getSource().equals(this.clearButton)) {
	    logField.setText("");
	}
    }

    private void processFiles() {
	
	Thread t = new Thread() {
	    public void run() {
		logField.setText("");
		closeButton.setEnabled(false);
		runButton.setEnabled(false);
		File file = new File(dirField.getText());
		if (file.isDirectory() ) {
		    // recurse
		    File[] files = getFilesRecursive(file);
		    for (int i = 0; i < files.length; i++) {
			if (stopRunning)
			    break;
			processFile(files[i]);
		    }
		} else if (file.isFile()){
		    processFile(file);
		}
		closeButton.setEnabled(true);
		runButton.setEnabled(true);
	    }
	};
	t.start();
    }

    private void processFile(File file) {
	if (file.canWrite()) {
	    try {
	    String suffix = DicomAnonymizer.SUFFIX_ANON;	
		DicomObject dObj = DicomAnonymizer.readDicomObject(file);
		
		if (dObj != null && ! dObj.isEmpty() && (dObj.vm(Tag.TransferSyntaxUID)!= -1)) {
		    System.out.println("processing file: " + file.getPath());
		    logField.append("processing file: " + file.getPath() + "\n");
		    if (anonPanel.isRemovePatient())
		    	dObj = DicomAnonymizer.removePatientInfo(dObj, anonPanel.getNewPatientID(), anonPanel.getNewPatientName());
		    
		    if (anonPanel.isRemovePatientAdditional())
		    	dObj = DicomAnonymizer.removePatientAdditionalInfo(dObj);
		    
		    if (anonPanel.isRemoveInstitution()) {
		    	dObj = DicomAnonymizer.removeInstitutionInfo(dObj, anonPanel.getNewInstitutionName());		    	
		    }
		    
		    if (anonPanel.isRemovePhysician())
		    	dObj = DicomAnonymizer.removePhysicianInfo(dObj);
		    
		    if (anonPanel.isRemoveManufacturer())
		    	dObj = DicomAnonymizer.removeManufacturerInfo(dObj);

		    if (anonPanel.isRemovePrivate())
		    	dObj = DicomAnonymizer.removePrivateTags(dObj);
		    
		    if (anonPanel.isRemoveThis())
		    	dObj = DicomAnonymizer.removeTags(dObj, anonPanel.removeThisTags());
		    
		    if (anonPanel.isRemoveImageData()) {
		    	dObj = DicomAnonymizer.removeImageData(dObj);
		    	suffix = DicomAnonymizer.SUFFIX_HEADER;
		    }
		    
		    if (anonPanel.isRename()) {
		    	file.delete();
		    	DicomAnonymizer.writeDicomFile(dObj, new File(file.getAbsolutePath()+suffix));				    		    	
		    } else {
		    	DicomAnonymizer.writeDicomFile(dObj, file);	
		    }
		} else {
		    System.out.println("NOT processing file: " + file.getPath());
		    logField.append("NOT processing file: " + file.getPath() + "\n");
		}
		
	    } catch (Exception e) {
		System.out.println("\t" + e.getLocalizedMessage());
		logField.append("\t" + e.getLocalizedMessage() + "\n");
		e.printStackTrace();
	    }
	    
	} else {
		System.out.println("File NOT writeable: " + file.getPath());
	    logField.append("File NOT writeable: " + file.getPath() + "\n");
	}
    }

    private void chooseDir() {
	if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    this.dirField.setText(fileChooser.getSelectedFile().getAbsolutePath());
	}
    }
    
    
    private JPanel createSimplePanel() {
	CellConstraints cc = new CellConstraints();
	JPanel jp = new JPanel(new FormLayout(
		"3dlu, pref:grow, 3dlu",
		"3dlu, pref, 3dlu, fill:pref:grow, 3dlu"
	));
	jp.setOpaque(false);
	
	anonPanel = new SimpleAnonPanel();
	jp.add(anonPanel, cc.xyw(1, 2, 3));
	
	logField = new JTextArea();
	JScrollPane jsp = new JScrollPane(logField);
	jsp.setOpaque(false);
	jsp.getViewport().setOpaque(false);
	jp.add(jsp, cc.xyw(2,4,1));
	
	
	return jp;
    }
    
    
	private File[] getFilesRecursive(File dir) {
	ArrayList<File> al = new ArrayList<File>();
	visitAllFiles(dir, al);
	File[] files = new File[al.size()];
	int i = 0;
	for (Iterator<File> iter = al.iterator(); iter.hasNext();) {
	    File element = (File) iter.next();
	    files[i] = element;
	    i++;
	}
	return files;
    }

    // Process all files under dir
    private static void visitAllFiles(File dir, ArrayList<File> al) {
	if (dir.isDirectory() && ! stopRunning) {
	    String[] children = dir.list();
	    for (int i = 0; i < children.length; i++) {
		visitAllFiles(new File(dir, children[i]), al);
	    }
	} else {
	    al.add(dir);
	}
    }
    
    public static void main(String[] args) {
	new DicomAnonymizerGui(true);
    }

    
}
