package lu.tudor.santec.dicom.gui.selector;

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

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class FoundFilesTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columns = {
				Translatrix.getTranslationString("dicom.Modality"), 
				Translatrix.getTranslationString("dicom.PatientName"), 
				Translatrix.getTranslationString("dicom.PatientId"), 
				Translatrix.getTranslationString("dicom.ImageDate"),
				Translatrix.getTranslationString("dicom.ImageNr"), 
				Translatrix.getTranslationString("dicom.FileSize")};
	private Vector files = new Vector();
	
	public int getRowCount() {
		return files.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex <= -1)
				return null;
			DicomFile dr = ((DicomFile) files.get(rowIndex));
			switch (columnIndex) {
			case 0:
				try {
					return dr.getDicomHeader().getHeaderStringValue("0008,0060");
				} catch (Exception e) {
					return "-";
				}
			case 1:
				try {
					return dr.getDicomHeader().getHeaderStringValue("0010,0010");
				} catch (Exception e) {
					return "-";
				}
			case 2:
				try {
					return dr.getDicomHeader().getHeaderStringValue("0010,0020");
				} catch (Exception e) {
					return "-";
				}
			case 3:
				try {
					String date = dr.getDicomHeader().getHeaderStringValue("0008,0023");
					if (date == null || date.equals(""))
						date = dr.getDicomHeader().getHeaderStringValue("0008,0021");
					if (date == null || date.equals(""))
						date = "-";
					return date;
				} catch (Exception e) {
					return "-";
				}
			case 4:
				try {
					String series = dr.getDicomHeader().getHeaderStringValue("0020,0011");
					if (series == null || series.equals(""))
						series = "-";
					String img = dr.getDicomHeader().getHeaderStringValue("0020,0013");
					return series + ":" + img;
				} catch (Exception e) {
					return "";
				}
			case 5:
				return dr.getFileSize()/1024 + "MB";
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setFiles(Vector files) {
		this.files = files;
		this.fireTableDataChanged();
	}

	public DicomFile getFile(int line) {
		try {
			return (DicomFile) files.get(line);	
		} catch (Exception e) {
			return null;
		}
		
	}
}
