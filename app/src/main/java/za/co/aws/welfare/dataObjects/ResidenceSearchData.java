package za.co.aws.welfare.dataObjects;

/** A read only data object to contain results of a Residence search. */
public class ResidenceSearchData {

    private int mID;
    private String mShackID;
    private String mStreetAddress;
    private String mLat, mLon;
    private String mAnimalNames;
    private String mResidentName;
    private String mResidentID;
    private String mResidentTel;

    public ResidenceSearchData(int mID, String mShackID, String mStreetAddress, String name,
                               String id, String tel, String mLat, String mLon, String animalNames) {
        this.mID = mID;
        this.mShackID = mShackID;
        this.mStreetAddress = mStreetAddress;
        this.mLat = mLat;
        this.mLon = mLon;
        this.mAnimalNames = animalNames;
        this.mResidentName = name;
        this.mResidentID = id;
        this.mResidentTel = tel;
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

    public String getAnimalNames() {
        return mAnimalNames;
    }

    public String getResidentName() {
        return mResidentName;
    }

    public String getResidentID() {
        return mResidentID;
    }

    public String getResidentTel() {
        return mResidentTel;
    }
}
