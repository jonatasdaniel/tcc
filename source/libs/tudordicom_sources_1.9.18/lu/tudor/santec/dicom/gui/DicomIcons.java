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

package lu.tudor.santec.dicom.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.ImageIcon;


/**
 *  
 * 
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DicomIcons {
	
// the logger for this class
private static Logger logger = Logger.getLogger("lu.tudor.santec.dicom.gui.Icons");
    
	//Path
	private static final String iconpath = "resources/icons/";


    // mousecursors
    public static final String CURSOR_ZOOM = "cursor_zoom.gif";
    public static final String CURSOR_WINDOW = "cursor_window.gif";
    
    public static final String ERROR = "error.png";
    
    public static final String ACTION_UNDO = "action_undo.png";
    public static final String ACTION_MOVE = "action_move.png";
    public static final String ACTION_MOVE_UNDO = "action_move_undo.png";
    public static final String ACTION_ZOOM = "action_zoom.png";
    public static final String ACTION_ZOOM_UNDO = "action_zoom_undo.png";
    public static final String ACTION_WINDOW = "action_window.png";
    public static final String ACTION_WINDOW_UNDO = "action_window_undo.png";
    public static final String ACTION_TEXT = "action_text.png";
    public static final String ACTION_ROIS = "action_rois.png";
    public static final String ACTION_IMAGEJ = "action_imagej.png";
    public static final String ACTION_EXPORT = "action_export.png";
    public static final String ACTION_CORNERS = "action_corners.png";
    public static final String ACTION_HELP = "action_help.png";
    public static final String ACTION_ROTATE = "action_rotate.png";
    public static final String ACTION_ROTATE_CC = "action_rotate_cc.png";
    public static final String ACTION_MEASURE = "action_measure.png";
    public static final String ACTION_ANGLE = "action_angle.png";
    public static final String ACTION_HEADER = "action_header.png";
    public static final String ACTION_HEX = "action_hex.png";
    public static final String ACTION_STACK = "action_stack.png";
    public static final String ACTION_INVERT = "action_invert.png";
    public static final String ACTION_INTERPOLATE = "action_interpolate.png";
    public static final String ACTION = "action_empty.png";
    public static final String ACTION_HISTOGRAM = "action_histogram.png";
    public static final String ACTION_ONE2ONE = "action_one2one.png";	
    public static final String ACTION_ROI = "action_roi.png";
	public static final String ACTION_CROP = "action_crop.png";
    public static final String ACTION_IMAGE = "action_image.png";
	public static final String ACTION_OVERLAY = "action_overlay.png";
	public static final String ACTION_TOPO = "action_topo.png";
	public static final String ACTION_SEGMENTATION = "action_segmentation.png";
	public static final String ACTION_3D = "action_3d.png";
	
    public static final String ICON_DIFF = "action_diff.png";
    public static final String ICON_DIFF_IMAGES = "action_diff_images.png";
    
    // Arrows
    public static final String ICON_UP = "1uparrow.png";
    public static final String ICON_DOWN = "1downarrow.png";
    public static final String ICON_LEFT = "1leftarrow.png";
    public static final String ICON_RIGHT = "1rightarrow.png";
    
    // PACS
    public static final String PACS = "open.png";
    public static final String PACS_OK = "pacs_ok.png";
    public static final String PACS_OFF = "pacs_off.gif";
    public static final String PACS_ON = "pacs_on.gif";
    public static final String PACS_PROBLEM = "pacs_problem.png";
    public static final String PACS_UPDATE = "pacs_update.gif";

    //  File Dialog
    public static final String OPEN_FILE = "open_file.png";
    public static final String OPEN_DICOM_STORE = "open_dicom_store.png";
    public static final String OPEN_DICOMCD = "open_dicomcd.png";
    public static final String OPEN_REMOTE_DICOMDIR = "open_dicom_dir.png";
    public static final String OPEN_QUERY = "open_query.png";
    public static final String OPEN_SELECTOR = "open_selector.png";
    
    // other stuff
    public static final String ICON_HELP = "icon_help.png";
    public static final String ICON_INFO = "icon_info.png";
    public static final String ICON_SETTINGS = "icon_settings.png";
    public static final String ICON_RELOAD = "icon_reload.png";
    public static final String ICON_SEARCH = "icon_search.png";
    public static final String ICON_ANON = "anon_header.png";
    
    public static final String VIDEO_PLAY = "video_play.png";
    public static final String VIDEO_PAUSE = "video_pause.png";
    
    public static final String SCREEN_1 = "screens_1.png";
    public static final String SCREEN_2H = "screens_2h.png";
    public static final String SCREEN_2V = "screens_2v.png";
    public static final String SCREEN_4 = "screens_4.png";
    public static final String THREE_MONITOR = "3display.png";
    
    public static final String DICOM_VIEWER = "dicom_viewer_64.png";

	public static final String STATUS_FAILED = "status_failed.png";
	public static final String STATUS_OK = "status_ok.png";
	public static final String STATUS_UNKNOWN = "status_unknown.png";

	public static final String CLOSE = "close.png";
	
	public static final String FILTER_ADD = "filter_add.png";
	public static final String FILTER_REMOVE = "filter_remove.png";
	public static final String FILTER_EDIT = "filter_edit.png";
	
	public static final String SEARCH = "search.png";

	public static final String CONFIG_SAVE = "config_save.png";
	public static final String CONFIG_LOAD = "config_load.png";

	public static final String HEADERDATA_EVAL = "headerdata_eval.png";

	public static final String PREV = "prev.png";
	public static final String NEXT = "next.png";

	public static final String MEMORY = "memory.png";


    
    public static ImageIcon getIcon22(String iconname) {
        return getIcon(iconname, 22);
    }
    
    public static ImageIcon getIcon32(String iconname) {
        return getIcon(iconname, 32);
    }
    
    public static ImageIcon getIcon16(String iconname) {
        return getIcon(iconname, 16);
    }
    
    public static ImageIcon getScreenDependentIcon(String iconname) {
	 int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	 if (height > 1024) 
	     return getIcon32(iconname);
	 if (height < 800) 
	     return getIcon16(iconname);
	 
        return getIcon(iconname, 22);
    }
    
    public static ImageIcon getIcon(String iconname) {
        
        java.net.URL imgURL = DicomIcons.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            logger.warning("Missing Icon: " + iconpath + iconname);
        }
        return null;
    }
    public static ImageIcon getIcon(String iconname, int size) {
        
        java.net.URL imgURL = DicomIcons.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(size,size,Image.SCALE_SMOOTH));
        } else {
        	logger.warning("Missing Icon: " + iconname);
        }
        return null;
    }
    public static Image getImage(String iconname, int height) {
        
        java.net.URL imgURL = DicomIcons.class.getResource(iconpath + iconname);
        
        if (imgURL != null) {
        	return new ImageIcon(imgURL).getImage().getScaledInstance(-1,height,Image.SCALE_SMOOTH);
        }
        else {
        	logger.warning("Missing Icon: " + iconname);
        }
        return null;     
    }
        
}
