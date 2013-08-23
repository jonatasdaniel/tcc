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

import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomTagTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private String[] columns = {Translatrix.getTranslationString("dicom.TagName"), Translatrix.getTranslationString("dicom.TagValue")};
	private Vector tags = new Vector();
	
	public int getRowCount() {
		return tags.size();
	}

	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			if (rowIndex <= -1)
				return ;
			HeaderTag dr = ((HeaderTag) tags.get(rowIndex));
			switch (columnIndex) {
			case 0:
				try {
					 dr.setTagNr((String)aValue);
				} catch (Exception e) {
					return;
				}
				break;
			case 1:
				dr.setTagValue((String)aValue);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			if (rowIndex <= -1)
				return null;
			HeaderTag dr = ((HeaderTag) tags.get(rowIndex));
			switch (columnIndex) {
			case 0:
				try {
					return dr.getTagNr();
				} catch (Exception e) {
					return "";
				}
			case 1:
				return dr.getTagValue();
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setTags(Vector tags) {
		this.tags = tags;
		this.fireTableDataChanged();
	}
	
	public void addTag(HeaderTag tag) {
		this.tags.add(tag);
		this.fireTableDataChanged();
	}

	public HeaderTag getTag(int line) {
		try {
			return (HeaderTag) tags.get(line);	
		} catch (Exception e) {
			return null;
		}
	}
	
	public HeaderTag[] getTags() {
		try {
			return (HeaderTag[]) tags.toArray(new HeaderTag[0]);	
		} catch (Exception e) {
			return null;
		}
	}

	public void removeTag(int row) {
		this.tags.remove(row);
		this.fireTableDataChanged();
	}

	public Vector getTagsVector() {
		return tags;
	}
}
