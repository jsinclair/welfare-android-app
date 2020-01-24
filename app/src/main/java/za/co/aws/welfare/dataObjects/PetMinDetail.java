package za.co.aws.welfare.dataObjects;

/**
 * Data object used in the Resident activity, to correctly display animals in that resident AND
 * to help the user navigate to that animal's view/edit page.
 */
public class PetMinDetail {

    private int mID;
    private int mSterilised;
    private String mName;


    public PetMinDetail(int mID, String mName, int sterilised) {
        this.mID = mID;
        this.mName = mName;
        this.mSterilised = sterilised;
    }

    public int getID() {
        return mID;
    }

    public String getName() {
        return mName;
    }

    public int getSterilised() {
        return mSterilised;
    }

}
