package lu.tudor.santec.dicom;

/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2006 by SANTEC/TUDOR www.santec.tudor.lu                   
 *                                                                           
 *                                                                           
 *  This library is free software; you can redistribute it and/or modify it  
 *  under the terms of the GNU Lesser General Public License as published    
 *  by the Free Software Foundation; either version 2 of the License, or     
 *  (at your option) any later version.                                      
 *                                                                           
 *  This software is distributed in the hope that it will be useful, but     
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 *  Lesser General Public License for more details.                          
 *                                                                           
 *  You should have received a copy of the GNU Lesser General Public         
 *  License along with this library; if not, write to the Free Software      
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
 *                                                                           
 *****************************************************************************/

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.measure.Calibration;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij3d.Install_J3D;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.JOptionPane;

import loci.formats.IFormatReader;
import loci.formats.in.DicomReader;
import loci.plugins.util.ImageProcessorReader;
import lu.tudor.santec.dicom.gui.header.DicomHeader;

import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

/**
 * This class is capable to open DICOM images using the JAI Library. This is needed if
 * you like to open compressed (lossless jpeg,...) DICOM images.
 * 
 * 
 * @author Andreas Jahnen andreas.jahnen(at)tudor.lu
 *
 */
public class DicomOpener{
	
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomOpener.class.getName());
	
	
	/**
	 * @param p_File the file to load
	 * @return
	 * @throws FileNotFoundException
	 */
        public static ImagePlus loadImage(File p_File) throws FileNotFoundException {
    	return loadImage(p_File, null, null);
        }
        
	/**
	 * @param p_File the file to load
	 * @return
	 * @throws FileNotFoundException
	 */
        public static ImagePlus loadImageStack(File p_File) throws FileNotFoundException {
    	return loadImageStack(p_File, null, null);
        }
    
	/**
	 * @param p_File the file to load
	 * @param dh the dicom header of the file (if allready read, else null) 
	 * @return
	 * @throws FileNotFoundException
	 */
        public static ImagePlus loadImage(File p_File, Component parentComponent) throws FileNotFoundException {
    	return loadImage(p_File, null, parentComponent);
        }
        
	/**
	 * @param p_File the file to load
	 * @param dh the dicom header of the file (if allready read, else null) 
	 * @return
	 * @throws FileNotFoundException
	 */
        public static ImagePlus loadImageStack(File p_File, Component parentComponent) throws FileNotFoundException {
    	return loadImageStack(p_File, null, parentComponent);
        }
    
	/**
	 * @param p_File the file to load
	 * @param dh the dicom header of the file (if allready read, else null) 
	 * @param parentComponent the parent component (for eventual error messages, might be null)
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ImagePlus loadImage(File p_File, DicomHeader dh, Component parentComponent) throws FileNotFoundException {
	    
		// check if file is null:
		if (!p_File.canRead()) {
			logger.error("Provided File is NULL");
			return null;
		}
		
		// check if file is there:
		if (!p_File.canRead()) {
			logger.error("Unable to load file...File not readable: " + p_File);
			return null;
		}
		
		long fileSize = p_File.length()/1024/1024;
		if (fileSize > 5) {
			logger.info("Trying to open a large File with " + fileSize + "mb " + p_File.getAbsolutePath());
		}

		// b) read file name:
		ImagePlus image = null;
		try {
		    	if (dh == null) {
		    		dh = new DicomHeader(p_File);
		    		logger.info("DicomHeader read!");		    		
		    	}
		    	
		    	
		    	long start = System.currentTimeMillis();
		    	if (dh.hasPixelData()) {
		    		
//		    		BufferedImage myJpegImage = null;
//		    		Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("DICOM");
//		    		while (iter.hasNext()) {
//						ImageReader imageReader = (ImageReader) iter.next();
//						System.out.println(imageReader);
//					}
//		    		ImageReader reader = iter.next();
//		    		DicomImageReadParam param = (DicomImageReadParam) reader.getDefaultReadParam();
//		    		ImageInputStream iis = ImageIO.createImageInputStream(p_File);
//		    		reader.setInput(iis, false);
//		    		myJpegImage = reader.read(0, param);
//		    		
//		    		image = new ImagePlus(p_File.getName(), myJpegImage);
		    		
		    		DicomReader dicomReader = new DicomReader();
		    	    try {
			    	    String s = p_File.getAbsolutePath();
			    	    dicomReader.setGroupFiles(false);
			    	    dicomReader.setOriginalMetadataPopulated(false);
			    		dicomReader.setId(s);
	//		    		ImageProcessor ip = Util.openProcessors(dicomReader, 0)[0];
			    		ImageProcessorReader r = new ImageProcessorReader(dicomReader);
			    		ImageProcessor ip = r.openProcessors(0) [0];
			    		dicomReader.close();
			    		
			    		image = new ImagePlus(p_File.getName(), ip);
				    } catch (NegativeArraySizeException eArr) {
						// if fails, try via imageJ
				    	logger.info("opening image via ImageJ");
						image = new ImagePlus(p_File.getAbsolutePath());
				    } finally {
						try {
							if (dicomReader != null)
								dicomReader.close();				    
						} catch (Exception e) {
							logger.warn("Error closing DICOM READER", e);
						}
				    }
		    	    logger.info("loading image took: " + (System.currentTimeMillis()-start) + "ms");
		    	    
		    	    
		    	    image.setProperty("Info", dh.toString());
		    	    image.setProperty(DicomHeader.class.getSimpleName(), dh);
		    	    
		    	    FileInfo fi = new FileInfo();
		    	    fi.directory = p_File.getParent()+ File.separator;
		    	    fi.fileName = p_File.getName();
		    	    
		    	    if (dh.getHeaderIntegerValue(Tag.BitsAllocated)==8)
						fi.fileType = FileInfo.GRAY8;
					else if (dh.getHeaderIntegerValue(Tag.BitsAllocated)==32)
						fi.fileType = FileInfo.GRAY32_UNSIGNED;
					else if (dh.getHeaderIntegerValue(Tag.PixelRepresentation)==1)
						fi.fileType = FileInfo.GRAY16_SIGNED;

		    	    image.setFileInfo(fi);
		    	    
		    	    try {
		    	    	double rescaleIntercept = dh.getHeaderDoubleValue(Tag.RescaleIntercept);
		    	    	double rescaleSlope = dh.getHeaderDoubleValue(Tag.RescaleSlope);
		    	    	
		    	    	if (fi.fileType==FileInfo.GRAY16_SIGNED) {
//		    	    		if (rescaleIntercept!=0.0 && rescaleSlope!=1.0)
//		    	    			image.getProcessor().add(rescaleIntercept);
//		    	    		else {
//		    	    			if (rescaleIntercept!=0.0 && rescaleSlope!=0.0) {
			    	    			double[] coeff = new double[2];
			    	    			coeff[0] = rescaleIntercept;
			    	    			coeff[1] = rescaleSlope;
			    	    			image.getCalibration().setFunction(Calibration.STRAIGHT_LINE, coeff, "HU");
//			    	    		}
//		    	    		}
		    	    	} else {
//		    	    		if (rescaleIntercept!=0.0 && rescaleSlope!=0.0) {
		    	    			double[] coeff = new double[2];
		    	    			coeff[0] = rescaleIntercept;
		    	    			coeff[1] = rescaleSlope;
		    	    			image.getCalibration().setFunction(Calibration.STRAIGHT_LINE, coeff, "HU");
//		    	    		}
		    	    	}
					} catch (Exception e) {
						logger.info("error setting image calibration for image: " + p_File);
					}
		    	} else {
		    	    image =  createDummyImage(p_File, dh, "File does not contain Image Data.") ;
		    	}
			
			return image;
			
		} catch (Throwable e) {
			logger.error("Unable to open image file: "
					+ p_File.getAbsolutePath() + "\n" + e.getMessage(), e);
			
			if (parentComponent != null) {
			    JOptionPane.showMessageDialog(parentComponent,
				    "Unable to open image file: "
					+ p_File.getAbsolutePath() + "\n" + e.getMessage(),
				    "Error loading Image",
				    JOptionPane.ERROR_MESSAGE);
			}
			return createDummyImage(p_File, dh, e.getLocalizedMessage()) ;
		}
	}
	
	/**
	 * @param p_File the file to load
	 * @param dh the dicom header of the file (if allready read, else null) 
	 * @param parentComponent the parent component (for eventual error messages, might be null)
	 * @return
	 * @throws FileNotFoundException
	 */
	public static ImagePlus loadImageStack(File p_File, DicomHeader dh, Component parentComponent) throws FileNotFoundException {
	    
		// check if file is there:
		if (!p_File.canRead()) {
			System.out.println("Unable to load file... " + p_File);
			return null;
		}

		// b) read file name:
		ImagePlus image = null;
		try {
		    	if (dh == null)
		    	    dh = new DicomHeader(p_File);
		    	
		    	if (dh.hasPixelData()) {
		    	    IFormatReader dicomReader = new DicomReader();
		    	    try {
			    	dicomReader.setGroupFiles(false);
		    	    dicomReader.setId(p_File.getAbsolutePath());
		    		int slices = dicomReader.getImageCount();
//		    		ImageProcessor ip = Util.openProcessors(r, 0)[0];
		    		ImageProcessorReader r = new ImageProcessorReader(dicomReader);
		    		ImageProcessor ip = r.openProcessors(0) [0];
		    		image = new ImagePlus(p_File.getName(), ip);
		    		//		    		System.out.println(slices + " slices");
		    		//		    		System.out.println(ip.getWidth() + " + " + ip.getHeight());
		    		//	    			
		    		if (slices >1) {
		    			// multislice
		    			ImageStack is = new ImageStack(ip.getWidth(), ip.getHeight());
		    			for (int i = 0; i < slices; i++) {
		    				//	        		    			ImageProcessor[] processor = Util.openProcessors(dicomReader, i);
		    				ImageProcessor[] processor =  r.openProcessors(i);
		    				System.out.println("Slice: " + i + "/" + slices +  " Processoranz: "  + processor.length);
		    				//        		    			System.out.println(processor.getWidth() + " + " + processor.getHeight());

		    				is.addSlice(p_File.getName() + "[" + (i+1) + "]", processor[0]);
		    			}
		    			image.setStack(null, is);
		    		}
		    		dicomReader.close();
			    } catch (NegativeArraySizeException eArr) {
			    	// if fails, try via imageJ
			    	image = new ImagePlus(p_File.getAbsolutePath());
			    }
		    	    
		    	    image.setProperty("Info", dh.toString());
		    	    image.setProperty(DicomHeader.class.getSimpleName(), dh);
		    	    
		    	    FileInfo fi = new FileInfo();
		    	    fi.directory = p_File.getParent()+ File.separator;
		    	    fi.fileName = p_File.getName();
		    	    
		    	    try {
		    	    	double rescaleIntercept = dh.getHeaderDoubleValue(Tag.RescaleIntercept);
		    	    	double rescaleSlope = dh.getHeaderDoubleValue(Tag.RescaleSlope);
		    	    	
		    	    	if (fi.fileType==FileInfo.GRAY16_SIGNED) {
		    	    		if (rescaleIntercept!=0.0 && rescaleSlope!=1.0)
		    	    			image.getProcessor().add(rescaleIntercept);
		    	    		else {
		    	    			if (rescaleIntercept!=0.0 && rescaleSlope!=0.0) {
			    	    			double[] coeff = new double[2];
			    	    			coeff[0] = rescaleIntercept;
			    	    			coeff[1] = rescaleSlope;
			    	    			image.getCalibration().setFunction(Calibration.STRAIGHT_LINE, coeff, "gray value");
			    	    		}
		    	    		}
		    	    	} else {
		    	    		if (rescaleIntercept!=0.0 && rescaleSlope!=0.0) {
		    	    			double[] coeff = new double[2];
		    	    			coeff[0] = rescaleIntercept;
		    	    			coeff[1] = rescaleSlope;
		    	    			image.getCalibration().setFunction(Calibration.STRAIGHT_LINE, coeff, "gray value");
		    	    		}
		    	    	}
					} catch (Exception e) {
						System.err.println("error setting image calibration for image: " + p_File);
					}
		    	    
		    	    
		    	    image.setFileInfo(fi);			
		    	} else {
		    	    image =  createDummyImage(p_File, dh, "File does not contain Image Data.") ;
		    	}
			
			return image;
			
		} catch (Throwable e) {
		    
		    
		    
		    
			System.out.println("Unable to open image file: "
					+ p_File.getAbsolutePath() + "\n" + e.getMessage());
			e.printStackTrace();
			
			if (parentComponent != null) {
			    JOptionPane.showMessageDialog(parentComponent,
				    "Unable to open image file: "
					+ p_File.getAbsolutePath() + "\n" + e.getMessage(),
				    "Error loading Image",
				    JOptionPane.ERROR_MESSAGE);
			}
			
			dh = new DicomHeader(p_File);
			return createDummyImage(p_File, dh, e.getLocalizedMessage()) ;
		}
	}

	private static ImagePlus createDummyImage(File file, DicomHeader dh, String message) {
		int rows = 0;
		int cols = 0;
		
		if (! dh.isEmpty()) {			
			rows = dh.getHeaderIntegerValue(Tag.Rows);
			cols = dh.getHeaderIntegerValue(Tag.Columns);
		}
	    
	    if (rows == 0) rows = 512;
	    if (cols == 0) cols = 512;
	    
	    byte[] bArr = new byte[cols*rows];
		for (@SuppressWarnings("unused") byte b : bArr) {
		    b = Byte.MAX_VALUE;
		}
		ImageProcessor iProz = new ByteProcessor(cols, rows, bArr, null);
		
		iProz.setColor(Color.WHITE);
		iProz.setAntialiasedText(true);
		iProz.setFont(new Font("Arial", Font.BOLD, 22));
		iProz.drawString(
//				"Unable to open image:\n" +			
//				"Modality:"+dh.getHeaderStringValue(Tag.Modality)+"\n"+
//				"Size:"+dh.getHeaderIntegerValue(Tag.Rows) + "x" +dh.getHeaderIntegerValue(Tag.Columns)+"\n"+
//				(dh.containsHeaderTag(Tag.TransferSyntaxUID)?"Transfer Syntax:"+dh.getHeaderStringValue(Tag.TransferSyntaxUID)+"\n":"")+
//				"File:" + file.getParentFile().getName() + "/" + file.getName() + "\n" + 
				message, 10, 40);
		
		ImagePlus ip = new ImagePlus(file.getName(), iProz);
		ip.setProperty("Info", dh.toString());
		ip.setProperty(DicomHeader.class.getSimpleName(), dh);

		FileInfo fi = new FileInfo();
		fi.directory = file.getParent()+ File.separator;
		fi.fileName = file.getName();
		ip.setFileInfo(fi);
		
		return ip;
	}
	
	public static boolean isImageIOWorking() {
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
		while (readers.hasNext()) {
			ImageReader imageReader = (ImageReader) readers.next();
			if (imageReader.getClass().getSimpleName().endsWith("CLibJPEGImageReader"))
				return true;	
		}
		return false;
	}
	
	public static String getJavaImageInfos() {
		System.out.println("Gathering Java Infos....");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("OS: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.arch")).append("\n");
		sb.append("Java: ").append(System.getProperty("java.runtime.version")).append(" ").append(System.getProperty("sun.arch.data.model")).append("bit\n");
		System.out.print("\tChecking J3D... ");
		sb.append("Java 3D: ");
		try {
			String version = Install_J3D.getJava3DVersion();
			sb.append(version + "\n");
			System.out.println(version);
		} catch (Throwable e) {
			System.out.println("NOT Installed");
			sb.append("NOT Installed" + "\n");
		}
		
		System.out.print("\tChecking ImageIO");
		sb.append("ImageIO: " + (DicomOpener.isImageIOWorking()?"OK!":"MISSING Native Libs!") + "\n");
		sb.append("JPEG Readers: ");
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
		while (readers.hasNext()) {
			ImageReader imageReader = (ImageReader) readers.next();
			sb.append(imageReader.getClass().getSimpleName()).append(", ");
			System.out.println("\t\t" + imageReader.getClass().getSimpleName());
		}
		sb.append("\n");
		
		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public static void main(String args[]) {
		try {
//			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("jpeg");
//			while (readers.hasNext()) {
//				ImageReader imageReader = (ImageReader) readers.next();
//				System.out.println(imageReader.getClass().getName());
//			}
			
			getJavaImageInfos();
			
			
			loadImage(new File("/home/hermenj/Downloads/test2/1.2.840.113704.6.97392825010140.20001020.41231.11282400032"), null ,null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
