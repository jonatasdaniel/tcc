package lu.tudor.santec.dicom.gui.header;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.DicomObjectToStringParam;
import org.dcm4che2.data.ElementDictionary;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputHandler;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.StringUtils;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomHeader {
	
	private static ElementDictionary dict = ElementDictionary.getDictionary();

	private static Logger logger = Logger
			.getLogger("lu.tudor.santec.dicom.gui.viewer.DicomHeader");

	private static final Pattern tagParser = Pattern.compile("(\\w{4},\\w{4})(\\[(.*?)\\])?");
	
	private static final Pattern timeParser  = Pattern.compile("(\\d{2}):?(\\d{2}):?(\\d{2})(\\.\\d*)?");
	
	
	
	/**
	 * the dicomObject containing the Header
	 */
	private DicomObject dicomObj;

	/**
	 * Flag if Header contains Pixeldata
	 */
	private boolean hasPixelData = false;

	/**
	 * creates a new, empty DicomHeader
	 */
	public DicomHeader() {
	}
	
	/**
	 * reads the dicom header from the given file
	 * if not a dicom file, isEmpty will return true.
	 * 
	 * @param f
	 */
	public DicomHeader(File f) {
		if( ! f.exists() || ! f.canRead() || ! f.isFile()) 
		    return;
		
		readHeaderDCM4CHE(f);
	}
	
	/**
	 * reads the dicom header from the given byte[]
	 * if not a dicom file, isEmpty will return true.
	 * 
	 * @param f
	 */
	public DicomHeader(byte[] data) {
	    if (data == null || data.length == 0)
		return;
	    readHeaderDCM4CHE(data);
	}
	
	/**
	 * reads the dicom header from the given DicomObject
	 * @param dicomObj
	 */
	public DicomHeader(DicomObject dicomObj) {
	    this.dicomObj = dicomObj;
        if (dicomObj.contains(Tag.PixelData)) {
        	hasPixelData = true;
        } else {
        	hasPixelData = false;
        }
	}
	
	
	/**
	 * Reads the dicom Header from the given slice-number of the given ImagePlus, if possible
	 * Please use DicomHeader(File f) instead
	 * @param ipl
	 */
	public DicomHeader(ImagePlus ipl, int sliceNumber) throws DicomHeaderParseException {
		File file = getFile(ipl, sliceNumber);
		
		if (file != null && file.canRead() && file.isFile()) {
			readHeaderDCM4CHE(file);
		} else if (ipl.getProperty(DicomHeader.class.getSimpleName()) != null){
			DicomHeader dh = (DicomHeader) ipl.getProperty(DicomHeader.class.getSimpleName());
			this.dicomObj = dh.dicomObj;
			this.hasPixelData = dh.hasPixelData;
		}
	}
	
	
	/**
	 * Reads the dicom Header from the current slice of the given ImagePlus, if possible
	 * Please use DicomHeader(File f) instead
	 * @param ipl
	 */
	public DicomHeader(ImagePlus ipl) throws DicomHeaderParseException {
		this(ipl, ipl.getCurrentSlice());
	}

	
	public static File getFile(ImagePlus ipl, int sliceNumber) throws DicomHeaderParseException {
		File file = null;
		String path = null;
		String name = null;
		
		try {
			// get Path from fileInfo
			path = ipl.getOriginalFileInfo().directory;
			if (path == null || path.length() == 0) 
				path = ipl.getFileInfo().directory;
			
			if (ipl.getStackSize() == 1) {
				// get name from fileInfo
				name = ipl.getOriginalFileInfo().fileName;
				if (name == null || name.length() == 0) 
					name = ipl.getFileInfo().fileName;
				
				file = new File(path, name);
			} else {
				// get Name from Slice properties
				name = ipl.getStack().getSliceLabel(sliceNumber).split("(\r|\n|\r\n)")[0];	
			}
			
			file = new File(path, name);
			
			if (! file.exists() && file.canRead()) {
				logger.warning("Error original filepath \"" + file.getAbsolutePath() + "\" for ImagePlus " + ipl.getTitle() + " does not exist");
				DicomHeaderParseException ee = new DicomHeaderParseException("Error original filepath \"" + file.getAbsolutePath() + "\" for ImagePlus " + ipl.getTitle() + " does not exist");
				ee.printStackTrace();
				throw ee;
			}
			
		} catch (Exception e) {
			logger.warning("Error getting original filepath for ImagePlus " + ipl.getTitle());
			DicomHeaderParseException ee = new DicomHeaderParseException("Error getting original filepath for ImagePlus " + ipl.getTitle());
			ee.printStackTrace();
			throw ee;
		}
		
		if (file != null)
			logger.fine("FILE:        " + file.getAbsolutePath());

		
		return file;
	}

	/**
	 * @return the dicomObject
	 */
	public DicomObject getDicomObject() {
	    return dicomObj;
	}

	/**
	 * @param dicomObject the dicomObject to set
	 */
	public void setDicomObject(DicomObject dicomObject) {
	    this.dicomObj = dicomObject;
	}
	
	/**
	 * @return the hasPixelData
	 */
	public boolean hasPixelData() {
	    return hasPixelData;
	}

	/**
	 * reads the dicom header from a dicom file
	 * @param f
	 */
	private void readHeaderDCM4CHE(File f) {
	    DicomInputStream in = null;
	    try {
	    	in = new DicomInputStream(f);
	    	PixelDataFoundHandler handler = new PixelDataFoundHandler();
	    	in.setHandler(handler);
	    	DicomObject dcmobj =  in.readDicomObject();
	    	in.close();
	    	if (handler.stopTagFound()) {
	    		hasPixelData = true;
	    		dcmobj.putBytes(Tag.PixelData, VR.OB, null);
	    	} else {
	    		hasPixelData = false;
	    	}
	    	this.dicomObj = dcmobj;
		} catch (Throwable e) {
//		    e.printStackTrace();
		    logger.info("File " + f.getAbsolutePath() +  " has no DICOM Header !!!");
		} finally {
		    try {
		    	if (in != null) in.close();			
		    } catch (Exception e2) {}
		}
	}
	
	/**
	 * reads the dicom header from a byte[]
	 * @param f
	 */
	private void readHeaderDCM4CHE(byte[] data) {
		try {
		    DicomInputStream in = new DicomInputStream(new ByteArrayInputStream(data));
		    PixelDataFoundHandler handler = new PixelDataFoundHandler();
	            in.setHandler(handler);
	            DicomObject dcmobj =  in.readDicomObject();
	            in.close();
	            if (handler.stopTagFound()) {
	            	hasPixelData = true;
	            	dcmobj.putBytes(Tag.PixelData, VR.OB, null);
	            } else {
	            	hasPixelData = false;
	            }
	            this.dicomObj = dcmobj;
		} catch (Throwable e) {
//		    e.printStackTrace();
		    logger.info("given data has no DICOM Header !!!");
		}
	}
	
	/**
	 * reads a double value from the Dicomheader 
	 * @param tagNr the Tag to read
	 * @return the value as double
	 */
	public double getHeaderDoubleValue(int tagNr) {
		try {
			return dicomObj.getDouble(tagNr);			
		} catch (UnsupportedOperationException e) {
			try {
				logger.warning("Tag is not of type double, trying to parse.....");
				return Double.parseDouble(getHeaderStringValue(tagNr));				
			} catch (Exception e2) {
				logger.warning("error getting tag " + toTagString(tagNr));
				return -1;
			}
		} catch (Exception e) {
			logger.warning("error getting tag " + toTagString(tagNr));
			return -1;
		}
	}
	
	/**
	 * 
	 * @param headerNr e.g. "0018,0050" to get Slice Thickness<br>
	 * or "0008,0102#0054,0220" to get the Coding Scheme Designator after View Code Sequence
	 * @return double
	 * @throws DicomHeaderParseException
	 */
	public double getHeaderDoubleValue(String tagNr) throws DicomHeaderParseException {
		return getHeaderDoubleValue(toTagInt(tagNr));
	}
	
	/**
	 * reads a int value from the Dicomheader 
	 * @param tagNr the Tag to read
	 * @return the value as int
	 */
	public int getHeaderIntegerValue(int tagNr) {
		try {
			return dicomObj.getInt(tagNr);			
		} catch (Exception e) {
			logger.warning("error getting tag " + toTagString(tagNr));
			return -1;
		}
	}
		
	/**
	 * 
	 * @param headerNr e.g. "0018,0050" to get Slice Thickness<br>
	 * or "0008,0102#0054,0220" to get the Coding Scheme Designator after View Code Sequence
	 * @return int
	 * @throws DicomHeaderParseException
	 */
	public int getHeaderIntegerValue(String tagNr) throws DicomHeaderParseException {
	    return getHeaderIntegerValue(toTagInt(tagNr));
	}
	
	
	/**
	 * reads a String value from the Dicomheader 
	 * @param tagNr the Tag to read
	 * @return the value as String
	 */
	public String getHeaderStringValue(int tagNr) {
		try {
			DicomElement elem = dicomObj.get(tagNr);
			String[] val = elem.getStrings(dicomObj.getSpecificCharacterSet(),false);
			StringBuffer sb = new StringBuffer();
			for (String string : val) {
				sb.append(string);
				sb.append("/");
			}
			sb.deleteCharAt(sb.length()-1);
			return sb.toString();
		} catch (Exception e) {
		    	try {
		    		DicomElement elem = dicomObj.get(tagNr);
		    		String val = elem.getValueAsString(dicomObj.getSpecificCharacterSet(), 1024);
		    		if (val == null) val = "";
		    		return val;
		    	} catch (Exception ee) {
		    		return "";
		    	}		    	
		}
	}
	
	public String[] getHeaderStringValues(int tagNr) {
	    try {
	    	DicomElement elem = dicomObj.get(tagNr);
	    	String[] val = elem.getStrings(dicomObj.getSpecificCharacterSet(),false);
	    	return val;
	    } catch (Exception e) {
			try {
				DicomElement elem = dicomObj.get(tagNr);
			    String str = elem.getString(dicomObj.getSpecificCharacterSet(),false);
			    return new String[] {str};
			} catch (Exception e2) {
			    return null;
			}
	    }
	}
		
	/**
	 * reads a String value from the Dicomheader 
	 * @param tagNr the Tag to read
	 * @param dcmelement 
	 * @return the value as String
	 */
	public String getHeaderStringValue(DicomElement dcmelement) {
	    try {
			String val = dcmelement.getString(dicomObj.getSpecificCharacterSet(),false);
			if (val == null) val = "";
			return val;
	    } catch (Exception e) {
	    	return "";
	    }
	}

	/**
	 * 
	 * @param headerNr e.g. "0018,0050" to get Slice Thickness<br>
	 * @return String
	 * @throws DicomHeaderParseException 
	 */
	public String getHeaderStringValue(String headerNr) throws DicomHeaderParseException {
	    headerNr = headerNr.replaceAll("x", "0").replaceAll("X", "0");
	    
		if (headerNr.indexOf("#") > 0) {
			try {
				return getHeaderValueInsideTag(headerNr.split("#"))+"";				
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
		}
	    
	    return getHeaderStringValue(toTagInt(headerNr));
	}
	
	@Deprecated
	public Time getHeaderTimeValue(String tagNr) throws DicomHeaderParseException {
	    return getHeaderTimeValue(toTagInt(tagNr));
	}

	@Deprecated
	public Time getHeaderTimeValue(int tagNr) {
		String time = getHeaderStringValue(tagNr);
		if (time.length() != 6)
			return null;

		try {
			int hour = Integer.parseInt(time.substring(0, 2));
			int min = Integer.parseInt(time.substring(2, 4));
			int sec = Integer.parseInt(time.substring(4, 6));
			return new Time(hour, min, sec);
		} catch (Exception e) {
			logger.warning("error parsing time: " + time);
		}
		return null;
	}
	
	public Date getHeaderDateValue(String tagNr) throws DicomHeaderParseException {
	    return getHeaderDateValue(toTagInt(tagNr));
	}

	public Date getHeaderDateValue(int tagNr) {
	    return dicomObj.getDate(tagNr);
	    
	}
	
	
	public HeaderTag getHeaderTag(String tagNr) throws DicomHeaderParseException {
		return getHeaderTag(toTagInt(tagNr));
	}
		
	private HeaderTag getHeaderTag(int tagInt) {
		return new HeaderTag(dicomObj.get(tagInt), dicomObj);
	}

	/**
	 * checks if the Header contains the given tag
	 * @param tagNr
	 * @return
	 * @throws DicomHeaderParseException
	 */
	public boolean containsHeaderTag(String tagNr) throws DicomHeaderParseException {
		if (! tagNr.contains("#")) {
			return containsHeaderTag(toTagInt(tagNr));
		} else {
			return containsHeaderValueInsideTag(tagNr.split("#"));
		}
	}

	private boolean containsHeaderValueInsideTag(String[] tagHierarchy) {
		try {
			DicomObject obj = dicomObj;
			DicomElement elem = null;
			for (int i = 0; i < tagHierarchy.length-1; i++) {
				String tag = tagHierarchy[i];
					elem = obj.get(toTagInt(tag.replaceAll("x", "0").replaceAll("X", "0")));
					obj = elem.getDicomObject();
			}			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * checks if the Header contains the given tag
	 * @param tagNr
	 * @return
	 */
	public boolean containsHeaderTag(int tagNr) {
		try {
			return dicomObj.contains(tagNr);			
		} catch (Exception e) {
			return false;
		}
	}	    
	
	
	/**
	 * retrieves a specific HeaderTag that is inside anotehr tag
	 * or "0008,0102, 0054,0220" to get the Coding Scheme Designator after View Code Sequence
	 * @return String
	 * 	 * @param tagHierarchy; e.g. {Tag.UID, Tag.SOPInstanceUID, Tag.CodeMeaning}
	 * @return
	 */
	public Comparable getHeaderValueInsideTag(int[] tagHierarchy) {
		
		try {
			DicomObject obj = dicomObj;
			for (int i = 0; i < tagHierarchy.length-1; i++) {
				
				obj = obj.get(tagHierarchy[i]).getDicomObject();
			}
			DicomElement elem = obj.get(tagHierarchy[tagHierarchy.length-1]);

			return getHeaderValue(elem);		
		} catch (Exception e) {
			String tags = "";
			for (int i = 0; i < tagHierarchy.length; i++) {
				tags += toTagString(tagHierarchy[i]) + " ";
			}
		    logger.info("DICOM Tag: " + tags + " not found");
		    return "";
	    } 
	}
	
	
	public Comparable getHeaderValue(String tagNr){
		if (tagNr.indexOf("#") > 0) {
			try {
				return getHeaderValueInsideTag(tagNr.split("#"));				
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
		}
		
	    try {
	    	int tag = toTagInt(tagNr);
	    	return getHeaderValue(tag);		
	    } catch (Exception e) {
	    	return null;
	    }
	}
	
	public Comparable getHeaderValue(int tagNr) {
		if (isEmpty())
		    return null;
		
		try {
			DicomElement element = dicomObj.get(tagNr);			
			return getHeaderValue(element);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public Comparable getHeaderValue(DicomElement element) {
	    try {
			if (isEmpty())
			    return null;
			
			if (element == null)
			    return null;
			
			String vr = element.vr().toString();
			int tagNr = element.tag();
			
			if (vr.equals("US") || vr.equals("UL") || vr.equals("SS") || vr.equals("SL") || vr.equals("IS")) {
			    return element.getInt(false);
			} else if (vr.equals("DS") || vr.equals("FL") || vr.equals("FD") || vr.equals("OF")) {
			    return element.getDouble(false);
			} else if (vr.equals("DA")){
			    return element.getDate(false);
			} else if (vr.equals("DT")) {
			    return element.getDate(false);
			} else if (vr.equals("TM")) {
				String timeString = element.getString(dicomObj.getSpecificCharacterSet(),false);
				try {
					Matcher matcher = timeParser.matcher(timeString);
					if (matcher.matches()) {
						timeString = matcher.group(1) + ":"  + matcher.group(2) + ":" + matcher.group(3);
						String muSec = ".000";
						if (matcher.groupCount() == 4 && matcher.group(4) != null) {
							muSec = matcher.group(4);
							if (muSec.length() < 4) {
								while (muSec.length() < 4) {
									muSec += "0";
								}						
							} else {
								muSec = muSec.substring(0, 4);
							}
						} 
						timeString += muSec;
					} 
				} catch (Exception e) {
					logger.info("Error parsing Time");
				}
			    return timeString;
			} else {
				try {
					StringBuffer sb = new StringBuffer();
					String[] values = element.getStrings(dicomObj.getSpecificCharacterSet(),false);
					if (values != null && values.length > 0) {
						sb.append(values[0]);
						for (int i = 1; i < values.length; i++) {
							sb.append("/").append(values[i]);
						}			
					}
					return sb.toString();
				} catch (UnsupportedOperationException e) {
					try {
						return element.getString(dicomObj.getSpecificCharacterSet(),false);					
					} catch (Exception e2) {
						return element.toString();
					}
				}
			}
	    } catch (Throwable e) {
	    	logger.log(Level.WARNING,"Error reading Tag from : " + element,e);
	    }
	    return null;
	}
		
		
	/**
	 * retrieves a specific HeaderTag that is inside anotehr tag
	 * or "0008,0102, 0054,0220" to get the Coding Scheme Designator after View Code Sequence
	 * @return String
	 * @param headerNr
	 * @param parentTagNr
	 * @return
	 * @throws DicomHeaderParseException 
	 */
	public Comparable getHeaderValueInsideTag(String[] tagHierarchy) throws DicomHeaderParseException {

		try {
			DicomObject obj = dicomObj;
			DicomElement elem = null;
			for (int i = 0; i < tagHierarchy.length-1; i++) {
				String tag = tagHierarchy[i];
					elem = obj.get(toTagInt(tag.replaceAll("x", "0").replaceAll("X", "0")));
					obj = elem.getDicomObject();
			}
			
			String tag = tagHierarchy[tagHierarchy.length-1];
			
			DicomElement[] elems = getElement(elem, tag);
//			DicomElement elem = obj.get(toTagInt(tag.replaceAll("x", "0").replaceAll("X", "0")));
			
			if (elems.length == 1) {
				return getHeaderValue(elems[0]);				
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(getHeaderStringValue(elems[0]));
				for (int i = 1; i < elems.length; i++) {
					sb.append("#").append(getHeaderStringValue(elems[i]));
				}
				return sb.toString();		
			}
			
		} catch (Exception e) {
			String tags = "";
			for (int i = 0; i < tagHierarchy.length; i++) {
				tags += tagHierarchy[i] + "#";
			}
		    logger.log(Level.INFO, "DICOM Tag: " + tags + " not found");
		    return "";
	    } 
		

	}
	
	
	private DicomElement[] getElement(DicomElement elem, String tagString) throws DicomHeaderParseException {
		Matcher matcher = tagParser.matcher(tagString);
		matcher.matches();
		tagString = matcher.group(1);
		String nr = matcher.group(3);
		if (nr != null) {
			if (nr.equals("*")) {
				DicomElement[] arr = new DicomElement[elem.countItems()];
				for (int i = 0; i < arr.length; i++) {
					arr[i] = elem.getDicomObject(i).get(toTagInt(matcher.group(1).replaceAll("x", "0").replaceAll("X", "0")));
				}
				return arr;
			} else {
				try {
					return new DicomElement[] 
							{elem.getDicomObject(Integer.parseInt(nr)-1).get(toTagInt(matcher.group(1).replaceAll("x", "0").replaceAll("X", "0")))};					
				} catch (NumberFormatException e) {
					throw new DicomHeaderParseException(tagString);
				}
			}
		} else {
			return new DicomElement[] 
					{elem.getDicomObject().get(toTagInt(tagString.replaceAll("x", "0").replaceAll("X", "0")))};
		}
	}
	

	/**
	 * returns the name of the given Tag
	 * @param tagNr
	 * @return
	 */
	public static String getHeaderName(int tagNr) {
//	    return ElementDictionary.getDictionary().nameOf(tagNr);
	    return dict.nameOf(tagNr);
	}
	
	/**
	 * returns the name of the given Header field
	 * @param tagNr
	 * @return the name of the Field e.g. Patients Name
	 */
	public static String getHeaderName(String tagNr) {
		try {
			return getHeaderName(toTagInt(tagNr));
		} catch (Exception e) {
			logger.info("DICOM Tag: " + tagNr + " not found in Dict");
			return "";
		}
	}
	
	/**
	 * Returns the Value representation of the given header field
	 * @param tagNr
	 * @return the value representation e.g. CS
	 */
	public static String getHeaderFieldType(int tagNr) { 
	    return getHeaderFieldType(toTagString(tagNr));
	}
	
	/**
	 * Returns the Value representation of the given header field
	 * @param tagNr
	 * @return the value representation e.g. CS
	 */
	public static String getHeaderFieldType(String tagNr) {
		try {
			DicomObject de = new BasicDicomObject();
			String val = de.vrOf(toTagInt(tagNr)) +"";
			return val;
//			String val =  DicomDictionary.getDictionary().getProperty(tagNr.replaceAll(",", ""));
//			if (val == null)
//				return "";
//			return val.substring(0,2);
		} catch (Exception e) {
			logger.info("DICOM Tag: " + tagNr + " not found in Dict");
			return "";
		}
	}

        /**
	 * Returns the DCM4CHE VR that is equivalent to the given 
         * text String. This method can be used with the results of 
         * getHeaderFieldType to produce a value that can be used with
         * DCM4CHE classes.
	 * @param tagNr
	 * @return the value representation e.g. CS
         * @author David Pinelle, University of Saskatchewan, Mar 2010
	 */
        public static VR getHeaderFieldTypeAsVR(String vrName) {
            //added by David Pinelle, University of Saskatchewan, Mar 2010
            if (vrName.equalsIgnoreCase("AE")) {
                return VR.AE;
            } else if (vrName.equalsIgnoreCase("AS")) {
                return VR.AS;
            } else if (vrName.equalsIgnoreCase("AT")) {
                return VR.AT;
            } else if (vrName.equalsIgnoreCase("CS")) {
                return VR.CS;
            } else if (vrName.equalsIgnoreCase("DA")) {
                return VR.DA;
            } else if (vrName.equalsIgnoreCase("DS")) {
                return VR.DS;
            } else if (vrName.equalsIgnoreCase("DT")) {
                return VR.DT;
            } else if (vrName.equalsIgnoreCase("FD")) {
                return VR.FD;
            } else if (vrName.equalsIgnoreCase("FL")) {
                return VR.FL;
            } else if (vrName.equalsIgnoreCase("IS")) {
                return VR.IS;
            } else if (vrName.equalsIgnoreCase("LO")) {
                return VR.LO;
            } else if (vrName.equalsIgnoreCase("LT")) {
                return VR.LT;
            } else if (vrName.equalsIgnoreCase("OB")) {
                return VR.OB;
            } else if (vrName.equalsIgnoreCase("OF")) {
                return VR.OF;
            } else if (vrName.equalsIgnoreCase("OW")) {
                return VR.OW;
            } else if (vrName.equalsIgnoreCase("PN")) {
                return VR.PN;
            } else if (vrName.equalsIgnoreCase("SH")) {
                return VR.SH;
            } else if (vrName.equalsIgnoreCase("SL")) {
                return VR.SL;
            } else if (vrName.equalsIgnoreCase("SQ")) {
                return VR.SQ;
            } else if (vrName.equalsIgnoreCase("SS")) {
                return VR.SS;
            } else if (vrName.equalsIgnoreCase("ST")) {
                return VR.ST;
            } else if (vrName.equalsIgnoreCase("TM")) {
                return VR.TM;
            } else if (vrName.equalsIgnoreCase("UI")) {
                return VR.UI;
            } else if (vrName.equalsIgnoreCase("UL")) {
                return VR.UL;
            } else if (vrName.equalsIgnoreCase("UN")) {
                return VR.UN;
            } else if (vrName.equalsIgnoreCase("US")) {
                return VR.US;
            } else if (vrName.equalsIgnoreCase("UT")) {
                return VR.UT;
            } else return null;
        }
	
	/**
	 * returns the String representation of the given header field
	 * if it exists in the header
	 * @param tagNr
	 * @return
	 */
	public String getHeader(int tagNr) {
	    try {
	    	DicomElement dcmele = dicomObj.get(tagNr);
	    	return toElementString(dcmele);
	    } catch (Exception e) {
			logger.info("DICOM Tag: " + toTagString(tagNr) + " not found");
			return "";
	    }
	}
	
	private String toElementString(DicomElement dcmele) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(toTagString(dcmele.tag()))
	    	.append(" [").append(dcmele.vr()).append("] ")
	    	.append(dict.nameOf(dcmele.tag())).append(": ")
	    	.append(getValue(dcmele));
	    return sb.toString();
	}
	
	

	/**
	 * returns a String representation of the value of the given header tag, 
	 * not depending on its data type or if it is an array
	 * @param tag
	 * @return
	 */
	private Object getValue(DicomElement elem) {
		StringBuffer sb = new StringBuffer();
		try {
			String[] values = elem.getStrings(dicomObj.getSpecificCharacterSet(), false);
			sb.append(values[0]);
			for (int i = 1; i < values.length; i++) {
				sb.append("/").append(values[i]);
			}
		} catch (Exception e) {
			try {
				sb.append(elem.getString(dicomObj.getSpecificCharacterSet(), false));
			} catch (Exception e2) {
				return "";
			}
		}
		return sb.toString();
	}

	/**
	 * returns the String representation of the given header field
	 * if it exists in the header
	 * @param tagNr
	 * @return
	 * @throws DicomHeaderParseException 
	 */
	public String getHeader(String tagNr) throws DicomHeaderParseException {
	    return getHeader(toTagInt(tagNr));
	}
	
	public String toString() {
	    DicomObjectToStringParam param = new DicomObjectToStringParam(
	                true,   // name
	                64,     // valueLength;
	                100,     // numItems;
	                100,    // lineLength;
	                Integer.MAX_VALUE,    // numLines;
	                "",     // indent
	                System.getProperty("line.separator", "\n")
		    );
	    StringBuffer sb = new StringBuffer();
	    if (dicomObj == null) {
		return "";
	    }
	    
	    toStringBuffer(dicomObj, sb, param);
	    return sb.toString();
	}

	 public int toStringBuffer(DicomObject obj, StringBuffer sb, DicomObjectToStringParam param) {
	    if (sb == null)
		throw new NullPointerException();   
            if (param == null)
                param = DicomObjectToStringParam.getDefaultParam();
            int lines = 0;
            for (Iterator<DicomElement> it = obj.iterator(); lines < param.numLines && it.hasNext();) {
                DicomElement e = it.next();
                sb.append(param.indent);
                sb.append(toElementString(e));
                sb.append(param.lineSeparator);
                if (e.countItems() > 0) {
                    DicomObjectToStringParam param1 = new DicomObjectToStringParam(
                            param.name, param.valueLength, param.numItems,
                            param.lineLength, param.numLines - lines,
                            param.indent + "   ", param.lineSeparator);
                    if (e.hasDicomObjects())
                        lines += itemsToStringBuffer(e, sb, param1);
                    else
                        lines += fragsToStringBuffer(e, sb, param1);
                }
            }
        
	    return lines;
	    
	}
	
	private int itemsToStringBuffer(DicomElement e, StringBuffer sb,
            DicomObjectToStringParam param) {
        int lines = 0;
        for (int i = 0, n = e.countItems(); i < n && lines < param.numLines; i++) {
            if (++lines == param.numLines) {
                sb.append("...").append(param.lineSeparator);
                break;
            }
            DicomObject item = e.getDicomObject(i);
//            long off = item.getItemOffset();
            sb.append(param.indent);
            sb.append("ITEM #");
            sb.append(i + 1);
            if (i >= param.numItems) {
                sb.append("...").append(param.lineSeparator);
                break;
            }
//            if (off != -1) {
//                sb.append(" @");
//                sb.append(off);
//            }
            sb.append(":");
            sb.append(param.lineSeparator);
            DicomObjectToStringParam param1 = new DicomObjectToStringParam(
                    param.name, param.valueLength, param.numItems,
                    param.lineLength, param.numLines - lines, param.indent + "   ",
                    param.lineSeparator);
            
            lines += toStringBuffer(item, sb, param1);
        }
        return lines;
    }
	
	private static int fragsToStringBuffer(DicomElement e, StringBuffer sb,
            DicomObjectToStringParam param) {
		System.out.println("print a fragment.");
        int lines = 0;
        for (int i = 0, n = e.countItems(); i < n; ++i) {
            if (++lines >= param.numLines) {
                sb.append("...").append(param.lineSeparator);
                break;
            }
            sb.append(param.indent);
            sb.append("ITEM #");
            sb.append(i + 1);
            if (i >= param.numItems) {
                sb.append("...").append(param.lineSeparator);
                break;
            }
            sb.append(" [");
            sb.append((e.getFragment(i).length + 1) & ~1);
            sb.append(" bytes]");
            sb.append(param.lineSeparator);
        }
        return lines;
    }

	/**
	 * checks wether the header is empty or not
	 * @return
	 */
	public boolean isEmpty() {
	    if (dicomObj == null || dicomObj.size() == 0) {
			return true;
	    } 
		return false;
	}

	public DicomHeader clone() {
	    DicomHeader dh = new DicomHeader();
	    dicomObj.copyTo(dh.dicomObj);
	    dh.hasPixelData = hasPixelData;
	    return dh;
	}
	
	/**
	 * converts the string representation of a header number 
	 * e.g. 0008,0010 to the corresponding integer as 0x00080010
	 * as used in the @see org.dcm4che2.data.Tag
	 * @param headerNr e.g. 0008,0010
	 * @return 0x00080010 as int
	 * @throws DicomHeaderParseException 
	 */
	public static int toTagInt(String headerNr) throws DicomHeaderParseException {
	    try {
	    	headerNr = headerNr.replaceAll(",", "");
	    	long tag = Long.parseLong(headerNr, 16);
	    	if (tag > Integer.MAX_VALUE) {
				return (int) ((tag-Integer.MAX_VALUE)*-1);
			} else {
				return (int) tag;
			}
	    	
	    } catch (Exception e) {
//	    	e.printStackTrace();
	    	throw new DicomHeaderParseException("Unable to parse Tag " + headerNr + " to int");
	    }
	}
	
	
	/**
	 * converts the int representation of a header number 
	 * e.g. 0x00080010 to the corresponding String 0008,0010 
	 * @param headerNr e.g. 0x00080010 as int
	 * @return 0008,0010 as String 
	 */
	public static String toTagString(int tagNr) {
	    return StringUtils.shortToHex(tagNr >> 16) + 
		',' + StringUtils.shortToHex(tagNr);
	}
	
	/**
	 * returns the header as Vector of HeaderTags (for compatibility issues
	 * @return
	 */
	public Vector<HeaderTag> getAsHeaderTags() { 
	    Vector<HeaderTag> tags = new Vector<HeaderTag>();
	    for (Iterator<DicomElement> iterator = dicomObj.iterator(); iterator.hasNext();) {
		DicomElement element = (DicomElement) iterator.next();
		HeaderTag ht = new HeaderTag(element, dicomObj);
		tags.add(ht);
	    }
	    return tags;
	}
	
	/**
	 * returns the header as Vector of HeaderTags
	 * @return
	 */
	public Vector<HeaderTag> getHeaderTagsAsFlatList() { 
	    Vector<HeaderTag> tags = new Vector<HeaderTag>();
	    addTags(dicomObj, tags, "");
	    return tags;
	}
	
	
	private void addTags(DicomObject dicomObj, Vector<HeaderTag> tags, String prefix) {
	    for (Iterator<DicomElement> iterator = dicomObj.iterator(); iterator.hasNext();) {
		DicomElement element = (DicomElement) iterator.next();
		HeaderTag ht = new HeaderTag(element, dicomObj);
		ht.tagName = prefix+ht.tagName;
		tags.add(ht);		    
		if (element.hasDicomObjects()) {
		    for (int i = 0; i < element.countItems(); i++) {
			addTags(element.getDicomObject(i), tags, prefix+"  ");
                    }
		}     
	    }
	}
	
	/**
	 * @author Johannes Hermen johannes.hermen(at)tudor.lu
	 */
	private class PixelDataFoundHandler implements DicomInputHandler {

		private boolean tagFound = false;

		public boolean readValue(DicomInputStream in) throws IOException {
		    if (in.tag() == Tag.PixelData) {
			tagFound = true;
		    }
		    
		    if ((in.tag() & 0xffffffffL) >= Tag.PixelData 
			    && in.level() == 0) {
			return false;
		    }
		    return in.readValue(in);
		}

		public boolean stopTagFound() {
		    return tagFound;
		}
		
	}

	public static DicomHeader getHeader(ImagePlus image) {
		// TODO Auto-generated method stub
		try {
			return new DicomHeader(image);
		} catch (DicomHeaderParseException e) {
			logger.log(Level.WARNING, e.getMessage());
			return new DicomHeader();
		}
	}

	public static String getHeaderValue(ImagePlus image, String tag) {
		// TODO Auto-generated method stub
		try {
			return new DicomHeader(image).getHeaderStringValue(tag);
		} catch (DicomHeaderParseException e) {
			logger.log(Level.WARNING, e.getMessage());
			return null;
		}
	}

	
	
	public static void main(String[] argsv) {
		DicomHeader dh = new DicomHeader(new File("/home/hermenj/clemens_CD/DICOM/00007D6E/AAAB2384/AA35EB06/0000FBF3/EE3C10B9"));
		System.out.println(dh.toString());
		
		
		try {
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(dh.getHeaderStringValue("0040,030E"));
			String tags = "0040,030E#0018,9345[*]";
			System.out.println(dh.getHeaderValueInsideTag(tags.split("#")));
		} catch (DicomHeaderParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
	}
	
}

