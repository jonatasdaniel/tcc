package br.furb.dicomreader.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.media.DicomDirReader;
import org.dcm4che2.media.FileSetInformation;

import android.graphics.Bitmap;

public class DicomFileReader {

	private final File file;
	private List<DicomObject> images;
	private List<File> imageFiles = new ArrayList<File>();
	

	public DicomFileReader(String dirName) {
		this.file = new File(dirName);
	}

	public void read() throws IOException {
		DicomInputStream inputStream = new DicomInputStream(file);
		DicomObject dicomObj = inputStream.readDicomObject();
		String pid = dicomObj.getString(Tag.PatientName);
		// DicomObject pat = inputStream.findPatientRecord(pid);
	}

	public void readDir() throws IOException {
		DicomDirReader din = new DicomDirReader(file);
		FileSetInformation info = din.getFileSetInformation();
		DicomObject dicomObj = info.getDicomObject();

		DicomObject patient = din.findFirstRootRecord();
		String sex = patient.getString(Tag.PatientSex);
		String name = patient.getString(Tag.PatientName);
		System.out.println(name);
	}
	
	public List<DicomObject> readImages() throws IOException {
		if(images == null) {
			images = new ArrayList<DicomObject>();
			
			File dicomDir = new File(file.getParentFile().getAbsolutePath() + "/DICOM");
			File[] files = dicomDir.listFiles();
			Arrays.sort(files);
			
			if(files != null) {
				for (File file : files) {
					imageFiles.add(file);
					DicomInputStream inputStream = new DicomInputStream(file);
					DicomObject dicomObj = inputStream.readDicomObject();
					images.add(dicomObj);
					inputStream.close();
				}
			}
		}
		
		return images;
	}
	
	public List<File> readImagesFiles() throws IOException {
		return imageFiles;
	}
	
	public byte[] getPixelData(DicomObject image) {
		DicomElement element = image.get(Tag.PixelData);
		return element.getBytes();
	}

	public List<DicomObject> getImagesFromSeries(DicomDirReader dir, DicomObject seriesRecord) {
		List<DicomObject> imageVector = new ArrayList<DicomObject>();
		try {
			DicomObject next = dir.findFirstChildRecord(seriesRecord);
			while (next != null) {
				try {
					imageVector.add(next);
				} catch (Exception e) {
					
				}
				next = dir.findNextSiblingRecord(next);
			}
		} catch (IOException e) {
			
		}
		return imageVector;
	}
	
	public File[] getImagePathsFromSeries(DicomDirReader dir, DicomObject seriesRecord) {
		List<DicomObject> imageVector = getImagesFromSeries(dir, seriesRecord);
		File[] files = new File[imageVector.size()];
		int i = 0;
		for (Iterator<DicomObject> iter = imageVector.iterator(); iter.hasNext();) {
			DicomObject element = (DicomObject) iter.next();
			try {
				File f = dir.toReferencedFile(element);

				files[i] = f.getAbsoluteFile();
				i++;
			} catch (Exception e) {
				
			}
		}
		return files;
	}

	public Bitmap getBitmap() {

		return null;
	}

}