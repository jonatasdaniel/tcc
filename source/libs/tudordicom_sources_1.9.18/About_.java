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

import javax.swing.JOptionPane;

import lu.tudor.santec.dicom.gui.DicomIcons;
import lu.tudor.santec.dicom.gui.viewer.Viewer;
import ij.plugin.PlugIn;

/**
 * ImageJ Plugin that shows an about dialog for the Tudor Dicom Tools
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class About_ implements PlugIn {

	private final static String infoMessage = "<html><h1>Tudor DICOM Plugins -  Version " + Viewer.VERSION +"</h1>"
		+ "<h2>Centre de Recherche Public Henri Tudor</h2>"
		+ "<h3>Centre de Ressources Santec www.santec.tudor.lu<br>" +
				"29, Avenue John F. Kennedy<br>" +
				"L-1855 Luxembourg - Kirchberg<h3>"
			+ "written by Johannes Hermen johannes.hermen@tudor.lu<br>"
			+ "Distributable under LGPL license - (c) 2009 Tudor/Santec<br>" 
			+ "Visit www.santec.tudor.lu/project/dicom for Infos and Updates<br><br>"
			+ "the following libraries were used to build and run this project:<br>"
			+ "<ul>"
			+ "<li><p>dcm4che: A OpenSource DICOM Toolkit<br>Distributable under LGPL license<br>Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG<br>http://sourceforge.net/projects/dcm4che/<br>&nbsp;</p></li>"
//			+ "<li><p>imageJ: Image Processing and Analysis in Java<br>Distributable under public-domain<br>Rasband, W.S., ImageJ, U. S. National Institutes of Health<br>http://rsb.info.nih.gov/ij/<br>&nbsp;</p></li>"
			+ "<li><p>FormLayout: Build better screens faster<br>Distributable under BSD Licence<br>Copyright (c) 2003 JGoodies<br>http://www.jgoodies.com/freeware/forms/<br>&nbsp;</p></li>"
			+ "<li><p>L2FProd.com: Common Components (ButtonBar)<br>Distributable under SkinLF License<br>Copyright 2004, l2fprod.com<br>http://www.l2fprod.com/<br>&nbsp;</p></li>"
			+ "</ul>";
	
	
	public void run(String arg0) {
		System.out.println("run: " + "About");
		JOptionPane.showMessageDialog(null, infoMessage,
				"Tudor DICOM - About", JOptionPane.INFORMATION_MESSAGE,
				DicomIcons.getIcon(DicomIcons.ICON_INFO));

	}

}
