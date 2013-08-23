package lu.tudor.santec.dicom.gui.selector;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class DIRFileFilter extends FileFilter {

	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		return false;
	}

	public String getDescription() {
		return "Directories only";
	}

}
