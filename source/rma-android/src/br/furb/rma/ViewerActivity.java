package br.furb.rma;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import br.furb.rma.models.Dicom;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.AxialContainer;
import br.furb.rma.view.CoronalContainer;
import br.furb.rma.view.SagitalContainer;

public class ViewerActivity extends Activity {

	private Dicom dicom;
	
	private LinearLayout sagitalView;
	private SagitalContainer sagitalContainer;
	private LinearLayout axialView;
	private AxialContainer axialContainer;
	private LinearLayout coronalView;
	private CoronalContainer coronalContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		try {
			dicom = readDicom();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		sagitalView = (LinearLayout) findViewById(R.viewer.sagital_container);
		sagitalContainer = new SagitalContainer(this, dicom);
		sagitalView.addView(sagitalContainer);
		
		axialView = (LinearLayout) findViewById(R.viewer.axial_container);
		axialContainer = new AxialContainer(this, dicom);
		axialView.addView(axialContainer);
		
		coronalView = (LinearLayout) findViewById(R.viewer.coronal_container);
		coronalContainer = new CoronalContainer(this, dicom);
		coronalView.addView(coronalContainer);
	}
	
	private Dicom readDicom() throws Exception {
		if(DicomReader.getLastDicomReaded() != null) {
			return DicomReader.getLastDicomReaded();
		}
		String dirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		final DicomReader reader = new DicomReader(new File(dirName));
		
		return reader.read();
	}

}
