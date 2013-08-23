package lu.tudor.santec.dicom.receiver;
/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
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
 *  
 *  changed / extended by Johannes Hermen Santec/Tudor www.santec.tudor.lu
 *                                                                           *
 *****************************************************************************/

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.tool.dcmrcv.DcmRcv;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 1.25 $ $Date: 2013-04-02 07:35:04 $
 * @since Oct 13, 2005
 */
public class DicomStorageServer extends StorageService {

    /**
     * static logger for this class
     */
    private static Logger log = Logger.getLogger(DicomStorageServer.class
	    .getName());
    
    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private static final String[] ONLY_DEF_TS = { UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_TS = { UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_LE_TS = { UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian };

    private static final String[] NON_RETIRED_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian,
            UID.JPEGBaseline1, UID.JPEGExtended24, UID.JPEGLSLossyNearLossless,
            UID.JPEG2000, UID.MPEG2, };

    private static final String[] NON_RETIRED_LE_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
            UID.JPEGLSLossyNearLossless, UID.JPEG2000, UID.MPEG2, };

    private static final String[] CUIDS = {
            UID.BasicStudyContentNotificationSOPClassRetired,
            UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired,
            UID.HardcopyColorImageStorageSOPClassRetired,
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraOralXRayImageStorageForPresentation,
            UID.DigitalIntraOralXRayImageStorageForProcessing,
            UID.StandaloneModalityLUTStorageRetired,
            UID.EncapsulatedPDFStorage, UID.StandaloneVOILUTStorageRetired,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
            UID.ColorSoftcopyPresentationStateStorageSOPClass,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass,
            UID.XRayAngiographicImageStorage, UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage, UID.EnhancedXRFImageStorage,
            UID.XRayAngiographicBiPlaneImageStorageRetired,
            UID.PositronEmissionTomographyImageStorage,
            UID.StandalonePETCurveStorageRetired, UID.CTImageStorage,
            UID.EnhancedCTImageStorage, UID.NuclearMedicineImageStorage,
            UID.UltrasoundMultiFrameImageStorageRetired,
            UID.UltrasoundMultiFrameImageStorage, UID.MRImageStorage,
            UID.EnhancedMRImageStorage, UID.MRSpectroscopyStorage,
            UID.RTImageStorage, UID.RTDoseStorage, UID.RTStructureSetStorage,
            UID.RTBeamsTreatmentRecordStorage, UID.RTPlanStorage,
            UID.RTBrachyTreatmentRecordStorage,
            UID.RTTreatmentSummaryRecordStorage,
            UID.NuclearMedicineImageStorageRetired,
            UID.UltrasoundImageStorageRetired, UID.UltrasoundImageStorage,
            UID.RawDataStorage, UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage, UID.RealWorldValueMappingStorage,
            UID.SecondaryCaptureImageStorage,
            UID.MultiFrameSingleBitSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiFrameTrueColorSecondaryCaptureImageStorage,
            UID.VLImageStorageTrialRetired, UID.VLEndoscopicImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VLMicroscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage, UID.VideoPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.StereometricRelationshipStorage,
            UID.VLMultiFrameImageStorageTrialRetired,
            UID.StandaloneOverlayStorageRetired, UID.BasicTextSRStorage,
            UID.EnhancedSRStorage, UID.ComprehensiveSRStorage,
            UID.ProcedureLogStorage, UID.MammographyCADSRStorage,
            UID.KeyObjectSelectionDocumentStorage,
            UID.ChestCADSRStorage, UID.XRayRadiationDoseSRStorage,
            UID.EncapsulatedPDFStorage, UID.EncapsulatedCDAStorage,
            UID.StandaloneCurveStorageRetired,
            UID.TwelveLeadECGWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage, UID.HangingProtocolStorage,
            UID.SiemensCSANonImageStorage,
            UID.Dcm4cheAttributesModificationNotificationSOPClass };

    private Executor executor = new NewThreadExecutor("DCMRCV");

    private Device device = new Device("DCMRCV");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection nc = new NetworkConnection();

    private String[] tsuids = NON_RETIRED_LE_TS;

    private File destination;

    private boolean devnull;

    private int fileBufferSize = 256;

    private int rspdelay = 0;

    private String keyStoreURL = "resource:tls/test_sys_2.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET;

    private boolean allowDoubles;

    private Integer port;

    private String password;

    private Vector<DICOMListener> dicomListeners = new Vector<DICOMListener>();

    private String AETName;

    private String storeDir;

    private boolean isRunning;

    private boolean useDICOMDIR;

    private DicomDirReader dicomDirWriter;

	private boolean stripImagesToHeaders = false; 


	public DicomStorageServer(String AETName, Integer port, String storeDir,
    		boolean allowDoubles, String managePassword, boolean startup) throws IOException {
    	this(AETName, port, storeDir, allowDoubles, managePassword, startup, false);
    }
    
    public DicomStorageServer(String AETName, Integer port, String storeDir,
		boolean allowDoubles, String managePassword, boolean startup, boolean stripImagesToHeaders) throws IOException {
	super(CUIDS);
	
	this.stripImagesToHeaders = stripImagesToHeaders;
	this.allowDoubles = allowDoubles;
	this.port = port;
	this.password = managePassword;
	this.AETName = AETName;
	this.storeDir = storeDir;
	
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
        ae.register(this);

        ae.setAETitle(AETName);
        nc.setPort(port);
        
        initTransferCapability();
        
        if (storeDir.toUpperCase().endsWith("DICOMDIR")) {
            useDICOMDIR = true;
            File store = new File(storeDir);
            store.getParentFile().mkdirs();
            dicomDirWriter = new DicomDirReader();
            dicomDirWriter.loadDicomDirFile(store, true, false);
            setDestination(new File(storeDir).getParent());
        } else {
            setDestination(storeDir);
        }
        
        if (startup)
            start();
	
    }

    public final void setHostname(String hostname) {
        nc.setHostname(hostname);
    }

    public final void setTlsWithoutEncyrption() {
        nc.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        nc.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        nc.setTlsAES_128_CBC();
    }
    
    public final void disableSSLv2Hello() {
        nc.disableSSLv2Hello();
    }
    
    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        nc.setTlsNeedClientAuth(needClientAuth);
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
        
    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout) {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay) {
        nc.setSocketCloseDelay(delay);
    }

    public final void setIdleTimeout(int timeout) {
        ae.setIdleTimeout(timeout);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setMaxPDULengthSend(int maxLength) {
        ae.setMaxPDULengthSend(maxLength);
    }

    public void setMaxPDULengthReceive(int maxLength) {
        ae.setMaxPDULengthReceive(maxLength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        nc.setSendBufferSize(bufferSize);
    }

    private void setDimseRspDelay(int delay) {
        rspdelay = delay;
    }

    private void setTransferSyntax(String[] tsuids) {
        this.tsuids = tsuids;
    }

    private void initTransferCapability() {
        TransferCapability[] tc = new TransferCapability[CUIDS.length + 1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i + 1] = new TransferCapability(CUIDS[i], tsuids,
                    TransferCapability.SCP);
        ae.setTransferCapability(tc);
    }

    private void setFileBufferSize(int size) {
        fileBufferSize = size;
    }

    private void setMaxOpsPerformed(int maxOps) {
        ae.setMaxOpsPerformed(maxOps);
    }

    private void setDestination(String filePath) {
        this.destination = new File(filePath);
        this.devnull = "/dev/null".equals(filePath);
        if (!devnull)
            destination.mkdir();
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
            return DcmRcv.class.getClassLoader().getResourceAsStream(
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
    
    public void start() throws IOException {
        device.startListening(executor);
        System.out.println("Start Server listening on port " + nc.getPort());
        this.openManagePort();
        isRunning = true;
    }
    
    public void stop() throws IOException {
        device.stopListening();
        System.out.println("Stopped Server listening on port " + nc.getPort());
        nc.unbind();
        isRunning = false;
    }

    private static String[] split(String s, char delim, int defPos) {
        String[] s2 = new String[2];
        s2[defPos] = s;
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmrcv -h' for more information.");
        System.exit(1);
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

    /** Overwrite {@link StorageService#cstore} to send delayed C-STORE RSP 
     * by separate Thread, so reading of following received C-STORE RQs from
     * the open association is not blocked.
     */
	@Override
	public void cstore(final Association as, final int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid) throws DicomServiceException, IOException {
		final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
		onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
		if (rspdelay > 0) {
			executor.execute(new Runnable() {
				public void run() {
					try {
						Thread.sleep(rspdelay);
						as.writeDimseRSP(pcid, rsp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} else {
			as.writeDimseRSP(pcid, rsp);
		}
		onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
	}

    
    @Override
	protected void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp) throws IOException, DicomServiceException {
		if (destination == null) {
			super.onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
			fireDicomEvent(new DicomEvent(this.destination));
		} else {
			try {
				String cuid = rq.getString(Tag.AffectedSOPClassUID);
				String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
				BasicDicomObject fmi = new BasicDicomObject();
				fmi.initFileMetaInformation(cuid, iuid, tsuid);

				if (destination.isFile()) {
					destination = destination.getParentFile();
				} else if (!destination.exists()) {
					destination.mkdirs();
				}

				File file = devnull ? destination : new File(destination, iuid);
				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream bos = new BufferedOutputStream(fos, fileBufferSize);
				DicomOutputStream dos = new DicomOutputStream(bos);
				dos.writeFileMetaInformation(fmi);
				dataStream.copyTo(dos);
				dos.close();

				if (stripImagesToHeaders) {
					try {
						// read orig file until pixel data
						DicomInputStream in;
						in = new DicomInputStream(file);
						in.setHandler(new StopTagInputHandler(Tag.PixelData));
						DicomObject dcmobj = in.readDicomObject();
						in.close();
						// delete orig
						file.delete();
						// write header to file
						FileOutputStream fos2 = new FileOutputStream(file);
						BufferedOutputStream bos2 = new BufferedOutputStream(fos2);
						DicomOutputStream dos2 = new DicomOutputStream(bos2);
						dos2.writeDicomFile(dcmobj);
						dos2.close();
					} catch (Exception e) {
						log.log(Level.WARNING, "error stripping file :" + file.getAbsolutePath());
					}
				}
				if (useDICOMDIR) {
					File newfile = new File(dicomDirWriter.append(file));
					if (file.exists() && !file.getAbsolutePath().equals(newfile.getAbsolutePath()))
						file.delete();
					fireDicomEvent(new DicomEvent(this.destination, newfile, DicomEvent.ADD));
				} else {
					fireDicomEvent(new DicomEvent(this.destination, file, DicomEvent.ADD));
				}

			} catch (IOException e) {
				throw new DicomServiceException(rq, Status.ProcessingFailure, e.getMessage());
			}
		}
	}

	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * opens a manage port that allows to stop the server
	 */
	public void openManagePort() {
		Thread t = new Thread() {
			public void run() {
				try {
					ServerSocket s = new ServerSocket(port.intValue() + 1);
					log.info("openend new DicomServerManageConnection at: "
							+ s.getLocalPort());
					while (true) {
						Socket incoming = s.accept();
						handleSocket(incoming);
						incoming.close();
					}
				} catch (Exception e) {

				}
			}

			public void handleSocket(Socket incoming) {
				try {
					incoming.setSoTimeout(20000); // 20 seconds
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(incoming.getInputStream()));
					OutputStreamWriter writer = new OutputStreamWriter(incoming
							.getOutputStream());
					log.info("openend new DicomServerManageConnection at: "
							+ incoming.getPort());
					String compName =  "unKnown";
					String compAddress =  "127.0.0.1";
					try{
                        compName =  InetAddress.getLocalHost().getHostName();
                        compAddress =  InetAddress.getAllByName(compName)[0].toString();
					}
					catch(Exception e){
					}
					// accept only from local address
					if (incoming.getInetAddress().toString().equals(
							incoming.getLocalAddress().toString())) {
						writer.write("Welcome to DicomStorageServer@"+compAddress+" [enter 'password:help' for more info]" + "\n");
						writer.flush();
					} else {
						writer
								.write("DicomStorageServer@"+compAddress+": only local connections allowed"
										+ "\n");
						writer.flush();
						return;
					}
					while (true) {
						String s = reader.readLine();
						String pass = s.split(":")[0];
						String cmd = s.split(":")[1];
						log.info("recieved |xxx:" + cmd + "| on "
								+ incoming.getLocalAddress() + " from "
								+ incoming.getInetAddress());
						// wrong password
						if (!pass.equals(password)) {
							writer.write("wrong password" + "\n");
							writer.flush();
						}
						// handle commands
						if (cmd.toLowerCase().equals("exit")) {
							try {
								writer.write("Server exiting" + "\n");
								writer.flush();								
							} catch (Exception e) {
							}
							DicomStorageServer.this.stop();
							log.info("Dicom Server exiting......");
							System.exit(0);
						} else if (cmd.toLowerCase().equals("info")) {
							writer.write("DicomStorageServer with AET '"
									+ AETName + "' on DICOMDIR: '" + storeDir
									+ "'\n");
							writer.flush();
						} else if (cmd.toLowerCase().equals("help")) {
							writer.write("password:info will show a small Server Info.\n");
							writer.write("password:exit will exit the Server.\n");
							writer.flush();
						}

					}
				} catch (SocketTimeoutException e) {
					log.info("DicomStorageServer closing manage connection (SocketTimeOut)");
				}catch (ArrayIndexOutOfBoundsException e) {
					log.info("DicomStorageServer closing manage connection (wrong command send)");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		t.start();

	}


	public void fireDicomEvent(DicomEvent event) {
//		System.out.println("DICOM UPDATE: " + event.toString());
		for (Iterator<DICOMListener> iter = dicomListeners.iterator(); iter.hasNext();) {
			DICOMListener listener = (DICOMListener) iter.next();
			listener.fireDicomEvent(event);
		}
	}
	
	public void addDICOMListener(DICOMListener dl) {
		dicomListeners.add(dl);
	}
	
	public void removeDICOMListener(DICOMListener dl) {
		dicomListeners.remove(dl);
	}
	
    
    public boolean getStripImagesToHeaders() {
		return stripImagesToHeaders;
	}

	public void setStripImagesToHeaders(boolean stripImagesToHeaders) {
		this.stripImagesToHeaders = stripImagesToHeaders;
	}

	public static void main(String[] args) {
		
		if (args.length != 4) {
			System.out.println("Please start with parameters AETitle PORT STOREAGEDIR STRIPTOHEADERS(true/false)");
			System.out.println("e.g. lu.tudor.santec.dicom.receiver.DicomStorageServer TUDOR 5104 DICOMSTORE/DICOMDIR false");
			System.out.println("Exiting....");
			System.exit(-1);
		}
	    
	    try {
		DicomStorageServer dss = new DicomStorageServer(
			args[0],
			Integer.parseInt(args[1]),
			args[2],
			true,
			"password",
			true,
			Boolean.parseBoolean(args[3])
		);
		
		dss.addDICOMListener(new DICOMListener() {
		    public void fireDicomEvent(DicomEvent event) {
			System.out.println("event: " + event);
		    }
		});
		
		System.out.println("DICOM STORE started....press CTRL-C to stop." );
		
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    
	}
}
