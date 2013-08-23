/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <DicomHelper.java> is part of Minimal Dicom Viewer.
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

import java.io.ByteArrayOutputStream;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.SequenceDicomElement;
import org.dcm4che2.data.Tag;

public class DicomHelper {

	public static byte[] readPixelData(DicomObject dcmObj)
	{
		DicomElement dicomElement = dcmObj.get(Tag.PixelData);
        if(dicomElement instanceof SequenceDicomElement)
        {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	SequenceDicomElement sde = (SequenceDicomElement)dicomElement;
        	int count = sde.countItems();
        	for(int i = 0; i < count; i++)
        	{
        		Object bts = sde.getObject(i);
        		if(bts instanceof byte[])
        		{
        			byte bytes[] = (byte[])bts;
        			try
        			{
        				baos.write(bytes);
        			}
        			catch(Exception ex)
        			{
        				throw new IllegalStateException(ex.getMessage());
        			}
        		}
        	}
        	return baos.toByteArray();
        }
        else 
        {
        	if(dicomElement != null)
        	{
        		return dicomElement.getBytes();
        	}
        }
        return null;
	}
	
	
	/*
	 * switch the grayscale of the pixels 
	 */
	public static int[] invertPixels(int pixelData[])
	{
		if(pixelData == null)return pixelData;
		for(int i = 0; i < pixelData.length; i++)
		{
			int pixelGrayLevel = pixelData[i] & 0xff;
			pixelGrayLevel = 255 - pixelGrayLevel;
			pixelData[i] = (0xFF << 24) | // alpha
			(pixelGrayLevel << 16) | // red
			(pixelGrayLevel << 8) | // green
			pixelGrayLevel; // blue
		}
		return pixelData;
	}
	
	
	public static int[] setBrightnessAndContrast(int pixelData[], int brightness, int contrast)
	{
		LUTable lut = new LUTable();
		
		double contrastVal = Math.pow(contrast/127., 2);
		lut.setContrast(contrastVal);
		lut.setBrightness(256 - brightness);
		
		if(pixelData == null)return pixelData;
		for(int i = 0; i < pixelData.length; i++)
    	{
			int gray = pixelData[i] & 0xffffffff;
						
			int red 	= (gray >> 16) & 0xff;
			int green 	= (gray >> 8) & 0xff;
			int blue 	= gray & 0xff;

			red = lut.getValue(red);
			green = lut.getValue(green);
			blue = lut.getValue(blue);
    		
			pixelData[i] = (0xFF << 24) | 
			(red << 16) | 
			(green << 8) | 
			blue; 
    	}
		return pixelData;
	}
	
	
	/*
	 * prepare for painting: convert a given byte[] to int[]
	 */
	public static int[] convertToIntPixelData(byte bytePixels[], int bitsAllocated, int width, int height, boolean invert)
	{
		int outputPixels[] = null;
		// TODO show Memory Error Dialog
		// actually we do not accept byte data exceeding 4MB
		if(width * height * 4 > 4*1024*1024)return outputPixels;
		if(bytePixels == null || width < 1 || height < 1)return outputPixels;
		outputPixels = new int[width * height];
		int pixelGrayLevel = 0;
		int pixelGrayLevel2 = 0;
		int max = 0;
		int min = 65530;
		for(int i = 0; i < bytePixels.length; i++)
		{
			if(bitsAllocated == 16)
			{
				int intPixelLevel = (bytePixels[i+1] & 0xff) << 8 | (bytePixels[i] & 0xff);
				if(intPixelLevel > max)max = intPixelLevel;
				if(intPixelLevel < min)min = intPixelLevel;
				i++;
			}
			else if(bitsAllocated == 12)
			{
				int intPixelLevel = (bytePixels[i+2] & 0xff) <<  8 | (bytePixels[i+1] & 0xf0);
				int intPixelLevel2 = (bytePixels[i+1] & 0x0f) << 8 | (bytePixels[i] & 0xff);
				if(intPixelLevel > max)max = intPixelLevel;
				if(intPixelLevel < min)min = intPixelLevel;
				if(intPixelLevel2 > max)max = intPixelLevel2;
				if(intPixelLevel2 < min)min = intPixelLevel2;
				i+=2;
			}
		}
		int windowWidth = max - min;
		int windowOffset = min;
		for(int i = 0, j = 0; i < bytePixels.length; i++)
		{
			if(bitsAllocated == 16)
			{
				int intPixelLevel = (bytePixels[i+1] & 0xff) << 8 | (bytePixels[i] & 0xff);
				
				pixelGrayLevel = (256 * (intPixelLevel - windowOffset) / windowWidth);
				pixelGrayLevel = (pixelGrayLevel > 255) ? 255 : ((pixelGrayLevel < 0) ? 0 : pixelGrayLevel);
				if(invert)pixelGrayLevel = 255 - pixelGrayLevel;
				i++;
				outputPixels[j++] = (0xFF << 24) | // alpha
    			(pixelGrayLevel << 16) | // red
    			(pixelGrayLevel << 8) | // green
    			pixelGrayLevel; // blue
			}
			else if(bitsAllocated == 12)
			{
				int intPixelLevel = (bytePixels[i+2] & 0xff) <<  8 | (bytePixels[i+1] & 0xf0);
				int intPixelLevel2 = (bytePixels[i+1] & 0x0f) << 8 | (bytePixels[i] & 0xff);
				pixelGrayLevel = 128 * (intPixelLevel - windowOffset) / windowWidth;
				pixelGrayLevel = (pixelGrayLevel > 255) ? 255 : ((pixelGrayLevel < 0) ? 0 : pixelGrayLevel);
				pixelGrayLevel2 = 128 * (intPixelLevel2 - windowOffset) / windowWidth;
				pixelGrayLevel2 = (pixelGrayLevel2 > 255) ? 255 : ((pixelGrayLevel2 < 0) ? 0 : pixelGrayLevel2);
				
				i+=2;
				
				outputPixels[j++] = (0xFF << 24) | // alpha
    			(pixelGrayLevel2 << 16) | // red
    			(pixelGrayLevel2 << 8) | // green
    			pixelGrayLevel2; // blue
				
				outputPixels[j++] = (0xFF << 24) | // alpha
    			(pixelGrayLevel << 16) | // red
    			(pixelGrayLevel << 8) | // green
    			pixelGrayLevel; // blue
			}
			else
			{
				pixelGrayLevel = bytePixels[i] & 0xff;// > 0 ? bytePixels[i] : 255 - bytePixels[i];
				
				outputPixels[j++] = (0xFF << 24) | // alpha
    			(pixelGrayLevel << 16) | // red
    			(pixelGrayLevel << 8) | // green
    			pixelGrayLevel; // blue
			}
		}
		return outputPixels;
	}
}
