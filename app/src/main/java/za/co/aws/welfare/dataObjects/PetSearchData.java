package za.co.aws.welfare.dataObjects;

/** Used during a search for pets. */
public class PetSearchData {

    private int mID;
    private int mAnimalTypeID;
    private String mAnimalTypeDesc;
    private String mPetName;
    private String mPetDOB;

    public PetSearchData(int mID, int mAnimalTypeID, String mAnimalTypeDesc, String mPetName, String mPetDOB) {
        this.mID = mID;
        this.mAnimalTypeID = mAnimalTypeID;
        this.mAnimalTypeDesc = mAnimalTypeDesc;
        this.mPetName = mPetName;
        this.mPetDOB = mPetDOB;
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
}
