package lu.tudor.santec.dicom.sender;   

/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.io.TranscoderInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVOutputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 1.12 $ $Date: 2013-04-02 07:35:04 $
 * @since Oct 13, 2005
 */
public class DicomSender extends StorageCommitmentService {
    
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomSender.class.getName());

    private static final int PEEK_LEN = 1024;

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private static final String[] ONLY_IVLE_TS = { 
        UID.ImplicitVRLittleEndian
    };

    private static final String[] IVLE_TS = { 
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ExplicitVRBigEndian,
    };

    private static final String[] EVLE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRBigEndian, 
    };

    private static final String[] EVBE_TS = { 
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ImplicitVRLittleEndian, 
    };

    private static final int STG_CMT_ACTION_TYPE = 1;

    /** TransferSyntax: DCM4CHE URI Referenced */
    private static final String DCM4CHEE_URI_REFERENCED_TS_UID = "1.2.40.0.13.1.1.2.4.94";

    private Executor executor = new NewThreadExecutor("DCMSND");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("DCMSND");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private Map<String, Set<String>> as2ts = new HashMap<String, Set<String>>();

    private ArrayList<FileInfo> files = new ArrayList<FileInfo>();

    private Association assoc;

    private int priority = 0;
    
    private int transcoderBufferSize = 1024;

    private int filesSent = 0;

    private long totalSize = 0L;

    private boolean fileref = false;

    private boolean stgcmt = false;
    
    private long shutdownDelay = 1000L;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET;

    private DcmURL url;

	private String error = "";;

    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setLocalPort(int port) {
        conn.setPort(port);
    }
    
    public final void setRemoteHost(String hostname) {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port) {
        remoteConn.setPort(port);
    }
    
    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
    }
    
    public final void disableSSLv2Hello() {
        conn.disableSSLv2Hello();
    }
    
    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        conn.setTlsNeedClientAuth(needClientAuth);
    }  
    
    public final void setKeyStoreURL(String url) {
        keyStoreURL = url;
    }
    
    public final void setKeyStorePassword(String pw) {
        keyStorePassword = pw.toCharArray();
    }
    
    public final void setKeyPassword(String pw) {
        keyPassword = pw.toCharArray();
    }
    
    public final void setTrustStorePassword(String pw) {
        trustStorePassword = pw.toCharArray();
    }
    
    public final void setTrustStoreURL(String url) {
        trustStoreURL = url;
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
    	if (calling == null)
    		calling = "";
        ae.setAETitle(calling);
    }
    
    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }
    
    public final void setOfferDefaultTransferSyntaxInSeparatePresentationContext(
            boolean enable) {
        ae.setOfferDefaultTransferSyntaxInSeparatePresentationContext(enable);
    }

    public final void setSendFileRef(boolean fileref) {
        this.fileref = fileref;
    }

    public final void setStorageCommitment(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public final boolean isStorageCommitment() {
        return stgcmt;
    }
    
    public final void setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }
    

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout) {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout) {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength) {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        conn.setSendBufferSize(bufferSize);
    }

    public final void setTranscoderBufferSize(int transcoderBufferSize) {
        this.transcoderBufferSize = transcoderBufferSize;
    }

    public final int getNumberOfFilesToSend() {
        return files.size();
    }

    public final int getNumberOfFilesSent() {
        return filesSent;
    }

    public final long getTotalSizeSent() {
        return totalSize;
    }

    public void addFile(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++)
                addFile(fs[i]);
            return;
        }
        FileInfo info = new FileInfo(f);
        DicomObject dcmObj = new BasicDicomObject();
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(f);
            in.setHandler(new StopTagInputHandler(Tag.StudyDate));
            in.readDicomObject(dcmObj, PEEK_LEN);
            info.tsuid = in.getTransferSyntax().uid();
            info.fmiEndPos = in.getEndOfFileMetaInfoPosition();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("WARNING: Failed to parse " + f + " - skipped.");
            System.out.print('F');
            return;
        } finally {
            CloseUtils.safeClose(in);
        }
        info.cuid = dcmObj.getString(Tag.SOPClassUID);
        if (info.cuid == null) {
            System.err.println("WARNING: Missing SOP Class UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        info.iuid = dcmObj.getString(Tag.SOPInstanceUID);
        if (info.iuid == null) {
            System.err.println("WARNING: Missing SOP Instance UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        addTransferCapability(info.cuid, info.tsuid);
        files.add(info);
        System.out.print('.');
    }

    private void addTransferCapability(String cuid, String tsuid) {
        Set<String> ts = as2ts.get(cuid);
        if (fileref) {
            if (ts == null) {
                as2ts.put(cuid,
                        Collections.singleton(DCM4CHEE_URI_REFERENCED_TS_UID));
            }
        } else {
            if (ts == null) {
                ts = new HashSet<String>();
                ts.add(UID.ImplicitVRLittleEndian);
                as2ts.put(cuid, ts);
            }
            ts.add(tsuid);
        }
    }

    private void configureTransferCapability() {
        int off = stgcmt ? 1 : 0;
        TransferCapability[] tc = new TransferCapability[off + as2ts.size()];
        if (stgcmt) {
            tc[0] = new TransferCapability(
                    UID.StorageCommitmentPushModelSOPClass,
                    ONLY_IVLE_TS, 
                    TransferCapability.SCU);
        }
        Iterator<Map.Entry<String, Set<String>>> iter = as2ts.entrySet().iterator();
        for (int i = off; i < tc.length; i++) {
            Map.Entry<String, Set<String>> e = iter.next();
            String cuid = e.getKey();
            Set<String> ts = e.getValue();
            tc[i] = new TransferCapability(cuid, 
                    ts.toArray(new String[ts.size()]),
                    TransferCapability.SCU);
        }
        ae.setTransferCapability(tc);
    }

    public void start() throws IOException { 
        if (conn.isListening()) {
            conn.bind(executor );
            System.out.println("Start Server listening on port " + conn.getPort());
        }
    }

    public void stop() {
        if (conn.isListening()) {
            try {
                Thread.sleep(shutdownDelay);
            } catch (InterruptedException e) {
                // Should not happen
                e.printStackTrace();                
            }
            conn.unbind();
        }
    }
    
    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void send() throws Exception{
    	this.error = "";
        for (int i = 0, n = files.size(); i < n; ++i) {
            FileInfo info = files.get(i);
            TransferCapability tc = assoc.getTransferCapabilityAsSCU(info.cuid);
            if (tc == null) {
        	throw new Exception("SOP Class UID: \"" + UIDDictionary.getDictionary().prompt(
        				info.cuid)
                        + "\" is not supported by " + remoteAE.getAETitle());
            }
            String tsuid = selectTransferSyntax(tc.getTransferSyntax(),fileref ? DCM4CHEE_URI_REFERENCED_TS_UID : info.tsuid);
            if (tsuid == null) {
	        	throw new Exception("SOP Class UID: \"" + UIDDictionary.getDictionary().prompt(
	                        info.cuid)
	                        + "\" with TransferSyntax UID: \""
	                        + UIDDictionary.getDictionary().prompt(
	                                fileref ? DCM4CHEE_URI_REFERENCED_TS_UID 
	                                        : info.tsuid)
	                        + "\" is not supported by " + remoteAE.getAETitle());
	            }
            try {
                DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    @Override
                    public void onDimseRSP(Association as, DicomObject cmd,
                            DicomObject data) {
                	DicomSender.this.onDimseRSP(cmd);
                    }
                };

                assoc.cstore(info.cuid, info.iuid, priority,
                        new DataWriter(info), tsuid, rspHandler);
            } catch (NoPresentationContextException e) {
        	throw new Exception("WARNING: " + e.getMessage()
                        + " - cannot send " + info.f);
            } catch (IOException e) {
                e.printStackTrace();
                throw new Exception("ERROR: Failed to send - " + info.f + ": "
                        + e.getMessage());
            } catch (InterruptedException e) {
                // should not happen
                e.printStackTrace();
            }
        }
        try {
            assoc.waitForDimseRSP();
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    public boolean commit() {
        DicomObject actionInfo = new BasicDicomObject();
        actionInfo.putString(Tag.TransactionUID, VR.UI, UIDUtils.createUID());
        DicomElement refSOPSq = actionInfo.putSequence(Tag.ReferencedSOPSequence);
        for (int i = 0, n = files.size(); i < n; ++i) {
            FileInfo info = files.get(i);
            if (info.transferred) {
                BasicDicomObject refSOP = new BasicDicomObject();
                refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, info.cuid);
                refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI, info.iuid);
                refSOPSq.addDicomObject(refSOP);
            }
        }
        try {
            DimseRSP rsp = assoc.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance, STG_CMT_ACTION_TYPE,
                actionInfo, UID.ImplicitVRLittleEndian);
            rsp.next();
            DicomObject cmd = rsp.getCommand();
            int status = cmd.getInt(Tag.Status);
            if (status == 0) {
                return true;
            }
            System.err.println(
                    "WARNING: Storage Commitment request failed with status: "
                    + StringUtils.shortToHex(status) + "H");
            System.err.println(cmd.toString());
        } catch (NoPresentationContextException e) {
            System.err.println("WARNING: " + e.getMessage()
                    + " - cannot request Storage Commitment");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(
                    "ERROR: Failed to send Storage Commitment request: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
        return false;
    }
    
    private String selectTransferSyntax(String[] available, String tsuid) {
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
            return selectTransferSyntax(available, IVLE_TS);
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
            return selectTransferSyntax(available, EVLE_TS);
        if (tsuid.equals(UID.ExplicitVRBigEndian))
            return selectTransferSyntax(available, EVBE_TS);
        for (int j = 0; j < available.length; j++)
            if (available[j].equals(tsuid))
                return tsuid;
        return null;
    }

    private String selectTransferSyntax(String[] available, String[] tsuids) {
        for (int i = 0; i < tsuids.length; i++)
            for (int j = 0; j < available.length; j++)
                if (available[j].equals(tsuids[i]))
                    return available[j];
        return null;
    }

    public void close() {
        try {
            assoc.release(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final class FileInfo {
        File f;

        String cuid;

        String iuid;

        String tsuid;

        long fmiEndPos;

        long length;
        
        boolean transferred;

        public FileInfo(File f) {
            this.f = f;
            this.length = f.length();
        }
                
    }

    private class DataWriter implements org.dcm4che2.net.DataWriter {

        private FileInfo info;

        public DataWriter(FileInfo info) {
            this.info = info;
        }

        public void writeTo(PDVOutputStream out, String tsuid)
                throws IOException {
            if (tsuid.equals(info.tsuid)) {
                FileInputStream fis = new FileInputStream(info.f);
                try {
                    long skip = info.fmiEndPos;
                    while (skip > 0)
                        skip -= fis.skip(skip);
                    out.copyFrom(fis);
                } finally {
                    fis.close();
                }
            } else if (tsuid.equals(DCM4CHEE_URI_REFERENCED_TS_UID)) {
                DicomObject attrs;
                DicomInputStream dis = new DicomInputStream(info.f);
                try {
                    dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                    attrs = dis.readDicomObject();
                } finally {
                    dis.close();
                }
                DicomOutputStream dos = new DicomOutputStream(out);
                attrs.putString(Tag.RetrieveURI, VR.UT, info.f.toURI().toString());
                dos.writeDataset(attrs, tsuid);
             } else {
                DicomInputStream dis = new DicomInputStream(info.f);
                try {
                    DicomOutputStream dos = new DicomOutputStream(out);
                    dos.setTransferSyntax(tsuid);
                    TranscoderInputHandler h = new TranscoderInputHandler(dos,
                            transcoderBufferSize);
                    dis.setHandler(h);
                    dis.readDicomObject();
                } finally {
                    dis.close();
                }
            }
        }

    }

    private void promptErrRSP(String prefix, int status, FileInfo info,
            DicomObject cmd) {
        System.err.println(prefix + StringUtils.shortToHex(status) + "H for "
                + info.f + ", cuid=" + info.cuid + ", tsuid=" + info.tsuid);
        System.err.println(cmd.toString());
        this.error  += prefix + " " + cmd.toString() + "\n";
    }

    private void onDimseRSP(DicomObject cmd) {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        FileInfo info = null;
        try {
        	info = files.get(msgId - 1);
        } catch (Exception e) {
        	info = files.get(0);
		}
        switch (status) {
        case 0:
            info.transferred = true;
            totalSize += info.length;
            ++filesSent;
            System.out.print('.');
            logger.info("DICOM-SENT for file " + (info!=null?info.f:"") + " SUCCESSFUL");
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            info.transferred = true;
            totalSize += info.length;
            ++filesSent;
            promptErrRSP("WARNING: Received RSP with Status ", status, info,
                    cmd);
            System.out.print('W');
            break;
        default:
            promptErrRSP("ERROR: Received RSP with Status ", status, info, cmd);
            System.out.print('F');
        }
    }
    
    @Override
    protected synchronized void onNEventReportRSP(Association as, int pcid,
            DicomObject rq, DicomObject info, DicomObject rsp) {
        notifyAll();
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }
    
    private static KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DicomSender.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }
    
    
  public DicomSender(DcmURL url) {
      this.url = url;
      
      remoteAE.setInstalled(true);
      remoteAE.setAssociationAcceptor(true);
      remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

      device.setNetworkApplicationEntity(ae);
      device.setNetworkConnection(conn);
      ae.setNetworkConnection(conn);
      ae.setAssociationInitiator(true);
      ae.setAssociationAcceptor(true);
      ae.register(this);
      ae.setAETitle("DCMSND");

	setCalling(url.getCallingAET());
	setCalledAET(url.getCalledAET());
	setRemoteHost(url.getHost());
	setRemotePort(url.getPort());
	
  }

public boolean send(File file) throws Exception {

    addFile(file);
    
    configureTransferCapability();

	start();
	open();	    
	
	int filesToSend = getNumberOfFilesToSend();
	send();
	int filesSend = getNumberOfFilesSent();

	close();
	stop();


	if (filesToSend == filesSend) {
	    logger.info("SENDING of file " + file.getAbsolutePath() + " to " + url.toString() + " done");	    
	    this.files.clear();
	    this.filesSent=0;
	} else {
	    logger.warn("FAILED to send file " + file.getAbsolutePath() + " to " + url.toString());
	    this.files.clear();
	    this.filesSent=0;
	    return false;
	}
	
	
	return true;
	
}


public String getUrl() {
	return this.url.toString();
}

public String getError() {
	return this.error;
}

public static void main(String[] args) {
	try {
		
		DcmURL url = new DcmURL("dicom://OSIRIX:test@10.14.1.8:5104");
		if (args.length == 1)
			try {
				url = new DcmURL(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
			DicomSender dSender = new DicomSender(url);
			
			if (dSender.send(new File("/home/hermenj/im1"))) {
			    System.out.println("sending successfull");
			} else {
			    System.out.println("sending failed");
			}
			
	} catch (Exception e) {
		e.printStackTrace();
	}
}
    
}



    
