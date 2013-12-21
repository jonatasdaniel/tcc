package br.furb.rma.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import br.furb.rma.R;
import br.furb.rma.activities.VolumetricViewerActivity;
import br.furb.rma.models.Dicom;

public class VolumetricContainer extends Container {

	private Button btnVolumetric;
	
	private int sagitalSelectedIndex;
	private int coronalSelectedIndex;
	private int axialSelectedIndex;
	
	private EditText edtEyeX;
	private EditText edtEyeY;
	private EditText edtEyeZ;
	
	public VolumetricContainer(Context context, Dicom dicom) {
		super(context, dicom, R.layout.volumetric_container);
		
		edtEyeX = (EditText) findViewById(R.volumetric.eye_x);
		edtEyeY = (EditText) findViewById(R.volumetric.eye_y);
		edtEyeZ = (EditText) findViewById(R.volumetric.eye_z);
		
		btnVolumetric = (Button) findViewById(R.volumetric.button);
		btnVolumetric.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent it = new Intent(getContext(), VolumetricViewerActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("sagitalSelectedIndex", sagitalSelectedIndex);
				extras.putInt("axialSelectedIndex", axialSelectedIndex);
				extras.putInt("coronalSelectedIndex", coronalSelectedIndex);
				extras.putFloat("eyeX", Float.parseFloat(edtEyeX.getText().toString()));
				extras.putFloat("eyeY", Float.parseFloat(edtEyeY.getText().toString()));
				extras.putFloat("eyeZ", Float.parseFloat(edtEyeZ.getText().toString()));
				it.putExtras(extras);
				
				getContext().startActivity(it);
			}
		});
		
		TextView tvPatientName = (TextView) findViewById(R.volumetric.patient_name);
		tvPatientName.setText("Paciente: " + getDicom().getPatient().getName());
		
		TextView tvQtdeImages = (TextView) findViewById(R.volumetric.image_number);
		tvQtdeImages.setText("Total de imagens: " + getDicom().getImages().size());
	}

	public void setSagitalSelectedIndex(int sagitalSelectedIndex) {
		this.sagitalSelectedIndex = sagitalSelectedIndex;
	}

	public void setCoronalSelectedIndex(int coronalSelectedIndex) {
		this.coronalSelectedIndex = coronalSelectedIndex;
	}

	public void setAxialSelectedIndex(int axialSelectedIndex) {
		this.axialSelectedIndex = axialSelectedIndex;
	}
	
}