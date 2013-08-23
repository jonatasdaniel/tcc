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
import ij.gui.Roi;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JToggleButton;

import lu.tudor.santec.dicom.gui.DicomIcons;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomImageCornerSelector {

	private DicomImagePanel dicomImagePanel;
	private int size;
	private Point[] points = new Point[4];
	Vector<Roi> v = new Vector<Roi>();
	private JToggleButton button;
	
	public DicomImageCornerSelector(DicomImagePanel dip) {
	    this(dip, 100);
	}
	
	public DicomImageCornerSelector(DicomImagePanel dip, int sizePixels) {
		
		this.dicomImagePanel = dip;
		this.size = sizePixels;
		
//		 select corners manual
		button = this.dicomImagePanel.addModeButton(DicomIcons.getScreenDependentIcon(DicomIcons.ACTION_CORNERS), "Select Corners",new ImageListener() {
			int i = 0;
			public void pixelClicked(ImagePlus ip, int x, int y) {
				int corner = RoiBuilder.CORNER_TOP_LEFT;

				if (x > (ip.getWidth()/2)) {
					if (y > (ip.getHeight()/2)) {
						corner = RoiBuilder.CORNER_BOTTOM_RIGHT;
						i = corner - 1;
					} else {
						corner = RoiBuilder.CORNER_TOP_RIGHT;	
						i = corner - 1;
					}
				} else {
					if (y > (ip.getHeight()/2)) {
						corner = RoiBuilder.CORNER_BOTTOM_LEFT;
						i = corner - 1;
					} else {
						corner = RoiBuilder.CORNER_TOP_LEFT;
						i = corner - 1;
					}
				}				
//				if (i >= 4) {
//					v.removeAllElements();
//					i = 0;
//				} else {
//					RoiBuilder.createCornerRoi(v,x,y,size, corner, Color.YELLOW);
					points[i] = new Point(x,y);
//					i++;
//				}
				v.removeAllElements();
				for (int c = 0; c < points.length; c++) {
					if (points[c] != null) {
						corner = c + 1;
						RoiBuilder.createCornerRoi(v,points[c].x,points[c].y,size, corner, Color.YELLOW);
					}
				}
				
				dicomImagePanel.setDefaultRois(v);
				
			}
			public void mouseMoved(int x, int y, MouseEvent e) {				
			}
			public void optionSelected(boolean selected) {
				if (!selected) {
					v.removeAllElements();
				} else {
					for (int c = 0; c < points.length; c++) {
						if (points[c] != null) {
							int corner = c + 1;
							RoiBuilder.createCornerRoi(v,points[c].x,points[c].y,size, corner, Color.YELLOW);
						}
					}
					
					dicomImagePanel.setDefaultRois(v);
				}
			}
			public void imageChanged(ImagePlus image) {
			}
			});
	}

	public Point[] getCorners() {
		return points;
	}

	public void resetCorners() {
		this.points = new Point[4];
		v.removeAllElements();
		dicomImagePanel.setDefaultRois(v);
	}
	
	public void setVisible(boolean visible){
		this.button.setVisible(visible);
		resetCorners();
	}
	
}
