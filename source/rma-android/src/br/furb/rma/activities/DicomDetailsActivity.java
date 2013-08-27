package br.furb.rma.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import br.furb.rma.R;
import br.furb.rma.adapters.DicomDetailAdapter;
import br.furb.rma.models.DicomPatient;
import br.furb.rma.models.DicomStudy;
import br.furb.rma.models.Property;

public class DicomDetailsActivity extends Activity {

	private DicomPatient patient;
	private DicomStudy study;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dicom_details_activity);
		
		patient = (DicomPatient) getIntent().getExtras().getSerializable("patient");
		study = (DicomStudy) getIntent().getExtras().getSerializable("study");
		
		ListView listView = (ListView) findViewById(R.dicom_details.list);
		DicomDetailAdapter adapter = new DicomDetailAdapter(this, createProperties(patient, study));
		listView.setAdapter(adapter);
	}

	private List<Property> createProperties(DicomPatient patient, DicomStudy study) {
		List<Property> properties = new ArrayList<Property>();
		
		if(patient.getName() != null) {
			properties.add(new Property(getString(R.string.dicom_patient_name), patient.getName()));
		}
		
		if(patient.getGender() != null) {
			properties.add(new Property(getString(R.string.dicom_patient_gender), patient.getGender()));
		}
		
		return properties;
	}

}
