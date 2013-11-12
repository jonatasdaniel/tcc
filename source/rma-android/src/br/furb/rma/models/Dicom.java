package br.furb.rma.models;

import java.io.File;
import java.util.List;

public class Dicom {

	private String name;
	private File file;

	private double spacingBetweenSlices;

	private DicomPatient patient;

	private List<DicomImage> images;

	public Dicom(File file) {
		super();
		this.name = file.getName();
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public DicomPatient getPatient() {
		return patient;
	}

	public void setPatient(DicomPatient patient) {
		this.patient = patient;
	}

	public List<DicomImage> getImages() {
		return images;
	}

	public void setImages(List<DicomImage> images) {
		this.images = images;
	}

	public double getSpacingBetweenSlices() {
		return spacingBetweenSlices;
	}

	public void setSpacingBetweenSlices(double spacingBetweenSlices) {
		this.spacingBetweenSlices = spacingBetweenSlices;
	}

}