package lu.tudor.santec.dicom.gui;

public interface SpaceListener {

    public long getFreeMBLimit();
    
    public void spaceLow(String path, double freeMB);
    
}
