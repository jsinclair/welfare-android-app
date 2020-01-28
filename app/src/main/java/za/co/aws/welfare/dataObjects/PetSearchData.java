package za.co.aws.welfare.dataObjects;

import android.os.Parcel;
import android.os.Parcelable;

/** Used during a search for pets. */
public class PetSearchData implements Parcelable {

    private int mID;
    private int mAnimalTypeID;
    private String mAnimalTypeDesc;
    private String mPetName;
    private String mPetDOB;
    private String mGender;
    private int mSterilised;

    public PetSearchData(int mID, int mAnimalTypeID, String mAnimalTypeDesc, String mPetName, String mPetDOB, String gender, int sterilised) {
        this.mID = mID;
        this.mAnimalTypeID = mAnimalTypeID;
        this.mAnimalTypeDesc = mAnimalTypeDesc;
        this.mPetName = mPetName;
        this.mPetDOB = mPetDOB;
        this.mGender = gender;
        this.mSterilised = sterilised;
    }

    public int getID() {
        return mID;
    }

    public int getAnimalTypeID() {
        return mAnimalTypeID;
    }

    public String getPetName() {
        return mPetName;
    }

    public String getPetDOB() {
        return mPetDOB;
    }

    public String getmAnimalTypeDesc() {
        return mAnimalTypeDesc;
    }

    public String getGender() {
        return mGender;
    }

    public int isSterilised() {
        return mSterilised;
    }

    /** ---------------------- PARCEL AND COMPARABLE STUFF HERE ---------------------- */
    public PetSearchData(Parcel in) {
        this.mID = in.readInt();
        this.mAnimalTypeID = in.readInt();
        this.mAnimalTypeDesc = in.readString();
        this.mPetName = in.readString();
        this.mPetDOB = in.readString();
        this.mGender = in.readString();
        this.mSterilised = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeInt(mAnimalTypeID);
        dest.writeString(mAnimalTypeDesc);
        dest.writeString(mPetName);
        dest.writeString(mPetDOB);
        dest.writeString(mGender);
        dest.writeInt(mSterilised);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PetSearchData createFromParcel(Parcel in) {
            return new PetSearchData(in);
        }

        public PetSearchData[] newArray(int size) {
            return new PetSearchData[size];
        }
    };
}
