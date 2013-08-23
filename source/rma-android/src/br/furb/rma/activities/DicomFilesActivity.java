package br.furb.rma.activities;

import java.io.File;
import java.io.FileFilter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import br.furb.rma.R;
import br.furb.rma.adapters.DicomFileAdapter;
import br.furb.rma.models.DicomFile;

public class DicomFilesActivity extends Activity {

	public final static int ADD_ITEM = 0;
	
	private ListView listView;
	private DicomFileAdapter adapter;
	private boolean loadFiles = false;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dicom_files_activity);
		
		listView = (ListView) findViewById(R.files.list);
		
		init();
		
		loadFiles();
	}

	private void init() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				if(msg.what == ADD_ITEM) {
					adapter.addItem((DicomFile) msg.obj);
				}
			}
		};
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				loadFiles = false;
			}
		});
	}

	private void loadFiles() {
		loadFiles = true;
		
		final File sdcard = Environment.getExternalStorageDirectory();
		final String[] extensions = {"dcm"};
		final FileFilter filter = new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				if(extensions.length > 0) {
					for (String extension : extensions) {
						if(pathname.getName().endsWith(".".concat(extension))) {
							return true;
						}
					}
					return false;
				} else {
					return true;
				}
			}
		};
		
		adapter = new DicomFileAdapter(this);
		listView.setAdapter(adapter);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				File[] files = sdcard.listFiles(filter);
				for (File f : files) {
					if(!loadFiles) {
						break;
					}
					DicomFile dicomFile = new DicomFile(f.getName(), f);
					Message msg = new Message();
					msg.what = ADD_ITEM;
					msg.obj = dicomFile;
					handler.sendMessage(msg);
				}
				
				loadFiles = false;
			}
		}).start();
	}

}
