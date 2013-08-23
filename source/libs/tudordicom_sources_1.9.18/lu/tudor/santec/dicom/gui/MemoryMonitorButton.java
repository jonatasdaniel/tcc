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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 */
public class MemoryMonitorButton extends JButton implements ActionListener {

	private static final long serialVersionUID = 3030537064914356613L;
	private Runtime rt = Runtime.getRuntime();
	private NumberFormat nf = NumberFormat.getNumberInstance();
	private boolean drawLeds;
	private int SIZE = 32;
	private int BAR_WITH = 8;
	private int VOFFSET = 5;
	private int HOFFSET = 5;
	private static final Color USED = Color.RED;
	private static final Color ALLOC = Color.YELLOW;
	private static final Color FREE = Color.GREEN;
	private static final int LEDSPAN = 1;
	private static final int LEDSIZE = 2;
	private Vector<SpaceListener> listeners = new Vector<SpaceListener>();
	
	public MemoryMonitorButton(boolean enabled, boolean drawLeds) {
		this(enabled, drawLeds, 32);
	}
	
	public MemoryMonitorButton(boolean enabled, boolean drawLeds, int size) {
		this.SIZE= size;
		this.BAR_WITH = size/ 4;
		this.HOFFSET = size / 6;
            this.setEnabled(enabled);
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
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				}
			}
		}.start();
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		

		double maxVM = (rt.maxMemory()/1024);	 
		double totalAlloc = (rt.totalMemory()/1024);	
		double freeAlloc = (rt.freeMemory()/1024);
		double used = totalAlloc - freeAlloc;
		double free = maxVM - used;
		double freeMB = free/1024;

		if (this.drawLeds) {
		this.drawLeds(g ,used, totalAlloc, maxVM);
		} else {
			drawBar(g ,used, totalAlloc, maxVM);
		}
		
		this.setToolTipText("<html><h2>&nbsp;&nbsp; Memory Status: &nbsp;&nbsp;</h2>"+
				"<b>&nbsp;&nbsp; Max: </b>"+ nf.format(maxVM/1024) + " mb<br>" +
				"<b>&nbsp;&nbsp; Allocated: </b>"+ nf.format(totalAlloc/1024) + " mb<br>" +
				"<b>&nbsp;&nbsp; Used: </b>"+ nf.format(used/1024) + " mb<br>" +
				"<b>&nbsp;&nbsp; Free: </b>"+ nf.format(free/1024) + " mb<br>&nbsp;" );
		
		for (SpaceListener listener : listeners) {
		    if (freeMB <= listener.getFreeMBLimit())
			listener.spaceLow(null, freeMB);
		}
		
	}



	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setLayout(new GridLayout(1,0));
		jf.add(new MemoryMonitorButton(true, true, 22));
		jf.add(new MemoryMonitorButton(true, true, 22));
		jf.pack();
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}



	public void actionPerformed(ActionEvent e) {
		System.out.println("Running GC!");
		System.gc();
	}
	
	
	public void drawBar(Graphics g, double used, double totalAlloc, double maxVM) {
		int usedDraw = (int) ((used/maxVM)*30);
		int allocDraw = (int) ((totalAlloc/maxVM)*30);

		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif",Font.PLAIN, 10));
		g.drawString("Mem",HOFFSET+ BAR_WITH +1, VOFFSET + (int)(SIZE/2.5));
		g.drawString("Info",HOFFSET+ BAR_WITH +1, VOFFSET+ (int)(SIZE/1.2));
		
		g.setColor(FREE);
		g.fillRect(HOFFSET,VOFFSET,BAR_WITH,SIZE-1);
		g.setColor(ALLOC);
		g.fillRect(HOFFSET,SIZE-allocDraw+VOFFSET,BAR_WITH,allocDraw);
		g.setColor(USED);
		g.fillRect(HOFFSET,SIZE-usedDraw+VOFFSET,BAR_WITH,usedDraw);
	}
	
	public void drawLeds(Graphics g, double used, double totalAlloc, double maxVM) {
		
		int usedDraw = SIZE -(int) ((used/maxVM)*30);
		int allocDraw = SIZE -(int) ((totalAlloc/maxVM)*30);

		g.setColor(Color.BLACK);
		g.setFont(new Font("SansSerif",Font.PLAIN, 10));
		g.drawString("Mem",HOFFSET+ BAR_WITH +2, VOFFSET + (int)(SIZE/2.5));
		g.drawString("Info",HOFFSET+ BAR_WITH +2, VOFFSET+ (int)(SIZE/1.2));
		
		g.setColor(FREE);		
		for (int i = 0; i <=SIZE; i+=(LEDSIZE+LEDSPAN)){
			if (VOFFSET+i >= allocDraw) {
				g.setColor(ALLOC);		
			}
			if (VOFFSET+i >= usedDraw) {
				g.setColor(USED);		
			}
			g.fillRect(HOFFSET,VOFFSET+i,BAR_WITH, LEDSIZE);
		}
		
		
	}
	
	public void addMemSpaceListener(SpaceListener dsl) {
	    this.listeners.add(dsl);
	}
	
	public void removeMemSpaceListener(SpaceListener dsl) {
	    this.listeners.remove(dsl);
	}
	
}
