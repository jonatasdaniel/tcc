package br.furb.rma;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;
import br.furb.rma.activities.ViewerRenderer;
import br.furb.rma.models.Dicom;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.Square;

public class FlatViewerActivity extends Activity {

	private GLSurfaceView surfaceView;
	private SeekBar seekBar;
	private Dicom dicom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flat_viewer_activity);
		
		seekBar = (SeekBar) findViewById(R.flat_viewer.seek_bar);
		
		try {
			dicom = DicomReader.getLastDicomReaded();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		seekBar.setMax(dicom.getImages().size());

		surfaceView = (GLSurfaceView) findViewById(R.viewer.gl_surface_view);
		surfaceView.setZOrderOnTop(true);
		surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		surfaceView.getHolder().setFormat(PixelFormat.RGB_888);
		surfaceView.setRenderer(new ViewerRenderer(new Square(dicom),
				FlatViewerActivity.this));
	}
	

}
