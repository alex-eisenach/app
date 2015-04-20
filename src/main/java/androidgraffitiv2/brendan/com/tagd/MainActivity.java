package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
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
import com.google.android.gms.maps.model.Marker;
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
import java.net.URL;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button mapButton = (Button) findViewById(R.id.map_btn);
        Button swipeButton = (Button) findViewById(R.id.swipeButton);
        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        mapButton.setOnClickListener(mapListener);
        swipeButton.setOnClickListener(swipeListener);


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
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "picture.jpeg");
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
