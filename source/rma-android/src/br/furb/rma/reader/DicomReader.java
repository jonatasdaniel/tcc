package br.furb.rma.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.media.DicomDirReader;

import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;

public class DicomReader {

	private File file;

	public DicomReader(File file) {
		super();
		this.file = file;
	}
	
	public Dicom read() throws IOException {
		Dicom dicom = new Dicom(file);
		
		DicomDirReader reader = new DicomDirReader(file);
		dicom.setPatient(readPatient(reader));
		dicom.setStudy(readStudy(reader));
		dicom.setImages(readImages(reader));
		
		return dicom;
	}
	
	private List<DicomImage> readImages(DicomDirReader reader) throws IOException {
		List<DicomImage> images = new ArrayList<DicomImage>();
		
		File dicomDir = new File(file.getParentFile().getAbsolutePath() + "/DICOM");
		File[] files = dicomDir.listFiles();
		Arrays.sort(files);
		
		if(files != null) {
			for (File file : files) {
				DicomImageReader imageReader = new DicomImageReader(file);
				DicomImage image = imageReader.read();
				images.add(image);
			}
		}
		
		return images;
	}

	private DicomPatient readPatient(DicomDirReader reader) throws IOException {
		DicomPatient patient = new DicomPatient();
		
		DicomObject dicomObject = reader.findFirstRootRecord();
		patient.setName(dicomObject.getString(Tag.PatientName));
		patient.setGender(dicomObject.getString(Tag.PatientSex));
		
		return patient;
	}
	
	private DicomStudy readStudy(DicomDirReader reader) throws IOException {
		DicomStudy study = new DicomStudy();
		
		return study;
	}

}