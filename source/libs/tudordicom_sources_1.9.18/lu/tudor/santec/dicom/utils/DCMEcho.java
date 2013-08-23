package lu.tudor.santec.dicom.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dcm4che.util.DcmURL;
import org.dcm4che2.net.Status;
import org.dcm4che2.tool.dcmecho.DcmEcho;

public class DCMEcho {
	
	/**
	 * static logger for this class
	 */
	private static Logger log = Logger.getLogger(DCMEcho.class.getName());

	public static int sendEcho(DcmURL url) throws Exception {

	    DcmEcho dcmecho = new DcmEcho("SantecEcho");
	    if (url.getCallingAET() != null)
	    	dcmecho.setCalling(url.getCallingAET());
	    dcmecho.setCalledAET(url.getCalledAET(), false);
	    dcmecho.setRemoteHost(url.getHost());
	    dcmecho.setRemotePort(url.getPort());
	    
	    log.info("sending echo to: " + url.toString());
	    try {
		dcmecho.open();
		dcmecho.echo();
		dcmecho.close();
	    } catch (Exception e) {
		log.log(Level.WARN, e.getLocalizedMessage(), e);
		throw e;
	    } 
	    
	    log.log(Level.FATAL, "DCMEcho succeeded "+ url);
	    return Status.Success;
	}
	
	
	public static String checkConnection(String host, int port) {
		StringBuffer report = new StringBuffer("TCP Connection Report: \n");
		try {
			report.append("   Proxy for " + host + " is: " +  DCMEcho.getProxyForURL("http://"+host+":"+port) + "\n");
			
			report.append("   connecting to: " + host + "\n");
			InetAddress addr = InetAddress.getByName(host);
			
			if (addr.isReachable(4)) {
				report.append("      " + host + " is reachable\n");
			} else {
				report.append("      " + host + " is NOT reachable\n");
				return report.toString();
			}
			
			report.append("   connecting to port: " + port + " on " + host + "\n");
			SocketAddress sockaddr = new InetSocketAddress(addr, port);
			Socket  socket = new Socket();
			socket.connect(sockaddr, 4);
			report.append("      port " + port + " on " + host + " is reachable\n");
		} catch (Throwable e) {
			report.append("      " + e.getClass().getSimpleName());
			report.append("      " + e.getMessage());
		}

		return report.toString();
	}

	
	public static void main(String[] args) {
		try {
			
			DcmURL url = new DcmURL("dicom://SANTEC:test@localhost:5104");
			if (args.length == 1)
				try {
					url = new DcmURL(args[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				System.out
						.println("DCM echo to " + url + " "
								+ (DCMEcho.sendEcho(url) == Status.Success ? "Succeeded"
										: "Failed"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getProxyForURL(String url) {
		String proxyAddr = "NONE";
		try {
			List<Proxy> l = ProxySelector.getDefault().select(new URI(url));
			if (l != null) {
				for (Proxy proxy : l) {
					InetSocketAddress addr = (InetSocketAddress) proxy.address();
					if (addr == null) {
						System.out.println("No Proxy for " + url);
					} else {
						System.out.println("proxy hostname : " + addr.getHostName());
						System.out.println("proxy port : " + addr.getPort());
						proxyAddr = proxy.toString();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error loading proxies " + e.getMessage());
		}
		return proxyAddr;
	}
}