/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <MinimalDicomViewer.java> is part of Minimal Dicom Viewer.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.dcm4che2.data.VRMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MinimalDicomViewer extends Activity 
{

	public static final String FILE_NAME 					= "file_name";
	public static final String SEEKBAR_VISIBILITY 			= "SeekBar_Visibility";
	public static final String DISCLAIMER_ACCEPTED 			= "Disclaimer_Accepted";
	public static final String PATIENTDATA_VISIBILITY 		= "PatientData_Visibility";
	
	
	public static final short OUT_OF_MEMORY = 0;
	
	/**
	 * The thread is started.
	 */
	public static final short STARTED = 1;
	
	/**
	 * The thread is finished.
	 */
	public static final short FINISHED = 2;
	
	/**
	 * The thread progression update.
	 */
	public static final short PROGRESSION_UPDATE = 3;
	
	/**
	 * An error occurred while the thread running that cannot
	 * be managed.
	 */
	public static final short UNCATCHABLE_ERROR_OCCURRED = 4;
	
	
	
	private DicomImageView imageView;
	private DicomFileLoader dicomFileLoader;
	private File[] fileArray = null;
	private int currentFileIndex = -1;
	private String actualFileName = "";
	
	private boolean isInitialized = false;
	
	private static final short MENU_INVERT = 3;
	private static final short MENU_ABOUT = 4;
	private static final short MENU_SWITCH_SEEKBAR_VISIBILITY = 2;
	private static final short MENU_CONFIGURE_DISCLAIMER_DIALOG = 1;
	private static final short MENU_CONFIGURE_LANGUAGE = 0;
	private static final short MENU_EXPORT_TO_JPEG = 5;
	private static final short MENU_CONFIGURE_PATIENT_DATA = 6;
	private static final short MENU_CONFIGURE_APP = 7;
	
	
	private static final short PROGRESS_IMAGE_LOAD = 0;
	private ProgressDialog imageLoadingDialog;
	
	private SeekBar brightnessSeekBar;
	private TextView brightnessValue;
	private TextView brightnessLabel;
	
	private SeekBar contrastSeekBar;
	private TextView contrastValue;
	private TextView contrastLabel;
	
	private boolean allowEvaluateProgressValue = true;
	private boolean seekBarVisibility = true;
	private boolean patientDataVisibility = false;
	
	public static final String PREFERENCES_NAME = "MDVPreferencesFile";
	Context context;
	
	
	
	public static int iBrightness = 128;
	public static int iContrast = 128;
	
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        context = this;
        VRMap.getVRMap();
        VRMap.loadVRMap( "org/dcm4che2/data/VRMap.ser" );
        setContentView(R.layout.main);
        imageView = (DicomImageView)findViewById(R.id.imageView);
        brightnessSeekBar = (SeekBar)findViewById(R.id.brightnessSeekBar);
        brightnessValue = (TextView)findViewById(R.id.brightnessValue);
        brightnessLabel = (TextView)findViewById(R.id.brightnessLabel);
        brightnessLabel.setText(Messages.getLabel(Messages.LABEL_BRIGHTNESS, Messages.Language));
        contrastLabel = (TextView)findViewById(R.id.contrastLabel);
        contrastLabel.setText(Messages.getLabel(Messages.LABEL_CONTRAST, Messages.Language));
        
        
        contrastSeekBar = (SeekBar)findViewById(R.id.contrastSeekBar);
        contrastValue = (TextView)findViewById(R.id.contrastValue);
        contrastLabel = (TextView)findViewById(R.id.contrastLabel);
        
        
        // Set the seek bar change index listener
        brightnessSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekbar) {}
			public void onStartTrackingTouch(SeekBar seekbar) {}
			public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				brightnessValue.setText("" + 100*progress/255);
				// on creation image on imageView may be null
				if(allowEvaluateProgressValue && imageView.getImage() != null)
				{
					ImageGray16Bit imageGray16Bit = imageView.getImage();
					int imageData[] = imageGray16Bit.getOriginalImageData();
					if(imageData == null)
					{
						return;
					}
					iBrightness = progress;
					imageData = DicomHelper.setBrightnessAndContrast(imageData, iBrightness, iContrast);
					imageGray16Bit.setImageData(imageData);
					imageView.setImage(imageGray16Bit);
					imageView.draw();
				}
			}
		});
        brightnessSeekBar.setMax(255);
        brightnessSeekBar.setProgress(iBrightness);
        
        contrastSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			public void onStopTrackingTouch(SeekBar seekbar) {}
			public void onStartTrackingTouch(SeekBar seekbar) {}
			
			public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				contrastValue.setText("" + 100*progress/255);
				// on creation image on imageView may be null
				if(allowEvaluateProgressValue && imageView.getImage() != null)
				{
					ImageGray16Bit imageGray16Bit = imageView.getImage();
					int imageData[] = imageGray16Bit.getOriginalImageData();
					if(imageData == null)
					{
						return;
					}
					iContrast = progress;
					imageData = DicomHelper.setBrightnessAndContrast(imageData, iBrightness, iContrast);
					imageGray16Bit.setImageData(imageData);
					imageView.setImage(imageGray16Bit);
					imageView.draw();
				}
			}
		});
        contrastSeekBar.setMax(255);
        contrastSeekBar.setProgress(iContrast);
        
        ((TextView)findViewById(R.id.PatientNameLabel)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.PatientNameValue)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.PatientPrenameLabel)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.PatientPrenameValue)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.PatientBirthLabel)).setVisibility(View.INVISIBLE);
        ((TextView)findViewById(R.id.PatientBirthValue)).setVisibility(View.INVISIBLE);
        
        ((TextView)findViewById(R.id.PatientNameLabel)).setText(Messages.getLabel(Messages.PATIENT_NAME_LABEL, Messages.Language));
        ((TextView)findViewById(R.id.PatientPrenameLabel)).setText(Messages.getLabel(Messages.PATIENT_PRENAME_LABEL, Messages.Language));
        ((TextView)findViewById(R.id.PatientBirthLabel)).setText(Messages.getLabel(Messages.PATIENT_BIRTHDATE_LABEL, Messages.Language));
        
        String fileName = null;        
        
        SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
        if(settings != null)
        {
        	boolean value = settings.getBoolean(SEEKBAR_VISIBILITY, true);
        	if(!value)
        	{
        		brightnessSeekBar.setVisibility(View.INVISIBLE);
				brightnessLabel.setVisibility(View.INVISIBLE);
				brightnessValue.setVisibility(View.INVISIBLE);
				
				contrastSeekBar.setVisibility(View.INVISIBLE);
				contrastLabel.setVisibility(View.INVISIBLE);
				contrastValue.setVisibility(View.INVISIBLE);
				seekBarVisibility = false;
        	}
        	patientDataVisibility = settings.getBoolean(PATIENTDATA_VISIBILITY, false);
        	if(patientDataVisibility)
        	{
        		((TextView)findViewById(R.id.PatientNameLabel)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.PatientNameValue)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.PatientPrenameLabel)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.PatientPrenameValue)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.PatientBirthLabel)).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.PatientBirthValue)).setVisibility(View.VISIBLE);
        	}
        }
		
		// If the saved instance state is not null get the file name
		if (savedInstanceState != null) 
		{
			fileName = savedInstanceState.getString(FILE_NAME);
		} 
		else // Get the intent
		{
			Intent intent = getIntent();
			if (intent != null) 
			{
				Bundle extras = intent.getExtras();
				fileName = extras == null ? null : extras.getString("DicomFileName");
			}
		}
		if (fileName == null) 
		{
			showExitAlertDialog(Messages.getLabel(Messages.ERROR_LOADING_FILE, Messages.Language),
					Messages.getLabel(Messages.THE_FILE_CANNOT_BE_LOADED, Messages.Language)+"\n\n" +
					Messages.getLabel(Messages.CANNOT_RETRIEVE_NAME, Messages.Language));
		} 
		else 
		{
			// Get the File object for the current file
			File currentFile = new File(fileName);
			
			// Start the loading thread to load the DICOM image
			actualFileName = fileName;
			dicomFileLoader = new DicomFileLoader(loadingHandler, fileName);
			dicomFileLoader.start();
			//busy = true;
			
			
			// Get the files array = get the files contained
			// in the parent of the current file
			fileArray = currentFile.getParentFile().listFiles(new DicomFileFilter());
			
			// Sort the files array
			Arrays.sort(fileArray);
			
			// If the files array is null or its length is less than 1,
			// there is an error because it must at least contain 1 file:
			// the current file
			if (fileArray == null || fileArray.length < 1) 
			{
				showExitAlertDialog(Messages.getLabel(Messages.ERROR_LOADING_FILE, Messages.Language),
						Messages.getLabel(Messages.THE_FILE_CANNOT_BE_LOADED, Messages.Language)+"\n\n" +
						Messages.getLabel(Messages.NO_DICOM_FILES_IN_DIRECTORY, Messages.Language));
			} 
			else 
			{
				// Get the file index in the array
				currentFileIndex = getIndex(currentFile);
				
				// If the current file index is negative
				// or greater or equal to the files array
				// length there is an error
				if (currentFileIndex < 0 || currentFileIndex >= fileArray.length) 
				{
					showExitAlertDialog(Messages.getLabel(Messages.ERROR_LOADING_FILE, Messages.Language),
							Messages.getLabel(Messages.THE_FILE_CANNOT_BE_LOADED, Messages.Language)+"\n\n" +
							Messages.getLabel(Messages.FILE_IS_NOT_IN_DIRECTORY, Messages.Language));
				// Else initialize views and navigation bar
				} 
			}
		}
    }
    
    
    
    private void setFilenameLabel(TextView textView, String text)
    {
    	String toPrint = text.substring(text.lastIndexOf("/") + 1);
    	textView.setTextColor(Color.rgb(255, 0, 0));
    	textView.setText(Messages.getLabel(Messages.FILE, Messages.Language) +": " + toPrint);
    }
    
    
    @Override
	protected void onPause() 
    {
		// We wait until the end of the loading thread
		// before putting the activity in pause mode
		if (dicomFileLoader != null) 
		{
			// Wait until the loading thread die
			while (dicomFileLoader.isAlive()) 
			{
				try 
				{
					synchronized(this) 
					{
						wait(10);
					}
				} 
				catch (InterruptedException e){}
			}
		}
		super.onPause();
		storePreferencesData();
	}
    
    
    
    @Override
	protected void onStop() 
    {
    	super.onStop();
    	storePreferencesData();
    }
    
    
    @Override
	protected void onDestroy() 
    {
		super.onDestroy();
		fileArray = null;
		dicomFileLoader = null;
		
		// Free the drawable callback
		if (imageView != null) 
		{
			Drawable drawable = imageView.getDrawable();
			if (drawable != null)drawable.setCallback(null);
		}
	}
    
    @Override
	protected void onSaveInstanceState(Bundle outState) 
    {
		super.onSaveInstanceState(outState);
		// Save the current file name
		String currentFileName = fileArray[currentFileIndex].getAbsolutePath();
		outState.putString(FILE_NAME, currentFileName);
	}
    
    
    
    @Override
	protected Dialog onCreateDialog(int id) 
    {
		switch(id) 
		{
         // Create image load dialog
        case PROGRESS_IMAGE_LOAD:
        	imageLoadingDialog = new ProgressDialog(this);
        	imageLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	imageLoadingDialog.setMessage(Messages.getLabel(Messages.LOADING_IMAGE, Messages.Language));
        	imageLoadingDialog.setCancelable(false);
        	return imageLoadingDialog;
            
        default:
            return null;
        }
    }
    
    
    @Override
	public void onLowMemory() 
    {
		// Hint the garbage collector
		System.gc();
		// Show the exit alert dialog
		showExitAlertDialog(Messages.getLowMemory(Messages.Language), Messages.getLowMemory(Messages.Language));
		super.onLowMemory();
	}
    
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, MENU_INVERT, MENU_INVERT, Messages.getLabel(Messages.MENU_INVERT_PICTURE, Messages.Language));
		menu.add(1, MENU_ABOUT, MENU_ABOUT, Messages.getLabel(Messages.MENU_ABOUT, Messages.Language));
		menu.add(2, MENU_CONFIGURE_LANGUAGE, MENU_CONFIGURE_LANGUAGE, Messages.getLabel(Messages.CONFIGURE_LANGUAGE, Messages.Language));
		menu.add(3, MENU_EXPORT_TO_JPEG, MENU_EXPORT_TO_JPEG, Messages.getLabel(Messages.MENU_EXPORT_TO_JPEG, Messages.Language));
		menu.add(4, MENU_CONFIGURE_APP, MENU_CONFIGURE_APP, Messages.getLabel(Messages.MENU_CONFIGURE_APP, Messages.Language));
		return true;
    }
    
    
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
    	int visibility;
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
			
		case MENU_INVERT:
			paintInvert();
			imageView.updateMatrix();
			return true;
			
		case MENU_SWITCH_SEEKBAR_VISIBILITY:
			visibility = brightnessSeekBar.getVisibility();
			if(visibility == View.VISIBLE)
			{
				visibility = View.INVISIBLE;
				seekBarVisibility = false;
			}
			else
			{
				visibility = View.VISIBLE;
				seekBarVisibility = true;
			}
			brightnessSeekBar.setVisibility(visibility);
			brightnessLabel.setVisibility(visibility);
			brightnessValue.setVisibility(visibility);
			contrastSeekBar.setVisibility(visibility);
			contrastLabel.setVisibility(visibility);
			contrastValue.setVisibility(visibility);
			
			storePreferencesData();
			return true;
			
		case MENU_CONFIGURE_DISCLAIMER_DIALOG:
			displayConfigureDisclaimer();
			return true;
			
		case MENU_CONFIGURE_LANGUAGE:
			displayLanguage();
			return true;
			
		case MENU_EXPORT_TO_JPEG:
			exportToJpeg();
			return true;
			
		case MENU_CONFIGURE_PATIENT_DATA:
			visibility = ((TextView)findViewById(R.id.PatientNameLabel)).getVisibility();
			if(visibility == View.VISIBLE)
			{
				visibility = View.INVISIBLE;
				patientDataVisibility = false;
			}
			else
			{
				visibility = View.VISIBLE;
				patientDataVisibility = true;
			}
			((TextView)findViewById(R.id.PatientNameLabel)).setVisibility(visibility);
			((TextView)findViewById(R.id.PatientNameValue)).setVisibility(visibility);
			((TextView)findViewById(R.id.PatientPrenameLabel)).setVisibility(visibility);
			((TextView)findViewById(R.id.PatientPrenameValue)).setVisibility(visibility);
			((TextView)findViewById(R.id.PatientBirthLabel)).setVisibility(visibility);
			((TextView)findViewById(R.id.PatientBirthValue)).setVisibility(visibility);
			return true;
			
		case MENU_CONFIGURE_APP:
			showAppConfigureDialog();
			return true;
		
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
    
    
    private boolean afdSeekBarVisibility;
    private boolean afdHideDisclaimerDialog;
    private boolean afdPatientDataVisibility;
    
    
    private void showAppConfigureDialog()
    {
        final boolean[] states = {seekBarVisibility, MDVFileChooser.bHideDisclaimerDialog, patientDataVisibility};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        afdSeekBarVisibility = seekBarVisibility;
        afdHideDisclaimerDialog = MDVFileChooser.bHideDisclaimerDialog;
        afdPatientDataVisibility = patientDataVisibility;
        
        final CharSequence[] items = {
        		Messages.getLabel(Messages.MENU_TOOLBAR_VISIBLITY_ON, Messages.Language), 
        		Messages.getLabel(Messages.MENU_DISCLAIMER_DIALOG_OFF, Messages.Language),
        		Messages.getLabel(Messages.MENU_PATIENT_DATA_ON, Messages.Language)
    	};        
        
        builder.setTitle(Messages.getLabel(Messages.TITLE_CONFIGURE_APP, Messages.Language));
        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
            public void onClick(DialogInterface dialogInterface, int item, boolean state) 
            {
            	if(item == 0)afdSeekBarVisibility = state;
            	else if(item == 1)afdHideDisclaimerDialog = state;
            	else if(item == 2)afdPatientDataVisibility = state;
            }
        });
        builder.setPositiveButton(Messages.getLabel(Messages.BUTTON_OK, Messages.Language), new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int id) 
            {
                seekBarVisibility 						= afdSeekBarVisibility;
                MDVFileChooser.bHideDisclaimerDialog 	= afdHideDisclaimerDialog;
                patientDataVisibility 					= afdPatientDataVisibility;
                
                int visibility = (seekBarVisibility == true) ? View.VISIBLE : View.INVISIBLE;
                brightnessSeekBar.setVisibility(visibility);
    			brightnessLabel.setVisibility(visibility);
    			brightnessValue.setVisibility(visibility);
    			contrastSeekBar.setVisibility(visibility);
    			contrastLabel.setVisibility(visibility);
    			contrastValue.setVisibility(visibility);
    			
    			visibility = (patientDataVisibility == true) ? View.VISIBLE : View.INVISIBLE;
    			
    			((TextView)findViewById(R.id.PatientNameLabel)).setVisibility(visibility);
    			((TextView)findViewById(R.id.PatientNameValue)).setVisibility(visibility);
    			((TextView)findViewById(R.id.PatientPrenameLabel)).setVisibility(visibility);
    			((TextView)findViewById(R.id.PatientPrenameValue)).setVisibility(visibility);
    			((TextView)findViewById(R.id.PatientBirthLabel)).setVisibility(visibility);
    			((TextView)findViewById(R.id.PatientBirthValue)).setVisibility(visibility);
                
                
                // and now store data
                SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
        	    SharedPreferences.Editor editor = settings.edit();	    
                editor.putBoolean(MDVFileChooser.HIDE_DISCLAIMER_DIALOG, MDVFileChooser.bHideDisclaimerDialog );
                editor.putBoolean(SEEKBAR_VISIBILITY, seekBarVisibility );
        	    editor.putBoolean(PATIENTDATA_VISIBILITY, patientDataVisibility );
        	    editor.commit();
            }
        });
        builder.setNegativeButton(Messages.getLabel(Messages.BUTTON_CANCEL, Messages.Language), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                 dialog.cancel();
            }
        });
        builder.create().show();

    }
    
    
    boolean paintInverted = false;
    
    
    private void storePreferencesData()
    {
    	SharedPreferences settings = getSharedPreferences(PREFERENCES_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();	    
	    editor.putBoolean(SEEKBAR_VISIBILITY, seekBarVisibility );
	    editor.putBoolean(PATIENTDATA_VISIBILITY, patientDataVisibility );
	    editor.commit();
    }
    
    
    
    
    
    
    
    String resultPathFromFileDialog = null;
    Intent fileDialogIntent;
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	resultPathFromFileDialog = data.getStringExtra(FileDialog.RESULT_PATH);
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    @Override
	protected void onResume() 
    {
		// If there is no external storage available, quit the application
		if (!ExternalDevice.isExternalDeviceAvailable()) 
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(Messages.getMessageExternalDevice(Messages.Language))
				   .setTitle(Messages.getMessageExternalDeviceHeader(Messages.Language))
			       .setCancelable(false)
			       .setPositiveButton("Exit", new DialogInterface.OnClickListener() 
			       {
			           public void onClick(DialogInterface dialog, int id) 
			           {
			                MinimalDicomViewer.this.finish();
			           }
			       });
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
		super.onResume();
		if(resultPathFromFileDialog != null && (!resultPathFromFileDialog.equals(FileDialog.NO_FILE_SELECTED)))
		{
			if(!resultPathFromFileDialog.toLowerCase().endsWith(".jpg"))resultPathFromFileDialog += ".jpg";
			Toast.makeText(this, Messages.getLabel(Messages.FILE_WRITTEN, Messages.Language) + ":\n" + resultPathFromFileDialog, Toast.LENGTH_SHORT).show();
			exportJpegToFile(resultPathFromFileDialog);
			// prevent from calling more than once
			resultPathFromFileDialog = null;
		}
    }
    
    
    private void exportJpegToFile(String path)
    {
    	ImageGray16Bit imageGray16Bit = imageView.getImage();
    	Bitmap bitmap = Bitmap.createBitmap(imageGray16Bit.getImageData(), imageGray16Bit.getWidth(), imageGray16Bit.getHeight(), Bitmap.Config.ARGB_8888);
    	try
    	{
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		bitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos);
    		File f = new File(path);
    		//new FileOutputStream("sdcard/image1.jpg")
    		f.createNewFile();
    		//write the bytes in file
    		FileOutputStream fo = new FileOutputStream(f);
    		fo.write(baos.toByteArray());
    		fo.flush();
    		fo.close();

    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    }
    
    
    private void exportToJpeg()
    {
    	fileDialogIntent = new Intent(this, FileDialog.class);
    	fileDialogIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    	fileDialogIntent.putExtra(FileDialog.SELECTION_MODE, FileDialog.MODE_CREATE);
    	fileDialogIntent.putExtra(FileDialog.START_PATH, "/sdcard");
		startActivityForResult(fileDialogIntent, 0);
    }
    
    
    
    private void paintInvert()
    {
    	// sometimes this happens on startup and image is not quite loaded
    	// v1.2
    	// 03.01.2012 09:20:21
    	// 1 Berichte/Woche
    	// 1 Berichte
    	// java.lang.NullPointerException
    	// at de.mdv.MinimalDicomViewer.paintInvert(MinimalDicomViewer.java:693)
    	if(imageView.getImage() == null)
    	{
    		return;
    	}
    	allowEvaluateProgressValue = false;
		brightnessSeekBar.setProgress(0);
		ImageGray16Bit imageGray16Bit = imageView.getImage();
		int imageData[] = imageGray16Bit.getOriginalImageData();
		if(imageData == null)
		{
			return;
		}
		imageData = DicomHelper.invertPixels(imageData);
		imageGray16Bit.setImageData(imageData);
		imageGray16Bit.setOriginalImageData(imageData);
		imageView.setImage(imageGray16Bit);
		imageView.draw();
		imageView.paintCachedSize();
		allowEvaluateProgressValue = true;
		if(!paintInverted)paintInverted = true;
		else paintInverted = false;
    }
	

	// Needed to implement the SeekBar.OnSeekBarChangeListener
	public void onStartTrackingTouch(SeekBar seekBar) 
	{
		// nothing to do.
	}

	
	// Needed to implement the SeekBar.OnSeekBarChangeListener
	public void onStopTrackingTouch(SeekBar seekBar) 
	{
		System.gc(); // TODO needed ?
	}
	
	
	/**
	 * Get the index of the file in the files array.
	 * @param file
	 * @return Index of the file in the files array
	 * or -1 if the files is not in the list.
	 */
	private int getIndex(File file) {
		
		if (fileArray == null) 
			throw new NullPointerException("The files array is null.");
		
		for (int i = 0; i < fileArray.length; i++) 
		{
			if (fileArray[i].getName().equals(file.getName()))return i;
		}
		return -1;
	}
	
	
	private void setImage(ImageGray16Bit image) 
	{
		if (image == null)
			throw new NullPointerException("The 16-Bit grayscale image is null");
		
		try 
		{
			// Set the image
			imageView.setImage(image);
			
			// If it is not initialized, set the window width and center
			// as the value set in the LISA 16-Bit grayscale image
			// that comes from the DICOM image file.
			if (!isInitialized) 
			{
				isInitialized = true;
			} 
			imageView.draw();
		} 
		catch (OutOfMemoryError ex) 
		{
			System.gc();
			showExitAlertDialog(Messages.getHeaderOutOfMemoryErrorMessage(Messages.Language),
					Messages.getOutOfMemoryErrorMessage(Messages.Language));
		} 
		catch (ArrayIndexOutOfBoundsException ex) 
		{
			showExitAlertDialog(Messages.getHeaderIndexOutOfBoundsMessage(Messages.Language),
					Messages.getIndexOutOfBoundsMessage(Messages.Language));
		}
	}
	
	
	
	private void showExitAlertDialog(String title, String message) 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
			   .setTitle(title)
		       .setCancelable(false)
		       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		               MinimalDicomViewer.this.finish();
		           }
		       });
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
	
	
	
	private final Handler loadingHandler = new Handler() 
	{
		public void handleMessage(Message message) 
		{
			switch (message.what) 
			{

			case STARTED:
				showDialog(PROGRESS_IMAGE_LOAD);
				break;

			case FINISHED:
				try 
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				} 
				catch (IllegalArgumentException ex) 
				{	
					// Do nothing		
				}
				if (message.obj instanceof ImageGray16Bit) 
				{
					setImage((ImageGray16Bit) message.obj);
					((TextView)findViewById(R.id.PatientNameValue)).setText( ((ImageGray16Bit) message.obj).getPatientName());
					((TextView)findViewById(R.id.PatientPrenameValue)).setText( ((ImageGray16Bit) message.obj).getPatientPrename());
					((TextView)findViewById(R.id.PatientBirthValue)).setText( ((ImageGray16Bit) message.obj).getPatientBirth());
				}
				setFilenameLabel((TextView)findViewById(R.id.currentFileLabel), actualFileName);

				break;

			case UNCATCHABLE_ERROR_OCCURRED:
				try 
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				} 
				catch (IllegalArgumentException ex){}

				// Get the error message
				String errorMessage;

				if (message.obj instanceof String)
					errorMessage = (String) message.obj;
				else
					errorMessage = "Unknown error";

				// Show an alert dialog
				showExitAlertDialog("[ERROR] Loading file",
						"An error occured during the file loading.\n\n"
						+ errorMessage);
				break;

			case OUT_OF_MEMORY:
				try 
				{
					dismissDialog(PROGRESS_IMAGE_LOAD);
				} 
				catch (IllegalArgumentException ex){}

				// Show an alert dialog
				showExitAlertDialog(Messages.getHeaderOutOfMemoryErrorMessage(Messages.Language),
						Messages.getOutOfMemoryErrorMessage(Messages.Language));
				break;
			}
		}
	};
    
    
	private static final class DicomFileLoader extends Thread 
	{
		// The handler to send message to the parent thread
		private final Handler mHandler;
		
		// The file to load
		private final String fileName;
		
		public DicomFileLoader(Handler handler, String fileName) 
		{
			
			if (handler == null)
				throw new NullPointerException("The handler is null while calling the loading thread.");
			
			mHandler = handler;
			
			if (fileName == null)
				throw new NullPointerException("The file is null while calling the loading thread.");
			
			this.fileName = fileName;
		}
		
		public void run() 
		{
			// If the image data is null, do nothing.
			File f = new File(fileName);
			if (!f.exists()) 
			{
				Message message = mHandler.obtainMessage();
				message.what = UNCATCHABLE_ERROR_OCCURRED;
				message.obj = "The file doesn't exist.";
				mHandler.sendMessage(message);
				return;
			}
			
			mHandler.sendEmptyMessage(STARTED);
			// If image exists show image
			try {
				ImageGray16Bit image = null;
				
				DicomReader reader = new DicomReader(fileName);
				int pixelData[] = reader.getPixelData();
				if(pixelData != null)
				{
					image = new ImageGray16Bit();
		    		image.setOriginalImageData(pixelData);
		    		pixelData = DicomHelper.setBrightnessAndContrast(pixelData, iBrightness, iContrast);
		    		image.setImageData(pixelData);
		    		image.setWidth(reader.getWidth());
		    		image.setHeight(reader.getHeight());
		    		image.setPatientName(reader.getPatientName());
		    		image.setPatientPrename(reader.getPatientPrename());
		    		image.setPatientBirth(reader.getPatientBirthString());
				}
				// Send the LISA 16-Bit grayscale image
				Message message = mHandler.obtainMessage();
				message.what = FINISHED;
				message.obj = image;
				mHandler.sendMessage(message);
				return;
				
			} 
			catch (Exception ex) 
			{
				mHandler.sendEmptyMessage(FINISHED);
			}
		}
	}
    
    
    public synchronized void prevImage(View view) 
    {
		while (dicomFileLoader.isAlive()) 
		{
			try 
			{
				synchronized(this){wait(10);}
			} 
			catch (InterruptedException e) {}
		}
		
		// If the current file index is 0, there is
		// no previous file in the files array
		// We add the less or equal to zero because it is
		// safer
		if (currentFileIndex <= 0) 
		{
			currentFileIndex = 0;
			return;			
		}
		//  Decrease the file index
		currentFileIndex--;
		
		actualFileName = fileArray[currentFileIndex].getAbsolutePath();
		dicomFileLoader = new DicomFileLoader(loadingHandler,fileArray[currentFileIndex].getAbsolutePath());
		
		dicomFileLoader.start();
	}
    
    
    public synchronized void nextImage(View view) 
    {
		// Wait until the loading thread die
		while (dicomFileLoader.isAlive()) 
		{
			try 
			{
				synchronized(this){wait(10);}
			} 
			catch (InterruptedException e) {}
		}
		
		// If the current file index is the last file index,
		// there is no next file in the files array
		// We add the greater or equal to (mFileArray.length - 1)
		// because it is safer
		if (currentFileIndex >= (fileArray.length - 1)) 
		{
			currentFileIndex = (fileArray.length - 1);
			return;
		}
		//  Increase the file index
		currentFileIndex++;
		// Start the loading thread to load the DICOM image
		actualFileName = fileArray[currentFileIndex].getAbsolutePath();
		dicomFileLoader = new DicomFileLoader(loadingHandler,  fileArray[currentFileIndex].getAbsolutePath());
		
		dicomFileLoader.start();
	}
    
    
    public void displayLanguage() 
	{
		String title = "no Title configured";
		title = Messages.getLabel(Messages.CONFIGURE_LANGUAGE, Messages.Language);
		final String items[] = {"English","Deutsch"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, Messages.Language,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// onClick Action
				Messages.Language = whichButton;
			}
		})
		.setPositiveButton(Messages.getLabel(Messages.BUTTON_OK, Messages.Language), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// on Ok button action
				SharedPreferences settings = getSharedPreferences(MinimalDicomViewer.PREFERENCES_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();	    
			    editor.putInt(MDVFileChooser.SELECTED_LANGUAGE, Messages.Language );
			    editor.commit();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
    
    
    
    public void displayConfigureDisclaimer() 
	{
		String title = "no Title configured";
		title = Messages.getLabel(Messages.CONFIGURE_DISCLAIMER_DIALOG, Messages.Language);
		int preSelected = MDVFileChooser.bHideDisclaimerDialog == false ? 0 : 1;
		final String items[] = {Messages.getLabel(Messages.SHOW_DISCLAIMER_DIALOG, Messages.Language),Messages.getLabel(Messages.HIDE_DISCLAIMER_DIALOG, Messages.Language)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setSingleChoiceItems(items, preSelected,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// onClick Action
				if(whichButton == 0)
				{
					MDVFileChooser.bHideDisclaimerDialog = false;
				}
				else MDVFileChooser.bHideDisclaimerDialog = true;
				
			}
		})
		.setPositiveButton(Messages.getLabel(Messages.BUTTON_OK, Messages.Language), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// on Ok button action
				SharedPreferences settings = getSharedPreferences(MinimalDicomViewer.PREFERENCES_NAME, 0);
			    SharedPreferences.Editor editor = settings.edit();	    
			    editor.putBoolean(MDVFileChooser.HIDE_DISCLAIMER_DIALOG, MDVFileChooser.bHideDisclaimerDialog );
			    editor.commit();
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}
}