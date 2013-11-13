package br.furb.rma.models;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;

public class DicomImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private File file;
	private byte[] dataSet;
	private int[] pixelData;
	private int[][] matrix;
	private Bitmap bitmap;
	private int bitsAllocated;
	private int pixelRepresentation;
	private int columns;
	private int rows;
	private String imageType;
	private int minX;
	private int maxX;
	private int minY;
	private int maxY;
	private boolean bigEndian;

	public Bitmap createBitmap(int[] pixelData) {
		Bitmap bmp = Bitmap.createBitmap(pixelData, columns, rows,
				Bitmap.Config.ARGB_8888);
		return bmp;
	}

	public Bitmap getBitmap() {
		if (pixelData != null && bitmap == null) {
			bitmap = createBitmap(getPixelData());
			pixelData = null;
		}
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
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

	public int[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}

	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}
	
	public void release() {
		dataSet = null;
		matrix = null;
	}

	public void applyBoundingBox(int minX, int maxX, int minY, int maxY) {
		int columns = maxX - minX + 1;
		int rows = maxY - minY + 1;
		setColumns(columns);
		setRows(rows);
		int x, xOriginal, yOriginal;
		int y = 0;
		int[] pixels = new int[rows * columns];
		int[][] newMatrix = new int[columns][rows];
		
		int count = 0;
		for (int i = 0; i < newMatrix.length; i++) {
			for (int j = 0; j < newMatrix[i].length; j++) {
				y = count / columns;
				x = count - (y * columns);
				xOriginal = x + minX;
				yOriginal = y + minY;
				
				newMatrix[i][j] = matrix[xOriginal][yOriginal];
				pixels[count] = newMatrix[i][j]; 
				count++;
			}
		}
		
		pixelData = pixels;
	}
	
	
}