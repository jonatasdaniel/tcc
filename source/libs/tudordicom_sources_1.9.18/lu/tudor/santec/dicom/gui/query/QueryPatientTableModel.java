package lu.tudor.santec.dicom.gui.query;

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

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class QueryPatientTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columns = {
			Translatrix.getTranslationString("dicom.PatientName"), 
			Translatrix.getTranslationString("dicom.PatientID")
	};
	
	private Vector patients = new Vector();
	
	public int getRowCount() {
		return patients.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		DicomObject dr = ((DicomObject) patients.get(rowIndex));
		switch (columnIndex) {
		case 0:
			try {
				return dr.getString(Tag.PatientName).replaceAll("\\^",",");				
			} catch (Exception e) {
				return "";
			}
		case 1:
			return dr.getString(Tag.PatientID);
		default:
			return null;
		}
	}
	
	public void setPatients(Vector patients) {
		this.patients = patients;
		this.fireTableDataChanged();
	}

	public DicomObject getDimse(int line) {
		try {
			return (DicomObject) patients.get(line);	
		} catch (Exception e) {
			return null;
		}
		
	}
}
