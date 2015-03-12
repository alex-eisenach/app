package androidgraffitiv2.brendan.com.vg_v2;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.IOException;


public class TestActivity extends Activity {

    //public Uri imageUri;
    private Mat imgMAT;
    private Mat imgMASK;
    private Mat imgCANNY;
    private Size ksize;
    public Bitmap bitmap;
    private Bundle extras;
    private ImageButton currPaint;

    //represents the instance on custom
    // view that was added to layout
    private DrawingView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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


        imgMAT = new Mat();
        imgMASK = new Mat();
        imgCANNY = new Mat();
        ksize = new Size(3, 3);



        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, pathUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageView imageView = (ImageView) findViewById(R.id.image_camera);


        imageView.setImageBitmap(bitmap);
        Toast.makeText(this, pathUri.toString(), Toast.LENGTH_LONG).show();

        drawView = (DrawingView)findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        //get first button and store it as instance variable
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        //show current selected color
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));


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
}
