package lu.tudor.santec.dicom.gui.header;

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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;

/**
 * 
 * Class to create a line-wise Diff-View of to Strings
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: Diff.java,v $
 * <br>Revision 1.2  2009-04-07 09:34:14  hermen
 * <br>changed a lot
 * <br>
 *
 */
public class Diff {

	Vector<String> diff = new Vector<String>();
	
	public Vector<String> diff(String s1, String s2) {
		try {
			diff.clear();
			
	        int MAX = 10000;
	        String[] x = new String[MAX];   // lines in first file
	        String[] y = new String[MAX];   // lines in second file
	        int M = 0;                      // number of lines of first file
	        int N = 0;                      // number of lines of second file
	        
			BufferedReader in0 = new BufferedReader(new StringReader(s1));
		
	        BufferedReader in1 = new BufferedReader(new StringReader(s2));
	        String line;
	        while ((line = in0.readLine()) != null)
	            x[M++] = line;
	        while ((line = in1.readLine()) != null)
	            y[N++] = line;
	
	
	        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
	        int[][] opt = new int[M+1][N+1];
	
	        // compute length of LCS and all subproblems via dynamic programming
	        for (int i = M-1; i >= 0; i--) {
	            for (int j = N-1; j >= 0; j--) {
	                if (x[i].equals(y[j]))
	                    opt[i][j] = opt[i+1][j+1] + 1;
	                else 
	                    opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
	            }
	        }
	
	        // recover LCS itself and print out non-matching lines to standard output
	        int i = 0, j = 0;
	        while(i < M && j < N) {
	            if (x[i].equals(y[j])) {
	                i++;
	                j++;
	            }
	            else if (opt[i+1][j] >= opt[i][j+1]) 
	            	diff.add(x[i++]);
	            else                                 
	            	i++;
	        }
	
	        while(i < M || j < N) {
	            if      (i == M) {
	            	j++;
	            } else if (j == N) { 
	            	diff.add(x[i++]);
	            }
	        }
        
		} catch (Exception e) {
			e.printStackTrace();
		}
		return diff;
	}


}