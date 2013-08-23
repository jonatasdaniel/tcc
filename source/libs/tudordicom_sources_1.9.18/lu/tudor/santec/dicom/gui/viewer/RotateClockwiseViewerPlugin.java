package lu.tudor.santec.dicom.gui.viewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.viewer.DicomImagePanel;
import lu.tudor.santec.dicom.gui.viewer.ImageListener;
import lu.tudor.santec.i18n.Translatrix;

public class RotateClockwiseViewerPlugin implements ActionListener {

	private DicomImagePanel dicomImagePanel;
	private JButton button;

	public RotateClockwiseViewerPlugin(DicomImagePanel dip) {
		this.dicomImagePanel = dip;
		
		button = new JButton(DicomIcons.getIcon(DicomIcons.ACTION_ROTATE));
		button.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.rotateImage"));
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
		System.out.println("C");
		ImageProcessor ip = RotateClockwiseViewerPlugin.this.dicomImagePanel.getImage().getProcessor().rotateRight();
		RotateClockwiseViewerPlugin.this.dicomImagePanel.getImage().setProcessor(
				RotateClockwiseViewerPlugin.this.dicomImagePanel.getImage().getTitle(), ip);
		RotateClockwiseViewerPlugin.this.dicomImagePanel.getBigDialog().repaint();
	}
	
	public void setVisible(boolean visible){
		button.setVisible(visible);
	}
}
