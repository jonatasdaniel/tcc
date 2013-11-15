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
	private boolean bBoxEnabled = true;
	
	private int maxX, minX, maxY, minY;
	private Dicom dicom;
	private DicomImage image;
	private int[][] matrix;

	public DicomImageReader(Dicom dicom, File file) {
		super();
		this.dicom = dicom;
		this.file = file;
		maxX = -1;
		minX = -1;
		maxY = -1;
		minY = -1;
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
		
		minX = image.getColumns();
		minY = image.getRows();
		maxX = 0;
		maxY = 0;
		
		//byte[] dataSet = readImageDataSet(dicomObj, image.getBitsAllocated());
		readImageDataSet(dicomObj, image.getBitsAllocated());
		//int[] pixelData = toIntArray(dataSet);
//		int[] pixelData = readImageDataSet(dicomObj, image.getBitsAllocated());
		//matrix = toMatrix(dataSet);
		image.setMatrix(matrix);
		//findMinMax();
		image.setMinX(findMinX());
		image.setMaxX(findMaxX());
		image.setMinY(findMinY());
		image.setMaxY(findMaxY());
		if(bBoxEnabled) {
			//pixelData = crop(pixelData, matriz);
		}
		//image.setPixelData(pixelData);
		//image.setBitmap(image.createBitmap(pixelData));
		//image.setFile(file);
		
		inputStream.close();
		
		//pixelData = null;
		dicomObj = null;
		//dataSet = null;
//		pixels = null;
		//System.gc();
		
		return image;
	}

	private int findMaxY() {
		for (int y = matrix[0].length - 1; y >= 0; y--) {
			for (int x = 0; x < matrix.length; x++) {
				if (matrix[x][y] != 0) {
					maxY = y;
					return maxY;
				}
			}
		}
		return 0;
	}

	private int findMinY() {
		for (int y = 0; y < matrix[0].length; y++) {
			for (int x = 0; x < matrix.length; x++) {
				if(matrix[x][y] != 0) {
					minY = y;
					return minY;
				}
			}
		}
		return 0;
	}

	private int findMaxX() {
		for (int i = matrix.length - 1; i >= 0; i--) {
			for (int j = 0; j < matrix[i].length; j++) {
				if(matrix[i][j] != 0) {
					maxX = i;
					return maxX;
				}
			}
		}
		return image.getColumns();
	}

	private int findMinX() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if(matrix[i][j] != 0) {
					minX = i;
					return minX;
				}
			}
		}
		return 0;
	}

	private void findMinMax() {		
		for (int i = matrix.length / 2; i >= 0; i--) {
			for (int j = 0; j < matrix[i].length; j++) {
				if(matrix[i][j] != 0) {
					if(i < minX) {
						minX = i;
					}
				}
				int x = matrix.length - (i + 1);
				if(matrix[x][j] != 0) {
					if(x > maxX) {
						maxX = x;
					}
				}
			}
		}
		
		for (int i = 0; i < matrix.length; i++) {
			for(int j = matrix[i].length / 2; j >= 0; j--) {
				if(matrix[i][j] != 0) {
					if(j < minY) {
						minY = j;
					}
				}
				
				int y = matrix[i].length - (j + 1);
				if(matrix[i][y] != 0) {
					if(y > maxY) {
						maxY = y;
					}
				}
			}
		}
	}

	private int[] crop(int[] pixelData, int[][] matriz) {
		int columns = maxX - minX;
		int rows = maxY - minY;
		image.setColumns(columns);
		image.setRows(rows);
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
				
				newMatrix[i][j] = matriz[xOriginal][yOriginal];
				pixels[count] = newMatrix[i][j]; 
				count++;
			}
			
			
		}
		
		return pixels;
	}
	
	private int getPixelAt(int[] pixels, int x, int y) {
		int index = x * image.getColumns();
		index += y;
		
		return pixels[index];
	}

	private int[][] toMatrix(byte[] dataSet) {
		matrix = new int[image.getColumns()][image.getRows()];
		int value = 0;
		int limiar = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < dataSet.length; i++) {
			y = i / image.getColumns();
			x = i - (y * image.getColumns());
			
			value = (int) dataSet[i];
			limiar = limiar(value);
			
			matrix[x][y] += limiar << 24;
			matrix[x][y] += value << 16;
			matrix[x][y] += value << 8;
			matrix[x][y] += value;
		}
		
		return matrix;
	}
	
	private int[] toIntArray(byte[] dataSet) {
		matrix = new int[512][512];
		int[] array = new int[dataSet.length];
		int value = 0;
		int limiar = 0;
		int x = 0;
		int y = 0;
		for (int i = 0; i < dataSet.length; i++) {
			y = i / image.getColumns();
			x = i - (y * image.getColumns());
			
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
			
			matrix[x][y] = array[i];
		}
		
		return array;
	}

	private void readImageDataSet(DicomObject dicomObject, int bitsAllocated) {
		DicomElement element = dicomObject.get(Tag.PixelData);
		byte[] bytes = element.getBytes();
		
		short[] shortArray = null;
//		int[] transformed = null;
		if(bitsAllocated == 16) {
			shortArray = read16BitsImage(bytes, dicomObject);
//			transformed = read16BitsImageNew(bytes, dicomObject);
		}
		
		//byte[] transformed = shortToBytes(shortArray, dicomObject);
		//return transformed;
		bytes = null;
		element = null;		
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
			//byte[] pixels8 = new byte[size];

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
			int x = 0;
			int y = 0;
			int limiar = 0;
			double scale = 256.0 / (max - min + 1);
			
			matrix = new int[image.getColumns()][image.getRows()];
			
			for (int i = 0; i < size; i++) {
				value = (shortArray[i] & 0xffff) - min;
				if (value < 0)
					value = 0;
				value = (int) (value * scale + 0.5);
				if (value > 255)
					value = 255;
				//pixels8[i] = (byte) value;
				
				y = i / image.getColumns();
				x = i - (y * image.getColumns());
				
				//value = (int) pixels8[i];
				limiar = limiar(value);
				
				matrix[x][y] += limiar << 24;
				matrix[x][y] += value << 16;
				matrix[x][y] += value << 8;
				matrix[x][y] += value;
			}
			
			//return pixels8;
			return null;
		} else {
			return null;
		}
	}
	
	private short[] read16BitsImage(byte[] pixels, DicomObject dicomObject) {
		int totBytes = pixels.length;
		//short[] shortArray = new short[totBytes / 2];
		int shortSize = totBytes / 2;
		
		int width = image.getColumns();
		int height = image.getRows();
		int size = width * height;
		//byte[] pixels8 = new byte[size];

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
		int x = 0;
		int y = 0;
		int limiar = 0;
		double scale = 256.0 / (max - min + 1);
		
		matrix = new int[image.getColumns()][image.getRows()];
		
		short shortValue = 0;

		//for (int i = 0; i < shortArray.length; i++) {
		for (int i = 0; i < shortSize; i++) {
			//shortArray[i] = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
			shortValue = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
			
			//value = (shortArray[i] & 0xffff) - min;
			value = (shortValue & 0xffff) - min;
			if (value < 0)
				value = 0;
			value = (int) (value * scale + 0.5);
			if (value > 255)
				value = 255;
			//pixels8[i] = (byte) value;
			
			y = i / image.getColumns();
			x = i - (y * image.getColumns());
			
			//value = (int) pixels8[i];
			limiar = limiar(value);
			
			matrix[x][y] += limiar << 24;
			matrix[x][y] += value << 16;
			matrix[x][y] += value << 8;
			matrix[x][y] += value;
		}
		
		//return shortArray;
		return null;
	}

}