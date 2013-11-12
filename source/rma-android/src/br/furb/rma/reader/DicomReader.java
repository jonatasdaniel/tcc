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

public class DicomReader {

	private static List<Dicom> stack;
	
	private File file;
	private DicomReaderListener listener;
	private boolean lazy;
	private int maxImages;
	private Dicom dicom;
	
	public DicomReader(File file) {
		super();
		stack = new ArrayList<Dicom>();
		this.file = file;
	}
	
	public DicomReader lazy(boolean lazy) {
		this.lazy = lazy;
		return this;
	}
	
	public DicomReader maxImages(int max) {
		this.maxImages = max;
		return this;
	}
	
	public void setListener(DicomReaderListener listener) {
		this.listener = listener;
	}
	
	public Dicom read() throws IOException {
		dicom = new Dicom(file);
		
		DicomDirReader reader = new DicomDirReader(file);
		dicom.setPatient(readPatient(reader));
		if(!lazy) {
			dicom.setImages(readImages());
		}
		
		stack.add(dicom);
		
		return dicom;
	}
	
	private List<DicomImage> readImages() throws IOException {
		List<DicomImage> images = new ArrayList<DicomImage>();
		
		File dicomDir = new File(file.getParentFile().getAbsolutePath() + "/DICOM");
		File[] files = dicomDir.listFiles();
		Arrays.sort(files);
		
		int count = 0;
		int size = files.length;
		
		if(files != null) {
			for (File file : files) {
				if(count++ == maxImages) {
					break;
				}
				DicomImageReader imageReader = new DicomImageReader(dicom, file);
				if(listener != null) {
					listener.onChange("Lendo imagem " + count + " de " + size);
				}
				DicomImage image = imageReader.read();
				images.add(image);
			}
		}
		
		return images;
	}

	private DicomPatient readPatient(DicomDirReader reader) throws IOException {
		if(listener != null) {
			listener.onChange("Lendo paciente");
		}
		DicomPatient patient = new DicomPatient();
		
		DicomObject dicomObject = reader.findFirstRootRecord();
		patient.setName(dicomObject.getString(Tag.PatientName));
		patient.setGender(dicomObject.getString(Tag.PatientSex));
		
		return patient;
	}
	
	public static Dicom getLastDicomReaded() {
		if(!stack.isEmpty()) {
			return stack.get(stack.size()-1);
		} else {
			return null;
		}
	}

}