package androidgraffitiv2.brendan.com.tagd;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Alex Eisenach on 4/19/2015.
 */
public class SlidePageFragment extends Fragment {

    ImageView imageView;
    String imagePath;
    public ArrayList<parseJson> geoData;
    public String sourceURL = "";
    public static String EXTRA_MESSAGE = "one";
    public static String MASK_MESSAGE = "two";

    public final static int LAT_TAG = 0;
    public final static int LON_TAG = 1;
    public final static int ID_TAG = 2;
    public final static int FARM_TAG = 3;
    public final static int SERVER_TAG = 4;
    public final static int SECRET_TAG = 5;


    public static final SlidePageFragment newInstance(String message, String maskedmessage) {
        SlidePageFragment f = new SlidePageFragment();
        Bundle bdl = new Bundle(1);
        bdl.putString(EXTRA_MESSAGE, message);
        bdl.putString(MASK_MESSAGE, maskedmessage);
        System.out.println("EXTRA_MESSAGE:  " + message);
        System.out.println("MASK_MESSAGE:  " + maskedmessage);
        f.setArguments(bdl);
        return f;
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context c = getActivity().getApplicationContext();
        String message = getArguments().getString(EXTRA_MESSAGE);

        //ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_slide, container, false);
        View v = inflater.inflate(R.layout.fragment_slide, container, false);
        Button swipeButton = (Button) v.findViewById(R.id.button_here);
        imageView = (ImageView) v.findViewById(R.id.image);

        System.out.println("SOURCEURL:   " + message);
        Picasso.with(c).load(message).into(imageView);

        swipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("CLICK!");
                String maskMessage = getArguments().getString(MASK_MESSAGE);
                System.out.println("clickmask :" + maskMessage);
                if (maskMessage != null) {
                    Context c = getActivity().getApplicationContext();
                    //ImageView imageView = (ImageView) v.findViewById(R.id.image);

                    Picasso.with(c).load(maskMessage).into(imageView);

                }
            }


        });






        //rootView.addView(v);


        //String value = getArguments().getString("key");


        //String downString = getActivity().getIntent().getExtras().getString("selectedImage");




        return v;
    }

    public void setImage(ImageView image, String msg) {
        Context c = getActivity().getApplicationContext();
        Picasso.with(c).load(msg).into(image);
    }

    public void authRequest() {
        RestClient client = RestApplication.getRestClient();
        client.getPhotoGeo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonArray) {
                //System.out.println(jsonArray);
                //Log.d("DEBUG", "timeline: " + jsonArray.toString());


                try {

                    //Code to filter through the levels of objects & arrays in raw flickr data
                    JSONObject topObj = jsonArray.getJSONObject("photos");
                    JSONArray photoArray = topObj.getJSONArray("photo");

                    //Printouts for debugging
                    //System.out.println("tobObj raw printout:  " + topObj);
                    //System.out.println("photoArray raw printout:  " + photoArray);
                    //System.out.println("photoArray printout:  " + photoArray.getJSONObject(1));
                    //System.out.println("photoArray latitutde printout:  " + photoArray.getJSONObject(1).getString("latitude"));
                    //System.out.println("photoArray Length:  " + photoArray.length());

                    //Build the geoData object
                    geoData = parseJson.fromJson(photoArray);

                    //Set up the shared preferences stuff
                    SharedPreferences mSettings = getActivity().getSharedPreferences("pref",Context.MODE_PRIVATE);
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

                    }

                    editor.apply();

                    //System.out.println("geoData:  " + geoData.get(1).getID());


                } catch (JSONException j) {
                    Log.i("Panda", "Panda");
                }


            }

        });

        //Endpoint stuff to prototype sharedpref functionality
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("pref",Context.MODE_PRIVATE);
        String strSaved = sharedPreferences.getString("far0", "");
        //int intSaved = sharedPreferences.getInt("id0", 0);
        //System.out.println("SAVER:  " + strSaved);
        //Toast.makeText(this, strSaved, Toast.LENGTH_LONG).show();

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

        //sharedpref stuff
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("pref",Context.MODE_PRIVATE);
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

        else {
            return null;
        }
    }


}
