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
public class FileExistsException extends Exception {
	private static final long serialVersionUID = 1L;
	private File file;
	
	public FileExistsException(File f) {
		this.file = f;
	}

	public String toString() {
		return "File "+file.getAbsolutePath()+" allready exists";
	}
	
	public File getFile() {
		return file;
	}
	
}
