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
		image.setBitsAllocated(dicomObj.getInt(Tag.BitsAllocated));
		double spacingBetweenSlices = dicomObj.getDouble(Tag.SpacingBetweenSlices);
		dicom.setSpacingBetweenSlices(spacingBetweenSlices);
		image.setPixelRepresentation(dicomObj.getInt(Tag.PixelRepresentation)); // 1 - com sinal
		image.setColumns(dicomObj.getInt(Tag.Columns));
		image.setRows(dicomObj.getInt(Tag.Rows));
		image.setImageType(dicomObj.getString(Tag.ImageType));
		image.setBigEndian(dicomObj.bigEndian());
		
		minX = image.getColumns();
		minY = image.getRows();
		maxX = 0;
		maxY = 0;
		
		readImageDataSet(dicomObj, image.getBitsAllocated());
		
		image.setMatrix(matrix);
		
		image.setMinX(findMinX());
		image.setMaxX(findMaxX());
		image.setMinY(findMinY());
		image.setMaxY(findMaxY());
		
		inputStream.close();
		dicomObj = null;
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

	private void readImageDataSet(DicomObject dicomObject, int bitsAllocated) {
		DicomElement element = dicomObject.get(Tag.PixelData);
		byte[] bytes = element.getBytes();
		
		if(bitsAllocated == 16) {
			read16BitsImage(bytes, dicomObject);
		}
		
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
	
	private void read16BitsImage(byte[] pixels, DicomObject dicomObject) {
		int totBytes = pixels.length;
		int shortSize = totBytes / 2;
		
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

		for (int i = 0; i < shortSize; i++) {
			shortValue = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
			
			value = (shortValue & 0xffff) - min;
			if (value < 0)
				value = 0;
			value = (int) (value * scale + 0.5);
			if (value > 255)
				value = 255;
			
			limiar = limiar(value);
			
			matrix[x][y] += limiar << 24;
			matrix[x][y] += value << 16;
			matrix[x][y] += value << 8;
			matrix[x][y] += value;
			
			if(++x == image.getColumns()) {
				x = 0;
				y++;
			}
		}
	}

}