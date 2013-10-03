package br.furb.rma.models;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class DicomImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private File file;
	private byte[] dataSet;
	private int[] pixelData;
	private Bitmap bitmap;
	private int bitsAllocated;
	private int pixelRepresentation;
	private int columns;
	private int rows;
	private String imageType;
	private boolean bigEndian;

	public Bitmap createBitmap(int[] pixelData) {
		Bitmap bmp = Bitmap.createBitmap(pixelData, columns, rows,
				Bitmap.Config.ARGB_8888);
		//Bitmap novo = toGrayscale(bmp);
		Bitmap novo = bmp;
		return novo;
	}
	
	public Bitmap toGrayscale(Bitmap bmpOriginal)
    {        
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();    

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

	public Bitmap getBitmap() {
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
}