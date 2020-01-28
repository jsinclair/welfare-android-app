package za.co.aws.welfare.dataObjects;

import android.os.Parcel;
import android.os.Parcelable;

public class ReminderData implements Parcelable {

    private int mID;
    private String mDate;
    private String mAnimalNames;

    public ReminderData(int mID, String mDate, String mAnimalNames) {
        this.mID = mID;
        this.mDate = mDate;
        this.mAnimalNames = mAnimalNames;
    }

    public int getID() {
        return mID;
    }

    public String getDate() {
        return mDate;
    }

    public String getAnimalNames() {
        return mAnimalNames;
    }

    /** ---------------------- PARCEL AND COMPARABLE STUFF HERE ---------------------- */
    public ReminderData(Parcel in) {
        this.mID = in.readInt();
        this.mAnimalNames = in.readString();
        this.mDate = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mAnimalNames);
        dest.writeString(mDate);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ReminderData createFromParcel(Parcel in) {
            return new ReminderData(in);
        }

        public ReminderData[] newArray(int size) {
            return new ReminderData[size];
        }
    };
}
