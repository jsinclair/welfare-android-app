package za.co.aws.welfare.dataObjects;

import android.os.Parcel;
import android.os.Parcelable;


/** A read only data object to contain results of a Residence search. */
public class ResidenceSearchData implements Parcelable {

    private final int mID;
    private final String mShackID;
    private final String mStreetAddress;
    private final String mLat, mLon;
    private final String mAnimalNames;
    private final String mResidentName;
    private final String mResidentID;
    private final String mResidentTel;
    private final String mAnimalsSterilisedStat;

    public ResidenceSearchData(int mID, String mShackID, String mStreetAddress, String name,
                               String id, String tel, String mLat, String mLon, String animalNames, String animalsSterilisedStat) {
        this.mID = mID;
        this.mShackID = mShackID;
        this.mStreetAddress = mStreetAddress;
        this.mLat = mLat;
        this.mLon = mLon;
        this.mAnimalNames = animalNames;
        this.mResidentName = name;
        this.mResidentID = id;
        this.mResidentTel = tel;
        this.mAnimalsSterilisedStat = animalsSterilisedStat;
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

    public String getAllAnimalsSterilised() {
        return mAnimalsSterilisedStat;
    }

    /** ---------------------- PARCEL AND COMPARABLE STUFF HERE ---------------------- */
    public ResidenceSearchData(Parcel in) {
        this.mID = in.readInt();
        this.mShackID = in.readString();
        this.mStreetAddress = in.readString();
        this.mLat = in.readString();
        this.mLon = in.readString();
        this.mAnimalNames = in.readString();
        this.mResidentName = in.readString();
        this.mResidentID = in.readString();
        this.mResidentTel = in.readString();
        this.mAnimalsSterilisedStat = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mShackID);
        dest.writeString(mStreetAddress);
        dest.writeString(mLat);
        dest.writeString(mLon);
        dest.writeString(mAnimalNames);
        dest.writeString(mResidentName);
        dest.writeString(mResidentID);
        dest.writeString(mResidentTel);
        dest.writeString(mAnimalsSterilisedStat);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ResidenceSearchData createFromParcel(Parcel in) {
            return new ResidenceSearchData(in);
        }

        public ResidenceSearchData[] newArray(int size) {
            return new ResidenceSearchData[size];
        }
    };
}
