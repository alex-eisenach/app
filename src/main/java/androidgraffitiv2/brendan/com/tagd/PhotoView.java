package androidgraffitiv2.brendan.com.tagd;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback.EmptyCallback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class PhotoView extends Activity {

    ImageView imageView;
    ProgressBar progressBar;
    private static int TAKE_PICTURE = 1;
    public Uri imageUri;
    private String str;
    private Bitmap bitmap;

    static final int RES_CODE_SWITCHER = 99;

    String currentUrl = "https://farm9.staticflickr.com/8750/16979603541_fc705dca46.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);            //sets content of PhotoView to XML activity_photo

        imageView = (ImageView) findViewById(R.id.image);

        Button cameraButton = (Button) findViewById(R.id.button_camera);
        Button mapButton = (Button) findViewById(R.id.map_btn);
        Button swipeButton = (Button) findViewById(R.id.swipeButton);
        Button gridButton = (Button) findViewById(R.id.gridButton);

        //on click listener
        cameraButton.setOnClickListener(cameraListener);
        mapButton.setOnClickListener(mapListener);
        swipeButton.setOnClickListener(swipeListener);
        gridButton.setOnClickListener(gridListener);

        //All the code here is to retrieve the imageURL from MainActivity
        Bundle extras = new Bundle();
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
        //Done retrieving the URL string, saved to picpath, now loading from Picasso

        Picasso.with(this)
                .load(picpath)
                .into(imageView, new EmptyCallback() {
                    @Override public void onSuccess() {
                        //progressBar.setVisibility(View.GONE);
                        System.out.println("I am in:  " + "On Success");

                    }
                    @Override
                    public void onError() {
                        //progressBar.setVisibility(View.GONE);
                        System.out.println("I am in:  " + "On ERROR");
                    }
                });

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

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    /*private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    File file = new File(Environment.getExternalStorageDirectory().getPath() +"/actress_wallpaper.jpg");
                    try
                    {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(CompressFormat.JPEG, 75, ostream);
                        ostream.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }
            }).start();
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if (placeHolderDrawable != null) {
            }
        }
    };*/

}