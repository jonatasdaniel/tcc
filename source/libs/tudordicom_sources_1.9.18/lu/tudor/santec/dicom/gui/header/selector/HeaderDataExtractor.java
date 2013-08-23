package lu.tudor.santec.dicom.gui.header.selector;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.gui.header.HeaderTag;
import lu.tudor.santec.dicom.gui.selector.DicomFile;
import lu.tudor.santec.dicom.gui.selector.DicomMatcher;
import lu.tudor.santec.i18n.Translatrix;

public class HeaderDataExtractor {

    	/**
    	 * static logger for this class
    	 */
    	private static Logger logger = Logger.getLogger(HeaderDataExtractor.class
    			.getName());
    	
    	private Vector<HeaderTag> headerTags;
    	private Vector<HeaderTag> filterTags;
    	
    	private Component parent;
    	private ProgressMonitor progressMonitor;

	private DicomMatcher dicomMatcher;

    	public HeaderDataExtractor(Component parent)  {
    		this.parent = parent;
    	}
    	
    	public void setHeaderTags(Vector<HeaderTag> tags) {
    		this.headerTags = tags;
    	}
    	
    	public void setFilterTags(Vector<HeaderTag> tags) {
		this.filterTags = tags;
	}
    	
    	public Vector<String[]> workDir(File dir, File f) {
    		try {
    			ArrayList<File> al = new ArrayList<File>();
    			visitAllFiles(dir, al);
    			return workFiles(al, f, true);				
			} catch (Throwable e) {
				logger.log(Level.SEVERE,e.getMessage(),e );
			}
    		return null;
    	}
    	
    	
	public Vector<String[]> workFiles(ArrayList<File> al, File f, boolean extendedInfo) {
		long start = System.currentTimeMillis();
		Vector<String[]> results = new Vector<String[]>();
		ExportToCSV cvsExporter = null;
		try {
			
			if (f != null) {  // export to csv file
			    cvsExporter = new ExportToCSV(f);
			    
			    String[] columns = createColumnStrings(extendedInfo);
			    cvsExporter.println(columns);
			}
			
			
			
			if (filterTags != null) {
				this.dicomMatcher = new DicomMatcher(null);
				HeaderTag[] tags = new HeaderTag[this.filterTags.size()];
				for (int i = 0; i < tags.length; i++) {
					tags[i] = this.filterTags.get(i);
				}
				this.dicomMatcher.setHeaderTags(tags);
			}
	
	
			if (parent != null) {
				progressMonitor = new ProgressMonitor(parent, Translatrix.getTranslationString("dicom.searchingFiles"), "", 0, al.size());
				progressMonitor.setMillisToDecideToPopup(5);
				progressMonitor.setMillisToPopup(5);
			}
	
			for (int i = 0; i < al.size(); i++) {
				if (parent != null && progressMonitor.isCanceled()) {
					break;
				}
				try {
					File file = (File) al.get(i);
					if (!file.exists() || file.isDirectory())
						continue;
					
					DicomFile dFile = new DicomFile(file);
					if (parent != null) {
						progressMonitor.setNote("img " + dFile.getFile().getName() + " ( " + (i + 1) + " of " + (al.size() + 1) + " )");
						progressMonitor.setProgress(i);
					}
	
					if (dicomMatcher != null) {
						if (!dicomMatcher.matchFile(dFile))
							continue;
					}
	
					String[] result = extractFile(dFile, extendedInfo);
					// printArray(result);
					if (result != null) {
						if (cvsExporter == null) {
							results.add(result);							
						} else { // export to csv file
							cvsExporter.println(result);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			
			if (parent != null)
				progressMonitor.close();
		
		} catch (Throwable e) {
			logger.log(Level.SEVERE,e.getMessage(),e );
		}
		
		if (cvsExporter != null)
			try {
				cvsExporter.close();
				
				JOptionPane.showMessageDialog(parent,
					    "Exportet \n" + al.size() +" rows with " + headerTags.size() + "cols \nto:\n" +
					    		f.getAbsolutePath() + "\n" +
					    		"took: " + (System.currentTimeMillis() - start)/1000/60 + "min\n" +
					    		"\n Delimiter: ; Quote Char: \"");
				
			} catch (IOException e) {
				logger.log(Level.WARNING, "", e);
			}
		
		return results;
	}

	public String[] createColumns(boolean extendedInfo) {
		String[] result = new String[headerTags.size()];
		if (extendedInfo) {
			result = new String[headerTags.size()+4];
		}
			
	    	int i =0;
        	for (HeaderTag ht : headerTags) {
    	    		String tagValue = ht.getTagName();
    	    		result[i] = tagValue;
    	    		i++;
        	}	
        	if (extendedInfo) {	
	        	result[result.length-4] = "filesize (kb)";
		        result[result.length-3] = "hasPixeldata";
		        result[result.length-2] = "path";
		        result[result.length-1] = "filename";
        	}
        	return result;
    	}
	
	public String[] createColumnTooltips(boolean extendedInfo) {
		String[] result = new String[headerTags.size()];
		if (extendedInfo) {
			result = new String[headerTags.size()+4];
		}
		int i = 0;
		for (HeaderTag ht : headerTags) {
			String tagValue = ht.getTagVR() + ":" + ht.getTagNr();
			result[i] = tagValue;
			i++;
		}
		if (extendedInfo) {
			result[result.length - 4] = "filesize (kb)";
			result[result.length - 3] = "hasPixeldata";
			result[result.length - 2] = "path";
			result[result.length - 1] = "filename";
		}
		return result;
	}
	
	public String[] createColumnStrings(boolean extendedInfo) {
		String[] result = new String[headerTags.size()];
		if (extendedInfo) {
			result = new String[headerTags.size()+4];
		}
    	int i =0;
	for (HeaderTag ht : headerTags) {
    		String tagValue = ht.getTagName() + " [" + ht.getTagVR() + ":" + ht.getTagNr() + "]";
    		result[i] = tagValue;
    		i++;
	}
	if (extendedInfo) {
		result[result.length-4] = "filesize (kb)";
		result[result.length-3] = "hasPixeldata";
		result[result.length-2] = "path";
		result[result.length-1] = "filename";		
	}
	return result;
}
    		
    	private String[] extractFile(DicomFile dfile, boolean extendedInfo) {
    		String[] result = new String[headerTags.size()];
    		if (extendedInfo) {
    			result = new String[headerTags.size()+4];
    		}
    	    boolean isEmpty = true;
    		try {
    		    logger.info("testing file: " +dfile.getFile() );
    			DicomHeader dh = dfile.getDicomHeader();
    			int i = 0;
    			if (dh == null ) {
    			    logger.warning("NO DICOM file (Header is NULL): " + dfile.getFile().getAbsolutePath());
    				return null;
    			}
    			for (HeaderTag ht : headerTags) {
    			    	String tagValue = null;
    			    	try {
    			    		if (ht.getTagNr().indexOf("#") > 0) {
    			    			tagValue = dh.getHeaderValueInsideTag(ht.getTagNr().split("#"))+"";
    			    		} else {
    			    			tagValue = dh.getHeaderStringValue(ht.getTagNr());    			    	    
    			    		}
						} catch (Exception e) {
							logger.log(Level.WARNING, "Error getting Tag: " + ht.toString() + " on file "+ dfile.getFile().getName(), e);
						}
    			    	
    			    	if (tagValue != null && !tagValue.equals("")) isEmpty = false;
    			    	if (tagValue.length() > 200) {
    			    		tagValue = tagValue.substring(0, 199) + " [TRUNCATED AT 200chars]";
    			    	}
    			    	result[i] = tagValue;
    			    	i++;
    			}
    			if (isEmpty) {
    			    logger.warning("NO DICOM file: " + dfile.getFile().getAbsolutePath());
    				return null;
    			}
    			if (extendedInfo) {
	    			result[result.length-4] = (dfile.getFile().length()/1024) + "";
	    			result[result.length-3] = dfile.getDicomHeader().hasPixelData() + "";
	    			result[result.length-2] = dfile.getFile().getParentFile().getAbsolutePath();
	    			result[result.length-1] = dfile.getFile().getName();
    			}
    			return result;			
    		} catch (Throwable e) {
    			logger.info("no DICOM file: " + dfile.getFile().getName());
    			e.printStackTrace();
    			return null;
    		}
    	}

    	// Process only files under dir
    	public static void visitAllFiles(File dir, ArrayList<File> al) {
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    visitAllFiles(new File(dir, children[i]), al);
                }
            } else {
                al.add(dir);
            }
        }
    	
        
        public static String printArray(String[] ar) {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < ar.length; i++) {
    	    sb.append("'").append(ar[i]).append("',");
    	}
    	System.out.println(sb.toString());
    	return sb.toString();
        }
    	
    }

