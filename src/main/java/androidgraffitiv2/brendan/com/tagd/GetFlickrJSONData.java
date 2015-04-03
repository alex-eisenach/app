package androidgraffitiv2.brendan.com.tagd;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Brendan on 4/1/15.
 */



public class GetFlickrJSONData extends GetRawData {

    private String LOG_TAG = GetFlickrJSONData.class.getSimpleName();
    private List<Photo> mPhoto;
    private Uri mDestinationUri;

    public void execute() {
        DownloadJSONData downloadJSONData = new DownloadJSONData();
        downloadJSONData.execute(mDestinationUri.toString());
    }

    public void processResult() {
        if(getmDownloadStatus() != DownloadStatus.OK){
            Log.e(LOG_TAG, "Error downloading raw file");
            return;
        }

        final String FLICKR_PHOTO = "photo";
        final String FLICKR_ID = "id";

        try {
            JSONObject jsonData = new JSONObject();
            JSONArray itemsArray = jsonData.getJSONArray(FLICKR_PHOTO);
            for (int i = 0; i<itemsArray.length(); i++){
                JSONObject jsonPhoto = itemsArray.getJSONObject(i);
                String id = jsonPhoto.getString(FLICKR_ID);


                Photo photoObject = new Photo(id);
                this.mPhoto.add(photoObject);

            }

            for(Photo singlePhoto: mPhoto) {
                Log.v(LOG_TAG, singlePhoto.toString());
            }


        } catch(JSONException jsone) {
            jsone.printStackTrace();
            Log.e(LOG_TAG, "Error processing JSON data");
        }


    }


    public class DownloadJSONData extends DownloadRawData {
        protected void onPostExecute(String webData) {
            super.onPostExecute(webData);
            processResult();



        }

        protected String doInBackground(String...params){
            return super.doInBackground(params);
        }

    }

}


