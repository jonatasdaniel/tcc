package br.furb.rma.activities;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import br.furb.rma.R;
import br.furb.rma.models.Dicom;
import br.furb.rma.reader.DicomReader;
import br.furb.rma.view.AxialContainer;
import br.furb.rma.view.ContainerListener;
import br.furb.rma.view.CoronalContainer;
import br.furb.rma.view.SagitalContainer;
import br.furb.rma.view.VolumetricContainer;

public class ViewerActivity extends Activity {

	private Dicom dicom;
	
	private LinearLayout sagitalView;
	private SagitalContainer sagitalContainer;
	private LinearLayout axialView;
	private AxialContainer axialContainer;
	private LinearLayout coronalView;
	private CoronalContainer coronalContainer;
	private LinearLayout volumetricView;
	private VolumetricContainer volumetricContainer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
		
		String path = null;
		if(getIntent().getExtras() != null && getIntent().getExtras().containsKey("dir")) {
			path = getIntent().getExtras().getString("dir") + "/DICOMDIR";
		} else {
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		}
		
		try {
			dicom = readDicom(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		volumetricView = (LinearLayout) findViewById(R.viewer.volumetric_container);
		volumetricContainer = new VolumetricContainer(this, dicom);
		volumetricView.addView(volumetricContainer);
		
		sagitalView = (LinearLayout) findViewById(R.viewer.sagital_container);
		sagitalContainer = new SagitalContainer(this, dicom);
		sagitalContainer.setListener(new ContainerListener() {
			
			@Override
			public void onImageChanged(int currentImageIndex) {
				volumetricContainer.setSagitalSelectedIndex(currentImageIndex);
			}
		});
		sagitalView.addView(sagitalContainer);
		
		axialView = (LinearLayout) findViewById(R.viewer.axial_container);
		axialContainer = new AxialContainer(this, dicom);
		axialContainer.setListener(new ContainerListener() {
			
			@Override
			public void onImageChanged(int currentImageIndex) {
				volumetricContainer.setAxialSelectedIndex(currentImageIndex);
			}
		});
		axialView.addView(axialContainer);
		
		coronalView = (LinearLayout) findViewById(R.viewer.coronal_container);
		coronalContainer = new CoronalContainer(this, dicom);
		coronalContainer.setListener(new ContainerListener() {
			
			@Override
			public void onImageChanged(int currentImageIndex) {
				volumetricContainer.setCoronalSelectedIndex(currentImageIndex);
			}
		});
		coronalView.addView(coronalContainer);
	}

	private Dicom readDicom(String path) throws Exception {
		final DicomReader reader = new DicomReader(new File(path));
		
		return reader.maxImages(100).read();
	}

}
