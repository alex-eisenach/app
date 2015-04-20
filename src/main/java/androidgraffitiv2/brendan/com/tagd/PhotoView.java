package androidgraffitiv2.brendan.com.tagd;


import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback.EmptyCallback;
import com.squareup.picasso.Picasso;

public class PhotoView extends Activity {

    ImageView imageView;
    ProgressBar progressBar;

    String currentUrl = "https://farm9.staticflickr.com/8750/16979603541_fc705dca46.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);            //sets content of PhotoView to XML activity_photo

        imageView = (ImageView) findViewById(R.id.image);

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