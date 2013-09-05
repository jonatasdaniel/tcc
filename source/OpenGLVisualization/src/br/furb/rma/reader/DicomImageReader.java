package br.furb.rma.reader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;

import br.furb.rma.models.DicomImage;

public class DicomImageReader {

	private File file;

	public DicomImageReader(File file) {
		super();
		this.file = file;
	}
	
	public DicomImage read() throws IOException {
		DicomInputStream inputStream = new DicomInputStream(file);
		DicomObject dicomObj = inputStream.readDicomObject();
		
		DicomImage image = new DicomImage();
		
		image.setBitsAllocated(dicomObj.getInt(Tag.BitsAllocated));
		image.setPixelRepresentation(dicomObj.getInt(Tag.PixelRepresentation)); // 1 - com sinal
		image.setColumns(dicomObj.getInt(Tag.Columns));
		image.setRows(dicomObj.getInt(Tag.Rows));
		image.setImageType(dicomObj.getString(Tag.ImageType));
		image.setBigEndian(dicomObj.bigEndian());
		byte[] dataSet = readImageDataSet(dicomObj, image.getBitsAllocated());
		image.setDataSet(dataSet);
		image.setFile(file);
		
		inputStream.close();
		
		return image;
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
		
		return shortToBytes(shortArray, dicomObject);
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