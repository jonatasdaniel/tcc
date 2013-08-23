package lu.tudor.santec.dicom.gui.viewer;

import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

public class DicomImageROI {

	private DicomImagePanel dicomImagePanel;
	private ImageIcon icon = DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ROI);
	private String tooltip = Translatrix.getTranslationString("dicom.dicomImagePanel.roi");
	private Roi roi;
	
	public DicomImageROI(DicomImagePanel dip) {
		this.dicomImagePanel = dip;
		
		this.dicomImagePanel.addModeButton(icon, tooltip, new ImageListener() {
			Vector<Roi> v = new Vector<Roi>();
			int count = 0;
			int x1, x2, y1, y2 = 0;
		    
    		public void mouseMoved(int x, int y, MouseEvent e) {
    			if (count == 1){
    				v.removeAllElements();
    				x2 = x;
    				y2 = y;
    				roi = new Roi(x1, y1, x2-x1, y2-y1);
    				v.add(roi);
    			}
				
				dicomImagePanel.setRois(v);
    		}
    
    		public void optionSelected(boolean selected) {
    		}
    
    		public void pixelClicked(ImagePlus ip, int x, int y) {
    			if (count == 0){
    				v.removeAllElements();
    				count++;
    				x1=x;
    				y1=y;
    			} else if (count == 1) {
    				v.removeAllElements();
					x2 = x;
					y2 = y;
					count ++;
					roi = new Roi(x1, y1, x2-x1, y2-y1);
					v.add(roi);
					count = 2;
    			} else if (count == 2) {
    				v.removeAllElements();
    				count = 0;
    			}
    			dicomImagePanel.setRois(v);
    		}
			public void imageChanged(ImagePlus image) {
			}
    		
    	});
		
	}
	
}
