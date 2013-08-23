package lu.tudor.santec.dicom.gui.dicomdir;

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

import javax.swing.filechooser.FileFilter;

/**
 * A File Filter that only matches DICOMDIR files and Directories 
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DICOMDIRFileFilter extends FileFilter {

	    //Accept all directories and all dicomdir / DICOMDIR files.
	    public boolean accept(File f) {
	        if (f.isDirectory()) {
	            return true;
	        }
	        if (f.getName().equalsIgnoreCase("dicomdir")) {
	                    return true;
	        } 
	        return false;
	    }

	    //The description of this filter
	    public String getDescription() {
	        return "DICOMDIR";
	    }
}
