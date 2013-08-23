package lu.tudor.santec.dicom.demoapps;

import ij.ImagePlus;

import java.io.File;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.exporter.DicomExporter;

import org.dcm4che2.data.DicomObject;

public class ImageTranscoder {

    public static void main(String[] args) {

	File inFile = null;
	File outFile = null;
	try {
	    inFile = new File(args[0]);
	    outFile = new File(args[1]);
	} catch (Exception e) {
	    System.err.println("dicomExporter inFile outFile");
	    System.exit(-1);
	}
	
	try {
	
    		DicomExporter dicomExporter = new DicomExporter();
    	    
        	// load image
        	ImagePlus ip = DicomOpener.loadImage(inFile, null, null);
        	
        	// write converted image
        	DicomObject header = dicomExporter.createHeader(ip, true, true, true);
        	dicomExporter.write(header, ip, outFile, true);

        	System.out.println("image " + inFile.getAbsolutePath() + " transcoded to " + outFile.getAbsolutePath());
	} catch (Exception e) {
	    System.out.println("Converting failed:");
	    e.printStackTrace();
	} 
    }
}
