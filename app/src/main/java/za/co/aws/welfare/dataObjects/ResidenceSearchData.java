package za.co.aws.welfare.dataObjects;

/** A read only data object to contain results of a Residence search. */
public class ResidenceSearchData {

    private int mID;
    private String mShackID;
    private String mStreetAddress;
    private String mLat, mLon;
    private int mDistance;

    public ResidenceSearchData(int mID, String mShackID, String mStreetAddress, String mLat, String mLon, int mDistance) {
        this.mID = mID;
        this.mShackID = mShackID;
        this.mStreetAddress = mStreetAddress;
        this.mLat = mLat;
        this.mLon = mLon;
        this.mDistance = mDistance;
    }

    public int getID() {
        return mID;
    }

    public String getShackID() {
        return mShackID;
    }

    public String getStreetAddress() {
        return mStreetAddress;
    }

    public String getLat() {
        return mLat;
    }

    public String getLon() {
        return mLon;
    }

    public int getDistance() {
        return mDistance;
    }
}
