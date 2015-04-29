package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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


/**
 * Created by Alex Eisenach on 4/19/2015.
 */
public class MapActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener{


    public ArrayList<parseJson> geoData;
    public String sourceURL = "";

    private static int TAKE_PICTURE = 1;
    public Uri imageUri;
    private String str;
    private Bitmap bitmap;

    static final int RES_CODE_SWITCHER = 99;

    public final static int LAT_TAG = 0;
    public final static int LON_TAG = 1;
    public final static int ID_TAG = 2;
    public final static int FARM_TAG = 3;
    public final static int SERVER_TAG = 4;
    public final static int SECRET_TAG = 5;
    public final static int TITLE_TAG = 6;


    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button mapButton = (Button) findViewById(R.id.map_btnMAP);
        Button swipeButton = (Button) findViewById(R.id.swipeButtonMAP);
        Button gridButton = (Button) findViewById(R.id.gridButtonMAP);

        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        mapButton.setOnClickListener(mapListener);
        swipeButton.setOnClickListener(swipeListener);
        gridButton.setOnClickListener(gridListener);

        if (servicesOK()) {


            if (initMap()) {
                //Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                //gotoLocation(BOULDER_LAT, BOULDER_LNG, DEFAULTZOOM);
                mMap.setMyLocationEnabled(true);
                onMapReady(mMap);

            } else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
            }

        } else {
            setContentView(R.layout.activity_main);
        }

    }

    private View.OnClickListener cameraListener = new View.OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);            //takes in view v
        }
    };

    private View.OnClickListener mapListener = new View.OnClickListener() {
        public void onClick(View v) {
            goToMap();
        }
    };

    private View.OnClickListener swipeListener = new View.OnClickListener() {
        public void onClick(View v) {
            goToSwipe();
        }
    };

    private View.OnClickListener gridListener = new View.OnClickListener() {
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


    //Testing Google Play Services for map
    public boolean servicesOK() {
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }


    //adding markers
    public void onMapReady(GoogleMap map) {

        map.setOnMarkerClickListener(this );
        authRequest();

        //call prefHandler to grab the data from the Flickr pulldown
        ArrayList<String> latData = prefHandler(LAT_TAG);
        ArrayList<String> lonData = prefHandler(LON_TAG);
        ArrayList<String> idData = prefHandler(ID_TAG);
        ArrayList<String> titleData = prefHandler(TITLE_TAG);

        //printouts for test
        System.out.println("latData for DropPins:  " + latData);
        System.out.println("lonData for DropPins:  " + lonData);
        System.out.println("idData for DropPins:  " + idData);

        System.out.println("titleData for Drop Pins:  " + titleData);

        // Drop some markers
        map.addMarker(new MarkerOptions()
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(new LatLng(40.0274, -105.2519)));
        //.title("Hello world"));

        // Loop & drop pins like dey hot
        for (int i = 0; i < idData.size(); i++) {

            String[] loopParse = titleData.get(i).split("_");

            if (loopParse[1].equals("BASE")) {

                double latDouble = Double.valueOf(latData.get(i));
                double lonDouble = Double.valueOf(lonData.get(i));
                String idStr = idData.get(i);
                System.out.println("idSTR:  " + idStr);

                Marker marker = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .position(new LatLng(latDouble, lonDouble))
                        .title(idStr));

            }

            else { continue; }


        }
    }

    public boolean onMarkerClick(Marker marker) {

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

    }

    private void goToPhoto(String filepath)
    {
        Intent switchActivity = new Intent(this, PhotoView.class);
        switchActivity.putExtra("selectedImage", filepath);
        startActivity(switchActivity);
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
                    System.out.println("photoArray title printout:  " + photoArray.getJSONObject(1).getString("title"));
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

                    System.out.println("geoData:  " + geoData.get(1).getID());


                } catch (JSONException j) {
                    Log.i("Panda", "Panda");
                }


            }

        });


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
        System.out.println("TEST" + sharedPreferences.getString("tit" + String.valueOf(0),""));
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
            System.out.println("titleHolder:   " + titleHolder);
            return titleHolder;
        }

        else {
            return null;
        }
    }


}
