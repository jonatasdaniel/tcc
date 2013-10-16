package br.furb.rma.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import br.furb.rma.R;
import br.furb.rma.models.Camera;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.PhotoCube;
import br.furb.rma.view.Square;

public class ViewerActivity extends Activity {

	private GLSurfaceView surfaceView;
	private List<Button> buttons;
	private Button btnAxial;
	private Button btnSagital;
	private Button btnCoronal;
	private Button btn2D;
	private SeekBar seekBar;
	
	private Dicom dicom;
	private ViewerRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		final DicomReader reader = new DicomReader(new File(dirName));
		
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
			dicom = reader.maxImages(30).read();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		buttons = new ArrayList<Button>();
		buttons.add(btnAxial = (Button) findViewById(R.viewer.btn_axial));
		buttons.add(btnSagital = (Button) findViewById(R.viewer.btn_sagital));
		buttons.add(btnCoronal = (Button) findViewById(R.viewer.btn_coronal));
		buttons.add(btn2D = (Button) findViewById(R.viewer.btn_2d));
		
		for (final Button btn : buttons) {
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					for (final Button button : buttons) {
						if(v != button) {
							button.setOnClickListener(null);
							button.setEnabled(true);
							button.setOnClickListener(this);
						} else {
							button.setOnClickListener(null);
							button.setEnabled(false);
							button.setOnClickListener(this);
						}
					}
					
					if(v == btn2D) {
						flatViewerClick(v);
					}
				}
			});
		}
		
		seekBar = (SeekBar) findViewById(R.viewer.seekbar);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		
		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (DicomImage image : dicom.getImages()) {
			bitmaps.add(image.getBitmap());
		}
		
		PhotoCube cube = new PhotoCube(this, dicom, bitmaps);
		Square square = new Square(dicom, bitmaps);
		
		renderer = new ViewerRenderer(square, cube, ViewerActivity.this);
		surfaceView.setRenderer(renderer);
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
	
	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Camera camera = renderer.getCamera();
			camera.setEyeZ(seekBar.getProgress());
			renderer.setCamera(camera);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
		}
	};

}