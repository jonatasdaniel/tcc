package br.furb.rma.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import br.furb.rma.R;
import br.furb.rma.adapters.DicomFileAdapter;
import br.furb.rma.models.DicomFile;

public class DicomFilesActivity extends Activity {

	private ListView listView;
	private DicomFileAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dicom_files_activity);
		
		listView = (ListView) findViewById(R.files.list);
		
		loadFiles();
	}

	private void loadFiles() {
		List<DicomFile> files = new ArrayList<DicomFile>();
		for (int i = 0; i < 10; i++) {
			DicomFile f = new DicomFile("Arquivo " + i, null);
			files.add(f);
		}
		
		adapter = new DicomFileAdapter(this, files);
		listView.setAdapter(adapter);
	}

}
