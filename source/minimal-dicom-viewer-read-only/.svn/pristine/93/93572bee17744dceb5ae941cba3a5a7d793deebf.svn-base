/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <FileDialog.java> is part of Minimal Dicom Viewer.
 *
 * Minimal Dicom Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minimal Dicom Viewer is distributed as Open Source Software ( OSS )
 * and comes WITHOUT ANY WARRANTY and even with no IMPLIED WARRANTIES OF MERCHANTABILITY,
 * OF SATISFACTORY QUALITY, AND OF FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License ( GPLv3 ) for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Minimal Dicom Viewer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Released date: 02-12-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FileDialog extends ListActivity {

	private static final String ITEM_KEY = "key";
	private static final String ITEM_IMAGE = "image";
	private static final String ROOT = "/";

	public static final String START_PATH = "START_PATH";
	public static final String RESULT_PATH = "RESULT_PATH";
	public static final String SELECTION_MODE = "SELECTION_MODE";
	public static final String NO_FILE_SELECTED = "||||";

	private List<String> path = null;
	private TextView myPath;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;

	private LinearLayout layoutSelect;
	private LinearLayout layoutCreate;
	private InputMethodManager inputManager;
	private String parentPath;
	private String currentPath = ROOT;
	
	public static final int MODE_CREATE = 0;

	public static final int MODE_OPEN = 1;

	private int selectionMode = MODE_CREATE;

	private File selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);
		mFileName = (EditText) findViewById(R.id.fdEditTextFile);

		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		selectButton = (Button) findViewById(R.id.fdButtonSelect);
		selectButton.setEnabled(false);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					exchangeFile(selectedFile.getPath());
				}
			}
		});

		final Button newButton = (Button) findViewById(R.id.fdButtonNew);
		newButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setCreateVisible(v);

				mFileName.setText("");
				mFileName.requestFocus();
			}
		});
		
		selectionMode = getIntent().getIntExtra(SELECTION_MODE,
				MODE_CREATE);
		if (selectionMode == MODE_OPEN) {
			newButton.setEnabled(false);
		}

		layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		layoutCreate.setVisibility(View.GONE);

		final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSelectVisible(v);
			}

		});
		final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mFileName.getText().length() > 0) {
					getIntent().putExtra(RESULT_PATH,
							currentPath + "/" + mFileName.getText());
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		String startPath = getIntent().getStringExtra(START_PATH);
		if (startPath != null) {
			getDir(startPath);
		} else {
			getDir(ROOT);
		}
		
		// set the default location values
		((Button) findViewById(R.id.fdButtonCancel)).setText(Messages.getLabel(Messages.BUTTON_CANCEL, Messages.Language));
		((Button) findViewById(R.id.fdButtonCreate)).setText(Messages.getLabel(Messages.BUTTON_CREATE, Messages.Language));
		((Button) findViewById(R.id.fdButtonSelect)).setText(Messages.getLabel(Messages.BUTTON_SELECT, Messages.Language));
		((Button) findViewById(R.id.fdButtonNew)).setText(Messages.getLabel(Messages.BUTTON_NEW, Messages.Language));
		((Button) findViewById(R.id.fdButtonCancel)).setText(Messages.getLabel(Messages.BUTTON_CANCEL, Messages.Language));
		((TextView) findViewById(R.id.textViewFilename)).setText(Messages.getLabel(Messages.LABEL_FILE_NAME, Messages.Language)+ ":");
		
	}

	private void getDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}
		myPath.setText(Messages.getLabel(Messages.LABEL_LOCATION, Messages.Language) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.folder);
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				filesMap.put(file.getName(), file.getName());
				filesPathMap.put(file.getName(), file.getPath());
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList,
				//R.layout.file_dialog_row,
				R.layout.rowlayout,
				new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
						R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) 
		{
			boolean hasNoExtension = file.lastIndexOf(".") == -1 ? true : false;
			
			if(hasNoExtension || file.toLowerCase().endsWith(".dcm"))
			{
				addItem(file, R.drawable.file_dicom);
			}
			
			else if(file.toLowerCase().endsWith(".jpeg") || file.toLowerCase().endsWith(".jpg"))
			{
				addItem(file, R.drawable.file_jpeg);
			}
			else addItem(file, R.drawable.file_generic);
		}

		fileList.notifyDataSetChanged();

		setListAdapter(fileList);

	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		File file = new File(path.get(position));

		setSelectVisible(v);

		if (file.isDirectory()) {
			selectButton.setEnabled(false);
			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
			} else {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.icon)
						.setTitle(
								"[" + file.getName() + "] "
										+ getText(R.string.cant_read_folder))
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
			}
		} else {
			selectedFile = file;
			v.setSelected(true);
			selectButton.setEnabled(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			selectButton.setEnabled(false);

			if (layoutCreate.getVisibility() == View.VISIBLE) {
				layoutCreate.setVisibility(View.GONE);
				layoutSelect.setVisibility(View.VISIBLE);
			} else {
				if (!currentPath.equals(ROOT)) {
					getDir(parentPath);
				} else {
					return super.onKeyDown(keyCode, event);
				}
			}

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void setCreateVisible(View v) {
		layoutCreate.setVisibility(View.VISIBLE);
		layoutSelect.setVisibility(View.GONE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}

	private void setSelectVisible(View v) {
		layoutCreate.setVisibility(View.GONE);
		layoutSelect.setVisibility(View.VISIBLE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}
	
	
	boolean result = false;
	
	
	
	private void exchangeFile(String filename)
	{
		result = false;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(Messages.getLabel(Messages.OVERWRITE_EXISTING_FILE_DIALOG_TEXT, Messages.Language)+
				"\n" + filename + "\n" + Messages.getLabel(Messages.OVERWRITE_EXISTING_FILE_DIALOG_TEXT_2, Messages.Language))
				.setTitle(Messages.getLabel(Messages.OVERWRITE_EXISTING_FILE_DIALOG_HEADER, Messages.Language))
				.setCancelable(false)
				.setPositiveButton(Messages.getLabel(Messages.BUTTON_YES, Messages.Language), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int id) 
					{
						getIntent().putExtra(RESULT_PATH, selectedFile.getPath());
						setResult(RESULT_OK, getIntent());
						finish();
					}
				})
				.setNegativeButton(Messages.getLabel(Messages.BUTTON_NO, Messages.Language), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int id) 
					{
						getIntent().putExtra(RESULT_PATH, NO_FILE_SELECTED);
						setResult(RESULT_OK, getIntent());
						finish();
					}
				})
				.setNeutralButton(Messages.getLabel(Messages.BUTTON_CANCEL, Messages.Language), new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog, int id) 
					{
						// 
					}
				});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}