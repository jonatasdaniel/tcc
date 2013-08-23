package lu.tudor.santec.dicom.gui.viewer;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

public class DicomImageHistogramViewerPlugin implements ActionListener {

	private DicomImagePanel dicomImagePanel;
	private JButton button;
	private Logger logger = Logger.getLogger(DicomImageHistogramViewerPlugin.class.getName());
	
	
	public DicomImageHistogramViewerPlugin(DicomImagePanel dip) {
		this.dicomImagePanel = dip;
		
    	button = new JButton(DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_HISTOGRAM));
    	button.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.histogram"));
    	button.addActionListener(this);

    	
    	this.dicomImagePanel.addFunctionButton(button, new ImageListener() {
    
    		public void mouseMoved(int x, int y, MouseEvent e) {
    		}
    
    		public void optionSelected(boolean selected) {
    		}
    
    		public void pixelClicked(ImagePlus ip, int x, int y) {
    		}
			public void imageChanged(ImagePlus image) {
			}
    	});
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getModifiers() == KeyEvent.VK_CONTROL+1){
			IJ.setKeyDown(KeyEvent.VK_ALT);
		}
		try {
			ImagePlus imp = new ImagePlus("Image", this.dicomImagePanel.imageProcessor);
			Vector<Roi> rois = this.dicomImagePanel.getRois();
			if (rois.size()==1) {
				Roi roi = (Roi) rois.iterator().next();
				imp.setRoi(roi);
			}
			WindowManager.setTempCurrentImage(imp);
			IJ.run("Histogram");
		} catch (NullPointerException e1) {
			logger.log(Level.INFO, "There is no image open!");
		}
		
		
	}

	

}
