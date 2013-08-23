package lu.tudor.santec.dicom.gui.selector;

import java.io.File;

import lu.tudor.santec.dicom.gui.header.DicomHeader;

public class DicomFile {

	private DicomHeader dicomHeader;
	private File file;
	private long fileSize;

	public DicomFile(File f) {
		this.file = f;
		this.fileSize = f.length()/1024;
		this.dicomHeader = new DicomHeader(f);
	}

	/**
	 * @return the dicomHeader
	 */
	public DicomHeader getDicomHeader() {
		return dicomHeader;
	}

	/**
	 * @param dicomHeader the dicomHeader to set
	 */
	public void setDicomHeader(DicomHeader dicomHeader) {
		this.dicomHeader = dicomHeader;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the fileSize
	 */
	public long getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
}
