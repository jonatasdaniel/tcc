package br.furb.dicomreader.reader;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.io.DicomInputStream;

import android.graphics.Bitmap;


public class DicomFileReader {
	
	private final File file;
	
	public DicomFileReader(String fileName) {
		this.file = new File(fileName);
	}
	
	public void read() throws IOException {
		DicomInputStream inputStream = new DicomInputStream(file);
		DicomObject dicomObj = inputStream.readDicomObject();
		
		Object modafoca = dicomObj.get(org.dcm4che2.data.Tag.DateTime);
		System.out.println(dicomObj);
	}
	
	public Bitmap getBitmap() {
		
		return null;
	}

}