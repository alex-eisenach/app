package androidgraffitiv2.brendan.com.vg_v2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import org.opencv.android.Utils;
import org.opencv.android.LoaderCallbackInterface;

//OpenCV Imports
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;



import java.io.File;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends ActionBarActivity {

    private static String logtag = "VG2App";
    private static int TAKE_PICTURE = 1;
    private Uri imageUri;
    private Mat imgMAT;
    private Mat imgMASK;
    private Mat imgCANNY;
    private Size ksize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button cameraButton = (Button)findViewById(R.id.button_camera);
        //on click listener
        cameraButton.setOnClickListener(cameraListener);

        if (!OpenCVLoader.initDebug()) {}

        //OpenCV Additions
        imgMAT = new Mat();
        imgMASK = new Mat();
        imgCANNY = new Mat();
        ksize = new Size(3,3);

    }

    private OnClickListener cameraListener = new OnClickListener () {
        public void onClick(View v) {
            takePhoto(v);            //takes in view v
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
        if(resultCode== Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            //notify other applications of your content, keeps everyone on the same page
            getContentResolver().notifyChange(selectedImage, null);

            ImageView imageView = (ImageView)findViewById(R.id.image_camera);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr,selectedImage);

                Utils.bitmapToMat(bitmap, imgMAT);

                // Convert to greyscale via cvtColor
                Imgproc.cvtColor(imgMAT, imgMASK, Imgproc.COLOR_RGB2GRAY);

                // Blur it
                Imgproc.blur(imgMASK, imgMASK, ksize);

                // Canny edge detection & drawing
                Imgproc.Canny(imgMASK, imgCANNY, 20, 60);

                // Show the Canny Edge detector image
                Utils.matToBitmap(imgCANNY, bitmap);
                imageView.setImageBitmap(bitmap);
                Toast.makeText(MainActivity.this, selectedImage.toString(), Toast.LENGTH_LONG).show();
            }

            catch(Exception e) {
                Log.e(logtag, e.toString());
            }

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
}
