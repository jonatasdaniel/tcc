package lu.tudor.santec.dicom.gui;

import java.io.File;

import lu.tudor.santec.i18n.SwingLocalizer;
import lu.tudor.santec.i18n.Translatrix;
import lu.tudor.santec.settings.SettingsPanel;

public class DicomSettings {

	private static DICOMSettingsPlugin dicomPlugin;
	private static SettingsPanel settings;

	public static SettingsPanel getSettingPanel() {
		if (settings == null) {
		    try {
	        	    Translatrix.addBundle("lu.tudor.santec.settings.resources.WidgetResources");
	        	    Translatrix.addBundle("lu.tudor.santec.dicom.gui.resources.WidgetResources");
	        	    Translatrix.addBundle(SwingLocalizer.getBundle());
	        	    Translatrix.setDefaultWhenMissing(true);
	        	    SwingLocalizer.localizeJFileChooser();
	        	    SwingLocalizer.localizeJOptionPane();		    
			} catch (Exception e) {
			    e.printStackTrace();
			}
	                
			dicomPlugin = new DICOMSettingsPlugin("dicom");
//			loggingPlugin = new LoggingPlugin("logging");
			settings = new SettingsPanel(null);
			settings.addPlugin(dicomPlugin);
//			settings.addPlugin(loggingPlugin);
			settings.setSettingsFile(new File("settings.xml"));
			settings.loadSettings();
		} 
		return settings;
	}
	
	public static DICOMSettingsPlugin getDicomSettingsPlugin() {
		if (dicomPlugin == null) {
			getSettingPanel();
		}
		return dicomPlugin;
	}
}
