package lu.tudor.santec.dicom.dicomdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import lu.tudor.santec.dicom.gui.header.DicomHeader;
import lu.tudor.santec.dicom.receiver.DICOMListener;
import lu.tudor.santec.dicom.receiver.DicomEvent;
import lu.tudor.santec.dicom.receiver.DicomStorageServer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;


/**
 * imports dicom images and header values into a database. 
 *
 * @author Johannes Hermen johannes.hermen(at)tudor.lu
 *
 * @version
 * <br>$Log: DicomHeaderDBImporter.java,v $
 * <br>Revision 1.6  2010-06-18 14:36:39  hermen
 * <br>updated dcm4che
 * <br>
 * <br>Revision 1.5  2009-12-11 11:00:44  hermen
 * <br>*** empty log message ***
 * <br>
 * <br>Revision 1.4  2009-11-25 13:52:30  hermen
 * <br>before modification
 * <br>
 * <br>Revision 1.1  2009-07-23 13:17:04  hermen
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
public class DicomHeaderDBImporter implements DICOMListener {
    
    	public static final String SOFTWARE_VERSION = "0.1";

    
	private File propertyFile;
	private File filtertagFile;
	
	private String DB_URL;
	private String DB_USER;
	private String DB_PASSWORD;
        private String DB_DRIVER_CLASS;
	private String DICOM_AET_NAME;
	private Integer DICOM_PORT;
	private String DICOM_STORE_DIR;
	private String ROOT_DIR;
	private String ERROR_DIR;
	private String OK_DIR;
	private boolean DELETE_FILES_AFTER_IMPORT;
        private boolean CHECKSUM;
        private boolean CHECK_RECONSTRUCTION;
        
        private long used_files;
        private long imported_files;
	
	private LinkedHashMap<String, String> dicomTags = new LinkedHashMap<String, String>();
        private LinkedHashMap<String, String> filterTags = new LinkedHashMap<String, String>();

        public enum DICOM{ SERIES, OBJECT, TAGS }
	
	private Queue<File> images2import = new ConcurrentLinkedQueue<File>();
	
	private File LOGFILE = new File("DicomHeaderDBImporter.log");

        int itemOrder = 1;
	
	/**
	 * static logger for this class
	 */
	private static Logger logger = Logger.getLogger(DicomHeaderDBImporter.class
		.getName());
	
	
	/**
	 * @param propertyFile
	 */
	public DicomHeaderDBImporter(File propertyFile, File filtertagFile) {
		this.propertyFile = propertyFile;
		this.filtertagFile = filtertagFile;
		
		try {
    		    // create an appender that creates a new logfile at midnight, 
    		    // old file is renamed to LOGFILE.yyyy-MM-dd
    		    Layout layout = new TTCCLayout("ISO8601");
    		    DailyRollingFileAppender fileAppender = new DailyRollingFileAppender(
    			    layout,
    			    LOGFILE.getAbsolutePath(),
    		    	"'.'yyyy-MM-dd");
    		    
    		    BasicConfigurator.configure(fileAppender);
    		    
//    		    Logger.getRootLogger().addAppender(new ConsoleAppender(layout));
    		    
		    	// set loglevel for dcm4che lib to warn (as not rest is not interresting)
		    	Logger.getLogger("org.dcm4che2").setLevel(Level.WARN);
			
		    	// set log level for own classes to info
		    	logger.setLevel(Level.INFO);
			logger.info("Added logging to file: "	+ LOGFILE);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		readConfig();
                readFilterTags();
		
		//prepareStatements();
		try {
		    DicomStorageServer storageServer = new DicomStorageServer( 
			    this.DICOM_AET_NAME,
			    this.DICOM_PORT,
			    this.DICOM_STORE_DIR,
			    false,
			    "xxxxx",
			    false);
		    
		    storageServer.addDICOMListener(this);
		    
		} catch (Exception e) {
		    logger.error(e.getLocalizedMessage());
		}
		
		// create 15 importing threads
		for (int i = 0; i < 5; i++) {
		    createDBImporterThread();
		}
		
		
	}
	
	private void createDBImporterThread() {
		// create a new thread that checks the queue and imports the images if available
		new Thread() {
		    public void run() {
                        try {
                            Connection dbCon = getNewDatabaseConnection();
                            PreparedStatement insertDCMSeriesStmt = prepareStatement( DICOM.SERIES, dbCon );
                            PreparedStatement insertDCMObjectStmt = prepareStatement( DICOM.OBJECT, dbCon );
                            PreparedStatement insertDCMTagsStmt = prepareStatement( DICOM.TAGS, dbCon );
                            
                            while (true) {
                                try {
                                    // get file frm queue
                                    File f = images2import.poll();
                                    if (f != null) {
                                        // import file
                                        insertFile2DB( f, insertDCMSeriesStmt, insertDCMObjectStmt, insertDCMTagsStmt, dbCon );
                                    } else {
                                        // no image in queue -> wait 500ms and try again
                                        Thread.sleep(500);
                                    }
                                } catch (Exception e) {
                                    logger.error("error importing images...", e);
                                }
                            }
                        } catch (SQLException ex) {
                            java.util.logging.Logger.getLogger(DicomHeaderDBImporter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }
		    }
		}.start();
	}

	/**
	 * returns an instance of the configured database connection
	 * @return
	 * @throws java.sql.SQLException
	 */
	private Connection getNewDatabaseConnection() throws java.sql.SQLException {

            // The database connection:
            try {
                Class.forName(this.DB_DRIVER_CLASS);

                Connection dbConnection = DriverManager.getConnection(
                		this.DB_URL,
                		this.DB_USER,
                                this.DB_PASSWORD);
                dbConnection.setAutoCommit( false );
                return dbConnection;
            } catch (java.lang.ClassNotFoundException io) {
    			System.err.println("unable to connecto to DB: " + this.DB_URL);
    			logger.warn("unable to connecto to DB: " + this.DB_URL);
    			io.printStackTrace();
                        return null;
            }
        }
	
	/**
	 * read the config file
	 */
	private void readConfig() {
		Properties properties = new Properties();
		InputStream in;
		if (propertyFile != null && propertyFile.canRead()) {
			try {
				in = new FileInputStream(propertyFile);
			} catch (FileNotFoundException e) {
				in = DicomHeaderDBImporter.class.getResourceAsStream("DicomHeaderDBImporter.properties");
			}
		} else {
			in = DicomHeaderDBImporter.class.getResourceAsStream("DicomHeaderDBImporter.properties");
		}
		try {
			properties.load(in);
		} catch (Exception e) {
			System.err.println("no usable Settings found");
			logger.warn("no usable Settings found");
			e.printStackTrace();
			System.exit(-1);
		}
		
		this.ROOT_DIR=properties.getProperty("ROOT_DIR");
                
		this.ERROR_DIR=this.ROOT_DIR + File.separator +properties.getProperty("ERROR_DIR");
		new File(this.ERROR_DIR).mkdirs();
		this.OK_DIR=this.ROOT_DIR + File.separator +properties.getProperty("OK_DIR");
		new File(this.OK_DIR).mkdirs();
		String str = properties.getProperty("DELETE_FILES_AFTER_IMPORT");
		if (str.toUpperCase().equals("TRUE") || str.toUpperCase().equals("YES"))
			this.DELETE_FILES_AFTER_IMPORT=true;
		else 
			this.DELETE_FILES_AFTER_IMPORT=false;
		this.DICOM_AET_NAME = properties.getProperty("DICOM_AET_NAME");
		this.DICOM_PORT = new Integer(properties.getProperty("DICOM_PORT"));
		this.DICOM_STORE_DIR = this.ROOT_DIR + File.separator + properties.getProperty("DICOM_STORE_DIR");
		this.DB_DRIVER_CLASS  = properties.getProperty("DB_DRIVER_CLASS");
		this.DB_URL = properties.getProperty("DB_URL");
		this.DB_USER = properties.getProperty("DB_USER");
		this.DB_PASSWORD  = properties.getProperty("DB_PASSWORD");
                
                String strChecksum = properties.getProperty("CHECKSUM");
                if ( strChecksum.toUpperCase().equals("TRUE") || strChecksum.toUpperCase().equals("YES") )
                    this.CHECKSUM=true;
                else
                    this.CHECKSUM=false;
                
                String strCheckRecon = properties.getProperty("CHECK_RECONSTRUCTION");
                if ( strCheckRecon.toUpperCase().equals("TRUE") || strCheckRecon.toUpperCase().equals("YES") )
                    this.CHECK_RECONSTRUCTION=true;
                else
                    this.CHECK_RECONSTRUCTION=false;

		for (Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext();) {
			String param = (String) iter.next();
			if (param.startsWith("00")) {
				dicomTags.put(param, properties.getProperty(param));
			}
		}
	}

        /**
         * read Tags for filtering
         */
        private void readFilterTags(){
            Properties properties = new Properties();
            InputStream in;
            if (filtertagFile != null && filtertagFile.canRead()) {
                    try {
                            in = new FileInputStream(filtertagFile);
                    } catch (FileNotFoundException e) {
                            in = DicomHeaderDBImporter.class.getResourceAsStream("filtertags.properties");
                    }
            } else {
                    in = DicomHeaderDBImporter.class.getResourceAsStream("filtertags.properties");
            }
            try {
                    properties.load(in);
            } catch (Exception e) {
                    System.err.println("no filtertags found");
                    logger.warn("no filtertags found");
                    e.printStackTrace();
                    System.exit(-1);
            }

            for (Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext();) {
                String param = (String) iter.next();
                filterTags.put(param, properties.getProperty(param));
            }
        }
	
	/**
	 * prepare the insert statements
	 */
	private PreparedStatement prepareStatement(DICOM dicom, Connection con) {

            PreparedStatement stmt = null;
            try {

                switch( dicom ){

                    case SERIES:    StringBuffer statement = new StringBuffer();
                                    statement.append("INSERT INTO dcmseries (");
                                    for (Iterator<String> iter = dicomTags.values().iterator(); iter.hasNext();) {
                                            String column = (String) iter.next();
                                            statement.append(column +",");
                                    }
                                    statement.deleteCharAt(statement.length()-1); // delete the comma at last position
                                    statement.append(") VALUES (");
                                    for (int i = 0; i < dicomTags.size()-1; i++) {
                                        statement.append("?,");
                                    }
                                    statement.append("?);");
                                    return con.prepareStatement(statement.toString());

                    case OBJECT:    return con.prepareStatement(
                                        "INSERT INTO dcmobject (seriesuid, sopinstanceuid, entrydate, checksumimage, dbimporterversion)" +
                                            "VALUES (?,?,?,?,?);"
                                    );

                    case TAGS:      return con.prepareStatement(
                                        "INSERT INTO dcmtag (dcmobject_id,tag,vr,value,itemorder,parentid) VALUES (?,?,?,?,?,?);"
                                    );
                }
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
            return stmt;
	}
	
	/**
         * insert the given file into the database
         * @param f
         * @param insertDCMSeriesStmt
         * @param insertDCMObjectStmt
         * @param insertDCMTagsStmt
         * @param currentDbConnection
         */
	private void insertFile2DB(File f,
                                   PreparedStatement insertDCMSeriesStmt,
                                   PreparedStatement insertDCMObjectStmt,
                                   PreparedStatement insertDCMTagsStmt,
                                   Connection currentDbConnection) {
	    
	    logger.info(" \r\n\r\n" +
			"#######################################################\r\n" +
			"#\r\n" + 
			"#  STARTING IMPORT FOR " + f.getName() + "\r\n" +
			"#\r\n" + 		
			"#######################################################");

	    	used_files++;
		
		long start = System.currentTimeMillis();

                boolean importFile = true;


		try {
		    DicomHeader dh = new DicomHeader(f);
		    DicomObject dcmObjectHeader = dh.getDicomObject();


                    // if a rule to filter exist -> no import
                    if ( !filterTags.isEmpty() ){
                        for (Iterator<String> iter = filterTags.keySet().iterator(); iter.hasNext();) {
                            String tag = (String) iter.next();

                            // check comma separated filter e. g. 0008,0060=MR, UR, NM, US
                            if( filterTags.get(tag).contains(",") ){
                                String[] splittedValue = filterTags.get(tag).split(", ");
                                for( int i=0; i<splittedValue.length; i++ ){
                                    if( splittedValue[i].equals(dh.getHeaderStringValue(tag)) ){
                                        importFile=false;
                                        logger.info("NOT importing File, matches Filtertags: " + filterTags.get(tag));
                                        break;
                                    }
                                }
                            } else if ( dh.getHeaderStringValue(tag).equals(filterTags.get(tag)) ){
                                importFile=false;
                                logger.info("NOT importing File, matches Filtertags: " + filterTags.get(tag));
                                break;
                            }
                        }
                    }

                    // filter the reconstruction-images
                    if( importFile && CHECK_RECONSTRUCTION){
                	importFile = checkRecontruction(currentDbConnection, dh);
                    }
                    
                    if ( importFile ){

                        // insert dcmSeries if it not exists
                	if (! seriesExists(currentDbConnection, dh)) {
                	    try{
                		int i = 1; // index for database-fields in series
                		for (Iterator<String> iter = dicomTags.keySet().iterator(); iter.hasNext(); i++) {
                		    String tag = (String) iter.next();
                		    String VR = DicomHeader.getHeaderFieldType(tag);
                		    
                		    // if Date
                		    if ("DA".equals(VR)) {
                			try {
                			    Date d = dh.getHeaderDateValue(tag);
                			    insertDCMSeriesStmt.setDate(i, new java.sql.Date(d.getTime()));
                			} catch (Exception e) {
                			    insertDCMSeriesStmt.setDate(i, null);
                			}
                			// if Time
                		    } else if ("TM".equals(VR)) {
                			try {
                			    Date d = dh.getHeaderDateValue(tag);
                			    insertDCMSeriesStmt.setTime(i, new java.sql.Time(d.getTime()));
                			} catch (Exception e) {
                			    insertDCMSeriesStmt.setTime(i, null);
                			}
                			// else String
                		    } else {
                			String s = dh.getHeaderStringValue(tag);
                			s = s.replaceAll("\0", "");
                			insertDCMSeriesStmt.setString(i, s);
                		    }
                		}
                		insertDCMSeriesStmt.execute();
                		
                		
                		// Exception if Seriesuid exist
                	    } catch ( SQLException sqle ){
                		 if (sqle.getMessage().indexOf("duplicate key") < 0)  {
                             	logger.error("Error adding series: " , sqle);                        	
                                 } else {
                             	logger.warn("Series allready in DB");                        	
                                 }       	
                		currentDbConnection.rollback();
                	    }
                	}

                        // insert the dicom-object and the tags
                        try{
                            int j = 1; // index for database-fields in dicom-objects
                            insertDCMObjectStmt.setString( j++, dh.getDicomObject().getString(Tag.SeriesInstanceUID) );
                            insertDCMObjectStmt.setString( j++, dh.getDicomObject().getString(Tag.SOPInstanceUID) );
                            insertDCMObjectStmt.setTimestamp(j++, new Timestamp(System.currentTimeMillis()));

                            // storing the md5 of pixel-data in database, if CHECKSUM is true
                            if ( CHECKSUM ){
                                DicomInputStream dis = new DicomInputStream(f);
                                DicomObject dcmObj = new BasicDicomObject();
                                dis.readDicomObject(dcmObj, -1);
                                DicomElement de = dcmObj.get(Tag.PixelData);
                                byte [] bytes = de.getBytes();

                                MessageDigest md = MessageDigest.getInstance("MD5");
                                md.update(bytes);
                                byte[] md5 = md.digest();
                                BigInteger bi=new BigInteger(1, md5);
                                insertDCMObjectStmt.setString(j++, bi.toString(16));
                            }else{
                                insertDCMObjectStmt.setString(j++, null);
                            }

                            insertDCMObjectStmt.setString(j++, SOFTWARE_VERSION);
                            insertDCMObjectStmt.execute();
                            
//                            currentDbConnection.commit();
                            
                            int parentID = getDCMObjectID(currentDbConnection, dh.getDicomObject().getString(Tag.SOPInstanceUID));

                            itemOrder = 1;
                            // recursive insert of dicom-tags
                            insertDCMTagsStmt.clearBatch();
                            insertDicomTags(parentID, dcmObjectHeader, null, insertDCMTagsStmt);
                            insertDCMTagsStmt.executeBatch();

                            currentDbConnection.commit();

                            logger.info("File inserted");
                            imported_files++;
                        } catch ( SQLException sqle ){
                            currentDbConnection.rollback();
                            if (sqle.getMessage().indexOf("duplicate key") < 0)  {
                        	logger.error("Error importing image: " , sqle);                        	
                            } else {
                        	logger.warn("File with UID=" + dh.getDicomObject().getString(Tag.SOPInstanceUID) + " allready in db");                        	
                            }
                        }
                    }

                    if (DELETE_FILES_AFTER_IMPORT) {
                            f.delete();
                    } else {
                        boolean success = f.renameTo(new File(OK_DIR, f.getName()));
                        if (success) {
                                    logger.info("moved file to: "  + OK_DIR + File.separator + f.getName());
                        } else {
                            logger.warn("unable to move file to: "  + OK_DIR + File.separator + f.getName());
                        }
                    }

                } catch (Exception e) {
			boolean success = f.renameTo(new File(ERROR_DIR, f.getName()));
			logger.log(Level.WARN, "Failed to insert file: " + f.getAbsolutePath() + " " + e.getMessage(), e);
        		if (success) {
        		    	logger.info("moved file to: "  + ERROR_DIR + File.separator + f.getName());
        		} else {
        		    	logger.warn("unable to move file to: "  + ERROR_DIR + File.separator + f.getName());
        		}
        		try {
        			currentDbConnection.rollback();
        		} catch (SQLException e1) {
        			logger.log(Level.ERROR, e1.getMessage(), e1);
        		}
                }
		
		logger.info("END OF IMPORT FOR " + f.getName() + " took " + (System.currentTimeMillis()-start)+ "µsec. \r\n" + 
			"Files used: " + used_files + "  Files imported: " + imported_files + " \r\n" +
			"#######################################################");
	}
	
	private void insertDicomTags(int dcmobject_id,
                                     DicomObject obj,
                                     Integer parentId,
                                     PreparedStatement stmt) throws Exception {

            for (Iterator<DicomElement> it = obj.iterator(); it.hasNext();) {
                DicomElement e = it.next();

                StringBuffer sb = new StringBuffer();
                    try {
                        String[] values = e.getStrings(obj.getSpecificCharacterSet(),false);
                        sb.append(values[0].replaceAll("\0", ""));
                        int i;
                        for ( i = 1; i < values.length; i++) {
                            sb.append("/").append(values[i].replaceAll("\0", ""));
                        }

                    } catch (Exception ee) {
                        try {
                            sb.append(e.getString(obj.getSpecificCharacterSet(),false));
                        } catch (Exception e2) {

                        }
                    }
                stmt.setInt(1, dcmobject_id);
                stmt.setString(2, DicomHeader.toTagString(e.tag()));
                stmt.setString(3, e.vr().toString());

                // only the first 150 chars are saved if the tag-value is longer
                if ( sb.length() > 150 ){
                    stmt.setString(4, sb.toString().substring(0, 150).concat("*** end of tag-value not applicable ***"));
                }else{
                    stmt.setString(4, sb.toString());
                }

                stmt.setInt(5, itemOrder);
                if (parentId == null) {
                    stmt.setNull(6, Types.INTEGER);
                } else {
                    stmt.setInt(6, parentId);
                }
                stmt.addBatch();
                //System.out.println( stmt );
                
                itemOrder++;
                if (e.countItems() > 0) {
                    if (e.hasDicomObjects()) {
                        for (int i = 0; i < e.countItems(); i++) {
                            insertDicomTags(dcmobject_id, e.getDicomObject(i), itemOrder-1, stmt);
                        }
                    }
                }
            }
	}
	
	
	boolean checkRecontruction(Connection dbConnection, DicomHeader dh) throws Exception{
            String sqlQueryFilterReconstructions = new String(
                    "SELECT seriesinstanceuid FROM dcmseries " +
                            "WHERE '" + dh.getHeaderStringValue(Tag.StudyInstanceUID) + "' = studyinstanceuid " +
                            "AND '" + dh.getHeaderStringValue(Tag.SeriesInstanceUID) + "' <> seriesinstanceuid " +
                            "AND '" + dh.getHeaderStringValue(Tag.AcquisitionNumber) + "' = seriesnumber " +
                            "OR '" + dh.getHeaderStringValue(Tag.StudyInstanceUID) + "' = studyinstanceuid " +
                            "AND '" + dh.getHeaderStringValue(Tag.SeriesInstanceUID) + "' <> seriesinstanceuid " +
                            "AND '" + dh.getHeaderStringValue(Tag.AcquisitionNumber) + "' = acquisitionnumber; "
                );
                ResultSet rs = dbConnection.createStatement().executeQuery(sqlQueryFilterReconstructions);
                if ( rs.next() ) {
                    return false;
                }else {
                    logger.info("NOT importing File, is recontruction of existing...");
                    return true;                    
                }
	}
	
	
	private boolean seriesExists(Connection dbConnection, DicomHeader dh) throws Exception{
	    String findseries = new String(
                    "SELECT seriesinstanceuid FROM dcmseries " +
                            "WHERE seriesinstanceuid= '" +  dh.getHeaderStringValue(Tag.SeriesInstanceUID)  + "' ;"
                );
                ResultSet rs = dbConnection.createStatement().executeQuery(findseries);
                if ( rs.next() ) {
                    return true;                    
                } else {
                    return false;
                }
	}
	
	private int getDCMObjectID(Connection dbConnection, String uid) throws Exception{
	    String findDCMObjectID = new String(
                    "SELECT id FROM dcmobject " +
                            "WHERE sopinstanceuid= '" +  uid  + "' ;"
                );
//	    System.out.println(uid);
//	    String findDCMObjectID = new String("SELECT currval('dcmobject_id_seq') as val;");
                ResultSet rs = dbConnection.createStatement().executeQuery(findDCMObjectID);
                rs.next();
                return rs.getInt("id");
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		File confFile = null;
		File filterFile = null;
		try {
			confFile = new File(args[0]);
			filterFile = new File(args[1]);
		} catch (Exception e) {
		}
		
		new DicomHeaderDBImporter(confFile, filterFile);
		
	}


	public void fireDicomEvent(DicomEvent event) {	
		// add files to queue
		images2import.add(event.getFile());
	    
//	    insertFile2DB(event.getFile());
		
	}

}
