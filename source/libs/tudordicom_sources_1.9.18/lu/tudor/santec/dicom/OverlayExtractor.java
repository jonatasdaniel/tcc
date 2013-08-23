package lu.tudor.santec.dicom;

import ij.ImagePlus;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;

public class OverlayExtractor {

    public OverlayExtractor() {
	
    }
    
    public static boolean hasOverlayImage(File f) {
	DicomInputStream in;
	try {
	    in = new DicomInputStream(f);
	    in.setHandler(new StopTagInputHandler(Tag.PixelData));
	    DicomObject dcmobj =  in.readDicomObject();
	    in.close();
	    return hasOverlayImage(dcmobj);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return false;
    }
    
    public static boolean hasOverlayImage(DicomObject dcmobj) {
	return dcmobj.contains(Tag.OverlayRows);
    }
    
    public static ImagePlus extractOverlay(File f) {
	DicomInputStream in;
	try {
	    in = new DicomInputStream(f);
	    in.setHandler(new StopTagInputHandler(Tag.PixelData));
	    DicomObject dcmobj =  in.readDicomObject();
	    in.close();
	    return extractOverlay(dcmobj);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    public static ImagePlus extractOverlay(File f, Color foreground, Color background) {
	DicomInputStream in;
	try {
	    in = new DicomInputStream(f);
	    in.setHandler(new StopTagInputHandler(Tag.PixelData));
	    DicomObject dcmobj =  in.readDicomObject();
	    in.close();
	    return extractOverlay(dcmobj, foreground, background);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    public static ImagePlus extractOverlay(DicomObject dcmobj) {
	    return extractOverlay(dcmobj, Color.green, new Color(0,0,0,0));
    }
    
    public static ImagePlus extractOverlay(DicomObject dcmobj, Color foreground, Color background) {
	try {
            
            if (dcmobj.contains(Tag.OverlayData)) {
	        	DicomElement olData = dcmobj.get(Tag.OverlayData);
	        	
	        	int rows = dcmobj.get(Tag.OverlayRows).getInt(false);
	        	int cols = dcmobj.get(Tag.OverlayColumns).getInt(false);
	        	
	        	byte[] data = olData.getBytes();
	        	
	        	return overlayFromBytes(data, rows, cols, foreground, background);
            }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }
    
    
    public static ImagePlus overlayFromBytes(byte[] data, int rows, int cols, Color fg, Color bg) {
	byte[] byteData = new byte[data.length*8];
	
	
	
	
	int j = 0;
	for (int i = 0; i < data.length; i++) {
	    byte b = data[i];
	    if ((b & 0x01) > 0 ) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x02) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x04) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x08) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x10) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x20) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x40) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	    if ((b & 0x80) > 0) byteData[j++]=(byte) 0; else byteData[j++]= (byte) 255;
	}
	
	if (rows*cols != byteData.length) {
		System.err.println("overlay should be " + rows + "x" + cols + "=" +(rows*cols) + "byte but is " + byteData.length + "byte - Adjusting data...");
		byteData = Arrays.copyOf(byteData, rows*cols);
	} 

	byte[] cmap = {(byte)fg.getRed(), (byte) fg.getGreen(), (byte) fg.getBlue(), (byte)fg.getAlpha(), (byte)bg.getRed(), (byte) bg.getGreen(), (byte) bg.getBlue(), (byte)bg.getAlpha() };
	
	ImageProcessor ip = new ByteProcessor(cols,rows, byteData, new IndexColorModel(1,2,cmap,0,true));
	
	return new ImagePlus("overlay",ip);
    }
    
    public static byte[] bytesFromOverLay(ByteProcessor ip) {
	byte[] pixels = (byte[]) ip.getPixels();
	byte[] binaryPixels = new byte[pixels.length/8]; 
	int j = 7;
	for (int i = 0; i < binaryPixels.length; i++) {
	    StringBuffer sb = new StringBuffer();
	    for (int k = 0; k < 8; k++) {
		sb.append((((char)pixels[j--]) >0?1:0));
	    }
	    j+=16;
	    binaryPixels[i] = (byte) (Short.parseShort(sb.toString(), 2)); 
	}	
	return binaryPixels;
    }
    
    public static ImagePlus createOverlayImage(ImagePlus orig, DicomObject dcmobj) {
	ImagePlus overlay = extractOverlay(dcmobj);
	if (overlay == null) return orig;
//	new ImageJ();
	ColorProcessor cp = (ColorProcessor) orig.getProcessor().convertToRGB();
	ColorProcessor cp2 = (ColorProcessor) overlay.getProcessor().convertToRGB();
//	new ImagePlus("conv", cp2).show();
	cp.copyBits(cp2, 0, 0, Blitter.COPY_ZERO_TRANSPARENT);
//	new ImagePlus("add", cp).show();
	return new ImagePlus("img+overlay", cp);
    }
    
    public static DicomObject image2Overlay(ImagePlus orig, DicomObject dcmobj) throws Throwable{
		ImageProcessor ip = orig.getProcessor();
		ip = ip.convertToByte(true);
		orig = new ImagePlus("test", ip);

		byte[] pixels = (byte[]) ip.getPixels();
		byte[] bitData = new byte[pixels.length/8];
		int pixcount = 0 ;
		for (int i = 0; i < bitData.length; i++) {
		    for (byte j = 0; j < 8; j++) {
			if (pixels[pixcount] < 0)
			    bitData[i] = (byte) (bitData[i] | (1 << j));
			else
			    bitData[i] = (byte) (bitData[i] | (0 << j));
			pixcount++;
		    }
		}
		
		dcmobj.putInt(Tag.OverlayRows, VR.US, orig.getHeight());
		dcmobj.putInt(Tag.OverlayColumns, VR.US, orig.getWidth());
		dcmobj.putInt(Tag.NumberOfFramesInOverlay, VR.IS, 1);
		dcmobj.putString(Tag.OverlayDescription, VR.LO, "DOSE INFO");
		dcmobj.putString(Tag.OverlayType, VR.CS, "G");
		dcmobj.putShorts(Tag.OverlayOrigin, VR.SS, new short[] {1,1});
		dcmobj.putInt(Tag.OverlayBitsAllocated, VR.US, 1);
		dcmobj.putInt(Tag.OverlayBitPosition, VR.US, 0);
		dcmobj.putBytes(Tag.OverlayData, VR.OB, bitData);
		
		return dcmobj;
    }
    
    
    public static BufferedImage removeColor(BufferedImage ipl, Color color) {

//    	BufferedImage dimg = new BufferedImage(ipl.getWidth(), ipl.getHeight(), BufferedImage.TYPE_INT_ARGB);
    	BufferedImage dimg = new BufferedImage(ipl.getWidth(), ipl.getHeight(), BufferedImage.TRANSLUCENT);
    	Graphics2D g = dimg.createGraphics();  
//        g.setComposite(AlphaComposite.Src);  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));  
        g.drawImage(ipl, null, 0, 0);  
        g.dispose();  
        for(int i = 0; i < dimg.getHeight(); i++) {  
            for(int j = 0; j < dimg.getWidth(); j++) {  
                if(dimg.getRGB(j, i) == color.getRGB()) {  
                dimg.setRGB(j, i, 0x8F1C1C);  
                }  
            }  
        }  
        return dimg;  
    }
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
//	ImagePlus ip = OverlayExtractor.extractOverlay(new File("/home/hermenj/9d9accdd"));
//	new ImageJ().show();
//	ip.show();
//	OverlayExtractor.extractOverlay(new File("/media/ETIAM/images/im510"));
	
	
	try {
	    ImagePlus ip = new ImagePlus("/home/hermenj/1");
	    // read orig file
	    DicomInputStream in;
	    in = new DicomInputStream(new File("/home/hermenj/1"));
	    in.setHandler(new StopTagInputHandler(Tag.PixelData));
	    DicomObject dcmobj =  in.readDicomObject();
	    in.close();
	    
	    dcmobj =  image2Overlay(ip, dcmobj);
	    
	    FileOutputStream fos = new FileOutputStream("/home/hermenj/1_ov");
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    DicomOutputStream dos = new DicomOutputStream(bos);
	    dos.writeDicomFile(dcmobj);
	    dos.close();
	    
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

    
    
    
}
