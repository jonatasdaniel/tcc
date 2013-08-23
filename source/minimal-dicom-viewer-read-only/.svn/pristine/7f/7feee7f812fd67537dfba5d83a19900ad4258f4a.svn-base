/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <ImageGray16Bit.java> is part of Minimal Dicom Viewer.
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
 * Released date: 13-11-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

public class ImageGray16Bit {

	private int width = 0;
	
	/**
	 * Image height that correspond to
	 * the number of row.
	 */
	private int height = 0;
	
	/**
	 * Image data.
	 */
	private int[] imageData = null;
	private int[] originalImageData = null;
	
	private String patientName;
	private String patientPrename;
	private String patientBirth;
	
	
	public void setImageData(int[] imageData)
	{
		this.imageData = imageData;
	}
	
	public int[] getImageData()
	{
		return imageData;
	}
	
	// make a safe copy of the image data 
	public void setOriginalImageData(int[] imageData)
	{
		if(imageData == null)
			this.originalImageData = imageData;
		this.originalImageData = new int[imageData.length];
		System.arraycopy(imageData, 0, this.originalImageData, 0, imageData.length);
	}
	
	// return only a copy of the original image data - leave the original image data unchanged
	public int[] getOriginalImageData()
	{
		if(this.originalImageData == null)
			return originalImageData;
		int result[] = new int[this.originalImageData.length];
		System.arraycopy(originalImageData, 0, result, 0, this.originalImageData.length);
		return result;
	}
	
	
	public void setWidth(int width)
	{
		this.width = width;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void setPatientName(String value)
	{
		this.patientName = value;
	}
	public void setPatientPrename(String value)
	{
		this.patientPrename = value;
	}
	public void setPatientBirth(String value)
	{
		this.patientBirth = value;
	}
	
	public String getPatientName()
	{
		return patientName;
	}
	public String getPatientPrename()
	{
		return patientPrename;
	}
	public String getPatientBirth()
	{
		return patientBirth;
	}
}
