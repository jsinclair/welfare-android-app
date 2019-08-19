package za.co.aws.welfare.dataObjects;

/**
 * Data object used in the Resident activity, to correctly display animals in that resident AND
 * to help the user navigate to that animal's view/edit page.
 */
public class ResidentAnimalDetail {

    private int mID;
    private String mName;

    public ResidentAnimalDetail(int mID, String mName) {
        this.mID = mID;
        this.mName = mName;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

}
