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
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.models.Camera;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.Square;

public class ViewerActivity extends Activity {

	private final static int FLAT_VIEWER = 0;
	
	private GLSurfaceView surfaceView;
	private Button btn2D;
	private TextView tvAngle;
	private SeekBar seekBar;
	
	private float angle = 120;
	private float radius = 3;
	
	private Camera camera;
	
	private Dicom dicom;
	private ViewerRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		camera = new Camera();
		camera.setEyeX(retornaX(angle, radius));
		camera.setEyeZ(retornaZ(angle, radius));
		
		//String dirName = getIntent().getExtras().getString("dir") + "/DICOMDIR";
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		//String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_jonny/dicomdir";
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
			dicom = reader.maxImages(5).read();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		btn2D = (Button) findViewById(R.viewer.btn_2d);
		btn2D.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flatViewerClick(v);
			}
		});
		
		tvAngle = (TextView) findViewById(R.viewer.angle);
		tvAngle.setText(angle + "º");
		
		seekBar = (SeekBar) findViewById(R.viewer.seekbar);
		seekBar.setMax(360);
		seekBar.setProgress((int) angle);
		seekBar.setOnSeekBarChangeListener(seekBarListener);
		
		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (DicomImage image : dicom.getImages()) {
			bitmaps.add(image.getBitmap());
		}
		
		Square square = new Square(bitmaps);
		
		renderer = new ViewerRenderer(square, camera);
		surfaceView.setRenderer(renderer);
	}

	public void flatViewerClick(View view) {
		Intent intent = new Intent(this, FlatViewerActivity.class);
		startActivityForResult(intent, FLAT_VIEWER);
	}
	
	public float retornaX(float angle, float radius) {
		return (float) (radius * Math.cos(Math.PI * angle / 180.0));
	}

	public float retornaZ(float angle, float radius) {
		return (float) (radius * Math.sin(Math.PI * angle / 180.0));
	}
	
	private SeekBar.OnSeekBarChangeListener seekBarListener = new SeekBar.OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			Camera camera = renderer.getCamera();
			int progress = seekBar.getProgress();
			angle = progress;
			
			camera.setEyeX(retornaX(angle, radius));
			camera.setEyeZ(retornaZ(angle, radius));
			renderer.setCamera(camera);
			tvAngle.setText(angle + "º");
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == FLAT_VIEWER) {
			btn2D.setEnabled(true);
		}
	};

}