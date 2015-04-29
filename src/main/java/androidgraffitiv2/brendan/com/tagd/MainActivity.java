package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.googlecode.flickrjandroid.Transport;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends Activity {

    private static int TAKE_PICTURE = 1;
    public Uri imageUri;
    private String str;
    private Bitmap bitmap;

    private String apiKey = "1ae9506f05e76f22f7e7d89b5277cd75";
    //jsonLint
    //https://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=1ae9506f05e76f22f7e7d89b5277cd75&user_id=132191189@N03&format=json&nojsoncallback=1
    private String sharedSecret = "30540280f392b674";
    private Transport transport;

    static final int RES_CODE_SWITCHER = 99;

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    GoogleMap mMap;

    public String sourceURL = "";
    public String maskURL = "";
    public ArrayList<String> URLarray;

    public final static int LAT_TAG = 0;
    public final static int LON_TAG = 1;
    public final static int ID_TAG = 2;
    public final static int FARM_TAG = 3;
    public final static int SERVER_TAG = 4;
    public final static int SECRET_TAG = 5;
    public final static int TITLE_TAG = 6;

    public ArrayList<parseJson> geoData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button mapButton = (Button) findViewById(R.id.map_btn);
        Button swipeButton = (Button) findViewById(R.id.swipeButton);
        Button gridButton = (Button) findViewById(R.id.gridButton);

        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        mapButton.setOnClickListener(mapListener);
        swipeButton.setOnClickListener(swipeListener);
        gridButton.setOnClickListener(gridListener);

        //function here to build URLarray
        ArrayList<String> URLtemp = new ArrayList<>();
        URLarray = buildURLArray(URLtemp);

        System.out.println("URLarray:  " + URLarray);

        GridView gv = (GridView) findViewById(R.id.grid_view);
        gv.setAdapter(new GridViewAdapter(this, URLarray));
        gv.setOnScrollListener(new ScrollListener(this));

    }


    /*public void setOnClickListener (OnClickListener l) {

        ArrayList<String> idData = prefHandler(ID_TAG);
        ArrayList<String> farmData = prefHandler(FARM_TAG);
        ArrayList<String> serverData = prefHandler(SERVER_TAG);
        ArrayList<String> secretData = prefHandler(SECRET_TAG);
        ArrayList<String> titleData = prefHandler(TITLE_TAG);

        String tracker = marker.getTitle();
        int index = 0;
        for (int a = 0; a<idData.size(); a++) {
            if (idData.get(a).equals(tracker)) {
                index = a;
                break;
            } else {
                index = -1;
            }
        }

        System.out.println("INDEX:  " + index);
        System.out.println("TRACKER: " + tracker);
        System.out.println("IDDATA:  " + idData.get(index));

        sourceURL = createSourceUrl(idData.get(index), serverData.get(index), farmData.get(index), secretData.get(index));

        goToPhoto(sourceURL);

        return true;

    }*/



    private void goToPhoto(String filepath)
    {
        Intent switchActivity = new Intent(this, PhotoView.class);
        switchActivity.putExtra("selectedImage", filepath);
        startActivity(switchActivity);
    }



    public ArrayList<String> buildURLArray(ArrayList<String> urlList) {

        authRequest();

        ArrayList<String> idData = prefHandler(ID_TAG);
        ArrayList<String> farmData = prefHandler(FARM_TAG);
        ArrayList<String> serverData = prefHandler(SERVER_TAG);
        ArrayList<String> secretData = prefHandler(SECRET_TAG);
        ArrayList<String> titleData = prefHandler(TITLE_TAG);

        System.out.println("titledata = " + titleData);


        for (int i = 0; i < idData.size(); i++) {

            String parseTitle = titleData.get(i);
            String[] titleMask = parseTitle.split("_");
            //System.out.println("titleMASK = " + titleMask[1]);

            if (titleMask[1].equals("BASE")) {
                String tempURL = createSourceUrl(idData.get(i), serverData.get(i), farmData.get(i), secretData.get(i));
                urlList.add(tempURL);
            }
            else if (titleMask[1].equals("BASEMASK")) {
                continue;
            }


            else {

                System.out.println("MASKURL = NULL");
            }

            //System.out.println("MASKURL:  " + maskURL);

        }

        return urlList;

    }

    //authrequest and prefhandler
    public void authRequest() {
        RestClient client = RestApplication.getRestClient();
        client.getPhotoGeo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonArray) {
                System.out.println(jsonArray);
                Log.d("DEBUG", "timeline: " + jsonArray.toString());


                try {

                    //Code to filter through the levels of objects & arrays in raw flickr data
                    JSONObject topObj = jsonArray.getJSONObject("photos");
                    JSONArray photoArray = topObj.getJSONArray("photo");

                    //Printouts for debugging
                    System.out.println("tobObj raw printout:  " + topObj);
                    System.out.println("photoArray raw printout:  " + photoArray);
                    System.out.println("photoArray printout:  " + photoArray.getJSONObject(1));
                    System.out.println("photoArray latitutde printout:  " + photoArray.getJSONObject(1).getString("latitude"));
                    System.out.println("photoArray Length:  " + photoArray.length());

                    //Build the geoData object
                    geoData = parseJson.fromJson(photoArray);

                    //Set up the shared preferences stuff
                    SharedPreferences mSettings = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSettings.edit();

                    //Record length of the array so end-user knows how many pictures are coming in
                    editor.putInt("arrayLength", photoArray.length());

                    //Loop to dump relevant data into the sharedpref's - all in one commit/apply
                    for (int i=0; i<photoArray.length(); i++) {
                        editor.putString("lat"+String.valueOf(i), geoData.get(i).getLatitude());
                        editor.putString("lon"+String.valueOf(i), geoData.get(i).getLongitude());
                        editor.putString("id"+String.valueOf(i), geoData.get(i).getID());
                        editor.putString("ser"+String.valueOf(i), geoData.get(i).getServer());
                        editor.putString("sec"+String.valueOf(i), geoData.get(i).getSecret());
                        editor.putString("far"+String.valueOf(i), geoData.get(i).getFarm());
                        editor.putString("tit"+String.valueOf(i), geoData.get(i).getTitle());
                    }

                    editor.apply();

                    System.out.println("geoData:  " + geoData.get(1).getTitle());


                } catch (JSONException j) {
                    Log.i("Panda", "Panda");
                }


            }

        });

        //Endpoint stuff to prototype sharedpref functionality
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String strSaved = sharedPreferences.getString("tit0", "");
        //int intSaved = sharedPreferences.getInt("id0", 0);
        System.out.println("SAVER:  " + strSaved);
        Toast.makeText(this, strSaved, Toast.LENGTH_LONG).show();

    }

    public String createSourceUrl(String id, String server, String farm, String secret)
    {

        String output = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + ".jpg";
        return output;
    }

    public ArrayList<String> prefHandler(int dataselector) {

        //declare the output arraylist
        ArrayList<String> latHolder = new ArrayList<>();
        ArrayList<String> lonHolder = new ArrayList<>();
        ArrayList<String> idHolder = new ArrayList<>();
        ArrayList<String> serverHolder = new ArrayList<>();
        ArrayList<String> secretHolder = new ArrayList<>();
        ArrayList<String> farmHolder = new ArrayList<>();
        ArrayList<String> titleHolder = new ArrayList<>();

        //sharedpref stuff
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        //retrieve the number of pictures coming in for data
        int numPics = sharedPreferences.getInt("arrayLength", 0);
        //loop to store the sharedpref stuff into array
        for (int i = 0; i < numPics; i++) {
            //populate lat/lon/id holder arrays from shared preferences in a loop
            latHolder.add(i, sharedPreferences.getString("lat" + String.valueOf(i), ""));
            lonHolder.add(i, sharedPreferences.getString("lon" + String.valueOf(i), ""));
            idHolder.add(i, sharedPreferences.getString("id" + String.valueOf(i), ""));
            serverHolder.add(i, sharedPreferences.getString("ser" + String.valueOf(i), ""));
            secretHolder.add(i, sharedPreferences.getString("sec" + String.valueOf(i), ""));
            farmHolder.add(i, sharedPreferences.getString("far" + String.valueOf(i), ""));
            titleHolder.add(i, sharedPreferences.getString("tit" + String.valueOf(i), ""));
        }

        if (dataselector == LAT_TAG) {
            return latHolder;
        }
        if (dataselector == LON_TAG) {
            return lonHolder;
        }
        if (dataselector == ID_TAG) {
            return idHolder;
        }

        if (dataselector == SERVER_TAG) {
            return serverHolder;
        }
        if (dataselector == SECRET_TAG) {
            return secretHolder;
        }
        if (dataselector == FARM_TAG) {
            return farmHolder;
        }
        if (dataselector == TITLE_TAG) {
            return titleHolder;
        }

        else {
            return null;
        }
    }

    private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);            //takes in view v

        }
    };

    private OnClickListener mapListener = new OnClickListener() {
        public void onClick(View v) {
            goToMap();
        }
    };

    private OnClickListener swipeListener = new OnClickListener() {
        public void onClick(View v) {
            goToSwipe();
        }
    };

    private OnClickListener gridListener = new OnClickListener() {
        public void onClick(View v) {
            goToGrid();
        }
    };

    private void goToGrid(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void goToSwipe() {
        Intent intent = new Intent(this, ScreenSlidePager.class);
        intent.putExtra("numpics", "10");
        startActivity(intent);

    }

    private void goToMap() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);

    }

    private void takePhoto(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), UUID.randomUUID().toString()+"_BASE");
        System.out.println("filepath:  " + photo.getAbsolutePath());
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //make sure user hit OK button
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) {

            str = imageUri.toString();

            Intent switchActivity = new Intent(this, TestActivity.class);
            switchActivity.putExtra("selectedImage", str);
            startActivity(switchActivity);

        }

        if (requestCode == RES_CODE_SWITCHER && resultCode == Activity.RESULT_OK) {

            try {
                if (bitmap != null) {
                    bitmap.recycle();
                }

                InputStream stream = getContentResolver().openInputStream(intent.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                Uri chosenImage = getImageUri(this, bitmap);
                str = chosenImage.toString();
                getContentResolver().notifyChange(chosenImage, null);


            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException err) {
                err.printStackTrace();
            }

            Intent switchActivity = new Intent(this, TestActivity.class);
            switchActivity.putExtra("selectedImage", str);
            startActivity(switchActivity);


            super.onActivityResult(requestCode, resultCode, intent);


        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}
