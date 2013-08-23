package lu.tudor.santec.dicom.receiver;

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

import java.io.File;
import java.util.logging.Logger;

import lu.tudor.santec.dicom.gui.dicomdir.DICOMDIRVIEW;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomDirWatcher extends Thread {

	private DicomDirListener listener;

	private File dicomdirFile;

	private int interval;

	private long oldLastModified = 0;

	private final static Logger log = Logger.getLogger("DicomDirWatcher");

	public DicomDirWatcher(File dicomdirFile, int interval,
			DICOMDIRVIEW panel) {
		this.dicomdirFile = dicomdirFile;
		this.interval = interval;
		this.oldLastModified  = this.dicomdirFile.lastModified();
		log.info("+++++++++ adding dicom listener to " + dicomdirFile);
		this.listener = panel;
		this.start();
	}
	
	public void fireDicomEvent(DicomEvent d_Event) {
		listener.dicomdirChanged(d_Event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			long lastModified = this.dicomdirFile.lastModified();
			// if file hast changed
			if (this.oldLastModified != lastModified) {
				this.oldLastModified = lastModified;
				this.fireDicomEvent(new DicomEvent(this.dicomdirFile));
			}
			try {
				Thread.sleep(this.interval);
			} catch (InterruptedException e) {
			}
		}
	}


}
