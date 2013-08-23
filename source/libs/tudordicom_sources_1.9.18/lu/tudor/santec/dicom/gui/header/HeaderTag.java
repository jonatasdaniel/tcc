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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.util.StringUtils;

/**
 * represents one Tag of the dicom header with all its properties
 * 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: HeaderTag.java,v $
 * <br>Revision 1.17  2013-05-23 10:00:49  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.16  2013-01-09 08:27:05  hermen
 * <br>bugfixes
 * <br>
 * <br>Revision 1.15  2012-11-13 15:09:09  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.14  2012-11-07 10:07:29  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.13  2011-04-28 12:48:32  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.12  2010-09-22 12:08:48  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.11  2010-05-03 09:46:10  hermen
 * <br>small fixes
 * <br>
 * <br>Revision 1.10  2009-07-09 10:15:09  moll
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.9  2009-07-07 13:20:26  hermen
 * <br>changed handling of DicomHeader
 * <br>
 * <br>Revision 1.8  2009-05-28 12:54:08  hermen
 * <br>changed a lot
 * <br>
 * <br>Revision 1.7  2009-04-07 09:34:14  hermen
 * <br>changed a lot
 * <br>
 *
 */
public class HeaderTag implements Serializable{
		
    	private static final long serialVersionUID = 1L;

		private static Vector<HeaderTag> allTags;

		public String tagType = "";
		
		public String tagNr = null;

		public String tagName = null;

		public String tagValue = null;

		private int length;

		private int location;

		private DicomElement dicomElement;
		
		/**
		 * @return the dicomElement
		 */
		public DicomElement getDicomElement() {
		    return dicomElement;
		}

		/**
		 * @param dicomElement the dicomElement to set
		 */
		public void setDicomElement(DicomElement dicomElement) {
		    this.dicomElement = dicomElement;
		}

		public HeaderTag() {
			
		}
		
		public HeaderTag(String headerLine) {
			if (headerLine != null && headerLine.length() > 9)
				try {
					int idx1 = headerLine.indexOf(" ");
					int idx2 = headerLine.indexOf(": ", idx1);
					tagNr = headerLine.substring(0, idx1);
					tagName = headerLine.substring(idx1 + 2, idx2).trim();
					tagValue = headerLine.substring(idx2 + 2, headerLine.length()).trim();
				} catch (Exception e) {
//					e.printStackTrace();
					tagValue = headerLine;
				}
				else
					tagValue = headerLine;
		}
		
		public HeaderTag(int tagNr) {
			this.tagNr = DicomHeader.toTagString(tagNr);
			this.tagName = DicomHeader.getHeaderName(tagNr);
			this.tagType = DicomHeader.getHeaderFieldType(tagNr);
		}
		
		public HeaderTag(int tagNr, String tagValue) {
			this.tagNr = DicomHeader.toTagString(tagNr);
			this.tagName = DicomHeader.getHeaderName(tagNr);
			this.tagValue = tagValue;
			this.tagType = DicomHeader.getHeaderFieldType(tagNr);
		}
		
		public HeaderTag(String tagNr, String tagName, String tagValue) {
				this.tagNr = tagNr;
				this.tagName = tagName;
				if (tagName == null || tagName.equals("")) {
					this.tagName = DicomHeader.getHeaderName(tagNr);
				} 
				// if not in dict.
				if (this.tagName == null || this.tagName.equals("")) {
					this.tagName = this.tagNr;
				} 
				
				this.tagValue = tagValue;
				this.tagType = DicomHeader.getHeaderFieldType(tagNr);
		}
		
		public HeaderTag(String tagNr, String tagName, String tagValue, String tagType) {
			this.tagNr = tagNr;
			this.tagName = tagName;
			if (tagName == null || tagName.equals("")) {
				this.tagName = DicomHeader.getHeaderName(tagNr);
			}
			this.tagValue = tagValue;
			this.tagType = tagType;
			if (tagType == null || tagType.equals("")) {
			    this.tagType = DicomHeader.getHeaderFieldType(tagNr);			    
			}
		}
		
		public HeaderTag(String tagNr, String tagType, String tagName, String tagValue, int location, int length) {
			this.tagType = tagType;
			if (tagType == null || tagType.equals("")) {
			    this.tagType = DicomHeader.getHeaderFieldType(tagNr);			    
			}
			this.tagNr = tagNr;
			this.tagName = tagName;
			if (tagName == null || tagName.equals("")) {
				this.tagName = DicomHeader.getHeaderName(tagNr);
			}
			if (tagValue != null)
				this.tagValue = tagValue.replace((char)0, ' ');
			this.location = location;
			this.length = length;
	}
		
		public HeaderTag(DicomElement element, DicomObject dObj) {
		    this.dicomElement = element;
		    int tag = element.tag();
			this.tagNr = StringUtils.shortToHex(tag >> 16) + 
				',' + StringUtils.shortToHex(tag);
			this.tagType = element.vr().toString();
			try {
			    if (element.hasDicomObjects()) {
//				System.out.println(element);
				int elemAnz = element.countItems();
//				System.out.println("has " + elemAnz + " subobjects");
				for (int i = 0; i < elemAnz; i++) {
//				    System.out.println(i + " " + element.getDicomObject(i));
				    
				}
				tagValue = "";
			    } else {
				tagValue = element.getString(dObj.getSpecificCharacterSet(),false);		
				if (tagValue == null)
				    tagValue = "";
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
			tagName = DicomHeader.getHeaderName(tagNr);
		}

		public String toString() {
			return tagNr + " " + (tagType!=""?"["+tagType+"] ":"") +tagName + ": " + tagValue;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/**
		 * @return the location
		 */
		public int getLocation() {
			return location;
		}

		/**
		 * @return the tagName
		 */
		public String getTagName() {
			return tagName;
		}

		/**
		 * @param tagName the tagName to set
		 */
		public void setTagName(String tagName) {
			this.tagName = tagName;
		}

		/**
		 * @return the tagNr
		 */
		public String getTagNr() {
			return tagNr;
		}

		/**
		 * @param tagNr the tagNr to set
		 */
		public void setTagNr(String tagNr) {
			this.tagNr = tagNr;
		}

		/**
		 * @return the tagType
		 */
		public String getTagVR() {
			return tagType;
		}

		/**
		 * @param tagType the tagType to set
		 */
		public void setTagType(String tagType) {
			this.tagType = tagType;
		}

		/**
		 * @return the tagValue
		 */
		public String getTagValue() {
			return tagValue;
		}

		/**
		 * @param tagValue the tagValue to set
		 */
		public void setTagValue(String tagValue) {
			this.tagValue = tagValue;
		}
		
		public int getTagInt() throws DicomHeaderParseException {
			return DicomHeader.toTagInt(this.tagNr);
		}
		
//		@SuppressWarnings("unchecked")
//		public static Vector<HeaderTag> getAllTags() {
//		    
//		    if (allTags == null) {
//        		    allTags = new Vector<HeaderTag>();
//        		    
//        		    
//        		    Properties dict  = DicomDictionary.getDictionary();
//        		    SortedSet<String> set = new TreeSet(dict.keySet());
//        		    for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
//        				String tagNr = (String) iter.next();
//        				try {
//        					String name = dict.getProperty(tagNr);
//        					allTags.add(new HeaderTag(
//        							(tagNr.substring(0,4) + "," + tagNr.substring(4,8)),
//        							name.substring(2),
//        							null,
//        							name.substring(0,2)
//        					));							
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//        		    }
//		    }
//		    return allTags;
//		}
		
		@SuppressWarnings("unchecked")
		public static Vector<HeaderTag> getAllTags() {
		    
		    if (allTags == null) {
        		    allTags = new Vector<HeaderTag>();
        		    
        		    Field[] fields = Tag.class.getFields();
        			DicomObject de = new BasicDicomObject();
        			for (int i = 0; i < fields.length; i++) {
        				String name = fields[i].getName();
        				int tag = Tag.forName(name);
        				String tagNr = DicomHeader.toTagString(tag);
        				try {
        					allTags.add(new HeaderTag(
        							tagNr,
        							name,
        							null,
        							de.vrOf(tag)+""
        					));							
						} catch (Exception e) {
							e.printStackTrace();
						}
        		    }
		    }
		    return allTags;
		}
		
		public static Vector<HeaderTag> loadTags(String fileName) {
		    return loadTags(new File(fileName));
		}
		
		public static Vector<HeaderTag> loadTags(File file) {
		    try {
			return loadTags(new FileInputStream(file));
		    } catch (FileNotFoundException e) {
			System.err.println("unable to load tags from file: " + file);
		    }
		    return new Vector<HeaderTag>();
		}
		
		@SuppressWarnings("unchecked")
		public static Vector<HeaderTag> loadTags(InputStream in) {
		    Properties tags = new Properties();
			try {
				tags.load(in);
			} catch (Exception e) {
				System.out.println(e.getLocalizedMessage());
			} 
			Vector<HeaderTag> headerTags = new Vector<HeaderTag>();
			SortedSet<String> set = new TreeSet(tags.keySet());
			for (Iterator<String> iter = set.iterator(); iter.hasNext();) {
				String tagNr = (String) iter.next();
				headerTags.add(new HeaderTag(tagNr, null, (String)tags.get(tagNr)));
			}
			return headerTags;
		}

		public static void saveTags(String fileName, Vector<HeaderTag> headerTags) { 
		    saveTags(new File(fileName), headerTags);
		}
		
		public static void saveTags(File file, Vector<HeaderTag> headerTags) {
		    	try {
			    saveTags(new FileOutputStream(file), headerTags);
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			}
		}
		
		public static void saveTags(OutputStream out, Vector<HeaderTag> headerTags) {
		    try {
			Properties pStore = new Properties();
                	    for (Iterator<HeaderTag> iter = headerTags.iterator(); iter.hasNext();) {
                		HeaderTag element = (HeaderTag) iter.next();
                		if (element.getTagValue() == null) 
                		    pStore.put(element.getTagNr(), "");
                		else
                		    pStore.put(element.getTagNr(), element.getTagValue());
                	    }
    			pStore.store(out, "");
    		    } catch (IOException e) {
    			e.printStackTrace();
    		    }
		}
		
		
		public int tagValuetoInt(String tag) {
		    String[] tags = tag.split(",");
		    String intString = "0x" + tags[0] + tags[1];
		    return Integer.parseInt(intString, 16);
		}
		
		
		public static void main(String[] args) {
			Field[] fields = Tag.class.getFields();
			DicomObject de = new BasicDicomObject();
			for (int i = 0; i < fields.length; i++) {
				String name = fields[i].getName();
				int tag = Tag.forName(name);
				String tagNr = DicomHeader.toTagString(tag);
				de.vrOf(tag);
				System.out.println(tagNr + "; " + de.vrOf(tag) + "; "+ name);
			}
		}
}