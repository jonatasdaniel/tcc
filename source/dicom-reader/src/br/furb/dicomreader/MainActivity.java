package br.furb.dicomreader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.furb.dicomreader.reader.DicomFileReader;

public class MainActivity extends Activity {

	private ImageView imageView;
	private int min;
	private int max;
	private byte[] quadradoPreto;

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
			int columns = image.getInt(Tag.Columns);
			int rows = image.getInt(Tag.Rows);
			String imageType = image.getString(Tag.ImageType);
			boolean big = image.bigEndian();

			byte[] pixels = reader.getPixelData(image);
			short[] shorts = read16BitImage(pixels);
			byte[] novo = convertShortToByte(shorts, image);
			//byte[] bah = convertShortToByte2(shorts);
			
			novo = quadradoPreto();
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			Bitmap bitmap = BitmapFactory.decodeByteArray(novo, 0, novo.length, opts);
			
			imageView.setImageBitmap(bitmap);
			
			
			bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ALPHA_8);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			stream.write(novo);
			boolean yes = bitmap.compress(CompressFormat.JPEG, 100, stream);
			yes = bitmap.compress(CompressFormat.PNG, 100, stream);
			byte[] opa = stream.toByteArray();
			boolean igual = novo.equals(opa);
			
			bitmap = BitmapFactory.decodeByteArray(opa, 0, opa.length);
			
			bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_4444);
			stream = new ByteArrayOutputStream();
			stream.write(novo);
			yes = bitmap.compress(CompressFormat.JPEG, 100, stream);
			yes = bitmap.compress(CompressFormat.PNG, 100, stream);
			opa = stream.toByteArray();
			igual = novo.equals(opa);
			
			bitmap = BitmapFactory.decodeByteArray(opa, 0, opa.length);
			
			bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
			stream = new ByteArrayOutputStream();
			stream.write(novo);
			yes = bitmap.compress(CompressFormat.JPEG, 100, stream);
			yes = bitmap.compress(CompressFormat.PNG, 100, stream);
			opa = stream.toByteArray();
			igual = novo.equals(opa);
			
			bitmap = BitmapFactory.decodeByteArray(opa, 0, opa.length);
			
			bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
			stream = new ByteArrayOutputStream();
			stream.write(novo);
			yes = bitmap.compress(CompressFormat.JPEG, 100, stream);
			yes = bitmap.compress(CompressFormat.PNG, 100, stream);
			opa = stream.toByteArray();
			igual = novo.equals(opa);
			
			bitmap = BitmapFactory.decodeByteArray(opa, 0, opa.length);
			
			imageView.setImageBitmap(bitmap);
			
			Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private byte[] quadradoPreto() {
		if(quadradoPreto != null) {
			return quadradoPreto;
		}
		quadradoPreto = new byte[512 * 512];
		
		int linha = 0;
		int coluna = 0;
		
		for (int i = 0; i < quadradoPreto.length; i++) {
			linha = i / 512;
			coluna = i - linha * 512;
			
			quadradoPreto[i] = 1;
			
			if(linha > 150 && coluna > 150) {
				quadradoPreto[i] = 0;
			}
			if(linha < 350 && coluna > 150) {
				quadradoPreto[i] = 0;
			}
			if(linha > 150 && coluna < 350) {
				quadradoPreto[i] = 0;
			}
			if(linha > 150 && linha < 350 && coluna > 150 && coluna < 350) {
				quadradoPreto[i] = 0;
			}
		}
		
		return quadradoPreto;
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
		min = (int)minimum;
		max = (int)maximum;
		
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

	public void findMinAndMax(short[] pixels, int width, int height) {
		int size = width * height;
		int value;
		min = 65535;
		max = 0;
		for (int i = 0; i < size; i++) {
			value = pixels[i] & 0xffff;
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}
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
