package lu.tudor.santec.dicom.demoapps;

import ij.ImagePlus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.exporter.DicomExporter;
import lu.tudor.santec.dicom.receiver.DICOMListener;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import lu.tudor.santec.dicom.receiver.DicomStorageServer;
import lu.tudor.santec.dicom.sender.DicomSender;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.DicomObject;

/**
 *  DicomNode that receives images (even compressed), 
 *  transcodes them to uncompressed and then
 *  sends them to another node
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: TranscodeNode.java,v $
 * <br>Revision 1.6  2012-03-02 08:56:34  hermen
 * <br>bugfixes on import/export of images
 * <br>
 * <br>Revision 1.5  2010-06-18 14:36:39  hermen
 * <br>updated dcm4che
 * <br>
 * <br>Revision 1.4  2010-05-18 07:09:16  hermen
 * <br>added exporting patches
 * <br>
 * <br>Revision 1.3  2009-07-06 09:31:30  hermen
 * <br>fixed loading of headers-only files
 * <br>added 1:1 view
 * <br>
 * <br>Revision 1.2  2009-01-19 15:31:30  hermen
 * <br>fixed exporting
 * <br>
 *
 */
public class TranscodeNode implements DICOMListener {

    private DicomSender dicomSender;
    private DicomExporter dicomExporter;
    private DicomStorageServer dss;
    private File propertyFile;
    private File LOGFILE = new File("TranscodeNode.log");
    private String DICOM_REC_AET_NAME;
    private Integer DICOM_REC_PORT;
    private String DICOM_SND_AET_NAME;
    private String DICOM_SND_ADDRESS;
    private Integer DICOM_SND_PORT;
    private String DICOM_STORE_DIR;
    private Boolean KEEP_FILES;
    private static Logger logger = Logger.getLogger(TranscodeNode.class
	    .getName());

    public TranscodeNode(File propertyFile) {
	
	this.propertyFile = propertyFile;
	
	try {
		Handler fh = new FileHandler(LOGFILE.getAbsolutePath(), 1024*1024*2, 5, true);
		fh.setFormatter(new SimpleFormatter());
		Logger.getLogger("").addHandler(fh);
		Logger.getLogger("").setLevel(Level.INFO);
		logger.info("Added logging to file: "	+ LOGFILE);
	} catch (Exception e) {
		e.printStackTrace();
	} 
	
	readConfig();
	
    }

    private void readConfig() {
	Properties properties = new Properties();
	InputStream in;
	if (propertyFile != null && propertyFile.canRead()) {
		try {
			in = new FileInputStream(propertyFile);
		} catch (FileNotFoundException e) {
			in = Dicom2dbImporter.class.getResourceAsStream("TranscodeNode.properties");
		}
	} else {
		in = Dicom2dbImporter.class.getResourceAsStream("TranscodeNode.properties");
	}
	try {
		properties.load(in);
	} catch (Exception e) {
		System.err.println("no usable Settings found");
		logger.warning("no usable Settings found");
		e.printStackTrace();
		System.exit(-1);
	}
	
	this.DICOM_REC_AET_NAME = properties.getProperty("DICOM_REC_AET_NAME");
	this.DICOM_REC_PORT = new Integer(properties.getProperty("DICOM_REC_PORT"));
	this.DICOM_SND_AET_NAME = properties.getProperty("DICOM_SND_AET_NAME");
	this.DICOM_SND_ADDRESS = properties.getProperty("DICOM_SND_ADDRESS");
	this.DICOM_SND_PORT = new Integer(properties.getProperty("DICOM_SND_PORT"));
	this.DICOM_STORE_DIR = properties.getProperty("DICOM_STORE_DIR");
	this.KEEP_FILES = new Boolean(properties.getProperty("KEEP_FILES"));
	
    }

    public void start() {

	try {
	    // start dicom listener node
	    this.dss = new DicomStorageServer(
		    DICOM_REC_AET_NAME, 
		    DICOM_REC_PORT,
		    DICOM_STORE_DIR, 
		    true, 
	            "password",
	    	    false);
	    this.dss.addDICOMListener(this);
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}

	// create dicom sender
	DcmURL url = new DcmURL(
		"dicom",
		DICOM_SND_AET_NAME,
		DICOM_REC_AET_NAME,
		DICOM_SND_ADDRESS,
		DICOM_SND_PORT);
	
	this.dicomSender = new DicomSender(url);
	
	// create exporter 
	this.dicomExporter = new DicomExporter();
    }

    public void fireDicomEvent(DicomEvent event) {
	logger.info("event: " + event);

	File inFile = event.getFile();
	File outFile = new File(inFile.getAbsolutePath() + ".converted.dcm");

	try {
	    
        	// load image
        	ImagePlus ip = DicomOpener.loadImage(inFile, null, null);
        	
        	// write converted image
        	DicomObject header = this.dicomExporter.createHeader(ip, true, true, true);
        	this.dicomExporter.write(header, ip, outFile, true);
        
        	// send converted file
        	this.dicomSender.send(outFile);

	} catch (Exception e) {
	    logger.log(Level.WARNING, "Converting/Sending failed:", e);
	    e.printStackTrace();
	} finally {
	    // delete files
	    if (! KEEP_FILES) {
        	    if (inFile != null && inFile.exists()) inFile.delete();
        	    if (outFile != null && outFile.exists()) outFile.delete();
	    }
	}
	
    }

    public static void main(String[] args) {

	File f = null;
	try {
		f = new File(args[0]);
	} catch (Exception e) {
	}
	
	TranscodeNode tn = new TranscodeNode(f);
	tn.start();
	
    }

}
