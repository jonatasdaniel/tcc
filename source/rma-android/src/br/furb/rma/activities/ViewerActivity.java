package br.furb.rma.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.Square;

public class ViewerActivity extends Activity {

	private GLSurfaceView surfaceView;
	private Dicom dicom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		final DicomReader reader = new DicomReader(new File(dirName)).maxImages(4);
		
		try {
			dicom = reader.read();
		} catch(Exception e) {
			e.printStackTrace();
		}

		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		surfaceView.setRenderer(new ViewerRenderer(new Square(dicom),
				ViewerActivity.this));
	}
	
	public void dicomDetailsClick(View view) {
		Intent intent = new Intent(this, DicomDetailsActivity.class);
		Bundle extras = new Bundle();
		DicomPatient patient = new DicomPatient();
		patient.setName("Jonatas Daniel Hermann");
		extras.putSerializable("patient", patient);
		extras.putSerializable("study", new DicomStudy());
		intent.putExtras(extras);
		startActivity(intent);
	}

}