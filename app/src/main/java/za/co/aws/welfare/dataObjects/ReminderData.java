package za.co.aws.welfare.dataObjects;

public class ReminderData {

    private int mID;
    private String mDate;
    private String mAnimalNames;

    public ReminderData(int mID, String mDate, String mAnimalNames) {
        this.mID = mID;
        this.mDate = mDate;
        this.mAnimalNames = mAnimalNames;
    }

    public int getmID() {
        return mID;
    }

    public String getmDate() {
        return mDate;
    }

    public String getmAnimalNames() {
        return mAnimalNames;
    }
}
