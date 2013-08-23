/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <MDVFileChooser.java> is part of Minimal Dicom Viewer.
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
 * Released date: 13-11-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomInputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MDVFileChooser extends ListActivity {
	
	
	
	private static final String TOP_DIRECTORY = "top_directory";
	
	private File topDirectoryFile;
	
	private static final short MENU_ABOUT = 1;
	
	private int totalFiles = 0;
	
	ArrayAdapter<String> mAdapter;
	
	public static final String SELECTED_LANGUAGE			= "Selected_Language";
	public static final String HIDE_DISCLAIMER_DIALOG		= "Hide_Disclaimer_Dialog";
	int selectedLanguage = -1;
	public static boolean bHideDisclaimerDialog = false;
	
	
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		// Set the content view
		MinimalDicomViewer.iContrast = 128;
		MinimalDicomViewer.iBrightness = 128;
		
		SharedPreferences settings 	= getSharedPreferences(MinimalDicomViewer.PREFERENCES_NAME, 0);
		bHideDisclaimerDialog 		= settings.getBoolean(HIDE_DISCLAIMER_DIALOG, false);
		
		selectedLanguage = settings.getInt(SELECTED_LANGUAGE, -1);
		if(selectedLanguage == -1)
		{
			displayLanguage();
		}

		// Check if the external storage is available
		if (ExternalDevice.isExternalDeviceAvailable()) {

			if (savedInstanceState != null) 
			{
				String topDirectoryString = savedInstanceState.getString(TOP_DIRECTORY);
				topDirectoryFile = (topDirectoryString == null) ? Environment.getExternalStorageDirectory(): new File(savedInstanceState.getString("top_directory"));
			} 
			else 
			{
				// Set the top directory
				topDirectoryFile = Environment.getExternalStorageDirectory();
				// Display the disclaimer
				if(selectedLanguage != -1 && !bHideDisclaimerDialog)displayDisclaimer();
			}
		}
	}
	
	
	protected void onResume() {

		// If there is no external storage available, quit the application
		if (!ExternalDevice.isExternalDeviceAvailable()) {

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(Messages.getLabel(Messages.NO_EXTERNAL_DEVICE_FOUND, Messages.Language)+"\n" +
					Messages.getLabel(Messages.APP_WILL_QUIT_NOW, Messages.Language))
					.setTitle(Messages.getLabel(Messages.NO_EXTERNAL_DEVICE_FOUND, Messages.Language))
					.setCancelable(false)
					.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							MDVFileChooser.this.finish();
						}
					});

			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		} 
		else
		{
			fill();
		}

		super.onResume();
	}
	
	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		super.onListItemClick(l, v, position, id);
		String itemName = mAdapter.getItem(position);
		// If it is a directory, display its content
		if (itemName.charAt(0) == '/')
		{
			 topDirectoryFile = new File(topDirectoryFile.getPath() + itemName);
			 fill();
		// If itemNam = ".." go to parent directory
		} 
		else if (itemName.equals(".."))
		{
			topDirectoryFile = topDirectoryFile.getParentFile();
			fill();
		// If it is a file.
		} 
		else
		{
			try 
			{
				// Create a DICOMReader to parse meta information
				BasicDicomObject bdo = new BasicDicomObject();
		    	DicomInputStream dis = new DicomInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(topDirectoryFile.getPath() + "/" + itemName)));
		    	dis.readDicomObject(bdo, -1);
		    	dis.close();
		    	String strMetaInformation = bdo.getString(0x00020002); // MediaStorageSOPClassUID
				
				if(strMetaInformation != null && strMetaInformation.equals("1.2.840.10008.1.3.10")) {
					
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(Messages.getLabel(Messages.MEDIA_STORAGE_DEVICE_NOT_SUPPORTED, Messages.Language))
						   .setTitle(Messages.getLabel(Messages.ERROR_LOADING_FILE, Messages.Language) + itemName)
					       .setCancelable(false)
					       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                // Do nothing
					           }
					       });
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
					
				} 
				else 
				{
					// Open the dicom Viewer
					Intent intent = new Intent(this, MinimalDicomViewer.class);
					intent.putExtra("DicomFileName", topDirectoryFile.getPath() + "/" + itemName);
					intent.putExtra("FileCount", totalFiles);
					startActivity(intent);
				}
			} 
			catch (Exception ex) 
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(Messages.getLabel(Messages.ERROR_OPENING_FILE, Messages.Language) + itemName
						+ ". \n" + ex.getMessage())
					   .setTitle(Messages.getLabel(Messages.ERROR_OPENING_FILE, Messages.Language) + itemName)
				       .setCancelable(false)
				       .setPositiveButton("Close", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                // Do nothing
				           }
				       });
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
				
			}
		}
	}
	
	protected void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);
		outState.putString(TOP_DIRECTORY, topDirectoryFile.getAbsolutePath());
	}
	
	protected Dialog onCreateDialog(int id) 
	{
		return super.onCreateDialog(id);
	}
	
	
	public void onBackPressed() 
	{
		// If the directory is the external storage directory or there is no parent,
		// super.onBackPressed(). Else go to parent directory.
		if (topDirectoryFile.getParent() == null || topDirectoryFile.equals(Environment.getExternalStorageDirectory()))
		{
			super.onBackPressed();
		} 
		else 
		{
			topDirectoryFile = topDirectoryFile.getParentFile();
			fill();
		}
	}
	
	
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ABOUT, 1, Messages.getLabel(Messages.MENU_ABOUT, Messages.Language));
		return true;
	}
	
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case MENU_ABOUT:
			Dialog dialog = new Dialog(this);
        	dialog.setContentView(R.layout.dialog_about);
        	dialog.setTitle(Messages.getLabel(Messages.ABOUT_HEADER, Messages.Language));
        	TextView text = (TextView)dialog.findViewById(R.id.AboutText);
        	text.setText(Messages.getAboutMessage(Messages.Language));
        	dialog.show();
			return true;
		
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	
	private void fill()
	{
		// If the external storage is not available, we cannot
		// fill the view
		if (!ExternalDevice.isExternalDeviceAvailable())return;

		// Get the children directories and the files of top directories 
		File[] childrenFiles = topDirectoryFile.listFiles();

		// Declare the directories and the files array
		List<String> directoryList = new ArrayList<String>();
		List<String> fileList = new ArrayList<String>();

		// Loop on all children
		for (File child: childrenFiles) 
		{
			// If it is a directory
			if (child.isDirectory()) 
			{
				String directoryName = child.getName();
				if (directoryName.charAt(0) != '.')
					directoryList.add("/" + child.getName());
			} 
			else 
			{
				String[] fileName = child.getName().split("\\.");
				if (!child.isHidden()) 
				{
					if (fileName.length > 1) 
					{
						// dicom files have no extension or dcm extension
						if (fileName[fileName.length-1].equalsIgnoreCase("dcm"))
						{
							fileList.add(child.getName());
						}
					} 
					else 
					{
						fileList.add(child.getName());
					}
				}
			}
		}
		// Sort both list
		Collections.sort(directoryList, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(fileList, String.CASE_INSENSITIVE_ORDER);
		
		totalFiles = fileList.size();
		// Output list will be files before directories
		// then we add the directoryList to the fileList
		fileList.addAll(directoryList);
		if (!topDirectoryFile.equals(Environment.getExternalStorageDirectory()))fileList.add(0, "..");
		mAdapter = new ArrayAdapter<String>(this, R.layout.file_chooser_item, R.id.fileName, fileList);
		setListAdapter(mAdapter);
	}
	
	
	private void displayDisclaimer() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(Messages.getDisclaimer(Messages.Language))
			   .setTitle(Messages.getLabel(Messages.LABEL_DISCLAIMER, Messages.Language))
		       .setCancelable(false)
		       .setPositiveButton(Messages.getLabel(Messages.LABEL_DECLINE, Messages.Language), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   MDVFileChooser.this.finish();
		           }
		       })
		       .setNegativeButton(Messages.getLabel(Messages.LABEL_ACCEPT, Messages.Language), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   setContentView(R.layout.file_chooser_list);
		           }
		       });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	/*
	 * display language selection dialog
	 */
	public void displayLanguage() 
	{
		String title = "no Title configured";
		int preSelectedItem = 0;

		if(Locale.getDefault().getLanguage().toLowerCase().startsWith("en"))
		{
			preSelectedItem = 0;
		}
		else
		{
			preSelectedItem = 1;
		}
		Messages.Language = preSelectedItem;

		title = Messages.getLabel(Messages.CONFIGURE_LANGUAGE, Messages.Language);
		final String items[] = {"English","Deutsch"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, preSelectedItem,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// onClick Action
				Messages.Language = whichButton;
			}
		})
		.setPositiveButton(Messages.getLabel(Messages.BUTTON_OK, Messages.Language), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// on OK button action
				storeLanguageData();
				// display disclaimer dialog
				displayDisclaimer();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	
	private void storeLanguageData()
    {
    	SharedPreferences settings = getSharedPreferences(MinimalDicomViewer.PREFERENCES_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();	    
	    editor.putInt(SELECTED_LANGUAGE, Messages.Language );
	    editor.commit();
    }
}
