package lu.tudor.santec.dicom.gui.filechooser;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;

public class DicomFileFilter extends FileFilter {
	private Vector<HeaderTag> filterTags;
	private String description;

	public DicomFileFilter(Vector<HeaderTag> headerTags) {
		this.filterTags = headerTags;
		this.description = "DICOM Filter: ";
		for (Iterator<HeaderTag> iter = headerTags.iterator(); iter.hasNext();) {
			HeaderTag ht = (HeaderTag) iter.next();
			this.description += ht.tagName + "=" + ht.tagValue + " ";
		}
	}
	
	public boolean accept(File f) {
		if (f.isDirectory())
			return true;
		boolean matches = true;
		DicomHeader dh = new DicomHeader(f);
		if (dh.isEmpty() )
			return false;

		for (Iterator<HeaderTag> iter = filterTags.iterator(); iter.hasNext();) {
			try {
				HeaderTag ht = (HeaderTag) iter.next();
//				System.out.println("compare tag: " + ht.tagValue + " = " + ht.tagValue);
				if (! ht.tagValue.matches(dh.getHeaderStringValue(ht.tagNr))) {
					matches = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		dh = null;
		System.gc();
		return matches;
	}

	public String getDescription() {
		return description;
	}

}
