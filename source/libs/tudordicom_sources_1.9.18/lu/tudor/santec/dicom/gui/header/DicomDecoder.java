//package lu.tudor.santec.dicom.gui.header;
//
//import ij.io.FileInfo;
//import ij.util.Tools;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Properties;
//import java.util.Vector;
//
//public class DicomDecoder {
//
//	private static final int PIXEL_REPRESENTATION = 0x00280103;
//	private static final int TRANSFER_SYNTAX_UID = 0x00020010;
//	private static final int SLICE_SPACING = 0x00180088;
//	private static final int SAMPLES_PER_PIXEL = 0x00280002;
//	private static final int PHOTOMETRIC_INTERPRETATION = 0x00280004;
//	private static final int PLANAR_CONFIGURATION = 0x00280006;
//	private static final int NUMBER_OF_FRAMES = 0x00280008;
//	private static final int ROWS = 0x00280010;
//	private static final int COLUMNS = 0x00280011;
//	private static final int PIXEL_SPACING = 0x00280030;
//	private static final int BITS_ALLOCATED = 0x00280100;
//	private static final int WINDOW_CENTER = 0x00281050;
//	private static final int WINDOW_WIDTH = 0x00281051;	
//	private static final int RESCALE_INTERCEPT = 0x00281052;
//	private static final int RESCALE_SLOPE = 0x00281053;
//	private static final int RED_PALETTE = 0x00281201;
//	private static final int GREEN_PALETTE = 0x00281202;
//	private static final int BLUE_PALETTE = 0x00281203;
//	private static final int ICON_IMAGE_SEQUENCE = 0x00880200;
//	private static final int ITEM = 0xFFFEE000;
//	private static final int ITEM_DELIMINATION = 0xFFFEE00D;
//	private static final int SEQUENCE_DELIMINATION = 0xFFFEE0DD;
//	private static final int PIXEL_DATA = 0x7FE00010;
//	
//	private static final int AE=0x4145, AS=0x4153, AT=0x4154, CS=0x4353, DA=0x4441, DS=0x4453, DT=0x4454,
//		FD=0x4644, FL=0x464C, IS=0x4953, LO=0x4C4F, LT=0x4C54, PN=0x504E, SH=0x5348, SL=0x534C, 
//		SS=0x5353, ST=0x5354, TM=0x544D, UI=0x5549, UL=0x554C, US=0x5553, UT=0x5554,
//		OB=0x4F42, OW=0x4F57, SQ=0x5351, UN=0x554E, QQ=0x3F3F;
//		
//	public static Properties dictionary;
//
//	private static final int ID_OFFSET = 128;  //location of "DICM"
//	
//	private static final String DICM = "DICM";
//	
//	private BufferedInputStream f;
//	private int location = 0;
//	private boolean littleEndian = true;
//	
//	private int elementLength;
//	private int vr;  // Value Representation
//	private static final int IMPLICIT_VR = 0x2D2D; // '--' 
// 	private boolean dicmFound; // "DICM" found at offset 128
// 	private boolean oddLocations;  // one or more tags at odd locations
// 	private boolean bigEndianTransferSyntax = false;
//	double windowCenter, windowWidth;
//	double rescaleIntercept, rescaleSlope;
//	boolean inSequence;
//	
//	private Vector<HeaderTag> headerTags = new Vector<HeaderTag>();
//	private boolean transferSyntaxFound = false;
//
//	public DicomDecoder() {
//		if (dictionary==null) {
//			dictionary = DicomDictionary.getDictionary();
//		}
//	}
//
//	String getString(int length) throws IOException {
//		byte[] buf = new byte[length];
//		int pos = 0;
//		while (pos<length) {
//			int count = f.read(buf, pos, length-pos);
////			System.out.println("reading " + length + "  bytes " + location + " " + (length-pos));
//			pos += count;
//		}
//		location += length;
//		return new String(buf);
//	}
//  
//	int getByte() throws IOException {
//		int b = f.read();
//		if (b ==-1) throw new IOException("unexpected EOF");
////		System.out.println(location);
//		++location;
//		return b;
//	}
//
//	int getShort() throws IOException {
//		int b0 = getByte();
//		int b1 = getByte();
//		if (littleEndian)
//			return ((b1 << 8) + b0);
//		else
//			return ((b0 << 8) + b1);
//	}
//  
//	final int getInt() throws IOException {
//		int b0 = getByte();
//		int b1 = getByte();
//		int b2 = getByte();
//		int b3 = getByte();
//		if (littleEndian)
//			return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
//		else
//			return ((b0<<24) + (b1<<16) + (b2<<8) + b3);
//	}
//	
//	double getDouble() throws IOException {
//		int b0 = getByte();
//		int b1 = getByte();
//		int b2 = getByte();
//		int b3 = getByte();
//		int b4 = getByte();
//		int b5 = getByte();
//		int b6 = getByte();
//		int b7 = getByte();
//		
//		long res = 0;
//		
//		if (littleEndian) {
//			res += b0;
//			res += ( ((long)b1) << 8);
//			res += ( ((long)b2) << 16);
//			res += ( ((long)b3) << 24);
//			res += ( ((long)b4) << 32);
//			res += ( ((long)b5) << 40);
//			res += ( ((long)b6) << 48);
//			res += ( ((long)b7) << 56);		    
//		}
//		else {
//		    	res += b7;
//			res += ( ((long)b6) << 8);
//			res += ( ((long)b5) << 16);
//			res += ( ((long)b4) << 24);
//			res += ( ((long)b3) << 32);
//			res += ( ((long)b2) << 40);
//			res += ( ((long)b1) << 48);
//			res += ( ((long)b0) << 56);
//		}
//		return Double.longBitsToDouble(res);
//	}
//	
//	float getFloat() throws IOException {
//		int b0 = getByte();
//		int b1 = getByte();
//		int b2 = getByte();
//		int b3 = getByte();
//				
//		int res = 0;
//		
//		if (littleEndian) {
//			res += b0;
//			res += ( ((long)b1) << 8);
//			res += ( ((long)b2) << 16);
//			res += ( ((long)b3) << 24);	    
//		}
//		else {
//		    	res += b3;
//			res += ( ((long)b2) << 8);
//			res += ( ((long)b1) << 16);
//			res += ( ((long)b0) << 24);
//		}
//		return Float.intBitsToFloat(res);
//	}
//	
//  
//	byte[] getLut(int length) throws IOException {
//		if ((length&1)!=0) { // odd
//			getString(length);
//			return null;
//		}
//		length /= 2;
//		byte[] lut = new byte[length];
//		for (int i=0; i<length; i++)
//			lut[i] = (byte)(getShort()>>>8);
//		return lut;
//	}
//  
//  	int getLength() throws IOException {
//		int b0 = getByte();
//		int b1 = getByte();
//		int b2 = getByte();
//		int b3 = getByte();
//		
//		// We cannot know whether the VR is implicit or explicit
//		// without the full DICOM Data Dictionary for public and
//		// private groups.
//		
//		// We will assume the VR is explicit if the two bytes
//		// match the known codes. It is possible that these two
//		// bytes are part of a 32-bit length for an implicit VR.
//		
//		vr = (b0<<8) + b1;
//		
//		switch (vr) {
//			case OB: case OW: case SQ: case UN:
//				// Explicit VR with 32-bit length if other two bytes are zero
//					if ( (b2 == 0) || (b3 == 0) ) return getInt();
//				// Implicit VR with 32-bit length
//				vr = IMPLICIT_VR;
//				if (littleEndian)
//					return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
//				else
//					return ((b0<<24) + (b1<<16) + (b2<<8) + b3);		
//			case AE: case AS: case AT: case CS: case DA: case DS: case DT:  case FD:
//			case FL: case IS: case LO: case LT: case PN: case SH: case SL: case SS:
//			case ST: case TM:case UI: case UL: case US: case UT: case QQ:
//				// Explicit vr with 16-bit length
//				if (littleEndian)
//					return ((b3<<8) + b2);
//				else
//					return ((b2<<8) + b3);
//			default:
//				// Implicit VR with 32-bit length...
//				vr = IMPLICIT_VR;
//				if (littleEndian)
//					return ((b3<<24) + (b2<<16) + (b1<<8) + b0);
//				else
//					return ((b0<<24) + (b1<<16) + (b2<<8) + b3);
//		}
//	}
//
//	int getNextTag() throws IOException {
//		int groupWord = getShort();
//		if (groupWord==0x0800 && bigEndianTransferSyntax) {
//			littleEndian = false;
//			groupWord = 0x0008;
//		}
//		int elementWord = getShort();
//		int tag = groupWord<<16 | elementWord;
//		elementLength = getLength();
//		
//		// hack needed to read some GE files
//		// The element length must be even!
//		if (elementLength==13 && !oddLocations) elementLength = 10; 
//		
//		// "Undefined" element length.
//		// This is a sort of bracket that encloses a sequence of elements.
//		if (elementLength==-1) {
//			elementLength = 0;
//			inSequence = true;
//		}
// 		//IJ.log("getNextTag: "+tag+" "+elementLength);
//		return tag;
//	}
//  
//	public Vector<HeaderTag> getDicomHeader(File file) throws IOException  {
//		
//		elementLength = 0;
//		vr = 0;
//	 	dicmFound = false;
//	 	oddLocations = false;
//	 	bigEndianTransferSyntax = false;
//		windowCenter = 0;
//		windowWidth  = 0;
//		rescaleIntercept = 0;
//		rescaleSlope = 0;
//		inSequence = false;
//
//	    	this.headerTags.clear();
//		transferSyntaxFound = false;
//		
//		
////		long start = System.currentTimeMillis();
//		
//		long skipCount = 0;
//
//		int bitsAllocated = 16;
//		int samplesPerPixel = 1;
//		int planarConfiguration = 0;
//		String photoInterpretation = "";
//			
//		f = new BufferedInputStream(new FileInputStream(file));
//		
//		skipCount = (long)ID_OFFSET;
//		
//		while (skipCount > 0) 
//			skipCount -= f.skip( skipCount );
//		
//		location += ID_OFFSET;
//		
//		if (!getString(4).equals(DICM)) {
//			f.close();
//			f = new BufferedInputStream(new FileInputStream(file));
////			System.out.println(DICM + " not found at offset "+location+"; reseting to offset 0");
//			location = 0;
////			return "";
//		} else {
//			dicmFound = true;
////			System.out.println(DICM + " found at offset " + location);
//		}
//		
//		boolean decodingTags = true;
//		
//		while (decodingTags) {
//			int tag = getNextTag();
//			if (elementLength > 10000 && tag != PIXEL_DATA) {
//			    System.out.println("Error reading dicom header, tags \"" + tag2hex(tag) + "\" length is :" + elementLength);
//			    decodingTags = false;
//			    continue;
//			}
//			
//			if ((location&1)!=0) // DICOM tags must be at even locations
//				oddLocations = true;
//			if (inSequence && tag != PIXEL_DATA) {
//				addInfo(tag, null, location, elementLength);
//				continue;
//			}
//			String s;
//			switch (tag) {
//				case TRANSFER_SYNTAX_UID:
//					s = getString(elementLength);
//					addInfo(tag, s, location, elementLength);
////					if (s.indexOf("1.2.4")>-1||s.indexOf("1.2.5")>-1) {
////						f.close();
////						String msg = "ImageJ cannot open compressed DICOM images.\n \n";
////						msg += "Transfer Syntax UID = "+s;
////						throw new IOException(msg);
////					}
//					if (s.indexOf("1.2.840.10008.1.2.2")>=0)
//						bigEndianTransferSyntax = true;
//					transferSyntaxFound = true;
//					break;
//				case NUMBER_OF_FRAMES:
//					s = getString(elementLength);
//					addInfo(tag, s, location, elementLength);
//					break;
//				case SAMPLES_PER_PIXEL:
//					samplesPerPixel = getShort();
//					addInfo(tag, samplesPerPixel, location, elementLength);
//					break;
//				case PHOTOMETRIC_INTERPRETATION:
//					photoInterpretation = getString(elementLength);
//					addInfo(tag, photoInterpretation, location, elementLength);
//					break;
//				case PLANAR_CONFIGURATION:
//					planarConfiguration = getShort();
//					addInfo(tag, planarConfiguration, location, elementLength);
//					break;
//				case ROWS:
//					addInfo(tag, getShort(), location, elementLength);
//					break;
//				case COLUMNS:
//					addInfo(tag, getShort(), location, elementLength);
//					break;
//				case PIXEL_SPACING:
//					addInfo(tag, getString(elementLength), location, elementLength);
//					break;
//				case SLICE_SPACING:
//					addInfo(tag, getString(elementLength), location, elementLength);
//					break;
//				case BITS_ALLOCATED:
//					bitsAllocated = getShort();
//					addInfo(tag, bitsAllocated, location, elementLength);
//					break;
//				case PIXEL_REPRESENTATION:
//					int pixelRepresentation = getShort();
//					addInfo(tag, pixelRepresentation, location, elementLength);
//					break;
//				case WINDOW_CENTER:
//					String center = getString(elementLength);
//					int index = center.indexOf('\\');
//					if (index!=-1) center = center.substring(index+1);
//					windowCenter = s2d(center);
//					addInfo(tag, center, location, elementLength);
//					break;
//				case WINDOW_WIDTH:
//					String width = getString(elementLength);
//					index = width.indexOf('\\');
//					if (index!=-1) width = width.substring(index+1);
//					windowWidth = s2d(width);
//					addInfo(tag, width, location, elementLength);
//					break;
//				case RESCALE_INTERCEPT:
//					String intercept = getString(elementLength);
//					rescaleIntercept = s2d(intercept);
//					addInfo(tag, intercept, location, elementLength);
//					break;
//				case RESCALE_SLOPE:
//					String slop = getString(elementLength);
//					rescaleSlope = s2d(slop);
//					addInfo(tag, slop, location, elementLength);
//					break;
//				case RED_PALETTE:
//					addInfo(tag, elementLength/2, location, elementLength);
//					break;
//				case GREEN_PALETTE:
//					addInfo(tag, elementLength/2, location, elementLength);
//					break;
//				case BLUE_PALETTE:
//					addInfo(tag, elementLength/2, location, elementLength);
//					break;
//				case PIXEL_DATA:
//					// Start of image data...
////					System.out.println("start of imagedata " + location);
//					if (elementLength!=0) {
//						addInfo(tag, location, location, elementLength);
//					} else {
//						addInfo(tag, null, location, elementLength);
//					}
//					decodingTags = false;
//					break;
//				case 0x7F880010:
//					// What is this? - RAK
//					if (elementLength!=0) {
//						decodingTags = false;
//					}
//					break;
//				case 40:
//					// fixes out of mem on some pics
//					decodingTags = false;
//					break;
//				default:
//					// Not used, skip over it...
//					addInfo(tag, null, location, elementLength);
//			}
//		} // while(decodingTags)
//	
//		f.close();
////		System.out.println("--------------------------------------------------------------");
//		return getDicomInfo();
//	}
//	
//	Vector<HeaderTag> getDicomInfo() {
//		return headerTags ;
//	}
//
//	void addInfo(int tag, String value, int location, int length) throws IOException {
////		System.out.println("addInfo(): " + tag + "; " + value + "; " + length);
//		if (!transferSyntaxFound && length > 2048) {
////			System.out.println("no valid dicom Tag at pos: " + location);
//			throw new IOException("no valid dicom Tag at pos: " + location);
//		}
//		HeaderTag headerTag = getHeaderInfo(tag, value, location, length);
////		System.out.println("\t" + headerTag);
//		// TODO
////		if (inSequence && info!=null && vr!=SQ) info = ">" + info;
//		
////		if (headerTag!=null &&  tag!=ITEM) {
////			dicomInfo.append(tag2hex(tag)+info+"\n");
////		}		
//		
//		if (headerTag != null) {
//			headerTags.add(headerTag);
//		}
//	}
//
//	void addInfo(int tag, int value, int location, int length) throws IOException {
//		addInfo(tag, Integer.toString(value), location, length);
//	}
//
//	@SuppressWarnings("fallthrough")
//	HeaderTag getHeaderInfo(int tag, String value, int location, int length) throws IOException {
//		
//	    	if (tag==ITEM_DELIMINATION || tag==SEQUENCE_DELIMINATION) {
//			inSequence = false;
//		}
//		String key = i2hex(tag);
//		//while (key.length()<8)
//		//	key = '0' + key;
//		String id = (String)dictionary.get(key);
//		
//		String keyString = key.substring(0,4) + "," + key.substring(4,8);
//		String type = "";
//		if (id!=null) {
//			if (vr==IMPLICIT_VR && id!=null)
//				vr = (id.charAt(0)<<8) + id.charAt(1);
//			type = id.substring(0,2);
//			id = id.substring(2);
//		}
//		if (tag==ITEM)
//			return id!=null?new HeaderTag(keyString, type, id, "", location, length):null;
//		if (value!=null)
//			return new HeaderTag(keyString, type, id, value, location, length);
//		switch (vr) {
//			case FD:
//        			value = Double.toString(getDouble());
//        			break;
//			case FL:
//    			value = Float.toString(getFloat());
//    			break;
//			case AE: case AS: case AT: case CS: case DA: case DS: case DT:  case IS: case LO: 
//			case LT: case PN: case SH: case ST: case TM: case UI:
//				value = getString(elementLength);
//				break;
//			case US:
//				if (elementLength==2)
//					value = Integer.toString(getShort());
//				else {
//					value = "";
//					int n = elementLength/2;
//					for (int i=0; i<n; i++)
//						value += Integer.toString(getShort())+" ";
//				}
//				break;
//			case IMPLICIT_VR:
//				value = getString(elementLength);
//				if (elementLength>44) value=null;
//				break;
//			case SQ:
//				value = "";
//				boolean privateTag = ((tag>>16)&1)!=0;
//				if (tag!=ICON_IMAGE_SEQUENCE && !privateTag)
//					break;		
//				// else fall through and skip icon image sequence or private sequence
//			default:
//				long skipCount = (long)elementLength;
//				while (skipCount > 0) skipCount -= f.skip(skipCount);
//				location += elementLength;
//				value = "";
//		}
//		if (value!=null && id==null && !value.equals(""))
//			return new HeaderTag(keyString, "", "", value, location, length);
//		else if (id==null)
//			return null;
//		else
//			return new HeaderTag(keyString, type, id, value, location, length);
//	}
//
//	static char[] buf8 = new char[8];
//	
//	/** Converts an int to an 8 byte hex string. */
//	String i2hex(int i) {
//		for (int pos=7; pos>=0; pos--) {
//			buf8[pos] = Tools.hexDigits[i&0xf];
//			i >>>= 4;
//		}
//		return new String(buf8);
//	}
//
//	char[] buf10;
//	
//	String tag2hex(int tag) {
//		if (buf10==null) {
//			buf10 = new char[11];
//			buf10[4] = ',';
//			buf10[9] = ' ';
//		}
//		int pos = 8;
//		while (pos>=0) {
//			buf10[pos] = Tools.hexDigits[tag&0xf];
//			tag >>>= 4;
//			pos--;
//			if (pos==4) pos--; // skip coma
//		}
//		return new String(buf10);
//	}
//	
// 	double s2d(String s) {
//		Double d;
//		try {d = new Double(s);}
//		catch (NumberFormatException e) {d = null;}
//		if (d!=null)
//			return(d.doubleValue());
//		else
//			return(0.0);
//	}
//  
//	void getSpatialScale(FileInfo fi, String scale) {
//		double xscale=0, yscale=0;
//		int i = scale.indexOf('\\');
//		if (i>0) {
//			yscale = s2d(scale.substring(0, i));
//			xscale = s2d(scale.substring(i+1));
//		}
//		if (xscale!=0.0 && yscale!=0.0) {
//			fi.pixelWidth = xscale;
//			fi.pixelHeight = yscale;
//			fi.unit = "mm";
//		}
//	}
//	
//	boolean dicmFound() {
//		return dicmFound;
//	}
//	
//	
//	@SuppressWarnings("unchecked")
//	public class RawHeaderField implements Comparable{
//		private String id;
//		private String name;
//		private int start;
//		private int length;
//		private String value;
//
//		public RawHeaderField(String id, String name, String value, int start, int end) {
//			this.id = id;
//			this.name = name;
//			this.start = start;
//			this.length = end;
//			this.value = value;
//		}
//
//		/**
//		 * @return the end
//		 */
//		public int getLength() {
//			return length;
//		}
//
//		/**
//		 * @param end the end to set
//		 */
//		public void setLength(int end) {
//			this.length = end;
//		}
//
//		/**
//		 * @return the id
//		 */
//		public String getId() {
//			return id;
//		}
//
//		/**
//		 * @param id the id to set
//		 */
//		public void setId(String id) {
//			this.id = id;
//		}
//
//		/**
//		 * @return the start
//		 */
//		public int getStart() {
//			return start;
//		}
//
//		/**
//		 * @param start the start to set
//		 */
//		public void setStart(int start) {
//			this.start = start;
//		}
//
//		/**
//		 * @return the value
//		 */
//		public String getValue() {
//			return value;
//		}
//
//		/**
//		 * @param value the value to set
//		 */
//		public void setValue(String value) {
//			this.value = value;
//		}
//		
//		public String toString() {
//			return id.substring(0,4) + "," + id.substring(4) + " " + name + " start:" + start + " length:" + length;
//		}
//
//		/**
//		 * @return the name
//		 */
//		public String getName() {
//			return name;
//		}
//
//		/**
//		 * @param name the name to set
//		 */
//		public void setName(String name) {
//			this.name = name;
//		}
//
//		public int compareTo(Object o) {
//			if (o == null)
//				return 0;
//			if (this.start <= ((RawHeaderField)o).start )
//				return -1;
//			else 
//				return 1;
//		}
//			
//	}
//
//}

