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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import lu.tudor.santec.i18n.Translatrix;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class QueryStudyTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columns = {
			Translatrix.getTranslationString("dicom.StudyDesc"),
			Translatrix.getTranslationString("dicom.Date"),
			Translatrix.getTranslationString("dicom.Time")};
	private Vector studies = new Vector();
	
	public int getRowCount() {
		return studies.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
	    DicomObject ds;
		try {
			ds = ((DicomObject) studies.get(rowIndex));
		} catch (Exception e) {
			return null;
		}
		switch (columnIndex) {
		case 0:
			if (ds.getString(Tag.StudyDescription) != null) {
				return ds.getString(Tag.StudyDescription);
			} else {
				return ds.getString(Tag.StudyComments);
			}
		case 1:
//			String date = ds.getString(Tags.StudyDate);
//			return date.substring(6,8) + "."+ date.substring(4,6) + "." + date.substring(0,4);
			return ds.getDate(Tag.StudyDate);
		case 2:
//			String ttime = ds.getString(Tags.StudyTime);
//			try {
//				return new Time(Integer.parseInt(ttime.substring(0,2)), Integer.parseInt(ttime.substring(2,4)), Integer.parseInt(ttime.substring(4,6)));	
//			} catch (Exception e) {
//				return ttime;
//			}
			return ds.getDate(Tag.StudyTime);
		default:
			return null;
		}
	}
	
	public void setStudies(Vector<DicomObject> studies) {
		this.studies = studies;
		Collections.sort(this.studies, new Comparer());
		this.fireTableDataChanged();
	}

	public DicomObject getDimse(int line) {
		try {
			return (DicomObject) studies.get(line);	
		} catch (Exception e) {
			return null;
		}
	}
	
	class Comparer implements Comparator {
        public int compare(Object obj1, Object obj2)
        {
			try {
			    DicomObject ds1 = ((DicomObject) obj1);
			    DicomObject ds2 = ((DicomObject) obj2);
	        	if (ds1.getDate(Tag.StudyDate).before(ds2.getDate(Tag.StudyDate)))
	        		return 1;
	        	else if (ds1.getDate(Tag.StudyDate).after(ds2.getDate(Tag.StudyDate))) 
	        		return -1; 
	        	else {
	        		if (ds1.getDate(Tag.StudyTime).before(ds2.getDate(Tag.StudyTime)))
	            		return 1;
	        		else
	        			return -1;
	        	}
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
        }
	}
}
