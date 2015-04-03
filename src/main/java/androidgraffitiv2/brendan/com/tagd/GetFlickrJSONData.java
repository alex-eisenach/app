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

    public GetFlickrJSONData(String searchCriteria, boolean matchAll) {
        super(null);
        createAndUpdateUri(searchCriteria, matchAll);
    }

    public boolean createAndUpdateUri(String searchCriteria, boolean matchAll){
        final String FLICKR_API_BASE_URL = "    //https://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=1ae9506f05e76f22f7e7d89b5277cd75&user_id=132191189@N03&format=json&nojsoncallback=1";
        final String PHOTO_EXTRAS = "extras";
        /*final String NO_JSON_CALLBACK_PARAM = "nojsoncallback";

        mDestinationUri = Uri.parse(FLICKR_API_BASE_URL).buildUpon()
                .appendQueryParameter()*/

        mDestinationUri = Uri.parse(FLICKR_API_BASE_URL).buildUpon()
                .build();

        return mDestinationUri != null;
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


