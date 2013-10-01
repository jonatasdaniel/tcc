package br.furb.rma.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import br.furb.rma.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/joelho_dalton/DICOMDIR";
		((TextView) findViewById(R.main.text_view)).setText(path);
		
		//startActivity(new Intent(this, DicomFilesActivity.class));
	}
	
	public void btnClick(View v) {
		startActivity(new Intent(this, ViewerActivity.class));
	}

}