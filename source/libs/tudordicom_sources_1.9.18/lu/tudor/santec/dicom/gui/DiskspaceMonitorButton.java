package lu.tudor.santec.dicom.gui;

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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class DiskspaceMonitorButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 3030537064914356613L;
	private static NumberFormat nf = NumberFormat.getNumberInstance();
	private boolean drawLeds;
	private File path;
	private File disc;
	private Vector<SpaceListener> listeners = new Vector<SpaceListener>();
	private String label = "Disc";
	private int SIZE;
	private int BAR_WITH = 8;
	private int VOFFSET = 5;
	private int HOFFSET = 5;
	private static final Color USED = Color.RED;
	private static final Color ALLOC = Color.YELLOW;
	private static final Color FREE = Color.GREEN;
	private static final Color USED_DISABLED = new Color(255,0,0,40);
	private static final Color ALLOC_DISABLED = new Color(255,255,0,40);
	private static final Color FREE_DISABLED = new Color(0,255,0,40);
	private static final int LEDSPAN = 1;
	private static final int LEDSIZE = 2;
	
	public DiskspaceMonitorButton(File path, boolean drawLeds) {
		this(path, drawLeds, "Disc", 32);
	}
	
	public DiskspaceMonitorButton(File path, boolean drawLeds, int size) {
		this(path, drawLeds, "Disc", size);
	}
	
	public DiskspaceMonitorButton(File path, boolean drawLeds, String label) {
		this(path, drawLeds, "Disc", 32);
	}
	
	public DiskspaceMonitorButton(File path, boolean drawLeds, String label, int size) {
		this.SIZE= size;
		this.BAR_WITH = size/ 4;
		this.HOFFSET = size / 6;
	    this.path = path;
	    this.label = label;
	   
	    try {
			for (File disc : File.listRoots()) {
	//		    System.err.println(disc.getAbsolutePath());
			    if (path.getAbsolutePath().startsWith(disc.getAbsolutePath())) {
			    	this.disc = disc;
			    }
			}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    

	    
            this.setFocusPainted(false);
            this.drawLeds = drawLeds;
		nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        try {
//        	this.setIcon(new ImageIcon(MemoryMonitorButton.class.getResource("resources/memory.png")));	
        	this.setIcon(DicomIcons.getIcon(DicomIcons.MEMORY, size));
		} catch (Exception e) {
			System.out.println("Icon for memory button missing");
		}
		this.setSize(40,40);
		this.addActionListener(this);
		new Thread() {
			public void run() {
				while(true) {	
				try {
					repaint();
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				}
			}
		}.start();
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);

		double totalSpace = (path.getTotalSpace()/1024/1024);	 
		double totalAvailable = (path.getUsableSpace()/1024/1024);	
		double freeMB = (path.getFreeSpace()/1024);
		double free = freeMB/1024;
		double used = totalSpace - free;

		if (this.drawLeds) {
		this.drawLeds(g ,used, totalAvailable, totalSpace);
		} else {
			drawBar(g ,used, totalAvailable, totalSpace);
		}
		
		if (isEnabled()) {
			String pathName = "";
			String discName = "";
			try {
				pathName = path.getAbsolutePath();
				discName = disc.getAbsolutePath();				
			} catch (Exception e) {}
			
			this.setToolTipText("<html><h2>&nbsp;&nbsp; Disc-Space Status: &nbsp;&nbsp;</h2>"+
					"<b>&nbsp;&nbsp; Dir:</b>"+ pathName + " <br>" +
					"<b>&nbsp;&nbsp; Disc:</b>"+ discName + " <br><br>" +
					"<b>&nbsp;&nbsp; Total: </b>"+ nf.format(totalSpace/1024) + " gb<br>" +
					"<b>&nbsp;&nbsp; Used: </b>"+ nf.format(used/1024) + " gb<br>" +
					"<b>&nbsp;&nbsp; Free: </b>"+ nf.format(free/1024) + " gb<br>&nbsp;" );
			
			for (SpaceListener listener : listeners) {
				if (freeMB <= listener.getFreeMBLimit())
					listener.spaceLow(path.getAbsolutePath(), freeMB);
			}
		}
	}
	
	public static String getDiskInfo(File path) {
		double totalSpace = (path.getTotalSpace()/1024/1024);	 
		double freeMB = (path.getFreeSpace()/1024);
		double free = freeMB/1024;
		return "Total: "+ nf.format(totalSpace/1024) + " gb, " + "Free: "+ nf.format(free/1024) + " gb ";
	}



	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.add(new DiskspaceMonitorButton(new File("."), true, 22));
		jf.pack();
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void actionPerformed(ActionEvent e) {
	}
	
	
	public void drawBar(Graphics g, double used, double totalAlloc, double totalSpace) {
		int usedDraw = (int) ((used/totalSpace)*30);
		int allocDraw = (int) ((totalAlloc/totalSpace)*30);

		if (isEnabled())
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("SansSerif",Font.PLAIN,10));
		g.drawString(label ,HOFFSET+ BAR_WITH +1, VOFFSET + (int)(SIZE/2.5));
		g.drawString("Info",HOFFSET+ BAR_WITH +1, VOFFSET+ (int)(SIZE/1.2));
		
		if (isEnabled())
			g.setColor(FREE);
		else
			g.setColor(FREE_DISABLED);
		g.fillRect(HOFFSET,VOFFSET,BAR_WITH,SIZE-1);
		if (isEnabled())
			g.setColor(ALLOC);
		else
			g.setColor(ALLOC_DISABLED);
		g.fillRect(HOFFSET,SIZE-allocDraw+VOFFSET,BAR_WITH,allocDraw);
		if (isEnabled())
			g.setColor(USED);
		else
			g.setColor(USED_DISABLED);
		g.fillRect(HOFFSET,SIZE-usedDraw+VOFFSET,BAR_WITH,usedDraw);
	}
	
	public void drawLeds(Graphics g, double used, double totalAlloc, double totalSpace) {
		
		int usedDraw = SIZE -(int) ((used/totalSpace)*30);
		int allocDraw = SIZE -(int) ((totalAlloc/totalSpace)*30);

		if (isEnabled())
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("SansSerif",Font.PLAIN,10));
		g.drawString(label,HOFFSET+ BAR_WITH +1, VOFFSET + (int)(SIZE/2.5));
		g.drawString("Info",HOFFSET+ BAR_WITH +1, VOFFSET+ (int)(SIZE/1.2));
		
		if (isEnabled())
			g.setColor(FREE);
		else
			g.setColor(FREE_DISABLED);		
		for (int i = 0; i <=SIZE; i+=(LEDSIZE+LEDSPAN)){
			if (VOFFSET+i >= allocDraw) {
				if (isEnabled())
					g.setColor(ALLOC);
				else
					g.setColor(ALLOC_DISABLED);		
			}
			if (VOFFSET+i >= usedDraw) {
				if (isEnabled())
					g.setColor(USED);
				else
					g.setColor(USED_DISABLED);
			}
			g.fillRect(HOFFSET,VOFFSET+i,BAR_WITH, LEDSIZE);
		}
	}
	
	public void addDiscSpaceListener(SpaceListener dsl) {
	    this.listeners.add(dsl);
	}
	
	public void removeDiscSpaceListener(SpaceListener dsl) {
	    this.listeners.remove(dsl);
	}
		
}
