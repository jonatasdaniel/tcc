package br.furb.rma.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import br.furb.rma.R;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;

public class ViewerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer_activity);
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