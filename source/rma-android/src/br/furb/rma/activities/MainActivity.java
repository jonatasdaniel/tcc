package br.furb.rma.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import br.furb.rma.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startActivity(new Intent(this, DicomFilesActivity.class));
		//startActivity(new Intent(this, ViewerActivity.class));
	}

}