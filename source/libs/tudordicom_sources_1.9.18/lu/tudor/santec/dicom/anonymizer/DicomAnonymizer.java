package lu.tudor.santec.dicom.anonymizer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lu.tudor.santec.dicom.gui.header.DicomHeader;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;

/**
 * Helper class that provides methods to anonymize dicom files
 * 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: DicomAnonymizer.java,v $
 * <br>Revision 1.21  2012-04-12 13:05:46  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.20  2012-03-29 13:40:31  hermen
 * <br>bugfixes
 * <br>
 * <br>Revision 1.19  2012-02-23 10:16:07  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.18  2012-02-23 09:44:40  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.17  2012-02-15 10:08:45  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.16  2012-01-23 14:23:49  hermen
 * <br>improved anonymizer
 * <br>
 * <br>Revision 1.15  2012-01-17 10:32:00  hermen
 * <br>fixed NPE
 * <br>
 * <br>Revision 1.14  2011-12-15 11:58:56  hermen
 * <br>added new age calculation to decimal year
 * <br>
 * <br>Revision 1.13  2011-12-02 09:09:03  hermen
 * <br>fixed age calculation
 * <br>
 * <br>Revision 1.12  2011-09-26 09:02:27  hermen
 * <br>-small bugfixes
 * <br>-new dicom-header contructor with (imageplus) and (imageplus, slicenumber)
 * <br>
 * <br>Revision 1.11  2011-08-16 13:26:30  hermen
 * <br>ver 1.9
 * <br>
 * <br>
 * <br>improved anonymizer/stripper
 * <br>
 * <br>Revision 1.10  2010-11-09 09:43:41  hermen
 * <br>added reindex of dicomdir
 * <br>
 * <br>Revision 1.9  2010-06-18 14:36:39  hermen
 * <br>updated dcm4che
 * <br>
 * <br>Revision 1.8  2010-01-04 14:19:42  hermen
 * <br>added series desc. to dicomdir
 * <br>
 * <br>Revision 1.7  2009-04-17 08:54:26  hermen
 * <br>changed anonymizer
 * <br>
 * <br>Revision 1.6  2009-04-16 08:33:48  hermen
 * <br>set age
 * <br>
 * <br>Revision 1.5  2009-03-25 10:18:51  hermen
 * <br>added anon tags
 * <br>
 * <br>Revision 1.4  2008-11-13 13:20:30  hermen
 * <br>added bugfixes from Marc Guenther
 * <br>
 * <br>Revision 1.3  2008-10-30 10:28:38  hermen
 * <br>redesign and adaption to dcm4che 2.0.15
 * <br>
 *
 */
public class DicomAnonymizer {
    
        public static final int[] INCIDENT_TAGS = {
    		Tag.AcquisitionComments,
    		Tag.AcquisitionContextSequence,
    		Tag.AcquisitionDate,
    		Tag.AcquisitionDateTime,
    		Tag.AcquisitionDeviceProcessingDescription,
    		Tag.AcquisitionProtocolDescription,
    		Tag.AcquisitionTime,
    		Tag.AccessionNumber,
    		Tag.AdmissionID,
    		Tag.AdmittingDate,
    		Tag.AdmittingDiagnosesCodeSequence,
    		Tag.AdmittingDiagnosesDescription,
    		Tag.AdmittingTime,
    		Tag.DischargeDiagnosisDescription,
    		Tag.MedicalAlerts,
    		Tag.MedicalRecordLocator,
    		Tag.NamesOfIntendedRecipientsOfResults,
    		Tag.PreMedication,
    		Tag.ResultsComments,
    		Tag.VisitComments,
        };

    	public static final int[] PATIENT_TAGS = {
	    Tag.PatientName,
	    Tag.PatientID,
	    Tag.PatientAddress,
	    Tag.PatientBirthDate,
	    Tag.PatientBirthName,
	    Tag.PatientBirthTime,
	    Tag.PatientInstitutionResidence,
	    Tag.PatientInsurancePlanCodeSequence,
	    Tag.PatientMotherBirthName,
	    Tag.PatientReligiousPreference,
	    Tag.PatientTelephoneNumbers,
	    Tag.PatientComments,
	    Tag.PatientState,
	    Tag.PatientTransportArrangements,
	    Tag.PatientPrimaryLanguageCodeSequence,
	    Tag.PatientPrimaryLanguageModifierCodeSequence,
	    Tag.PersonName,
	    Tag.PersonAddress,
	    Tag.PersonIdentificationCodeSequence,
	    Tag.PersonTelephoneNumbers,
	    Tag.OtherPatientIDsSequence,
	    Tag.OtherPatientIDs,
	    Tag.OtherPatientNames,
	    Tag.AdditionalPatientHistory,
	    Tag.AuthorObserverSequence,
	    Tag.ConfidentialityConstraintOnPatientDataDescription,
	    Tag.DistributionAddress,
	    Tag.DistributionName,
	    Tag.IdentifyingComments,
	    Tag.ImageComments,
	    Tag.Impressions,
	    Tag.InsurancePlanIdentification,
	    Tag.InterpretationDiagnosisDescription,
	    Tag.InterpretationIDIssuer,
	    Tag.InterpretationRecorder,
	    Tag.InterpretationText,
	    Tag.InterpretationTranscriber,
	    Tag.IssuerOfAdmissionID,
	    Tag.Occupation,
	    Tag.PatientInstitutionResidence,
	    Tag.TextComments,
	    Tag.TextString,
	    Tag.VisitComments
    	};
    	
    	public static final int[] PATIENT_ADDITIONAL_TAGS = {
    	    Tag.PatientSex,
    	    Tag.PatientSize,
    	    Tag.PatientAge,
    	    Tag.MilitaryRank,
    	    Tag.EthnicGroup,
    	    Tag.PatientSexNeutered,
    	    Tag.LastMenstrualDate,
    	    Tag.PregnancyStatus,
    	    Tag.SmokingStatus,
    	    Tag.Allergies,
    	};
    
    	public static final int[] ORGANISATION_TAGS = { 
    	    Tag.InstitutionAddress,
    	    Tag.InstitutionName,
	    Tag.InstitutionalDepartmentName, 
	    Tag.InstitutionCodeSequence,
	    Tag.PerformedStationName,
	    Tag.ResponsibleOrganization,
	    Tag.ScheduledStationAETitle,
	    Tag.ScheduledStationName,
	    Tag.ScheduledStationGeographicLocationCodeSequence,
	    Tag.ScheduledStationNameCodeSequence,
	    Tag.ScheduledStudyLocation,
	    Tag.ScheduledStudyLocationAETitle,
	    Tag.StationName,
	    Tag.VerifyingOrganization
	};
    	
    	public static final int[] PHYSICIAN_TAGS = { 
	    Tag.PhysiciansOfRecord,
	    Tag.PhysiciansOfRecordIdentificationSequence,
	    Tag.PerformingPhysicianName,
	    Tag.PerformingPhysicianIdentificationSequence,
	    Tag.OperatorsName,
	    Tag.ReferringPhysicianName, 
	    Tag.ReferringPhysicianAddress,
	    Tag.NameOfPhysiciansReadingStudy, 
	    Tag.RequestingPhysician,
	    Tag.RequestingPhysicianIdentificationSequence,
	    Tag.ActualHumanPerformersSequence,
	    Tag.HumanPerformerName,
	    Tag.HumanPerformerOrganization,
	    Tag.InterpretationApproverSequence,
	    Tag.InterpretationAuthor,
	    Tag.IntendedRecipientsOfResultsIdentificationSequence,
	    Tag.OrderCallbackPhoneNumber,
	    Tag.OrderEnteredBy,
	    Tag.OrderEntererLocation,
	    Tag.PhysicianApprovingInterpretation,
	    Tag.PhysiciansReadingStudyIdentificationSequence,
	    Tag.PhysiciansOfRecord,
	    Tag.PhysiciansOfRecordIdentificationSequence,
	    Tag.ReferringPhysicianAddress,
	    Tag.ReferringPhysicianName,
	    Tag.ReferringPhysicianIdentificationSequence,
	    Tag.ReferringPhysicianTelephoneNumbers,
	    Tag.RequestingService,
	    Tag.ResponsiblePerson,
	    Tag.ScheduledHumanPerformersSequence,
	    Tag.ScheduledPerformingPhysicianIdentificationSequence,
	    Tag.ScheduledPerformingPhysicianName,
	};
    	
    	
    	public static final int[] MANUFACTURER_TAGS = {
	    Tag.Manufacturer,
	    Tag.ManufacturerModelName,
	    Tag.SoftwareVersions,   
    	};
    
    	public static final int[] OVERLAY_TAGS = {
	    Tag.OverlayComments,
	    Tag.OverlayData,
	    Tag.OverlayTime,
	    Tag.OverlayDate,
    	};

		public static final int[] IMAGE_TAGS = {
			Tag.PixelData
		};
		
		
		public static String SUFFIX_ANON = "_anon.dcm";
		public static String SUFFIX_HEADER = "_header.dcm";
    
    	private static DecimalFormat df =   new DecimalFormat  ( "000" );

    	
    	
	public DicomAnonymizer() {
	    
	}
	
	/**
	 * Anonymizes a Dicom File by removing all 
	 * - Patient
	 * - Institution
	 * - Manufacturer 
	 * Information
	 * @param f
	 */
	public static void anonymizeFile(File f) throws Exception{
	    
	    DicomObject dObj = readDicomObject(f);
	    
	    removePatientInfo(dObj, "", "");
	    removePatientAdditionalInfo(dObj);
	    removeInstitutionInfo(dObj, "");
	    removePhysicianInfo(dObj);
	    removeManufacturerInfo(dObj);
	    
	    writeDicomFile(dObj, f);
	}
	
	/**
	 * replaces the given dicom tag (if existing) with the given value.
	 * @param dObj
	 * @param tag
	 * @param newValue
	 */
	public static void replaceTag(DicomObject dObj, int tag, String newValue) {
	    if (tag != 0 && dObj.contains(tag)) {
		VR vr = dObj.vrOf(tag);
		try {
		    dObj.putString(tag, vr, newValue);		    
		} catch (Exception e) {
		    System.err.println("Error replacing Tag: " + tag + " with new value: " + newValue);
		}
	    }
	}
	
	/**
	 * adds/replaces the given dicom tag with the given value.
	 * @param dObj
	 * @param tag
	 * @param newValue
	 */
	public static void addTag(DicomObject dObj, int tag, String newValue) {
		VR vr = dObj.vrOf(tag);
		try {
		    dObj.putString(tag, vr, newValue);		    
		} catch (Exception e) {
		    System.err.println("Error adding Tag: " + tag + " with value: " + newValue);
		}
	}
	
	
	public static DicomObject removePatientInfo(DicomObject dObj, String patientID, String newName) {
		return removePatientInfo(dObj, patientID, newName, true);
	}
	
	public static DicomObject removePatientInfo(DicomObject dObj, String patientID, String newName, boolean calculateAge) {
	    
		
		
		// read PatientBirthDate
	    Date bDate = null;
	    Calendar bDayCal = null;
	    try {
	    	bDate = dObj.getDate(Tag.PatientBirthDate);			
		} catch (Exception e) {
			System.err.println("error parsing birthdate " + e.getMessage());
		}
	    if (bDate != null) {
		    bDayCal = new GregorianCalendar();
		    bDayCal.setTime(bDate);
	    }

	    // generate name if not set
	    if (newName == null || "".equals(newName)) {
        	    newName = "ANONYMOUS";
        	    try {
	        		String gender = dObj.getString(Tag.PatientSex);		
	        		newName += "_" + gender;
	        		if (bDayCal != null)
	        			newName += "_" + bDayCal.get(Calendar.YEAR); 
        	    } catch (Exception e) {
        	    }
	    }
	    
//		String patName = dObj.getString(Tag.PatientName);
//		String[] patNames = patName.split("\\^"); 
//		
//		for (int i = 0; i < patNames.length; i++) {
//			System.out.println("\t" + patNames[i]);
//
//			for (Iterator<DicomElement> iterator = dObj.datasetIterator(); iterator.hasNext();) {
//				DicomElement elem = (DicomElement) iterator.next();
//				if (elem.gets)
//			}
//
//			
//
//		}
	    
	    // remove patient tags
	    for (int tag : PATIENT_TAGS) {
	    	dObj.remove(tag);
	    }

	    
	    if (calculateAge) {
	    	// get image date
	    	Date imageDate = null;
	    	if (dObj.contains(Tag.InstanceCreationDate))
	    		imageDate = dObj.getDate(Tag.InstanceCreationDate);
	    	else if (dObj.contains(Tag.AcquisitionDate))
	    		imageDate = dObj.getDate(Tag.AcquisitionDate);
	    	else if (dObj.contains(Tag.SeriesDate))
	    		imageDate = dObj.getDate(Tag.SeriesDate);
	    	
	    	// calculate and set the PatientAge
	    	if (bDate != null && imageDate != null) {
	    		String age = calculateAge(bDate, imageDate);
	    		addTag(dObj, Tag.PatientAge, age);
	    	}
	    }
	    
		
	    // set PatientBirthDate with year only
		if (bDayCal != null)
			addTag(dObj, Tag.PatientBirthDate, bDayCal.get(Calendar.YEAR)+"0101");
		
		
	    addTag(dObj, Tag.PatientName, newName);
	    addTag(dObj, Tag.PatientID, patientID);
	    addTag(dObj, Tag.PatientIdentityRemoved, "true");
	    
	    return dObj;
	}
	
	public static long calculateAgeInDays(Date birthDate, Date imageDate) {
	    GregorianCalendar startCal = new GregorianCalendar();
	    startCal.setTime(birthDate);
	    long start = startCal.getTimeInMillis() +  startCal.getTimeZone().getOffset(  startCal.getTimeInMillis() );
	    
	    GregorianCalendar endCal = new GregorianCalendar();
	    endCal.setTime(imageDate);
	    long end = endCal.getTimeInMillis() +  endCal.getTimeZone().getOffset(  endCal.getTimeInMillis() );
	    
	    long msDiff = Math.abs(end - start);
	    long days = msDiff /(1000*60*60*24);
	   
	    return days;
	}
	
	public static String calculateAgeString(DicomObject dObj) {		
		// read PatientBirthDate
	    Date bDate = null;
	    if (dObj.contains(Tag.PatientBirthDate)) {
	    	bDate = dObj.getDate(Tag.PatientBirthDate);
	    }
    	// get image date
    	Date imageDate = null;
    	if (dObj.contains(Tag.InstanceCreationDate))
    		imageDate = dObj.getDate(Tag.InstanceCreationDate);
    	else if (dObj.contains(Tag.AcquisitionDate))
    		imageDate = dObj.getDate(Tag.AcquisitionDate);
    	else if (dObj.contains(Tag.SeriesDate))
    		imageDate = dObj.getDate(Tag.SeriesDate);
    	else if (dObj.contains(Tag.StudyDate))
    		imageDate = dObj.getDate(Tag.StudyDate);
    	else if (dObj.contains(Tag.ContentDate))
    		imageDate = dObj.getDate(Tag.ContentDate);
    	
    	return calculateAgeString(bDate, imageDate);
    	
	}
    
	/**
	 * Age Calculation due to Lucian Krille for EPI-CT
	 * Zahlenwerte extrahieren 
	 * GebYeNum = YEAR(GebDat);
	 * GebMoNum = MONTH(GebDat);
	 * GebDaNum = DAY(GebDat);
	 * UntYeNum = YEAR(UntDat);
	 * UntMoNum = MONTH(UntDat);
	 * UntDaNum = DAY(UntDat);
	 * 
	 * normieren der Monatslänge 
	 * IF UntDaNum>30 THEN  UntDaNum=30;
	 * IF GebDaNum>30 THEN  GebDaNum=30;
	 * 
	 * Kalkulation 
	 * KalkJahr = UntYeNum-GebYeNum; 
	 * KalkMon = (UntMoNum-GebMoNum) + (UntDaNum-GebDaNum)/30;   (hier sind Tage mit drinnen)
	 * KalkMonRound = round(KalkMon,1);   (Runden des Monats OHNE Nachkommastelle)
	 * UntAlter2 =  KalkJahr  + KalkMonRound/12;  (Jahr + Monat in Jahren)
	 * UntAlter2 = round(UntAlter2,.01);  (Runden auf 2 Nachommastellen -> exakt 12 distinkte Nachkommaziffern )
	 * 
	 * @param bDate
	 * @param imageDate
	 * @return
	 */
	public static String calculateAgeString(Date bDate, Date imageDate) {
		DecimalFormat dFormat = new DecimalFormat("#.00");
		
    	if (bDate == null || imageDate == null) 
    		return null;
    	
    	if (bDate.after(imageDate))
    		return null;
    	
		
    	Calendar bDayCal = new GregorianCalendar();
    	bDayCal.setTime(bDate);
    	Calendar imageCal = new GregorianCalendar();
    	imageCal.setTime(imageDate);
    	
    	// extract values
    	 int bDayYeNum = bDayCal.get(Calendar.YEAR);
    	 int bDayMoNum = bDayCal.get(Calendar.MONTH)+1;
    	 int bDayDaNum = bDayCal.get(Calendar.DAY_OF_MONTH);
    	 int imgYeNum = imageCal.get(Calendar.YEAR);
    	 int imgMoNum = imageCal.get(Calendar.MONTH)+1;
    	 int imgDaNum = imageCal.get(Calendar.DAY_OF_MONTH);
    	 
    	 // normalize month
    	 if (bDayDaNum > 30) bDayDaNum = 30;
    	 if (imgDaNum > 30) imgDaNum = 30;
    	 
    	 // calculate Date
    	 int ageYears = imgYeNum-bDayYeNum;
    	 double ageMonth = (imgMoNum-bDayMoNum) + (imgDaNum-bDayDaNum)/30.0;
    	 int ageMonthRounded = (int) Math.round(ageMonth);
    	 double age = ageYears + ageMonthRounded/12.0;
    	 String ageString = dFormat.format(age);
    	 
//    	 System.out.println("ageYears " + ageYears);
//    	 System.out.println("ageMonth " + ageMonth);
//    	 System.out.println("ageMonthRounded " + ageMonthRounded);
//    	 System.out.println("age " + age);
//    	 System.out.println("ageString " + ageString);
    	 
    	 
    	 return ageString;

	}
	
	public static String calculateAge(Date birthDate, Date imageDate) {
	 
	    String age = "";
	    long days = calculateAgeInDays(birthDate, imageDate);
	    
	    if ( (days / 365) > 2) {
	    	age = df.format(days / 365) +"Y";			
	    } else {
	    	age = df.format(days / 30) +"M";	
	    }
//	    } else if ( (days / 30) > 6) {
//		addTag(dObj, Tag.PatientAge, df.format(days / 30) +"M");	
//	    } else if ( (days / 7) > 1) {
//		addTag(dObj, Tag.PatientAge, df.format(days / 7) +"W");
//	    } else {
//		addTag(dObj, Tag.PatientAge, df.format(days) +"D");	
//	    }    
	  return age;  
	}

	public static DicomObject removePatientAdditionalInfo(DicomObject dObj) {
	    	
	    for (int tag : PATIENT_ADDITIONAL_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    return dObj;
	}
	
	public static DicomObject removePhysicianInfo(DicomObject dObj) {
	    
	    for (int tag : PHYSICIAN_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    return dObj;
	}
	
	public static DicomObject removeInstitutionInfo(DicomObject dObj, String institutionName) {
	    
	    for (int tag : ORGANISATION_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    for (int tag : PHYSICIAN_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    addTag(dObj, Tag.InstitutionName, institutionName);
	    
	    return dObj;
	}
	
	public static DicomObject removeManufacturerInfo(DicomObject dObj) {
	    	
	    for (int tag : MANUFACTURER_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    return dObj;
	}
	
	public static DicomObject removeImageData(DicomObject dObj) {
	    for (int tag : IMAGE_TAGS) {
			dObj.remove(tag);
	    }
	    return dObj;
	}
	
	public static DicomObject removeOverlayInfo(DicomObject dObj) {
	    	
	    for (int tag : OVERLAY_TAGS) {
		dObj.remove(tag);
//		replaceTag(dObj, tag, "");
	    }
	    
	    return dObj;
	}
	
	public static DicomObject removePrivateTags(DicomObject dObj) {
	    dObj = dObj.excludePrivate();
	    return dObj;
	}
	
	public static DicomObject removeTags(DicomObject dObj, String[] tagStrings) throws Exception{
		if (tagStrings == null || tagStrings.length == 0)
			return dObj;
		
		int[] tags = new int[tagStrings.length];
		for (int i = 0; i < tags.length; i++) {
			tags[i] = DicomHeader.toTagInt(tagStrings[i]);
		}
		
	    dObj = dObj.exclude(tags);
	    return dObj;
	}
	
	public static DicomObject removeTags(DicomObject dObj, int[] tags) {
		if (tags == null || tags.length == 0)
			return dObj;
		
	    dObj = dObj.exclude(tags);
	    return dObj;
	}
	
	

	
	
	
	/**
	 * Read the file into a dicom object
	 * @param f the dicom file
	 * @return the read dicom object
	 */
	public static DicomObject readDicomObject(File f) {
	    DicomObject dcmObj;
	    DicomInputStream din = null;
	    try {
	        din = new DicomInputStream(f);
	        dcmObj = din.readDicomObject();
	        return dcmObj;
	    }
	    catch (IOException e) {
		e.printStackTrace();
	        return null;
	    }
	    finally {
	        try {
	            din.close();
	        }
	        catch (IOException ignore) {
	        }
	    }
	}
	
	/**
	 * write the dicom object to a file
	 * @param dObj the object to write
	 * @param f the file to write to
	 */
	public static void writeDicomFile(DicomObject dObj, File f) {
	    FileOutputStream fos;
	    try {
	        fos = new FileOutputStream(f);
	    }
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	        return;
	    }
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    DicomOutputStream dos = new DicomOutputStream(bos);
	    try {
	        dos.writeDicomFile(dObj);
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	        return;
	    }
	    finally {
	        try {
	            dos.close();
	        }
	        catch (IOException ignore) {
	        }
	    }
	}
	
//	public static void main(String[] args) {
//		try {
////		    DicomAnonymizer.anonymizeFile(new File("/home/hermenj/1"));
//			
//			
////			long days = calculateAgeInDays(null, new Date(61,1,1));
////			System.out.println( days/365);
//			
//			
//			System.out.println(calculateAgeString(new Date(81,0,13), new Date()));
//			
//		} catch (Exception e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//	}

	
}

