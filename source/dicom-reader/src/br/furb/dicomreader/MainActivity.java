package br.furb.dicomreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import br.furb.dicomreader.reader.DicomFileReader;

public class MainActivity extends Activity {

	private ImageView imageView;
	private SeekBar seekBar;
	private TextView tvImageName;
	private int min;
	private int max;
	private byte[] quadradoPreto;
	
	private List<byte[]> images = new ArrayList<byte[]>();
	private List<File> imageFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = (ImageView) findViewById(R.main.image);
		seekBar = (SeekBar) findViewById(R.main.seekr);
		tvImageName = (TextView) findViewById(R.main.image_name);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int position = seekBar.getProgress();
				byte[] image = images.get(position);
				File file = imageFiles.get(position);
				printImage(image, file);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
			}
		});

		String dirName = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		DicomFileReader reader = new DicomFileReader(dirName);
		try {
			// reader.read();
			reader.readDir();
			
			List<DicomObject> images = reader.readImages();
			imageFiles = reader.readImagesFiles();
			seekBar.setMax(images.size());
			for (DicomObject dicomObject : images) {
				byte[] pixels = reader.getPixelData(dicomObject);
				short[] shorts = read16BitImage(pixels);
				byte[] data = convertShortToByte(shorts, dicomObject);
				this.images.add(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
		//createImage(reader);
		createModafocaImage(reader);
	}

	protected void printImage(byte[] image, File file) {
		//BitmapFactory.Options opts = new BitmapFactory.Options();
		//opts.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ALPHA_8);
		bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(image));
		
		imageView.setImageBitmap(bitmap);
		
		tvImageName.setText(file.getName());
	}

	private void createModafocaImage(DicomFileReader reader) {
		try {
			DicomObject image = reader.readImages().get(0);
			File file = reader.readImagesFiles().get(0);
			int ba = image.getInt(Tag.BitsAllocated);
			int pr = image.getInt(Tag.PixelRepresentation); // 1 - com sinal
			int columns = image.getInt(Tag.Columns);
			int rows = image.getInt(Tag.Rows);
			String imageType = image.getString(Tag.ImageType);
			boolean big = image.bigEndian();

			byte[] pixels = reader.getPixelData(image);
			short[] shorts = read16BitImage(pixels);
			byte[] novo = convertShortToByte(shorts, image);
			
			printImage(novo, file);
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
