/*                                                                           *
 *  Copyright (c) 2002, 2003 by TIANI MEDGRAPH AG                            *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package org.dcm4che.util;

import java.util.StringTokenizer;

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since    May, 2002
 * @version    $Revision: 1.1 $ $Date: 2008-10-30 10:28:38 $
 */
public final class DcmURL extends Object
{
    // Constants -----------------------------------------------------
    /**  Description of the Field */
    public final static int DICOM_PORT = 104;

    private final static int DELIMITER = -1;
    private final static int CALLED_AET = 0;
    private final static int CALLING_AET = 1;
    private final static int HOST = 2;
    private final static int PORT = 3;
    private final static int END = 4;

    // Attributes ----------------------------------------------------
    private DcmProtocol protocol;
    private String calledAET;
    private String callingAET;
    private String host;
    private int port = DICOM_PORT;


    // Constructors --------------------------------------------------
    /**
     *Constructor for the DcmURL object
     *
     * @param  spec  Description of the Parameter
     */
    public DcmURL(String spec)
    {
        parse(spec.trim());
        if (calledAET == null) {
            throw new IllegalArgumentException("Missing called AET");
        }
        if (host == null) {
            throw new IllegalArgumentException("Missing host name");
        }
    }


    /**
     *Constructor for the DcmURL object
     *
     * @param  protocol    Description of the Parameter
     * @param  calledAET   Description of the Parameter
     * @param  callingAET  Description of the Parameter
     * @param  host        Description of the Parameter
     * @param  port        Description of the Parameter
     */
    public DcmURL(String protocol, String calledAET, String callingAET,
            String host, int port)
    {
        this.protocol = DcmProtocol.valueOf(protocol);
        this.calledAET = calledAET;
        this.callingAET = callingAET;
        this.host = host;
        this.port = port;
    }


    // Public --------------------------------------------------------
    /**
     *  Gets the protocol attribute of the DcmURL object
     *
     * @return    The protocol value
     */
    public final String getProtocol()
    {
        return protocol.toString();
    }


    /**
     *  Gets the cipherSuites attribute of the DcmURL object
     *
     * @return    The cipherSuites value
     */
    public final String[] getCipherSuites()
    {
        return protocol.getCipherSuites();
    }


    public final boolean isTLS()
    {
        return protocol.isTLS();
    }
    
    /**
     *  Gets the callingAET attribute of the DcmURL object
     *
     * @return    The callingAET value
     */
    public final String getCallingAET()
    {
        return callingAET;
    }


    /**
     *  Gets the calledAET attribute of the DcmURL object
     *
     * @return    The calledAET value
     */
    public final String getCalledAET()
    {
        return calledAET;
    }


    /**
     *  Gets the host attribute of the DcmURL object
     *
     * @return    The host value
     */
    public final String getHost()
    {
        return host;
    }


    /**
     *  Gets the port attribute of the DcmURL object
     *
     * @return    The port value
     */
    public final int getPort()
    {
        return port;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(64);
        sb.append(protocol).append("://").append(calledAET);
        if (callingAET != null) {
            sb.append(':').append(callingAET);
        }
        sb.append('@').append(host).append(':').append(port);
        return sb.toString();
    }


    // Private -------------------------------------------------------

    private void parse(String s)
    {
        int delimPos = s.indexOf("://");
        if (delimPos == -1) {
            throw new IllegalArgumentException(s);
        }
        protocol = DcmProtocol.valueOf(s.substring(0, delimPos));
        StringTokenizer stk = new StringTokenizer(
                s.substring(delimPos + 3), ":@/", true);
        String tk;
        int state = CALLED_AET;
        boolean tcpPart = false;
        while (stk.hasMoreTokens()) {
            tk = stk.nextToken();
            switch (tk.charAt(0)) {
                case ':':
                    state = tcpPart ? PORT : CALLING_AET;
                    break;
                case '@':
                    tcpPart = true;
                    state = HOST;
                    break;
                case '/':
                    return;
                default:
                    switch (state) {
                        case CALLED_AET:
                            calledAET = tk;
                            break;
                        case CALLING_AET:
                            callingAET = tk;
                            break;
                        case HOST:
                            host = tk;
                            break;
                        case PORT:
                            port = Integer.parseInt(tk);
                            return;
                        default:
                            throw new RuntimeException();
                    }
                    state = DELIMITER;
                    break;
            }
        }
    }
    
    public DcmURL clone() {
	return new DcmURL(this.toString());
    }
}

