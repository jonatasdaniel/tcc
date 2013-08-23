package br.furb.dicomreader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import br.furb.dicomreader.reader.DicomFileReader;

public class MainActivity extends Activity {

	private ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		imageView = (ImageView) findViewById(R.main.image);
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		String[] list = new File(dirName).getParentFile().list();
		for (String f : list) {
			System.out.println(f);
		}
		DicomFileReader reader = new DicomFileReader(dirName);
		try {
			//reader.read();
			reader.readDir();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		createImage(reader);
	}

	public int[] invertPixels(int pixelData[])
    {
            if(pixelData == null)return pixelData;
            for(int i = 0; i < pixelData.length; i++)
            {
                    int pixelGrayLevel = pixelData[i] & 0xff;
                    pixelGrayLevel = 255 - pixelGrayLevel;
                    pixelData[i] = (0xFF << 24) | // alpha
                    (pixelGrayLevel << 16) | // red
                    (pixelGrayLevel << 8) | // green
                    pixelGrayLevel; // blue
            }
            return pixelData;
    }
	
	private void createImage(DicomFileReader reader) {
		try {
			byte[] pixels = reader.getPixelData(reader.readImages().get(0));
			int[] intPixels = new int[pixels.length];
			for (int i = 0; i < pixels.length; i++) {
				intPixels[i] = (int) pixels[i];
			}
			
			Bitmap imageBitmap = Bitmap.createBitmap(intPixels, 2, 2, Bitmap.Config.ARGB_8888);
			Drawable d = Drawable.createFromStream(new ByteArrayInputStream(pixels), "img");
			Bitmap bitmap = BitmapFactory.decodeByteArray(pixels, 0, pixels.length);
			bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(pixels));
			imageView.setImageBitmap(imageBitmap);
		} catch(Exception e) {
			System.out.println(e);
		}
	}

}
