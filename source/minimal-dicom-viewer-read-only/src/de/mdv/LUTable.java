/*
 * copyright (C) 2011 Robert Schmidt
 *
 * This file <LUTable.java> is part of Minimal Dicom Viewer.
 *
 * Minimal Dicom Viewer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Minimal Dicom Viewer is distributed as Open Source Software ( OSS )
 * and comes WITHOUT ANY WARRANTY and even with no IMPLIED WARRANTIES OF MERCHANTABILITY,
 * OF SATISFACTORY QUALITY, AND OF FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License ( GPLv3 ) for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with Minimal Dicom Viewer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Released date: 13-11-2011
 *
 * Version: 1.0
 * 
 */
package de.mdv;

public class LUTable {
	
	private int[] values;
    private double a = 1;
    private double b = 127;
   
    public LUTable()
    {
        values = new int[256];
        init();
    }
 
    public int getValue(int x)
    {
        return values[x];
    }
 
    public void setContrast(double a)
    {
        this.a = a;
        init();
    }
 
    public void setBrightness(double b)
    {
        this.b = b;
        init();
    }
 
    private void init()
    {
        for (int x = 0; x <= 255; x++)
        {
        	values[x] = (int)(a * ( x - b ) + 127.);
           
            if (values[x] > 255)
            {
                values[x] = 255;
            }
           
            if (values[x] < 0)
            {
                values[x] = 0;
            }
        }
    }

}
