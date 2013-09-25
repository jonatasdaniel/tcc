package br.furb.rma.models;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;

public class DicomImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private File file;
	private byte[] dataSet;
	private int[] pixelData;
	private int bitsAllocated;
	private int pixelRepresentation;
	private int columns;
	private int rows;
	private String imageType;
	private boolean bigEndian;
	
	public Bitmap createBitmap() {
		Bitmap bmp = Bitmap.createBitmap(pixelData, columns, rows, Bitmap.Config.ARGB_8888);
		return bmp;
	}

	public byte[] getDataSet() {
		return dataSet;
	}

	public void setDataSet(byte[] dataSet) {
		this.dataSet = dataSet;
	}

	public int getBitsAllocated() {
		return bitsAllocated;
	}

	public void setBitsAllocated(int bitsAllocated) {
		this.bitsAllocated = bitsAllocated;
	}

	public int getPixelRepresentation() {
		return pixelRepresentation;
	}

	public void setPixelRepresentation(int pixelRepresentation) {
		this.pixelRepresentation = pixelRepresentation;
	}

	public int getColumns() {
		return columns;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public void setBigEndian(boolean bigEndian) {
		this.bigEndian = bigEndian;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int[] getPixelData() {
		return pixelData;
	}

	public void setPixelData(int[] pixelData) {
		this.pixelData = pixelData;
	}
}