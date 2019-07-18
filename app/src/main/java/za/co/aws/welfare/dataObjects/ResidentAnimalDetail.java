package za.co.aws.welfare.dataObjects;

/**
 * Data object used in the Resident activity, to correctly display animals in that resident AND
 * to help the user navigate to that animal's view/edit page.
 */
public class ResidentAnimalDetail {

    private int mID;
    private String mName;
    private String mWelfareNumber;

    public ResidentAnimalDetail(int mID, String mName, String mWelfareNumber) {
        this.mID = mID;
        this.mName = mName;
        this.mWelfareNumber = mWelfareNumber;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public String getWelfareNumber() {
        return mWelfareNumber;
    }
}
