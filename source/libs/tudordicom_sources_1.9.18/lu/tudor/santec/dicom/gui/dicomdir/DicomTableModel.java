package lu.tudor.santec.dicom.gui.dicomdir;

import org.dcm4che2.data.DicomObject;

public interface DicomTableModel {

    public DicomObject getRecord(int line);
    
}
