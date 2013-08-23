package lu.tudor.santec.dicom.exporter;

import ij.ImageStack;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.io.FileInfo;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.*;

import ij.io.ImageWriter;

import java.util.Hashtable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import lu.tudor.santec.dicom.gui.header.DicomHeader;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.iod.value.ImageTypeValue1;
import org.dcm4che2.iod.value.ImageTypeValue2;
import org.dcm4che2.util.UIDUtils;

public class DicomExporter {

	private NumberFormat nf = new DecimalFormat("0000");
    private String charset = "ISO_IR 100";
    private String transferSyntax = UID.ExplicitVRBigEndian;
    private Properties cfg = new Properties();
    Hashtable ht0028 = new Hashtable(); //added Apr 2010

    /**
     * static logger for this class
     */
    private static Logger logger = Logger.getLogger(DicomExporter.class
	    .getName());

    public DicomExporter() {
        try {
            cfg.load(DicomExporter.class.getResourceAsStream("DicomExporter.cfg"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void write(DicomObject header, ImagePlus ip, File dcmFile, boolean multislice) throws IOException {
        //Convenience method. Automatically dispatches the call to the appropriate
        //method based on whether the image is single frame or multiframe.
        //assumption -- createHeader(...) has already been called
        //David Pinelle, University of Saskatchewan, March 2010

        boolean isCompressed = false;
        boolean isStack = false;
        String transSynUID = header.getString(Tag.TransferSyntaxUID);

        if (transSynUID.indexOf("1.2.4")>-1||transSynUID.indexOf("1.2.5")>-1) {
            isCompressed = true;
        }

        if(ip.getStackSize()> 1) {
            isStack = true;
        }

        if (!isStack) {
            writeImage(header, ip, dcmFile);
        } else if (isStack) {
        	if (multislice)
        		writeMultiframe(header, ip, dcmFile);
        	else {
        		int stackSize = ip.getStackSize();
                ImageStack is = ip.getStack();
                for (int i = 1; i<=stackSize; i++) {
                    ImagePlus thisIP = new ImagePlus(new Integer(i).toString(), is.getProcessor(i));
                    header.putString(Tag.InstanceNumber, VR.IS, i+"");
                    createUID(header, Tag.SOPInstanceUID);
                    writeImage(header, thisIP, new File(dcmFile + "_" + nf.format(i) + ".dcm"));
                }
        	}
        }
    }

    public void writeMultiframe(DicomObject header, ImagePlus ip, File dcmFile) throws IOException {
        //This method saves multiframe DICOM as a single file
        //David Pinelle, University of Saskatchewan, March 2010

        int stackSize = ip.getStackSize();
        ImageStack is = ip.getStack();

        FileOutputStream fos = new FileOutputStream(dcmFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DicomOutputStream dos = new DicomOutputStream(bos);

        try {
            dos.writeDicomFile(header);
            int pixelLength = 0;

            FileInfo fi = (new ImagePlus(new Integer(1).toString(), is.getProcessor(1))).getFileInfo();
            Object pixels = fi.pixels;
            if (ip.getBitDepth() == 8) {
                pixelLength = ((byte[])pixels).length * stackSize;
                dos.writeHeader(Tag.PixelData, VR.OB, pixelLength);
            } else if (ip.getBitDepth() == 16) {
                pixelLength = ((short[])pixels).length * 2 * stackSize;
                dos.writeHeader(Tag.PixelData, VR.OW, pixelLength);
            } else if (ip.getBitDepth() == 24) { //handles RGB
                pixelLength = ((int[])pixels).length * 3 * stackSize;
                dos.writeHeader(Tag.PixelData, VR.OW, pixelLength);
            }

            for (int i = 1; i<=stackSize; i++) {
                ImagePlus thisIP = new ImagePlus(new Integer(i).toString(), is.getProcessor(i));
                FileInfo sliceFI = thisIP.getFileInfo();
                ImageWriter iw = new ImageWriter (sliceFI);
                iw.write(dos);

                //if ((pixelLength&1) != 0) {
                if ((pixelLength%2) != 0) {
                    dos.write(0);
                }
            }

            } catch (Exception e ) {
        	e.printStackTrace();
            } finally {
                dos.close();
            }
    }

    public void writeCompressedImage(DicomObject header, ImagePlus ip, File dcmFile) throws IOException {
        //TODO: in the future, this method should be completed so that it compresses and then writes
        //DICOM images
        /*
        * notes by Damien Evans at
        * http://www.dcm4che.org/confluence/display/d1/codecs
        *  switch on TransferSyntaxUID [0002,0010]
        1.2.840.10008.1.2.4.50 	JPEG Baseline (Process 1): Default Transfer Syntax for Lossy JPEG 8 Bit Image Compression
        1.2.840.10008.1.2.4.51 	JPEG Extended (Process 2 & 4): Default Transfer Syntax for Lossy JPEG 12 Bit Image Compression (Process 4 only)
        1.2.840.10008.1.2.4.57 	JPEG Lossless, Non-Hierarchical (Process 14)
        1.2.840.10008.1.2.4.70 	JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]): Default Transfer Syntax for Lossless JPEG Image Compression
        1.2.840.10008.1.2.4.80 	JPEG-LS Lossless Image Compression
        1.2.840.10008.1.2.4.81 	JPEG-LS Lossy (Near-Lossless) Image Compression
        1.2.840.10008.1.2.4.90 	JPEG 2000 Image Compression (Lossless Only)
        1.2.840.10008.1.2.4.91 	JPEG 2000 Image Compression
        1.2.840.10008.1.2.5 	 RLE (Run Length Encoding) Lossles

        1.2.840.10008.1.2.4.50 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.51 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.57 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.70 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.80 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.81 	com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        1.2.840.10008.1.2.4.90 	com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib
        1.2.840.10008.1.2.4.91 	com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib
        1.2.840.10008.1.2.5 	org.dcm4che2.imageioimpl.plugins.rle.RLEImageReader
          */

        String transSynUID = header.getString(Tag.TransferSyntaxUID);

        if (transSynUID.trim().equals("1.2.840.10008.1.2.4.50")) {
            //JPEG Baseline (Process 1): Default Transfer Syntax for Lossy JPEG 8 Bit Image Compression
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.51")) {
            //JPEG Extended (Process 2 & 4): Default Transfer Syntax for Lossy JPEG 12 Bit Image Compression (Process 4 only)
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.57")) {
            //JPEG Lossless, Non-Hierarchical (Process 14)
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.70")) {
            //JPEG Lossless, Non-Hierarchical, First-Order Prediction (Process 14 [Selection Value 1]): Default Transfer Syntax for Lossless JPEG Image Compression
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.80")) {
            //JPEG-LS Lossless Image Compression
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.81")) {
            //JPEG-LS Lossy (Near-Lossless) Image Compression
            //com.sun.media.imageioimpl.plugins.jpeg.CLibJPEGImageReader
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.90")) {
            //JPEG 2000 Image Compression (Lossless Only)
            //com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.4.91")) {
            //JPEG 2000 Image Compression
            //com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderCodecLib
        } else if (transSynUID.trim().equals("1.2.840.10008.1.2.5")) {
            //RLE (Run Length Encoding) Lossles
            //org.dcm4che2.imageioimpl.plugins.rle.RLEImageReader
        }
    }

    public void writeImage(DicomObject header, ImagePlus ip, File dcmFile) throws IOException {
            //write a single (non-stack) image that is not compressed
            FileOutputStream fos = new FileOutputStream(dcmFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DicomOutputStream dos = new DicomOutputStream(bos);
            
            FileInfo fi = ip.getFileInfo();
            Object pixels = fi.pixels;
            ImageWriter iw = new ImageWriter(fi);
            try {
                dos.writeDicomFile(header);
                int pixelLength = 0;

                if (ip.getBitDepth() == 8) {
                    pixelLength = ((byte[])pixels).length;
                    dos.writeHeader(Tag.PixelData, VR.OB, pixelLength);
                } else if (ip.getBitDepth() == 16) {
                    pixelLength = ((short[])pixels).length * 2;
                    dos.writeHeader(Tag.PixelData, VR.OW, pixelLength);
                } else if (ip.getBitDepth() == 24) {
                    pixelLength = ((int[])pixels).length * 3;
                    dos.writeHeader(Tag.PixelData, VR.OW, pixelLength);
                }
                                 
                // write the pixel data
                iw.write(dos);

                if ((pixelLength&1) != 0) {
                    dos.write(0);
                }

            } catch (Exception e ) {
        	e.printStackTrace();
            } finally {
                dos.close();
            }
    }    

    public DicomObject setHeaderFromString (String lociHeader, ImagePlus ip) {
        // This method accepts a properly formatted header string and creates a
        // DicomObject containing the fields / values.
        //
        // Tags starting with 0028 are not used since they refer to the PixelData
        // and may not be valid after opening and editing the image in ImageJ.
        // The 0028 tags / values are stored in the HashTable ht0028.
        // David Pinelle, University of Saskatchewan, April 2010

        DicomObject dcmObj=null;
        if (lociHeader.length()>10) {
            dcmObj = new BasicDicomObject();
            String[] result = lociHeader.split("\\n"); //split into tokens at each new line character

            for (int i=0; i<result.length; i++) {
                try{
                    result[i] = result[i].trim();
                    String tagID = result[i].substring(0,9);
                    String tagValue = result[i].substring(result[i].indexOf(":")+1).trim();
                    int tagAsInt = DicomHeader.toTagInt(tagID);

                    String tagType = DicomHeader.getHeaderFieldType(tagAsInt);
                    VR tagTypeVR = DicomHeader.getHeaderFieldTypeAsVR(tagType);

                    if(!tagID.startsWith("0028")) {
                        dcmObj.putString(tagAsInt, tagTypeVR, tagValue);
                    } else {
                        //Many tags starting with 0028 cause problems (compression is not used, pixel data is
                        //transformed in ImageJ, etc). Store 0028 tags in a HashTable but do not add them to
                        //the DicomObject.
                        if (tagID.equals("0028,0009") || tagID.equals("0028,1040")) {
                            //needed for angio images
                            if (tagID.equals("0028,0009")) {
                                dcmObj.putString(tagAsInt, VR.AT, tagValue);
                            } else if (tagID.equals("0028,1040")) {
                                dcmObj.putString(tagAsInt, VR.CS, tagValue);
                            }
                        } else {
                            ht0028.put(tagID, tagValue);
                        }
                    }
                } catch (Exception e) {
                  //error adding tag
                  //ignore, do not add tag
                }
            }
        }
        return dcmObj;
    }

    public String parseHeaderTagsInImagePlus (ImagePlus ip) {
        //parses header information from "Info" property in ImagePlus
        //The String stored in the property is parsed, entries that are not valid are removed.
        //This also works with DICOM files opened with the LOCI Bioformats plugin
        //update from the end Mar 2010
        //David Pinelle, University of Saskatchewan, April 2010
        String newHeader = new String();
        String originalHeader = (String)ip.getProperty("Info");
        //System.out.println(originalHeader);

        if (originalHeader!=null) {
            String[] result = originalHeader.split("\\n"); //split into tokens at each new line character
            for (int i=0; i<result.length; i++) {
                //check the token to make sure it is valid
                result[i] = result[i].trim();
                Pattern p = Pattern.compile("^\\w{4},\\w{4}\\s"); //check to see if line is valid DICOM
                Matcher m = p.matcher(result[i]);
                boolean isDicom = m.lookingAt();
                if (isDicom) {  //keep line
                    String newEntry = result[i].replace(" =", ":");
                    newHeader = newHeader + newEntry + "\n";
                } else if (result[i].startsWith("Series")) { //handle multiframe syntax used in Bioformats Plugin
                    //remove the Series prefix
                    int splitIndex = result[i].indexOf(" ", 7);
                    String seriesString = result[i].substring(splitIndex+1);
                    //get a new Matcher for the new string, check to see if it is DICOM
                    Matcher m2 = p.matcher(seriesString);
                    boolean isDicom2 = m2.lookingAt();
                    if (isDicom2) {
                        String newEntry = result[i].replace(" =", ":");
                        newHeader = newHeader + newEntry + "\n";
                    }
                } else {
                    //entry is not valid DICOM, discard
                }
            }
        }
        return newHeader;
    }

    public DicomObject processPaletteColorData(DicomObject header, ImagePlus ip) {
        //This method handles PALETTE COLOR images.
        //It relies on tag values that are stored in ht0028, and on the data that
        //are store in the LUT fields in FileInfo. The method accepts a DicomObject,
        //adds all relevant metadata, and then returns the modified DicomObject.
        //David Pinelle, University of Saskatchewan, April 2010

        boolean codeAsSigned = false;
        if (ht0028.contains("0028,0103")) { //check Pixel Representation to determine whether values are signed
            String code = (String)ht0028.get("0028,0103");
            if (code!=null) {
                if (code.trim().equals("1")) {
                    codeAsSigned = true;
                }
            }
        }

        //tags to process
        String paletteArray[] = {
            "0028,1101", //Red Palette Color Lookup Table Descriptor US|SS
            "0028,1102", //Green Palette Color Lookup Table Descriptor US|SS
            "0028,1103", //Blue Palette Color Lookup Table Descriptor US|SS
            "0028,0014", //Ultrasound Color Data Present US
            "0028,1199", //Palette Color Lookup Table UID UI
            "0028,1221", //Segmented Red Palette Color Lookup Table Data OW(US|SS)
            "0028,1222", //Segmented Green Palette Color Lookup Table Data OW(US|SS)
            "0028,1223", //Segmented Blue Palette Color Lookup Table Data OW(US|SS)
            "0028,0006"  //Planar configuration
        };

        //process data for tags 0028,1201; 0028, 1202; 0028, 1203. The data is
        //stored in LUTs in the FileInfo object
        // "0028,1201", //Red Palette Color Lookup Table Data OW(US|SS)
        // "0028,1202", //Green Palette Color Lookup Table Data OW(US|SS)
        // "0028,1203", //Blue Palette Color Lookup Table Data OW(US|SS)
        byte red[] = ip.getFileInfo().reds;
        byte green[] = ip.getFileInfo().greens;
        byte blue[] = ip.getFileInfo().blues;
        byte redOut[] = new byte[red.length*2];
        byte greenOut[] = new byte[green.length*2];
        byte blueOut[] = new byte[blue.length*2];
        byte b = 0;

        if (red!=null && green!=null && blue!=null) {
            //convert LUT data into the proper format
            for (int i = 0; i <red.length; i++) {
                int index = i*2;
                redOut[index+1]=red[i];
                redOut[index]=(byte)(b>>>8);
                blueOut[index+1]=blue[i];
                blueOut[index]=(byte)(b>>>8);
                greenOut[index+1]=green[i];
                greenOut[index]=(byte)(b>>>8);
            }

            try { //add LUT data to DicomObject
                VR vrValue = VR.OW;
                header.putBytes(DicomHeader.toTagInt("0028,1201"), vrValue, redOut, false);
                header.putBytes(DicomHeader.toTagInt("0028,1202"), vrValue, greenOut, false);
                header.putBytes(DicomHeader.toTagInt("0028,1203"), vrValue, blueOut, false);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }

        for (int i=0; i<paletteArray.length; i++) {
            try { //add more tags needed to handle PALETTE COLOR images
                int numberOfValues = 1; // used for 1101-1103, which can have up to 3 values each
                String currentTag = paletteArray[i];
                if (ht0028.containsKey(currentTag)) {
                    int tagAsInt = DicomHeader.toTagInt(currentTag);
                    String valString = (String)ht0028.get(currentTag);

                    //determine VR associated with tag
                    VR vrValue;
                    if ( currentTag.equals("0028,1101") ||
                         currentTag.equals("0028,1102") ||
                         currentTag.equals("0028,1103") ) {
                         //US|SS
                        if (codeAsSigned) {
                            vrValue = VR.US;
                        } else {
                            vrValue = VR.SS;
                        }

                        if (valString.contains(" ")) { //determine # variables stored in tag
                            String count[] = valString.split(" ");
                            numberOfValues = count.length;
                        }
                    } else if ( currentTag.equals("0028,1221") ||
                                currentTag.equals("0028,1222") ||
                                currentTag.equals("0028,1223")) {
                        vrValue = VR.OW;
                    } else if ( currentTag.equals("0028,1199")) {
                        vrValue = VR.UI;
                    } else {
                        vrValue = VR.US;
                    }

                    if (numberOfValues>1) { //0028,1101-1103 can store several values
                        String valueArray[] = valString.split(" ");
                        for (int k=0; k<valueArray.length; k++) { 
                            valueArray[k]=valueArray[k].trim(); //trim each value
                        }
                        header.putStrings(tagAsInt, vrValue, valueArray);
                        numberOfValues=1;
                    } else {
                        header.putString(tagAsInt, vrValue, valString);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return header;
    }

    @SuppressWarnings("unchecked")
    public DicomObject createHeader(ImagePlus ip, boolean derived, boolean keepUID, boolean keepSeriesUID) throws IOException {
        //changed default behavior -- do not look up metadata in file -- not very efficient and
        //can potentially cause problems with web-based deployments where file access may be costly
        //and not always available. File access also causes a problem when working with compressed
        //images that were opened with the Bioformats Plugin. Instead, get tags from "Info" property
        //in ImagePlus. For PALETTE COLOR images, get the Palette Color Lookup Table Data from the
        //LUT fields in FileInfo.
        //David Pinelle, Univ Saskatchewan, Apr 2010

	DicomObject header = new BasicDicomObject();
	header.putString(Tag.SpecificCharacterSet, VR.CS, charset);
	header.putString(Tag.SOPClassUID, VR.UI, UIDUtils.createUID());
	header.putString(Tag.MediaStorageSOPClassUID, VR.UI, UIDUtils.createUID());

        String headerString = parseHeaderTagsInImagePlus(ip);
        DicomObject testObj = setHeaderFromString(headerString, ip);
        if (testObj!=null) {
            header = testObj;
        } else {
            logger.warning("file does not contain a dicom header, using default...");
        }

	//set attributes from the image to the header
	int bitDepth = ip.getBitDepth();
        String originalType = "";
        if( ht0028.containsKey("0028,0004"))
            originalType = ((String)ht0028.get("0028,0004")).trim();

        if (bitDepth == 8) {

            if(originalType.equalsIgnoreCase("PALETTE COLOR")) {   //Read Palette Color metadata, DP April 2010
                header.putString(Tag.PhotometricInterpretation, VR.CS,"PALETTE COLOR");
                header = processPaletteColorData(header, ip);
            } else if (originalType.equalsIgnoreCase("MONOCHROME2")) { //need this for angio images, DP April 2010
                header.putString(Tag.PhotometricInterpretation, VR.CS,"MONOCHROME2");
            } else {
                header.putString(Tag.PhotometricInterpretation, VR.CS,"MONOCHROME");
            }

            header.putInt(Tag.SamplesPerPixel, VR.US, 1);
            header.putInt(Tag.BitsAllocated, VR.US, 8);
            header.putInt(Tag.HighBit, VR.US, 7);
	} else if (bitDepth == 16) {
            if(originalType.equalsIgnoreCase("PALETTE COLOR")) {   //Read Palette Color metadata, DP April 2010
                header.putString(Tag.PhotometricInterpretation, VR.CS,"PALETTE COLOR");
                header = processPaletteColorData(header, ip);
            } else if (originalType.equalsIgnoreCase("MONOCHROME")) {
                header.putString(Tag.PhotometricInterpretation, VR.CS,"MONOCHROME");
            } else {
                header.putString(Tag.PhotometricInterpretation, VR.CS,"MONOCHROME2");
            }

            header.putInt(Tag.SamplesPerPixel, VR.US, 1);
            header.putInt(Tag.BitsAllocated, VR.US, 16);
            header.putInt(Tag.HighBit, VR.US, 15);
	} else if (bitDepth == 24) {
            //modified Mar 2010 by David Pinelle
            //the old BitsAllocated, HighBit values caused the PixelData value to be
            //set too high. This caused viewers to expect PixelData value of (width * length * 3 * 3)
            //instead of the (width * length * 3) size that is produced by the code
            header.putString(Tag.PhotometricInterpretation, VR.CS, "RGB");
            header.putInt(Tag.PlanarConfiguration, VR.US, 0); //added DP
            header.putInt(Tag.SamplesPerPixel, VR.US, 3);
            header.putInt(Tag.BitsAllocated, VR.US, 8); //DP
            header.putInt(Tag.HighBit, VR.US, 7); //DP
	}
        header.putInt(Tag.BitsStored, VR.US, bitDepth);
        header.putInt(Tag.Rows, VR.US, ip.getHeight());
        header.putInt(Tag.Columns, VR.US, ip.getWidth());
        header.putInt(Tag.PixelRepresentation, VR.US, 0);
        header.putInt(Tag.NumberOfFrames, VR.IS, ip.getStackSize());

        ensureUS(header, Tag.BitsAllocated, 8);
        ensureUS(header, Tag.BitsStored, header.getInt(Tag.BitsAllocated));
        ensureUS(header, Tag.HighBit, header.getInt(Tag.BitsStored) - 1);
        ensureUS(header, Tag.PixelRepresentation, 0);
        
        try {
        	double[] d = ip.getCalibration().getCoefficients();
        	header.putString(Tag.RescaleIntercept, VR.DS, d[0] + "");
        	header.putString(Tag.RescaleSlope, VR.DS, d[1] + "");			
		} catch (Exception e) {
			logger.info("No Calibration found in image...");
		}
        
        
        ensureCS(header, Tag.Modality, "SC");

        ensureUID(header, Tag.StudyInstanceUID);

	if (keepSeriesUID) {
	    ensureUID(header, Tag.SeriesInstanceUID);
	} else {
	    createUID(header, Tag.SeriesInstanceUID);
	}

	if (keepUID) {
	    ensureUID(header, Tag.SOPInstanceUID);
	} else {
	    createUID(header, Tag.SOPInstanceUID);
	}
		
	Date now = new Date();
	ensureDate(header, Tag.InstanceCreationDate, VR.DA, now);
	ensureDate(header, Tag.InstanceCreationTime, VR.TM, now);
	
	if (derived) { 	// set ImageType to derived Secondary if wanted
	    header.putString(Tag.ImageType, VR.CS, ImageTypeValue1.DERIVED + "/" + ImageTypeValue2.SECONDARY);
        }

	header.initFileMetaInformation(transferSyntax);
	return header;
    }
    
    private void createUID(DicomObject attrs, int tag) {
         attrs.putString(tag, VR.UI, UIDUtils.createUID());   
    }

    private void ensureUID(DicomObject attrs, int tag) {
        if (!attrs.containsValue(tag)) {
            attrs.putString(tag, VR.UI, UIDUtils.createUID());
        }        
    }

    private void ensureUS(DicomObject attrs, int tag, int val) {
        if (!attrs.containsValue(tag)) {
            attrs.putInt(tag, VR.US, val);
        }        
    }    
    
    private void ensureCS(DicomObject attrs, int tag, String val) {
        if (!attrs.containsValue(tag)) {
            attrs.putString(tag, VR.CS, val);
        }        
    }  

    private void ensureDate(DicomObject attrs, int tag, VR vr , Date val) {
        if (!attrs.containsValue(tag)) {
            attrs.putDate(tag, vr, val);
        }        
    }    
    
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            
            DicomExporter exporter = new DicomExporter();
            
            File dcmFile = new File("/home/hermenj/jhermen.dcm");            
            ImagePlus ip = new ImagePlus("/home/hermenj/im0");
            
            DicomObject dObj = exporter.createHeader(ip, true, true, true);
            exporter.write(dObj, ip, dcmFile, true);
                        
            ImagePlus image = new ImagePlus(dcmFile.getAbsolutePath());
            new ImageJ().show();
            WindowManager.addWindow(new ImageWindow(image));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

