package dicomdir;

import org.dcm4che2.data.Tag;
import org.dcm4che2.media.StdGenJPEGApplicationProfile;


public class MyExtendedApplicationProfile extends StdGenJPEGApplicationProfile  {
    
    private static final int[] STD_GEN_JPEG_SERIES_KEYS = {
	    Tag.SpecificCharacterSet, Tag.Modality, Tag.InstitutionName,
	    Tag.InstitutionAddress, Tag.PerformingPhysicianName,
	    Tag.SeriesInstanceUID, Tag.SeriesNumber,
	    Tag.SeriesDescription};
    
    public MyExtendedApplicationProfile() {
	super();
	setSeriesKeys(STD_GEN_JPEG_SERIES_KEYS);
    }
    
}