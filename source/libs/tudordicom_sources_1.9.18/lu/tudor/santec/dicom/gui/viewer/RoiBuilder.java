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
import ij.gui.Line;
import ij.gui.Roi;

import java.awt.Color;
import java.util.Vector;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class RoiBuilder {

	public static final int CORNER_TOP_LEFT = 1;
	public static final int CORNER_TOP_RIGHT = 2;
	public static final int CORNER_BOTTOM_LEFT = 3;
	public static final int CORNER_BOTTOM_RIGHT = 4;
	public static final int VERTICAL = 1;
	public static final int HORIZONTAL = 2;
	
	public static void createCornerRoi(Vector<Roi> roiVector, int xPos, int yPos, int size, int corner, Color color) {
		Roi roi;
		String colorStr = color.getRed()+","+color.getGreen()+","+color.getBlue();
		int halfSize = (size / 3) * 2;
		
		switch (corner) {
		case CORNER_TOP_LEFT:
			roi = new Line(xPos,yPos,xPos+size,yPos);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos,yPos,xPos,yPos+size);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos+halfSize,yPos,xPos,yPos+halfSize);
			roi.setName(colorStr);
			roiVector.add(roi);
			break;
		case CORNER_TOP_RIGHT:
			roi = new Line(xPos,yPos,xPos-size,yPos);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos,yPos,xPos,yPos+size);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos-halfSize,yPos,xPos,yPos+halfSize);
			roi.setName(colorStr);
			roiVector.add(roi);
			break;
		case CORNER_BOTTOM_LEFT:
			roi = new Line(xPos,yPos,xPos,yPos-size);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos,yPos,xPos+size,yPos);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos+halfSize,yPos,xPos,yPos-halfSize);
			roi.setName(colorStr);
			roiVector.add(roi);
			break;
		case CORNER_BOTTOM_RIGHT:
			roi = new Line(xPos,yPos,xPos,yPos-size);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos,yPos,xPos-size,yPos);
			roi.setName(colorStr);
			roiVector.add(roi);
			roi = new Line(xPos-halfSize,yPos,xPos,yPos-halfSize);
			roi.setName(colorStr);
			roiVector.add(roi);
			break;

		default:
			break;
		}
	}
	
	public static void createDistanceRoi(Vector<Roi> roiVector, int x1, int y1, int x2, int y2, int size, Color color) {

		int halfSize = size/2;
		
		Roi roi = new Line(x1,y1,x2,y2);
		roi.setName(color.getRed()+","+color.getGreen()+","+color.getBlue());
		roiVector.add(roi);
		
		if (Math.abs(x1-x2) < Math.abs(y1-y2)) {
			// vertical
			roi = new Line(x1-halfSize,y1,x1+halfSize,y1);
			roi.setName(color.getRed()+","+color.getGreen()+","+color.getBlue());
			roiVector.add(roi);
			roi = new Line(x2-halfSize,y2,x2+halfSize,y2);
			roi.setName(color.getRed()+","+color.getGreen()+","+color.getBlue());
			roiVector.add(roi);
		} else {
			// Horizontal
			roi = new Line(x1,y1-halfSize,x1,y1+halfSize);
			roi.setName(color.getRed()+","+color.getGreen()+","+color.getBlue());
			roiVector.add(roi);
			roi = new Line(x2,y2-halfSize,x2,y2+halfSize);
			roi.setName(color.getRed()+","+color.getGreen()+","+color.getBlue());
			roiVector.add(roi);
		}

	}
	
}
