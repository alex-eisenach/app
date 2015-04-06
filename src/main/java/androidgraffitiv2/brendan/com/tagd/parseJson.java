package androidgraffitiv2.brendan.com.tagd;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * This is a temporary, sample model that demonstrates the basic structure
 * of a SQLite persisted Model object. Check out the ActiveAndroid wiki for more details:
 * https://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model
 * 
 */
@Table(name = "items")
public class parseJson extends Model {
    // Define table fields
    @Column(name = "id")
    private String id;
    @Column(name = "latitude")
    private  String latitude;
    @Column(name = "longitude")
    private String longitude;

    public parseJson() {
        super();

    }

    // Parse model from JSON
    public parseJson(JSONObject object){
        super();

        try {
            this.id = object.getString("id");
            this.latitude = object.getString("latitude");
            this.longitude = object.getString("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Getters
    public String getID() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    // Record Finders
    public static parseJson byId(long id) {
        return new Select().from(parseJson.class).where("id = ?", id).executeSingle();
    }

    public static List<parseJson> recentItems() {
        return new Select().from(parseJson.class).orderBy("id DESC").limit("300").execute();
    }

    public static ArrayList<parseJson> fromJson(JSONArray jsonArray) {
        ArrayList<parseJson> parseArray = new ArrayList<parseJson>(jsonArray.length());

        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject geoJson = null;
            try {
                geoJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            parseJson geo = new parseJson(geoJson);
            geo.save();
            parseArray.add(geo);
        }

        return parseArray;
    }
}


