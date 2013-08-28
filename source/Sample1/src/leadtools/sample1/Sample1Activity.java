package leadtools.sample1;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Sample1Activity extends Activity {
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
   }

   private void displayAlert(String msg) {
      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      alert.setMessage(msg).setPositiveButton("OK", null).show();
   }
   
   public void onRun(View v) {
      try {
         new LoadTask().execute("");
      } catch(Exception ex) {
         displayAlert(ex.getMessage());
      }
   }
   
   class LoadTask extends AsyncTask<String, Void, Bitmap> {
      private ProgressDialog mDialog;
      
      @Override
      protected void onPreExecute () {
         mDialog = ProgressDialog.show(Sample1Activity.this, "", "Running");
      }
      
      private HttpGet getHttpGet(String url) {
         return new HttpGet(url);
      }

      private HttpPost getHttpPost(String url, Map<?,?> map) throws UnsupportedEncodingException {
         HttpPost httpost = new HttpPost(url);
         
         JSONObject holder = new JSONObject(map);

         StringEntity se = new StringEntity(holder.toString());

         httpost.setEntity(se);

         httpost.setHeader("Content-type", "application/json");
         
         return httpost;
      }

      protected Bitmap doInBackground(String... urls) {
          try {
             BasicResponseHandler responseHandler = new BasicResponseHandler();
             HttpClient httpclient = new DefaultHttpClient();
             HttpPost httpost = null;
             HttpGet httpget = null;
             HttpResponse httpResponse = null;
             HttpEntity resEntity = null;
             String authenticationToken = null;
             
          // ************* Authentication Token ***************
             Map<String, String> authenMap = new HashMap<String, String>();
             //"{\"userName\":\"guest\",\"password\":\"guest\",\"userData\":\"\"}"
             authenMap.put("userName", "guest");
             authenMap.put("password", "guest");
             authenMap.put("userData", "");
             
             httpost = getHttpPost("http://demo.leadtools.com/MedicalViewerService/AuthenticationService.svc/AuthenticateUser", authenMap);

             authenticationToken = httpclient.execute(httpost, responseHandler);
          // ###********** Authentication Token ************###

          // ************* Find Studies ***************           
             Map<String, String> patientsOptionsMap = new HashMap<String, String>();
             patientsOptionsMap.put("PatientID", "");
             patientsOptionsMap.put("PatientName", "");

             JSONArray madalityArray = new JSONArray("[]");
             
             Map<String, Object> studiesOptionsMap = new HashMap<String, Object>();
             studiesOptionsMap.put("AccessionNumber", "");
             studiesOptionsMap.put("ReferDoctorName", "");
             studiesOptionsMap.put("ModalitiesInStudy", madalityArray);

             Map<String, Object> findStudiesOptionsMap = new HashMap<String, Object>();
             findStudiesOptionsMap.put("PatientsOptions", new JSONObject(patientsOptionsMap));
             findStudiesOptionsMap.put("StudiesOptions", new JSONObject(studiesOptionsMap));
             
             Map<String, Object> findStudiesMap = new HashMap<String, Object>();
             //"{\"authenticationCookie\":\"eILeu9FnOgs9N/xUj8okGA==\",\"options\":{\"PatientsOptions\":{\"PatientID\":\"\",\"PatientName\":\"\"},\"StudiesOptions\":{\"AccessionNumber\":\"\",\"ReferDoctorName\":\"\",\"ModalitiesInStudy\":[]}}}"
             findStudiesMap.put("authenticationCookie", authenticationToken);
             findStudiesMap.put("options", new JSONObject(findStudiesOptionsMap));
             
             httpost = getHttpPost("http://demo.leadtools.com/MedicalViewerService/ObjectQueryService.svc/FindStudies", findStudiesMap);

             String studySearchResults = httpclient.execute(httpost, responseHandler);
          // ###********** Find Studies ************###

             JSONArray studiesArray = new JSONArray(studySearchResults);
             if(studiesArray.length() < 1)
                return null;

             // Now you can create a list with the the resulted "studiesArray"
             /*for (int i = 0; i < studiesArray.length(); i++) {
                JSONObject object = studiesArray.getJSONObject(i);
                // [{"AccessionNumber":"","AdditionalPatientHistory":null,"AdmittingDiagnosesDescription":null,"Age":null,"Date":"1\/1\/1970 12:00:00 AM","Description":"BRAIN IAC","Id":null,"InstanceUID":"0.0.0.0.2.373.920663105","ModalitiesInStudy":null,"NameOfDoctorsReading":null,"NumberOfRelatedInstances":0,"NumberOfRelatedSeries":0,"Patient":{"BirthDate":"10\/12\/1962 12:00:00 AM","Comments":null,"ID":"1982367","Name":"John Doe","NumberOfRelatedInstances":null,"NumberOfRelatedSeries":null,"NumberOfRelatedStudies":null,"Sex":"M"},"ReferringPhysiciansName":"Dr. Well","Size":null,"Weight":null},
                String instanceUID = object.getString("InstanceUID");
                String accessionNumber = object.getString("AccessionNumber");
             }*/
          // ************* Find Series ***************
          // ------ Find the series for the first study-----
             String studyInstanceUID = studiesArray.getJSONObject(0).getString("InstanceUID");
             Map<String, Object> findSeriesOptionsMap = new HashMap<String, Object>();
             findSeriesOptionsMap.put("StudiesOptions", new JSONObject(String.format("{\"StudyInstanceUID\":\"%s\"}", studyInstanceUID)));
             
             Map<String, Object> findSeriesMap = new HashMap<String, Object>();
             //"{\"authenticationCookie\":\"eILeu9FnOgs9N/xUj8okGA==\",\"options\":{\"StudiesOptions\":{\"StudyInstanceUID\":\"1.2.124.1233532.10.3.0.82.200814266.201.1521383254\"}}}"
             findSeriesMap.put("authenticationCookie", authenticationToken);
             findSeriesMap.put("options", new JSONObject(findSeriesOptionsMap));
             
             httpost = getHttpPost("http://demo.leadtools.com/MedicalViewerService/ObjectQueryService.svc/FindSeries", findSeriesMap);

             String selectedStudySeriesResult = httpclient.execute(httpost, responseHandler);
          // ###********** Find Series ************###
             
             JSONArray seriesArray = new JSONArray(selectedStudySeriesResult);
             if(studiesArray.length() < 1)
                return null;
             
             String seriesID = seriesArray.getJSONObject(0).getString("InstanceUID");
          // ************* Get Selected Series Presentation Info ***************
          // ------ Get the presentation info for the first series -----
             httpget = getHttpGet(String.format("http://demo.leadtools.com/MedicalViewerService/ObjectRetrieveService.svc/GetPresentationInfo?auth=%s&series=%s", authenticationToken, seriesID));             
             // Send the request and read the response
             httpResponse = httpclient.execute(httpget);
             
             resEntity = httpResponse.getEntity();
             
             String info = EntityUtils.toString(resEntity);
          // ###********** Get Selected Series Info ************###
          
             JSONArray infoArray = new JSONArray(info);
             if(studiesArray.length() < 1)
                return null;

             JSONObject sopInfoObj = infoArray.getJSONObject(0);
             String sopInstanceUID = sopInfoObj.getString("SOPInstanceUID");
             
          // ************* Get the images (First Frame) ***************             
             int frameNumber = 1;
             InputStream in = new URL(String.format("http://demo.leadtools.com/MedicalViewerService/ObjectRetrieveService.svc/GetImage?auth=%s&instance=%s&frame=%d&cx=%d&cy=%d", authenticationToken, sopInstanceUID, frameNumber, 1024, 1024)).openStream();          
          // ###********** Get the images url (First Frame) ************###
              return BitmapFactory.decodeStream(in);
          } catch (Exception e) {
              return null;
          }
      }

      protected void onPostExecute(Bitmap bm) {
         mDialog.dismiss();
         if(bm == null) {
            displayAlert("Ann Error Occured");
         } else {
            ImageView imgView = (ImageView) findViewById(R.id.imageview1);
            imgView.setImageBitmap(bm);
         }
      }
   }
}