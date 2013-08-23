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
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * @author Christian Moll, christian.moll(at)tudor.lu
 *
 */
public class ExportToXSL {

	private FileOutputStream fos;
	
	private HSSFSheet sheet;
	
	private HSSFWorkbook workbook;
	
	private HSSFCellStyle cellStyle;

	private int rowIndex=0;
	
	/**
	 * @throws FileNotFoundException 
	 * 
	 */
	public ExportToXSL(File xlsOut) throws FileNotFoundException {
		super();
		fos = new FileOutputStream(xlsOut);
		workbook = new HSSFWorkbook();
		cellStyle = workbook.createCellStyle();
		sheet = workbook.createSheet("Results");
	}
	
	@SuppressWarnings("deprecation")
	public void println(Object[] line) {
//		 Create a row and put some cells in it. Rows are 0 based.
	    HSSFRow row = sheet.createRow((short)rowIndex);
	    
	    boolean printed = true;
	    for (int i = 0; i < line.length; i++) {
	    	try {
	    		Date interValue;
	    		interValue = (Date)line[i];
	    		System.out.println(interValue + " " + line[i]);
	    		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
		    	row.createCell((short)i).setCellValue(interValue);
		    	row.getCell((short)i).setCellStyle(cellStyle);
		    	printed = true;
	    	} catch (Exception e) {
//	    		e.printStackTrace();
	    		printed = false;
	    	}
	    	try {
	    		if (!printed) {
					double interValue;
					interValue = Double.parseDouble(line[i].toString());
					row.createCell((short) i).setCellValue(interValue);
					printed = true;
				}
	    	} catch (Exception e) {
//	    		e.printStackTrace();
	    		printed = false;
	    	}
	    	if (!printed){
	    		try {
	    			String interValue = line[i].toString();
	    			row.createCell((short)i).setCellValue(interValue);
	    			printed = true;
				} catch (Exception e) {
					row.createCell((short)i).setCellValue("");
				}
	    	}
	    }
	    
		rowIndex++;
	}
	
	public boolean writeStreamToBook() {
		try {
			workbook.write(fos);
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public void close() throws IOException {
	    fos.close();
	}
}
