package androidgraffitiv2.brendan.com.tagd;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

//import android.provider.ContactsContract;

/**
 * Created by Brendan on 4/27/15.
 */


final class GridViewAdapter extends BaseAdapter {
    private final Context context;
    private ArrayList<String> urls = new ArrayList<String>();

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

    public GridViewAdapter(Context context, ArrayList<String> urls) {
        this.context = context;
        this.urls = urls;
        //this.items = items;

        // Ensure we get a different ordering of images on each run.
        //Collections.addAll(urls, Data.);
        //Collections.shuffle(urls);

        // Triple up the list.
        ArrayList<String> copy = new ArrayList<String>(urls);
        urls.addAll(copy);
        urls.addAll(copy);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //view.setLayoutParams(new GridView.LayoutParams(100, 100));
        }

        // Get the image URL for the current position.
        String url = getItem(position);
        //Button button = (Button) convertView.findViewById(R.id.grid_view);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                        //.placeholder(R.drawable.placeholder) //
                        //.error(R.drawable.error) //
                .fit() //
                .tag(context) //
                .into(view);

        return view;
    }

    @Override
    public int getCount() {
        return urls.size();
    }

    @Override
    public String getItem(int position) {
        return urls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
