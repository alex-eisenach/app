package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

//added for button functionality


public class TestActivity extends Activity implements OnClickListener{

    //public Uri imageUri;
    private Mat imgMAT;
    private Mat imgMASK;
    private Mat imgCANNY;
    private Size ksize;
    public Bitmap bitmap;
    public Bitmap bitmap2;
    private Bundle extras;
    private ImageButton currPaint;
    private Canvas picCanvas;
    private Drawable picDrawable;
    private Context instance;
    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton drawBtn, eraseBtn, newBtn, saveBtn;
    public ArrayList<parseJson> geoData;
    public File photoFile;
    public Uri imageUri;


    //represents the instance on custom
    // view that was added to layout
    private DrawingView drawView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // DrawingView stuff
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        TestActivity actinstance = new TestActivity();

        Bundle extras = new Bundle();

        ContentResolver cr = getContentResolver();

        extras = getIntent().getExtras();
        String picpath = new String();
    if (savedInstanceState == null) {
        if (extras == null) {
            picpath = null;
        } else {
            picpath = extras.getString("selectedImage");
        }
    }
    else{
            picpath = (String) savedInstanceState.getSerializable("selectedImage");
        }

        Uri pathUri = Uri.parse(picpath);
        photoFile = new File(pathUri.getPath());


        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, pathUri);
            bitmap2 = BitmapFactory.decodeFile(pathUri.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ImageView imageView = (ImageView) findViewById(R.id.image_camera);


        //drawView.setImageBitmap(bitmap);
        //Toast.makeText(this, pathUri.toString(), Toast.LENGTH_LONG).show();



        picDrawable = new BitmapDrawable(this.getResources(), bitmap2);
        System.out.println("PICPATH:   " + picpath);
        //picDrawable = loadImageFromWeb(picpath);

        drawView = (DrawingView)findViewById(R.id.drawing);
        drawView.setBackground(picDrawable);
        drawView.setBrushSize(mediumBrush);


        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        //get first button and store it as instance variable
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        drawBtn = (ImageButton)findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
        eraseBtn.setOnClickListener(this);
        //show current selected color
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        newBtn = (ImageButton)findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test, menu);
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

    @Override
    public void onClick(View view){
        //respond to clicks
        drawView.setDrawingCacheEnabled(true);
        if(view.getId()==R.id.draw_btn){

            final Dialog brushDialog = new Dialog(this);
            brushDialog.setTitle("Brush size:");
            brushDialog.setContentView(R.layout.brush_chooser);
            ImageButton smallBtn = (ImageButton) brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(smallBrush);
                    drawView.setLastBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });

            ImageButton mediumBtn = (ImageButton) brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(mediumBrush);
                    drawView.setLastBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });

            ImageButton largeBtn = (ImageButton) brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawView.setErase(false);
                    drawView.setBrushSize(largeBrush);
                    drawView.setLastBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();
        }

            else if(view.getId()==R.id.erase_btn) {

                final Dialog brushDialog = new Dialog(this);
                brushDialog.setTitle("Eraser size:");
                brushDialog.setContentView(R.layout.brush_chooser);

            ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
            smallBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(smallBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
            mediumBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(mediumBrush);
                    brushDialog.dismiss();
                }
            });
            ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
            largeBtn.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    drawView.setErase(true);
                    drawView.setBrushSize(largeBrush);
                    brushDialog.dismiss();
                }
            });

            brushDialog.show();

        }

            else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Upload");
            saveDialog.setMessage("Push drawing to Flickr?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){

                    File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), photoFile.getName()+"MASK");


                    try {
                        Double latTest = 44.542111;
                        Double lonTest = -105.062444;

                        //LatLng geoDatar = new LatLng(latTest, lonTest);
                        LatLng geoDatar = getLocation();
                        System.out.println("geoDatar:   " + geoDatar.toString());
                        Boolean bool = setGeoTag(photoFile, geoDatar);

                        //Build File object to save drawing Cache into

                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(photo);
                            drawView.getDrawingCache().compress(Bitmap.CompressFormat.JPEG, 30, out);
                            System.out.println("JPEG Compression complete");
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (out != null) {
                                    out.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Boolean bool2 = setGeoTag(photo, geoDatar);


                        System.out.println("AbsPath:  " + photoFile.getAbsolutePath());
                        System.out.println("GetPath:  " + photoFile.getPath());
                        System.out.println("BOOL:  " + bool);

                        ExifInterface exif = new ExifInterface(photoFile.getPath());

                        String exifLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                        String exifLon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                        String exifStr = exif.getAttribute(ExifInterface.TAG_MODEL);
                        String exifSize = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                        String exifLatRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                        String exifLonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                        String exifFocal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);

                        System.out.println("EXIFSTR:  " + exifStr);
                        System.out.println("EXIFLAT:  " + exifLat);
                        System.out.println("EXIFLON:  " + exifLon);
                        System.out.println("EXIFSIZE:  " + exifSize);
                        System.out.println("EXIFLATREF:  " + exifLatRef);
                        System.out.println("EXIFLONREF:  " + exifLonRef);
                        System.out.println("EXIFFOCAL:  " + exifFocal);

                    } catch (IOException e) { System.out.println("IO Exception"); }

                    authPost(photoFile);
                    authPost(photo);

                    Toast saveToast = Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT);
                    saveToast.show();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                    // Save Stuff
                    /*
                    String imgSaved = MediaStore.Images.Media.insertImage(
                            getContentResolver(), drawView.getDrawingCache(),
                            UUID.randomUUID().toString()+".jpeg", "drawing");




                    if(imgSaved!=null){
                        Toast savedToast = Toast.makeText(getApplicationContext(),
                                "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                        savedToast.show();
                    }
                    else{
                        Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                        unsavedToast.show();
                    }
                    */
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();

            drawView.destroyDrawingCache();

        }

        else if(view.getId()==R.id.new_btn) {
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("New drawing");
            newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    drawView.startNew();
                    dialog.dismiss();
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }
    }

    public void paintClicked(View view){
        //use chosen color
        drawView.setErase(false);
        drawView.setBrushSize(drawView.getLastBrushSize());

        if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();

            drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;


        }

    }

    public LatLng getLocation() {

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            LatLng locdata = new LatLng(latitude, longitude);
            return locdata;
        } catch (Exception e) {
            Toast.makeText(this, "Couldn't get Location", Toast.LENGTH_LONG).show();
            System.out.println("LocationManager Error");
            return null;
        }

    }

    public static Drawable loadImageFromWeb(String url)
    {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            System.out.println("URL Value:  " + url.toString());
            return Drawable.createFromStream(is, "src name");
        } catch (Exception e) {
            System.out.println("INPUTSTREAM ERROR");
            return null;
        }
    }

    public void authPost(File photo) {

        //File photoObj = new File(filepath);

        RestClient client = RestApplication.getRestClient();
        client.postPhoto(photo, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
            System.out.println("UPLOADRESPONSE");
                try {
                    System.out.write(response);
                } catch (IOException b ) { System.out.println("WeirdError1"); }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                System.out.println("HTTP 4XX Status Error");
                System.out.println("Status Code:  " + statusCode);
                try {
                    System.out.write(errorResponse);
                } catch (IOException v ) {System.out.println("WeirdError"); }
            }

        });


    }

    static public boolean setGeoTag(File image, LatLng geoTag) {
        if (geoTag != null) {
            try {
                ExifInterface exif = new ExifInterface(
                        image.getAbsolutePath());

                double latitude = Math.abs(geoTag.latitude);
                double longitude = Math.abs(geoTag.longitude);

                int num1Lat = (int) Math.floor(latitude);
                int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
                double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

                int num1Lon = (int) Math.floor(longitude);
                int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
                double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;

                String lat = num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000";
                String lon = num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000";

                if (geoTag.latitude > 0) {
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                } else {
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                }

                if (geoTag.longitude > 0) {
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                } else {
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                }

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lat);
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lon);
                System.out.println("latlat:  " + lat);
                System.out.println("lonlon:  " + lon);

                exif.saveAttributes();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

}
