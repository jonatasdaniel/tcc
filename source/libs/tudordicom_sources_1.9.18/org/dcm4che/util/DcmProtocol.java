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

/**
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since    March 4, 2003
 * @version    $Revision: 1.1 $ $Date: 2008-10-30 10:28:38 $
 */
public class DcmProtocol
{
    /**  Description of the Field */
    public final static DcmProtocol DICOM =
            new DcmProtocol("dicom", null);
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS =
            new DcmProtocol("dicom-tls",
            new String[]{
            "SSL_RSA_WITH_NULL_SHA",
			"TLS_RSA_WITH_AES_128_CBC_SHA",
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_3DES =
            new DcmProtocol("dicom-tls.3des",
            new String[]{
            "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_AES =
            new DcmProtocol("dicom-tls.aes",
            new String[]{
            "TLS_RSA_WITH_AES_128_CBC_SHA",
		    "SSL_RSA_WITH_3DES_EDE_CBC_SHA"
            });
    /**  Description of the Field */
    public final static DcmProtocol DICOM_TLS_NODES =
            new DcmProtocol("dicom-tls.nodes",
            new String[]{
            "SSL_RSA_WITH_NULL_SHA",
            });


    /**
     *  Description of the Method
     *
     * @param  protocol  Description of the Parameter
     * @return           Description of the Return Value
     */
    public static DcmProtocol valueOf(String protocol)
    {
        String lower = protocol.toLowerCase();
        if (lower.equals("dicom")) {
            return DICOM;
        }
        if (lower.equals("dicom-tls")) {
            return DICOM_TLS;
        }
        if (lower.equals("dicom-tls.3des")) {
            return DICOM_TLS_3DES;
        }
        if (lower.equals("dicom-tls.aes")) {
            return DICOM_TLS_AES;
        }
        if (lower.equals("dicom-tls.nodes")) {
            return DICOM_TLS_NODES;
        }
        throw new IllegalArgumentException("protocol:" + protocol);
    }


    private final String name;
    private final String[] cipherSuites;


    private DcmProtocol(String name, String[] cipherSuites)
    {
        this.name = name;
        this.cipherSuites = cipherSuites;
    }


    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String toString()
    {
        return name;
    }


    /**
     *  Gets the cipherSuites attribute of the DcmProtocol object
     *
     * @return    The cipherSuites value
     */
    public String[] getCipherSuites()
    {
        return cipherSuites == null ? null
                 : (String[]) cipherSuites.clone();
    }


    /**
     *  Gets the tLS attribute of the DcmProtocol object
     *
     * @return    The tLS value
     */
    public boolean isTLS()
    {
        return cipherSuites != null;
    }
}

