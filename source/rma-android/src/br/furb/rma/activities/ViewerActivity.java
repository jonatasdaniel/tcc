package br.furb.rma.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.Square;

public class ViewerActivity extends Activity {

	private GLSurfaceView surfaceView;
	private List<ToggleButton> toggleControls;
	private ToggleButton btnAxial;
	private ToggleButton btnSagital;
	private ToggleButton btnCoronal;
	private ToggleButton btn2D;
	
	private Dicom dicom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		String path = getIntent().getStringExtra("path");
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		//String dirName = path + "/joelho_dalton/DICOMDIR";
		final DicomReader reader = new DicomReader(new File(dirName)).maxImages(35);
		
//		reader.setListener(new DicomReaderListener() {
//			
//			@Override
//			public void onChange(String status) {
//				Message msg = new Message();
//				msg.what = 1;
//				msg.obj = status;
//				handler.sendMessage(msg);
//			}
//		});
		
		try {
			dicom = reader.read();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		toggleControls = new ArrayList<ToggleButton>();
		toggleControls.add(btnAxial = (ToggleButton) findViewById(R.viewer.btn_axial));
		toggleControls.add(btnSagital = (ToggleButton) findViewById(R.viewer.btn_sagital));
		toggleControls.add(btnCoronal = (ToggleButton) findViewById(R.viewer.btn_coronal));
		toggleControls.add(btn2D = (ToggleButton) findViewById(R.viewer.btn_2d));
		
		for (ToggleButton btn : toggleControls) {
			btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) {
						for (ToggleButton toggle : toggleControls) {
							if(buttonView != toggle) {
								toggle.setOnCheckedChangeListener(null);
								toggle.setChecked(false);
								toggle.setOnCheckedChangeListener(this);
							}
						}
					} else {
						toggleControls.get(0).setChecked(true);
					}
				}
			});
		}
		
		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		surfaceView.setRenderer(new ViewerRenderer(new Square(dicom),
				ViewerActivity.this));
	}

	public void flatViewerClick(View view) {
		Intent intent = new Intent(this, FlatViewerActivity.class);
		startActivity(intent);
	}
	
	public void dicomDetailsClick(View view) {
		Intent intent = new Intent(this, DicomDetailsActivity.class);
		Bundle extras = new Bundle();
		DicomPatient patient = dicom.getPatient();
		extras.putSerializable("patient", patient);
		extras.putSerializable("study", new DicomStudy());
		intent.putExtras(extras);
		startActivity(intent);
	}

}