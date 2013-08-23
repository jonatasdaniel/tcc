/*******************************************************************************
 *                                                                            
 *   Copyright (c) 2004, 2007 by CRP Henri TUDOR - SANTEC LUXEMBOURG 
 *   check http://santec.tudor.lu for more information
 * 
 *   Contributor(s):
 *    Andreas Jahnen   andreas.jahnen(at)tudor.lu                            
 *    Johannes Hermen  johannes.hermen(at)tudor.lu                            
 *    Christian Moll   christian.moll(at)tudor.lu                            
 *                                                                            
 *   This library is free software; you can redistribute it and/or modify it  
 *   under the terms of the GNU Lesser General Public License (version 2.1)
 *   as published by the Free Software Foundation.
 *                                                                            
 *   This software is distributed in the hope that it will be useful, but     
 *   WITHOUT ANY WARRANTY; without even the implied warranty of               
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 *   Lesser General Public License for more details.                          
 *                                                                            
 *   You should have received a copy of the GNU Lesser General Public         
 *   License along with this library; if not, write to the Free Software      
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
 *                                                                            
 *     
 *******************************************************************************/
package lu.tudor.santec.dicom.gui.header.selector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class is able to write csv formated lines 
 * 
 * @author Christian Moll, christian.moll(at)tudor.lu
 *
 */

public class ExportToCSV {

	private PrintWriter pWriter;
	
	private FileOutputStream fos;

	/**
	 * Constructor
	 * 
	 * @param CSVfos FileOutputStream
	 * @throws FileNotFoundException 
	 *           
	 *            
	 */
	public ExportToCSV(File cvsOut) throws FileNotFoundException {
		super();
		fos = new FileOutputStream(cvsOut);
		pWriter = new PrintWriter(fos);
	}

	/**
	 * converts rows to csv and print them to the FileOutputStream
	 * @param line Object[]
	 */
	public void println(Object[] line) {
		String sBuffer = new String();
		for (int i = 0; i < line.length; i++) {
			sBuffer = sBuffer + "\"" + line[i] + "\"" + ";";
		}
		pWriter.println(sBuffer);
		pWriter.flush();
	}
	
	public void close() throws IOException {
	    fos.close();
	}
}
