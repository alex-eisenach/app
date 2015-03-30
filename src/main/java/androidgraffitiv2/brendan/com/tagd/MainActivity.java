package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.googlecode.flickrjandroid.Transport;
import com.googlecode.flickrjandroid.photos.geo.GeoInterface;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


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
    private String latStr;
    private String lonStr;
    private Float latFloat;
    private Float lonFloat;
    private GeoInterface geoInterface;

    private String apiKey = "1ae9506f05e76f22f7e7d89b5277cd75";
    //jsonLint
    //https://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=1ae9506f05e76f22f7e7d89b5277cd75&user_id=132191189@N03&format=json&nojsoncallback=1
    private String sharedSecret = "30540280f392b674";
    private Transport transport;

    //represents the instance on custom
    // view that was added to layout
    private DrawingView drawView;

    static final int RES_CODE_SWITCHER = 99;

    private static final int GPS_ERRORDIALOG_REQUEST = 9001;
    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button)findViewById(R.id.button_camera);
        Button opencvButton = (Button)findViewById(R.id.opencv_btn);
        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        opencvButton.setOnClickListener(galleryListener);

        if (!OpenCVLoader.initDebug()) {}

        //OpenCV Additions
        imgMAT = new Mat();
        imgMASK = new Mat();
        imgCANNY = new Mat();
        ksize = new Size(3,3);

        if (servicesOK()){
            //fragment of map
            //setContentView(R.layout.map_activity);

            if (initMap()){
                Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
                //gotoLocation(BOULDER_LAT, BOULDER_LNG, DEFAULTZOOM);
                mMap.setMyLocationEnabled(true);
                onMapReady(mMap);

            }
            else {
                Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
            }

        }
        else {
            setContentView(R.layout.activity_main);
        }

    }

    //Testing Google Play Services for map
    public boolean servicesOK(){
        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS){
            return true;
        }

        else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)){
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        }

        else {
            Toast.makeText(this, "Can't connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    private boolean initMap(){
        if (mMap == null){
            SupportMapFragment mapFrag =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }


    //adding markers
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(new LatLng(40.0274, -105.2519))
                .title("Hello world"));


  /*          map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                    .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                    .position(new LatLng(latFloat, lonFloat)));*/


    }

    private OnClickListener cameraListener = new OnClickListener () {
        public void onClick(View v) {
            takePhoto(v);            //takes in view v
        }
    };

    private OnClickListener galleryListener = new OnClickListener () {
        public void onClick (View v) {
            Intent gallerySwitcher = new Intent();
            gallerySwitcher.setType("image/*");
            gallerySwitcher.setAction(Intent.ACTION_GET_CONTENT);
            gallerySwitcher.addCategory(Intent.CATEGORY_OPENABLE);
            File chosenPhoto = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"picture.jpg");
            chosenImageUri = Uri.fromFile(chosenPhoto);
            gallerySwitcher.putExtra(MediaStore.EXTRA_OUTPUT, chosenImageUri);
            startActivityForResult(gallerySwitcher, RES_CODE_SWITCHER);
        }
    };

    private void takePhoto(View v){
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
        if(resultCode== Activity.RESULT_OK && requestCode == TAKE_PICTURE) {

            Uri selectedImage = imageUri;
            str = selectedImage.toString();

            //notify other applications of your content, keeps everyone on the same page
            getContentResolver().notifyChange(selectedImage, null);
            //ContentResolver cr = getContentResolver();


            Intent switchActivity = new Intent(this, TestActivity.class);
            switchActivity.putExtra("selectedImage", str);
            startActivity(switchActivity);

//            Bitmap bitmap;
//
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(cr,selectedImage);
//
//                Utils.bitmapToMat(bitmap, imgMAT);
//
//                // Convert to greyscale via cvtColor
//                Imgproc.cvtColor(imgMAT, imgMASK, Imgproc.COLOR_RGB2GRAY);
//
//                // Blur it
//                Imgproc.blur(imgMASK, imgMASK, ksize);
//
//                // Canny edge detection & drawing
//                Imgproc.Canny(imgMASK, imgCANNY, 20, 60);
//
//                // Show the Canny Edge detector image
//                Utils.matToBitmap(imgCANNY, bitmap);
//
//
//
//
//                imageView.setImageBitmap(bitmap);
//                //Toast.makeText(MainActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();
//            }
//
//            catch(Exception e) {
//                Log.e(logtag, e.toString());
//            }

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
