package za.co.aws.welfare.dataObjects;

/** Used during a search for pets. */
public class PetSearchData {

    private int mID;
    private int mAnimalTypeID;
    private String mPetName;
    private String mPetDOB;
    private String mPetWelfareID;

    public PetSearchData(int mID, int mAnimalTypeID, String mPetName, String mPetDOB, String mPetWelfareID) {
        this.mID = mID;
        this.mAnimalTypeID = mAnimalTypeID;
        this.mPetName = mPetName;
        this.mPetDOB = mPetDOB;
        this.mPetWelfareID = mPetWelfareID;
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

    public String getPetWelfareID() {
        return mPetWelfareID;
    }
}
