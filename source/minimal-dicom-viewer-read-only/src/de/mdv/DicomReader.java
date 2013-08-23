/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <DicomReader.java> is part of Minimal Dicom Viewer.
 *
 * Minimal Dicom Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minimal Dicom Viewer is distributed as Open Source Software ( OSS )
 * and comes WITHOUT ANY WARRANTY and even with no IMPLIED WARRANTIES OF MERCHANTABILITY,
 * OF SATISFACTORY QUALITY, AND OF FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License ( GPLv3 ) for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Minimal Dicom Viewer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Released date: 02-12-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

import java.text.DateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;

public class DicomReader {

	
	BasicDicomObject bdo;
	DicomInputStream dis;
	int pixelData[] = null;
	int width, height;
	
	String PatientName = "";
	String PatientPrename = "";
	Date PatientBirth = null;
	String PatientBirthString = "";
	
	public DicomReader(String fileName)
	{
		this.init(fileName);
	}
	
	
	private void init(String fileName)
	{
		try
		{
			bdo = new BasicDicomObject();
			dis = new DicomInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(fileName)));
			dis.readDicomObject(bdo, -1);
			height = bdo.getInt(org.dcm4che2.data.Tag.Rows);
	    	width = bdo.getInt(org.dcm4che2.data.Tag.Columns);
	    	boolean invert = bdo.get(0x00280004).toString().toUpperCase().endsWith("[MONOCHROME1]") ? true : false;
	    	
	    	String completeName = bdo.getString(org.dcm4che2.data.Tag.PatientName);
	    	StringTokenizer tokenizer = new StringTokenizer(completeName, "^");
	    	int counter = 0;
	    	while(tokenizer.hasMoreElements())
	    	{
	    		if(counter == 0)
	    			PatientName = tokenizer.nextToken();
	    		else if(counter == 1)
	    			PatientPrename = tokenizer.nextToken();
	    		counter++;
	    	}
	    	PatientBirth = bdo.getDate(org.dcm4che2.data.Tag.PatientBirthDate);
	    	if(PatientBirth != null)
	    	{
	    		PatientBirthString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(PatientBirth);
	    	}
	    	int bitsAllocated = bdo.getInt(org.dcm4che2.data.Tag.BitsAllocated);
	    	if(bitsAllocated == 8 || bitsAllocated == 12 || bitsAllocated == 16)
	    	{
	    		byte bytePixels[] = DicomHelper.readPixelData(bdo);
	    		pixelData = DicomHelper.convertToIntPixelData(bytePixels, bitsAllocated, width, height, invert);
	    	}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new IllegalArgumentException(ex.getCause());
		}
	}
	
	
	public int[] getPixelData()
	{
		return pixelData;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public String getPatientName()
	{
		return PatientName;
	}
	
	public String getPatientPrename()
	{
		return PatientPrename;
	}
	public String getPatientBirthString()
	{
		return PatientBirthString;
	}
}
