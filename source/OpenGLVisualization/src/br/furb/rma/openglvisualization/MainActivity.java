package br.furb.rma.openglvisualization;

import java.io.File;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import br.furb.rma.models.Dicom;
import br.furb.rma.reader.DicomReader;

public class MainActivity extends Activity {

	private GLSurfaceView surfaceView;
	private ImageView imageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		surfaceView = (GLSurfaceView) findViewById(R.main.gl_surface_view);
		//imageView = (ImageView) findViewById(R.main.image_view);
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		DicomReader reader = new DicomReader(new File(dirName));
		try {
			Dicom dicom = reader.read();
			surfaceView.setRenderer(new GLRenderer(new Square(dicom), this));
			//imageView.setImageBitmap(dicom.getImages().get(0).createBitmap());
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

}
