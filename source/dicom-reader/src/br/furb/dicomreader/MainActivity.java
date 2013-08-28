package br.furb.dicomreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;
import br.furb.dicomreader.reader.DicomFileReader;

public class MainActivity extends Activity {

	private ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.main.image);

		String dirName = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		DicomFileReader reader = new DicomFileReader(dirName);
		try {
			// reader.read();
			reader.readDir();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//createImage(reader);
		createModafocaImage(reader);
	}

	public int[] invertPixels(int pixelData[]) {
		if (pixelData == null)
			return pixelData;
		for (int i = 0; i < pixelData.length; i++) {
			int pixelGrayLevel = pixelData[i] & 0xff;
			pixelGrayLevel = 255 - pixelGrayLevel;
			pixelData[i] = (0xFF << 24) | // alpha
					(pixelGrayLevel << 16) | // red
					(pixelGrayLevel << 8) | // green
					pixelGrayLevel; // blue
		}
		return pixelData;
	}
	
	private void createModafocaImage(DicomFileReader reader) {
		try {
			DicomObject image = reader.readImages().get(0);
			int ba = image.getInt(Tag.BitsAllocated);
			int pr = image.getInt(Tag.PixelRepresentation); // 1 - com sinal
			boolean big = image.bigEndian();

			byte[] pixels = reader.getPixelData(image);
			short[] shorts = read16BitImage(pixels);
			byte[] novo = convertShortToByte(shorts);
			byte[] bah = convertShortToByte2(shorts);
			boolean igual = novo.equals(bah);
			
			byte[][] arrays = new byte[][] {pixels, novo, bah};
			
			Bitmap bitmap = null;

			for (int i = 0; i < arrays.length; i++) {
				BitmapFactory.Options opts = new Options();
				opts.inScaled = false;
				bitmap = BitmapFactory.decodeByteArray(arrays[i], 0, arrays[i].length, opts);
				imageView.setImageBitmap(bitmap);
				bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(arrays[i]));
				imageView.setImageBitmap(bitmap);
				BitmapDrawable drawable = new BitmapDrawable(new ByteArrayInputStream(arrays[i]));
				if(drawable != null) {
					bitmap = drawable.getBitmap();
					imageView.setImageBitmap(bitmap);
					imageView.setImageDrawable(drawable);
				}
			}
			
			Toast.makeText(this, "foi", Toast.LENGTH_SHORT).show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] convertShortToByte2(short[] input) {
		int short_index, byte_index;
		int iterations = input.length;

		byte[] buffer = new byte[input.length * 2];

		short_index = byte_index = 0;

		for (/* NOP */; short_index != iterations; /* NOP */) {
			buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
			buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

			++short_index;
			byte_index += 2;
		}

		return buffer;
	}
	
	private byte[] convertShortToByte(short[] input) {
		int index;
		int iterations = input.length;

		ByteBuffer bb = ByteBuffer.allocate(input.length * 2);

		for (index = 0; index != iterations; ++index) {
			bb.putShort(input[index]);
		}

		return bb.array();
		
		/*int size = width*height;
		short[] pixels16 = (short[])ip.getPixels();
		byte[] pixels8 = new byte[size];
		boolean doScaling = false;
		if (doScaling) {
			int value, min=(int)ip.getMin(), max=(int)ip.getMax();
			double scale = 256.0/(max-min+1);
			for (int i=0; i<size; i++) {
				value = (pixels16[i]&0xffff)-min;
				if (value<0) value = 0;
				value = (int)(value*scale+0.5);
				if (value>255) value = 255;
				pixels8[i] = (byte)value;
			}
			return new ByteProcessor(width, height, pixels8, ip.getCurrentColorModel());
		} else {
			int value;
			for (int i=0; i < size; i++) {
				value = pixels16[i]&0xffff;
				if (value>255) value = 255;
				pixels8[i] = (byte)value;
			}
			return new ByteProcessor(width, height, pixels8, ip.getColorModel());
		}*/
	}

	private void createImage(DicomFileReader reader) {
		try {
			DicomObject image = reader.readImages().get(0);
			int ba = image.getInt(Tag.BitsAllocated);
			int pr = image.getInt(Tag.PixelRepresentation); // 1 - com sinal
			boolean big = image.bigEndian();

			byte[] pixels = reader.getPixelData(image);

			short[] shorts = new short[pixels.length / 2];
			for (int i = 0; i < shorts.length; i++) {
				shorts[i] += pixels[i] & 0xff << 8;
				shorts[i] += pixels[i + 1] & 0xff;
			}

			int[] intPixels = new int[pixels.length];
			for (int i = 0; i < pixels.length; i++) {
				intPixels[i] = (int) pixels[i];
			}

			Bitmap imageBitmap = Bitmap.createBitmap(intPixels, 2, 2,
					Bitmap.Config.ARGB_4444);

			Bitmap bitmap = BitmapFactory.decodeByteArray(pixels, 0,
					pixels.length);
			bitmap = BitmapFactory
					.decodeStream(new ByteArrayInputStream(pixels));
			imageView.setImageBitmap(imageBitmap);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private short[] read16BitImage(byte[] pixels) {
		int totBytes = pixels.length;
		short[] shortArray = new short[totBytes / 2];

		for (int i = 0; i < shortArray.length; i++)
			shortArray[i] = (short) (((pixels[2 * i + 1] & 0xFF) << 8) | (pixels[2 * i] & 0xFF));
		
		return shortArray;
	}

}
