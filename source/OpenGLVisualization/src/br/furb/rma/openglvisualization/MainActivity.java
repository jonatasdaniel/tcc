package br.furb.rma.openglvisualization;

import java.io.File;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.GestureDetector;
import android.widget.ImageView;
import br.furb.rma.models.Dicom;
import br.furb.rma.reader.DicomReader;

public class MainActivity extends Activity {

	private GLSurfaceView surfaceView;
	private ImageView imageView;
//	private ProgressDialog progressDialog;
	private Dicom dicom;
	
//	private Handler handler;
	
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		handler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				if(msg.what == 0) {
//					progressDialog.dismiss();
//					surfaceView.setRenderer(new GLRenderer(new Square(dicom), MainActivity.this));
//					//imageView.setImageBitmap(dicom.getImages().get(0).createBitmap());
//				} else if(msg.what == 1) {
//					progressDialog.setMessage(msg.obj.toString());
//				}
//				super.handleMessage(msg);
//			}
//		};
		
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		final DicomReader reader = new DicomReader(new File(dirName)).maxImages(4);
		
		try {
			dicom = reader.read();
		} catch(Exception e) {
			e.printStackTrace();
		}

		surfaceView = (GLSurfaceView) findViewById(R.main.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
		surfaceView.setRenderer(new GLRenderer(new Square(dicom),
				MainActivity.this));
		//imageView = (ImageView) findViewById(R.main.image_view);
		
		
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
		
//		gestureDetector = new GestureDetector(new MyGestureDetector(imageView, dicom));
//		imageView.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				return gestureDetector.onTouchEvent(event);
//			}
//		});
		
//		progressDialog = ProgressDialog.show(this, "Carregando...", "Aguarde, carregando arquivo DICOM!", true);
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					dicom = reader.read();
//				} catch(Exception e) {
//					e.printStackTrace();
//				}
//				Message msg = new Message();
//				msg.what = 0;
//				handler.sendMessage(msg);
//			}
//		}).start();
	}

}
