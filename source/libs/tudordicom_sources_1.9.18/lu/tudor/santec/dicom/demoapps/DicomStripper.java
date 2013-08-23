package lu.tudor.santec.dicom.demoapps;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;

/**
 * strips the image data off the dicom file, to only keep the header data
 * 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: DicomStripper.java,v $
 * <br>Revision 1.1  2009-04-07 09:34:14  hermen
 * <br>changed a lot
 * <br>
 *
 */
public class DicomStripper {

    public static void main(String[] args) {

	File inFile = null;
	File outFile = null;
	try {
	    inFile = new File(args[0]);
	    outFile = new File(args[1]);
	} catch (Exception e) {
	    System.err.println("DicomStripper inFile outFile");
	    System.exit(-1);
	}
	
	try {
	
	    DicomInputStream in = new DicomInputStream(inFile);
            in.setHandler(new StopTagInputHandler(Tag.PixelData));
            DicomObject dcmobj =  in.readDicomObject();
            in.close();
	    
            FileOutputStream fos = new FileOutputStream(outFile);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    DicomOutputStream dos = new DicomOutputStream(bos);
	    dos.writeDicomFile(dcmobj);
	    dos.close();
	    
	    System.out.println("image " + inFile.getAbsolutePath() + " stripped to " + outFile.getAbsolutePath());
	} catch (Exception e) {
	    System.out.println("stripping failed:");
	    e.printStackTrace();
	} 
    }
}
