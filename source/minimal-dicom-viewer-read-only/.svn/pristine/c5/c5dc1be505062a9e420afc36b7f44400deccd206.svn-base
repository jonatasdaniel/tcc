/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <DicomFileFilter.java> is part of Minimal Dicom Viewer.
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

import java.io.File;
import java.io.FileFilter;

public class DicomFileFilter implements FileFilter{
	
	public boolean accept(File pathname) 
	{
		if (pathname.isFile() && !pathname.isHidden()) 
		{
			// Get the file name
			String fileName = pathname.getName();
			fileName = fileName.toLowerCase();
			// If the file is a dicomdir return false
			if (fileName.equals("dicomdir"))
				return false;
			// file must end with ".dcm" or no extension
			if(fileName.lastIndexOf(".") == -1)return true;
			if(fileName.endsWith("dcm"))return true;
		}
		return false;
	}

}
