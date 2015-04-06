package androidgraffitiv2.brendan.com.tagd;

/**
 * Created by Brendan on 4/5/15.
 */
import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;

/*
 *
 * This is the object responsible for communicating with a REST API.
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes:
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 *
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 *
 */
public class RestClient extends OAuthBaseClient {

    public static final Class<? extends Api> REST_API_CLASS = FlickrApi.class; // Change this
    public static final String REST_URL = "http://api.flickr.com/services/rest"; // Change this, base API URL
    public static final String REST_CONSUMER_KEY = "1ae9506f05e76f22f7e7d89b5277cd75";       // Change this
    public static final String REST_CONSUMER_SECRET = "30540280f392b674"; // Change this
    public static final String REST_CALLBACK_URL = "oauth://tagd"; // Change this (here and in manifest)


    public RestClient(Context context) {
        super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);

    }

    // CHANGE THIS
    // DEFINE METHODS for different API endpoints here
    public void getPhotoGeo(JsonHttpResponseHandler handler) {
        String apiUrl = getApiUrl("?nojsoncallback=1&method=flickr.people.getPublicPhotos&extras=geo");
        // Can specify query string params directly or through RequestParams.
        RequestParams params = new RequestParams();
        //params.put("format", "json");
        getClient().get(apiUrl, params, handler);
    }

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */
}
