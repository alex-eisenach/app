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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googlecode.flickrjandroid.Transport;
import com.googlecode.flickrjandroid.photos.GeoData;
import com.googlecode.flickrjandroid.photos.geo.GeoInterface;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private static String logtag = "VG2App";
    private static int TAKE_PICTURE = 1;
    public Uri imageUri;
    public Uri chosenImageUri;
    private Mat imgMAT;
    private Mat imgMASK;
    private Mat imgCANNY;
    private Size ksize;
    private String str;
    private ImageButton currPaint;
    private Bitmap bitmap;
    public String latStr;
    public String lonStr;
    private Float latFloat;
    private Float lonFloat;
    private GeoInterface geoInterface;
    public ArrayList<parseJson> geoData;

    public List<String> latArray = new ArrayList<String>();
    public List<String> lonArray = new ArrayList<String>();

    public final static int LAT_TAG = 0;
    public final static int LON_TAG = 1;
    public final static int ID_TAG = 2;

    private String apiKey = "1ae9506f05e76f22f7e7d89b5277cd75";
    //jsonLint
    //https://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=1ae9506f05e76f22f7e7d89b5277cd75&user_id=132191189@N03&format=json&nojsoncallback=1
    private String sharedSecret = "30540280f392b674";
    private Transport transport;

    //represents the instance on custom
    //view that was added to layout
    private DrawingView drawView;

    static final int RES_CODE_SWITCHER = 99;

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button opencvButton = (Button) findViewById(R.id.opencv_btn);
        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        opencvButton.setOnClickListener(galleryListener);


        if (servicesOK()) {
            //fragment of map
            //setContentView(R.layout.map_activity);


            //CHANGE so i can upload


            if (initMap()) {
                //Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();

                //gotoLocation(BOULDER_LAT, BOULDER_LNG, DEFAULTZOOM);
                mMap.setMyLocationEnabled(true);
                onMapReady(mMap);

            } else {
                //Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
            }

        } else {
            setContentView(R.layout.activity_main);
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

        authRequest();

        //call prefHandler to grab the data from the Flickr pulldown
        ArrayList<String> latData = prefHandler(LAT_TAG);
        ArrayList<String> lonData = prefHandler(LON_TAG);
        ArrayList<String> idData = prefHandler(ID_TAG);

        //printouts for test
        System.out.println("latData for DropPins:  " + latData);
        System.out.println("lonData for DropPins:  " + lonData);
        System.out.println("idData for DropPins:  " + idData);


        // Drop some markers
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(new LatLng(40.0274, -105.2519))
                .title("Hello world"));

        // Loop & drop pins like dey hot
        for (int i = 0; i < idData.size(); i++) {

            double latDouble = Double.valueOf(latData.get(i));
            double lonDouble = Double.valueOf(lonData.get(i));
            double idDouble = Double.valueOf(idData.get(i));

            map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .position(new LatLng(latDouble, lonDouble))
                    .title("ID: " + String.valueOf(idDouble)));

        }


    }

    private OnClickListener cameraListener = new OnClickListener() {
        public void onClick(View v) {
            takePhoto(v);            //takes in view v
        }
    };

    private OnClickListener galleryListener = new OnClickListener() {
        public void onClick(View v) {
            Intent gallerySwitcher = new Intent();
            gallerySwitcher.setType("image/*");
            gallerySwitcher.setAction(Intent.ACTION_GET_CONTENT);
            gallerySwitcher.addCategory(Intent.CATEGORY_OPENABLE);
            File chosenPhoto = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
            chosenImageUri = Uri.fromFile(chosenPhoto);
            gallerySwitcher.putExtra(MediaStore.EXTRA_OUTPUT, chosenImageUri);
            startActivityForResult(gallerySwitcher, RES_CODE_SWITCHER);
        }
    };

    private void takePhoto(View v) {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpg");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //make sure user hit OK button
        if (resultCode == Activity.RESULT_OK && requestCode == TAKE_PICTURE) {

            Uri selectedImage = imageUri;
            str = selectedImage.toString();

            //notify other applications of your content, keeps everyone on the same page
            getContentResolver().notifyChange(selectedImage, null);
            //ContentResolver cr = getContentResolver();


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
                    }

                    editor.apply();

                    System.out.println("geoData:  " + geoData.get(1).getID());


                } catch (JSONException j) {
                    Log.i("Panda", "Panda");
                }


            }

        });

        //Endpoint stuff to prototype sharedpref functionality
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String strSaved = sharedPreferences.getString("id0", "");
        //int intSaved = sharedPreferences.getInt("id0", 0);
        System.out.println("SAVER:  " + strSaved);
        Toast.makeText(this, strSaved, Toast.LENGTH_LONG).show();

    }

    public ArrayList<String> prefHandler(int dataselector) {

        //declare the output arraylist
        ArrayList<String> latHolder = new ArrayList<>();
        ArrayList<String> lonHolder = new ArrayList<>();
        ArrayList<String> idHolder = new ArrayList<>();

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
        }

        if (dataselector == LAT_TAG) {
            return latHolder;
        }
        if (dataselector == LON_TAG) {
            return lonHolder;
        }
        if (dataselector == ID_TAG) {
            return idHolder;
        } else {
            return null;
        }
    }

}
