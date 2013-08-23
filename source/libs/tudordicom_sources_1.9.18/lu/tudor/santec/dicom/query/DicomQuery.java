package lu.tudor.santec.dicom.query;

/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.ProgressMonitor;

import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.query.DcmQR.QueryRetrieveLevel;

import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.NoPresentationContextException;

public class DicomQuery
{
    // Attributes ----------------------------------------------------
    private Logger logger = Logger.getLogger("lu.tudor.santec.dicom.query.DicomQuery");
    
    private DcmURL url;
    
    private DcmQR query;

    private ProgressMonitor progressMonitor;
   
    
    // Constructors --------------------------------------------------
    /**
     *  Constructor for the MoveStudy object
     *
     * @param  cfg  Description of the Parameter
     * @param  url  Description of the Parameter
     */
    public DicomQuery(DcmURL url)
    {
        
	this.url = url.clone();
	
        query = new DcmQR();
        
        query.setCalledAET(url.getCalledAET(), true);
        if (url.getCallingAET() != null)
            query.setCalling(url.getCallingAET());
        query.setRemoteHost(url.getHost());
        query.setRemotePort(url.getPort());
        
    }
     
	public Vector<DicomObject> query(Vector<HeaderTag> filters, QueryRetrieveLevel level) throws Exception {

		query.setQueryLevel(level);
		for (int retKey : level.getReturnKeys()) {
			query.addReturnKey(retKey);
		}

		if (filters != null) {
			for (HeaderTag headerTag : filters) {
				query.addMatchingKey(headerTag.getTagInt(), headerTag.getTagValue());
			}
		}

		query.configureTransferCapability(true);

		query.start();
		query.open();

		Collection<DicomObject> result = null;
		result = query.query();

		query.close();
		query.stop();

		return new Vector<DicomObject>(result);
	}

    public Vector<DicomObject> queryPatients(int searchTag, String searchString) throws Exception
    {
	
    	query.setQueryLevel(DcmQR.QueryRetrieveLevel.PATIENT);
        
        query.addReturnKey(Tag.PatientID);
        query.addReturnKey(Tag.NumberOfPatientRelatedStudies);
        query.addReturnKey(Tag.NumberOfPatientRelatedSeries);
        query.addReturnKey(Tag.NumberOfPatientRelatedInstances);

        query.addMatchingKey(searchTag, searchString);

        query.configureTransferCapability(true);

        query.start();
        query.open();
            
        Collection<DicomObject> result = null;
        try {
            result = query.query();
	} catch (NoPresentationContextException e) {
	    // if query for patient not supported, try query for studies.....
	    logger.info("query for patient not supported, try query for studies.....");
	    query.close();
	    query.stop();
	    

	    query.setQueryLevel(DcmQR.QueryRetrieveLevel.STUDY);
	    
	    query.addMatchingKey(searchTag, searchString);

	    query.addReturnKey(Tag.PatientID);
	    query.addReturnKey(Tag.NumberOfPatientRelatedStudies);
	    query.addReturnKey(Tag.NumberOfPatientRelatedSeries);
	    query.addReturnKey(Tag.NumberOfPatientRelatedInstances);

	    query.configureTransferCapability(true);

	    query.start();
	    query.open();
	    
	    Collection<DicomObject> resultStudies = query.query();
	    HashMap<String, DicomObject> patients = new HashMap<String, DicomObject>();
	    for (DicomObject study : resultStudies) {
		patients.put(study.getString(Tag.PatientID), study);
	    }
	    result = patients.values();
	}

        query.close();
        query.stop();
	
	return new Vector<DicomObject>(result);
    }
       
    public Vector<DicomObject> queryStudiesByPatient(DicomObject patient) throws Exception
    {
	
	query.setQueryLevel(DcmQR.QueryRetrieveLevel.STUDY);
        
        query.addMatchingKey(Tag.PatientID, patient.getString(Tag.PatientID));

        query.addReturnKey(Tag.StudyInstanceUID);
        query.addReturnKey(Tag.StudyDescription);
        query.addReturnKey(Tag.StudyComments);
        query.addReturnKey(Tag.StudyDate);
        query.addReturnKey(Tag.StudyTime);

        query.configureTransferCapability(true);

        query.start();
        query.open();
            
        List<DicomObject> result = query.query();

        query.close();
        query.stop();
	
	return new Vector<DicomObject>(result);
	
    }
    
    public Vector<DicomObject> querySeriesByStudy(DicomObject study, String wantedModality) throws Exception
    {
	
	query.setQueryLevel(DcmQR.QueryRetrieveLevel.SERIES);
        
        query.addMatchingKey(Tag.StudyInstanceUID, study.getString(Tag.StudyInstanceUID));

        if (wantedModality != null && ! wantedModality.equals(""))
            query.addMatchingKey(Tag.Modality, wantedModality);
        else
    		query.addReturnKey(Tag.Modality);
        
        query.addReturnKey(Tag.SeriesInstanceUID);
        query.addReturnKey(Tag.SeriesDescription);
        query.addReturnKey(Tag.SeriesNumber);
        query.addReturnKey(Tag.SeriesDate);
        query.addReturnKey(Tag.SeriesTime);

        query.configureTransferCapability(true);

        query.start();
        query.open();
            
        List<DicomObject> result = query.query();

        query.close();
        query.stop();
	
	return new Vector<DicomObject>(result);
    }
    
    public Vector<DicomObject> queryPicturesBySeries(DicomObject series) throws Exception
    {
	
	query.setQueryLevel(DcmQR.QueryRetrieveLevel.IMAGE);
        
        query.addMatchingKey(Tag.SeriesInstanceUID, series.getString(Tag.SeriesInstanceUID));

//        query.addReturnKey(Tag.InstanceNumber);
//        query.addReturnKey(Tag.InstanceCreationDate);
//        query.addReturnKey(Tag.InstanceCreationTime);

        query.configureTransferCapability(true);

        query.start();
        query.open();
            
        List<DicomObject> result = query.query();

        query.close();
        query.stop();
	
	return new Vector<DicomObject>(result);
    }


    /**
     *  Description of the Method
     *
     * @param  findRspList    Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void moveSeries(JDialog parent, String destUrl, DicomObject series)
	    throws Exception {
	try {
	    progressMonitor = new ProgressMonitor(parent,
		    "Moving Series to: \r\n" + destUrl, "moving.....", 0, 3);
	    progressMonitor.setMillisToDecideToPopup(0);
	    progressMonitor.setMillisToPopup(0);

	    progressMonitor.setProgress(2);

	    move(DcmQR.QueryRetrieveLevel.SERIES, destUrl, series);

	    progressMonitor.close();

	} catch (Exception e) {
	    progressMonitor.close();
	    throw e;
	}
    }
    
    /**
     *  Description of the Method
     *
     * @param  findRspList    Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void moveStudies(JDialog parent, String destUrl, DicomObject study)
	    throws Exception {
	try {
	    progressMonitor = new ProgressMonitor(parent,
		    "Moving Series to: \r\n" + destUrl, "moving.....", 0, 3);
	    progressMonitor.setMillisToDecideToPopup(0);
	    progressMonitor.setMillisToPopup(0);

	    progressMonitor.setProgress(2);

	    move(DcmQR.QueryRetrieveLevel.STUDY, destUrl, study);

	    progressMonitor.close();

	} catch (Exception e) {
	    progressMonitor.close();
	    e.printStackTrace();
	    throw e;
	}
    }
    
    private void move(QueryRetrieveLevel queryRetrieveLevel, String destUrl, DicomObject object) throws Exception {

    	DcmQR dcmqr = new DcmQR();
        dcmqr.setRemoteHost(this.url.getHost());
        dcmqr.setRemotePort(this.url.getPort());
        dcmqr.setCalledAET(this.url.getCalledAET(), false);
        if (url.getCallingAET() != null)
            dcmqr.setCalling(url.getCallingAET());
        dcmqr.setMoveDest(destUrl);

        dcmqr.setQueryLevel(queryRetrieveLevel);
        if (queryRetrieveLevel.equals(QueryRetrieveLevel.PATIENT)) {
            dcmqr.addMatchingKey(Tag.PatientID, object.getString(Tag.PatientID));            
        } else if (queryRetrieveLevel.equals(QueryRetrieveLevel.STUDY)) {
            dcmqr.addMatchingKey(Tag.StudyInstanceUID, object.getString(Tag.StudyInstanceUID));         
        } else if (queryRetrieveLevel.equals(QueryRetrieveLevel.SERIES)) {
            dcmqr.addMatchingKey(Tag.SeriesInstanceUID, object.getString(Tag.SeriesInstanceUID));         
        } else if (queryRetrieveLevel.equals(QueryRetrieveLevel.IMAGE)) {
            dcmqr.addMatchingKey(Tag.SOPClassUID, object.getString(Tag.SOPClassUID));         
        }
        
        dcmqr.configureTransferCapability(true);
        
        dcmqr.start();
        dcmqr.open();

        List<DicomObject> result = dcmqr.query();
       dcmqr.move(result);
       
       dcmqr.close();
       dcmqr.stop();
	
    }
    
    
    
    
    
    
    /**
     *  Description of the Method
     *
     * @param  findRspList    Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public void moveImage(JDialog parent, String destUrl, DicomObject image)
	    throws Exception {

	try {
	    progressMonitor = new ProgressMonitor(parent,
		    "Moving Image to: \r\n" + destUrl, "moving.....", 0, 3);
	    progressMonitor.setMillisToDecideToPopup(0);
	    progressMonitor.setMillisToPopup(0);

	    progressMonitor.setProgress(2);

	    move(DcmQR.QueryRetrieveLevel.IMAGE, destUrl, image);

	    progressMonitor.close();

	} catch (Exception e) {
	    progressMonitor.close();
	    throw e;
	}

    }
    
    /**
     * moves all PATIENTS matching the given patientId from the given pacs (queryUrl) to the given DICOM Node (destAET)
     * 
     * @param queryUrl Dicom URL of the queried PACS system
     * @param destAET AETitle of configured destination DICOM Node in the Pacs
     * @param HeaderTag[] filters 
     * @return number of moved Patients
     * @throws Exception
     */
    public static int moveSeries(DcmURL queryUrl, String destAET, HeaderTag[] filters) throws Exception {

	DcmQR dcmqr = new DcmQR();

        dcmqr.setRemoteHost(queryUrl.getHost());
        dcmqr.setRemotePort(queryUrl.getPort());
        dcmqr.setCalledAET(queryUrl.getCalledAET(), false);
        if (queryUrl.getCallingAET() != null)
            dcmqr.setCalling(queryUrl.getCallingAET());
        else {
            dcmqr.setCalling(destAET);
        }
        
        dcmqr.setMoveDest(destAET);

        dcmqr.setQueryLevel(QueryRetrieveLevel.SERIES);

        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
        	HeaderTag tag = filters[i];
        	int tagInt = Tag.toTag(tag.getTagNr().replaceAll(",", ""));
        	dcmqr.addMatchingKey(tagInt, tag.getTagValue());            	
            }            
        }
        
        dcmqr.configureTransferCapability(true);
        
        dcmqr.start();
        dcmqr.open();

        List<DicomObject> result = dcmqr.query();
        
       dcmqr.move(result);
       
       dcmqr.close();
       dcmqr.stop();
       
       return result.size();
	
    }
    
//    /**
//     * moves all STUDIES matching the given accessionNumber from the given pacs (queryUrl) to the given DICOM Node (destAET)
//     * 
//     * @param queryUrl Dicom URL of the queried PACS system
//     * @param destAET AETitle of configured destination DICOM Node in the Pacs
//     * @param accessionNumer number to match
//     * @return number of moved Studies
//     * @throws Exception
//     */
//    public static int moveByAccessionNumber(DcmURL queryUrl, String destAET, String accessionNumer, HeaderTag[] filters) throws Exception {
//
//	DcmQR dcmqr = new DcmQR();
//
//        dcmqr.setRemoteHost(queryUrl.getHost());
//        dcmqr.setRemotePort(queryUrl.getPort());
//        dcmqr.setCalledAET(queryUrl.getCalledAET(), false);
//        if (queryUrl.getCallingAET() != null)
//            dcmqr.setCalling(queryUrl.getCallingAET());
//        else {
//            dcmqr.setCalling(destAET);
//        }
//        
//        dcmqr.setMoveDest(destAET);
//
//        dcmqr.setQueryLevel(QueryRetrieveLevel.STUDY);
//        dcmqr.addMatchingKey(Tag.AccessionNumber, accessionNumer);         
//        
//        if (filters != null) {
//            for (int i = 0; i < filters.length; i++) {
//        	HeaderTag tag = filters[i];
//        	int tagInt = Tag.toTag(tag.getTagNr().replaceAll(",", ""));
//        	dcmqr.addMatchingKey(tagInt, tag.getTagValue());       	
//            }            
//        }
//        
//        dcmqr.configureTransferCapability(false);
//        
//        dcmqr.start();
//        dcmqr.open();
//
//        List<DicomObject> result = dcmqr.query();
//        
//        
//       dcmqr.move(result);
//       
//       dcmqr.close();
//       dcmqr.stop();
//       
//       return result.size();
//	
//    }
    
//    /**
//     * moves all PATIENTS matching the given patientId from the given pacs (queryUrl) to the given DICOM Node (destAET)
//     * 
//     * @param queryUrl Dicom URL of the queried PACS system
//     * @param destAET AETitle of configured destination DICOM Node in the Pacs
//     * @param patientId string to match
//     * @return number of moved Patients
//     * @throws Exception
//     */
//    public static int moveByPatientId(DcmURL queryUrl, String destAET, String patientId, HeaderTag[] filters) throws Exception {
//
//	DcmQR dcmqr = new DcmQR();
//
//        dcmqr.setRemoteHost(queryUrl.getHost());
//        dcmqr.setRemotePort(queryUrl.getPort());
//        dcmqr.setCalledAET(queryUrl.getCalledAET(), false);
//        if (queryUrl.getCallingAET() != null)
//            dcmqr.setCalling(queryUrl.getCallingAET());
//        else {
//            dcmqr.setCalling(destAET);
//        }
//        
//        dcmqr.setMoveDest(destAET);
//
//        dcmqr.setQueryLevel(QueryRetrieveLevel.SERIES);
//        dcmqr.addMatchingKey(Tag.PatientID, patientId);    
//
//        if (filters != null) {
//            for (int i = 0; i < filters.length; i++) {
//        	HeaderTag tag = filters[i];
//        	int tagInt = Tag.toTag(tag.getTagNr().replaceAll(",", ""));
//        	dcmqr.addMatchingKey(tagInt, tag.getTagValue());            	
//            }            
//        }
//        
//        dcmqr.configureTransferCapability(false);
//        
//        dcmqr.start();
//        dcmqr.open();
//
//        List<DicomObject> result = dcmqr.query();
//        
//       dcmqr.move(result);
//       
//       dcmqr.close();
//       dcmqr.stop();
//       
//       return result.size();
//	
//    }
    
    

    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
        try {
        	String url = "dicom://" + "OSIRIX";
        	url = url  + "@" +"10.14.1.8" + ":"+ "5104"; 
    		DcmURL dcmUrl = new DcmURL(url);
    		
            DicomQuery inst = new DicomQuery(dcmUrl);
            Vector<DicomObject>dos = inst.queryPatients(Tag.PatientName, "Anony*");
            for (DicomObject dicomObject : dos) {
		System.out.println(dicomObject);
	    }
            
        } catch (IllegalArgumentException e) {
        }
    }

    public void close() {
	// TODO Auto-generated method stub
	
    }

   

}

