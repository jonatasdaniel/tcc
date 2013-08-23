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

package lu.tudor.santec.dicom.query;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.ExtQueryTransferCapability;
import org.dcm4che2.net.ExtRetrieveTransferCapability;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.DicomService;
import org.dcm4che2.net.service.StorageService;


/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 1.9 $ $Date: 2013-05-23 10:00:49 $
 * @since Jan, 2006
 */
public class DcmQR {

    /**
     * static logger for this class
     */
    private static Logger logger = Logger.getLogger(DcmQR.class.getName());
    
    private static final int KB = 1024;

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };

    public static enum QueryRetrieveLevel {
        PATIENT("PATIENT", PATIENT_RETURN_KEYS, PATIENT_LEVEL_FIND_CUID,
                PATIENT_LEVEL_GET_CUID, PATIENT_LEVEL_MOVE_CUID),
        STUDY("STUDY", STUDY_RETURN_KEYS, STUDY_LEVEL_FIND_CUID,
                STUDY_LEVEL_GET_CUID, STUDY_LEVEL_MOVE_CUID),
        SERIES("SERIES", SERIES_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
                SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID),
        IMAGE("IMAGE", INSTANCE_RETURN_KEYS, SERIES_LEVEL_FIND_CUID,
                SERIES_LEVEL_GET_CUID, SERIES_LEVEL_MOVE_CUID);
        
        private final String code;
        private final int[] returnKeys;
        private final String[] findClassUids;
        private final String[] getClassUids;
        private final String[] moveClassUids;

        private QueryRetrieveLevel(String code, int[] returnKeys,
                String[] findClassUids, String[] getClassUids,
                String[] moveClassUids) {
            this.code = code;
            this.returnKeys = returnKeys;
            this.findClassUids = findClassUids;
            this.getClassUids = getClassUids;
            this.moveClassUids = moveClassUids;
        }
        
        public String getCode() {
            return code;
        }

        public int[] getReturnKeys() {
            return returnKeys;
        }
        
        public String[] getFindClassUids() {
            return findClassUids;
        }
        
        public String[] getGetClassUids() {
            return getClassUids;
        }
        
        public String[] getMoveClassUids() {
            return moveClassUids;
        }
    }

    private static final String[] PATIENT_LEVEL_FIND_CUID = {
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    private static final String[] STUDY_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND,
        UID.PatientStudyOnlyQueryRetrieveInformationModelFINDRetired };

    private static final String[] SERIES_LEVEL_FIND_CUID = {
        UID.StudyRootQueryRetrieveInformationModelFIND,
        UID.PatientRootQueryRetrieveInformationModelFIND, };

    private static final String[] PATIENT_LEVEL_GET_CUID = {
        UID.PatientRootQueryRetrieveInformationModelGET,
        UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };
    
    private static final String[] STUDY_LEVEL_GET_CUID = {
        UID.StudyRootQueryRetrieveInformationModelGET,
        UID.PatientRootQueryRetrieveInformationModelGET,
        UID.PatientStudyOnlyQueryRetrieveInformationModelGETRetired };

    private static final String[] SERIES_LEVEL_GET_CUID = {
        UID.StudyRootQueryRetrieveInformationModelGET,
        UID.PatientRootQueryRetrieveInformationModelGET };

    private static final String[] PATIENT_LEVEL_MOVE_CUID = {
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] STUDY_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE,
        UID.PatientStudyOnlyQueryRetrieveInformationModelMOVERetired };

    private static final String[] SERIES_LEVEL_MOVE_CUID = {
        UID.StudyRootQueryRetrieveInformationModelMOVE,
        UID.PatientRootQueryRetrieveInformationModelMOVE };

    private static final int[] PATIENT_RETURN_KEYS = {
        Tag.PatientName,
        Tag.PatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.NumberOfPatientRelatedStudies,
        Tag.NumberOfPatientRelatedSeries,
        Tag.NumberOfPatientRelatedInstances };

    public static final int[] PATIENT_MATCHING_KEYS = { 
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex };

    private static final int[] STUDY_RETURN_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.StudyID,
        Tag.StudyInstanceUID,
        Tag.NumberOfStudyRelatedSeries,
        Tag.NumberOfStudyRelatedInstances };

    public static final int[] STUDY_MATCHING_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.ModalitiesInStudy,
        Tag.ReferringPhysicianName,
        Tag.StudyID,
        Tag.StudyInstanceUID };

    public static final int[] PATIENT_STUDY_MATCHING_KEYS = {
        Tag.StudyDate,
        Tag.StudyTime,
        Tag.AccessionNumber,
        Tag.ModalitiesInStudy,
        Tag.ReferringPhysicianName,
        Tag.PatientName,
        Tag.PatientID,
        Tag.IssuerOfPatientID,
        Tag.PatientBirthDate,
        Tag.PatientSex,
        Tag.StudyID,
        Tag.StudyInstanceUID };

    private static final int[] SERIES_RETURN_KEYS = {
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.NumberOfSeriesRelatedInstances };

    private static final int[] SERIES_MATCHING_KEYS = {
        Tag.Modality,
        Tag.SeriesNumber,
        Tag.SeriesInstanceUID,
        Tag.RequestAttributesSequence
    };

    private static final int[] INSTANCE_RETURN_KEYS = {
        Tag.InstanceNumber,
        Tag.SOPClassUID,
        Tag.SOPInstanceUID, };

    private static final int[] MOVE_KEYS = {
        Tag.QueryRetrieveLevel,
        Tag.PatientID,
        Tag.StudyInstanceUID,
        Tag.SeriesInstanceUID,
        Tag.SOPInstanceUID, };

    private static final String[] IVRLE_TS = {
        UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_LE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] NATIVE_BE_TS = {
        UID.ExplicitVRBigEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] DEFLATED_TS = {
        UID.DeflatedExplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] NOPX_TS = {
        UID.NoPixelData,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] NOPXDEFL_TS = {
        UID.NoPixelDataDeflate,
        UID.NoPixelData,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] JPLL_TS = {
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEGLSLossless,
        UID.JPEG2000LosslessOnly,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] JPLY_TS = {
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static final String[] MPEG2_TS = { UID.MPEG2 };

    private static final String[] DEF_TS = {
        UID.JPEGLossless,
        UID.JPEGLosslessNonHierarchical14,
        UID.JPEGLSLossless,
        UID.JPEGLSLossyNearLossless,
        UID.JPEG2000LosslessOnly,
        UID.JPEG2000,
        UID.JPEGBaseline1,
        UID.JPEGExtended24,
        UID.MPEG2,
        UID.DeflatedExplicitVRLittleEndian,
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian};

    private static enum TS {
        IVLE(IVRLE_TS),
        LE(NATIVE_LE_TS),
        BE(NATIVE_BE_TS),
        DEFL(DEFLATED_TS),
        JPLL(JPLL_TS),
        JPLY(JPLY_TS),
        MPEG2(MPEG2_TS),
        NOPX(NOPX_TS),
        NOPXD(NOPXDEFL_TS);
        
        final String[] uids;
        TS(String[] uids) { this.uids = uids; }
    }

    private static enum CUID {
        CR(UID.ComputedRadiographyImageStorage),
        CT(UID.CTImageStorage),
        MR(UID.MRImageStorage),
        US(UID.UltrasoundImageStorage),
        NM(UID.NuclearMedicineImageStorage),
        SC(UID.SecondaryCaptureImageStorage),
        XA(UID.XRayAngiographicImageStorage),
        XRF(UID.XRayRadiofluoroscopicImageStorage),
        DX(UID.DigitalXRayImageStorageForPresentation),
        MG(UID.DigitalMammographyXRayImageStorageForPresentation),
        PR(UID.GrayscaleSoftcopyPresentationStateStorageSOPClass),
        KO(UID.KeyObjectSelectionDocumentStorage),
        SR(UID.BasicTextSRStorage);

        final String uid;
        CUID(String uid) { this.uid = uid; }
        
    }

    private static final String[] EMPTY_STRING = {};

    private Executor executor = new NewThreadExecutor("DCMQR");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("DCMQR");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private Association assoc;

    private int priority = 0;

    private boolean cget; 

    private String moveDest;

    private File storeDest;

    private boolean devnull;

    private int fileBufferSize = 256;

    private boolean evalRetrieveAET = false;

    private QueryRetrieveLevel qrlevel = QueryRetrieveLevel.STUDY;

    private List<String> privateFind = new ArrayList<String>();

    private final List<TransferCapability> storeTransferCapability =
            new ArrayList<TransferCapability>(8);

    private DicomObject keys = new BasicDicomObject();

    private int cancelAfter = Integer.MAX_VALUE;

    private int completed;

    private int warning;

    private int failed;
    
    private String errorComment;

    private boolean relationQR;

    private boolean dateTimeMatching;

    private boolean fuzzySemanticPersonNameMatching;

    private boolean noExtNegotiation;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET;
    
    public DcmQR() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(true);
        ae.setAETitle("DCMQR");
    }

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

    public final void setCalledAET(String called, boolean reuse) {
        remoteAE.setAETitle(called);
        if (reuse)
            ae.setReuseAssocationToAETitle(new String[] { called });
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }

    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
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

    public final void setMaxOpsPerformed(int maxOps) {
        ae.setMaxOpsPerformed(maxOps);
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

    public final void setFileBufferSize(int size) {
        fileBufferSize = size;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        DcmQR dcmqr = new DcmQR();
        
        dcmqr.setCalledAET("OSIRIX", true);
        dcmqr.setRemoteHost("10.14.1.8");
        dcmqr.setRemotePort(5104);
        
        dcmqr.setQueryLevel(QueryRetrieveLevel.STUDY);
        
        dcmqr.addMatchingKey(Tag.PatientName, "Anonymous Male 19*");

        dcmqr.addReturnKey(Tag.Modality);

        dcmqr.configureTransferCapability(true);
        
        try {
            dcmqr.start();
            dcmqr.open();
            
            List<DicomObject> result = dcmqr.query();
            
//            dcmqr.move(result);
//            dcmqr.get(result);
            
            dcmqr.close();
            dcmqr.stop();
            
        } catch (Exception e) {

        }
    }

    private void addStoreTransferCapability(String cuid, String[] tsuids) {
        storeTransferCapability.add(new TransferCapability(
                cuid, tsuids, TransferCapability.SCP));
    }

    private void setEvalRetrieveAET(boolean evalRetrieveAET) {
       this.evalRetrieveAET = evalRetrieveAET;
    }

    private boolean isEvalRetrieveAET() {
        return evalRetrieveAET;
    }

    private void setNoExtNegotiation(boolean b) {
        this.noExtNegotiation = b;
    }

    private void setFuzzySemanticPersonNameMatching(boolean b) {
        this.fuzzySemanticPersonNameMatching = b;
    }

    private void setDateTimeMatching(boolean b) {
        this.dateTimeMatching = b;
    }

    private void setRelationQR(boolean b) {
        this.relationQR = b;
    }

    public final int getFailed() {
        return failed;
    }

    public final int getWarning() {
        return warning;
    }

    private final int getTotalRetrieved() {
        return completed + warning;
    }

    private void setCancelAfter(int limit) {
        this.cancelAfter = limit;
    }

    private void addMatchingKey(int[] tagPath, String value) {
        keys.putString(tagPath, null, value);
    }
    
    public void addMatchingKey(int tag, String value) {
        keys.putString(tag, null, value);
    }

    private void addReturnKey(int[] tagPath) {
        keys.putNull(tagPath, null);
    }
    
    void addReturnKey(int tag) {
        keys.putNull(tag, null);
    }

    public void configureTransferCapability(boolean ivrle) {
        String[] findcuids = qrlevel.getFindClassUids();
        String[] movecuids = moveDest != null ? qrlevel.getMoveClassUids()
                : EMPTY_STRING;
        String[] getcuids = cget ? qrlevel.getGetClassUids()
                : EMPTY_STRING;
        TransferCapability[] tcs = new TransferCapability[findcuids.length
                + privateFind.size() + movecuids.length + getcuids.length
                + storeTransferCapability.size()];
        int i = 0;
        for (String cuid : findcuids)
            tcs[i++] = mkFindTC(cuid, ivrle ? IVRLE_TS : NATIVE_LE_TS);
        for (String cuid : privateFind)
            tcs[i++] = mkFindTC(cuid, ivrle ? IVRLE_TS : DEFLATED_TS);
        for (String cuid : movecuids)
            tcs[i++] = mkRetrieveTC(cuid, ivrle ? IVRLE_TS : NATIVE_LE_TS);
        for (String cuid : getcuids)
            tcs[i++] = mkRetrieveTC(cuid, ivrle ? IVRLE_TS : NATIVE_LE_TS);
        for (TransferCapability tc : storeTransferCapability) {
            tcs[i++] = tc;
        }
        ae.setTransferCapability(tcs);
        if (!storeTransferCapability.isEmpty()) {
            ae.register(createStorageService());
        }
    }

    private DicomService createStorageService() {
        String[] cuids = new String[storeTransferCapability.size()];
        int i = 0;
        for (TransferCapability tc : storeTransferCapability) {
            cuids[i++] = tc.getSopClass();
        }
        return new StorageService(cuids) {
            @Override
            protected void onCStoreRQ(Association as, int pcid, DicomObject rq,
                    PDVInputStream dataStream, String tsuid, DicomObject rsp)
                    throws IOException, DicomServiceException {
                if (storeDest == null) {
                    super.onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
                } else {
                    try {
                        String cuid = rq.getString(Tag.AffectedSOPClassUID);
                        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
                        BasicDicomObject fmi = new BasicDicomObject();
                        fmi.initFileMetaInformation(cuid, iuid, tsuid);
                        File file = devnull ? storeDest : new File(storeDest, iuid);
                        FileOutputStream fos = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(fos,
                                fileBufferSize);
                        DicomOutputStream dos = new DicomOutputStream(bos);
                        dos.writeFileMetaInformation(fmi);
                        dataStream.copyTo(dos);
                        dos.close();
                    } catch (IOException e) {
                        throw new DicomServiceException(rq, Status.ProcessingFailure, e
                                .getMessage());
                    }
                }
            }
            
        };
    }

    private TransferCapability mkRetrieveTC(String cuid, String[] ts) {
        ExtRetrieveTransferCapability tc = new ExtRetrieveTransferCapability(
                cuid, ts, TransferCapability.SCU);
        tc.setExtInfoBoolean(
                ExtRetrieveTransferCapability.RELATIONAL_RETRIEVAL, relationQR);
        if (noExtNegotiation)
            tc.setExtInfo(null);
        return tc;
    }

    private TransferCapability mkFindTC(String cuid, String[] ts) {
        ExtQueryTransferCapability tc = new ExtQueryTransferCapability(cuid,
                ts, TransferCapability.SCU);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES,
                relationQR);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.DATE_TIME_MATCHING,
                dateTimeMatching);
        tc.setExtInfoBoolean(ExtQueryTransferCapability.FUZZY_SEMANTIC_PN_MATCHING,
                fuzzySemanticPersonNameMatching);
        if (noExtNegotiation)
            tc.setExtInfo(null);
        return tc;
    }

    public void setQueryLevel(QueryRetrieveLevel qrlevel) {
        this.qrlevel = qrlevel;
        keys.putString(Tag.QueryRetrieveLevel, VR.CS, qrlevel.getCode());
        for (int tag : qrlevel.getReturnKeys()) {
            keys.putNull(tag, null);
        }
    }

    public final void addPrivate(String cuid) {
        privateFind.add(cuid);
    }

    private void setCGet(boolean cget) {
        this.cget = cget;
    }

    private boolean isCGet() {
        return cget;
    }

    public void setMoveDest(String aet) {
        moveDest = aet;
    }

    private boolean isCMove() {
        return moveDest != null;
    }

    private static int toPort(String port) {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmqr -h' for more information.");
        System.exit(1);
    }

    public void start() throws IOException { 
        if (conn.isListening()) {
            conn.bind(executor );
            System.out.println("Start Server listening on port " + conn.getPort());
        }
    }

    public void stop() {
        if (conn.isListening()) {
            conn.unbind();
        }
    }

    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public List<DicomObject> query() throws IOException, InterruptedException {
    	errorComment = null;
        List<DicomObject> result = new ArrayList<DicomObject>();
        TransferCapability tc = selectFindTransferCapability();
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        if (tc.getExtInfoBoolean(ExtQueryTransferCapability.RELATIONAL_QUERIES)
                || containsUpperLevelUIDs(cuid)) {
            logger.info("Send Query Request using: \n" + keys + " to " + remoteAE.getAETitle() );
            DimseRSP rsp = assoc.cfind(cuid, priority, keys, tsuid, cancelAfter);
            while (rsp.next()) {
                DicomObject cmd = rsp.getCommand();
                if (CommandUtils.isPending(cmd)) {
                	if (cmd.getString(Tag.ErrorID) != null || cmd.getString(Tag.ErrorComment) != null ) 
                    	errorComment = cmd.getString(Tag.ErrorID) + " " + cmd.getString(Tag.ErrorComment);
                    DicomObject data = rsp.getDataset();
                    result.add(data);
                    logger.info("Query Response: " +Integer.valueOf(result.size()));
                }
            }
        } else {
            List<DicomObject> upperLevelUIDs = queryUpperLevelUIDs(cuid, tsuid);
            List<DimseRSP> rspList = new ArrayList<DimseRSP>(upperLevelUIDs.size());
            for (int i = 0, n = upperLevelUIDs.size(); i < n; i++) {
                upperLevelUIDs.get(i).copyTo(keys);
                logger.info("Send Query Request  \n" + keys + " to " + remoteAE.getAETitle() );
                rspList.add(assoc.cfind(cuid, priority, keys, tsuid, cancelAfter));
            }
            for (int i = 0, n = rspList.size(); i < n; i++) {
                DimseRSP rsp = rspList.get(i);
                for (int j = 0; rsp.next(); ++j) {
                    DicomObject cmd = rsp.getCommand();
                    if (CommandUtils.isPending(cmd)) {
                    	if (cmd.getString(Tag.ErrorID) != null || cmd.getString(Tag.ErrorComment) != null ) 
                        	errorComment = cmd.getString(Tag.ErrorID) + " " + cmd.getString(Tag.ErrorComment);
                        DicomObject data = rsp.getDataset();
                        result.add(data);
//                        logger.info("Query Response " + data );
                    }
                }
            }
        }
    	if (errorComment != null) {
    	    throw new IOException(errorComment);
    	}
        return result;
    }

    @SuppressWarnings("fallthrough")
    private boolean containsUpperLevelUIDs(String cuid) {
        switch (qrlevel) {
        case IMAGE:
            if (!keys.containsValue(Tag.SeriesInstanceUID)) {
                return false;
            }
            // fall through
        case SERIES:
            if (!keys.containsValue(Tag.StudyInstanceUID)) {
                return false;
            }
            // fall through
        case STUDY:
            if (Arrays.asList(PATIENT_LEVEL_FIND_CUID).contains(cuid)
                    && !keys.containsValue(Tag.PatientID)) {
                return false;
            }
            // fall through
        case PATIENT:
            // fall through
        }
        return true;
    }

    private List<DicomObject> queryUpperLevelUIDs(String cuid, String tsuid)
            throws IOException, InterruptedException {
        List<DicomObject> keylist = new ArrayList<DicomObject>();
        if (Arrays.asList(PATIENT_LEVEL_FIND_CUID).contains(cuid)) {
            queryPatientIDs(cuid, tsuid, keylist);
            if (qrlevel == QueryRetrieveLevel.STUDY) {
                return keylist;
            }
            keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist,
                    Tag.StudyInstanceUID, STUDY_MATCHING_KEYS, QueryRetrieveLevel.STUDY);
        } else {
            keylist.add(new BasicDicomObject());
            keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist,
                    Tag.StudyInstanceUID, PATIENT_STUDY_MATCHING_KEYS, QueryRetrieveLevel.STUDY);
        }
        if (qrlevel == QueryRetrieveLevel.IMAGE) {
            keylist = queryStudyOrSeriesIUIDs(cuid, tsuid, keylist,
                    Tag.SeriesInstanceUID, SERIES_MATCHING_KEYS, QueryRetrieveLevel.SERIES);
        }
        return keylist;
    }

    private void queryPatientIDs(String cuid, String tsuid,
            List<DicomObject> keylist) throws IOException, InterruptedException {
        String patID = keys.getString(Tag.PatientID);
        String issuer = keys.getString(Tag.IssuerOfPatientID);
        if (patID != null) {
            DicomObject patIdKeys = new BasicDicomObject();
            patIdKeys.putString(Tag.PatientID, VR.LO, patID);
            if (issuer != null) {
                patIdKeys.putString(Tag.IssuerOfPatientID, VR.LO, issuer);
            }
            keylist.add(patIdKeys);
        } else {
            DicomObject patLevelQuery = new BasicDicomObject();
            keys.subSet(PATIENT_MATCHING_KEYS).copyTo(patLevelQuery);
            patLevelQuery.putNull(Tag.PatientID, VR.LO);
            patLevelQuery.putNull(Tag.IssuerOfPatientID, VR.LO);
            patLevelQuery.putString(Tag.QueryRetrieveLevel, VR.CS, "PATIENT");
            logger.info("Send Query Request using " + patLevelQuery);
            DimseRSP rsp = assoc.cfind(cuid, priority, patLevelQuery, tsuid,
                    Integer.MAX_VALUE);
            for (int i = 0; rsp.next(); ++i) {
                DicomObject cmd = rsp.getCommand();
                if (CommandUtils.isPending(cmd)) {
                    DicomObject data = rsp.getDataset();
                    logger.info("Query Response \n" + data);
                    DicomObject patIdKeys = new BasicDicomObject();
                    patIdKeys.putString(Tag.PatientID, VR.LO,
                            data.getString(Tag.PatientID));
                    issuer = keys.getString(Tag.IssuerOfPatientID);
                    if (issuer != null) {
                        patIdKeys.putString(Tag.IssuerOfPatientID, VR.LO,
                                issuer);
                    }
                    keylist.add(patIdKeys);
                }
            }
        }
    }

    private List<DicomObject> queryStudyOrSeriesIUIDs(String cuid, String tsuid,
            List<DicomObject> upperLevelIDs, int uidTag, int[] matchingKeys,
            QueryRetrieveLevel qrLevel) throws IOException,
            InterruptedException {
        List<DicomObject> keylist = new ArrayList<DicomObject>();
        String uid = keys.getString(uidTag);
        for (DicomObject upperLevelID : upperLevelIDs) {
            if (uid != null) {
                DicomObject suidKey = new BasicDicomObject();
                upperLevelID.copyTo(suidKey);
                suidKey.putString(uidTag, VR.UI, uid);
                keylist.add(suidKey);
            } else {
                DicomObject keys2 = new BasicDicomObject();
                keys.subSet(matchingKeys).copyTo(keys2);
                upperLevelID.copyTo(keys2);
                keys2.putNull(uidTag, VR.UI);
                keys2.putString(Tag.QueryRetrieveLevel, VR.CS, qrLevel.getCode());
                logger.info("Send Query Request using " +keys2);
                DimseRSP rsp = assoc.cfind(cuid, priority, keys2,
                        tsuid, Integer.MAX_VALUE);
                for (int i = 0; rsp.next(); ++i) {
                    DicomObject cmd = rsp.getCommand();
                    if (CommandUtils.isPending(cmd)) {
                        DicomObject data = rsp.getDataset();
                        logger.info("Query Response: \n" +data);
                        DicomObject suidKey = new BasicDicomObject();
                        upperLevelID.copyTo(suidKey);
                        suidKey.putString(uidTag, VR.UI, data.getString(uidTag));
                        keylist.add(suidKey);
                    }
                }
            }
        }
        return keylist;
    }

    private TransferCapability selectFindTransferCapability()
            throws NoPresentationContextException {
        TransferCapability tc;
        if ((tc = selectTransferCapability(privateFind)) != null)
            return tc;
        if ((tc = selectTransferCapability(qrlevel.getFindClassUids())) != null)
            return tc;
        throw new NoPresentationContextException(UIDDictionary.getDictionary()
                .prompt(qrlevel.getFindClassUids()[0])
                + " not supported by " + remoteAE.getAETitle());
    }

    private String selectTransferSyntax(TransferCapability tc) {
        String[] tcuids = tc.getTransferSyntax();
        if (Arrays.asList(tcuids).indexOf(UID.DeflatedExplicitVRLittleEndian) != -1)
            return UID.DeflatedExplicitVRLittleEndian;
        return tcuids[0];
    }

//    public void move(String moveDest, List<DicomObject> findResults)
//            throws IOException, InterruptedException {
//        if (moveDest == null)
//            throw new IllegalStateException("moveDest == null");
//        System.out.println("sending move request to " + remoteAE.getAETitle());
//        TransferCapability tc = selectTransferCapability(qrlevel.getMoveClassUids());
//        if (tc == null)
//            throw new NoPresentationContextException(UIDDictionary
//                    .getDictionary().prompt(qrlevel.getMoveClassUids()[0])
//                    + " not supported by " + remoteAE.getAETitle());
//        String cuid = tc.getSopClass();
//        String tsuid = selectTransferSyntax(tc);
//        for (int i = 0, n = Math.min(findResults.size(), cancelAfter); i < n; ++i) {
//            DicomObject keys = findResults.get(i).subSet(MOVE_KEYS);
//            if (isEvalRetrieveAET()
//                    && moveDest.equals(findResults.get(i)
//                            .getString(Tag.RetrieveAETitle))) {
//                logger.info("Skipping "  +keys);
//            } else {
//                logger.info("Send Retrieve Request using " +keys);
//                assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
//            }
//        }
//        assoc.waitForDimseRSP();
//    }
    
    public void move(List<DicomObject> findResults) throws IOException,
	    InterruptedException {
    	errorComment = null;
	if (moveDest == null)
	    throw new IllegalStateException("moveDest == null");
	TransferCapability tc = selectTransferCapability(qrlevel
		.getMoveClassUids());
	if (tc == null)
	    throw new NoPresentationContextException(UIDDictionary
		    .getDictionary().prompt(qrlevel.getMoveClassUids()[0])
		    + " not supported by" + remoteAE.getAETitle());
	String cuid = tc.getSopClass();
	String tsuid = selectTransferSyntax(tc);
	for (int i = 0, n = Math.min(findResults.size(), cancelAfter); i < n; ++i) {
	    DicomObject keys = findResults.get(i).subSet(MOVE_KEYS);
	    if (isEvalRetrieveAET()
		    && moveDest.equals(findResults.get(i).getString(
			    Tag.RetrieveAETitle))) {
		 logger.info("Skipping "  +keys);
	    } else {
		logger.info("Send Retrieve Request using " +keys);
		assoc.cmove(cuid, priority, keys, tsuid, moveDest, rspHandler);
	
	    }
	    if (errorComment != null) {
		    throw new IOException(errorComment);
	    }
	}
	assoc.waitForDimseRSP();
	if (errorComment != null) {
	    throw new IOException(errorComment);
	}
    }


    public void get(List<DicomObject> findResults)
            throws IOException, InterruptedException {
        TransferCapability tc = selectTransferCapability(qrlevel.getGetClassUids());
        if (tc == null)
            throw new NoPresentationContextException(UIDDictionary
                    .getDictionary().prompt(qrlevel.getGetClassUids()[0])
                    + " not supported by" + remoteAE.getAETitle());
        String cuid = tc.getSopClass();
        String tsuid = selectTransferSyntax(tc);
        for (int i = 0, n = Math.min(findResults.size(), cancelAfter); i < n; ++i) {
            DicomObject keys = findResults.get(i).subSet(MOVE_KEYS);
            logger.info("Send Retrieve Request using " +keys);
            assoc.cget(cuid, priority, keys, tsuid, rspHandler);
        }
        assoc.waitForDimseRSP();
    }
    
    private final DimseRSPHandler rspHandler = new DimseRSPHandler() {
        @Override
        public void onDimseRSP(Association as, DicomObject cmd,
                DicomObject data) {
            DcmQR.this.onMoveRSP(as, cmd, data);
        }
    };


    protected void onMoveRSP(Association as, DicomObject cmd, DicomObject data) {
        if (!CommandUtils.isPending(cmd)) {
            completed += cmd.getInt(Tag.NumberOfCompletedSuboperations);
            warning += cmd.getInt(Tag.NumberOfWarningSuboperations);
            failed += cmd.getInt(Tag.NumberOfFailedSuboperations);
            if (cmd.getString(Tag.ErrorID) != null || cmd.getString(Tag.ErrorComment) != null ) 
            	errorComment = cmd.getString(Tag.ErrorID) + " " + cmd.getString(Tag.ErrorComment);
            
        }

    }

    private TransferCapability selectTransferCapability(String[] cuid) {
        TransferCapability tc;
        for (int i = 0; i < cuid.length; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid[i]);
            if (tc != null)
                return tc;
        }
        return null;
    }

    private TransferCapability selectTransferCapability(List<String> cuid) {
        TransferCapability tc;
        for (int i = 0, n = cuid.size(); i < n; i++) {
            tc = assoc.getTransferCapabilityAsSCU(cuid.get(i));
            if (tc != null)
                return tc;
        }
        return null;
    }

    public void close() throws InterruptedException {
        assoc.release(true);
    }

    private void setStoreDestination(String filePath) {
        this.storeDest = new File(filePath);
        this.devnull = "/dev/null".equals(filePath);
        if (!devnull)
            storeDest.mkdir();
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
            return DcmQR.class.getClassLoader().getResourceAsStream(
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
    
    public void reset() {
	keys = new BasicDicomObject();
    }

	/**
	 * @return the errorComment
	 */
	public String getErrorComment() {
		return errorComment;
	}
}