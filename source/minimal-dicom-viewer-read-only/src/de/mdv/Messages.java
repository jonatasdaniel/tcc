/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <Messages.java> is part of Minimal Dicom Viewer.
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

// TODO use the special android values subfolder 

public class Messages 
{
	
	public final static int LANG_ENGL = 0;
	public final static int LANG_GER = 1;
	
	public static int Language = LANG_GER;
	
	
	public static final int MENU_ABOUT 								= 0;
	public static final int ABOUT_HEADER 							= 1;
	public static final int MENU_INVERT_PICTURE 					= 2;
	public static final int MENU_TOOLBAR_VISIBLITY_ON 				= 3;
	public static final int MENU_TOOLBAR_VISIBLITY_OFF 				= 4;
	public static final int LOADING_IMAGE 							= 5;
	public static final int FILE 									= 6;
	public static final int ERROR_LOADING_FILE 						= 7;
	public static final int THE_FILE_CANNOT_BE_LOADED 				= 8;
	public static final int CANNOT_RETRIEVE_NAME 					= 9;
	public static final int NO_DICOM_FILES_IN_DIRECTORY 			= 10;
	public static final int FILE_IS_NOT_IN_DIRECTORY 				= 11;
	public static final int LABEL_BRIGHTNESS 						= 12;
	public static final int LABEL_ACCEPT 							= 13;
	public static final int LABEL_DECLINE 							= 14;
	public static final int LABEL_DISCLAIMER 						= 15;
	public static final int CONFIGURE_LANGUAGE 						= 16;
	public static final int CONFIGURE_DISCLAIMER_DIALOG 			= 17;
	public static final int SHOW_DISCLAIMER_DIALOG 					= 18;
	public static final int HIDE_DISCLAIMER_DIALOG 					= 19;
	public static final int NO_EXTERNAL_DEVICE_FOUND 				= 20;
	public static final int APP_WILL_QUIT_NOW 						= 21;
	public static final int MEDIA_STORAGE_DEVICE_NOT_SUPPORTED 		= 22;
	public static final int ERROR_OPENING_FILE 						= 23;
	public static final int MENU_DISCLAIMER_DIALOG_ON 				= 24;
	public static final int MENU_DISCLAIMER_DIALOG_OFF 				= 25;
	public static final int BUTTON_OK 								= 26;
	public static final int MENU_EXPORT_TO_JPEG 					= 27;
	public static final int PATIENT_NAME_LABEL 						= 28;
	public static final int PATIENT_PRENAME_LABEL 					= 29;
	public static final int PATIENT_BIRTHDATE_LABEL 				= 30;
	public static final int MENU_PATIENT_DATA_ON 					= 31;
	public static final int MENU_PATIENT_DATA_OFF 					= 32;
	public static final int BUTTON_NEW 								= 33;
	public static final int BUTTON_CANCEL 							= 34;
	public static final int BUTTON_SELECT 							= 35;
	public static final int BUTTON_CREATE 							= 36;
	public static final int LABEL_FILE_NAME 						= 37;
	public static final int OVERWRITE_EXISTING_FILE_DIALOG_HEADER 	= 38;
	public static final int OVERWRITE_EXISTING_FILE_DIALOG_TEXT 	= 39;
	public static final int OVERWRITE_EXISTING_FILE_DIALOG_TEXT_2 	= 40;
	public static final int BUTTON_YES 								= 41;
	public static final int BUTTON_NO 								= 42;
	public static final int MENU_CONFIGURE_APP 						= 43;
	public static final int TITLE_CONFIGURE_APP 					= 44;
	public static final int FILE_WRITTEN							= 45;
	public static final int LABEL_LOCATION							= 46;
	public static final int MENU_OPEN_JPEG							= 47;
	public static final int LABEL_CONTRAST	 						= 48;
	
	
	
	
	
	
	
	private static String LabelsEngl[] = {
		"About", 
		"Minimal Dicom Viewer: About",
		"Invert Picture", 
		"Show Toolbar", 
		"Disable Toolbar", 
		"Loading Image ...", 
		"File",
		"Error Loading file",
		"The file cannot be loaded",
		"Cannot retrieve its name",
		"No Dicom files within directory",
		"File is not in directory",
		"Brightness",
		"Accept",
		"Decline",
		"Disclaimer",
		"Set Language",
		"Configure Disclaimer Dialog",
		"Show Disclaimer Dialog",
		"Hide Disclaimer Dialog",
		"No external Device found",
		"App will quit now",
		"Media Storage Directories (DicomDir) are not supported yet",
		"Error opening file",
		"Show Disclaimer Dialog",
		"Hide Disclaimer Dialog",
		"OK",
		"Export to JPEG",
		"Name",
		"Prename",
		"Date of Birth",
		"Show Patient data",
		"Hide Patient data",
		"New",
		"Cancel",
		"Select",
		"Save",
		"File Name",
		"Overwrite existing file",
		"Do you want to overwrite the following file ?",
		"",
		"Yes",
		"No",
		"Configure App",
		"Configure Settings",
		"File written",
		"Location",
		"Open JPEG File",
		"Contrast"
		};
	
	private static String LabelsGer[] = {
		"Über", 
		"Über Minimal Dicom Viewer", 
		"Bild invertieren", 
		"Toolbar einblenden", 
		"Toolbar ausblenden", 
		"Bild wird geladen ...", 
		"Datei",
		"Fehler beim Datei laden",
		"Datei kann nicht geladen werden",
		"Dateiname kann nicht ermittelt werden",
		"Keine Dicom Datei im Verzeichnis",
		"Datei befindet sich nicht im Verzeichnis",
		"Helligkeit",
		"Akzeptieren",
		"Ablehnen",
		"Haftungsausschluß",
		"Sprache auswählen",
		"Haftungsausschlußdialog konfigurieren",
		"Dialog anzeigen",
		"Dialog ausblenden",
		"Kein externes Speichergerät gefunden",
		"Die Anwendung beendet sich jetzt",
		"Kataloge wie Dicomdir werden derzeit nicht unterstützt",
		"Fehler bei Öffnen der Datei",
		"Haftungsausschluß einblenden",
		"Haftungsausschluß ausblenden",
		"OK",
		"JPEG Datei erstellen",
		"Name",
		"Vorname",
		"Geburtsdatum",
		"Patientendaten anzeigen",
		"Patientendaten ausblenden",
		"Neu",
		"Abbrechen",
		"Auswählen",
		"Sichern",
		"Dateiname",
		"Überschreiben einer Datei",
		"Soll die folgende Datei:",
		"überschrieben werden ?",
		"Ja",
		"Nein",
		"Einstellungen",
		"Einstellungen konfigurieren",
		"Datei geschrieben",
		"Ort",
		"JPEG Datei öffnen",
		"Kontrast"
	};
	
	private static final String ABOUT_MESSAGE_ENGL =
		"Minimal Dicom Viewer is a prototype of a minimalistic Dicom Viewer developed for the Android™ platform.\n\nAuthor: " +
		"Robert Schmidt \nAndroid™ is a trademark of Google Inc. Use of this trademark is subject to Google Permissions " +
		"(http://www.google.com/permissions/index.html).\n\n2011 © Robert Schmidt";
	
	private static final String ABOUT_MESSAGE_GER =
		"Minimal Dicom Viewer ist der Prototyp eines minimalistischen Dicom Bild Betrachters für die Android™ Plattform.\n\nAuthor: " +
		"Robert Schmidt \nAndroid™ is a trademark of Google Inc. Use of this trademark is subject to Google Permissions " +
		"(http://www.google.com/permissions/index.html).\n\n2011 © Robert Schmidt";
	
	
	private static final String DISCLAIMER_ENGL =
				"Minimal Dicom Viewer is a Free and Open Source Software (OSS) licensed under the terms of the GNU General Public" +
				" License as published by the Free Software Foundation, either version 3 " +
				"of the License, or (at your option) any later version.\n\n" +
				"THIS VERSION OF MINIMAL DICOM VIEWER IS NOT CERTIFIED AS A MEDICAL DEVICE " +
				"(CE-1 or FDA) FOR PRIMARY DIAGNOSIS OR CLINICAL PRACTICE. " +
				"THIS SOFTWARE CAN ONLY BE USED AS A REVIEWING OR SCIENTIFIC SOFTWARE AND " +
				"CANNOT BE USED AS A MEDICAL DEVICE FOR PRIMARY DIAGNOSTIC OR ANY OTHER " +
				"CLINICAL PRACTICE.\n\n" +
				"Minimal Dicom Viewer Software is distributed in the hope that it will be useful, " +
				"but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY " +
				"or FITNESS FOR A PARTICULAR PURPOSE.\n\n" +
				"Dicom COMPATIBILITY\n" +
				"Minimal Dicom Viewer implements a part of the DICOM standard. " +
				"It reads Dicom images that are coded on 8, 12 and 16 bits. " +
				"It actually supports only grayscale not compressed Dicom files.\n\n" +
				"2011 © Robert Schmidt";
	
	private static final String DISCLAIMER_GER =
		"Minimal Dicom Viewer ist freie und Open Source Software, sie kann unter den Bedingungen der GNU General Public" +
		"License veröffentlicht von der Free Software Foundation entweder in der Version 3 (GPLv3) oder höher lizenziert werden.\n\n" +
		"DIESE VERSION DES MINIMAL DICOM VIEWER IST NICHT ZERTIFIZIERT ALS EINE MEDIZINISCHE GERÄTSCHAFT " +
		"(CE-1 oder FDA) WELCHE FÜR DIAGNOSTISCHE ZWECKE ODER DEN KLINISCHEN EINSATZ GEEIGNET IST!\n" +
		"DIESE SOFTWARE IST NUR ZU TESTZWECKEN ZU BENUTZEN.\n\n" +
		"Minimal Dicom Viewer wird OHNE JEGLICHE GARANTIEANSPRÜCHE verteilt. Etwaige GEBRAUCHSANSPRÜCHE werden nicht garantiert\n" +
		"Diese Version unterstützt einen Teil des Dicom Standards wie Bilder die in " +
		"8, 12 oder 16 Bit Graustufen kodiert sind. Komprimierte Bilder werden derzeit nicht unterstützt.\n\n" +
		"2011 © Robert Schmidt";
	
	
	
	public final static String MESSAGE_OUT_OF_MEM_HEADER_ENGL = "Error out of Memory ";
	public final static String MESSAGE_OUT_OF_MEM_ENGL = 
	"OutOfMemoryError: During the loading of the image " +
	"an out of memory error occurred.\n\n" +
	"Your file is too large for your Android(TM) system";
	
	public final static String MESSAGE_OUT_OF_MEM_HEADER_GER = "Speicherfehler";
	public final static String MESSAGE_OUT_OF_MEM_GER = "Die Bilddatei kann wegen Speichermangel auf diesem Gerät nicht geladen werden";
	
	
	public final static String MESSAGE_INDEX_OUT_OF_BOUNDS_HEADER_ENGL = "Error Image drawing";
	public final static String MESSAGE_INDEX_OUT_OF_BOUNDS_HEADER_GER = "Fehler beim Zeichnen";
	
	public final static String MESSAGE_INDEX_OUT_OF_BOUNDS_ENGL = 
		"An uncatchable error occurs while " +
		"drawing the Dicom image.";
	
	public final static String MESSAGE_INDEX_OUT_OF_BOUNDS_GER = 
		"Beim Zeichnen des Dicom Images ist ein unbekannter Fehler aufgetreten";
	
	public final static String MESSAGE_LOW_MEMORY_ENGL = "Low Memory!";
	public final static String MESSAGE_LOW_MEMORY_GER = "Speicherplatz knapp!";
	
	public final static String MESSAGE_EXTERNAL_STORAGE_ERROR_ENGL = "There is no external storage.\n"
						+ "1) There is no external storage : add one.\n"
						+ "2) Your external storage is used by a computer:"
						+ " Disconnect the it from the computer.";
	public final static String MESSAGE_EXTERNAL_STORAGE_ERROR_GER = "Kein externes Speichergerät gefunden. Mögliche Ursachen\n"+
		"1. Es gibt keinen externen Speicher: bitte hinzufügen" +
		"2. Der externe Speicher ist mit dem Computer verbunden: bitte diskonnektieren";
	
	public final static String MESSAGE_EXTERNAL_STORAGE_HEADER_ERROR_ENGL = "Error No External Storage";
	public final static String MESSAGE_EXTERNAL_STORAGE_HEADER_ERROR_GER = "Fehler externer Speicher";
	
	
	public static String getHeaderOutOfMemoryErrorMessage(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_OUT_OF_MEM_HEADER_ENGL;
		return MESSAGE_OUT_OF_MEM_HEADER_GER;
	}
	
	public static String getOutOfMemoryErrorMessage(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_OUT_OF_MEM_ENGL;
		return MESSAGE_OUT_OF_MEM_GER;
	}
	
	public static String getHeaderIndexOutOfBoundsMessage(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_INDEX_OUT_OF_BOUNDS_HEADER_ENGL;
		return MESSAGE_INDEX_OUT_OF_BOUNDS_HEADER_GER;
	}
	
	public static String getIndexOutOfBoundsMessage(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_INDEX_OUT_OF_BOUNDS_ENGL;
		return MESSAGE_INDEX_OUT_OF_BOUNDS_GER;
	}
	
	public static String getLowMemory(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_LOW_MEMORY_ENGL;
		return MESSAGE_LOW_MEMORY_GER;
	}
	
	public static String getLabel(int label, int language)
	{
		if(language == LANG_ENGL)return LabelsEngl[label];
		return LabelsGer[label];
	}
	
	public static String getMessageExternalDevice(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_EXTERNAL_STORAGE_ERROR_ENGL;
		return MESSAGE_EXTERNAL_STORAGE_ERROR_GER;
	}
	
	public static String getMessageExternalDeviceHeader(int language)
	{
		if(language == LANG_ENGL)return MESSAGE_EXTERNAL_STORAGE_HEADER_ERROR_ENGL;
		return MESSAGE_EXTERNAL_STORAGE_HEADER_ERROR_GER;
	}
	
	public static String getAboutMessage(int language)
	{
		if(language == LANG_ENGL)return ABOUT_MESSAGE_ENGL;
		return ABOUT_MESSAGE_GER;
	}
	
	public static String getDisclaimer(int language)
	{
		if(language == LANG_ENGL)return DISCLAIMER_ENGL;
		return DISCLAIMER_GER;
	}
	
	
}
