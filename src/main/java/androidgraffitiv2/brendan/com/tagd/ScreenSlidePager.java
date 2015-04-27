package androidgraffitiv2.brendan.com.tagd;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import android.view.View;


import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Eisenach on 4/19/2015.
 */
public class ScreenSlidePager extends FragmentActivity {

    int NUM_PAGES;
    int numPics;
    private ViewPager mPager;
    ScreenSlidePagerAdapter mPagerAdapter;


    public ArrayList<parseJson> geoData;
    public String sourceURL = "";
    public String maskURL = "";

    public final static int LAT_TAG = 0;
    public final static int LON_TAG = 1;
    public final static int ID_TAG = 2;
    public final static int FARM_TAG = 3;
    public final static int SERVER_TAG = 4;
    public final static int SECRET_TAG = 5;
    public final static int TITLE_TAG = 6;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_slide);
        List<Fragment> fragments = getFragments();
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(true, new DepthPageTransformer());
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), fragments);
        mPager.setAdapter(mPagerAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<Fragment>();

        authRequest();
        ArrayList<String> idData = prefHandler(ID_TAG);
        ArrayList<String> farmData = prefHandler(FARM_TAG);
        ArrayList<String> serverData = prefHandler(SERVER_TAG);
        ArrayList<String> secretData = prefHandler(SECRET_TAG);
        ArrayList<String> titleData = prefHandler(TITLE_TAG);

        System.out.println("titledata = " + titleData);
        int loopcounter = 0;

        for (int i = 0; i < idData.size(); i++) {

            String parseTitle = titleData.get(i);
            String[] titleMask = parseTitle.split("_");
            System.out.println("titleMASK = " + titleMask[1]);

            if (titleMask[1].equals("BASE")) {
                sourceURL = createSourceUrl(idData.get(i), serverData.get(i), farmData.get(i), secretData.get(i));
                for (int a = 0; a < idData.size(); a++) {
                    String[] loopParse = titleData.get(a).split("_");
                    if (loopParse[0].equals(titleMask[0]) && a != i) {
                        loopcounter++;
                        maskURL = createSourceUrl(idData.get(a), serverData.get(a), farmData.get(a), secretData.get(a));
                        System.out.println("MASKURL:  " + maskURL);
                        System.out.println("BASEURL:  " + sourceURL);
                        fList.add(SlidePageFragment.newInstance(sourceURL, maskURL));

                    }
                }
            }
            else if (titleMask[1].equals("BASEMASK")) {
                continue;
            }


            else {

                System.out.println("MASKURL = NULL");
            }

            System.out.println("MASKURL:  " + maskURL);
        }
        return fList;
    }


    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }


        @Override
        public Fragment getItem(int position) {


            return this.fragments.get(position);

        }

        @Override
        public int getCount() {

            /*Bundle extras = new Bundle();

            ContentResolver cr = getContentResolver();

            extras = getIntent().getExtras();
            String picpath = extras.getString("numpics");

            numPics = Integer.decode(picpath);
            System.out.println("NUMPICS:  " + numPics);*/

            return this.fragments.size();
        }



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
                        editor.putString("ser"+String.valueOf(i), geoData.get(i).getServer());
                        editor.putString("sec"+String.valueOf(i), geoData.get(i).getSecret());
                        editor.putString("far"+String.valueOf(i), geoData.get(i).getFarm());
                        editor.putString("tit"+String.valueOf(i), geoData.get(i).getTitle());

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
        String strSaved = sharedPreferences.getString("far0", "");
        //int intSaved = sharedPreferences.getInt("id0", 0);
        System.out.println("SAVER:  " + strSaved);
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
        ArrayList<String> titleHolder = new ArrayList<>();

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
            serverHolder.add(i, sharedPreferences.getString("ser" + String.valueOf(i), ""));
            secretHolder.add(i, sharedPreferences.getString("sec" + String.valueOf(i), ""));
            farmHolder.add(i, sharedPreferences.getString("far" + String.valueOf(i), ""));
            titleHolder.add(i, sharedPreferences.getString("tit" + String.valueOf(i), ""));
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

        if (dataselector == TITLE_TAG) {
            return titleHolder;
        }

        else {
            return null;
        }
    }



}