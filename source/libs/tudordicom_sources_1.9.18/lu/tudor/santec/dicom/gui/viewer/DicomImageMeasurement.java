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
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Vector;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.i18n.Translatrix;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImageMeasurement {

	DicomImagePanel dicomImagePanel;
	
	public DicomImageMeasurement(DicomImagePanel dip) {
		
		this.dicomImagePanel = dip;
		
		this.dicomImagePanel.addModeButton(DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_MEASURE), Translatrix.getTranslationString("dicom.dicomImagePanel.measureImage"),new ImageListener() {
			Vector<Roi> v = new Vector<Roi>();
			Vector<OSDText> vText = new Vector<OSDText>();
			int x1, x2, y1, y2;
			int count = 1;
			private Line l;
			private NumberFormat nf  = NumberFormat.getInstance();
			public void pixelClicked(ImagePlus ip, int x, int y) { 
				if (count == 1) {
					v.removeAllElements();
					vText.removeAllElements();
					x1 = x;
					y1 = y;
					count ++;
					OvalRoi or = new OvalRoi(x-5,y-5,10,10);
					or.setName("0,255,0");
					v.add(or);
				} else if (count == 2) {
					String unit = "mm";
					x2 = x;
					y2 = y;
					count ++;
					OvalRoi or = new OvalRoi(x-5,y-5,10,10);
					or.setName("0,255,0");
					v.add(or);
					v.remove(l);
					l = new Line(x1,y1,x2,y2);
					l.setName("0,255,0");
					v.add(l);

					double xdiff = Math.abs(x1 - x2);
					double ydiff = Math.abs(y1 - y2) ;
					if ( dicomImagePanel.getPixelSizeX() == 0 || dicomImagePanel.getPixelSizeY() == 0 ) {
						unit = "px";
					} else {
						xdiff = xdiff  * dicomImagePanel.getPixelSizeX();
						ydiff = ydiff * dicomImagePanel.getPixelSizeY();
					}
					double length = Math.sqrt((xdiff*xdiff) + (ydiff*ydiff));
					vText.add(new OSDText("Length is: "+ nf.format(length) + " " + unit,x1,y1-10,Color.GREEN));	
					count = 1;					
				} 
				dicomImagePanel.setDefaultRois(v);
				dicomImagePanel.setDefaultOSDTexts(vText);
			}

			public void optionSelected(boolean selected) {
				if (!selected) {
					v.removeAllElements();
					dicomImagePanel.setDefaultRois(v);
					vText.removeAllElements();
					dicomImagePanel.setDefaultOSDTexts(vText);
				}
			}
			public void mouseMoved(int x, int y, MouseEvent e) {
				if (count == 2) {
					x2 = x;
					y2 = y;
					v.remove(l);
					l = new Line(x1,y1,x2,y2);
					l.setName("0,255,0");
					v.add(l);
					dicomImagePanel.setDefaultRois(v);
				}
			}
			public void imageChanged(ImagePlus image) {
			}
			});
	}
	
}
