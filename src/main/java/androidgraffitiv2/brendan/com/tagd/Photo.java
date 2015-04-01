package androidgraffitiv2.brendan.com.tagd;

/**
 * Created by Brendan on 4/1/15.
 */
public class Photo {
    private String mID;

    public Photo(String mID) {
        this.mID = mID;
    }

    public String getmID() {
        return mID;
    }

    @Override
    public String toString() {
        return "Photo{" +
                "mID='" + mID + '\'' +
                '}';
    }
}
