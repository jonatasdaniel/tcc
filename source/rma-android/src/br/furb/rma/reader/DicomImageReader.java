package br.furb.rma.reader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import br.furb.rma.LUTable;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;

public class DicomImageReader {

	private static final int LIMIAR = 50;
	private static final int BRIGHTNESS = (255 / 100) * 55;
	private static final int CONTRAST = (255 / 100) * 63;
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
		if(bBoxEnabled) {
			pixelData = crop(pixelData);
		}
		image.setBitmap(image.createBitmap(pixelData));
		image.setFile(file);
		
		inputStream.close();
		
		pixelData = null;
		dataSet = null;
		//pixels = null;
		System.gc();
		
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
		try {
			Method method = getReadMethod(bitsAllocated);
			shortArray = (short[]) method.invoke(this, bytes);
		} catch(Exception e) {
			return null;
		}
		
		byte[] transformed = shortToBytes(shortArray, dicomObject);
		return transformed;
		
	}

	public int[] convertToIntPixelData(byte bytePixels[], int bitsAllocated,
			int width, int height, boolean invert) {
		int outputPixels[] = null;
		// TODO show Memory Error Dialog
		// actually we do not accept byte data exceeding 4MB
		if (width * height * 4 > 4 * 1024 * 1024)
			return outputPixels;
		if (bytePixels == null || width < 1 || height < 1)
			return outputPixels;
		outputPixels = new int[width * height];
		int pixelGrayLevel = 0;
		int pixelGrayLevel2 = 0;
		int max = 0;
		int min = 65530;
		for (int i = 0; i < bytePixels.length; i++) {
			if (bitsAllocated == 16) {
				int intPixelLevel = (bytePixels[i + 1] & 0xff) << 8
						| (bytePixels[i] & 0xff);
				if (intPixelLevel > max)
					max = intPixelLevel;
				if (intPixelLevel < min)
					min = intPixelLevel;
				i++;
			} else if (bitsAllocated == 12) {
				int intPixelLevel = (bytePixels[i + 2] & 0xff) << 8
						| (bytePixels[i + 1] & 0xf0);
				int intPixelLevel2 = (bytePixels[i + 1] & 0x0f) << 8
						| (bytePixels[i] & 0xff);
				if (intPixelLevel > max)
					max = intPixelLevel;
				if (intPixelLevel < min)
					min = intPixelLevel;
				if (intPixelLevel2 > max)
					max = intPixelLevel2;
				if (intPixelLevel2 < min)
					min = intPixelLevel2;
				i += 2;
			}
		}
		int windowWidth = max - min;
		int windowOffset = min;
		
		LUTable lut = new LUTable();

		double contrastVal = Math.pow(CONTRAST / 127., 2);
		lut.setContrast(contrastVal);
		lut.setBrightness(256 - BRIGHTNESS);
		
		for (int i = 0, j = 0; i < bytePixels.length; i++) {
			if (bitsAllocated == 16) {
				int intPixelLevel = (bytePixels[i + 1] & 0xff) << 8
						| (bytePixels[i] & 0xff);

				pixelGrayLevel = (256 * (intPixelLevel - windowOffset) / windowWidth);
				pixelGrayLevel = (pixelGrayLevel > 255) ? 255
						: ((pixelGrayLevel < 0) ? 0 : pixelGrayLevel);
				if (invert)
					pixelGrayLevel = 255 - pixelGrayLevel;
				i++;
//				outputPixels[j++] = limiar((0xFF << 24) | // alpha
//						(pixelGrayLevel << 16) | // red
//						(pixelGrayLevel << 8) | // green
//						pixelGrayLevel/* blue */);
				
				outputPixels[j] = limiar((0xFF << 24) | // alpha
						(pixelGrayLevel << 16) | // red
						(pixelGrayLevel << 8) | // green
						pixelGrayLevel/* blue */);
				
				/* aplica brilho e contraste */
				/*int gray = outputPixels[j] & 0xffffffff;

				int red = (gray >> 16) & 0xff;
				int green = (gray >> 8) & 0xff;
				int blue = gray & 0xff;

				//red = lut.getValue(red);
				//green = lut.getValue(green);
				//blue = lut.getValue(blue);

				//pixelData[i] = (0xFF << 24) | (red << 16) | (green << 8) | blue;
				outputPixels[j] = limiar((0xFF << 24) | (red << 16) | (green << 8) | blue);*/
				
				j++;
				
			} else if (bitsAllocated == 12) {
				int intPixelLevel = (bytePixels[i + 2] & 0xff) << 8
						| (bytePixels[i + 1] & 0xf0);
				int intPixelLevel2 = (bytePixels[i + 1] & 0x0f) << 8
						| (bytePixels[i] & 0xff);
				pixelGrayLevel = 128 * (intPixelLevel - windowOffset)
						/ windowWidth;
				pixelGrayLevel = (pixelGrayLevel > 255) ? 255
						: ((pixelGrayLevel < 0) ? 0 : pixelGrayLevel);
				pixelGrayLevel2 = 128 * (intPixelLevel2 - windowOffset)
						/ windowWidth;
				pixelGrayLevel2 = (pixelGrayLevel2 > 255) ? 255
						: ((pixelGrayLevel2 < 0) ? 0 : pixelGrayLevel2);

				i += 2;

				outputPixels[j++] = limiar((0xFF << 24) | // alpha
						(pixelGrayLevel2 << 16) | // red
						(pixelGrayLevel2 << 8) | // green
						pixelGrayLevel2/* blue */);

				outputPixels[j++] = limiar((0xFF << 24) | // alpha
						(pixelGrayLevel << 16) | // red
						(pixelGrayLevel << 8) | // green
						pixelGrayLevel/* blue */); 
			} else {
				pixelGrayLevel = bytePixels[i] & 0xff;// > 0 ? bytePixels[i] :
														// 255 - bytePixels[i];

				outputPixels[j++] = limiar((0xFF << 24) | // alpha
						(pixelGrayLevel << 16) | // red
						(pixelGrayLevel << 8) | // green
						pixelGrayLevel/* blue */); 
			}
		}
		return outputPixels;
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

	private Method getReadMethod(int bitsAllocated) throws SecurityException, NoSuchMethodException {
		StringBuilder methodName = new StringBuilder();
		methodName.append("read").append(bitsAllocated).append("BitsImage");
		Method method = getClass().getDeclaredMethod(methodName.toString(), byte[].class);
		return method;
	}

	@SuppressWarnings("unused")
	private short[] read16BitsImage(byte[] pixels) {
		int totBytes = pixels.length;
		short[] shortArray = new short[totBytes / 2];

		for (int i = 0; i < shortArray.length; i++)
			shortArray[i] = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
		
		return shortArray;
	}

}