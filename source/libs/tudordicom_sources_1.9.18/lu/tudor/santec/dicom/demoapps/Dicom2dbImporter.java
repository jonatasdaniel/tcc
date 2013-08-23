package lu.tudor.santec.dicom.demoapps;

import ij.ImagePlus;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.receiver.DICOMListener;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import lu.tudor.santec.dicom.receiver.DicomStorageServer;

/**
 * imports dicom images and header values into a database. 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: Dicom2dbImporter.java,v $
 * <br>Revision 1.5  2010-06-18 14:36:39  hermen
 * <br>updated dcm4che
 * <br>
 * <br>Revision 1.4  2009-07-23 13:17:04  hermen
 * <br>added dicom db
 * <br>
 * <br>Revision 1.3  2009-07-14 09:28:13  hermen
 * <br>fixed toString to handle String[]
 * <br>
 * <br>Revision 1.2  2009-01-19 15:31:30  hermen
 * <br>fixed exporting
 * <br>
 * <br>Revision 1.1  2009-01-08 14:20:09  hermen
 * <br>*** empty log message ***
 * <br>
 *
 */
public class Dicom2dbImporter implements DICOMListener {
	private File propertyFile;
	
	@SuppressWarnings("unchecked")
	private LinkedHashMap dicomTags = new LinkedHashMap();

	private String DB_URL;
	private String DB_USER;
	private String DB_PASSWORD;
	private String DB_TABLE;
	private String DB_DRIVER_CLASS;
	private String IMAGE_COLUMN; 
	private String THUMB_COLUMN;
	private String FILESIZE_COLUMN;
	private String DICOM_AET_NAME;
	private Integer DICOM_PORT;
	private String DICOM_STORE_DIR;
	private String DICOM_MANAGE_PASS;
	private Connection dbConnection;
	private String ROOT_DIR;
	private String ERROR_DIR;
	private String OK_DIR;
	private boolean DELETE_FILES_AFTER_IMPORT;
	private int thumbsize = 300;
	private File LOGFILE = new File("Dicom2dbImporter.log");
	
	// the logger for this class
	private Logger logger = Logger
			.getLogger("lu.tudor.santec.dicom.dbimporter.DBImporter");
	
	
	public Dicom2dbImporter(File propertyFile) {
		this.propertyFile = propertyFile;
		
		try {
			Handler fh = new FileHandler(LOGFILE.getAbsolutePath(), 1024*1024*2, 5, true);
			fh.setFormatter(new SimpleFormatter());
			Logger.getLogger("").addHandler(fh);
			Logger.getLogger("").setLevel(Level.INFO);
			logger.info("Added logging to file: "	+ LOGFILE);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		readConfig();
		
		
		try {
		    DicomStorageServer storageServer = new DicomStorageServer( 
			    this.DICOM_AET_NAME,
			    this.DICOM_PORT,
			    this.DICOM_STORE_DIR,
			    false,
			    this.DICOM_MANAGE_PASS,
			    false);
		    
		    storageServer.addDICOMListener(this);
			storageServer.start();
			logger.info("Dicom2dbImporter started");
			System.out.println("Dicom2dbImporter started");
		} catch (IOException e) {
			System.err.println("Starting the server failed.......");
			logger.warning("Starting the server failed.......");
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
public Connection getDatabaseConnection() throws java.sql.SQLException {
        
    	if (this.dbConnection != null &&  ! this.dbConnection.isClosed()) {
    		return this.dbConnection;
    	}
    	
        // The database connection:
        try {
            dbConnection = null;
            Class.forName(this.DB_DRIVER_CLASS);
            
            dbConnection = DriverManager.getConnection(
            		this.DB_URL, 
            		this.DB_USER,
                    this.DB_PASSWORD);
            return dbConnection;
        } catch (java.lang.ClassNotFoundException io) {
			System.err.println("unable to connecto to DB: " + this.DB_URL);
			logger.warning("unable to connecto to DB: " + this.DB_URL);
			io.printStackTrace();
        }
        return null;
        
    }
	
	
	@SuppressWarnings("unchecked")
	private void readConfig() {
		Properties properties = new Properties();
		InputStream in;
		if (propertyFile != null && propertyFile.canRead()) {
			try {
				in = new FileInputStream(propertyFile);
			} catch (FileNotFoundException e) {
				in = Dicom2dbImporter.class.getResourceAsStream("DBImporter.properties");
			}
		} else {
			in = Dicom2dbImporter.class.getResourceAsStream("DBImporter.properties");
		}
		try {
			properties.load(in);
		} catch (Exception e) {
			System.err.println("no usable Settings found");
			logger.warning("no usable Settings found");
			e.printStackTrace();
			System.exit(-1);
		}
		
		this.ROOT_DIR=properties.getProperty("ROOT_DIR");
		this.ERROR_DIR=this.ROOT_DIR + File.separator +properties.getProperty("ERROR_DIR");
		new File(this.ERROR_DIR).mkdirs();
		this.OK_DIR=this.ROOT_DIR + File.separator +properties.getProperty("OK_DIR");
		new File(this.OK_DIR).mkdirs();
		String str = properties.getProperty("DICOM_AET_NAME");
		if (str.toUpperCase().equals("TRUE") || str.toUpperCase().equals("YES"))
			this.DELETE_FILES_AFTER_IMPORT=true;
		else 
			this.DELETE_FILES_AFTER_IMPORT=false;
		this.DICOM_AET_NAME = properties.getProperty("DICOM_AET_NAME");
		this.DICOM_PORT = new Integer(properties.getProperty("DICOM_PORT"));
		this.DICOM_STORE_DIR = this.ROOT_DIR + File.separator + properties.getProperty("DICOM_STORE_DIR");
		this.DICOM_MANAGE_PASS = properties.getProperty("DICOM_MANAGE_PASS");
		this.DB_DRIVER_CLASS = properties.getProperty("DB_DRIVER_CLASS");
		this.DB_URL  = properties.getProperty("DB_URL");
		this.DB_USER = properties.getProperty("DB_USER");
		this.DB_PASSWORD  = properties.getProperty("DB_PASSWORD");
		this.DB_TABLE = properties.getProperty("DB_TABLE");
		this.IMAGE_COLUMN = properties.getProperty("IMAGE_COLUMN");
		this.THUMB_COLUMN = properties.getProperty("THUMB_COLUMN");
		this.FILESIZE_COLUMN = properties.getProperty("FILESIZE_COLUMN");
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
			String param = (String) iter.next();
			if (param.startsWith("00")) {
				dicomTags.put(param, properties.getProperty(param));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void insertFile2DB(File f) {
		
		long start = System.currentTimeMillis();
		
		StringBuffer statement = new StringBuffer("INSERT INTO " + this.DB_TABLE + " ( ");
		for (Iterator iter = dicomTags.values().iterator(); iter.hasNext();) {
			String column = (String) iter.next();
			statement.append(column +", ");
		}
		statement.append(this.IMAGE_COLUMN +", ");
		statement.append(this.THUMB_COLUMN +", ");
		statement.append(this.FILESIZE_COLUMN +" ");

		statement.append(" ) VALUES (");
		for (int i = 0; i < dicomTags.keySet().size(); i++) {
			statement.append("?, ");	
		}
		statement.append("?, ");
		statement.append("?, ");
		statement.append("?);");
		
		System.out.println(statement.toString());
		
		PreparedStatement prepstat;
		try {
			prepstat = getDatabaseConnection().prepareStatement(statement.toString());
		
			ImagePlus ip = new ImagePlus(f.getAbsolutePath());
			DicomHeader dh = new DicomHeader(f);
			InputStream fIn = new FileInputStream(f);
			
			byte[] thumbnail = createThumb(ip);
			
			FileOutputStream fOut = new FileOutputStream("thumbnail.png");
			fOut.write(thumbnail);
			fOut.close();
			
			int i = 1;
			// set the dicom tags
			for (Iterator iter = dicomTags.keySet().iterator(); iter.hasNext();) {
				String tag = (String) iter.next();
				prepstat.setString(i, dh.getHeaderStringValue(tag));
				i++;
			}
			// set the image
			prepstat.setBinaryStream(i++, fIn , (int)f.length());
			// set the thumbnail
			prepstat.setBytes(i++, thumbnail);
			// set the filesize
			prepstat.setString(i, f.length()+"");
			
			prepstat.addBatch();
			prepstat.executeUpdate();
		
			logger.info("File inserted: " + f.getAbsolutePath());
			logger.info("insert took: " + (System.currentTimeMillis()-start));
			boolean success = f.renameTo(new File(OK_DIR, f.getName()));
			if (DELETE_FILES_AFTER_IMPORT) {
				f.delete();
			} else {
				if (success) {
					logger.info("moved file to: "  + OK_DIR + File.separator + f.getName());
				} else {
					logger.warning("unable to move file to: "  + OK_DIR + File.separator + f.getName());
				}				
			}
		} catch (Exception e) {
			boolean success = f.renameTo(new File(ERROR_DIR, f.getName()));
			logger.warning("Failed to insert file: " + f.getAbsolutePath() + " " + e.getLocalizedMessage());
		    if (success) {
		    	logger.info("moved file to: "  + ERROR_DIR + File.separator + f.getName());
		    } else {
		    	logger.warning("unable to move file to: "  + ERROR_DIR + File.separator + f.getName());
		    }
		} 
		
		logger.info("END OF IMPORT FOR " + f.getName() + "\r\n" + 		
			"#######################################################");
		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File f = null;
		try {
			f = new File(args[0]);
		} catch (Exception e) {
		}
		
		new Dicom2dbImporter(f);
		
	}


	public void fireDicomEvent(DicomEvent event) {
		logger.warning(" \r\n\r\n" +
				"#######################################################\r\n" +
				"#\r\n" + 
				"#  STARTING IMPORT FOR " + event.getFile().getName() + "\r\n" +
				"#\r\n" + 		
				"#######################################################");
		insertFile2DB(event.getFile());
	}
	
	private byte[] createThumb(ImagePlus imp) throws Exception {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		int width = imp.getWidth();
		int  height = imp.getHeight();
		ImageIcon i;
		if (width > height)
			i = new ImageIcon(imp.getImage().getScaledInstance(thumbsize, -1, Image.SCALE_SMOOTH));
		else
			i= new ImageIcon(imp.getImage().getScaledInstance(-1, thumbsize, Image.SCALE_SMOOTH));
		
		BufferedImage bi = new BufferedImage(i.getIconWidth(), i.getIconHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = (Graphics2D)bi.getGraphics();
		if (width > height)
			g.drawImage(i.getImage(), 0, 0, null);
		else
			g.drawImage(i.getImage(), 0, 0, null);
		ImageIO.write(bi, "png", bOut);
		bi = null;
		return bOut.toByteArray();
    }

}
