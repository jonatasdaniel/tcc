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
public class DicomImageAngleMeasurement {

	DicomImagePanel dicomImagePanel;
	
	public DicomImageAngleMeasurement(DicomImagePanel dip) {
		
		this.dicomImagePanel = dip;
		
		this.dicomImagePanel.addModeButton(DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_ANGLE), Translatrix.getTranslationString("dicom.dicomImagePanel.measureAngle"),new ImageListener() {
			Vector<Roi> v = new Vector<Roi>();
			Vector <OSDText>vText = new Vector<OSDText>();
			int x1, x2, y1, y2;
			int count = 1;
			private Line l;
			private Line l1;
			private NumberFormat nf  = NumberFormat.getInstance();
			private double deg1;

			public void pixelClicked(ImagePlus ip, int x, int y) {
				vText.removeAllElements();
				if (count == 1) {
					v.removeAllElements();
					x1 = x;
					y1 = y;
					count ++;
					OvalRoi or = new OvalRoi(x-5,y-5,10,10);
					or.setName("0,255,0");
					v.add(or);
				} else if (count == 2) {
					x2 = x;
					y2 = y;
					count ++;
					OvalRoi or = new OvalRoi(x-5,y-5,10,10);
					or.setName("0,255,0");
					v.add(or);
					v.remove(l);
					l1 = new Line(x1,y1,x2,y2);
					l1.setName("0,255,0");
					
					System.out.println("-------------------------------------------------------");
					
					double xdiff = (l1.x1 - l1.x2);
					double ydiff = (l1.y1 - l1.y2) ;
					double tan = ydiff / xdiff;
					deg1 = Math.toDegrees(Math.atan(tan));
					System.out.println(deg1 + " °");
					
					v.add(l1);
				} else if (count == 3) {
					x1 = x;
					y1 = y;
					count ++;
					OvalRoi or = new OvalRoi(x-5,y-5,10,10);
					or.setName("0,255,0");
					v.add(or);
				} else if (count == 4) {
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
					count = 1;
					
					double xdiff = (l.x1 - l.x2);
					double ydiff = (l.y1 - l.y2) ;
					double tan = ydiff / xdiff;
					double deg2 = Math.toDegrees(Math.atan(tan));
					 System.out.println(deg2 + " °");
					 
					 double deg3 = Math.abs(deg1-deg2);
					 if (deg3 > 90) {
						 deg3 = 180 - deg3;
					 }
					 
					 int xTextPos = (int)( ((l.x1 + l.x2)/2) + ((l1.x1 + l1.x2)/2) )/2 - 50;  
					 int yTextPos = (int)( ((l.y1 + l.y2)/2) + ((l1.y1 + l1.y2)/2) )/2;
					vText.add(new OSDText(nf.format(deg3) + " °",xTextPos,yTextPos,Color.GREEN));	
					dicomImagePanel.setDefaultOSDTexts(vText);

				}
				dicomImagePanel.setDefaultRois(v);
				dicomImagePanel.setOSDTexts(vText);
			}

			public void optionSelected(boolean selected) {
				if (!selected) {
					v.removeAllElements();
					dicomImagePanel.setDefaultRois(v);
					vText.removeAllElements();
					dicomImagePanel.setOSDTexts(vText);
				}
			}
			public void mouseMoved(int x, int y, MouseEvent e) {
				if (count == 2 || count == 4) {
					x2 = x;
					y2 = y;
					v.remove(l);
					vText.removeAllElements();
					l = new Line(x1,y1,x2,y2);
					l.setName("0,255,0");
					v.add(l);

					double xdiff = (x1 - x2);
					double ydiff = (y1 - y2) ;
					double tan = ydiff / xdiff;
					double deg = Math.toDegrees(Math.atan(tan));
					
					vText.add(new OSDText(nf.format(deg) + " °",x2,y2-10,Color.GREEN));	
					dicomImagePanel.setDefaultOSDTexts(vText);
					
					dicomImagePanel.setDefaultRois(v);
				}
			}
			public void imageChanged(ImagePlus image) {
			}	
		});
	}
	
}
