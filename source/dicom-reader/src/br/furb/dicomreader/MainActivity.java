package br.furb.dicomreader;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import br.furb.dicomreader.reader.DicomFileReader;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/imagem.dcm";
		String[] list = new File(fileName).getParentFile().list();
		for (String f : list) {
			System.out.println(f);
		}
		DicomFileReader reader = new DicomFileReader(fileName);
		try {
			reader.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
