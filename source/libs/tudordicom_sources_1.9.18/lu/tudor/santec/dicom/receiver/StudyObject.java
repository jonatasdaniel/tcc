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

import java.sql.Time;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class StudyObject {

	private DicomObject study;
	private String patientName;
	private String patientID;
	private String studyDesc;
	private String date;
	private Time time;
	
	public String  getDate() {
		return date;
	}


	public Time getTime() {
		return time;
	}


	public StudyObject(DicomObject study, String patientName, String patientID) {
		this.study = study;
		this.patientName = patientName;
		this.patientID = patientID;
		this.studyDesc = study.getString(Tag.StudyDescription);
		String date = study.getString(Tag.StudyDate);
		this.date = date.substring(6,8) + "."+ date.substring(4,6) + "." + date.substring(0,4);
		String ttime = study.getString(Tag.StudyTime);
		time = new Time(Integer.parseInt(ttime.substring(0,2)), Integer.parseInt(ttime.substring(2,4)), Integer.parseInt(ttime.substring(4,6)));
	}
	

	public DicomObject getDirRecord() {
		return study;
	}


	public void setDirRecord(DicomObject dirRecord) {
		this.study = dirRecord;
	}


	public String getPatientID() {
		return patientID;
	}
	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	public String getStudyDesc() {
		return studyDesc;
	}
	public void setStudyDesc(String studyDesc) {
		this.studyDesc = studyDesc;
	}
	
	
}
