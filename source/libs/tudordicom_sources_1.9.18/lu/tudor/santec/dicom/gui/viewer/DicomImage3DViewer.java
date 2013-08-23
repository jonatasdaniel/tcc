package lu.tudor.santec.dicom.gui.viewer;
/*****************************************************************************
 *                                                                           
 *  Copyright (c) 2006 by SANTEC/TUDOR www.santec.tudor.lu                   
 *                                                                           
 *                                                                           
 *  This library is free software; you can redistribute it and/or modify it  
 *  under the terms of the GNU Lesser General Public License as published    
 *  by the Free Software Foundation; either version 2 of the License, or     
 *  (at your option) any later version.                                      
 *                                                                           
 *  This software is distributed in the hope that it will be useful, but     
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        
 *  Lesser General Public License for more details.                          
 *                                                                           
 *  You should have received a copy of the GNU Lesser General Public         
 *  License along with this library; if not, write to the Free Software      
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  
 *                                                                           
 *****************************************************************************/

import ij.ImagePlus;
import ij.WindowManager;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import ij3d.Content;
import ij3d.Executer;
import ij3d.Image3DUniverse;
import ij3d.Install_J3D;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImage3DViewer {

	final DicomImagePanel dicomImagePanel;
	private Image3DUniverse univ = null;
	private Executer exec = null;
	
	public DicomImage3DViewer(DicomImagePanel dip) throws Exception {
		
		try {
			if (Install_J3D.getJava3DVersion() == null) {
				throw new Exception("No Java 3D installed");
			}			
		} catch (Throwable e) {
			throw new Exception("No Java 3D installed");
		}
		
		this.dicomImagePanel = dip;
		
		JButton threeDee = new JButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_3D)) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				System.out.println("3D");
				
				// Create a universe and show it
//				if (univ == null)  {
					univ = new Image3DUniverse();
					exec = new Executer(univ);
//				}
				univ.show();
				
				Series s = dicomImagePanel.getSeries();
				ImagePlus ip = s.getAsImageStack();
				
//				 Add the image as an isosurface
				new StackConverter(ip).convertToGray8();
				ip.setTitle(dicomImagePanel.getImage().getTitle());
				ip.show();
				WindowManager.getCurrentWindow().setVisible(false);
				
				exec.addContentFromImage(ip);

				
			}
		}); 
		threeDee.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.3dViewer"));
		
		this.dicomImagePanel.addFunctionButton(threeDee ,new ImageListener() {
			public void pixelClicked(ImagePlus ip, int x, int y) {
			}
			public void optionSelected(boolean selected) {
			}
			public void mouseMoved(int x, int y, MouseEvent e) {
			}
			public void imageChanged(ImagePlus image) {
			}
		});
	}
	
}
