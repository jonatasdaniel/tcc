
import ij.plugin.DICOM;

public class Main {

	public static void main(String[] args) {
		DICOM dicom = new DICOM();
		dicom.run("C:\\Users\\MarceloSimara\\Documents\\TCC\\arquivos_DICOM\\RM_Cranio-Ricardo\\CD\\CDSURF\\DICOMDIR\\559B1FDB\\9CCC98F7\\");
//		dicom.getBufferedImage();
	}

}
