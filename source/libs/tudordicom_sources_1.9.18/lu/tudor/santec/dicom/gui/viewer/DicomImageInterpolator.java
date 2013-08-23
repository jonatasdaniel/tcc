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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JToggleButton;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImageInterpolator {

	final DicomImagePanel dicomImagePanel;
	private JToggleButton interpolator;
	
	public DicomImageInterpolator(DicomImagePanel dip) {
		
		this.dicomImagePanel = dip;
		
			interpolator = new JToggleButton(new AbstractAction("",DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_INTERPOLATE)) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				dicomImagePanel.setInterpolation(interpolator.isSelected());
			}
		}); 
		interpolator.setToolTipText(Translatrix.getTranslationString("dicom.dicomImagePanel.interpolate"));
		
		this.dicomImagePanel.addFunctionButton(interpolator ,new ImageListener() {
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
