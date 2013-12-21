package br.furb.rma.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import br.furb.rma.R;
import br.furb.rma.models.Camera;
import br.furb.rma.models.Dicom;
import br.furb.rma.models.DicomImage;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.Square;

public class VolumetricViewerActivity extends Activity {
	
	private GLSurfaceView surfaceView;
	
	private Camera camera;
	
	private Dicom dicom;
	private VolumetricViewerRenderer renderer;
	
	private int sagitalSelectedIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.volumetric_viewer_activity);
		
		Bundle extras = getIntent().getExtras();
		float eyeX = extras.getFloat("eyeX");
		float eyeY = extras.getFloat("eyeY");
		float eyeZ = extras.getFloat("eyeZ");
		
		camera = new Camera();
		camera.setEyeX(eyeX);
		camera.setEyeY(eyeY);
		camera.setEyeZ(eyeZ);
		
		try {
			dicom = DicomReader.getLastDicomReaded();
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		
		this.sagitalSelectedIndex = getIntent().getExtras().getInt("sagitalSelectedIndex");
		List<DicomImage> images = dicom.getImages();
		
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		for (DicomImage image : images) {
			int[][] matrix = image.getMatrix();
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++) {
					
				}
			}
			
			
			//image.setPixelData(ha);
			
			bitmaps.add(image.getBitmap());
		}
		
		Square square = new Square(bitmaps);
		
		renderer = new VolumetricViewerRenderer(square, dicom, camera);
		surfaceView.setRenderer(renderer);
	}

}