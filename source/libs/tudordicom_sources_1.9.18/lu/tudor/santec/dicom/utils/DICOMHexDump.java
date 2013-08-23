package lu.tudor.santec.dicom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.poi.util.HexDump;

public class DICOMHexDump {

    private final static DecimalFormat format = new DecimalFormat("00000000");
    
    public static String[] dump2HEX(File f, long byteAnz, int linebreak) {
	
	String[] retVal = new String[3];
	StringBuffer sbLines = new StringBuffer();
	StringBuffer sbHex = new StringBuffer();
	StringBuffer sbText = new StringBuffer();
	
	try {
        	FileInputStream fr = new FileInputStream (f);
        	byte[] bytes = new byte[linebreak];
        	
        	for (int i = 0; i < (byteAnz/16); i++) {
        	    fr.read(bytes);
        	    sbLines.append(format.format(linebreak*i) + "\n");
                    sbHex.append(toHex(bytes)); 
        	    sbText.append(new String(bytes).replaceAll("[^\\p{Print}]","_") + "\n");
        	}
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
	sbHex.append("stripped after "+byteAnz+ " bytes...");
	retVal[0] = sbLines.toString();
	retVal[1] = sbHex.toString();
	retVal[2] = sbText.toString();
	return retVal;
    }
    
    
    private static String toHex(final byte[] value)
    {

        StringBuffer retVal = new StringBuffer();
        for(int x = 0; x < value.length; x++)
        {
            retVal.append(HexDump.toHex(value[x]));
            if (x == (value.length-1)) {
        	retVal.append("\n");
            } else {
        	retVal.append(" ");
            }
        }
        return retVal.toString();
    }
    
}
