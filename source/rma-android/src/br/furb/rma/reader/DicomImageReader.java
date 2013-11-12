package br.furb.rma.reader;

import java.io.File;
import java.io.IOException;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;

public class DicomImageReader {

	private static final int LIMIAR = 50;
	private File file;
	private boolean limiarEnabled = true;
	private boolean bBoxEnabled = false;
	
	private int maxX, minX, maxY, minY;
	private Dicom dicom;
	private DicomImage image;

	public DicomImageReader(Dicom dicom, File file) {
		super();
		this.dicom = dicom;
		this.file = file;
		maxX = 0;
		minX = 0;
		maxY = 0;
		minY = 0;
	}
	
	public DicomImage read() throws IOException {
		DicomInputStream inputStream = new DicomInputStream(file);
		DicomObject dicomObj = inputStream.readDicomObject();
		
		image = new DicomImage();
		double spacingBetweenSlices = dicomObj.getDouble(Tag.SpacingBetweenSlices);
		dicom.setSpacingBetweenSlices(spacingBetweenSlices);
		image.setBitsAllocated(dicomObj.getInt(Tag.BitsAllocated));
		image.setPixelRepresentation(dicomObj.getInt(Tag.PixelRepresentation)); // 1 - com sinal
		image.setColumns(dicomObj.getInt(Tag.Columns));
		image.setRows(dicomObj.getInt(Tag.Rows));
		image.setImageType(dicomObj.getString(Tag.ImageType));
		image.setBigEndian(dicomObj.bigEndian());
		byte[] dataSet = readImageDataSet(dicomObj, image.getBitsAllocated());
		int[] pixelData = toIntArray(dataSet);
//		int[] pixelData = readImageDataSet(dicomObj, image.getBitsAllocated());
		if(bBoxEnabled) {
			pixelData = crop(pixelData);
		}
		//image.setPixelData(pixelData);
		image.setBitmap(image.createBitmap(pixelData));
		//image.setFile(file);
		
		inputStream.close();
		
		pixelData = null;
		dicomObj = null;
		//dataSet = null;
		//pixels = null;
		//System.gc();
		
		return image;
	}
	
	private int[] crop(int[] pixelData) {
		int columns = maxX - minX;
		int rows = maxY - minY;
		int x, xOriginal, yOriginal;
		int y = 0;
		int[] pixels = new int[rows * columns];
		for (int i = 0; i < pixels.length; i++) {
			x = i - (y * image.getColumns());
			y = i / image.getColumns();
			xOriginal = x + minX;
			yOriginal = y + minY;
			pixels[i] = getPixelAt(pixelData, xOriginal, yOriginal);
		}
		return pixels;
	}
	
	private int getPixelAt(int[] pixels, int x, int y) {
		int index = x * image.getColumns();
		index += y;
		
		return pixels[index];
	}

	private int[] toIntArray(byte[] dataSet) {
		int[] array = new int[dataSet.length];
		int value = 0;
		int limiar = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < dataSet.length; i++) {
			x = i - (y * image.getColumns());
			y = i / image.getColumns();
			value = (int) dataSet[i];
			limiar = limiar(value);
			if(bBoxEnabled && limiar != 255) {
				if(x < minX) {
					minX = x;
				}
				if(x > maxX) {
					maxX = x;
				}
				if(y < minY) {
					minY = y;
				}
				if(y > maxY) {
					maxY = y;
				}
			}
			array[i] += limiar << 24;
			array[i] += value << 16;
			array[i] += value << 8;
			array[i] += value;
		}
		return array;
	}

	private byte[] readImageDataSet(DicomObject dicomObject, int bitsAllocated) {
		DicomElement element = dicomObject.get(Tag.PixelData);
		byte[] bytes = element.getBytes();
		
		short[] shortArray = null;
//		int[] transformed = null;
		if(bitsAllocated == 16) {
			shortArray = read16BitsImage(bytes);
//			transformed = read16BitsImageNew(bytes, dicomObject);
		}
		
		byte[] transformed = shortToBytes(shortArray, dicomObject);
		return transformed;
		
	}
	
	private int limiar(int pixel) {
		if(limiarEnabled && pixel < LIMIAR) {
			return 0;
		} else {
			return 255;
		}
	}

	private byte[] shortToBytes(short[] shortArray, DicomObject dicomObject) {
		if(shortArray != null) {
			int width = dicomObject.getInt(Tag.Rows);
			int height = dicomObject.getInt(Tag.Columns);
			int size = width * height;
			byte[] pixels8 = new byte[size];

			//findMinAndMax(input, width, height);
			
			double minimum = dicomObject.getDouble(Tag.WindowCenter) - dicomObject.getDouble(Tag.WindowWidth) / 2;
			double maximum = dicomObject.getDouble(Tag.WindowCenter) + dicomObject.getDouble(Tag.WindowWidth) / 2;
			if (minimum < 0.0)
				minimum = 0.0;
			if (maximum > 65535.0)
				maximum = 65535.0;
			int min = (int) minimum;
			int max = (int) maximum;
			
			int value;
			double scale = 256.0 / (max - min + 1);
			for (int i = 0; i < size; i++) {
				value = (shortArray[i] & 0xffff) - min;
				if (value < 0)
					value = 0;
				value = (int) (value * scale + 0.5);
				if (value > 255)
					value = 255;
				pixels8[i] = (byte) value;
			}
			
			return pixels8;
		} else {
			return null;
		}
	}
	
	private short[] read16BitsImage(byte[] pixels) {
		int totBytes = pixels.length;
		short[] shortArray = new short[totBytes / 2];

		for (int i = 0; i < shortArray.length; i++) {
			shortArray[i] = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
		}
		
		return shortArray;
	}

}