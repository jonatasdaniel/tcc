package lu.tudor.santec.dicom.receiver;

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

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomEvent {

	private static final long serialVersionUID = -3557202037346958952L;

	public static final String ADD = "ADD";
	
	private File dicomDir;

	private String type;

	private File file;

	// ***************************************************************************
	// * Constructor *
	// ***************************************************************************

	public DicomEvent(File dicomDir) {
		this.dicomDir = dicomDir;
	}

	public DicomEvent(File dicomDir, File file, String type) {
		this.dicomDir = dicomDir;
		this.file = file;
		this.type = type;
	}

	public File getDicomDir() {
		return dicomDir;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param dicomDir the dicomDir to set
	 */
	public void setDicomDir(File dicomDir) {
		this.dicomDir = dicomDir;
	}

	public String toString() {
		return getDicomDir() + ": " + getType() + " " + getFile();
	}
	
}
