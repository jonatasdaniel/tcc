package lu.tudor.santec.dicom.gui.viewer;
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
import ij.plugin.DICOM;
import ij.process.ImageProcessor;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.ProgressMonitor;

import org.dcm4che2.data.Tag;

import lu.tudor.santec.dicom.DicomOpener;
import lu.tudor.santec.dicom.OverlayExtractor;
import lu.tudor.santec.dicom.gui.header.DicomHeader;


/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class Series implements Serializable {
	
    
    
	private static final long serialVersionUID = 1L;
	private static final int thumbSize = 64;
	private String seriesInfo = "";
	private ArrayList<String> seriesImageFiles = new ArrayList<String>();
	private LinkedHashMap<String, ImagePlus> seriesImages = new LinkedHashMap<String, ImagePlus>();

	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(Series.class.getName());
	
	private double size = 0;
	private int currentImage = 0;
	private String memorySize;
	private NumberFormat nf  = NumberFormat.getInstance();
	
	protected ImagePlus imagePlus;
	protected JToggleButton button;
	protected String ID;

	public Series(File[] imageFiles, boolean inMemory) {
		if (imageFiles == null || imageFiles.length == 0) {
			logger.info("No Images to create Series.....");
			return;				
		}
		ProgressMonitor progressMonitor = new ProgressMonitor(null,"Loading DICOM Series", "preparing",
				0, imageFiles.length);
		progressMonitor.setMillisToDecideToPopup(200);
		ID = System.currentTimeMillis() + "";
		nf.setMaximumFractionDigits(2);

		boolean imageFound = false;
	    // for all other Images
		//System.out.println("series has image files: " + imageFiles.length);
		for (int i = 0; i < imageFiles.length; i++) {
		    
		        if (! imageFiles[i].canRead() || imageFiles[i].isDirectory())  {
		            continue;
		        }
			//System.out.println("adding file: " + i + " (" + imageFiles[i].getAbsolutePath() + ")");
			try {
				if (progressMonitor.isCanceled()) {
					break;
				}
				
				progressMonitor.setNote("img  ( " + (i + 1) + " of " + (imageFiles.length + 1)
						+ " )");
				progressMonitor.setProgress(i);
				
				// load the file if it is not compressed:
//				imagePlus = new ImagePlus(imageFiles[i].getAbsolutePath());
//				 if (imagePlus.getWidth() == 0 && imagePlus.getHeight() == 0) {   // only show error message once
//				     imagePlus = DicomOpener.loadImageStack(imageFiles[i], null, null);
//				 }
				     
				imagePlus = DicomOpener.loadImageStack(imageFiles[i], null, null);
				
				if (imagePlus == null) {
				    return;
				}
				   
				Properties p = imagePlus.getProperties();
				Calibration c = imagePlus.getCalibration();
				size += imageFiles[i].length();
				
				ImageStack is = imagePlus.getStack();
				FileInfo fi = imagePlus.getOriginalFileInfo();
//				System.out.println("file contains " + is.getSize() + " images");
				for (int j = 1; j < is.getSize()+1; j++) {
					
					String imageName = imageFiles[i].getAbsolutePath() + "#" +j;
					seriesImageFiles.add(imageName);
					imagePlus = new ImagePlus(imageName, is.getProcessor(j));
					imagePlus.setFileInfo(fi);
					imagePlus.setCalibration(c);
					// transfer header infos
					try {
						for (Iterator<Object> iter = p.keySet().iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							imagePlus.setProperty(key, p.get(key));
						}						
					} catch (Exception e) {
						System.out.println("File has no DICOM Header...");
							
						e.printStackTrace();
					}
					
					if (! imageFound) {
						// for first image
						
						// test if the image exists
						imagePlus.getProcessor().getMax();
						
						DicomHeader dh = new DicomHeader(new File(imageFiles[i].getAbsolutePath()));
						String name = dh.getHeaderStringValue(Tag.PatientName);
						if (name.length() > 12) {
							name = name.substring(0,11);
						}
						String birthdate = dh.getHeaderStringValue(Tag.PatientBirthDate);
						try {
							birthdate = birthdate.substring(6,8) + "."+ birthdate.substring(4,6) + "." + birthdate.substring(0,4);	
						} catch (Exception e) {
//							System.out.println("image has no birthdate");
						}
						String seriesdate = dh.getHeaderStringValue(Tag.SeriesDate);
						try {
							seriesdate = seriesdate.substring(6,8) + "."+ seriesdate.substring(4,6) + "." + seriesdate.substring(0,4);	
						} catch (Exception e) {
//							System.out.println("image has no seriesdate");
						}
						this.ID = dh.getHeaderStringValue(Tag.SeriesInstanceUID);
						seriesInfo = "Name: " + name + "<br>" +
							"Birthdate: " + birthdate + "<br>" +
							"Sex: " + dh.getHeaderStringValue(Tag.PatientSex) + "  &nbsp;&nbsp;&nbsp;  Modality: " + dh.getHeaderStringValue(Tag.Modality) + "<br>" +
							"Series Date: " + seriesdate + "<br>" +
							"Body Part: " + dh.getHeaderStringValue(Tag.BodyPartExamined) + "</p>" ;
						
//						System.out.println("first Image: " + imageName);
						if (inMemory) {
							this.seriesImages.put(imageName, imagePlus);
						}
					
						imageFound = true;
					} else {
//						System.out.println("adding Image: " + imageName);
						
						if (inMemory) {
							this.seriesImages.put(imageName, imagePlus);
						}
						
					}

				}
				
			} catch (Exception e) {
				logger.log(Level.WARNING, "File has no dicomheader", e);
			}
		}
		progressMonitor.close();
		memorySize = nf.format(size/1024/1024);
		String buttonText = "<html><body><p align=\"left\" >" + 
    	"Slice: " + (currentImage+1) + "/" + seriesImageFiles.size() + " size: " + memorySize +  "mb<br>" +
    	seriesInfo;
    
		button = new JToggleButton(buttonText, getThumb(imagePlus));
	}
	
	public Series(Collection<byte[]> images) {
		if (images == null || images.size() == 0) {
			logger.info("No Images to create Series.....");
			return;				
		}
		ProgressMonitor progressMonitor = new ProgressMonitor(null,"Loading DICOM Series", "preparing",
				0, images.size());
		progressMonitor.setMillisToDecideToPopup(200);
		ID = System.currentTimeMillis() + "";
		nf.setMaximumFractionDigits(2);

		boolean imageFound = false;
	    // for all other Images
//		System.out.println("series has image files: " + images.size());
		int i = 0;
		for (Iterator<byte[]> iter = images.iterator(); iter.hasNext();) {
			
			byte[] imgBytes = (byte[]) iter.next();
//			System.out.println("adding file: " + i + " (" + images.size() + ")");
			try {
				if (progressMonitor.isCanceled()) {
					break;
				}
				
				progressMonitor.setNote("img  ( " + (i + 1) + " of " + (images.size() + 1)
						+ " )");
				progressMonitor.setProgress(i);
				BufferedInputStream bIn = new BufferedInputStream(
						new ByteArrayInputStream(imgBytes));
				DICOM dic = new DICOM(bIn);
				dic.run(i + "");
				imagePlus = dic;
				
				Properties p = imagePlus.getProperties();
				Calibration c = imagePlus.getCalibration();
				size += imgBytes.length;
				
				ImageStack is = imagePlus.getStack();

				for (int j = 1; j < is.getSize()+1; j++) {
					
					String imageName = i + "#" + j ;
					seriesImageFiles.add(imageName);
					imagePlus = new ImagePlus(imageName, is.getProcessor(j));
		
					imagePlus.setCalibration(c);
					// transfer header infos
					try {
						for (Iterator<Object> iter2 = p.keySet().iterator(); iter2.hasNext();) {
							String key = (String) iter2.next();
							imagePlus.setProperty(key, p.get(key));
						}						
					} catch (Exception e) {
						logger.info("File has no dicomheader");
//						System.out.println("File has no dicomheader");
					}
					
					if (! imageFound) {
						// for first image
						
						// test if the image exists
						imagePlus.getProcessor().getMax();
						
						DicomHeader dh = new DicomHeader(imagePlus);
						String name = dh.getHeaderStringValue(Tag.PatientName);
						if (name.length() > 12) {
							name = name.substring(0,11);
						}
						String birthdate = dh.getHeaderStringValue(Tag.PatientBirthDate);
						try {
							birthdate = birthdate.substring(6,8) + "."+ birthdate.substring(4,6) + "." + birthdate.substring(0,4);	
						} catch (Exception e) {
//							System.out.println("image has no birthdate");
						}
						String seriesdate = dh.getHeaderStringValue(Tag.SeriesDate);
						try {
							seriesdate = seriesdate.substring(6,8) + "."+ seriesdate.substring(4,6) + "." + seriesdate.substring(0,4);	
						} catch (Exception e) {
//							System.out.println("image has no seriesdate");
						}
						seriesInfo = "Name: " + name + "<br>" +
							"Birthdate: " + birthdate + "<br>" +
							"Sex: " + dh.getHeaderStringValue(Tag.PatientSex) + "  &nbsp;&nbsp;&nbsp;  Modality: " + dh.getHeaderStringValue(Tag.Modality) + "<br>" +
							"Series Date: " + seriesdate + "<br>" +
							"Body Part: " + dh.getHeaderStringValue(Tag.BodyPartExamined) + "</p>" ;
						
						this.ID = dh.getHeaderStringValue(Tag.SeriesInstanceUID);
						this.seriesImages.put(imageName, imagePlus);
					
						imageFound = true;
					} else {
						this.seriesImages.put(imageName, imagePlus);
					}

				}
				i++;
			} catch (Exception e) {
				logger.log(Level.WARNING, "File has no dicomheader", e);
			}
		}
		progressMonitor.close();
		memorySize = nf.format(size/1024/1024);
		String buttonText = "<html><body><p align=\"left\" >" + 
    	"Slice: " + (currentImage+1) + "/" + seriesImageFiles.size() + " size: " + memorySize +  "mb<br>" +
    	seriesInfo;
    
		button = new JToggleButton(buttonText, getThumb(imagePlus));
	}

	public void next() {
		try {
			String f =(String) this.seriesImageFiles.get(++currentImage);
			imagePlus = (ImagePlus) this.seriesImages.get(f);
			double min=0, max = 0;
			try {
				max = imagePlus.getProcessor().getMax();
				min = imagePlus.getProcessor().getMin();
			} catch (Exception e) {
			}
			
			
			if (imagePlus == null) {
				System.out.println("Loading from file instead of memory");
				//imagePlus = new ImagePlus(f.getAbsolutePath());
			}
			
			if (min!= 0 && max != 0) {
				imagePlus.getProcessor().setMinAndMax(min,max);
			}
			imagePlus.updateImage();

			String buttonText = "<html><body><p align=\"left\" >" + 
	    	"Slice: " + (currentImage+1) + "/" + seriesImageFiles.size() + " size: " + memorySize +  "mb<br>"   +
	    	seriesInfo;
		    button.setText(buttonText);
		    ImageIcon ii = getThumb(imagePlus);
		    if (ii != null) {
		    	button.setIcon(ii);
		    }
		} catch (Exception e) {
			logger.info("No Image for Index: " + currentImage);
			currentImage--;
		}
	}

	public void previous() {
		try {
			String  f = (String) this.seriesImageFiles.get(--currentImage);
			imagePlus = (ImagePlus) this.seriesImages.get(f);
			double min=0, max = 0;
			try {
				max = imagePlus.getProcessor().getMax();
				min = imagePlus.getProcessor().getMin();
			} catch (Exception e) {
			}
			
			if (imagePlus == null) {
				System.out.println("Loading from file instead of memory");
//				imagePlus = new ImagePlus(f.getAbsolutePath());
			}

			if (min!= 0 && max != 0) {
				imagePlus.getProcessor().setMinAndMax(min,max);
			}
			 String buttonText = "<html><body><p align=\"left\" >" + 
		    	"Slice: " + (currentImage+1) + "/" + seriesImageFiles.size() +  " size: " + memorySize +  "mb<br>" +
		    	seriesInfo;
			 button.setText(buttonText);
			 ImageIcon ii = getThumb(imagePlus);
			 if (ii != null) {
				 button.setIcon(ii);
			 }
		} catch (Exception e) {
			logger.info("No Image for Index: " + currentImage);
			currentImage++;
		}			
	}
	
	public ImageIcon getThumb(ImagePlus imagePlus) {
	    	
	    	Image image = null;
	    try {
		DicomHeader dh = new DicomHeader(imagePlus);
		if (OverlayExtractor.hasOverlayImage(dh.getDicomObject())) {
		    image = OverlayExtractor.createOverlayImage(imagePlus, dh.getDicomObject()).getImage();
		} else {
		    image = imagePlus.getImage();
		}
	    } catch (Exception e) {
		e.printStackTrace();
		image = imagePlus.getImage();
	    }
	    
		if (image == null) {
			logger.warning("This is no Image!!!!");
			return null;
		}
		//	determine thumbnail size from WIDTH and HEIGHT
	    int thumbWidth = thumbSize;
	    int thumbHeight = thumbSize;
	    double thumbRatio = (double)thumbWidth / (double)thumbHeight;
	    int imageWidth = image.getWidth(null);
	    int imageHeight = image.getHeight(null);
	    double imageRatio = (double)imageWidth / (double)imageHeight;
	    if (thumbRatio < imageRatio) {
	      thumbHeight = (int)(thumbWidth / imageRatio);
	    } else {
	      thumbWidth = (int)(thumbHeight * imageRatio);
	    }
	    // draw original image to thumbnail image object and
	    // scale it to the new size on-the-fly
	    BufferedImage thumbImage = new BufferedImage(thumbWidth, 
	    thumbHeight, BufferedImage.TYPE_INT_RGB);
	    Graphics2D graphics2D = thumbImage.createGraphics();
	    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
	    return new ImageIcon(thumbImage);
	}
	
	@SuppressWarnings("unchecked")
	public ImagePlus getAsImageStack() {
		ImageStack stack = null;
		ImagePlus imgPlus = null;
		Object info = null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		for (Iterator iter = seriesImages.values().iterator(); iter.hasNext();) {
			ImagePlus ipl = (ImagePlus) iter.next();
			try {
				if (stack==null)
					stack = ipl.createEmptyStack();	
				info = ipl.getProperty("Info");
				ImageProcessor ip = ipl.getProcessor();
				if (ip.getMin()<min) min = ip.getMin();
				if (ip.getMax()>max) max = ip.getMax();
				stack.addSlice(ipl.getTitle(), ip);
				logger.info("Slice: " 	+ ipl.getFileInfo().fileName + "added");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		imgPlus = new ImagePlus("Imported Stack", stack);
		if (imgPlus.getBitDepth()==16 || imgPlus.getBitDepth()==32)
			imgPlus.getProcessor().setMinAndMax(min, max);
            Calibration cal = imgPlus.getCalibration();

		try {
			imgPlus.setProperty("Info",info);						
		} catch (Exception e) {
		}
		return imgPlus;
	}
	

}
