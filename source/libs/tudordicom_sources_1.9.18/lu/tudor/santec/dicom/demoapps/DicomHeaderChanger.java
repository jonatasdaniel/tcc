package lu.tudor.santec.dicom.demoapps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

/**
 * strips the image data off the dicom file, to only keep the header data
 * 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: DicomHeaderChanger.java,v $
 * <br>Revision 1.3  2013-02-05 10:03:46  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.2  2012-04-12 13:05:46  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.1  2012-04-06 14:20:19  hermen
 * <br>1.9.9
 * <br>
 * <br>Revision 1.1  2009-04-07 09:34:14  hermen
 * <br>changed a lot
 * <br>
 *
 */
public class DicomHeaderChanger {

    public static void main(String[] args) {

	File inFolder = new File("/home/hermenj/NCICT phantom models/images_10year/dcm");
	File outFolder = new File("/home/hermenj/NCICT phantom models/images_10year/dcm_new/");
	
	String patName = "NCICT_Phantom_10years";
	float sliceThickness = 3.0f;	
	
	
	outFolder.mkdirs();
	
	float sliceloc = 0.0f;
	int instanenumber = 1;
	String sopclassuid = null, seriesinstanceuid = null, studyinstanceuid = null;
	
	File[] files = inFolder.listFiles();
	Arrays.sort(files);
	
	for (File  inFile : files) {

		try {
			String fileName = inFile.getName();
			File outFile = new File(outFolder, patName + "_" + instanenumber + ".dcm");
			
		    DicomInputStream in = new DicomInputStream(inFile);
	        DicomObject dcmobj =  in.readDicomObject();
	        in.close();
	        
	        // MODIFY HEADER HERE!

	        if (sopclassuid == null) sopclassuid = dcmobj.getString(Tag.SOPClassUID);
	        if (studyinstanceuid == null) studyinstanceuid = dcmobj.getString(Tag.StudyInstanceUID);
	        if (seriesinstanceuid == null) seriesinstanceuid = dcmobj.getString(Tag.SeriesInstanceUID);
		    
	        dcmobj.putString(Tag.SOPClassUID, VR.UI, sopclassuid);
	        dcmobj.putString(Tag.StudyInstanceUID, VR.UI, studyinstanceuid);
	        dcmobj.putString(Tag.SeriesInstanceUID, VR.UI, seriesinstanceuid);
	        dcmobj.putString(Tag.InstanceNumber, VR.IS, instanenumber++ +"");
	        dcmobj.putString(Tag.Modality, VR.CS, "CT");
	        dcmobj.putDouble(Tag.SliceThickness, VR.FD, sliceThickness);
	        dcmobj.putStrings(Tag.PixelSpacing, VR.DS, new String[] {"1.0","1.0"});
	        dcmobj.putString(Tag.SliceLocation, VR.DS, (sliceloc+=sliceThickness)+"");
	        dcmobj.putString(Tag.PatientID, VR.LO, patName);
	        dcmobj.putString(Tag.PatientName, VR.PN, patName);

	        
	        FileOutputStream fos = new FileOutputStream(outFile);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    DicomOutputStream dos = new DicomOutputStream(bos);
		    dos.writeDicomFile(dcmobj);
		    dos.close();
		    
		    System.out.println("image " + inFile.getAbsolutePath() + " saved to " + outFile.getAbsolutePath());
		} catch (Exception e) {
		    System.out.println("writing failed:");
		    e.printStackTrace();
		} 
	    }

    }

}
