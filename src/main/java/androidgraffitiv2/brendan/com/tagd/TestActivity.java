package androidgraffitiv2.brendan.com.tagd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
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

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
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
    private Bundle extras;
    private ImageButton currPaint;
    private Canvas picCanvas;
    private Drawable picDrawable;
    private Context instance;
    private float smallBrush, mediumBrush, largeBrush;
    private ImageButton drawBtn, eraseBtn, newBtn, saveBtn;
    public ArrayList<parseJson> geoData;


    //represents the instance on custom
    // view that was added to layout
    private DrawingView drawView;

    //CHANGE so i can upload


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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


        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, pathUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ImageView imageView = (ImageView) findViewById(R.id.image_camera);


        //drawView.setImageBitmap(bitmap);
        //Toast.makeText(this, pathUri.toString(), Toast.LENGTH_LONG).show();



        picDrawable = new BitmapDrawable(this.getResources(), bitmap);
        System.out.println("PICPATH:   " + picpath);
        //picDrawable = loadImageFromWeb(picpath);


        drawView = (DrawingView)findViewById(R.id.drawing);



        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        //get first button and store it as instance variable
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        //show current selected color
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        drawView.setBackground(picDrawable);

        saveBtn = (ImageButton)findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);


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
            //draw button clicked
        }

        else if(view.getId()==R.id.save_btn){
            //save drawing
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("Save drawing");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    //save drawing
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();

            //write image to a file
            //insertImage method to attempt to write the image to the media
            // store for images on the device, which should save it to the user gallery
            //drawView.setBackground(null);
            // POST PHOTO TEST CODE
            authPost(bitmap);
            String imgSaved = MediaStore.Images.Media.insertImage(
                    getContentResolver(), drawView.getDrawingCache(),
                    UUID.randomUUID().toString()+".png", "drawing");



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

            drawView.destroyDrawingCache();

        }
    }

    public void paintClicked(View view){
        //use chosen color
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

    public void authPost(Bitmap photo) {

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

}
