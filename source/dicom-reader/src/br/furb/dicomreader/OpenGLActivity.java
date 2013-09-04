package br.furb.dicomreader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import br.furb.dicomreader.reader.DicomFileReader;
import android.app.Activity;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;

public class OpenGLActivity extends Activity {

	private GLSurfaceView glSurfaceView;
	private List<byte[]> images = new ArrayList<byte[]>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_gl);
		
		readImages();
		
		Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ALPHA_8);
		bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(images.get(0)));
		
		glSurfaceView = (GLSurfaceView) findViewById(R.main.gl);
		glSurfaceView.setRenderer(new GLRenderer(bitmap));
	}
	
	private void readImages() {
		String dirName = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		DicomFileReader reader = new DicomFileReader(dirName);
		try {
			// reader.read();
			reader.readDir();
			
			List<DicomObject> images = reader.readImages();
			for (DicomObject dicomObject : images) {
				byte[] pixels = reader.getPixelData(dicomObject);
				short[] shorts = read16BitImage(pixels);
				byte[] data = convertShortToByte(shorts, dicomObject);
				this.images.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private short[] read16BitImage(byte[] pixels) {
		int totBytes = pixels.length;
		short[] shortArray = new short[totBytes / 2];

		for (int i = 0; i < shortArray.length; i++)
			shortArray[i] = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
		
		return shortArray;
	}
	
	private byte[] convertShortToByte(short[] input, DicomObject image) {
		int width = image.getInt(Tag.Rows);
		int height = image.getInt(Tag.Columns);
		int size = width * height;
		byte[] pixels8 = new byte[size];

		//findMinAndMax(input, width, height);
		
		double minimum = image.getDouble(Tag.WindowCenter) - image.getDouble(Tag.WindowWidth) / 2;
		double maximum = image.getDouble(Tag.WindowCenter) + image.getDouble(Tag.WindowWidth) / 2;
		if (minimum<0.0)
			minimum = 0.0;
		if (maximum>65535.0)
			maximum = 65535.0;
		int min = (int)minimum;
		int max = (int)maximum;
		
		int value;
		double scale = 256.0 / (max - min + 1);
		for (int i = 0; i < size; i++) {
			value = (input[i] & 0xffff) - min;
			if (value < 0)
				value = 0;
			value = (int) (value * scale + 0.5);
			if (value > 255)
				value = 255;
			pixels8[i] = (byte) value;
		}
		
		return pixels8;
	}

}