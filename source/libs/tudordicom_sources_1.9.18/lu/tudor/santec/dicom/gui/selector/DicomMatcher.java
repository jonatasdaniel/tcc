package lu.tudor.santec.dicom.gui.selector;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ProgressMonitor;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.i18n.Translatrix;

public class DicomMatcher {

    
    
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomMatcher.class
			.getName());
	
	private HeaderTag[] headerTags;
	private Component parent;
	private ProgressMonitor progressMonitor;
	
	private String lastMessage;

	public DicomMatcher(Component parent)  {
		this.parent = parent;
	}
	
	public void setHeaderTags(HeaderTag[] tags) {
		this.headerTags = tags;
	}
	
	public void setHeaderTags(List<HeaderTag> tags) {
		this.headerTags = tags.toArray(new HeaderTag[0]);
	}
	
	@SuppressWarnings("unchecked")
	public Vector findMachingFiles(File dir) {
		ArrayList al = new ArrayList();
		visitAllFiles(dir, al);
		Vector matches = new Vector();
		
		if (parent != null)
			progressMonitor = new ProgressMonitor(parent, Translatrix.getTranslationString("dicom.searchingFiles"), "", 0, al.size());
		
		for (int i = 0; i < al.size(); i++) {
			if (parent != null && progressMonitor.isCanceled()) {
				break;
			}
			try {
				DicomFile dFile = new DicomFile((File)al.get(i));
				if (parent != null) {
					progressMonitor.setNote("img " + dFile.getFile().getName()
						+ " ( " + (i + 1) + " of " + (al.size() + 1)
						+ " )");
					progressMonitor.setProgress(i);
				}
				if (matchFile(dFile))
					matches.add(dFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (parent != null)
			progressMonitor.close();
		return matches;
	}
		
	
	
	public boolean matchFile(DicomFile dfile) {
		try {
		    logger.info("testing file: " +dfile.getFile() );
			DicomHeader dh = dfile.getDicomHeader();
			return matchFile(dh);		
		} catch (Exception e) {
			logger.info("no DICOM file: " + dfile.getFile().getName());
			return false;
		}
	}
	
	public boolean matchFile(DicomHeader dh) {
	    if (headerTags == null) {
			lastMessage = "DicomMatcher: no filterTags set -> MATCH";
			logger.warning("no filterTags set -> MATCH");
			return true;
	    }
		
			for (int i = 0; i < headerTags.length; i++) {
			    try {
					String tagNr = headerTags[i].getTagNr();
					String tagValue = headerTags[i].getTagValue();
					
					if (! dh.containsHeaderTag(tagNr)) {
					    lastMessage = "DicomMatcher: image is missing tag: " + tagNr;
					    logger.info("missing tag: " + tagNr);
					    return false;
					}
					
					if (tagValue == null || "".equals(tagValue)) {
						logger.info("DICOM Matcher: Tag found, no check on Tagvalue.");
						return true;	
					}
					
					String isVal = dh.getHeaderStringValue(tagNr).trim();
					String shouldExpr = tagValue; 
					logger.fine(isVal + " : " + shouldExpr);
					Pattern p = Pattern.compile(shouldExpr, Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(isVal);
					if (m.find()) {
					    lastMessage = "DicomMatcher: MATCH: " + tagNr + " is " + isVal;
					    logger.fine("  found!");
					} else {
					    lastMessage = "DicomMatcher: NOT MATCHING: " + tagNr + " is " + isVal;
					    logger.fine("");
					    return false;
					}
			    } catch (Exception e) {
					e.printStackTrace();
					return false;
			    }
			}
			logger.info("DICOM Matcher: Full Match, returning true!");
			return true;			
	}
	

	//	 Process only files under dir
	@SuppressWarnings("unchecked")
	private static void visitAllFiles(File dir, ArrayList al) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                visitAllFiles(new File(dir, children[i]), al);
            }
        } else {
            al.add(dir);
        }
    }
	
	public String getLastMessage() {
	    return lastMessage;
	}
	
}
