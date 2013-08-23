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

public class DicomHeaderParseException extends Exception{

	
	private String errorText;
	private String errorTag;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DicomHeaderParseException(String text, String dicomTag){
		this.errorTag = dicomTag;
		this.errorText = text;
	}
	
	public DicomHeaderParseException(String dicomTag){
		this.errorTag = dicomTag;
		this.errorText = dicomTag;
	}
	
	public String getMessage(){
		return errorText;
	}
	
	public String getTag(){
		return errorTag;
	}
}
